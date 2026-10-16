package jweb;

import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caches. The instance side — {@code Cache.create(ttl)}, {@code Cache.named(...)},
 * {@code getOrSet} — lives in memory on this instance, as fast as a map.
 * The shared side is what every instance sees:
 *
 * <pre>{@code
 * // A typed cache the instances share: JSON in a Redis when one is configured, memory otherwise
 * Cache.Shared<Price> prices = Cache.shared("prices", Price.class, Duration.ofMinutes(10));
 * Price p = prices.getOrSet("sku-9", () -> pricing.lookup("sku-9"));
 * prices.set("sku-9", p);  prices.delete("sku-9");  prices.increment("hits", 1, Duration.ofDays(1));
 *
 * // Work only one instance should do: the one that gets the lock runs it
 * Scheduler.everyHour("digest", () -> Cache.once("digest", Duration.ofMinutes(5), () -> sendDigest()));
 * try (Cache.Lock lock = Cache.lock("import", Duration.ofMinutes(1)).orElseThrow()) { ... }
 * }</pre>
 *
 * <p>Set {@code jweb.cache.redis.url} (or {@code jweb.redis.url} for cache and
 * PubSub together) and the shared side is {@link RedisCache} — the framework
 * speaks the protocol itself; {@code RateLimit} counts across instances
 * through it too. Any {@link Backend} bean replaces it; without any, the
 * shared side is this instance's memory, with the same semantics.</p>
 */
@SuppressWarnings("deprecation")
public class Cache<K, V> extends com.osmig.Jweb.framework.cache.Cache<K, V> {

    protected Cache(Duration defaultTtl) {
        super(defaultTtl);
    }

    // ==================== the shared side ====================

    /** What a shared store does: strings under keys, with a time to live. */
    public interface Backend {

        /** The value, or null when there is none or it expired. */
        String get(String key);

        /** Stores the value; {@code ttl} null keeps it until deleted. */
        void set(String key, String value, Duration ttl);

        /** Stores the value only when the key is free; whether it did. */
        boolean setIfAbsent(String key, String value, Duration ttl);

        /** Removes the key; whether there was one. */
        boolean delete(String key);

        /** Adds to a counter (0 when new) and returns it; a new key expires after {@code ttl}. */
        long increment(String key, long delta, Duration ttl);

        /** Removes every key starting with the prefix; how many. */
        int clear(String prefix);

        default void stop() {}
    }

    /** Thrown when the shared store cannot be reached. */
    public static class BackendException extends RuntimeException {
        public BackendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static volatile Backend backend = new MemoryBackend();
    private static final Map<String, Shared<?>> SHARED = new ConcurrentHashMap<>();
    private static volatile long lastWarning;

    /** Installs the shared store; null goes back to this instance's memory. */
    public static void backend(Backend newBackend) {
        backend = newBackend == null ? new MemoryBackend() : newBackend;
    }

    public static Backend backend() {
        return backend;
    }

    /** Whether the shared side really is shared (a store beyond this instance). */
    public static boolean distributed() {
        return !(backend instanceof MemoryBackend);
    }

    /** A typed cache every instance sees, five minutes per entry by default. */
    public static <V> Shared<V> shared(String name, Class<V> type) {
        return shared(name, type, Duration.ofMinutes(5));
    }

    /** A typed cache every instance sees, with its own default time to live. */
    @SuppressWarnings("unchecked")
    public static <V> Shared<V> shared(String name, Class<V> type, Duration ttl) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Shared<?> existing = SHARED.get(name);
        if (existing != null && existing.type == type) return (Shared<V>) existing;
        Shared<V> created = new Shared<>(name, type, ttl);
        SHARED.put(name, created);
        return created;
    }

    /**
     * Runs the work on this instance only when no instance has run it in the
     * last {@code ttl}: whoever takes the lock runs, the rest skip. For the
     * hourly job that five instances would otherwise run five times. Whether
     * it ran here.
     */
    public static boolean once(String name, Duration ttl, Runnable work) {
        Optional<Lock> lock = lock(name, ttl);
        if (lock.isEmpty()) return false;
        work.run();   // the lock is left to expire: "once per ttl", not "once at a time"
        return true;
    }

    /**
     * A lock the instances share, held until released or until {@code ttl}
     * passes (a holder that died lets go). Empty when another holds it.
     */
    public static Optional<Lock> lock(String name, Duration ttl) {
        String key = "lock:" + name;
        String token = Request.newId();
        try {
            return backend.setIfAbsent(key, token, ttl) ? Optional.of(new Lock(key, token)) : Optional.empty();
        } catch (BackendException e) {
            warn("lock " + name, e);
            return Optional.empty();
        }
    }

    /** A held lock; close it to release. */
    public static final class Lock implements AutoCloseable {
        private final String key;
        private final String token;

        Lock(String key, String token) {
            this.key = key;
            this.token = token;
        }

        /** Lets go, if this holder still holds it (an expired lock may be someone else's by now). */
        public void release() {
            try {
                if (token.equals(backend.get(key))) backend.delete(key);
            } catch (BackendException e) {
                warn("release lock " + key, e);
            }
        }

        @Override
        public void close() {
            release();
        }
    }

    static void warn(String what, BackendException e) {
        long now = System.currentTimeMillis();
        if (now - lastWarning > 30_000) {
            lastWarning = now;
            Log.warn("Shared cache: could not {} — {}", what, e.getMessage());
        }
    }

    /** Back to memory, empty. For tests. */
    public static void resetShared() {
        backend = new MemoryBackend();
        SHARED.clear();
    }

