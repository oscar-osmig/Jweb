package com.osmig.Jweb.framework.middleware;

import com.osmig.Jweb.framework.server.Request;

/**
 * Represents the continuation of the middleware chain.
 *
 * <p>Middleware implementations call {@code chain.next()} to pass
 * control to the next middleware or the final route handler.</p>
 *
 * <p>Usage in middleware:</p>
 * <pre>
 * (req, chain) -> {
 *     // Before request processing
 *     logger.info("Received: " + req.path());
 *
 *     // Continue to next middleware or handler
 *     Object result = chain.next();
 *
 *     // After request processing
 *     logger.info("Completed");
 *
 *     return result;
 * }
 * </pre>
 */
@FunctionalInterface
public interface MiddlewareChain {

    /**
     * Continues to the next middleware or route handler.
     *
     * @return the result from the route handler
     * @throws Exception if the handler throws
     */
    Object next() throws Exception;
}
