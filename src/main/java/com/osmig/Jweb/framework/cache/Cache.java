package com.osmig.Jweb.framework.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Simple in-memory cache with TTL support.
 *
 * <p>Usage:</p>
 * <pre>
 * // Create a cache
 * Cache&lt;String, User&gt; userCache = Cache.create();
 *
 * // Set with default TTL (5 minutes)
 * userCache.set("user:123", user);
 *
 * // Set with custom TTL
 * userCache.set("user:123", user, Duration.ofHours(1));
 *
 * // Get value
 * User user = userCache.get("user:123");
 *
 * // Get or compute if absent
 * User user = userCache.getOrSet("user:123", () -> fetchUser(123));
 *
 * // Check existence
 * if (userCache.has("user:123")) { ... }
 *
 * // Delete
 * userCache.delete("user:123");
 *
 * // Clear all
 * userCache.clear();
 * </pre>
 *
 * <p>Global cache:</p>
 * <pre>
 * // Use global cache for simple cases
 * Cache.global().set("key", value);
 * Object value = Cache.global().get("key");
 * </pre>
 *
 * <p>Named caches:</p>
 * <pre>
 * Cache&lt;String, User&gt; users = Cache.named("users");
 * Cache&lt;String, Product&gt; products = Cache.named("products");
 * </pre>
 */
