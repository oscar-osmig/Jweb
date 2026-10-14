package jweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Durable jobs on the in-memory store: a handler runs the payload it was
 * given, failures retry with backoff until the attempts are spent, keys
 * dedupe, delays hold, and the bookkeeping is right.
 */
class JobsTest {

    record Welcome(long userId, String name) {}

    private Jobs.Memory store;

    @BeforeEach
    void freshStore() {
        store = new Jobs.Memory();
        Jobs.store(store);
    }

    @AfterEach
    void reset() {
        Jobs.store(new Jobs.Memory());
    }

    @Test
    void aHandlerRunsThePayloadItWasEnqueuedWith() {
        List<Welcome> ran = new CopyOnWriteArrayList<>();
        Jobs.handle("welcome", Welcome.class, ran::add);

        Jobs.Job job = Jobs.enqueue("welcome", new Welcome(42, "Ada"));
        assertEquals(Jobs.Status.PENDING, job.status());
        assertEquals(4, job.maxAttempts(), "3 retries after the first attempt");
        assertEquals(new Welcome(42, "Ada"), job.payload(Welcome.class));
        assertEquals(1, Jobs.pending().size());

        assertEquals(1, Jobs.runPending());
        assertEquals(List.of(new Welcome(42, "Ada")), ran);
        Jobs.Job done = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.DONE, done.status());
        assertEquals(1, done.attempts());
        assertNull(done.error());
        assertEquals(0, Jobs.runPending(), "nothing left");
        assertEquals(1L, Jobs.stats().get(Jobs.Status.DONE));
    }

    @Test
    void aPayloadlessHandlerAndANullPayload() {
        AtomicInteger runs = new AtomicInteger();
        Jobs.handle("tick", runs::incrementAndGet);
        Jobs.enqueue("tick", null);
        Jobs.runPending();
        assertEquals(1, runs.get());
    }

    @Test
    void aFailureRetriesWithBackoffThenFailsForGood() {
        AtomicInteger attempts = new AtomicInteger();
        Jobs.handle("flaky", String.class, s -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("not yet " + attempts.get());
        });
        Jobs.Job job = Jobs.enqueue("flaky", "x", Jobs.options().retries(2));

        assertEquals(1, Jobs.runPending());
        Jobs.Job after1 = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.PENDING, after1.status(), "back in the queue");
        assertEquals(1, after1.attempts());
        assertTrue(after1.runAt().isAfter(Instant.now().plusSeconds(1)), "with a delay: " + after1.runAt());
        assertTrue(after1.error().contains("not yet 1"), after1.error());
        assertEquals(0, Jobs.runPending(), "not due yet");

        // make it due now and run again, twice more
        store.update(after1.with(Jobs.Status.PENDING, 1, Instant.now().minusSeconds(1), null, null, after1.error()));
        assertEquals(1, Jobs.runPending());
        Jobs.Job after2 = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.PENDING, after2.status());
        assertEquals(2, after2.attempts());
        store.update(after2.with(Jobs.Status.PENDING, 2, Instant.now().minusSeconds(1), null, null, after2.error()));
        assertEquals(1, Jobs.runPending());

        Jobs.Job dead = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.FAILED, dead.status(), "attempts spent");
        assertEquals(3, dead.attempts());
        assertTrue(dead.error().contains("not yet 3"), dead.error());
        assertEquals(1, Jobs.failed().size());
        assertEquals(3, attempts.get());

        // a failed job can be put back
        assertTrue(Jobs.retry(job.id()));
        assertFalse(Jobs.retry(job.id()), "only a failed job");
        Jobs.Job again = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.PENDING, again.status());
        assertEquals(0, again.attempts());
        assertTrue(Jobs.delete(job.id()));
        assertTrue(Jobs.find(job.id()).isEmpty());
    }

    @Test
    void noRetriesMeansOneAttempt() {
        Jobs.handle("once", String.class, s -> { throw new RuntimeException("no"); });
        Jobs.Job job = Jobs.enqueue("once", "x", Jobs.options().retries(0));
        Jobs.runPending();
        assertEquals(Jobs.Status.FAILED, Jobs.find(job.id()).orElseThrow().status());
    }

    @Test
    void aMissingHandlerFailsTheJobWithAClearError() {
        Jobs.Job job = Jobs.enqueue("nobody-handles-this", Map.of("a", 1), Jobs.options().retries(0));
        Jobs.runPending();
        Jobs.Job failed = Jobs.find(job.id()).orElseThrow();
        assertEquals(Jobs.Status.FAILED, failed.status());
        assertTrue(failed.error().contains("No handler registered"), failed.error());
    }

    @Test
    void aKeyKeepsOnePendingJobAndADelayHoldsIt() {
        AtomicInteger runs = new AtomicInteger();
        Jobs.handle("reindex", runs::incrementAndGet);
        Jobs.Job first = Jobs.enqueue("reindex", null, Jobs.options().key("reindex"));
        Jobs.Job second = Jobs.enqueue("reindex", null, Jobs.options().key("reindex"));
        assertEquals(first.id(), second.id(), "the pending one is returned");
        assertEquals(1, Jobs.pending().size());
        Jobs.runPending();
        assertEquals(1, runs.get());
        Jobs.Job third = Jobs.enqueue("reindex", null, Jobs.options().key("reindex"));
        assertNotNull(third);
        assertFalse(third.id().equals(first.id()), "after it ran, a new one can be queued");

        Jobs.Job later = Jobs.enqueue("reindex", null, Jobs.options().after(Duration.ofMinutes(5)));
        assertEquals(1, Jobs.runPending(), "the third ran; the delayed one did not");
        assertEquals(Jobs.Status.PENDING, Jobs.find(later.id()).orElseThrow().status());
        assertEquals(2, runs.get());
    }

    @Test
    void staleRunningJobsAreReleasedAndOldDoneJobsPurged() {
        Jobs.handle("slow", () -> {});
        Jobs.Job job = Jobs.enqueue("slow", null);
        Jobs.Job claimed = store.claim("dead-worker", Instant.now()).orElseThrow();
        assertEquals(Jobs.Status.RUNNING, claimed.status());
        // the worker died an hour ago
        store.update(claimed.with(Jobs.Status.RUNNING, 0, claimed.runAt(), Instant.now().minus(Duration.ofHours(1)), "dead-worker", null));
        assertEquals(1, store.releaseStale(Instant.now().minus(Duration.ofMinutes(10))));
        assertEquals(Jobs.Status.PENDING, Jobs.find(job.id()).orElseThrow().status());

        Jobs.runPending();
        assertEquals(Jobs.Status.DONE, Jobs.find(job.id()).orElseThrow().status());
        assertEquals(0, store.purgeDone(Instant.now().minus(Duration.ofDays(1))), "finished just now: kept");
        assertEquals(1, store.purgeDone(Instant.now().plusSeconds(1)));
        assertTrue(Jobs.find(job.id()).isEmpty());
    }

    @Test
    void theWorkerStartsAndStops() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        Jobs.handle("bg", runs::incrementAndGet);
        Jobs.configure(Duration.ofMillis(50), Duration.ofMinutes(10), Duration.ofHours(24), 2);
        Jobs.start();
        try {
            assertTrue(Jobs.started());
            Jobs.enqueue("bg", null);
            for (int i = 0; i < 100 && runs.get() < 1; i++) Thread.sleep(20);
            assertEquals(1, runs.get(), "the poller picked it up");
        } finally {
            Jobs.stop();
            Jobs.configure(Duration.ofSeconds(1), Duration.ofMinutes(10), Duration.ofHours(24), 8);
        }
        assertFalse(Jobs.started());
    }
}
