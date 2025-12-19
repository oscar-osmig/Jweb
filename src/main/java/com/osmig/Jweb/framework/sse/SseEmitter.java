package com.osmig.Jweb.framework.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Server-Sent Events emitter for real-time streaming.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // In your route handler
 * app.get("/events", req -> {
 *     SseEmitter emitter = SseEmitter.create();
 *
 *     // Send initial data
 *     emitter.send("Connected!");
 *
 *     // Store emitter for later use
 *     eventBus.subscribe(emitter);
 *
 *     return emitter.toResponse();
 * });
 *
 * // Later, broadcast to all subscribers
 * eventBus.broadcast(SseEvent.of("New message received"));
 * }</pre>
 */
public class SseEmitter {

    private static final Logger log = LoggerFactory.getLogger(SseEmitter.class);

    private final org.springframework.web.servlet.mvc.method.annotation.SseEmitter delegate;
    private final CopyOnWriteArrayList<Consumer<SseEmitter>> onCompleteCallbacks = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<Throwable>> onErrorCallbacks = new CopyOnWriteArrayList<>();
    private volatile boolean completed = false;

    private SseEmitter(long timeout) {
        this.delegate = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(timeout);

        delegate.onCompletion(() -> {
            completed = true;
            onCompleteCallbacks.forEach(cb -> cb.accept(this));
        });

        delegate.onError(ex -> {
            completed = true;
            onErrorCallbacks.forEach(cb -> cb.accept(ex));
        });

        delegate.onTimeout(() -> {
            completed = true;
            onCompleteCallbacks.forEach(cb -> cb.accept(this));
        });
    }

    /**
     * Creates an SSE emitter with default timeout (30 seconds).
     */
    public static SseEmitter create() {
        return new SseEmitter(30_000);
    }

    /**
     * Creates an SSE emitter with custom timeout.
     *
     * @param timeoutMs timeout in milliseconds (0 for no timeout)
     */
    public static SseEmitter create(long timeoutMs) {
        return new SseEmitter(timeoutMs);
    }

    /**
     * Sends a simple text message.
     *
     * @param data the message data
     */
    public void send(String data) {
        send(SseEvent.of(data));
    }

    /**
     * Sends an event.
     *
     * @param event the event to send
     */
    public void send(SseEvent event) {
        if (completed) {
            log.debug("Cannot send to completed emitter");
            return;
        }

        try {
            var builder = org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event();

            if (event.id() != null) {
                builder.id(event.id());
            }
            if (event.name() != null) {
                builder.name(event.name());
            }
            builder.data(event.data());

            if (event.retry() != null) {
                builder.reconnectTime(event.retry());
            }

            delegate.send(builder);
        } catch (IOException e) {
            log.debug("Failed to send SSE event: {}", e.getMessage());
            complete();
        }
    }

    /**
     * Sends a comment (keep-alive).
     *
     * @param comment the comment text
     */
    public void sendComment(String comment) {
        if (completed) return;
        try {
            delegate.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                .event().comment(comment));
        } catch (IOException e) {
            complete();
        }
    }

    /**
     * Completes the emitter.
     */
    public void complete() {
        if (!completed) {
            completed = true;
            delegate.complete();
        }
    }

    /**
     * Completes with an error.
     *
     * @param error the error
     */
    public void completeWithError(Throwable error) {
        if (!completed) {
            completed = true;
            delegate.completeWithError(error);
        }
    }

    /**
     * Checks if the emitter is completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Registers a completion callback.
     *
     * @param callback the callback
     * @return this emitter
     */
    public SseEmitter onComplete(Consumer<SseEmitter> callback) {
        onCompleteCallbacks.add(callback);
        return this;
    }

    /**
     * Registers an error callback.
     *
     * @param callback the callback
     * @return this emitter
     */
    public SseEmitter onError(Consumer<Throwable> callback) {
        onErrorCallbacks.add(callback);
        return this;
    }

    /**
     * Gets the Spring SseEmitter for returning from controller.
     */
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter toResponse() {
        return delegate;
    }
}
