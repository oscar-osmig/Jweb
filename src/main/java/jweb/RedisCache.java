package jweb;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The shared cache in Redis: {@code GET}/{@code SET PX}/{@code SET NX PX},
 * {@code INCRBY} with an expiry, {@code SCAN} to clear a prefix — spoken by
 * the framework itself over a few pooled connections, no client library.
 * Set {@code jweb.cache.redis.url} (or {@code jweb.redis.url}) and every
 * instance with the same URL shares {@code Cache.shared(...)}, the locks and
 * the rate limits.
 *
 * <pre>
 * jweb:
 *   cache:
 *     redis:
 *       url: ${REDIS_URL:redis://localhost:6379}     # redis://[user[:password]@]host[:port], rediss:// for TLS
 *       prefix: "jweb:cache:"                         # every key starts with it
 * </pre>
 *
 * <p>A command that fails on a dropped connection is retried once on a fresh
 * one; then {@link Cache.BackendException} — which {@code Cache.Shared} reads
 * as a miss, a lock as "not acquired", a rate limit as "let it through".</p>
 */
public final class RedisCache implements Cache.Backend {

    private final Redis.Endpoint endpoint;
    private final String prefix;
    private final BlockingQueue<Redis.Connection> pool;
    private volatile boolean stopped;

    public RedisCache(String url) {
        this(url, "jweb:cache:");
    }

    public RedisCache(String url, String prefix) {
        this(url, prefix, 4);
    }

    /** @param connections how many connections to keep open at most */
    public RedisCache(String url, String prefix, int connections) {
        this.endpoint = Redis.Endpoint.parse(url);
        this.prefix = prefix == null ? "" : prefix;
        this.pool = new ArrayBlockingQueue<>(Math.max(1, connections));
    }

    public String host() {
        return endpoint.host();
    }

    public int port() {
        return endpoint.port();
    }

    public String prefix() {
        return prefix;
    }

    // ==================== Backend ====================

    @Override
    public String get(String key) {
        Object reply = call("GET", prefix + key);
        return reply == null ? null : String.valueOf(reply);
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        if (ttl == null) {
            call("SET", prefix + key, value);
        } else {
            call("SET", prefix + key, value, "PX", Long.toString(millis(ttl)));
        }
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        Object reply = ttl == null
            ? call("SET", prefix + key, value, "NX")
            : call("SET", prefix + key, value, "PX", Long.toString(millis(ttl)), "NX");
        return "OK".equals(reply);
    }

    @Override
    public boolean delete(String key) {
        Object reply = call("DEL", prefix + key);
        return reply instanceof Long n && n > 0;
    }

    @Override
    public long increment(String key, long delta, Duration ttl) {
        String k = prefix + key;
        Object reply = call("INCRBY", k, Long.toString(delta));
        long value = reply instanceof Long n ? n : Long.parseLong(String.valueOf(reply));
        if (ttl != null && value == delta) {
            // the key was new: give it its expiry (a key that existed keeps its own)
            call("PEXPIRE", k, Long.toString(millis(ttl)));
        }
        return value;
    }

    @Override
    public int clear(String keyPrefix) {
        String pattern = (prefix + keyPrefix).replace("*", "\\*").replace("?", "\\?").replace("[", "\\[") + "*";
        int removed = 0;
        String cursor = "0";
        do {
            Object reply = call("SCAN", cursor, "MATCH", pattern, "COUNT", "500");
            if (!(reply instanceof List<?> page) || page.size() < 2) break;
            cursor = String.valueOf(page.get(0));
            if (page.get(1) instanceof List<?> keys && !keys.isEmpty()) {
                List<String> args = new ArrayList<>(keys.size() + 1);
                args.add("DEL");
                for (Object k : keys) args.add(String.valueOf(k));
                Object deleted = call(args.toArray(String[]::new));
                if (deleted instanceof Long n) removed += n.intValue();
            }
        } while (!"0".equals(cursor));
        return removed;
    }

    @Override
    public void stop() {
        stopped = true;
        Redis.Connection c;
        while ((c = pool.poll()) != null) c.close();
    }

    private static long millis(Duration ttl) {
        return Math.max(1, ttl.toMillis());
    }

    // ==================== the pool ====================

    private Object call(String... args) {
        IOException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            Redis.Connection connection = null;
            boolean broken = false;
            try {
                connection = borrow();
                return connection.call(args);
            } catch (IOException e) {
                last = e;
                broken = true;
                if (e.getMessage() != null && e.getMessage().startsWith("Redis error:")) {
                    // the server answered: the connection is fine, the command was not
                    broken = false;
                    throw new Cache.BackendException(e.getMessage(), e);
                }
            } catch (RuntimeException e) {
                // whatever went wrong mid-exchange, the connection may hold half a reply: never pool it
                broken = true;
                throw e;
            } finally {
                if (connection != null) {
                    if (broken || stopped) connection.close(); else if (!pool.offer(connection)) connection.close();
                }
            }
        }
        throw new Cache.BackendException("Redis at " + endpoint + " is unreachable: " + (last == null ? "" : last.getMessage()), last);
    }

    private Redis.Connection borrow() throws IOException {
        Redis.Connection connection = pool.poll();
        return connection != null ? connection : endpoint.connect();
    }

    @Override
    public String toString() {
        return "RedisCache(" + endpoint + ", " + prefix + "*)";
    }
}
