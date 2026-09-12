package com.osmig.Jweb.framework.health;

import java.util.Map;

/**
 * The pre-3.0 spelling of a health-check result.
 *
 * @deprecated Replaced by {@link jweb.HealthStatus} — {@code up()},
 *             {@code down(...)} and {@code degraded(...)} (inherited here) hand
 *             out that type, so declare {@code jweb.HealthStatus}. This
 *             subclass only keeps old subclasses compiling.
 */
@Deprecated
public class HealthStatus extends jweb.HealthStatus {

    protected HealthStatus(Status status, String message, Map<String, Object> details) {
        super(status, message, details);
    }
}
