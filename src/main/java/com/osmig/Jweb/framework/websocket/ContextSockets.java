package com.osmig.Jweb.framework.websocket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Which page is listening where: the render context a page announced on its
 * socket, mapped to the sink that reaches that page. The WebSocket handler
 * binds a sink that serializes to the socket; {@code jweb.RenderedPage} binds
 * one that collects, so a test can assert what a push would have sent.
 *
 * <p>This is how something that is not a browser event — a
 * {@code jweb.PubSub} message, a job finishing — reaches a page: run the
 * change in the page's context, flush it, and {@link #send} the messages
 * here.</p>
 */
public final class ContextSockets {

    private static final Map<String, Consumer<List<Object>>> SINKS = new ConcurrentHashMap<>();
    private static final Map<String, String> OWNERS = new ConcurrentHashMap<>();

    private ContextSockets() {}

    /** Routes pushes for the context to the sink (replacing an earlier one — a reconnect). */
    public static void bind(String contextId, Consumer<List<Object>> sink) {
        bind(contextId, null, sink);
    }

    /** As {@link #bind(String, Consumer)}, tagged with the socket session that owns it. */
    public static void bind(String contextId, String owner, Consumer<List<Object>> sink) {
        if (contextId == null) return;
        SINKS.put(contextId, sink);
        if (owner != null) OWNERS.put(contextId, owner); else OWNERS.remove(contextId);
    }

    /** Drops the context's sink if it is still this one. */
    public static void unbind(String contextId, Consumer<List<Object>> sink) {
        if (contextId != null && SINKS.remove(contextId, sink)) OWNERS.remove(contextId);
    }

    /** Drops the context's sink if the socket session that bound it is still the owner. */
    public static void unbindIfSession(String contextId, String owner) {
        if (contextId == null || owner == null) return;
        if (OWNERS.remove(contextId, owner)) SINKS.remove(contextId);
    }

    /** Drops whatever sink the context has. */
    public static void unbind(String contextId) {
        if (contextId != null) {
            SINKS.remove(contextId);
            OWNERS.remove(contextId);
        }
    }

    /** Whether a page is currently reachable for the context. */
    public static boolean isBound(String contextId) {
        return contextId != null && SINKS.containsKey(contextId);
    }

    /** Sends the messages to the page holding the context; {@code false} when none is listening. */
    public static boolean send(String contextId, List<Object> messages) {
        Consumer<List<Object>> sink = contextId == null ? null : SINKS.get(contextId);
        if (sink == null) return false;
        sink.accept(messages);
        return true;
    }

    /** How many contexts have a page listening. */
    public static int bound() {
        return SINKS.size();
    }
}
