package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.core.Page;
import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.middleware.MiddlewareStack;
import com.osmig.Jweb.framework.routing.PageRegistry;
import com.osmig.Jweb.framework.routing.Route;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.routing.Router;
import com.osmig.Jweb.framework.server.Request;
import com.osmig.Jweb.framework.template.Template;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JWeb - A minimalist Java web framework.
 *
 * <p>Build simple web applications entirely in Java with a clean, fluent API.</p>
 *
 * <h2>Page Routes</h2>
 * <pre>{@code
 * app.layout(MainLayout.class)
 *    .pages(
 *        "/", HomePage.class,
 *        "/about", AboutPage.class,
 *        "/contact", ContactPage.class
 *    );
 * }</pre>
 *
 * <h2>API Routes</h2>
 * <p>APIs are created using the {@code @Api} annotation on classes. They are
 * automatically discovered by Spring when placed in the component scan path.</p>
 *
 * <pre>{@code
 * @Api("/api/v1/users")
 * public class UserApi {
 *
 *     @Get
 *     public List<User> getAll() { ... }
 *
 *     @Get("/{id}")
 *     public User getById(@Param("id") Long id) { ... }
 *
 *     @Post
 *     public User create(@Body User user) { ... }
 * }
 * }</pre>
 *
 * @see com.osmig.Jweb.framework.api.Api
 */
public class JWeb {

    private final Router router;
    private final MiddlewareStack middlewareStack;
    private final PageRegistry pageRegistry;

    private JWeb() {
        this.router = new Router();
        this.middlewareStack = new MiddlewareStack();
        this.pageRegistry = new PageRegistry();
    }

    /**
     * Creates a new JWeb application.
     */
    public static JWeb create() {
        return new JWeb();
    }

    // ==================== Middleware ====================

    /**
     * Adds middleware to the application.
     * Middleware is executed in the order it was added.
     *
     * @param middleware the middleware to add
     * @return this for chaining
     */
    public JWeb use(Middleware middleware) {
        middlewareStack.use(middleware);
        return this;
    }

    /**
     * Adds middleware conditionally.
     *
     * @param condition  whether to add the middleware
     * @param middleware the middleware to add
     * @return this for chaining
     */
    public JWeb useIf(boolean condition, Middleware middleware) {
        middlewareStack.useIf(condition, middleware);
        return this;
    }

    /**
     * Adds middleware for specific path prefixes.
     *
     * @param pathPrefix the path prefix to match
     * @param middleware the middleware to add
     * @return this for chaining
     */
    public JWeb use(String pathPrefix, Middleware middleware) {
        middlewareStack.useForPath(pathPrefix, middleware);
        return this;
    }

    /**
     * Adds middleware for specific HTTP methods.
     *
     * @param methods    the HTTP methods to match
     * @param middleware the middleware to add
     * @return this for chaining
     */
    public JWeb useForMethods(List<String> methods, Middleware middleware) {
        middlewareStack.useForMethods(methods, middleware);
        return this;
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

    // ==================== Pages ====================

    /**
     * Sets the default layout for all pages.
     *
     * <pre>{@code
     * app.layout(MainLayout.class)
     *    .pages("/", HomePage.class, "/about", AboutPage.class);
     * }</pre>
     *
     * @param layoutClass the layout class to wrap pages
     * @return this for chaining
     */
    public JWeb layout(Class<? extends Template> layoutClass) {
        pageRegistry.setDefaultLayout(layoutClass);
        return this;
    }

    /**
     * Registers pages using simple map-style syntax.
     *
     * <pre>{@code
     * app.pages(
     *     "/", HomePage.class,
     *     "/about", AboutPage.class,
     *     "/contact", ContactPage.class
     * );
     * }</pre>
     *
     * @param pathsAndPages alternating paths and page classes
     * @return this for chaining
     */
    public JWeb pages(Object... pathsAndPages) {
        pageRegistry.register(pathsAndPages);
        return this;
    }

    // ==================== Getters ====================

    public Router getRouter() {
        return router;
    }

    public MiddlewareStack getMiddlewareStack() {
        return middlewareStack;
    }

    public PageRegistry getPageRegistry() {
        return pageRegistry;
    }
}
