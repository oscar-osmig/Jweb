package com.osmig.Jweb.framework.health;

/**
 * The pre-3.0 spelling of a health check.
 *
 * @deprecated Replaced by {@link jweb.HealthCheck} — the functional interface
 *             {@code Health.register(name, check)} takes. Existing lambdas
 *             keep working; a declared variable should use the new name.
 */
@Deprecated
@FunctionalInterface
public interface HealthCheck extends jweb.HealthCheck {
}
