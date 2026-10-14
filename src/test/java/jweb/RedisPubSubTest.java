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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisPubSub} speaks RESP to a small in-process Redis stand-in here —
 * AUTH, PING, PSUBSCRIBE, PUBLISH, the pmessage push — and to a real Redis
 * when {@code JWEB_TEST_REDIS_URL} is set (the CI workflow starts one).
 */
class RedisPubSubTest {

    private FakeRedis server;
    private RedisPubSub backend;

    @AfterEach
    void stop() throws IOException {
        if (backend != null) backend.stop();
        if (server != null) server.close();
    }

    @Test
    void parsesTheUrlForms() {
        RedisPubSub plain = new RedisPubSub("redis://cache.internal");
        assertEquals("cache.internal", plain.host());
        assertEquals(6379, plain.port());
        assertEquals("jweb:", plain.prefix());
        RedisPubSub full = new RedisPubSub("rediss://user:secret@host:6380/0", "app:");
        assertEquals(6380, full.port());
        assertEquals("app:", full.prefix());
        assertThrows(IllegalArgumentException.class, () -> new RedisPubSub("http://nope"));
    }

    @Test
    void publishesAndReceivesThroughTheServer() throws Exception {
        server = new FakeRedis("secret");
        backend = new RedisPubSub("redis://:secret@127.0.0.1:" + server.port(), "t:");
        List<String[]> received = new CopyOnWriteArrayList<>();
        CountDownLatch two = new CountDownLatch(2);
        backend.start((topic, json) -> {
            received.add(new String[]{topic, json});
            two.countDown();
        });
        awaitSubscribed(backend);

        backend.publish("room:1", "{\"a\":1}");
        backend.publish("room:2", "\"text with \\\"quotes\\\" and\\nnewline\"");
        assertTrue(two.await(5, TimeUnit.SECONDS), "two messages pushed back: " + received.size());
        assertEquals("room:1", received.get(0)[0]);
        assertEquals("{\"a\":1}", received.get(0)[1]);
        assertEquals("room:2", received.get(1)[0]);
        assertEquals("\"text with \\\"quotes\\\" and\\nnewline\"", received.get(1)[1]);
        assertEquals(List.of("AUTH", "PING", "PSUBSCRIBE"), server.commandsOn(0), "subscriber handshake");
        assertEquals(List.of("AUTH", "PING", "PUBLISH", "PUBLISH"), server.commandsOn(1), "publisher handshake");
    }

    @Test
    void wrongPasswordFailsThePublishAndTheSubscriptionStaysDown() throws Exception {
        server = new FakeRedis("secret");
        backend = new RedisPubSub("redis://:wrong@127.0.0.1:" + server.port());
        backend.start((t, j) -> {});
        Thread.sleep(300);
        assertFalse(backend.connected());
        PubSub.PublishException e = assertThrows(PubSub.PublishException.class, () -> backend.publish("x", "1"));
        assertTrue(e.getMessage().contains("unreachable"), e.getMessage());
    }

