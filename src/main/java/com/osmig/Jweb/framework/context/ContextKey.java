package com.osmig.Jweb.framework.context;

/**
 * A type-safe key for Context values.
 *
 * <p>ContextKey provides compile-time type safety when working with Context.
 * Define keys as static fields and reuse them for type-safe access.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Define keys (typically as static fields in a class)
 * public static final ContextKey<User> USER = Context.key("user");
 * public static final ContextKey<Theme> THEME = Context.key("theme");
 * public static final ContextKey<Locale> LOCALE = Context.key("locale");
 *
 * // Provide context
 * Context.provide(USER, currentUser, () -> {
 *     return page.render();
 * });
 *
 * // Consume context (no casting needed!)
 * User user = Context.use(USER);
 * Theme theme = Context.useOrDefault(THEME, Theme.LIGHT);
 * }</pre>
 *
 * @param <T> the type of value this key holds
 * @see Context
 */
public record ContextKey<T>(String name) {

    /**
     * Creates a new context key.
     *
     * @param name the unique name for this context
     */
    public ContextKey {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Context key name cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return "ContextKey[" + name + "]";
    }
}
