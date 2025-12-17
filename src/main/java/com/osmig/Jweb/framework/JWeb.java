package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.core.Page;
import com.osmig.Jweb.framework.routing.Route;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.routing.Router;
import com.osmig.Jweb.framework.server.Request;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JWeb - A minimalist Java web framework.
 *
 * Build simple web applications entirely in Java with a clean, fluent API.
 *
 * Example:
 * <pre>
 * app.get("/", () -> h1("Hello World"))
 *    .get("/about", AboutPage::new)
 *    .get("/users/:id", req -> div(h1("User " + req.param("id"))));
 * </pre>
 */
public class JWeb {

    private final Router router;

    private JWeb() {
        this.router = new Router();
    }

    /**
     * Creates a new JWeb application.
     */
    public static JWeb create() {
        return new JWeb();
    }

    // ==================== Routes ====================

    /**
     * GET route with a simple element.
     */
    public JWeb get(String path, Supplier<Element> handler) {
        router.get(path, handler);
        return this;
    }

    /**
     * GET route with request access.
     */
    public JWeb get(String path, RouteHandler handler) {
        router.get(path, handler);
        return this;
    }

    /**
     * GET route with a page component.
     */
    public JWeb route(String path, Supplier<? extends Page> pageSupplier) {
        router.get(path, () -> pageSupplier.get());
        return this;
    }

    /**
     * GET route with a page that needs the request.
     */
    public JWeb route(String path, RouteHandler handler) {
        router.get(path, handler);
        return this;
    }

    /**
     * POST route.
     */
    public JWeb post(String path, RouteHandler handler) {
        router.post(path, handler);
        return this;
    }

    /**
     * POST route with typed handler.
     */
    public JWeb post(String path, Function<Request, Object> handler) {
        router.post(path, handler::apply);
        return this;
    }

    /**
     * PUT route.
     */
    public JWeb put(String path, RouteHandler handler) {
        router.put(path, handler);
        return this;
    }

    /**
     * DELETE route.
     */
    public JWeb delete(String path, RouteHandler handler) {
        router.delete(path, handler);
        return this;
    }

    /**
     * Adds a route directly.
     */
    public JWeb addRoute(Route route) {
        router.addRoute(route);
        return this;
    }

    // ==================== Getters ====================

    public Router getRouter() {
        return router;
    }
}
