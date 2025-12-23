package com.osmig.Jweb.framework.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Context API for passing data through the component tree without prop drilling.
 *
 * <p>Context provides a way to share values between components without explicitly
 * passing props through every level of the tree. This is similar to React's Context API
 * but implemented for server-side rendering.</p>
 *
 * <h2>Basic Usage with String Keys</h2>
 * <pre>{@code
 * // Provide context at a high level
 * Context.provide("theme", darkTheme, () -> {
 *     return layout.render();  // All children can access theme
 * });
 *
 * // Consume context anywhere in the tree
 * Theme theme = Context.get("theme", Theme.class);
 * }</pre>
 *
 * <h2>Type-Safe Context Keys</h2>
 * <pre>{@code
 * // Define a typed context key (typically as a static field)
 * public static final ContextKey<User> USER = Context.key("user");
 * public static final ContextKey<Theme> THEME = Context.key("theme");
 *
 * // Provide with type safety
 * Context.provide(USER, currentUser, () -> page.render());
 *
 * // Consume with type safety (no casting needed)
 * User user = Context.use(USER);
 * Optional<User> maybeUser = Context.find(USER);
 * }</pre>
 *
 * <h2>Multiple Context Values</h2>
 * <pre>{@code
 * Context.provide(USER, currentUser)
 *        .provide(THEME, darkTheme)
 *        .provide("locale", Locale.US)
 *        .run(() -> page.render());
 * }</pre>
 *
 * <h2>Default Values</h2>
 * <pre>{@code
 * // Returns default if context not provided
 * Theme theme = Context.getOrDefault("theme", Theme.class, Theme.LIGHT);
 * User user = Context.useOrDefault(USER, User.anonymous());
 * }</pre>
 *
 * @see ContextKey
 */
public final class Context {

    private static final ThreadLocal<Map<String, Object>> contextStack =
        ThreadLocal.withInitial(HashMap::new);

    private Context() {}

    // ==================== Type-Safe Context Keys ====================

    /**
     * Creates a type-safe context key.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * public static final ContextKey<User> USER = Context.key("user");
     * }</pre>
     *
     * @param name the unique name for this context
     * @param <T> the type of value this key holds
     * @return a new context key
     */
    public static <T> ContextKey<T> key(String name) {
        return new ContextKey<>(name);
    }

    // ==================== Provide Context ====================

    /**
     * Provides a context value for the duration of a computation.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * Element result = Context.provide("user", currentUser, () -> {
     *     return page.render();
     * });
     * }</pre>
     *
     * @param key the context key
     * @param value the value to provide
     * @param computation the computation to run with this context
     * @param <T> the return type
     * @return the result of the computation
     */
    public static <T> T provide(String key, Object value, Supplier<T> computation) {
        Map<String, Object> context = contextStack.get();
        Object previous = context.get(key);
        try {
            context.put(key, value);
            return computation.get();
        } finally {
            if (previous == null) {
                context.remove(key);
            } else {
                context.put(key, previous);
            }
        }
    }

    /**
     * Provides a context value using a type-safe key.
     *
     * @param key the typed context key
     * @param value the value to provide
     * @param computation the computation to run
     * @param <T> the context value type
     * @param <R> the return type
     * @return the result of the computation
     */
    public static <T, R> R provide(ContextKey<T> key, T value, Supplier<R> computation) {
        return provide(key.name(), value, computation);
    }

    /**
     * Provides a context value and runs a void computation.
     *
     * @param key the context key
     * @param value the value to provide
     * @param computation the computation to run
     */
    public static void provide(String key, Object value, Runnable computation) {
        provide(key, value, () -> {
            computation.run();
            return null;
        });
    }

    /**
     * Provides a context value using a type-safe key (void).
     *
     * @param key the typed context key
     * @param value the value to provide
     * @param computation the computation to run
     * @param <T> the context value type
     */
    public static <T> void provide(ContextKey<T> key, T value, Runnable computation) {
        provide(key.name(), value, computation);
    }

    /**
     * Starts a builder for providing multiple context values.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * Context.provide(USER, currentUser)
     *        .provide(THEME, darkTheme)
     *        .run(() -> page.render());
     * }</pre>
     *
     * @param key the first context key
     * @param value the first value
     * @param <T> the value type
     * @return a context builder for chaining
     */
    public static <T> ContextBuilder provide(ContextKey<T> key, T value) {
        return new ContextBuilder().provide(key, value);
    }

