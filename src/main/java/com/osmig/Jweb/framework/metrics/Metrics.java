package com.osmig.Jweb.framework.metrics;

/**
 * The pre-3.0 spelling of the metrics registry.
 *
 * @deprecated Replaced by {@link jweb.Metrics} — shorter import, same statics
 *             and nested {@code Counter}/{@code Gauge}/{@code Timer} (inherited
 *             here). Existing code keeps working.
 */
@Deprecated
public class Metrics extends jweb.Metrics {

    protected Metrics() {}
}
