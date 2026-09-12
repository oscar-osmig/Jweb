package com.osmig.Jweb.framework.security;

/**
 * The pre-3.0 spelling of the rate limiter.
 *
 * @deprecated Replaced by {@link jweb.RateLimit} — shorter import, same
 *             statics and nested types (inherited here). Existing code keeps
 *             working.
 */
@Deprecated
public class RateLimit extends jweb.RateLimit {

    protected RateLimit() {}
}
