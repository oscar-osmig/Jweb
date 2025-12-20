package com.osmig.Jweb.framework.middleware;

import com.osmig.Jweb.framework.security.Csrf;
import com.osmig.Jweb.framework.security.CsrfException;
import com.osmig.Jweb.framework.util.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Built-in middleware implementations.
 *
 * <p>Usage:</p>
 * <pre>
 * app.use(Middlewares.logging())
 *    .use(Middlewares.cors("*"))
 *    .use(Middlewares.csrf())
 *    .use(Middlewares.rateLimit(100, 60000));
 * </pre>
 */
public final class Middlewares {

    private Middlewares() {
        // Static utility class
    }

    // ========== Logging ==========

    /**
     * Creates a logging middleware that logs request/response details.
     *
     * @return logging middleware
     */
    public static Middleware logging() {
        return (req, chain) -> {
            long start = System.currentTimeMillis();
            String method = req.method();
            String path = req.path();

            Log.info("--> {} {}", method, path);

            try {
                Object result = chain.next();
                long duration = System.currentTimeMillis() - start;
                Log.info("<-- {} {} ({}ms)", method, path, duration);
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                Log.error("<-- {} {} ERROR ({}ms): {}", method, path, duration, e.getMessage());
                throw e;
            }
        };
    }

    /**
     * Creates a logging middleware with a custom logger.
     *
     * @param logger the logger function
     * @return logging middleware
     */
    public static Middleware logging(java.util.function.Consumer<String> logger) {
        return (req, chain) -> {
            long start = System.currentTimeMillis();
            String method = req.method();
            String path = req.path();

            logger.accept("--> " + method + " " + path);

            try {
                Object result = chain.next();
                long duration = System.currentTimeMillis() - start;
                logger.accept("<-- " + method + " " + path + " (" + duration + "ms)");
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                logger.accept("<-- " + method + " " + path + " ERROR (" + duration + "ms)");
                throw e;
            }
        };
    }

    // ========== CORS ==========

    /**
     * Creates CORS middleware that allows all origins.
     *
     * @return CORS middleware
     */
    public static Middleware cors() {
        return cors("*");
    }

    /**
     * Creates CORS middleware for specific origins.
     *
     * @param allowedOrigins comma-separated list of allowed origins, or "*" for all
     * @return CORS middleware
     */
    public static Middleware cors(String allowedOrigins) {
        return cors(allowedOrigins, "GET, POST, PUT, DELETE, OPTIONS", "Content-Type, Authorization, X-CSRF-TOKEN");
    }

