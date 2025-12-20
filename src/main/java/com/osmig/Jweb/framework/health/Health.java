package com.osmig.Jweb.framework.health;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.server.Response;
import com.osmig.Jweb.framework.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health check management for application monitoring.
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Register health checks
 * Health.register("database", () -> {
 *     return dataSource.getConnection().isValid(1)
 *         ? HealthStatus.up()
 *         : HealthStatus.down("Connection failed");
 * });
 *
 * // Setup endpoints
 * Health.setupEndpoints(app);
 * // Creates: /health, /health/live, /health/ready
 * }</pre>
 *
 * <h2>Kubernetes Probes</h2>
 * <ul>
 *   <li><b>/health/live</b> - Liveness probe (is app running?)</li>
 *   <li><b>/health/ready</b> - Readiness probe (is app ready to serve traffic?)</li>
 *   <li><b>/health</b> - Full health status with all checks</li>
 * </ul>
 *
 * <h2>Custom Endpoints</h2>
 * <pre>{@code
 * app.get("/health", req -> Health.check());
 * app.get("/health/db", req -> Health.checkComponent("database"));
 * }</pre>
 */
public final class Health {

    private static final Logger log = LoggerFactory.getLogger(Health.class);

    private static final Map<String, HealthCheck> checks = new ConcurrentHashMap<>();
    private static final Map<String, HealthCheck> livenessChecks = new ConcurrentHashMap<>();
    private static final Map<String, HealthCheck> readinessChecks = new ConcurrentHashMap<>();

    private Health() {}

    /**
     * Registers a health check.
     *
     * @param name the check name
     * @param check the health check
     */
    public static void register(String name, HealthCheck check) {
        checks.put(name, check);
        readinessChecks.put(name, check); // By default, all checks are readiness checks
    }

    /**
     * Registers a liveness check (is the app running?).
     *
     * @param name the check name
     * @param check the health check
     */
    public static void registerLiveness(String name, HealthCheck check) {
        livenessChecks.put(name, check);
    }

    /**
     * Registers a readiness check (is the app ready to serve?).
     *
     * @param name the check name
     * @param check the health check
     */
    public static void registerReadiness(String name, HealthCheck check) {
        readinessChecks.put(name, check);
    }

    /**
     * Removes a health check.
     *
     * @param name the check name
     */
    public static void unregister(String name) {
        checks.remove(name);
        livenessChecks.remove(name);
        readinessChecks.remove(name);
    }

    /**
     * Runs all health checks and returns the overall status.
     *
     * @return JSON response with health status
     */
    public static Object check() {
        return checkInternal(checks);
    }

    /**
     * Runs liveness checks (for Kubernetes liveness probe).
     *
     * @return JSON response
     */
    public static Object checkLiveness() {
        if (livenessChecks.isEmpty()) {
            // Default liveness: app is running
            return Response.json(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
            ));
        }
        return checkInternal(livenessChecks);
    }

    /**
     * Runs readiness checks (for Kubernetes readiness probe).
     *
     * @return JSON response
     */
    public static Object checkReadiness() {
        return checkInternal(readinessChecks);
    }

    /**
     * Checks a specific component.
     *
     * @param name the component name
     * @return JSON response
     */
    public static Object checkComponent(String name) {
        HealthCheck check = checks.get(name);
        if (check == null) {
            return Response.notFound();
        }

        try {
            HealthStatus status = check.check();
            HttpStatus httpStatus = status.isUp() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return Response.json(httpStatus, status.toMap());
        } catch (Exception e) {
            log.error("Health check '{}' threw exception", name, e);
            return Response.json(HttpStatus.SERVICE_UNAVAILABLE, HealthStatus.down("Check failed", e).toMap());
        }
    }

    private static Object checkInternal(Map<String, HealthCheck> checksToRun) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> components = new LinkedHashMap<>();
        boolean allUp = true;
        boolean anyDegraded = false;

        for (Map.Entry<String, HealthCheck> entry : checksToRun.entrySet()) {
            String name = entry.getKey();
            HealthCheck check = entry.getValue();

            try {
                HealthStatus status = check.check();
                components.put(name, status.toMap());

                if (status.isDown()) {
                    allUp = false;
                } else if (status.getStatus() == HealthStatus.Status.DEGRADED) {
                    anyDegraded = true;
                }
            } catch (Exception e) {
                log.error("Health check '{}' threw exception", name, e);
                components.put(name, HealthStatus.down("Check threw exception", e).toMap());
                allUp = false;
            }
        }

        String overallStatus;
        if (!allUp) {
            overallStatus = "DOWN";
        } else if (anyDegraded) {
            overallStatus = "DEGRADED";
        } else {
            overallStatus = "UP";
        }

        result.put("status", overallStatus);
        result.put("timestamp", Instant.now().toString());
        if (!components.isEmpty()) {
            result.put("components", components);
        }

        HttpStatus httpStatus = allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return Response.json(httpStatus, result);
    }

    /**
     * Sets up standard health endpoints.
     *
     * @param app the JWeb application
     */
    public static void setupEndpoints(JWeb app) {
        app.get("/health", req -> check());
        app.get("/health/live", req -> checkLiveness());
        app.get("/health/ready", req -> checkReadiness());
    }

    /**
     * Sets up health endpoints with a custom prefix.
     *
     * @param app the JWeb application
     * @param prefix the URL prefix (e.g., "/api")
     */
    public static void setupEndpoints(JWeb app, String prefix) {
        String base = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        app.get(base + "/health", req -> check());
        app.get(base + "/health/live", req -> checkLiveness());
        app.get(base + "/health/ready", req -> checkReadiness());
    }

    /**
     * Clears all registered health checks.
     */
    public static void clear() {
        checks.clear();
        livenessChecks.clear();
        readinessChecks.clear();
    }
}
