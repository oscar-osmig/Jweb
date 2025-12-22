package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.middleware.Middleware;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.*;

/**
 * Fluent CORS configuration builder.
 *
 * <p>Usage:</p>
 * <pre>
 * // Allow all (development)
 * app.use(Cors.allowAll());
 *
 * // Single origin
 * app.use(Cors.origin("https://myapp.com"));
 *
 * // Multiple origins
 * app.use(Cors.origins("https://myapp.com", "https://admin.myapp.com"));
 *
 * // Full configuration
 * app.use(Cors.configure()
 *     .origins("https://myapp.com")
 *     .methods("GET", "POST", "PUT", "DELETE")
 *     .headers("Content-Type", "Authorization")
 *     .credentials()
 *     .maxAge(Duration.ofHours(1))
 *     .build());
 * </pre>
 */
public final class Cors {

    private Cors() {}

    // ==================== Quick Methods ====================

    /**
     * Allows all origins (use for development only).
     */
    public static Middleware allowAll() {
        return configure()
            .origins("*")
            .methods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .headers("*")
            .build();
    }

    /**
     * Allows a single origin.
     */
    public static Middleware origin(String origin) {
        return configure().origins(origin).build();
    }

    /**
     * Allows multiple origins.
     */
    public static Middleware origins(String... origins) {
        return configure().origins(origins).build();
    }

    /**
     * Starts a full CORS configuration builder.
     */
    public static CorsBuilder configure() {
        return new CorsBuilder();
    }

    // ==================== Builder ====================

    public static class CorsBuilder {
        private final Set<String> allowedOrigins = new LinkedHashSet<>();
        private final Set<String> allowedMethods = new LinkedHashSet<>();
        private final Set<String> allowedHeaders = new LinkedHashSet<>();
        private final Set<String> exposedHeaders = new LinkedHashSet<>();
        private boolean allowCredentials = false;
        private long maxAgeSeconds = 86400; // 24 hours

        CorsBuilder() {
            // Sensible defaults
            methods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
            headers("Content-Type", "Authorization", "X-Requested-With", "X-CSRF-TOKEN");
        }

        /**
         * Sets allowed origins.
         */
        public CorsBuilder origins(String... origins) {
            allowedOrigins.clear();
            allowedOrigins.addAll(Arrays.asList(origins));
            return this;
        }

        /**
         * Adds additional allowed origins.
         */
        public CorsBuilder addOrigin(String origin) {
            allowedOrigins.add(origin);
            return this;
        }

        /**
         * Sets allowed HTTP methods.
         */
        public CorsBuilder methods(String... methods) {
            allowedMethods.clear();
            allowedMethods.addAll(Arrays.asList(methods));
            return this;
        }

        /**
         * Adds additional allowed methods.
         */
        public CorsBuilder addMethod(String method) {
            allowedMethods.add(method.toUpperCase());
            return this;
        }

        /**
         * Sets allowed request headers.
         */
        public CorsBuilder headers(String... headers) {
            allowedHeaders.clear();
            allowedHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        /**
         * Adds additional allowed headers.
         */
        public CorsBuilder addHeader(String header) {
            allowedHeaders.add(header);
            return this;
        }

        /**
         * Sets headers exposed to the client.
         */
        public CorsBuilder exposeHeaders(String... headers) {
            exposedHeaders.addAll(Arrays.asList(headers));
            return this;
        }

        /**
         * Allows credentials (cookies, authorization headers).
         * Note: Cannot be used with origin("*").
         */
        public CorsBuilder credentials() {
            this.allowCredentials = true;
            return this;
        }

        /**
         * Allows credentials (cookies, authorization headers).
         */
        public CorsBuilder credentials(boolean allow) {
            this.allowCredentials = allow;
            return this;
        }

        /**
         * Sets how long preflight results can be cached.
         */
        public CorsBuilder maxAge(Duration duration) {
            this.maxAgeSeconds = duration.toSeconds();
            return this;
        }

        /**
         * Sets max age in seconds.
         */
        public CorsBuilder maxAge(long seconds) {
            this.maxAgeSeconds = seconds;
            return this;
        }

        /**
         * Builds the CORS middleware.
         */
        public Middleware build() {
            // Validation
            if (allowCredentials && allowedOrigins.contains("*")) {
                throw new IllegalStateException(
                    "Cannot use credentials() with wildcard origin '*'. Specify exact origins instead."
                );
            }

            // Build header values
            String originsValue = String.join(", ", allowedOrigins);
            String methodsValue = String.join(", ", allowedMethods);
            String headersValue = allowedHeaders.contains("*") ? "*" : String.join(", ", allowedHeaders);
            String exposedValue = exposedHeaders.isEmpty() ? null : String.join(", ", exposedHeaders);
            boolean credentials = allowCredentials;
            long maxAge = maxAgeSeconds;
            Set<String> originSet = new HashSet<>(allowedOrigins);

            return (req, chain) -> {
                String requestOrigin = req.header("Origin");

                // Determine the origin to use in response
                String responseOrigin;
                if (originSet.contains("*")) {
                    responseOrigin = "*";
                } else if (requestOrigin != null && originSet.contains(requestOrigin)) {
                    responseOrigin = requestOrigin;
                } else if (requestOrigin != null && originSet.isEmpty()) {
                    responseOrigin = requestOrigin;
                } else {
                    responseOrigin = originsValue;
                }

                // Handle preflight OPTIONS requests
                if ("OPTIONS".equalsIgnoreCase(req.method())) {
                    var builder = ResponseEntity.ok()
                        .header("Access-Control-Allow-Origin", responseOrigin)
                        .header("Access-Control-Allow-Methods", methodsValue)
                        .header("Access-Control-Allow-Headers", headersValue)
                        .header("Access-Control-Max-Age", String.valueOf(maxAge));

                    if (credentials) {
                        builder.header("Access-Control-Allow-Credentials", "true");
                    }
                    if (exposedValue != null) {
                        builder.header("Access-Control-Expose-Headers", exposedValue);
                    }
                    // Vary header for caching
                    builder.header("Vary", "Origin");

                    return builder.build();
                }

                // Process the actual request
                Object result = chain.next();

                // Add CORS headers to response
                if (result instanceof ResponseEntity<?> response) {
                    var builder = ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Access-Control-Allow-Origin", responseOrigin);

                    if (credentials) {
                        builder.header("Access-Control-Allow-Credentials", "true");
                    }
                    if (exposedValue != null) {
                        builder.header("Access-Control-Expose-Headers", exposedValue);
                    }
                    builder.header("Vary", "Origin");

                    return builder.body(response.getBody());
                }

                return result;
            };
        }
    }
}
