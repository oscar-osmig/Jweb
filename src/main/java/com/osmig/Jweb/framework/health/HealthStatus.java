package com.osmig.Jweb.framework.health;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check status result.
 */
public class HealthStatus {

    private final Status status;
    private final String message;
    private final Map<String, Object> details;

    private HealthStatus(Status status, String message, Map<String, Object> details) {
        this.status = status;
        this.message = message;
        this.details = details;
    }

    public static HealthStatus up() {
        return new HealthStatus(Status.UP, null, Map.of());
    }

    public static HealthStatus up(String message) {
        return new HealthStatus(Status.UP, message, Map.of());
    }

    public static HealthStatus down(String message) {
        return new HealthStatus(Status.DOWN, message, Map.of());
    }

    public static HealthStatus down(String message, Throwable error) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("error", error.getClass().getSimpleName());
        details.put("errorMessage", error.getMessage());
        return new HealthStatus(Status.DOWN, message, details);
    }

    public static HealthStatus degraded(String message) {
        return new HealthStatus(Status.DEGRADED, message, Map.of());
    }

    public HealthStatus withDetail(String key, Object value) {
        Map<String, Object> newDetails = new LinkedHashMap<>(details);
        newDetails.put(key, value);
        return new HealthStatus(status, message, newDetails);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public boolean isUp() {
        return status == Status.UP;
    }

    public boolean isDown() {
        return status == Status.DOWN;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", status.name());
        if (message != null) {
            map.put("message", message);
        }
        if (!details.isEmpty()) {
            map.putAll(details);
        }
        return map;
    }

    public enum Status {
        UP, DOWN, DEGRADED, UNKNOWN
    }
}
