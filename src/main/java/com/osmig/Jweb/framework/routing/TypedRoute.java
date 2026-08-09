package com.osmig.Jweb.framework.routing;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Type-safe routes: declare the path and its parameter types once, then both
 * the handler registration and every link to it are compile-time checked —
 * no more string-built URLs drifting out of sync with handlers.
 *
 * <pre>
 * // Declare (usually as constants):
 * static final TypedRoute.Path1&lt;Long&gt; USER = TypedRoute.path("/users/:id", Long.class);
 * static final TypedRoute.Path2&lt;String, Integer&gt; POST =
 *     TypedRoute.path("/blog/:slug/comments/:page", String.class, Integer.class);
 *
 * // Register — the handler receives parsed, typed params:
 * app.get(USER, (req, id) -&gt; userPage(id));            // id is a Long
 * app.get(POST, (req, slug, page) -&gt; comments(slug, page));
 *
 * // Link — refactoring the pattern updates every URL:
 * a(USER.url(42L), text("Profile"))                     // "/users/42"
 * </pre>
 *
 * Supported parameter types: String, Integer, Long, Double, Boolean, UUID.
 */
public final class TypedRoute {

    private TypedRoute() {}

    /** A route with no parameters. */
    public static Path0 path(String pattern) {
        return new Path0(pattern);
    }

    /** A route with one typed parameter. */
    public static <A> Path1<A> path(String pattern, Class<A> paramType) {
        return new Path1<>(pattern, paramType);
    }

    /** A route with two typed parameters (in path order). */
    public static <A, B> Path2<A, B> path(String pattern, Class<A> first, Class<B> second) {
        return new Path2<>(pattern, first, second);
    }

    // ==================== Path variants ====================

    public static final class Path0 extends Base {
        Path0(String pattern) {
            super(pattern, 0);
        }

        /** The URL for this route. */
        public String url() {
            return pattern();
        }
    }

    public static final class Path1<A> extends Base {
        private final Class<A> typeA;

        Path1(String pattern, Class<A> typeA) {
            super(pattern, 1);
            this.typeA = typeA;
        }

        /** Builds the URL with the parameter substituted (URL-encoded). */
        public String url(A value) {
            return substitute(List.of(value));
        }

        /** Parses the first path parameter from a matched request. */
        public A parse(String raw) {
            return convert(raw, typeA, paramNames().get(0));
        }
    }

    public static final class Path2<A, B> extends Base {
        private final Class<A> typeA;
        private final Class<B> typeB;

        Path2(String pattern, Class<A> typeA, Class<B> typeB) {
            super(pattern, 2);
            this.typeA = typeA;
            this.typeB = typeB;
        }

        public String url(A first, B second) {
            return substitute(List.of(first, second));
        }

        public A parseFirst(String raw) {
            return convert(raw, typeA, paramNames().get(0));
        }

        public B parseSecond(String raw) {
            return convert(raw, typeB, paramNames().get(1));
        }
    }

    // ==================== Shared machinery ====================

    public abstract static sealed class Base permits Path0, Path1, Path2 {
        private final String pattern;
        private final List<String> paramNames;

        Base(String pattern, int expectedParams) {
            this.pattern = pattern;
            this.paramNames = extractParamNames(pattern);
            if (paramNames.size() != expectedParams) {
                throw new IllegalArgumentException(
                    "Pattern '" + pattern + "' has " + paramNames.size()
                    + " parameter(s) but " + expectedParams + " type(s) were declared");
            }
        }

        /** The router pattern, e.g. {@code /users/:id}. */
        public String pattern() {
            return pattern;
        }

        /** The parameter names in path order. */
        public List<String> paramNames() {
            return paramNames;
        }

        String substitute(List<Object> values) {
            String result = pattern;
            for (int i = 0; i < paramNames.size(); i++) {
                String encoded = URLEncoder.encode(String.valueOf(values.get(i)), StandardCharsets.UTF_8);
                result = result.replace(":" + paramNames.get(i), encoded);
            }
            return result;
        }

        private static List<String> extractParamNames(String pattern) {
            List<String> names = new ArrayList<>();
            for (String segment : pattern.split("/")) {
                if (segment.startsWith(":")) {
                    names.add(segment.substring(1));
                }
            }
            return names;
        }

        @SuppressWarnings("unchecked")
        static <T> T convert(String raw, Class<T> type, String paramName) {
            try {
                if (type == String.class) return (T) raw;
                if (type == Integer.class) return (T) Integer.valueOf(raw);
                if (type == Long.class) return (T) Long.valueOf(raw);
                if (type == Double.class) return (T) Double.valueOf(raw);
                if (type == Boolean.class) return (T) Boolean.valueOf(raw);
                if (type == UUID.class) return (T) UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                throw new RouteParamException(paramName, raw, type);
            }
            throw new IllegalArgumentException("Unsupported route param type: " + type.getName());
        }
    }

    /** Thrown when a path value can't be converted to the declared type (→ 400). */
    public static class RouteParamException extends RuntimeException {
        public RouteParamException(String param, String raw, Class<?> type) {
            super("Path parameter '" + param + "' = '" + raw + "' is not a valid "
                + type.getSimpleName());
        }
    }
}
