package com.osmig.Jweb.framework.middleware;

/**
 * The pre-3.0 spelling of the middleware continuation.
 *
 * @deprecated Replaced by {@link jweb.MiddlewareChain} — the type
 *             {@code Middleware.handle} receives. A class that implements
 *             {@code Middleware} declares that parameter as
 *             {@code jweb.MiddlewareChain}.
 */
@Deprecated
@FunctionalInterface
public interface MiddlewareChain extends jweb.MiddlewareChain {
}