    /**
     * Creates fully configurable CORS middleware.
     *
     * @param allowedOrigins comma-separated origins or "*"
     * @param allowedMethods comma-separated HTTP methods
     * @param allowedHeaders comma-separated headers
     * @return CORS middleware
     */
    public static Middleware cors(String allowedOrigins, String allowedMethods, String allowedHeaders) {
        return (req, chain) -> {
            // Handle preflight OPTIONS requests
            if ("OPTIONS".equalsIgnoreCase(req.method())) {
                return ResponseEntity.ok()
                        .header("Access-Control-Allow-Origin", allowedOrigins)
                        .header("Access-Control-Allow-Methods", allowedMethods)
                        .header("Access-Control-Allow-Headers", allowedHeaders)
                        .header("Access-Control-Max-Age", "86400")
                        .build();
            }

            Object result = chain.next();

            // Add CORS headers to response
            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Access-Control-Allow-Origin", allowedOrigins)
                        .body(response.getBody());
            }

            return result;
        };
    }

    // ========== CSRF Protection ==========

    /**
     * Creates CSRF protection middleware.
     * Validates CSRF tokens on POST, PUT, DELETE requests.
     *
     * @return CSRF middleware
     */
    public static Middleware csrf() {
        return csrf(Set.of());
    }

    /**
     * Creates CSRF protection middleware with path exclusions.
     *
     * @param excludedPaths paths that don't require CSRF validation (e.g., API endpoints)
     * @return CSRF middleware
     */
    public static Middleware csrf(Set<String> excludedPaths) {
        return (req, chain) -> {
            // Skip CSRF check for safe methods
            if (!Csrf.requiresProtection(req)) {
                return chain.next();
            }

            // Skip excluded paths
            String path = req.path();
            if (excludedPaths.stream().anyMatch(path::startsWith)) {
                return chain.next();
            }

            // Validate CSRF token
            try {
                Csrf.validate(req);
            } catch (CsrfException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("CSRF validation failed: " + e.getMessage());
            }

            return chain.next();
        };
    }

    // ========== Rate Limiting ==========

    /**
     * Creates a rate limiting middleware.
     *
     * @param maxRequests maximum requests per window
     * @param windowMs    time window in milliseconds
     * @return rate limiting middleware
     */
    public static Middleware rateLimit(int maxRequests, long windowMs) {
        Map<String, RateLimitEntry> limits = new ConcurrentHashMap<>();

        return (req, chain) -> {
            String clientId = getClientId(req);
            long now = System.currentTimeMillis();

            RateLimitEntry entry = limits.compute(clientId, (k, existing) -> {
                if (existing == null || now - existing.windowStart > windowMs) {
                    return new RateLimitEntry(now, new AtomicInteger(1));
                }
                existing.count.incrementAndGet();
                return existing;
            });

            if (entry.count.get() > maxRequests) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf((windowMs - (now - entry.windowStart)) / 1000))
                        .body("Rate limit exceeded. Try again later.");
            }

            return chain.next();
        };
    }

    private static String getClientId(com.osmig.Jweb.framework.server.Request req) {
        // Try X-Forwarded-For header first (for proxies)
        String forwarded = req.header("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        // Fall back to remote address
        return req.raw().getRemoteAddr();
    }

    private record RateLimitEntry(long windowStart, AtomicInteger count) {}

    // ========== Security Headers ==========

    /**
     * Creates middleware that adds common security headers.
     *
     * @return security headers middleware
     */
    public static Middleware securityHeaders() {
        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("X-Content-Type-Options", "nosniff")
                        .header("X-Frame-Options", "DENY")
                        .header("X-XSS-Protection", "1; mode=block")
                        .header("Referrer-Policy", "strict-origin-when-cross-origin")
                        .header("Content-Security-Policy", "default-src 'self'")
                        .body(response.getBody());
            }

            return result;
        };
    }

    /**
     * Creates middleware with custom security headers.
     *
     * @param headers map of header name to value
     * @return security headers middleware
     */
    public static Middleware securityHeaders(Map<String, String> headers) {
        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                var builder = ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders());
                headers.forEach(builder::header);
                return builder.body(response.getBody());
            }

            return result;
        };
    }

    // ========== Request Timing ==========

    /**
     * Creates middleware that adds request timing to response headers.
     *
     * @return timing middleware
     */
    public static Middleware timing() {
        return (req, chain) -> {
            long start = System.currentTimeMillis();
            Object result = chain.next();
            long duration = System.currentTimeMillis() - start;

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("X-Response-Time", duration + "ms")
                        .body(response.getBody());
            }

            return result;
        };
    }

    // ========== Request ID ==========

    /**
     * Creates middleware that generates and tracks request IDs.
     *
     * @return request ID middleware
     */
    public static Middleware requestId() {
        return (req, chain) -> {
            String requestId = req.header("X-Request-ID");
            if (requestId == null) {
                requestId = java.util.UUID.randomUUID().toString();
            }

            // Store in request attribute for access in handlers
            req.raw().setAttribute("requestId", requestId);

            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("X-Request-ID", requestId)
                        .body(response.getBody());
            }

            return result;
        };
    }

    // ========== Caching Headers ==========

    /**
     * Creates middleware that adds cache control headers.
     *
     * @param maxAgeSeconds cache duration in seconds
     * @return caching middleware
     */
    public static Middleware cacheControl(long maxAgeSeconds) {
        return cacheControl(maxAgeSeconds, false, false);
    }

    /**
     * Creates middleware that adds cache control headers with options.
     *
     * @param maxAgeSeconds cache duration in seconds
     * @param isPrivate     true for private cache, false for public
     * @param mustRevalidate true to require revalidation
     * @return caching middleware
     */
    public static Middleware cacheControl(long maxAgeSeconds, boolean isPrivate, boolean mustRevalidate) {
        StringBuilder directive = new StringBuilder();
        directive.append(isPrivate ? "private" : "public");
        directive.append(", max-age=").append(maxAgeSeconds);
        if (mustRevalidate) {
            directive.append(", must-revalidate");
        }
        String cacheControl = directive.toString();

        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Cache-Control", cacheControl)
                        .body(response.getBody());
            }

            return result;
        };
    }

    /**
     * Creates middleware that disables caching (no-store, no-cache).
     *
     * @return no-cache middleware
     */
    public static Middleware noCache() {
        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(response.getBody());
            }

            return result;
        };
    }

    /**
     * Creates middleware for static asset caching (long cache, immutable).
     *
     * @return static caching middleware
     */
    public static Middleware staticCache() {
        return staticCache(31536000); // 1 year
    }

    /**
     * Creates middleware for static asset caching with custom duration.
     *
     * @param maxAgeSeconds cache duration in seconds
     * @return static caching middleware
     */
    public static Middleware staticCache(long maxAgeSeconds) {
        String cacheControl = "public, max-age=" + maxAgeSeconds + ", immutable";

        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Cache-Control", cacheControl)
                        .body(response.getBody());
            }

            return result;
        };
    }

    /**
     * Creates middleware that adds ETag support for responses.
     *
     * @return ETag middleware
     */
    public static Middleware etag() {
        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response && response.getBody() != null) {
                String body = response.getBody().toString();
                String etag = "\"" + Integer.toHexString(body.hashCode()) + "\"";

                // Check If-None-Match header
                String ifNoneMatch = req.header("If-None-Match");
                if (etag.equals(ifNoneMatch)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
                }

                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("ETag", etag)
                        .body(response.getBody());
            }

            return result;
        };
    }

    // ========== Compression ==========

    /**
     * Creates middleware that sets headers indicating compression is acceptable.
     *
     * <p>Note: Actual compression is typically handled by the servlet container
     * or a reverse proxy. This middleware sets the Vary header and can be used
     * with Spring Boot's compression settings.</p>
     *
     * <p>Enable compression in application.properties:</p>
     * <pre>
     * server.compression.enabled=true
     * server.compression.mime-types=text/html,text/css,application/javascript,application/json
     * server.compression.min-response-size=1024
     * </pre>
     *
     * @return compression headers middleware
     */
    public static Middleware compressionHeaders() {
        return (req, chain) -> {
            Object result = chain.next();

            if (result instanceof ResponseEntity<?> response) {
                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("Vary", "Accept-Encoding")
                        .body(response.getBody());
            }

            return result;
        };
    }
}
