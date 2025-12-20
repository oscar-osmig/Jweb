package com.osmig.Jweb.framework.middleware;

import com.osmig.Jweb.framework.server.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a stack of middleware and executes them in order.
 *
 * <p>Middleware is executed in the order it was added. Each middleware
 * can either pass control to the next middleware or short-circuit
 * the chain by returning a response directly.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * MiddlewareStack stack = new MiddlewareStack()
 *     .use(loggingMiddleware)
 *     .use(authMiddleware)
 *     .use(csrfMiddleware);
 *
 * Object result = stack.execute(request, () -> handler.handle(request));
 * </pre>
 */
public class MiddlewareStack {

    private final List<Middleware> middlewares = new ArrayList<>();

    /**
     * Adds a middleware to the stack.
     *
     * @param middleware the middleware to add
     * @return this stack for chaining
     */
    public MiddlewareStack use(Middleware middleware) {
        if (middleware != null) {
            middlewares.add(middleware);
        }
        return this;
    }

    /**
     * Adds a middleware conditionally.
     *
     * @param condition  whether to add the middleware
     * @param middleware the middleware to add
     * @return this stack for chaining
     */
    public MiddlewareStack useIf(boolean condition, Middleware middleware) {
        if (condition && middleware != null) {
            middlewares.add(middleware);
        }
        return this;
    }

    /**
     * Adds a middleware that only runs for specific paths.
     *
     * @param pathPrefix the path prefix to match
     * @param middleware the middleware to add
     * @return this stack for chaining
     */
    public MiddlewareStack useForPath(String pathPrefix, Middleware middleware) {
        if (middleware != null) {
            middlewares.add(Middleware.when(
                    req -> req.path().startsWith(pathPrefix),
                    middleware
            ));
        }
        return this;
    }

    /**
     * Adds a middleware that only runs for specific HTTP methods.
     *
     * @param methods    the HTTP methods to match
     * @param middleware the middleware to add
     * @return this stack for chaining
     */
    public MiddlewareStack useForMethods(List<String> methods, Middleware middleware) {
        if (middleware != null) {
            middlewares.add(Middleware.when(
                    req -> methods.stream().anyMatch(m -> m.equalsIgnoreCase(req.method())),
                    middleware
            ));
        }
        return this;
    }

    /**
     * Executes the middleware stack, ending with the final handler.
     *
     * @param request      the HTTP request
     * @param finalHandler the final route handler
     * @return the response
     * @throws Exception if any middleware or handler throws
     */
    public Object execute(Request request, MiddlewareChain finalHandler) throws Exception {
        if (middlewares.isEmpty()) {
            return finalHandler.next();
        }
        return executeAt(0, request, finalHandler);
    }

    private Object executeAt(int index, Request request, MiddlewareChain finalHandler) throws Exception {
        if (index >= middlewares.size()) {
            return finalHandler.next();
        }

        Middleware current = middlewares.get(index);
        return current.handle(request, () -> executeAt(index + 1, request, finalHandler));
    }

    /**
     * Returns the number of middlewares in the stack.
     *
     * @return the middleware count
     */
    public int size() {
        return middlewares.size();
    }

    /**
     * Creates an empty middleware stack.
     *
     * @return a new stack
     */
    public static MiddlewareStack create() {
        return new MiddlewareStack();
    }
}
