package jweb;

import java.util.UUID;

/**
 * A typed query parameter — declare name, type and default once, read it
 * everywhere without parsing or null checks:
 *
 * <pre>{@code
 * static final QueryParam<Integer> PAGE = QueryParam.of("page", Integer.class).orElse(1);
 * static final QueryParam<String>  SORT = QueryParam.of("sort", String.class).orElse("date");
 * static final QueryParam<Long>    USER = QueryParam.of("userId", Long.class).required();
 *
 * app.get("/products", req -> productList(PAGE.from(req), SORT.from(req)));
 * }</pre>
 *
 * <p>Supported types: String, Integer, Long, Double, Boolean, UUID. (The
 * {@code @Query} annotation in {@code jweb.api} is the {@code @REST}-method
 * form of the same idea; this class is for router handlers.)</p>
 *
 * @param <T> the parameter's Java type
 */
public class QueryParam<T> {

    private final String name;
    private final Class<T> type;
    private final T defaultValue;
    private final boolean required;

    protected QueryParam(String name, Class<T> type, T defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    /** Declares a query parameter by name and type (optional, null default). */
    public static <T> QueryParam<T> of(String name, Class<T> type) {
        return new QueryParam<>(name, type, null, false);
    }

    /** Returns a copy with a default used when the parameter is absent or invalid. */
    public QueryParam<T> orElse(T defaultValue) {
        return new QueryParam<>(name, type, defaultValue, false);
    }

    /** Returns a copy that throws {@link TypedRoute.RouteParamException} when absent. */
    public QueryParam<T> required() {
        return new QueryParam<>(name, type, null, true);
    }

    /** The parameter name. */
    public String name() {
        return name;
    }

    /** The parameter's Java type. */
    public Class<T> type() {
        return type;
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
