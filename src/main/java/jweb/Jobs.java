package jweb;

import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Background work: the in-process executors inherited from the framework —
 * {@code run}, {@code submit}, {@code track}, {@code delay}, {@code schedule} —
 * and, from here on, <b>durable jobs</b>: named, with a payload, stored,
 * retried with backoff, dead-lettered after the last attempt, and surviving a
 * restart when the store is the database.
 *
 * <pre>
 * record Welcome(long userId) {}
 *
 * // once, at startup: what a job named "welcome" does
 * Jobs.handle("welcome", Welcome.class, w -&gt; Mail.to(users.get(w.userId()).email())
 *     .subject("Welcome").text("Hello!").send());
 *
 * // anywhere: enqueue one — returns at once, the worker runs it
 * Jobs.enqueue("welcome", new Welcome(42));
 * Jobs.enqueue("digest", new Digest(42), Jobs.options().after(Duration.ofHours(1)).retries(5));
 * Jobs.enqueue("reindex", null, Jobs.options().key("reindex"));   // one pending at a time
 * </pre>
 *
 * <p><b>Stores.</b> {@link Memory} (the default) keeps jobs in this JVM — a
 * restart loses the pending ones. {@link SqlStore} keeps them in the
 * {@code jweb_jobs} table and is installed automatically when {@link Sql} is
 * connected; instances sharing the table share the queue, each claiming a
 * job with an atomic update, so one job runs once. Any {@link Store} plugs in
 * with {@link #store(Store)}.</p>
 *
 * <p><b>The worker</b> starts with the application ({@link #start()} by hand):
 * it polls the store ({@code jweb.jobs.poll}, default 1s), runs due jobs on
 * virtual threads ({@code jweb.jobs.concurrency}, default 8), and on an
 * exception schedules the next attempt at 2, 4, 8… seconds (capped at an
 * hour) until the retries ({@link Options#retries}, default 3) are used up —
 * then the job is {@link Status#FAILED} with the error, kept for inspection
 * ({@link #failed()}, {@link #retry(String)}). A job still {@code RUNNING}
 * after {@code jweb.jobs.timeout} (default 10 minutes; a worker died) is
 * released to run again. Finished jobs are purged after
 * {@code jweb.jobs.keep-done} (default 24 hours).</p>
 *
 * <p>In a test, {@link #runPending()} runs what is due on the calling thread
 * and returns how many ran.</p>
 */
@SuppressWarnings("deprecation")
public class Jobs extends com.osmig.Jweb.framework.async.Jobs {

    protected Jobs() {}

    // ==================== the model ====================

    /** Where a job is in its life. */
    public enum Status { PENDING, RUNNING, DONE, FAILED }

    /** One stored job. */
    public record Job(String id, String name, String payload, Status status, int attempts, int maxAttempts,
                      Instant runAt, Instant createdAt, Instant lockedAt, String lockedBy, String error, String key) {

        /** The payload parsed as the type. */
        public <T> T payload(Class<T> type) {
            return payload == null ? null : Json.parse(payload, type);
        }

        Job with(Status status, int attempts, Instant runAt, Instant lockedAt, String lockedBy, String error) {
            return new Job(id, name, payload, status, attempts, maxAttempts, runAt, createdAt, lockedAt, lockedBy, error, key);
        }
    }

    /** How to enqueue: when, how many attempts, a uniqueness key. */
    public static final class Options {
        private Instant runAt;
        private int retries = 3;
        private String key;

        Options() {}

        /** Run no earlier than this. */
        public Options at(Instant when) {
            this.runAt = when;
            return this;
        }

        /** Run after a delay. */
        public Options after(Duration delay) {
            this.runAt = Instant.now().plus(delay);
            return this;
        }

        /** Attempts after the first before the job is failed (default 3; 0 = no retry). */
        public Options retries(int retries) {
            this.retries = Math.max(0, retries);
            return this;
        }

        /** At most one pending or running job with this key: enqueueing again returns the existing one. */
        public Options key(String key) {
            this.key = key;
            return this;
        }
    }

    public static Options options() {
        return new Options();
    }

    // ==================== handlers ====================

    private static final Map<String, Handler> HANDLERS = new ConcurrentHashMap<>();

    private record Handler(Class<?> type, Consumer<Object> run) {}

    /** What a job with this name does; the payload arrives parsed as the type. */
    @SuppressWarnings("unchecked")
    public static <T> void handle(String name, Class<T> payloadType, Consumer<? super T> handler) {
        HANDLERS.put(name, new Handler(payloadType, (Consumer<Object>) handler));
    }

    /** What a job with this name does, when it carries no payload. */
    public static void handle(String name, Runnable handler) {
        HANDLERS.put(name, new Handler(null, payload -> handler.run()));
    }

    /** Whether a handler is registered for the name. */
    public static boolean handles(String name) {
        return HANDLERS.containsKey(name);
    }

    // ==================== enqueueing ====================

    /** Stores a job to run as soon as a worker is free. */
    public static Job enqueue(String name, Object payload) {
        return enqueue(name, payload, options());
    }

    /** Stores a job with options; the stored job (or, with a key, the one already pending). */
    public static Job enqueue(String name, Object payload, Options options) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("job name is blank");
        if (!HANDLERS.containsKey(name)) {
            Log.warn("Enqueued job '{}' has no handler on this instance; another instance must handle it", name);
        }
        Store store = store();
        if (options.key != null) {
            Optional<Job> existing = store.findByKey(options.key);
            if (existing.isPresent()) return existing.get();
        }
        Instant now = Instant.now();
        Job job = new Job(UUID.randomUUID().toString(), name, payload == null ? null : Json.stringify(payload),
            Status.PENDING, 0, options.retries + 1, options.runAt == null ? now : options.runAt, now,
            null, null, null, options.key);
        Job stored = store.add(job);
        Metrics.counter("jweb.jobs.enqueued").increment();
        return stored;
    }

    // ==================== looking ====================

    public static Optional<Job> find(String id) {
        return store().find(id);
    }

    /** Jobs waiting to run, soonest first. */
    public static List<Job> pending() {
        return store().list(Status.PENDING, 100);
    }

    /** Jobs that used up their attempts, with the last error. */
    public static List<Job> failed() {
        return store().list(Status.FAILED, 100);
    }

    public static List<Job> running() {
        return store().list(Status.RUNNING, 100);
    }

    /** Puts a failed job back in the queue with its attempts reset. */
    public static boolean retry(String id) {
        Optional<Job> job = store().find(id);
        if (job.isEmpty() || job.get().status() != Status.FAILED) return false;
        store().update(job.get().with(Status.PENDING, 0, Instant.now(), null, null, null));
        return true;
    }

    public static boolean delete(String id) {
        return store().delete(id);
    }

    /** Counts by status. */
    public static Map<Status, Long> stats() {
        return store().counts();
    }

    // ==================== the store ====================

    /** Where jobs live. Every method must be safe to call from several threads and instances. */
    public interface Store {
        Job add(Job job);

        Optional<Job> find(String id);

        /** The pending or running job with this key, if any. */
        Optional<Job> findByKey(String key);

        /** Writes the job back as it now is. */
        void update(Job job);

        /**
         * Writes a run's outcome, but only while {@code worker} still holds the
         * job: one released as stale (its worker looked dead) and claimed again
         * keeps the new worker's row, and the late result is dropped. Whether
         * the write landed. Stores that cannot check this just {@link #update}.
         */
        default boolean finish(Job job, String worker) {
            update(job);
            return true;
        }

        /**
         * The next due job, moved to {@code RUNNING} for this worker in one
         * atomic step — two workers never claim the same one.
         */
        Optional<Job> claim(String worker, Instant now);

        List<Job> list(Status status, int limit);

        Map<Status, Long> counts();

        boolean delete(String id);

        /** Running jobs locked before the instant go back to pending (their worker died); how many. */
        int releaseStale(Instant lockedBefore);

        /** Removes done jobs finished before the instant; how many. */
        int purgeDone(Instant before);
    }

    private static volatile Store store = new Memory();

    /** Installs the store. */
    public static void store(Store next) {
        store = next;
    }

    public static Store store() {
        return store;
    }

    // ==================== the worker ====================

    private static final String WORKER = "worker-" + UUID.randomUUID().toString().substring(0, 8);
    private static final ScheduledExecutorService POLLER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "jweb-jobs");
        t.setDaemon(true);
        return t;
    });
    private static final ExecutorService RUNNERS = Executors.newVirtualThreadPerTaskExecutor();
    private static volatile ScheduledFuture<?> polling;
    private static volatile Duration pollInterval = Duration.ofSeconds(1);
    private static volatile Duration runningTimeout = Duration.ofMinutes(10);
    private static volatile Duration keepDone = Duration.ofHours(24);
    private static volatile Semaphore slots = new Semaphore(8);
    private static final Object START_LOCK = new Object();

    /** Starts polling the store (idempotent). The application does this at startup. */
    public static void start() {
        synchronized (START_LOCK) {
            if (polling != null) return;
            polling = POLLER.scheduleWithFixedDelay(Jobs::tick, 0, pollInterval.toMillis(), TimeUnit.MILLISECONDS);
            Log.info("jweb.Jobs worker {} started: {} store, poll every {}", WORKER, store.getClass().getSimpleName(), pollInterval);
        }
    }

    /** Stops polling; running jobs finish. */
    public static void stop() {
        synchronized (START_LOCK) {
            if (polling != null) polling.cancel(false);
            polling = null;
        }
    }

    /** Whether the worker is polling. */
    public static boolean started() {
        return polling != null;
    }

    /** Poll interval, running timeout, how long done jobs are kept, and how many run at once. */
    public static void configure(Duration poll, Duration timeout, Duration keep, int concurrency) {
        pollInterval = poll;
        runningTimeout = timeout;
        keepDone = keep;
        slots = new Semaphore(Math.max(1, concurrency));
    }

    private static long lastHousekeeping;

    private static void tick() {
        try {
            Instant now = Instant.now();
            if (now.toEpochMilli() - lastHousekeeping > 60_000) {
                lastHousekeeping = now.toEpochMilli();
                int released = store.releaseStale(now.minus(runningTimeout));
                if (released > 0) Log.warn("Released {} job(s) whose worker stopped answering", released);
                store.purgeDone(now.minus(keepDone));
            }
            Semaphore permits = slots;   // the one acquired from is the one released to, even if configure() swaps it meanwhile
            while (permits.tryAcquire()) {
                Optional<Job> claimed = store.claim(WORKER, now);
                if (claimed.isEmpty()) {
                    permits.release();
                    break;
                }
                Job job = claimed.get();
                RUNNERS.execute(() -> {
                    try {
                        run(job);
                    } finally {
                        permits.release();
                    }
                });
            }
        } catch (Throwable e) {
            // anything escaping here would silently cancel every future tick of the scheduled poller
            Log.error("jweb.Jobs poll failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs every job that is due, on this thread, and returns how many ran —
     * what a test calls instead of waiting for the poller.
     */
    public static int runPending() {
        int ran = 0;
        Instant now = Instant.now();
        while (true) {
            Optional<Job> claimed = store.claim(WORKER, now);
            if (claimed.isEmpty()) return ran;
            run(claimed.get());
            ran++;
        }
    }

    private static void run(Job job) {
        Handler handler = HANDLERS.get(job.name());
        int attempt = job.attempts() + 1;
        long started = System.nanoTime();
        try (Trace.Span span = Trace.span("jweb.job", "job", job.name())) {
            span.tag("job.id", job.id()).tag("job.attempt", attempt);
            try {
                if (handler == null) {
                    throw new IllegalStateException("No handler registered for job '" + job.name() + "'");
                }
                Object payload = handler.type() == null || job.payload() == null ? null : Json.parse(job.payload(), handler.type());
                handler.run().accept(payload);
                finish(job.with(Status.DONE, attempt, job.runAt(), Instant.now(), job.lockedBy(), null), job.lockedBy());
                Metrics.counter("jweb.jobs.done").increment();
            } catch (Throwable e) {
                span.error(e);
                throw e;
            }
        } catch (Throwable e) {
            String error = e.getClass().getSimpleName() + ": " + e.getMessage();
            if (attempt < job.maxAttempts()) {
                long seconds = Math.min(3600, 1L << Math.min(attempt, 12));
                Instant next = Instant.now().plusSeconds(seconds);
                finish(job.with(Status.PENDING, attempt, next, null, null, error), job.lockedBy());
                Log.warn("Job '{}' ({}) failed on attempt {}/{}: {} — retrying in {}s",
                    job.name(), job.id(), attempt, job.maxAttempts(), error, seconds);
                Metrics.counter("jweb.jobs.retried").increment();
            } else {
                finish(job.with(Status.FAILED, attempt, job.runAt(), Instant.now(), job.lockedBy(), error), job.lockedBy());
                Log.error("Job '{}' ({}) failed for good after {} attempt(s): {}", job.name(), job.id(), attempt, error);
                Metrics.counter("jweb.jobs.failed").increment();
            }
        } finally {
            Metrics.timer("jweb.jobs.run").record((System.nanoTime() - started) / 1_000_000);
        }
    }

    private static void finish(Job outcome, String worker) {
        if (!store.finish(outcome, worker)) {
            Log.warn("Job '{}' ({}) finished after its lock had been released to another worker (it ran past jweb.jobs.timeout);"
                + " this run's outcome is discarded", outcome.name(), outcome.id());
        }
    }

    /** What a released job's row says about why it is pending again. */
    static final String RELEASED = "Released: its worker stopped answering (crashed or killed mid-run)";

    // ==================== the in-memory store ====================

    /** Jobs in this JVM: fine for one instance that may lose what is pending on restart. */
    public static final class Memory implements Store {

        private final Map<String, Job> jobs = new ConcurrentHashMap<>();

        @Override
        public Job add(Job job) {
            jobs.put(job.id(), job);
            return job;
        }

        @Override
        public Optional<Job> find(String id) {
            return Optional.ofNullable(jobs.get(id));
        }

        @Override
        public Optional<Job> findByKey(String key) {
            return jobs.values().stream()
                .filter(j -> key.equals(j.key()) && (j.status() == Status.PENDING || j.status() == Status.RUNNING))
                .findFirst();
        }

        @Override
        public void update(Job job) {
            jobs.put(job.id(), job);
        }

        @Override
        public boolean finish(Job job, String worker) {
            boolean[] landed = {false};
            jobs.computeIfPresent(job.id(), (id, current) -> {
                if (current.status() != Status.RUNNING || !worker.equals(current.lockedBy())) return current;
                landed[0] = true;
                return job;
            });
            return landed[0];
        }

        @Override
        public synchronized Optional<Job> claim(String worker, Instant now) {
            Optional<Job> due = jobs.values().stream()
                .filter(j -> j.status() == Status.PENDING && !j.runAt().isAfter(now))
                .min(Comparator.comparing(Job::runAt).thenComparing(Job::createdAt));
            if (due.isEmpty()) return Optional.empty();
            Job claimed = due.get().with(Status.RUNNING, due.get().attempts(), due.get().runAt(), now, worker, due.get().error());
            jobs.put(claimed.id(), claimed);
            return Optional.of(claimed);
        }

        @Override
        public List<Job> list(Status status, int limit) {
            return jobs.values().stream()
                .filter(j -> j.status() == status)
                .sorted(Comparator.comparing(Job::runAt).thenComparing(Job::createdAt))
                .limit(limit)
                .toList();
        }

        @Override
        public Map<Status, Long> counts() {
            Map<Status, Long> out = new java.util.EnumMap<>(Status.class);
            for (Status s : Status.values()) out.put(s, 0L);
            for (Job j : jobs.values()) out.merge(j.status(), 1L, Long::sum);
            return out;
        }

        @Override
        public boolean delete(String id) {
            return jobs.remove(id) != null;
        }

        @Override
        public int releaseStale(Instant lockedBefore) {
            int n = 0;
            for (Job j : new ArrayList<>(jobs.values())) {
                if (j.status() == Status.RUNNING && j.lockedAt() != null && j.lockedAt().isBefore(lockedBefore)) {
                    // the run that never reported back counts as an attempt: a job that takes its worker down
                    // with it every time is dead-lettered like any other, instead of running forever
                    int attempts = j.attempts() + 1;
                    Status next = attempts >= j.maxAttempts() ? Status.FAILED : Status.PENDING;
                    jobs.put(j.id(), j.with(next, attempts, Instant.now(), null, null, RELEASED));
                    n++;
                }
            }
            return n;
        }

        @Override
        public int purgeDone(Instant before) {
            int n = 0;
            for (Job j : new ArrayList<>(jobs.values())) {
                if (j.status() == Status.DONE && j.lockedAt() != null && j.lockedAt().isBefore(before)) {
                    if (jobs.remove(j.id()) != null) n++;
                }
            }
            return n;
        }

        /** Everything, for a test. */
        public void clear() {
            jobs.clear();
        }
    }

    // ==================== the SQL store ====================

    /**
     * Jobs in the {@code jweb_jobs} table, shared by every instance on the
     * database. Installed automatically when {@link Sql} is connected; the
     * table is created if missing (PostgreSQL, MySQL, H2, SQLite syntax).
     */
    public static final class SqlStore implements Store {

        public static final String TABLE = "jweb_jobs";
        private static final String COLUMNS =
            "id, name, payload, status, attempts, max_attempts, run_at, created_at, locked_at, locked_by, error, job_key";

        private final Sql.Database db;

        public SqlStore(Sql.Database db) {
            this.db = db;
            ensureTable();
        }

        private void ensureTable() {
            try {
                db.query("select 1 from " + TABLE + " where 1 = 0").rows();
            } catch (Sql.SqlException missing) {
                db.execute("create table " + TABLE + " ("
                    + "id varchar(36) primary key, name varchar(100) not null, payload text, "
                    + "status varchar(10) not null, attempts int not null, max_attempts int not null, "
                    + "run_at timestamp not null, created_at timestamp not null, "
                    + "locked_at timestamp, locked_by varchar(100), error text, job_key varchar(200))");
                db.execute("create index " + TABLE + "_due on " + TABLE + " (status, run_at)");
            }
        }

        /**
         * Instants go into the {@code timestamp} columns as UTC wall time and
         * come back the same way. Binding an Instant would go through the JVM's
         * zone (and read back through it), which shifts an hour during a DST
         * change and disagrees between instances in different zones — enough to
         * release a live job as stale or run a delayed one early.
         */
        private static java.time.LocalDateTime utc(Instant at) {
            return at == null ? null : java.time.LocalDateTime.ofInstant(at, java.time.ZoneOffset.UTC);
        }

        private static Instant instant(Sql.Row row, String column) {
            java.time.LocalDateTime at = row.getLocalDateTime(column);
            return at == null ? null : at.toInstant(java.time.ZoneOffset.UTC);
        }

        @Override
        public Job add(Job job) {
            db.update("insert into " + TABLE + " (" + COLUMNS + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                job.id(), job.name(), job.payload(), job.status().name(), job.attempts(), job.maxAttempts(),
                utc(job.runAt()), utc(job.createdAt()), utc(job.lockedAt()), job.lockedBy(), job.error(), job.key());
            return job;
        }

        @Override
        public Optional<Job> find(String id) {
            return db.query("select " + COLUMNS + " from " + TABLE + " where id = ?", id).first(SqlStore::read);
        }

        @Override
        public Optional<Job> findByKey(String key) {
            return db.query("select " + COLUMNS + " from " + TABLE + " where job_key = ? and status in ('PENDING', 'RUNNING')", key)
                .first(SqlStore::read);
        }

        @Override
        public void update(Job job) {
            db.update("update " + TABLE + " set status = ?, attempts = ?, run_at = ?, locked_at = ?, locked_by = ?, error = ? where id = ?",
                job.status().name(), job.attempts(), utc(job.runAt()), utc(job.lockedAt()), job.lockedBy(), job.error(), job.id());
        }

        @Override
        public boolean finish(Job job, String worker) {
            return db.update("update " + TABLE + " set status = ?, attempts = ?, run_at = ?, locked_at = ?, locked_by = ?, error = ?"
                    + " where id = ? and status = 'RUNNING' and locked_by = ?",
                job.status().name(), job.attempts(), utc(job.runAt()), utc(job.lockedAt()), job.lockedBy(), job.error(),
                job.id(), worker) == 1;
        }

        @Override
        public Optional<Job> claim(String worker, Instant now) {
            List<Job> candidates = db.query("select " + COLUMNS + " from " + TABLE
                    + " where status = 'PENDING' and run_at <= ? order by run_at, created_at", utc(now))
                .limit(10)
                .list(SqlStore::read);
            for (Job candidate : candidates) {
                int won = db.update("update " + TABLE + " set status = 'RUNNING', locked_at = ?, locked_by = ? "
                    + "where id = ? and status = 'PENDING'", utc(now), worker, candidate.id());
                if (won == 1) {
                    return Optional.of(candidate.with(Status.RUNNING, candidate.attempts(), candidate.runAt(), now, worker, candidate.error()));
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Job> list(Status status, int limit) {
            return db.query("select " + COLUMNS + " from " + TABLE + " where status = ? order by run_at, created_at", status.name())
                .limit(limit)
                .list(SqlStore::read);
        }

        @Override
        public Map<Status, Long> counts() {
            Map<Status, Long> out = new java.util.EnumMap<>(Status.class);
            for (Status s : Status.values()) out.put(s, 0L);
            db.query("select status, count(*) as n from " + TABLE + " group by status")
                .forEach(row -> out.put(Status.valueOf(row.getString("status")), row.getLong("n")));
            return out;
        }

        @Override
        public boolean delete(String id) {
            return db.update("delete from " + TABLE + " where id = ?", id) > 0;
        }

        @Override
        public int releaseStale(Instant lockedBefore) {
            // the run that never reported back counts as an attempt (see Memory.releaseStale); status is set
            // before attempts because MySQL evaluates assignments left to right
            return db.update("update " + TABLE
                + " set status = case when attempts + 1 >= max_attempts then 'FAILED' else 'PENDING' end,"
                + " attempts = attempts + 1, run_at = ?, locked_at = null, locked_by = null, error = ?"
                + " where status = 'RUNNING' and locked_at < ?", utc(Instant.now()), RELEASED, utc(lockedBefore));
        }

        @Override
        public int purgeDone(Instant before) {
            return db.update("delete from " + TABLE + " where status = 'DONE' and locked_at < ?", utc(before));
        }

        private static Job read(Sql.Row row) {
            return new Job(row.getString("id"), row.getString("name"), row.getString("payload"),
                Status.valueOf(row.getString("status")), row.getInt("attempts"), row.getInt("max_attempts"),
                instant(row, "run_at"), instant(row, "created_at"), instant(row, "locked_at"),
                row.getString("locked_by"), row.getString("error"), row.getString("job_key"));
        }
    }
}
