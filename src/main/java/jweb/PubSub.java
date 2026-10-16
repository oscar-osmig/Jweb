package jweb;

import com.osmig.Jweb.framework.events.EventDispatch;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;
import com.osmig.Jweb.framework.websocket.ContextSockets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * One change, every page: publish a message to a topic and every page that
 * subscribed re-renders what the message touched — on this instance and, with
 * a shared backend, on every other.
 *
 * <pre>
 * record Post(String author, String text) {}
 *
 * // in a page: subscribe while rendering, and the page's live regions follow
 * State&lt;List&lt;Post&gt;&gt; posts = useState(Posts.recent(room));
 * PubSub.subscribe("room:" + room, Post.class, post -&gt; posts.mutate(list -&gt; list.add(post)));
 * return live(posts, list -&gt; ul(each(list, p -&gt; li(p.author() + ": " + p.text()))));
 *
 * // from anywhere — a handler, a REST endpoint, a job, another instance
 * PubSub.publish("room:" + room, new Post("ada", "hello"));
 * </pre>
 *
 * <p>A subscription made during a render belongs to that page's context: the
 * handler runs in the context, the live regions reading the states it changed
 * are re-rendered, and the patches go down the page's socket — exactly what a
 * browser event does, started from the server. It ends with the context
 * (closed, or reaped after idling). Subscribe once per page, in
 * {@code render()}, not inside a {@code live} body, which re-runs.</p>
 *
 * <p>A subscription made outside a render is a plain server-side listener
 * until its {@link Subscription#close()}.</p>
 *
 * <p>Messages are JSON on the wire — locally too, so a page behaves the same
 * on one instance and on five: the handler gets the message parsed to the
 * class it asked for. A {@code String} arrives as a {@code String}; a record,
 * a {@code Map} or a list as that.</p>
 *
 * <p><b>Backends.</b> {@link Memory} (the default) reaches the pages on this
 * instance. {@link RedisPubSub} reaches every instance through Redis
 * ({@code jweb.pubsub.redis.url}). Any {@link Backend} plugs in with
 * {@link #backend(Backend)}. Delivery runs on one thread in publish order;
 * a handler that throws is logged and the others still run.</p>
 */
public final class PubSub {

    /** Where messages go between instances. */
    public interface Backend {
        /** Sends a message to every instance, this one included. */
        void publish(String topic, String json);

        /** Starts delivering received messages to the receiver. */
        void start(BiConsumer<String, String> receiver);

        /** Releases what {@link #start} took. */
        default void stop() {}
    }

    /** A live subscription; {@link #close()} ends it. */
    public interface Subscription extends AutoCloseable {
        String topic();

        boolean active();

        @Override
        void close();
    }

    /** A publish the backend could not complete — Redis unreachable, for one. */
    public static class PublishException extends RuntimeException {
        public PublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Map<String, Set<Sub>> BY_TOPIC = new ConcurrentHashMap<>();
    private static final Map<String, Set<Sub>> BY_CONTEXT = new ConcurrentHashMap<>();
    private static final AtomicLong PUBLISHED = new AtomicLong();
    private static final AtomicLong DELIVERED = new AtomicLong();
    private static final ExecutorService DELIVERY = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "jweb-pubsub");
        t.setDaemon(true);
        return t;
    });
    private static volatile Backend backend;

    static {
        StateManager.onContextClosed(PubSub::contextClosed);
        backend(new Memory());
    }

    private PubSub() {}

    // ==================== publishing ====================

    /** Publishes the message (serialized as JSON) to everyone subscribed to the topic, on every instance. */
    public static void publish(String topic, Object message) {
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("topic is blank");
        PUBLISHED.incrementAndGet();
        backend.publish(topic, Json.stringify(message));
    }

    // ==================== subscribing ====================

    /**
     * Subscribes to the topic; each message arrives parsed as the type. During a
     * render the subscription is the page's and its live regions follow;
     * otherwise it is a plain listener.
     */
    public static <T> Subscription subscribe(String topic, Class<T> type, Consumer<? super T> handler) {
        return register(topic, type, handler, false);
    }

    /** Subscribes to the message as the JSON text it travels as, whatever was published. */
    public static Subscription subscribe(String topic, Consumer<String> handler) {
        return register(topic, String.class, handler, true);
    }

    private static <T> Subscription register(String topic, Class<T> type, Consumer<? super T> handler, boolean raw) {
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("topic is blank");
        StateManager.StateContext context = StateManager.getContext();
        Sub sub = new Sub(topic, type, handler, context == null ? null : context.getSessionId(), raw);
        BY_TOPIC.computeIfAbsent(topic, t -> ConcurrentHashMap.newKeySet()).add(sub);
        if (sub.contextId != null) {
            BY_CONTEXT.computeIfAbsent(sub.contextId, c -> ConcurrentHashMap.newKeySet()).add(sub);
        }
        return sub;
    }

    /** How many subscriptions the topic has on this instance. */
    public static int subscribers(String topic) {
        Set<Sub> subs = BY_TOPIC.get(topic);
        return subs == null ? 0 : subs.size();
    }

    /** The topics with at least one subscription on this instance. */
    public static Set<String> topics() {
        return Set.copyOf(BY_TOPIC.keySet());
    }

    // ==================== backends ====================

    /** Installs a backend: stops the current one, starts this one. */
    public static synchronized void backend(Backend next) {
        Backend current = backend;
        if (current != null) {
            try {
                current.stop();
            } catch (RuntimeException e) {
                Log.warn("PubSub backend {} did not stop cleanly: {}", current.getClass().getSimpleName(), e.getMessage());
            }
        }
        backend = next;
        next.start(PubSub::receive);
        jweb.Metrics.gauge("jweb.pubsub.subscriptions", () -> BY_TOPIC.values().stream().mapToInt(Set::size).sum());
    }

    /** The backend in use. */
    public static Backend backend() {
        return backend;
    }

    /** Published and delivered counts since start — for a dashboard or a test. */
    public static long published() {
        return PUBLISHED.get();
    }

    public static long delivered() {
        return DELIVERED.get();
    }

    /** Blocks until every message received so far has been delivered. For tests. */
    public static void awaitDelivery() {
        try {
            DELIVERY.submit(() -> {}).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("PubSub delivery did not settle: " + e.getMessage(), e);
        }
    }

    /** Drops every subscription. For tests. */
    public static void reset() {
        BY_TOPIC.clear();
        BY_CONTEXT.clear();
    }

    // ==================== delivery ====================

    private static void receive(String topic, String json) {
        DELIVERY.execute(() -> deliver(topic, json));
    }

    private static void deliver(String topic, String json) {
        Set<Sub> subs = BY_TOPIC.get(topic);
        if (subs == null || subs.isEmpty()) return;
        try (Trace.Span span = Trace.span("jweb.pubsub.deliver", "topic", topic)) {
            int delivered = 0;
            for (Sub sub : subs) {
                if (!sub.active) continue;
                try {
                    if (sub.contextId == null) {
                        sub.accept(json);
                    } else {
                        deliverToPage(sub, json);
                    }
                    DELIVERED.incrementAndGet();
                    delivered++;
                } catch (RuntimeException e) {
                    span.error(e);
                    Log.warn("PubSub handler for '{}' failed: {}", topic, e.getMessage());
                }
            }
            span.tag("subscribers", delivered);
        }
    }

    /**
     * The handler runs in the page's context (its states, its handlers), the
     * regions it changed are re-rendered, and the result goes down the page's
     * socket when one is open. No socket: the state is current for when the
     * page reconnects, and nothing is sent. The lookup does not count as
     * activity, so a page nobody is looking at still expires on schedule.
     */
    private static void deliverToPage(Sub sub, String json) {
        StateManager.StateContext context = StateManager.findContext(sub.contextId);
        if (context == null) {
            sub.close();   // the page is gone; the reaper's listener may not have run yet
            return;
        }
        // rendered and sent under the page's lock, so a browser event handled in between cannot
        // put a newer version of a region on the wire before this older one
        synchronized (context) {
            List<Object> messages = EventDispatch.push(context, () -> sub.accept(json));
            if (!messages.isEmpty()) {
                ContextSockets.send(sub.contextId, messages);
            }
        }
    }

    private static void contextClosed(String contextId) {
        Set<Sub> subs = BY_CONTEXT.remove(contextId);
        if (subs == null) return;
        for (Sub sub : subs) sub.close();
    }

    // ==================== the subscription ====================

    private static final class Sub implements Subscription {

        final String topic;
        final Class<?> type;
        final Consumer<Object> handler;
        final String contextId;
        final boolean raw;
        volatile boolean active = true;

        @SuppressWarnings("unchecked")
        <T> Sub(String topic, Class<T> type, Consumer<? super T> handler, String contextId, boolean raw) {
            this.topic = topic;
            this.type = type;
            this.handler = (Consumer<Object>) handler;
            this.contextId = contextId;
            this.raw = raw;
        }

        void accept(String json) {
            handler.accept(raw ? json : Json.parse(json, type));
        }

        @Override
        public String topic() {
            return topic;
        }

        @Override
        public boolean active() {
            return active;
        }

        @Override
        public void close() {
            active = false;
            Set<Sub> subs = BY_TOPIC.get(topic);
            if (subs != null) {
                subs.remove(this);
                if (subs.isEmpty()) BY_TOPIC.remove(topic, subs);
            }
            if (contextId != null) {
                Set<Sub> mine = BY_CONTEXT.get(contextId);
                if (mine != null) {
                    mine.remove(this);
                    if (mine.isEmpty()) BY_CONTEXT.remove(contextId, mine);
                }
            }
        }
    }

    // ==================== the in-memory backend ====================

    /** This instance only: a message reaches the pages this JVM holds. */
    public static final class Memory implements Backend {

        private volatile BiConsumer<String, String> receiver;

        @Override
        public void publish(String topic, String json) {
            BiConsumer<String, String> r = receiver;
            if (r != null) r.accept(topic, json);
        }

        @Override
        public void start(BiConsumer<String, String> receiver) {
            this.receiver = receiver;
        }

        @Override
        public void stop() {
            receiver = null;
        }

        @Override
        public String toString() {
            return "PubSub.Memory";
        }
    }
}
