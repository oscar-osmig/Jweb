package jweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisCache} against an in-process Redis stand-in that speaks the
 * key-value commands it needs — GET, SET PX/NX, DEL, INCRBY, PEXPIRE, SCAN —
 * with expiry, and against a real Redis when {@code JWEB_TEST_REDIS_URL} is
 * set (the CI workflow starts one).
 */
class RedisCacheTest {

    record Price(String sku, long cents) {}

    private FakeRedisStore server;
    private RedisCache cache;

    @AfterEach
    void stop() throws IOException {
        Cache.resetShared();
        if (cache != null) cache.stop();
        if (server != null) server.close();
    }

    @Test
    void parsesTheUrlForms() {
        Redis.Endpoint plain = Redis.Endpoint.parse("redis://cache.internal");
        assertEquals("cache.internal", plain.host());
        assertEquals(6379, plain.port());
        assertFalse(plain.tls());
        assertNull(plain.password());
        Redis.Endpoint full = Redis.Endpoint.parse("rediss://user:secret@host:6380/0");
        assertEquals(6380, full.port());
        assertTrue(full.tls());
        assertEquals("user", full.username());
        assertEquals("secret", full.password());
        assertNull(Redis.Endpoint.parse("redis://:pw@h").username());
        assertEquals("pw", Redis.Endpoint.parse("redis://:pw@h").password());
        assertThrows(IllegalArgumentException.class, () -> Redis.Endpoint.parse("http://nope"));
        RedisCache c = new RedisCache("redis://h:1", "p:");
        assertEquals("h", c.host());
        assertEquals(1, c.port());
        assertEquals("p:", c.prefix());
    }

