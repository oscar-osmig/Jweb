package com.osmig.Jweb.framework.middleware;

import com.osmig.Jweb.framework.server.Request;

/**
 * Middleware interface for intercepting and processing HTTP requests.
 *
 * <p>Middleware sits between the HTTP request and the route handler,
 * allowing you to:</p>
 * <ul>
 *   <li>Execute code before the handler runs</li>
 *   <li>Execute code after the handler runs</li>
 *   <li>Modify the request or response</li>
 *   <li>Short-circuit the chain (skip the handler)</li>
 * </ul>
 *
 * <h2>Example: Logging Middleware</h2>
 * <pre>
 * Middleware logging = (req, chain) -> {
 *     long start = System.currentTimeMillis();
 *     try {
 *         return chain.next();
 *     } finally {
 *         long duration = System.currentTimeMillis() - start;
 *         System.out.println(req.method() + " " + req.path() + " - " + duration + "ms");
 *     }
 * };
 * </pre>
 *
 * <h2>Example: Authentication Middleware</h2>
 * <pre>
 * Middleware auth = (req, chain) -> {
 *     String token = req.header("Authorization");
 *     if (!isValidToken(token)) {
 *         return ResponseEntity.status(401).body("Unauthorized");
 *     }
 *     return chain.next();
 * };
 * </pre>
 */
@FunctionalInterface
public interface Middleware {

    /**
     * Handles the request, optionally delegating to the next middleware.
     *
     * @param request the HTTP request
     * @param chain   the middleware chain
     * @return the response object
     * @throws Exception if processing fails
     */
    Object handle(Request request, MiddlewareChain chain) throws Exception;

    /**
     * Creates a middleware that runs before the handler.
     *
     * @param action the action to run
     * @return a new middleware
     */
    static Middleware before(java.util.function.Consumer<Request> action) {
        return (req, chain) -> {
            action.accept(req);
            return chain.next();
        };
    }

    /**
     * Creates a middleware that runs after the handler.
     *
     * @param action the action to run (receives request and result)
     * @return a new middleware
     */
    static Middleware after(java.util.function.BiConsumer<Request, Object> action) {
        return (req, chain) -> {
            Object result = chain.next();
            action.accept(req, result);
            return result;
        };
    }

    /**
     * Creates a middleware that wraps the handler with try-catch.
     *
     * @param errorHandler handles any exception thrown
     * @return a new middleware
     */
    static Middleware catchErrors(java.util.function.BiFunction<Request, Exception, Object> errorHandler) {
        return (req, chain) -> {
            try {
                return chain.next();
            } catch (Exception e) {
                return errorHandler.apply(req, e);
            }
        };
    }

    /**
     * Creates a conditional middleware that only runs if the predicate matches.
     *
     * @param predicate the condition
     * @param middleware the middleware to run if condition matches
     * @return a new middleware
     */
    static Middleware when(java.util.function.Predicate<Request> predicate, Middleware middleware) {
        return (req, chain) -> {
            if (predicate.test(req)) {
                return middleware.handle(req, chain);
            }
            return chain.next();
        };
    }

    /**
     * Chains this middleware with another.
     *
     * @param next the next middleware
     * @return a combined middleware
     */
    default Middleware then(Middleware next) {
        return (req, chain) -> this.handle(req, () -> next.handle(req, chain));
    }
}
