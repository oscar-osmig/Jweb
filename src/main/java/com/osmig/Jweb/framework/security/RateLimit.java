package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.server.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Fluent rate limiting configuration.
 *
 * <p>Usage:</p>
 * <pre>
 * // Simple: 100 requests per minute
 * app.use(RateLimit.perMinute(100));
 *
 * // Per hour
 * app.use(RateLimit.perHour(1000));
 *
 * // Custom window
 * app.use(RateLimit.requests(50).per(Duration.ofSeconds(30)));
 *
 * // Full configuration
 * app.use(RateLimit.configure()
 *     .requests(100)
 *     .per(Duration.ofMinutes(1))
 *     .keyBy(req -> req.header("X-API-Key"))  // Rate limit by API key
 *     .skipIf(req -> req.path().startsWith("/health"))
 *     .onLimit((req, info) -> customResponse())
 *     .build());
 *
 * // Different limits for different endpoints
 * app.use(RateLimit.perMinute(10).forPath("/api/expensive/**"));
 * app.use(RateLimit.perMinute(100).forPath("/api/**"));
 * </pre>
 */
public final class RateLimit {

    private RateLimit() {}

    // ==================== Quick Methods ====================

    /**
     * Creates rate limit of N requests per second.
     */
    public static RateLimitBuilder perSecond(int requests) {
        return new RateLimitBuilder().requests(requests).per(Duration.ofSeconds(1));
    }

    /**
     * Creates rate limit of N requests per minute.
     */
    public static RateLimitBuilder perMinute(int requests) {
        return new RateLimitBuilder().requests(requests).per(Duration.ofMinutes(1));
    }

    /**
     * Creates rate limit of N requests per hour.
     */
    public static RateLimitBuilder perHour(int requests) {
        return new RateLimitBuilder().requests(requests).per(Duration.ofHours(1));
    }

    /**
     * Creates rate limit of N requests per day.
     */
    public static RateLimitBuilder perDay(int requests) {
        return new RateLimitBuilder().requests(requests).per(Duration.ofDays(1));
    }

    /**
     * Starts building with request count.
     */
    public static RateLimitBuilder requests(int count) {
        return new RateLimitBuilder().requests(count);
    }

    /**
     * Full configuration builder.
     */
    public static RateLimitBuilder configure() {
        return new RateLimitBuilder();
    }

    // ==================== Builder ====================

    public static class RateLimitBuilder {
        private int maxRequests = 100;
        private Duration window = Duration.ofMinutes(1);
        private Function<Request, String> keyExtractor = RateLimitBuilder::defaultKeyExtractor;
        private Function<Request, Boolean> skipCondition = null;
        private String pathPattern = null;
        private RateLimitHandler onLimitExceeded = null;
        private boolean includeHeaders = true;

        RateLimitBuilder() {}

        /**
         * Sets maximum requests allowed in the window.
         */
        public RateLimitBuilder requests(int count) {
            this.maxRequests = count;
            return this;
        }

        /**
         * Sets the time window.
         */
        public RateLimitBuilder per(Duration duration) {
            this.window = duration;
            return this;
        }

        /**
         * Custom key extractor (default: IP address).
         * Use this to rate limit by API key, user ID, etc.
         */
        public RateLimitBuilder keyBy(Function<Request, String> extractor) {
            this.keyExtractor = extractor;
            return this;
        }

        /**
         * Rate limit by IP address (default).
         */
        public RateLimitBuilder byIp() {
            this.keyExtractor = RateLimitBuilder::defaultKeyExtractor;
            return this;
        }

        /**
         * Rate limit by user ID (requires authentication).
         */
        public RateLimitBuilder byUser() {
            this.keyExtractor = req -> {
                var principal = Auth.getPrincipal(req);
                return principal != null ? principal.getId() : defaultKeyExtractor(req);
            };
            return this;
        }

        /**
         * Rate limit by API key header.
         */
        public RateLimitBuilder byApiKey() {
            return byApiKey("X-API-Key");
        }

        /**
         * Rate limit by custom header.
         */
        public RateLimitBuilder byApiKey(String headerName) {
            this.keyExtractor = req -> {
                String key = req.header(headerName);
                return key != null ? key : defaultKeyExtractor(req);
            };
            return this;
        }

        /**
         * Skip rate limiting for certain requests.
         */
        public RateLimitBuilder skipIf(Function<Request, Boolean> condition) {
            this.skipCondition = condition;
            return this;
        }

        /**
         * Only apply to requests matching this path pattern.
         */
        public RateLimitBuilder forPath(String pattern) {
            this.pathPattern = pattern;
            return this;
        }

