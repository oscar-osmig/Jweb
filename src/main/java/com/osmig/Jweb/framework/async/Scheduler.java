package com.osmig.Jweb.framework.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
 * Job scheduler with cron expression support.
 *
 * <p>Usage:</p>
 * <pre>
 * // Schedule with cron expression
 * Scheduler.cron("daily-report", "0 9 * * *", () -> {
 *     generateDailyReport();
 * });
 *
 * // Every minute
 * Scheduler.cron("health-check", "* * * * *", () -> {
 *     checkHealth();
 * });
 *
 * // Every hour at minute 30
 * Scheduler.cron("sync", "30 * * * *", () -> {
 *     syncData();
 * });
 *
 * // Every day at 2:30 AM
 * Scheduler.cron("backup", "30 2 * * *", () -> {
 *     backupDatabase();
 * });
 *
 * // Every Monday at 9 AM
 * Scheduler.cron("weekly-email", "0 9 * * 1", () -> {
 *     sendWeeklyNewsletter();
 * });
 *
 * // Fluent API
 * Scheduler.job("cleanup")
 *     .cron("0 3 * * *")    // 3 AM daily
 *     .timezone("America/New_York")
 *     .onError(e -> log.error("Cleanup failed", e))
 *     .run(() -> cleanupFiles());
 *
 * // Simple intervals
 * Scheduler.every(Duration.ofMinutes(5), "ping", () -> ping());
 * Scheduler.everyMinute("tick", () -> tick());
 * Scheduler.everyHour("sync", () -> sync());
 * Scheduler.daily("report", LocalTime.of(9, 0), () -> report());
 * </pre>
 *
 * <p>Cron expression format (5 fields):</p>
 * <pre>
 * ┌───────────── minute (0-59)
 * │ ┌───────────── hour (0-23)
 * │ │ ┌───────────── day of month (1-31)
 * │ │ │ ┌───────────── month (1-12)
 * │ │ │ │ ┌───────────── day of week (0-6, 0=Sunday)
 * │ │ │ │ │
 * * * * * *
 *
 * Special characters:
 * * = any value
 * , = list separator (e.g., "1,15" = 1st and 15th)
 * - = range (e.g., "1-5" = 1 through 5)
 * / = step (e.g., "*&#47;15" = every 15)
 * </pre>
 */