public class Cache<K, V> {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    // Single shared scheduler and a weak registry of all live caches. Declared
    // before GLOBAL below because constructing any Cache registers it here, so
    // these must be initialized first (static fields init in textual order).
    private static final ScheduledExecutorService CLEANUP_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Cache-Cleanup");
                t.setDaemon(true);
                return t;
            });

    private static final Map<Cache<?, ?>, Boolean> LIVE_CACHES =
            Collections.synchronizedMap(new WeakHashMap<>());

    static {
        CLEANUP_SCHEDULER.scheduleAtFixedRate(Cache::sweepAll, 1, 1, TimeUnit.MINUTES);
    }

    private static final Cache<String, Object> GLOBAL = new Cache<>(DEFAULT_TTL);
    private static final Map<String, Cache<?, ?>> NAMED_CACHES = new ConcurrentHashMap<>();

    private final Map<K, CacheEntry<V>> store = new ConcurrentHashMap<>();
    private final Duration defaultTtl;
    private final int maxSize;

    private Cache(Duration defaultTtl) {
        this(defaultTtl, Integer.MAX_VALUE);
    }

    private Cache(Duration defaultTtl, int maxSize) {
        this.defaultTtl = defaultTtl;
        this.maxSize = maxSize;
        // Register with the single global sweeper via a weak reference. This
        // replaces a per-instance scheduled task that held a strong reference
        // to `this` forever, so short-lived caches can now be garbage
        // collected instead of leaking themselves plus a repeating task.
        registerForCleanup(this);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new cache with default TTL (5 minutes).
     */
    public static <K, V> Cache<K, V> create() {
        return new Cache<>(DEFAULT_TTL);
    }

    /**
     * Creates a new cache with custom default TTL.
     */
    public static <K, V> Cache<K, V> create(Duration defaultTtl) {
        return new Cache<>(defaultTtl);
    }

    /**
     * Creates a new cache with TTL and max size.
     */
    public static <K, V> Cache<K, V> create(Duration defaultTtl, int maxSize) {
        return new Cache<>(defaultTtl, maxSize);
    }

    /**
     * Returns the global cache instance.
     */
    @SuppressWarnings("unchecked")
    public static <V> Cache<String, V> global() {
        return (Cache<String, V>) GLOBAL;
    }

    /**
     * Returns or creates a named cache.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> named(String name) {
        return (Cache<K, V>) NAMED_CACHES.computeIfAbsent(name, k -> new Cache<>(DEFAULT_TTL));
    }

    /**
     * Returns or creates a named cache with custom TTL.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> named(String name, Duration ttl) {
        return (Cache<K, V>) NAMED_CACHES.computeIfAbsent(name, k -> new Cache<>(ttl));
    }

    // ==================== Core Operations ====================

    /**
     * Gets a value from the cache.
     * Returns null if not found or expired.
     */
    public V get(K key) {
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.value;
    }

    /**
     * Gets a value, or computes and caches it if absent.
     */
    public V getOrSet(K key, Supplier<V> supplier) {
        return getOrSet(key, supplier, defaultTtl);
    }

    /**
     * Gets a value, or computes and caches it with custom TTL.
     *
     * <p>The compute-and-store is atomic per key, so under concurrent misses
     * the supplier runs once rather than every caller stampeding the
     * (potentially expensive) supplier.</p>
     */
    public V getOrSet(K key, Supplier<V> supplier, Duration ttl) {
        CacheEntry<V> entry = store.compute(key, (k, existing) -> {
            if (existing != null && !existing.isExpired()) {
                return existing;
            }
            V computed = supplier.get();
            if (computed == null) {
                return null; // removes the mapping
            }
            return new CacheEntry<>(computed, Instant.now().plus(ttl));
        });
        return entry != null ? entry.value : null;
    }

    /**
     * Sets a value with default TTL.
     */
    public void set(K key, V value) {
        set(key, value, defaultTtl);
    }

    /**
     * Sets a value with custom TTL.
     */
    public void set(K key, V value, Duration ttl) {
        if (value == null) {
            delete(key);
            return;
        }

        // Evict if at max size
        if (store.size() >= maxSize) {
            evictOldest();
        }

        Instant expiresAt = Instant.now().plus(ttl);
        store.put(key, new CacheEntry<>(value, expiresAt));
    }

    /**
     * Sets a value that never expires.
     */
    public void setForever(K key, V value) {
        set(key, value, Duration.ofDays(365 * 100)); // 100 years
    }

    /**
     * Checks if a key exists and is not expired.
     */
    public boolean has(K key) {
        return get(key) != null;
    }

    /**
     * Deletes a key from the cache.
     */
    public boolean delete(K key) {
        return store.remove(key) != null;
    }

    /**
     * Clears all entries.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Returns the number of entries (including expired).
     */
    public int size() {
        return store.size();
    }

    /**
     * Returns the number of non-expired entries.
     */
    public int activeSize() {
        return (int) store.values().stream()
            .filter(e -> !e.isExpired())
            .count();
    }

    // ==================== Bulk Operations ====================

    /**
     * Gets multiple values.
     */
    public Map<K, V> getAll(Iterable<K> keys) {
        Map<K, V> result = new ConcurrentHashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Sets multiple values.
     */
    public void setAll(Map<K, V> entries) {
        setAll(entries, defaultTtl);
    }

    /**
     * Sets multiple values with custom TTL.
     */
    public void setAll(Map<K, V> entries, Duration ttl) {
        entries.forEach((k, v) -> set(k, v, ttl));
    }

    /**
     * Deletes multiple keys.
     */
    public void deleteAll(Iterable<K> keys) {
        for (K key : keys) {
            delete(key);
        }
    }

    // ==================== Advanced ====================

    /**
     * Updates a value if it exists.
     */
    public boolean update(K key, V value) {
        if (has(key)) {
            set(key, value);
            return true;
        }
        return false;
    }

    /**
     * Refreshes the TTL of an existing entry.
     */
    public boolean touch(K key) {
        return touch(key, defaultTtl);
    }

    /**
     * Refreshes the TTL with custom duration.
     */
    public boolean touch(K key, Duration ttl) {
        CacheEntry<V> entry = store.get(key);
        if (entry != null && !entry.isExpired()) {
            set(key, entry.value, ttl);
            return true;
        }
        return false;
    }

    /**
     * Gets remaining TTL for a key.
     */
    public Duration ttl(K key) {
        CacheEntry<V> entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            return Duration.ZERO;
        }
        return Duration.between(Instant.now(), entry.expiresAt);
    }

    /**
     * Removes expired entries.
     */
    public int cleanup() {
        int removed = 0;
        var iterator = store.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    // ==================== Helpers ====================

    private void evictOldest() {
        // Simple eviction: remove first expired, or oldest
        K oldestKey = null;
        Instant oldestTime = Instant.MAX;

        for (var entry : store.entrySet()) {
            if (entry.getValue().isExpired()) {
                store.remove(entry.getKey());
                return;
            }
            if (entry.getValue().expiresAt.isBefore(oldestTime)) {
                oldestTime = entry.getValue().expiresAt;
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            store.remove(oldestKey);
        }
    }

    // The shared scheduler and weak registry are declared near the top of the
    // class so they initialize before the GLOBAL cache instance. One task
    // sweeps every live cache; caches no longer referenced elsewhere are
    // collected and drop out of the registry on their own.
    private static void registerForCleanup(Cache<?, ?> cache) {
        LIVE_CACHES.put(cache, Boolean.TRUE);
    }

    private static void sweepAll() {
        List<Cache<?, ?>> snapshot;
        synchronized (LIVE_CACHES) {
            snapshot = new ArrayList<>(LIVE_CACHES.keySet());
        }
        for (Cache<?, ?> cache : snapshot) {
            if (cache != null) {
                try {
                    cache.cleanup();
                } catch (RuntimeException ignored) {
                    // A misbehaving cache must not stop the others being swept.
                }
            }
        }
    }

    // ==================== Entry ====================

    private record CacheEntry<V>(V value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // ==================== Stats ====================

    /**
     * Returns cache statistics.
     */
    public CacheStats stats() {
        int total = store.size();
        int active = activeSize();
        int expired = total - active;
        return new CacheStats(total, active, expired, maxSize);
    }

    /**
     * Cache statistics.
     */
    public record CacheStats(int total, int active, int expired, int maxSize) {}
}