    @Test
    void theBackendRoundTripsThroughTheServer() throws Exception {
        server = new FakeRedisStore("secret");
        cache = new RedisCache("redis://:secret@127.0.0.1:" + server.port(), "t:", 2);
        Cache.backend(cache);
        assertTrue(Cache.distributed());

        Cache.Shared<Price> prices = Cache.shared("prices", Price.class, Duration.ofMinutes(5));
        assertEquals(Optional.empty(), prices.get("a"));
        prices.set("a", new Price("a", 100));
        assertEquals(new Price("a", 100), prices.get("a").orElseThrow());
        assertEquals("{\"sku\":\"a\",\"cents\":100}", server.values.get("t:prices:a"));
        assertTrue(server.expiry.get("t:prices:a") > System.currentTimeMillis() + 250_000, "SET PX carried the ttl");

        prices.set("forever", new Price("f", 1), null);
        assertNull(server.expiry.get("t:prices:forever"), "no ttl, no expiry");

        prices.set("brief", new Price("b", 1), Duration.ofMillis(40));
        Thread.sleep(90);
        assertEquals(Optional.empty(), prices.get("brief"), "expired on the server");

        assertTrue(cache.setIfAbsent("lock:x", "me", Duration.ofMinutes(1)));
        assertFalse(cache.setIfAbsent("lock:x", "you", Duration.ofMinutes(1)));
        assertEquals("me", cache.get("lock:x"));
        assertTrue(Cache.lock("import", Duration.ofMinutes(1)).isPresent());
        assertTrue(Cache.lock("import", Duration.ofMinutes(1)).isEmpty());

        assertEquals(1L, cache.increment("hits", 1, Duration.ofMillis(40)));
        assertEquals(4L, cache.increment("hits", 3, Duration.ofMillis(40)));
        assertTrue(server.commandsNamed("PEXPIRE") == 1, "the expiry is set once, when the key is new");
        Thread.sleep(90);
        assertEquals(2L, cache.increment("hits", 2, Duration.ofMinutes(1)), "a fresh window after expiry");

        prices.set("b", new Price("b", 2));
        Cache.shared("other", Price.class).set("z", new Price("z", 9));
        assertEquals(3, prices.clear(), "a, forever, b");
        assertEquals(Optional.empty(), prices.get("a"));
        assertEquals(new Price("z", 9), Cache.shared("other", Price.class).get("z").orElseThrow(), "another cache's keys stay");
        assertTrue(cache.delete("lock:x"));
        assertFalse(cache.delete("lock:x"));

        for (int i = 0; i < 20; i++) cache.get("a");
        assertTrue(server.connections.get() <= 2, "connections are pooled: " + server.connections.get());

        // the server drops every connection: the next call reconnects
        server.dropClients();
        assertNull(cache.get("gone"));
        assertEquals("OK", "OK");

        // a command Redis refuses is an error, not a reconnect
        cache.set("text", "abc", null);
        Cache.BackendException refused = assertThrows(Cache.BackendException.class, () -> cache.increment("text", 1, null));
        assertTrue(refused.getMessage().contains("not an integer"), refused.getMessage());
        int before = server.connections.get();
        cache.get("text");
        assertEquals(before, server.connections.get(), "the connection survived the error reply");

        // the server is gone: an exception, which the shared view reads as a miss
        server.close();
        assertThrows(Cache.BackendException.class, () -> cache.get("a"));
        assertEquals(Optional.empty(), prices.get("a"));
        assertFalse(Cache.once("digest", Duration.ofMinutes(1), () -> {}));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JWEB_TEST_REDIS_URL", matches = ".+")
    void roundTripsThroughARealRedis() {
        String prefix = "jweb-test:" + Request.newId() + ":";
        cache = new RedisCache(System.getenv("JWEB_TEST_REDIS_URL"), prefix);
        Cache.backend(cache);
        Cache.Shared<Price> prices = Cache.shared("prices", Price.class);
        try {
            prices.set("a", new Price("a", 100));
            assertEquals(new Price("a", 100), prices.get("a").orElseThrow());
            assertEquals(1L, cache.increment("n", 1, Duration.ofMinutes(1)));
            assertEquals(3L, cache.increment("n", 2, Duration.ofMinutes(1)));
            assertTrue(Cache.lock("l", Duration.ofMinutes(1)).isPresent());
            assertTrue(Cache.lock("l", Duration.ofMinutes(1)).isEmpty());
            assertEquals(1, prices.clear());
            assertEquals(Optional.empty(), prices.get("a"));
        } finally {
            cache.clear("");
        }
    }

    // ==================== a Redis stand-in with keys ====================

    /** Enough of Redis for a cache: AUTH, PING, GET, SET [PX] [NX], DEL, INCRBY, PEXPIRE, SCAN. */
    static final class FakeRedisStore implements AutoCloseable {

        private final ServerSocket server;
        private final String password;
        final Map<String, String> values = new ConcurrentHashMap<>();
        final Map<String, Long> expiry = new ConcurrentHashMap<>();
        final List<String> commands = new CopyOnWriteArrayList<>();
        final AtomicInteger connections = new AtomicInteger();
        private final List<Socket> clients = new CopyOnWriteArrayList<>();
        private volatile boolean open = true;

        FakeRedisStore(String password) throws IOException {
            this.password = password;
            this.server = new ServerSocket();
            this.server.setReuseAddress(true);
            this.server.bind(new java.net.InetSocketAddress("127.0.0.1", 0));
            Thread accept = new Thread(this::acceptLoop, "fake-redis-store");
            accept.setDaemon(true);
            accept.start();
        }

        int port() {
            return server.getLocalPort();
        }

        long commandsNamed(String name) {
            return commands.stream().filter(name::equals).count();
        }

        void dropClients() {
            for (Socket client : clients) {
                try {
                    client.close();
                } catch (IOException ignored) {
                    // dropping
                }
            }
            clients.clear();
        }

        private void acceptLoop() {
            while (open) {
                try {
                    Socket client = server.accept();
                    connections.incrementAndGet();
                    clients.add(client);
                    Thread t = new Thread(() -> serve(client), "fake-redis-store-client");
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    return;
                }
            }
        }

        private boolean live(String key) {
            Long at = expiry.get(key);
            if (at != null && at <= System.currentTimeMillis()) {
                values.remove(key);
                expiry.remove(key);
                return false;
            }
            return values.containsKey(key);
        }

        private void serve(Socket client) {
            try {
                InputStream in = new BufferedInputStream(client.getInputStream());
                OutputStream out = new BufferedOutputStream(client.getOutputStream());
                boolean authed = password == null;
                while (open) {
                    List<String> command = readCommand(in);
                    if (command == null) return;
                    String name = command.get(0).toUpperCase();
                    commands.add(name);
                    if (!authed && !name.equals("AUTH")) {
                        write(out, "-NOAUTH Authentication required.\r\n");
                        continue;
                    }
                    switch (name) {
                        case "AUTH" -> {
                            if (password != null && password.equals(command.get(command.size() - 1))) {
                                authed = true;
                                write(out, "+OK\r\n");
                            } else {
                                write(out, "-WRONGPASS invalid username-password pair\r\n");
                            }
                        }
                        case "PING" -> write(out, "+PONG\r\n");
                        case "GET" -> write(out, live(command.get(1)) ? bulk(values.get(command.get(1))) : "$-1\r\n");
                        case "SET" -> {
                            String key = command.get(1);
                            boolean nx = command.contains("NX");
                            int px = command.indexOf("PX");
                            if (nx && live(key)) {
                                write(out, "$-1\r\n");
                                break;
                            }
                            values.put(key, command.get(2));
                            if (px > 0) expiry.put(key, System.currentTimeMillis() + Long.parseLong(command.get(px + 1))); else expiry.remove(key);
                            write(out, "+OK\r\n");
                        }
                        case "DEL" -> {
                            int n = 0;
                            for (String key : command.subList(1, command.size())) {
                                if (live(key)) {
                                    values.remove(key);
                                    expiry.remove(key);
                                    n++;
                                }
                            }
                            write(out, ":" + n + "\r\n");
                        }
                        case "INCRBY" -> {
                            String key = command.get(1);
                            String current = live(key) ? values.get(key) : "0";
                            long value;
                            try {
                                value = Long.parseLong(current) + Long.parseLong(command.get(2));
                            } catch (NumberFormatException e) {
                                write(out, "-ERR value is not an integer or out of range\r\n");
                                break;
                            }
                            values.put(key, Long.toString(value));
                            write(out, ":" + value + "\r\n");
                        }
                        case "PEXPIRE" -> {
                            String key = command.get(1);
                            if (live(key)) {
                                expiry.put(key, System.currentTimeMillis() + Long.parseLong(command.get(2)));
                                write(out, ":1\r\n");
                            } else {
                                write(out, ":0\r\n");
                            }
                        }
                        case "SCAN" -> {
                            String pattern = command.get(command.indexOf("MATCH") + 1);
                            String prefix = pattern.substring(0, pattern.length() - 1).replace("\\", "");
                            List<String> keys = new ArrayList<>();
                            for (String key : values.keySet()) if (key.startsWith(prefix) && live(key)) keys.add(key);
                            StringBuilder reply = new StringBuilder("*2\r\n").append(bulk("0")).append("*").append(keys.size()).append("\r\n");
                            for (String key : keys) reply.append(bulk(key));
                            write(out, reply.toString());
                        }
                        default -> write(out, "-ERR unknown command '" + name + "'\r\n");
                    }
                }
            } catch (IOException ignored) {
                // the client went away
            }
        }

        private static List<String> readCommand(InputStream in) throws IOException {
            int type = in.read();
            if (type < 0) return null;
            if (type != '*') throw new IOException("expected an array, got " + (char) type);
            int count = Integer.parseInt(readLine(in));
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int t = in.read();
                if (t != '$') throw new IOException("expected a bulk string");
                int length = Integer.parseInt(readLine(in));
                byte[] bytes = in.readNBytes(length);
                readLine(in);
                parts.add(new String(bytes, StandardCharsets.UTF_8));
            }
            return parts;
        }

        private static String readLine(InputStream in) throws IOException {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = in.read()) >= 0) {
                if (c == '\r') {
                    in.read();
                    return sb.toString();
                }
                sb.append((char) c);
            }
            throw new IOException("eof");
        }

        private static String bulk(String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            return "$" + bytes.length + "\r\n" + s + "\r\n";
        }

        private static void write(OutputStream out, String s) throws IOException {
            out.write(s.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        @Override
        public void close() throws IOException {
            open = false;
            server.close();
            dropClients();
        }
    }
}
