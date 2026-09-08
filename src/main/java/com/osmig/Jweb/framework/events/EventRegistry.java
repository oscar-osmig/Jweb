package com.osmig.Jweb.framework.events;

import jweb.Event;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Registry that maps handler IDs to their Java lambda implementations.
 *
 * <p>Thread-safe and designed for concurrent access from multiple requests.</p>
 *
 * <p>The registry maintains handlers per session, so different users
 * have isolated handler namespaces.</p>
 */
public final class EventRegistry {

    private static final AtomicLong handlerIdCounter = new AtomicLong(0);

    // Session ID -> Handler ID -> Handler
    private static final Map<String, Map<String, EventHandler>> sessionHandlers = new ConcurrentHashMap<>();

    // For simple single-session use (e.g., development)
    private static final Map<String, EventHandler> globalHandlers = new ConcurrentHashMap<>();

    private EventRegistry() {
        // Static utility class
    }

    /**
     * Registers an event handler and returns a unique ID.
     *
     * <p>When a state context is active (i.e. during a page render), the
     * handler is scoped to that context and evicted with it. Handlers
     * registered outside a render fall back to the global registry.</p>
     *
     * @param eventType the DOM event type (click, change, submit, etc.)
     * @param handler the Java lambda to execute
     * @return the EventHandler with assigned ID
     */
    public static EventHandler register(String eventType, Consumer<Event> handler) {
        var context = com.osmig.Jweb.framework.state.StateManager.getContext();
        if (context != null) {
            return register(context.getSessionId(), eventType, handler);
        }
        String id = newHandlerId();
        EventHandler eventHandler = new EventHandler(id, eventType, handler);
        globalHandlers.put(id, eventHandler);
        return eventHandler;
    }

    /** Unique, unguessable handler ID (counter for uniqueness + random suffix). */
    private static String newHandlerId() {
        return "h_" + handlerIdCounter.incrementAndGet() + "_"
                + Long.toHexString(java.util.concurrent.ThreadLocalRandom.current().nextLong());
    }

    /**
     * Registers an event handler for a specific session.
     *
     * @param sessionId the session ID
     * @param eventType the DOM event type
     * @param handler the Java lambda
     * @return the EventHandler with assigned ID
     */
    public static EventHandler register(String sessionId, String eventType, Consumer<Event> handler) {
        String id = newHandlerId();
        EventHandler eventHandler = new EventHandler(id, eventType, handler);

        sessionHandlers
            .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
            .put(id, eventHandler);

        return eventHandler;
    }

    /**
     * Gets a handler by ID from the global registry.
     *
     * @param handlerId the handler ID
     * @return the handler, or null if not found
     */
    public static EventHandler get(String handlerId) {
        return globalHandlers.get(handlerId);
    }

    /**
     * Gets a handler by ID within one context — and only there. Handler ids
     * are capabilities: a message that names a context may run nothing but
     * that context's handlers, so there is no fallback to the global
     * registry (which serves renders that had no context at all).
     *
     * @param sessionId the context (session) ID
     * @param handlerId the handler ID
     * @return the handler, or null if the context has no such handler
     */
    public static EventHandler get(String sessionId, String handlerId) {
        if (sessionId == null) return null;
        Map<String, EventHandler> handlers = sessionHandlers.get(sessionId);
        return handlers == null ? null : handlers.get(handlerId);
    }

    /**
     * Executes a handler by ID with the given event.
     *
     * @param handlerId the handler ID
     * @param event the event to process
     * @return true if handler was found and executed
     */
    public static boolean execute(String handlerId, Event event) {
        EventHandler handler = get(handlerId);
        if (handler != null) {
            handler.handle(event);
            return true;
        }
        return false;
    }

    /**
     * Executes a handler within one context — no global fallback (see
     * {@link #get(String, String)}).
     *
     * @param sessionId the context (session) ID
     * @param handlerId the handler ID
     * @param event the event to process
     * @return true if handler was found in that context and executed
     */
    public static boolean execute(String sessionId, String handlerId, Event event) {
        EventHandler handler = get(sessionId, handlerId);
        if (handler != null) {
            handler.handle(event);
            return true;
        }
        return false;
    }

    /**
     * Removes all handlers for a session.
     * Call when a session ends or times out.
     *
     * @param sessionId the session ID
     */
    public static void clearSession(String sessionId) {
        sessionHandlers.remove(sessionId);
    }

    /**
     * Clears all global handlers.
     * Useful for testing or application restart.
     */
    public static void clearGlobal() {
        globalHandlers.clear();
    }

    /**
     * Clears all handlers (global and session).
     */
    public static void clearAll() {
        globalHandlers.clear();
        sessionHandlers.clear();
    }

    /**
     * Gets the total number of registered handlers.
     *
     * @return the handler count
     */
    public static int size() {
        int count = globalHandlers.size();
        for (Map<String, EventHandler> handlers : sessionHandlers.values()) {
            count += handlers.size();
        }
        return count;
    }

    /**
     * Resets the handler ID counter. For testing purposes.
     */
    static void resetIdCounter() {
        handlerIdCounter.set(0);
    }
}
