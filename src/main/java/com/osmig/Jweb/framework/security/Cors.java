package com.osmig.Jweb.framework.security;

/**
 * The pre-3.0 spelling of the CORS middleware factory.
 *
 * @deprecated Replaced by {@link jweb.Cors} — shorter import, same statics and
 *             {@code CorsBuilder} (inherited here). Existing code keeps working.
 */
@Deprecated
public class Cors extends jweb.Cors {

    protected Cors() {}
}
