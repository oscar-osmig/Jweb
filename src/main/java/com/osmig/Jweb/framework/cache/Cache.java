package com.osmig.Jweb.framework.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
    private static final Cache<String, Object> GLOBAL = new Cache<>(DEFAULT_TTL);
    private static final Map<String, Cache<?, ?>> NAMED_CACHES = new ConcurrentHashMap<>();

    private final Map<K, CacheEntry<V>> store = new ConcurrentHashMap<>();
    private final Duration defaultTtl;
    private final int maxSize;
    private volatile boolean cleanupScheduled = false;

    private Cache(Duration defaultTtl) {
        this(defaultTtl, Integer.MAX_VALUE);
    }

    private Cache(Duration defaultTtl, int maxSize) {
        this.defaultTtl = defaultTtl;
        this.maxSize = maxSize;
        scheduleCleanup();
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
     */
    public V getOrSet(K key, Supplier<V> supplier, Duration ttl) {
        V value = get(key);
        if (value != null) {
            return value;
        }
        value = supplier.get();
        if (value != null) {
            set(key, value, ttl);
        }
        return value;
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

    // Shared scheduler for all cache instances - non-blocking
    private static final ScheduledExecutorService CLEANUP_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Cache-Cleanup");
                t.setDaemon(true);
                return t;
            });

    private void scheduleCleanup() {
        if (cleanupScheduled) return;
        cleanupScheduled = true;

        // Non-blocking scheduled cleanup (replaces Thread.sleep)
        CLEANUP_SCHEDULER.scheduleAtFixedRate(
                this::cleanup,
                1, 1, TimeUnit.MINUTES
        );
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
