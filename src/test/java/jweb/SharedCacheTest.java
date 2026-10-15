package jweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shared side of {@code jweb.Cache}: typed values as JSON with a time
 * to live, locks and {@code once}, and {@code RateLimit} counting through
 * the shared store — on the memory backend, which is what every other
 * backend has to match.
 */
class SharedCacheTest {

    record Price(String sku, long cents) {}

    @BeforeEach
    @AfterEach
    void fresh() {
        Cache.resetShared();
    }

    @Test
    void typedValuesRoundTripAsJsonAndExpire() throws Exception {
        assertFalse(Cache.distributed());
        Cache.Shared<Price> prices = Cache.shared("prices", Price.class, Duration.ofMinutes(5));
        assertSame(prices, Cache.shared("prices", Price.class), "one view per name");
        assertEquals(Optional.empty(), prices.get("a"));

        prices.set("a", new Price("a", 100));
        assertEquals(new Price("a", 100), prices.get("a").orElseThrow());

        AtomicInteger computed = new AtomicInteger();
        assertEquals(new Price("a", 100), prices.getOrSet("a", () -> {
            computed.incrementAndGet();
            return new Price("x", 1);
        }));
        assertEquals(0, computed.get(), "a hit does not compute");
        Price b = prices.getOrSet("b", () -> {
            computed.incrementAndGet();
            return new Price("b", 5);
        });
        assertEquals(1, computed.get());
        assertEquals(b, prices.get("b").orElseThrow());

        prices.set("c", new Price("c", 1), Duration.ofMillis(40));
        assertTrue(prices.get("c").isPresent());
        Thread.sleep(90);
        assertEquals(Optional.empty(), prices.get("c"), "expired");

        prices.set("a", null);
        assertEquals(Optional.empty(), prices.get("a"), "null deletes");
        assertTrue(prices.delete("b"));
        assertFalse(prices.delete("b"));

        Cache.Shared<String> names = Cache.shared("names", String.class);
        names.set("1", "Ada");
        names.set("2", "Bob");
        assertEquals("Ada", names.get("1").orElseThrow());
        Cache.Shared<Long> counts = Cache.shared("counts", Long.class);
        assertEquals(1L, counts.increment("hits", 1, Duration.ofMinutes(1)));
        assertEquals(3L, counts.increment("hits", 2, Duration.ofMinutes(1)));
        assertEquals(Optional.of(3L), counts.get("hits"));

        assertEquals(2, names.clear());
        assertEquals(Optional.empty(), names.get("1"));
        assertEquals(Optional.of(3L), counts.get("hits"), "another cache's entries stay");
    }

    @Test
    void locksAreHeldUntilReleasedOrExpiredAndOnceRunsOnOneInstance() throws Exception {
        Optional<Cache.Lock> first = Cache.lock("import", Duration.ofMinutes(1));
        assertTrue(first.isPresent());
        assertTrue(Cache.lock("import", Duration.ofMinutes(1)).isEmpty(), "held");
        first.get().release();
        assertTrue(Cache.lock("import", Duration.ofMinutes(1)).isPresent(), "released");

        Optional<Cache.Lock> brief = Cache.lock("brief", Duration.ofMillis(40));
        Thread.sleep(90);
        Optional<Cache.Lock> next = Cache.lock("brief", Duration.ofMinutes(1));
        assertTrue(next.isPresent(), "an expired lock is free");
        brief.get().release();
        assertTrue(Cache.lock("brief", Duration.ofMinutes(1)).isEmpty(), "the old holder cannot release the new holder's lock");
        next.get().close();
        assertTrue(Cache.lock("brief", Duration.ofMinutes(1)).isPresent());

        AtomicInteger runs = new AtomicInteger();
        assertTrue(Cache.once("digest", Duration.ofMinutes(1), runs::incrementAndGet));
        assertFalse(Cache.once("digest", Duration.ofMinutes(1), runs::incrementAndGet));
        assertEquals(1, runs.get());

        Cache.backend(new Failing());
        assertFalse(Cache.once("x", Duration.ofMinutes(1), runs::incrementAndGet), "no store, nobody runs");
        assertEquals(1, runs.get());
        Cache.Shared<Price> prices = Cache.shared("p", Price.class);
        assertEquals(Optional.empty(), prices.get("a"), "a store failure is a miss");
        prices.set("a", new Price("a", 1));
        assertFalse(prices.delete("a"));
        assertEquals(0, prices.clear());
        assertEquals(new Price("a", 2), prices.getOrSet("a", () -> new Price("a", 2)), "computed, not stored, no exception");
    }

    @Test
    void rateLimitsCountAcrossInstancesThroughTheSharedStore() throws Exception {
        Cache.backend(new Delegating(new Cache.MemoryBackend()));
        assertTrue(Cache.distributed());
        Middleware a = RateLimit.perMinute(2).byIp().build();
        Middleware b = RateLimit.perMinute(2).byIp().build();   // "another instance", the same limiter
        Request req = MockRequest.get("/x").header("X-Forwarded-For", "1.2.3.4").build();

        assertEquals("ok", a.handle(req, () -> "ok"));
        assertEquals("ok", b.handle(req, () -> "ok"));
        assertEquals(429, status(a.handle(req, () -> "ok")), "the third, on either instance, is over");
        assertEquals(429, status(b.handle(req, () -> "ok")));

        Request other = MockRequest.get("/x").header("X-Forwarded-For", "5.6.7.8").build();
        assertEquals("ok", a.handle(other, () -> "ok"));

        Middleware local = RateLimit.perMinute(2).byIp().shared(false).build();
        assertEquals("ok", local.handle(req, () -> "ok"), "a limiter kept local does not see the shared count");

        Middleware named = RateLimit.perMinute(1).name("contact").byIp().build();
        assertEquals("ok", named.handle(req, () -> "ok"));
        ResponseEntity<?> over = (ResponseEntity<?>) named.handle(req, () -> "ok");
        assertEquals(429, over.getStatusCode().value());
        assertEquals("1", over.getHeaders().getFirst("X-RateLimit-Limit"));

        Cache.backend(new Failing());
        assertEquals("ok", a.handle(req, () -> "ok"), "a store that cannot be reached lets the request through");
    }

    private static int status(Object result) {
        return ((ResponseEntity<?>) result).getStatusCode().value();
    }

    /** A backend that is "not memory" for {@code distributed()}, backed by memory. */
    static final class Delegating implements Cache.Backend {
        private final Cache.Backend inner;

        Delegating(Cache.Backend inner) {
            this.inner = inner;
        }

        @Override public String get(String key) { return inner.get(key); }
        @Override public void set(String key, String value, Duration ttl) { inner.set(key, value, ttl); }
        @Override public boolean setIfAbsent(String key, String value, Duration ttl) { return inner.setIfAbsent(key, value, ttl); }
        @Override public boolean delete(String key) { return inner.delete(key); }
        @Override public long increment(String key, long delta, Duration ttl) { return inner.increment(key, delta, ttl); }
        @Override public int clear(String prefix) { return inner.clear(prefix); }
    }

    /** A store that is down. */
    static final class Failing implements Cache.Backend {
        private Cache.BackendException down() {
            return new Cache.BackendException("store down", null);
        }

        @Override public String get(String key) { throw down(); }
        @Override public void set(String key, String value, Duration ttl) { throw down(); }
        @Override public boolean setIfAbsent(String key, String value, Duration ttl) { throw down(); }
        @Override public boolean delete(String key) { throw down(); }
        @Override public long increment(String key, long delta, Duration ttl) { throw down(); }
        @Override public int clear(String prefix) { throw down(); }
    }
}