        /**
         * Custom handler when limit is exceeded.
         */
        public RateLimitBuilder onLimit(RateLimitHandler handler) {
            this.onLimitExceeded = handler;
            return this;
        }

        /**
         * Whether to include rate limit headers (X-RateLimit-*).
         */
        public RateLimitBuilder headers(boolean include) {
            this.includeHeaders = include;
            return this;
        }

        /**
         * Builds the rate limit middleware.
         */
        public Middleware build() {
            Map<String, RateLimitEntry> limits = new ConcurrentHashMap<>();
            long windowMs = window.toMillis();
            int max = maxRequests;
            Function<Request, String> keyFn = keyExtractor;
            Function<Request, Boolean> skipFn = skipCondition;
            String pattern = pathPattern;
            RateLimitHandler limitHandler = onLimitExceeded;
            boolean headers = includeHeaders;

            // Cleanup task - remove expired entries periodically
            scheduleCleanup(limits, windowMs);

            return (req, chain) -> {
                // Check path pattern
                if (pattern != null && !matchesPath(req.path(), pattern)) {
                    return chain.next();
                }

                // Check skip condition
                if (skipFn != null && skipFn.apply(req)) {
                    return chain.next();
                }

                String clientKey = keyFn.apply(req);
                long now = System.currentTimeMillis();

                RateLimitEntry entry = limits.compute(clientKey, (k, existing) -> {
                    if (existing == null || now - existing.windowStart > windowMs) {
                        return new RateLimitEntry(now, new AtomicLong(1));
                    }
                    existing.count.incrementAndGet();
                    return existing;
                });

                long remaining = Math.max(0, max - entry.count.get());
                long resetTime = entry.windowStart + windowMs;
                long retryAfter = Math.max(0, (resetTime - now) / 1000);

                RateLimitInfo info = new RateLimitInfo(max, remaining, resetTime, retryAfter);

                // Check if limit exceeded
                if (entry.count.get() > max) {
                    if (limitHandler != null) {
                        return limitHandler.handle(req, info);
                    }

                    var response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(retryAfter));

                    if (headers) {
                        response.header("X-RateLimit-Limit", String.valueOf(max))
                            .header("X-RateLimit-Remaining", "0")
                            .header("X-RateLimit-Reset", String.valueOf(resetTime / 1000));
                    }

                    return response.body(Map.of(
                        "error", "Rate limit exceeded",
                        "retryAfter", retryAfter
                    ));
                }

                // Process request
                Object result = chain.next();

                // Add rate limit headers
                if (headers && result instanceof ResponseEntity<?> response) {
                    return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .header("X-RateLimit-Limit", String.valueOf(max))
                        .header("X-RateLimit-Remaining", String.valueOf(remaining))
                        .header("X-RateLimit-Reset", String.valueOf(resetTime / 1000))
                        .body(response.getBody());
                }

                return result;
            };
        }

        private static String defaultKeyExtractor(Request req) {
            String forwarded = req.header("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                return forwarded.split(",")[0].trim();
            }
            String realIp = req.header("X-Real-IP");
            if (realIp != null && !realIp.isEmpty()) {
                return realIp;
            }
            return req.raw().getRemoteAddr();
        }

        private static boolean matchesPath(String path, String pattern) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            }
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
            }
            if (pattern.contains("*")) {
                String regex = pattern.replace("*", ".*");
                return path.matches(regex);
            }
            return path.equals(pattern);
        }

        // Shared scheduler for all rate limiters - non-blocking
        private static final ScheduledExecutorService CLEANUP_SCHEDULER =
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "RateLimit-Cleanup");
                    t.setDaemon(true);
                    return t;
                });

        private static void scheduleCleanup(Map<String, RateLimitEntry> limits, long windowMs) {
            // Non-blocking scheduled cleanup (replaces Thread.sleep)
            long intervalMs = Math.min(windowMs, 60000);
            CLEANUP_SCHEDULER.scheduleAtFixedRate(() -> {
                long now = System.currentTimeMillis();
                limits.entrySet().removeIf(e -> now - e.getValue().windowStart > windowMs * 2);
            }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        }
    }

    // ==================== Supporting Types ====================

    private record RateLimitEntry(long windowStart, AtomicLong count) {}

    /**
     * Rate limit information.
     */
    public record RateLimitInfo(
        int limit,
        long remaining,
        long resetTime,
        long retryAfter
    ) {}

    /**
     * Handler for rate limit exceeded.
     */
    @FunctionalInterface
    public interface RateLimitHandler {
        Object handle(Request request, RateLimitInfo info);
    }
}
