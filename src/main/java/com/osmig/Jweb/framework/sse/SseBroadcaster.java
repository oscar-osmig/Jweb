package com.osmig.Jweb.framework.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Broadcasts SSE events to multiple subscribers.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Create a broadcaster
 * SseBroadcaster broadcaster = new SseBroadcaster();
 *
 * // Subscribe endpoint
 * app.get("/events", req -> {
 *     SseEmitter emitter = SseEmitter.create(0); // no timeout
 *     broadcaster.subscribe(emitter);
 *     return emitter.toResponse();
 * });
 *
 * // Broadcast to all
 * broadcaster.broadcast("New notification!");
 * broadcaster.broadcast(SseEvent.json("update", data));
 *
 * // Cleanup on shutdown
 * broadcaster.shutdown();
 * }</pre>
 *
 * <h2>With Channels</h2>
 * <pre>{@code
 * SseBroadcaster broadcaster = new SseBroadcaster();
 *
 * app.get("/events/:channel", req -> {
 *     String channel = req.param("channel");
 *     SseEmitter emitter = SseEmitter.create(0);
 *     broadcaster.subscribe(channel, emitter);
 *     return emitter.toResponse();
 * });
 *
 * // Broadcast to specific channel
 * broadcaster.broadcast("news", SseEvent.of("Breaking news!"));
 * }</pre>
 */
public class SseBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcaster.class);

    private final Set<SseEmitter> globalEmitters = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Set<SseEmitter>> channelEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler;
    private final long heartbeatIntervalMs;

    /**
     * Creates a broadcaster with default heartbeat (15 seconds).
     */
    public SseBroadcaster() {
        this(15_000);
    }

    /**
     * Creates a broadcaster with custom heartbeat interval.
     *
     * @param heartbeatIntervalMs heartbeat interval in milliseconds (0 to disable)
     */
    public SseBroadcaster(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;

        if (heartbeatIntervalMs > 0) {
            this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

            heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeat,
                heartbeatIntervalMs,
                heartbeatIntervalMs,
                TimeUnit.MILLISECONDS
            );
        } else {
            this.heartbeatScheduler = null;
        }
    }

    /**
     * Subscribes an emitter to global broadcasts.
     *
     * @param emitter the emitter
     */
    public void subscribe(SseEmitter emitter) {
        globalEmitters.add(emitter);
        emitter.onComplete(e -> globalEmitters.remove(e));
        emitter.onError(e -> globalEmitters.remove(emitter));
        log.debug("Subscriber added. Total: {}", globalEmitters.size());
    }

    /**
     * Subscribes an emitter to a specific channel.
     *
     * @param channel the channel name
     * @param emitter the emitter
     */
    public void subscribe(String channel, SseEmitter emitter) {
        Set<SseEmitter> emitters = channelEmitters.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet());
        emitters.add(emitter);

        emitter.onComplete(e -> {
            emitters.remove(e);
            if (emitters.isEmpty()) {
                channelEmitters.remove(channel);
            }
        });

        emitter.onError(e -> emitters.remove(emitter));
        log.debug("Subscriber added to channel '{}'. Total: {}", channel, emitters.size());
    }

    /**
     * Broadcasts a message to all global subscribers.
     *
     * @param message the message
     */
    public void broadcast(String message) {
        broadcast(SseEvent.of(message));
    }

    /**
     * Broadcasts an event to all global subscribers.
     *
     * @param event the event
     */
    public void broadcast(SseEvent event) {
        for (SseEmitter emitter : globalEmitters) {
            emitter.send(event);
        }
    }

    /**
     * Broadcasts to a specific channel.
     *
     * @param channel the channel name
     * @param event the event
     */
    public void broadcast(String channel, SseEvent event) {
        Set<SseEmitter> emitters = channelEmitters.get(channel);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                emitter.send(event);
            }
        }
    }

    /**
     * Broadcasts to subscribers matching a predicate.
     *
     * @param predicate the filter predicate
     * @param event the event
     */
    public void broadcastIf(Predicate<SseEmitter> predicate, SseEvent event) {
        for (SseEmitter emitter : globalEmitters) {
            if (predicate.test(emitter)) {
                emitter.send(event);
            }
        }
    }

    /**
     * Gets the number of global subscribers.
     */
    public int getSubscriberCount() {
        return globalEmitters.size();
    }

    /**
     * Gets the number of subscribers for a channel.
     */
    public int getSubscriberCount(String channel) {
        Set<SseEmitter> emitters = channelEmitters.get(channel);
        return emitters != null ? emitters.size() : 0;
    }

    private void sendHeartbeat() {
        // Send comment as heartbeat (won't trigger client event handlers)
        for (SseEmitter emitter : globalEmitters) {
            emitter.sendComment("heartbeat");
        }
        for (Set<SseEmitter> emitters : channelEmitters.values()) {
            for (SseEmitter emitter : emitters) {
                emitter.sendComment("heartbeat");
            }
        }
    }

    /**
     * Shuts down the broadcaster.
     */
    public void shutdown() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
        }

        for (SseEmitter emitter : globalEmitters) {
            emitter.complete();
        }
        globalEmitters.clear();

        for (Set<SseEmitter> emitters : channelEmitters.values()) {
            for (SseEmitter emitter : emitters) {
                emitter.complete();
            }
        }
        channelEmitters.clear();

        log.info("SSE Broadcaster shut down");
    }
}