    // ==================== Shared ====================

    /**
     * A typed view of the shared store: values travel as JSON, so the type is
     * a record, a bean, a String, a Number — anything Jackson reads back.
     */
    public static final class Shared<V> {

        private final String name;
        private final Class<V> type;
        private final Duration ttl;

        Shared(String name, Class<V> type, Duration ttl) {
            this.name = name;
            this.type = type;
            this.ttl = ttl;
        }

        public String name() {
            return name;
        }

        private String key(String key) {
            return name + ":" + Objects.requireNonNull(key, "key");
        }

        /** The value, when it is there and not expired. A store failure is a miss. */
        public Optional<V> get(String key) {
            try {
                String json = backend.get(key(key));
                return json == null ? Optional.empty() : Optional.ofNullable(read(json));
            } catch (BackendException e) {
                warn("read " + name, e);
                return Optional.empty();
            }
        }

        /** The value, or the supplier's — stored for the default time to live. */
        public V getOrSet(String key, Supplier<V> supplier) {
            return getOrSet(key, ttl, supplier);
        }

        /**
         * The value, or the supplier's, stored for {@code ttl}. Two instances
         * missing at once both compute; the second write wins.
         */
        public V getOrSet(String key, Duration ttl, Supplier<V> supplier) {
            Optional<V> cached = get(key);
            if (cached.isPresent()) return cached.get();
            V value = supplier.get();
            if (value != null) set(key, value, ttl);
            return value;
        }

        /** Stores the value for the default time to live. */
        public void set(String key, V value) {
            set(key, value, ttl);
        }

        /** Stores the value for {@code ttl}; null for as long as it is not deleted. */
        public void set(String key, V value, Duration ttl) {
            if (value == null) {
                delete(key);
                return;
            }
            try {
                backend.set(key(key), write(value), ttl);
            } catch (BackendException e) {
                warn("write " + name, e);
            }
        }

        public boolean delete(String key) {
            try {
                return backend.delete(key(key));
            } catch (BackendException e) {
                warn("delete from " + name, e);
                return false;
            }
        }

        /** A counter every instance adds to; new counters expire after {@code ttl}. */
        public long increment(String key, long delta, Duration ttl) {
            return backend.increment(key(key), delta, ttl);
        }

        /** Removes every entry of this cache, on every instance. */
        public int clear() {
            try {
                return backend.clear(name + ":");
            } catch (BackendException e) {
                warn("clear " + name, e);
                return 0;
            }
        }

        private String write(V value) {
            return type == String.class ? (String) value : Json.stringify(value);
        }

        @SuppressWarnings("unchecked")
        private V read(String json) {
            return type == String.class ? (V) json : Json.parse(json, type);
        }

        @Override
        public String toString() {
            return "Cache.Shared(" + name + ", " + type.getSimpleName() + ", " + ttl + ")";
        }
    }

    // ==================== the memory backend ====================

    /** The shared store on one instance: a map with expiry. The default, and the one tests see. */
    public static final class MemoryBackend implements Backend {

        private record Entry(String value, long expiresAt) {
            boolean expired(long now) {
                return expiresAt > 0 && expiresAt <= now;
            }
        }

        private final Map<String, Entry> entries = new ConcurrentHashMap<>();
        private final java.util.concurrent.atomic.AtomicInteger writes = new java.util.concurrent.atomic.AtomicInteger();

        private static long until(Duration ttl) {
            return ttl == null ? 0 : Instant.now().plus(ttl).toEpochMilli();
        }

        /**
         * Expiry is otherwise lazy — checked only when a key is read again — so
         * a key written once and never read (a per-window rate-limit counter, a
         * per-session value) would live forever. Every so many writes, sweep.
         */
        private void sweepEvery(int writesBetweenSweeps) {
            if (writes.incrementAndGet() % writesBetweenSweeps != 0) return;
            long now = System.currentTimeMillis();
            entries.values().removeIf(e -> e.expired(now));
        }

        @Override
        public String get(String key) {
            Entry entry = entries.get(key);
            if (entry == null) return null;
            if (entry.expired(System.currentTimeMillis())) {
                entries.remove(key, entry);
                return null;
            }
            return entry.value();
        }

        @Override
        public void set(String key, String value, Duration ttl) {
            entries.put(key, new Entry(value, until(ttl)));
            sweepEvery(1024);
        }

        @Override
        public boolean setIfAbsent(String key, String value, Duration ttl) {
            long now = System.currentTimeMillis();
            Entry fresh = new Entry(value, until(ttl));
            Entry result = entries.compute(key, (k, existing) -> existing == null || existing.expired(now) ? fresh : existing);
            sweepEvery(1024);
            return result == fresh;
        }

        @Override
        public boolean delete(String key) {
            return entries.remove(key) != null;
        }

        @Override
        public long increment(String key, long delta, Duration ttl) {
            long now = System.currentTimeMillis();
            Entry result = entries.compute(key, (k, existing) -> {
                if (existing == null || existing.expired(now)) return new Entry(Long.toString(delta), until(ttl));
                return new Entry(Long.toString(Long.parseLong(existing.value()) + delta), existing.expiresAt());
            });
            sweepEvery(1024);
            return Long.parseLong(result.value());
        }

        @Override
        public int clear(String prefix) {
            int before = entries.size();
            entries.keySet().removeIf(k -> k.startsWith(prefix));
            return before - entries.size();
        }

        public int size() {
            return entries.size();
        }

        @Override
        public String toString() {
            return "Cache.MemoryBackend";
        }
    }
}
