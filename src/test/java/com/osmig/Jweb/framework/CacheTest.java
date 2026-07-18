package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.cache.Cache;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Covers the Cache correctness fixes: atomic getOrSet (no stampede) and basics. */
class CacheTest {

    @Test
    void getOrSetComputesOnceUnderConcurrency() throws InterruptedException {
        Cache<String, Integer> cache = Cache.create(Duration.ofMinutes(1));
        AtomicInteger supplierCalls = new AtomicInteger(0);

        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    cache.getOrSet("k", () -> {
                        supplierCalls.incrementAndGet();
                        return 42;
                    });
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS), "workers should finish");
        pool.shutdownNow();

        assertEquals(1, supplierCalls.get(),
                "the supplier must run exactly once even under concurrent misses");
        assertEquals(42, cache.get("k"));
    }

    @Test
    void getOrSetDoesNotCacheNull() {
        Cache<String, String> cache = Cache.create();
        String v = cache.getOrSet("missing", () -> null);
        assertNull(v);
        assertFalse(cache.has("missing"), "a null result must not create a mapping");
    }

    @Test
    void expiredEntriesAreNotReturned() {
        Cache<String, String> cache = Cache.create();
        cache.set("k", "v", Duration.ofMillis(1));
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        assertNull(cache.get("k"), "expired entry must not be returned");
    }
}
