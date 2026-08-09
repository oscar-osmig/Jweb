package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.server.Request;

import java.util.UUID;

/**
 * Type-safe query parameters — declare name, type and default once, read
 * everywhere without parsing or null checks.
 *
 * <pre>
 * static final Query&lt;Integer&gt; PAGE = Query.of("page", Integer.class).orElse(1);
 * static final Query&lt;String&gt;  SORT = Query.of("sort", String.class).orElse("date");
 * static final Query&lt;Long&gt;    USER = Query.of("userId", Long.class).required();
 *
 * app.get("/products", req -> productList(PAGE.from(req), SORT.from(req)));
 * </pre>
 *
 * Supported types: String, Integer, Long, Double, Boolean, UUID.
 */
public final class Query<T> {

    private final String name;
    private final Class<T> type;
    private final T defaultValue;
    private final boolean required;

    private Query(String name, Class<T> type, T defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    /** Declares a query parameter by name and type (optional, null default). */
    public static <T> Query<T> of(String name, Class<T> type) {
        return new Query<>(name, type, null, false);
    }

    /** Returns a copy with a default used when the parameter is absent or invalid. */
    public Query<T> orElse(T defaultValue) {
        return new Query<>(name, type, defaultValue, false);
    }

    /** Returns a copy that throws {@link TypedRoute.RouteParamException} when absent. */
    public Query<T> required() {
        return new Query<>(name, type, null, true);
    }

    /** The parameter name. */
    public String name() {
        return name;
    }

    /**
     * Reads this parameter from the request: parsed value, or the default
     * when absent/invalid, or an exception when {@link #required()}.
     */
    public T from(Request request) {
        String raw = request.query(name);
        if (raw == null || raw.isBlank()) {
            if (required) {
                throw new TypedRoute.RouteParamException(name, "(missing)", type);
            }
            return defaultValue;
        }
        try {
            return convert(raw);
        } catch (IllegalArgumentException e) {
            if (required || defaultValue == null) {
                throw new TypedRoute.RouteParamException(name, raw, type);
            }
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private T convert(String raw) {
        if (type == String.class) return (T) raw;
        if (type == Integer.class) return (T) Integer.valueOf(raw);
        if (type == Long.class) return (T) Long.valueOf(raw);
        if (type == Double.class) return (T) Double.valueOf(raw);
        if (type == Boolean.class) return (T) Boolean.valueOf(raw);
        if (type == UUID.class) return (T) UUID.fromString(raw);
        throw new IllegalArgumentException("Unsupported query param type: " + type.getName());
    }
}