    // ==================== Consume Context ====================

    /**
     * Gets a context value by key.
     *
     * @param key the context key
     * @param type the expected type
     * @param <T> the value type
     * @return the context value
     * @throws IllegalStateException if context is not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> type) {
        Object value = contextStack.get().get(key);
        if (value == null) {
            throw new IllegalStateException("Context not found: " + key);
        }
        return (T) value;
    }

    /**
     * Gets a context value using a type-safe key.
     *
     * @param key the typed context key
     * @param <T> the value type
     * @return the context value
     * @throws IllegalStateException if context is not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T use(ContextKey<T> key) {
        Object value = contextStack.get().get(key.name());
        if (value == null) {
            throw new IllegalStateException("Context not found: " + key.name());
        }
        return (T) value;
    }

    /**
     * Finds a context value, returning empty if not found.
     *
     * @param key the context key
     * @param type the expected type
     * @param <T> the value type
     * @return an Optional containing the value, or empty
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> find(String key, Class<T> type) {
        return Optional.ofNullable((T) contextStack.get().get(key));
    }

    /**
     * Finds a context value using a type-safe key.
     *
     * @param key the typed context key
     * @param <T> the value type
     * @return an Optional containing the value, or empty
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> find(ContextKey<T> key) {
        return Optional.ofNullable((T) contextStack.get().get(key.name()));
    }

    /**
     * Gets a context value or returns a default.
     *
     * @param key the context key
     * @param type the expected type
     * @param defaultValue the default value if not found
     * @param <T> the value type
     * @return the context value or default
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        Object value = contextStack.get().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Gets a context value using a type-safe key or returns a default.
     *
     * @param key the typed context key
     * @param defaultValue the default value if not found
     * @param <T> the value type
     * @return the context value or default
     */
    @SuppressWarnings("unchecked")
    public static <T> T useOrDefault(ContextKey<T> key, T defaultValue) {
        Object value = contextStack.get().get(key.name());
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Checks if a context value is present.
     *
     * @param key the context key
     * @return true if the context exists
     */
    public static boolean has(String key) {
        return contextStack.get().containsKey(key);
    }

    /**
     * Checks if a context value is present using a type-safe key.
     *
     * @param key the typed context key
     * @return true if the context exists
     */
    public static boolean has(ContextKey<?> key) {
        return contextStack.get().containsKey(key.name());
    }

    // ==================== Clear Context ====================

    /**
     * Clears all context values for the current thread.
     * Typically called at the end of request processing.
     */
    public static void clear() {
        contextStack.get().clear();
    }

    // ==================== Context Builder ====================

    /**
     * Builder for providing multiple context values at once.
     */
    public static class ContextBuilder {
        private final Map<String, Object> values = new HashMap<>();

        /**
         * Adds a context value to provide.
         *
         * @param key the typed context key
         * @param value the value
         * @param <T> the value type
         * @return this builder
         */
        public <T> ContextBuilder provide(ContextKey<T> key, T value) {
            values.put(key.name(), value);
            return this;
        }

        /**
         * Adds a context value with a string key.
         *
         * @param key the context key
         * @param value the value
         * @return this builder
         */
        public ContextBuilder provide(String key, Object value) {
            values.put(key, value);
            return this;
        }

        /**
         * Runs a computation with all provided context values.
         *
         * @param computation the computation to run
         * @param <T> the return type
         * @return the result of the computation
         */
        public <T> T run(Supplier<T> computation) {
            Map<String, Object> context = contextStack.get();
            Map<String, Object> previous = new HashMap<>();

            try {
                // Store previous values and set new ones
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    previous.put(entry.getKey(), context.get(entry.getKey()));
                    context.put(entry.getKey(), entry.getValue());
                }
                return computation.get();
            } finally {
                // Restore previous values
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    Object prev = previous.get(entry.getKey());
                    if (prev == null) {
                        context.remove(entry.getKey());
                    } else {
                        context.put(entry.getKey(), prev);
                    }
                }
            }
        }

        /**
         * Runs a void computation with all provided context values.
         *
         * @param computation the computation to run
         */
        public void run(Runnable computation) {
            run(() -> {
                computation.run();
                return null;
            });
        }
    }
}
