package jweb;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link Jobs.SqlStore} on H2: the table is created, claims are atomic, and the life cycle round-trips. */
class SqlJobStoreTest {

    record Payload(String what, int n) {}

    static Sql.Database db;
    static Jobs.SqlStore store;

    @BeforeAll
    static void connect() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:jobstore;DB_CLOSE_DELAY=-1");
        db = Sql.of(ds);
        store = new Jobs.SqlStore(db);
        Jobs.store(store);
    }

    @AfterAll
    static void disconnect() {
        Jobs.store(new Jobs.Memory());
        db.execute("drop table " + Jobs.SqlStore.TABLE);
    }

    @AfterEach
    void clean() {
        db.update("delete from " + Jobs.SqlStore.TABLE);
    }

    @Test
    void theTableIsCreatedOnceAndAJobRoundTrips() {
        new Jobs.SqlStore(db);   // a second construction finds the table
        List<Payload> ran = new CopyOnWriteArrayList<>();
        Jobs.handle("sql-job", Payload.class, ran::add);

        Jobs.Job job = Jobs.enqueue("sql-job", new Payload("x", 7), Jobs.options().key("k1"));
        Jobs.Job stored = Jobs.find(job.id()).orElseThrow();
        assertEquals("sql-job", stored.name());
        assertEquals(Jobs.Status.PENDING, stored.status());
        assertEquals("k1", stored.key());
        assertEquals(new Payload("x", 7), stored.payload(Payload.class));
        assertEquals(job.runAt().toEpochMilli() / 1000, stored.runAt().toEpochMilli() / 1000);
        assertEquals(job.id(), Jobs.enqueue("sql-job", new Payload("y", 1), Jobs.options().key("k1")).id());

        assertEquals(1, Jobs.runPending());
        assertEquals(List.of(new Payload("x", 7)), ran);
        Jobs.Job done = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.DONE, done.status());
        assertEquals(1, done.attempts());
        assertTrue(store.findByKey("k1").isEmpty(), "a done job no longer holds the key");
        Map<Jobs.Status, Long> stats = Jobs.stats();
        assertEquals(1L, stats.get(Jobs.Status.DONE));
        assertEquals(0L, stats.get(Jobs.Status.PENDING));
    }

    @Test
    void twoWorkersNeverClaimTheSameJob() {
        Jobs.handle("c", () -> {});
        Jobs.Job a = Jobs.enqueue("c", null);
        Jobs.Job b = Jobs.enqueue("c", null);
        Instant now = Instant.now();
        Optional<Jobs.Job> first = store.claim("w1", now);
        Optional<Jobs.Job> second = store.claim("w2", now);
        Optional<Jobs.Job> third = store.claim("w3", now);
        assertTrue(first.isPresent() && second.isPresent());
        assertNotEquals(first.get().id(), second.get().id());
        assertTrue(third.isEmpty());
        assertEquals("w1", Jobs.find(first.get().id()).orElseThrow().lockedBy());
        assertEquals(2, Jobs.running().size());
        assertTrue(List.of(a.id(), b.id()).containsAll(List.of(first.get().id(), second.get().id())));
    }

    @Test
    void failureRetryStaleReleaseAndPurgeAreStored() {
        Jobs.handle("fails", String.class, s -> { throw new IllegalArgumentException("nope"); });
        Jobs.Job job = Jobs.enqueue("fails", "p", Jobs.options().retries(1));
        Jobs.runPending();
        Jobs.Job retried = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.PENDING, retried.status());
        assertEquals(1, retried.attempts());
        assertTrue(retried.error().contains("nope"), retried.error());
        assertTrue(retried.runAt().isAfter(Instant.now()));

        store.update(retried.with(Jobs.Status.PENDING, 1, Instant.now().minusSeconds(1), null, null, retried.error()));
        Jobs.runPending();
        Jobs.Job failed = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.FAILED, failed.status());
        assertEquals(1, Jobs.failed().size());
        assertTrue(Jobs.retry(job.id()));
        assertEquals(Jobs.Status.PENDING, Jobs.find(job.id()).orElseThrow().status());

        // a worker that died mid-job
        Jobs.Job gone = store.claim("gone", Instant.now()).orElseThrow();
        store.update(gone.with(Jobs.Status.RUNNING, gone.attempts(), gone.runAt(), Instant.now().minus(Duration.ofHours(2)), "gone", null));
        assertEquals(1, store.releaseStale(Instant.now().minus(Duration.ofMinutes(10))));
        assertEquals(Jobs.Status.PENDING, Jobs.find(job.id()).orElseThrow().status());
        assertEquals(0, store.releaseStale(Instant.now().minus(Duration.ofMinutes(10))));

        Jobs.handle("fails", String.class, s -> {});   // now it succeeds
        Jobs.runPending();
        assertEquals(Jobs.Status.DONE, Jobs.find(job.id()).orElseThrow().status());
        assertEquals(0, store.purgeDone(Instant.now().minus(Duration.ofDays(1))));
        assertEquals(1, store.purgeDone(Instant.now().plusSeconds(5)));
        assertFalse(Jobs.find(job.id()).isPresent());
    }
}
