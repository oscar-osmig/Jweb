package com.osmig.Jweb.framework.health;

/**
 * Health check interface.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Health.register("database", () -> {
 *     try {
 *         dataSource.getConnection().isValid(1);
 *         return HealthStatus.up();
 *     } catch (Exception e) {
 *         return HealthStatus.down(e.getMessage());
 *     }
 * });
 *
 * Health.register("redis", () -> {
 *     return redis.ping().equals("PONG")
 *         ? HealthStatus.up()
 *         : HealthStatus.down("Redis not responding");
 * });
 * }</pre>
 */
@FunctionalInterface
public interface HealthCheck {

    /**
     * Performs the health check.
     *
     * @return the health status
     */
    HealthStatus check();
}
