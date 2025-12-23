package com.osmig.Jweb.framework.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Manages the lifecycle of all State instances.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates and tracks all State instances</li>
 *   <li>Generates unique IDs for states</li>
 *   <li>Notifies listeners of state changes (for WebSocket sync)</li>
 *   <li>Provides access to all states for serialization</li>
 * </ul>
 */
public final class StateManager {

    private static final AtomicLong idCounter = new AtomicLong(0);

    // Thread-local state context for request isolation
    private static final ThreadLocal<StateContext> currentContext = new ThreadLocal<>();

    // Context registry - maps session ID to context (for WebSocket lookup)
    private static final Map<String, StateContext> contextRegistry = new ConcurrentHashMap<>();

    // Global state change listeners (for WebSocket integration)
    private static final List<BiConsumer<State<?>, Object>> globalListeners = new ArrayList<>();

    // Non-blocking scheduled cleanup (replaces Thread.sleep patterns)
    private static final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "StateManager-Cleanup");
                t.setDaemon(true);
                return t;
            });

    // Context TTL in milliseconds (5 minutes)
    private static final long CONTEXT_TTL_MS = 5 * 60 * 1000;

    static {
        // Schedule periodic cleanup of stale contexts (runs every minute)
        cleanupScheduler.scheduleAtFixedRate(
                StateManager::cleanupStaleContexts,
                1, 1, TimeUnit.MINUTES
        );
    }

    private StateManager() {
        // Static utility class
    }

    /**
     * Removes stale contexts that haven't been explicitly cleaned up.
     * This prevents memory leaks from abandoned requests or exceptions.
     */
    private static void cleanupStaleContexts() {
        long now = System.currentTimeMillis();
        contextRegistry.entrySet().removeIf(entry -> {
            StateContext ctx = entry.getValue();
            return now - ctx.getCreatedAt() > CONTEXT_TTL_MS;
        });
    }

    /**
     * Creates a new State with an auto-generated ID.
     *
     * @param initialValue the initial value
     * @param <T> the type of the state
     * @return a new State instance
     */
    public static <T> State<T> createState(T initialValue) {
        String id = "state_" + idCounter.incrementAndGet();
        State<T> state = new State<>(id, initialValue);

        // Register with current context if available
        StateContext context = currentContext.get();
        if (context != null) {
            context.register(state);
        }

        return state;
    }

    /**
     * Creates a new State with a custom ID.
     *
     * @param id the state ID
     * @param initialValue the initial value
     * @param <T> the type of the state
     * @return a new State instance
     */
    public static <T> State<T> createState(String id, T initialValue) {
        State<T> state = new State<>(id, initialValue);

        StateContext context = currentContext.get();
        if (context != null) {
            context.register(state);
        }

        return state;
    }

    /**
     * Called when a state changes. Notifies global listeners.
     *
     * @param state the state that changed
     * @param oldValue the previous value
     * @param newValue the new value
     */
    static <T> void onStateChange(State<T> state, T oldValue, T newValue) {
        for (BiConsumer<State<?>, Object> listener : globalListeners) {
            listener.accept(state, newValue);
        }

        // Also notify the current context
        StateContext context = currentContext.get();
        if (context != null) {
            context.onStateChange(state);
        }
    }

    /**
     * Registers a global state change listener.
     * Used by WebSocket handler to push updates to clients.
     *
     * @param listener the listener to add
     */
    public static void addGlobalListener(BiConsumer<State<?>, Object> listener) {
        globalListeners.add(listener);
    }

    /**
     * Removes a global state change listener.
     *
     * @param listener the listener to remove
     */
    public static void removeGlobalListener(BiConsumer<State<?>, Object> listener) {
        globalListeners.remove(listener);
    }

    /**
     * Creates a new state context for a request/session.
     * Call this at the start of handling a request.
     *
     * @return the new context
     */
    public static StateContext createContext() {
        StateContext context = new StateContext();
        currentContext.set(context);
        contextRegistry.put(context.getSessionId(), context);
        return context;
    }

    /**
     * Gets a context by its session ID.
     * Used by WebSocket handler to restore context.
     *
     * @param sessionId the session ID
     * @return the context, or null if not found
     */
    public static StateContext getContextById(String sessionId) {
        return contextRegistry.get(sessionId);
    }

    /**
     * Sets the current thread's context.
     * Used by WebSocket handler to restore context for event handling.
     */
    public static void setContext(StateContext context) {
        currentContext.set(context);
    }

    /**
     * Gets the current state context.
     *
     * @return the current context, or null if none
     */
    public static StateContext getContext() {
        return currentContext.get();
    }

    /**
     * Clears the current state context.
     * Call this at the end of handling a request.
     */
    public static void clearContext() {
        currentContext.remove();
    }

    /**
     * Executes a runnable within a new state context.
     *
     * @param runnable the code to execute
     */
    public static void withContext(Runnable runnable) {
        StateContext context = createContext();
        try {
            runnable.run();
        } finally {
            clearContext();
        }
    }

    /**
     * Resets the ID counter. For testing purposes.
     */
    static void resetIdCounter() {
        idCounter.set(0);
    }

    /**
     * Represents a state context for a single request/session.
     * Tracks all states created during the request.
     */
    public static class StateContext {
        private final Map<String, State<?>> states = new ConcurrentHashMap<>();
        private final Set<State<?>> changedStates = ConcurrentHashMap.newKeySet();
        private final Map<String, RenderableComponent> components = new ConcurrentHashMap<>();
        private final String sessionId;
        private final long createdAt;

        StateContext() {
            this.createdAt = System.currentTimeMillis();
            this.sessionId = "ctx_" + createdAt + "_" + Thread.currentThread().threadId();
        }

        /**
         * Gets the creation timestamp of this context.
         * Used for TTL-based cleanup.
         */
        public long getCreatedAt() {
            return createdAt;
        }

        void register(State<?> state) {
            states.put(state.getId(), state);
        }

        void onStateChange(State<?> state) {
            changedStates.add(state);
        }

        /**
         * Registers a component for re-rendering.
         */
        public void registerComponent(String componentId, RenderableComponent component) {
            components.put(componentId, component);
        }

        /**
         * Gets all registered components.
         */
        public Map<String, RenderableComponent> getComponents() {
            return components;
        }

        /**
         * Gets a specific component.
         */
        public RenderableComponent getComponent(String componentId) {
            return components.get(componentId);
        }

        /**
         * Gets all states in this context.
         *
         * @return map of state ID to state
         */
        public Map<String, State<?>> getStates() {
            return states;
        }

        /**
         * Gets states that have changed since last clear.
         *
         * @return list of changed states
         */
        public List<State<?>> getChangedStates() {
            return new ArrayList<>(changedStates);
        }

        /**
         * Clears the changed states list.
         */
        public void clearChangedStates() {
            changedStates.clear();
            for (State<?> state : states.values()) {
                state.clearDirty();
            }
        }

        /**
         * Gets a state by ID.
         *
         * @param id the state ID
         * @return the state, or null if not found
         */
        @SuppressWarnings("unchecked")
        public <T> State<T> getState(String id) {
            return (State<T>) states.get(id);
        }

        /**
         * Gets the session ID for this context.
         *
         * @return the session ID
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * Serializes all states to JSON.
         *
         * @return JSON array of state objects
         */
        public String toJson() {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (State<?> state : states.values()) {
                if (!first) sb.append(",");
                sb.append(state.toJson());
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * Clears this context and removes it from the registry.
         * Call this when the request is complete to prevent memory leaks.
         */
        public void clearContext() {
            // Clear all states
            states.clear();
            changedStates.clear();
            components.clear();

            // Remove from registry to prevent memory leaks
            contextRegistry.remove(sessionId);

            // Clear thread-local if this is the current context
            if (currentContext.get() == this) {
                currentContext.remove();
            }
        }
    }
}