    @Test
    void reconnectsAfterTheServerComesBack() throws Exception {
        server = new FakeRedis(null);
        int port = server.port();
        backend = new RedisPubSub("redis://127.0.0.1:" + port);
        List<String> got = new CopyOnWriteArrayList<>();
        backend.start((t, j) -> got.add(t + "=" + j));
        awaitSubscribed(backend);
        backend.publish("a", "1");
        awaitSize(got, 1);

        server.close();
        awaitDisconnected(backend);
        server = new FakeRedis(null, port);
        awaitSubscribed(backend);

        backend.publish("b", "2");   // the publisher reconnects on this call
        awaitSize(got, 2);
        assertEquals(List.of("a=1", "b=2"), got);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JWEB_TEST_REDIS_URL", matches = ".+")
    void roundTripsThroughARealRedis() throws Exception {
        backend = new RedisPubSub(System.getenv("JWEB_TEST_REDIS_URL"), "jwebtest:");
        List<String> got = new CopyOnWriteArrayList<>();
        backend.start((t, j) -> got.add(t + "=" + j));
        awaitSubscribed(backend);
        backend.publish("real", "{\"ok\":true}");
        awaitSize(got, 1);
        assertEquals("real={\"ok\":true}", got.get(0));
    }

    // ==================== waiting ====================

    private static void awaitSubscribed(RedisPubSub backend) throws InterruptedException {
        for (int i = 0; i < 100 && !backend.connected(); i++) Thread.sleep(50);
        assertTrue(backend.connected(), "subscriber did not connect");
    }

    private static void awaitDisconnected(RedisPubSub backend) throws InterruptedException {
        for (int i = 0; i < 100 && backend.connected(); i++) Thread.sleep(50);
        assertFalse(backend.connected(), "subscriber did not notice the close");
    }

    private static void awaitSize(List<?> list, int size) throws InterruptedException {
        for (int i = 0; i < 100 && list.size() < size; i++) Thread.sleep(50);
        assertEquals(size, list.size(), "messages received: " + list);
    }

    // ==================== a Redis stand-in ====================

    /** Enough of Redis for pub/sub: AUTH, PING, PSUBSCRIBE (prefix patterns), PUBLISH. */
    static final class FakeRedis implements AutoCloseable {

        private final ServerSocket server;
        private final String password;
        private final Map<Integer, List<String>> commands = new ConcurrentHashMap<>();
        private final Map<Socket, List<String>> subscriptions = new ConcurrentHashMap<>();
        private final List<Socket> clients = new CopyOnWriteArrayList<>();
        private volatile boolean open = true;
        private int connections;

        FakeRedis(String password) throws IOException {
            this(password, 0);
        }

        FakeRedis(String password, int port) throws IOException {
            this.password = password;
            this.server = new ServerSocket();
            this.server.setReuseAddress(true);
            this.server.bind(new java.net.InetSocketAddress("127.0.0.1", port));
            Thread accept = new Thread(this::acceptLoop, "fake-redis");
            accept.setDaemon(true);
            accept.start();
        }

        int port() {
            return server.getLocalPort();
        }

        List<String> commandsOn(int connection) {
            return commands.getOrDefault(connection, List.of());
        }

        private void acceptLoop() {
            while (open) {
                try {
                    Socket client = server.accept();
                    int index;
                    synchronized (this) {
                        index = connections++;
                    }
                    clients.add(client);
                    Thread t = new Thread(() -> serve(client, index), "fake-redis-client-" + index);
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    return;
                }
            }
        }

        private void serve(Socket client, int index) {
            try {
                InputStream in = new BufferedInputStream(client.getInputStream());
                OutputStream out = new BufferedOutputStream(client.getOutputStream());
                boolean authed = password == null;
                while (open) {
                    List<String> command = readCommand(in);
                    if (command == null) return;
                    String name = command.get(0).toUpperCase();
                    commands.computeIfAbsent(index, i -> new CopyOnWriteArrayList<>()).add(name);
                    switch (name) {
                        case "AUTH" -> {
                            String given = command.get(command.size() - 1);
                            if (password != null && password.equals(given)) {
                                authed = true;
                                write(out, "+OK\r\n");
                            } else {
                                write(out, "-WRONGPASS invalid username-password pair\r\n");
                            }
                        }
                        case "PING" -> write(out, authed ? "+PONG\r\n" : "-NOAUTH Authentication required.\r\n");
                        case "PSUBSCRIBE" -> {
                            if (!authed) {
                                write(out, "-NOAUTH Authentication required.\r\n");
                                break;
                            }
                            String pattern = command.get(1);
                            subscriptions.computeIfAbsent(client, c -> new CopyOnWriteArrayList<>()).add(pattern);
                            write(out, "*3\r\n" + bulk("psubscribe") + bulk(pattern) + ":1\r\n");
                        }
                        case "PUBLISH" -> {
                            if (!authed) {
                                write(out, "-NOAUTH Authentication required.\r\n");
                                break;
                            }
                            String channel = command.get(1);
                            String message = command.get(2);
                            int delivered = 0;
                            for (var entry : subscriptions.entrySet()) {
                                for (String pattern : entry.getValue()) {
                                    String prefix = pattern.endsWith("*") ? pattern.substring(0, pattern.length() - 1) : pattern;
                                    if (channel.startsWith(prefix)) {
                                        OutputStream subscriber = entry.getKey().getOutputStream();
                                        synchronized (subscriber) {
                                            subscriber.write(("*4\r\n" + bulk("pmessage") + bulk(pattern) + bulk(channel) + bulk(message))
                                                .getBytes(StandardCharsets.UTF_8));
                                            subscriber.flush();
                                        }
                                        delivered++;
                                    }
                                }
                            }
                            write(out, ":" + delivered + "\r\n");
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
            synchronized (out) {
                out.write(s.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            open = false;
            server.close();
            for (Socket client : clients) {
                try {
                    client.close();
                } catch (IOException ignored) {
                    // closing anyway
                }
            }
        }
    }
}