public final class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final Map<String, ScheduledJob> jobs = new ConcurrentHashMap<>();

    private Scheduler() {}

    // ==================== Cron Scheduling ====================

    /**
     * Schedules a job with a cron expression.
     */
    public static void cron(String name, String cronExpression, Runnable task) {
        job(name).cron(cronExpression).run(task);
    }

    /**
     * Creates a job builder.
     */
    public static JobBuilder job(String name) {
        return new JobBuilder(name);
    }

    // ==================== Simple Intervals ====================

    /**
     * Runs a job at fixed intervals.
     */
    public static void every(Duration interval, String name, Runnable task) {
        job(name).every(interval).run(task);
    }

    /**
     * Runs a job every minute.
     */
    public static void everyMinute(String name, Runnable task) {
        every(Duration.ofMinutes(1), name, task);
    }

    /**
     * Runs a job every N minutes.
     */
    public static void everyMinutes(int minutes, String name, Runnable task) {
        every(Duration.ofMinutes(minutes), name, task);
    }

    /**
     * Runs a job every hour.
     */
    public static void everyHour(String name, Runnable task) {
        every(Duration.ofHours(1), name, task);
    }

    /**
     * Runs a job every N hours.
     */
    public static void everyHours(int hours, String name, Runnable task) {
        every(Duration.ofHours(hours), name, task);
    }

    /**
     * Runs a job daily at a specific time.
     */
    public static void daily(String name, LocalTime time, Runnable task) {
        job(name).cron(String.format("%d %d * * *", time.getMinute(), time.getHour())).run(task);
    }

    /**
     * Runs a job daily at midnight.
     */
    public static void dailyAtMidnight(String name, Runnable task) {
        daily(name, LocalTime.MIDNIGHT, task);
    }

    /**
     * Runs a job weekly on a specific day and time.
     */
    public static void weekly(String name, DayOfWeek day, LocalTime time, Runnable task) {
        int dayNum = day.getValue() % 7; // Convert to 0=Sunday format
        job(name).cron(String.format("%d %d * * %d", time.getMinute(), time.getHour(), dayNum)).run(task);
    }

    // ==================== Job Management ====================

    /**
     * Cancels a scheduled job.
     */
    public static boolean cancel(String name) {
        ScheduledJob job = jobs.remove(name);
        if (job != null) {
            job.cancel();
            log.info("Cancelled job: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Pauses a job (stops running but keeps configuration).
     */
    public static boolean pause(String name) {
        ScheduledJob job = jobs.get(name);
        if (job != null) {
            job.pause();
            log.info("Paused job: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Resumes a paused job.
     */
    public static boolean resume(String name) {
        ScheduledJob job = jobs.get(name);
        if (job != null) {
            job.resume();
            log.info("Resumed job: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Gets job info.
     */
    public static Optional<JobInfo> getJob(String name) {
        ScheduledJob job = jobs.get(name);
        return job != null ? Optional.of(job.getInfo()) : Optional.empty();
    }

    /**
     * Gets all job infos.
     */
    public static List<JobInfo> getAllJobs() {
        return jobs.values().stream().map(ScheduledJob::getInfo).toList();
    }

    /**
     * Shuts down the scheduler.
     */
    public static void shutdown() {
        log.info("Shutting down scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== Job Builder ====================

    public static class JobBuilder {
        private final String name;
        private String cronExpression;
        private Duration interval;
        private ZoneId timezone = ZoneId.systemDefault();
        private java.util.function.Consumer<Exception> errorHandler;
        private Duration initialDelay = Duration.ZERO;

        JobBuilder(String name) {
            this.name = name;
        }

        /**
         * Sets cron expression.
         */
        public JobBuilder cron(String expression) {
            this.cronExpression = expression;
            return this;
        }

        /**
         * Sets fixed interval.
         */
        public JobBuilder every(Duration interval) {
            this.interval = interval;
            return this;
        }

        /**
         * Sets timezone for cron evaluation.
         */
        public JobBuilder timezone(String timezone) {
            this.timezone = ZoneId.of(timezone);
            return this;
        }

        /**
         * Sets timezone.
         */
        public JobBuilder timezone(ZoneId timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * Sets initial delay before first execution.
         */
        public JobBuilder initialDelay(Duration delay) {
            this.initialDelay = delay;
            return this;
        }

        /**
         * Sets error handler.
         */
        public JobBuilder onError(java.util.function.Consumer<Exception> handler) {
            this.errorHandler = handler;
            return this;
        }

        /**
         * Schedules the job.
         */
        public void run(Runnable task) {
            // Cancel existing job with same name
            cancel(name);

            ScheduledJob job;
            if (cronExpression != null) {
                job = new CronJob(name, cronExpression, timezone, task, errorHandler);
            } else if (interval != null) {
                job = new IntervalJob(name, interval, initialDelay, task, errorHandler);
            } else {
                throw new IllegalStateException("Must specify cron() or every()");
            }

            jobs.put(name, job);
            job.start();
            log.info("Scheduled job '{}': {}", name, cronExpression != null ? cronExpression : interval);
        }
    }

    // ==================== Job Types ====================

    private interface ScheduledJob {
        void start();
        void cancel();
        void pause();
        void resume();
        JobInfo getInfo();
    }

    private static class IntervalJob implements ScheduledJob {
        private final String name;
        private final Duration interval;
        private final Duration initialDelay;
        private final Runnable task;
        private final java.util.function.Consumer<Exception> errorHandler;
        private ScheduledFuture<?> future;
        private boolean paused = false;
        private Instant lastRun;
        private int runCount = 0;

        IntervalJob(String name, Duration interval, Duration initialDelay, Runnable task,
                    java.util.function.Consumer<Exception> errorHandler) {
            this.name = name;
            this.interval = interval;
            this.initialDelay = initialDelay;
            this.task = task;
            this.errorHandler = errorHandler;
        }

        @Override
        public void start() {
            future = scheduler.scheduleAtFixedRate(this::execute,
                initialDelay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
        }

        private void execute() {
            if (paused) return;
            try {
                task.run();
                lastRun = Instant.now();
                runCount++;
            } catch (Exception e) {
                log.error("Job '{}' failed", name, e);
                if (errorHandler != null) errorHandler.accept(e);
            }
        }

        @Override
        public void cancel() {
            if (future != null) future.cancel(false);
        }

        @Override
        public void pause() {
            paused = true;
        }

        @Override
        public void resume() {
            paused = false;
        }

        @Override
        public JobInfo getInfo() {
            return new JobInfo(name, "every " + interval, paused, lastRun, runCount,
                future != null && !future.isCancelled() && !future.isDone());
        }
    }

    private static class CronJob implements ScheduledJob {
        private final String name;
        private final String cronExpression;
        private final ZoneId timezone;
        private final Runnable task;
        private final java.util.function.Consumer<Exception> errorHandler;
        private final CronExpression cron;
        private ScheduledFuture<?> future;
        private boolean paused = false;
        private Instant lastRun;
        private int runCount = 0;

        CronJob(String name, String cronExpression, ZoneId timezone, Runnable task,
                java.util.function.Consumer<Exception> errorHandler) {
            this.name = name;
            this.cronExpression = cronExpression;
            this.timezone = timezone;
            this.task = task;
            this.errorHandler = errorHandler;
            this.cron = new CronExpression(cronExpression);
        }

        @Override
        public void start() {
            scheduleNext();
        }

        private void scheduleNext() {
            ZonedDateTime now = ZonedDateTime.now(timezone);
            ZonedDateTime next = cron.nextExecution(now);
            if (next == null) return;

            long delayMs = Duration.between(now, next).toMillis();
            future = scheduler.schedule(() -> {
                execute();
                scheduleNext(); // Schedule next execution
            }, delayMs, TimeUnit.MILLISECONDS);
        }

        private void execute() {
            if (paused) return;
            try {
                task.run();
                lastRun = Instant.now();
                runCount++;
            } catch (Exception e) {
                log.error("Cron job '{}' failed", name, e);
                if (errorHandler != null) errorHandler.accept(e);
            }
        }

        @Override
        public void cancel() {
            if (future != null) future.cancel(false);
        }

        @Override
        public void pause() {
            paused = true;
        }

        @Override
        public void resume() {
            paused = false;
        }

        @Override
        public JobInfo getInfo() {
            ZonedDateTime next = cron.nextExecution(ZonedDateTime.now(timezone));
            return new JobInfo(name, cronExpression, paused, lastRun, runCount,
                future != null && !future.isCancelled() && !future.isDone());
        }
    }

    // ==================== Cron Expression Parser ====================

    private static class CronExpression {
        private final Set<Integer> minutes;
        private final Set<Integer> hours;
        private final Set<Integer> daysOfMonth;
        private final Set<Integer> months;
        private final Set<Integer> daysOfWeek;

        CronExpression(String expression) {
            String[] parts = expression.trim().split("\\s+");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Cron expression must have 5 fields: " + expression);
            }

            minutes = parseField(parts[0], 0, 59);
            hours = parseField(parts[1], 0, 23);
            daysOfMonth = parseField(parts[2], 1, 31);
            months = parseField(parts[3], 1, 12);
            daysOfWeek = parseField(parts[4], 0, 6);
        }

        private Set<Integer> parseField(String field, int min, int max) {
            Set<Integer> values = new TreeSet<>();

            for (String part : field.split(",")) {
                if (part.equals("*")) {
                    for (int i = min; i <= max; i++) values.add(i);
                } else if (part.contains("/")) {
                    String[] stepParts = part.split("/");
                    int step = Integer.parseInt(stepParts[1]);
                    int start = stepParts[0].equals("*") ? min : Integer.parseInt(stepParts[0]);
                    for (int i = start; i <= max; i += step) values.add(i);
                } else if (part.contains("-")) {
                    String[] rangeParts = part.split("-");
                    int from = Integer.parseInt(rangeParts[0]);
                    int to = Integer.parseInt(rangeParts[1]);
                    for (int i = from; i <= to; i++) values.add(i);
                } else {
                    values.add(Integer.parseInt(part));
                }
            }

            return values;
        }

        ZonedDateTime nextExecution(ZonedDateTime from) {
            ZonedDateTime next = from.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

            // Search for next valid time (max 4 years ahead)
            ZonedDateTime limit = from.plusYears(4);

            while (next.isBefore(limit)) {
                if (matches(next)) {
                    return next;
                }

                // Advance to next candidate
                if (!months.contains(next.getMonthValue())) {
                    next = next.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0);
                } else if (!daysOfMonth.contains(next.getDayOfMonth()) ||
                           !daysOfWeek.contains(next.getDayOfWeek().getValue() % 7)) {
                    next = next.plusDays(1).withHour(0).withMinute(0);
                } else if (!hours.contains(next.getHour())) {
                    next = next.plusHours(1).withMinute(0);
                } else {
                    next = next.plusMinutes(1);
                }
            }

            return null;
        }

        private boolean matches(ZonedDateTime dt) {
            return minutes.contains(dt.getMinute()) &&
                   hours.contains(dt.getHour()) &&
                   daysOfMonth.contains(dt.getDayOfMonth()) &&
                   months.contains(dt.getMonthValue()) &&
                   daysOfWeek.contains(dt.getDayOfWeek().getValue() % 7);
        }
    }

    // ==================== Job Info ====================

    /**
     * Information about a scheduled job.
     */
    public record JobInfo(
        String name,
        String schedule,
        boolean paused,
        Instant lastRun,
        int runCount,
        boolean active
    ) {}
}
