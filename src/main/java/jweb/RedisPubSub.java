package jweb;

import com.osmig.Jweb.framework.util.Log;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * {@link PubSub} across instances through Redis — PUBLISH on one, delivered on
 * all. Speaks the Redis protocol itself over a socket, so it needs no client
 * library and no Spring Data: set {@code jweb.pubsub.redis.url} and every
 * instance with the same URL shares every topic.
 *
 * <pre>
 * jweb:
 *   pubsub:
 *     redis:
 *       url: ${REDIS_URL:redis://localhost:6379}     # redis://[user[:password]@]host[:port], or rediss:// for TLS
 *       prefix: jweb:                                 # the channel a topic becomes: jweb:room:42
 * </pre>
 *
 * <p>Two connections: one subscribed to {@code prefix*} with a reader thread
 * that reconnects with backoff when the connection drops, one for publishing
 * that reconnects on the next publish. A publish while Redis is unreachable
 * throws {@link PubSub.PublishException}; nothing is queued.</p>
 */
public final class RedisPubSub implements PubSub.Backend {

    private final String host;
    private final int port;
    private final boolean tls;
    private final String username;
    private final String password;
    private final String prefix;

    private volatile BiConsumer<String, String> receiver;
    private volatile boolean running;
    private volatile boolean subscribed;
    private Thread reader;
    private volatile Socket readerSocket;

    private final Object publishLock = new Object();
    private Connection publisher;

    /** A backend for the URL, with channels prefixed {@code jweb:}. */
    public RedisPubSub(String url) {
        this(url, "jweb:");
    }

    public RedisPubSub(String url, String prefix) {
        URI uri = URI.create(url.trim());
        String scheme = uri.getScheme() == null ? "redis" : uri.getScheme().toLowerCase();
        if (!scheme.equals("redis") && !scheme.equals("rediss")) {
            throw new IllegalArgumentException("Redis URL must start with redis:// or rediss://, got " + url);
        }
        this.tls = scheme.equals("rediss");
        this.host = uri.getHost() == null ? "localhost" : uri.getHost();
        this.port = uri.getPort() < 0 ? 6379 : uri.getPort();
        String user = null, pass = null;
        if (uri.getUserInfo() != null) {
            String info = uri.getUserInfo();
            int colon = info.indexOf(':');
            if (colon < 0) {
                pass = info;
            } else {
                user = colon == 0 ? null : info.substring(0, colon);
                pass = info.substring(colon + 1);
            }
        }
        this.username = user;
        this.password = pass == null || pass.isEmpty() ? null : pass;
        this.prefix = prefix == null ? "" : prefix;
    }

    public static RedisPubSub of(String url) {
        return new RedisPubSub(url);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String prefix() {
        return prefix;
    }

    /** Whether the subscriber connection is up and subscribed right now. */
    public boolean connected() {
        return subscribed;
    }

    // ==================== Backend ====================

    @Override
    public void start(BiConsumer<String, String> receiver) {
        this.receiver = receiver;
        if (running) return;
        running = true;
        reader = new Thread(this::readLoop, "jweb-redis-pubsub");
        reader.setDaemon(true);
        reader.start();
    }

    @Override
    public void stop() {
        running = false;
        subscribed = false;
        Socket s = readerSocket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
        if (reader != null) reader.interrupt();
        synchronized (publishLock) {
            if (publisher != null) {
                publisher.close();
                publisher = null;
            }
        }
    }

    @Override
    public void publish(String topic, String json) {
        String channel = prefix + topic;
        synchronized (publishLock) {
            for (int attempt = 1; ; attempt++) {
                try {
                    if (publisher == null) {
                        publisher = connect();
                    }
                    publisher.command("PUBLISH", channel, json);
                    publisher.readReply();   // the number of receivers; not interesting
                    return;
                } catch (IOException e) {
                    if (publisher != null) {
                        publisher.close();
                        publisher = null;
                    }
                    if (attempt >= 2) {
                        throw new PubSub.PublishException("Redis at " + host + ":" + port + " is unreachable: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    // ==================== the subscriber ====================

    private void readLoop() {
        long backoff = 200;
        while (running) {
            Connection connection = null;
            try {
                connection = connect();
                readerSocket = connection.socket;
                connection.command("PSUBSCRIBE", prefix + "*");
                Object ack = connection.readReply();   // [psubscribe, pattern, count]
                if (!(ack instanceof List<?> list) || list.size() < 3) {
                    throw new IOException("Unexpected PSUBSCRIBE reply: " + ack);
                }
                subscribed = true;
                backoff = 200;
                Log.info("PubSub: subscribed to Redis {}:{} channels {}*", host, port, prefix);
                while (running) {
                    Object message = connection.readReply();
                    if (message instanceof List<?> parts && parts.size() == 4 && "pmessage".equals(parts.get(0))) {
                        String channel = String.valueOf(parts.get(2));
                        String payload = String.valueOf(parts.get(3));
                        String topic = channel.startsWith(prefix) ? channel.substring(prefix.length()) : channel;
                        BiConsumer<String, String> r = receiver;
                        if (r != null) r.accept(topic, payload);
                    }
                }
            } catch (IOException e) {
                subscribed = false;
                if (running) {
                    Log.warn("PubSub: Redis subscription lost ({}); reconnecting in {} ms", e.getMessage(), backoff);
                }
            } finally {
                subscribed = false;
                if (connection != null) connection.close();
                readerSocket = null;
            }
            if (!running) break;
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            backoff = Math.min(backoff * 2, 10_000);
        }
    }

    // ==================== one connection, speaking RESP ====================

    private Connection connect() throws IOException {
        Socket socket = tls ? SSLSocketFactory.getDefault().createSocket() : new Socket();
        socket.connect(new InetSocketAddress(host, port), 5_000);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        Connection connection = new Connection(socket);
        try {
            if (password != null) {
                if (username != null) {
                    connection.command("AUTH", username, password);
                } else {
                    connection.command("AUTH", password);
                }
                connection.readReply();   // +OK, or -ERR which throws
            }
            connection.command("PING");
            Object pong = connection.readReply();
            if (!"PONG".equals(pong)) {
                throw new IOException("Unexpected PING reply: " + pong);
            }
            return connection;
        } catch (IOException e) {
            connection.close();
            throw e;
        }
    }

    static final class Connection {

        final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedInputStream(socket.getInputStream());
            this.out = new BufferedOutputStream(socket.getOutputStream());
        }

        void command(String... args) throws IOException {
            out.write(('*' + Integer.toString(args.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
            for (String arg : args) {
                byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
                out.write(('$' + Integer.toString(bytes.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
                out.write(bytes);
                out.write('\r');
                out.write('\n');
            }
            out.flush();
        }

        /** A reply: String (simple or bulk), Long, List (array), or null; an error reply throws. */
        Object readReply() throws IOException {
            int type = in.read();
            if (type < 0) throw new EOFException("Redis closed the connection");
            String line = readLine();
            switch (type) {
                case '+':
                    return line;
                case '-':
                    throw new IOException("Redis error: " + line);
                case ':':
                    return Long.parseLong(line);
                case '$': {
                    int length = Integer.parseInt(line);
                    if (length < 0) return null;
                    byte[] bytes = in.readNBytes(length);
                    if (bytes.length < length) throw new EOFException("Redis closed the connection mid-reply");
                    readLine();   // the trailing CRLF
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                case '*': {
                    int count = Integer.parseInt(line);
                    if (count < 0) return null;
                    List<Object> items = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) items.add(readReply());
                    return items;
                }
                default:
                    throw new IOException("Unexpected Redis reply type '" + (char) type + "'");
            }
        }

        private String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c < 0) throw new EOFException("Redis closed the connection");
                if (c == '\r') {
                    int next = in.read();
                    if (next == '\n' || next < 0) return sb.toString();
                    sb.append((char) c).append((char) next);
                } else {
                    sb.append((char) c);
                }
            }
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
    }

    @Override
    public String toString() {
        return "RedisPubSub(" + host + ":" + port + ", " + prefix + "*)";
    }
}
