package jweb;

import com.osmig.Jweb.framework.util.Log;

import java.io.IOException;
import java.net.Socket;
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

    private final Redis.Endpoint endpoint;
    private final String prefix;

    private volatile BiConsumer<String, String> receiver;
    private volatile boolean running;
    private volatile boolean subscribed;
    private Thread reader;
    private volatile Socket readerSocket;

    private final Object publishLock = new Object();
    private Redis.Connection publisher;

    /** A backend for the URL, with channels prefixed {@code jweb:}. */
    public RedisPubSub(String url) {
        this(url, "jweb:");
    }

    public RedisPubSub(String url, String prefix) {
        this.endpoint = Redis.Endpoint.parse(url);
        this.prefix = prefix == null ? "" : prefix;
    }

    public static RedisPubSub of(String url) {
        return new RedisPubSub(url);
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
                        publisher = endpoint.connect();
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
                        throw new PubSub.PublishException("Redis at " + endpoint + " is unreachable: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    // ==================== the subscriber ====================

    private void readLoop() {
        long backoff = 200;
        while (running) {
            Redis.Connection connection = null;
            try {
                connection = endpoint.connect(0);   // a subscriber waits as long as it takes for the next message
                readerSocket = connection.socket;
                connection.command("PSUBSCRIBE", prefix + "*");
                Object ack = connection.readReply();   // [psubscribe, pattern, count]
                if (!(ack instanceof List<?> list) || list.size() < 3) {
                    throw new IOException("Unexpected PSUBSCRIBE reply: " + ack);
                }
                subscribed = true;
                backoff = 200;
                Log.info("PubSub: subscribed to Redis {} channels {}*", endpoint, prefix);
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
            } catch (IOException | RuntimeException e) {
                // a RuntimeException here (a receiver that threw, a reply that made no sense) must not
                // end the thread with running still true — nothing would ever resubscribe
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

    @Override
    public String toString() {
        return "RedisPubSub(" + endpoint + ", " + prefix + "*)";
    }
}
