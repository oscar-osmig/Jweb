package com.osmig.Jweb.framework;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.core.Page;
import jweb.ActionHandler;
import jweb.BindException;
import jweb.Guard;
import jweb.Middleware;
import com.osmig.Jweb.framework.middleware.MiddlewareStack;
import com.osmig.Jweb.framework.routing.Guards;
import com.osmig.Jweb.framework.routing.PageRegistry;
import com.osmig.Jweb.framework.routing.RecordBinder;
import com.osmig.Jweb.framework.routing.Route;
import jweb.RouteHandler;
import com.osmig.Jweb.framework.routing.Router;
import jweb.Request;
import com.osmig.Jweb.framework.template.Template;

import java.util.List;
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
 *        "/users/:id", UserPage.class          // params: beforeRender(req) or pathParam("id")
 *    );
 * }</pre>
 *
 * <h2>Guards</h2>
 * <pre>{@code
 * app.guard("/admin/**", Auth.requireLogin("/admin/login"));
 * app.guard("/api/v1/admin/**", req -> req.principal() != null ? null : Response.unauthorized());
 * }</pre>
 *
 * <h2>Action routes</h2>
 * <pre>{@code
 * record Pref(Theme theme, @Range(min = 1, max = 5) int level) {}
 * app.action("/act/pref", Pref.class, (req, pref) -> {
 *     Session.of(Visit.class, req).apply(pref);
 *     return Response.redirectBack(req);
 * });
 * }</pre>
 *
 * <h2>API Routes</h2>
 * <p>APIs are created using the {@code @REST} annotation on classes. They are
 * automatically discovered by Spring when placed in the component scan path.
 * REST controllers must live under {@code /api/v*} — those paths are reserved
 * for Spring MVC and excluded from the JWeb router.</p>
 *
 * <pre>{@code
 * @REST("/api/v1/users")
 * public class UserApi {
 *
 *     @GET
 *     public List<User> getAll() { ... }
 *
 *     @GET("/{id}")
 *     public User getById(@Param long id) { ... }
 *
 *     @POST
 *     public User create(@Body User user) { ... }
 * }
 * }</pre>
 *
 * @see jweb.api.REST
 */
public class JWeb {

    private final Router router;
    private final MiddlewareStack middlewareStack;
    private final PageRegistry pageRegistry;
    private final Guards guards;

    protected JWeb() {
        this.router = new Router();
        this.middlewareStack = new MiddlewareStack();
        this.pageRegistry = new PageRegistry();
        this.guards = new Guards();
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

    // ==================== Guards ====================

    /**
     * Guards every route under {@code pathPattern} — page routes, router
     * routes and {@code @REST} controllers alike. The guard returns null to
     * let the request through, or the response to send instead; guards run
     * after the middleware stack, in registration order, first answer wins.
     *
     * <pre>{@code
     * app.guard("/admin/**", req -> req.principal() != null ? null : Response.redirect("/admin/login"));
     * }</pre>
     *
     * @param pathPattern a prefix, {@code /prefix/**}, or a glob (as in {@link #use(String, Middleware)})
     * @param guard       the check
     * @return this for chaining
     */
    public JWeb guard(String pathPattern, Guard guard) {
        guards.add(pathPattern, guard);
        return this;
    }

    /**
     * Guards with a middleware — {@code Auth.requireRole("admin")},
     * {@code Auth.requireAuth("/login")}, {@code Jwt.protect()}: its own
     * response (or exception) answers, {@code chain.next()} passes.
     */
    public JWeb guard(String pathPattern, Middleware middleware) {
        guards.add(pathPattern, Guard.of(middleware));
        return this;
    }

    // ==================== Routes ====================

    /**
     * GET route with a simple element.
     */
    public JWeb get(String path, Supplier<? extends jweb.Element> handler) {
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

    // ==================== Typed Routes ====================

    /** GET with a typed no-param route. */
    public JWeb get(com.osmig.Jweb.framework.routing.TypedRoute.Path0 route, RouteHandler handler) {
        router.get(route.pattern(), handler);
        return this;
    }

    /**
     * GET with a one-param typed route — the handler receives the parsed value.
     *
     * <pre>
     * static final TypedRoute.Path1&lt;Long&gt; USER = TypedRoute.path("/users/:id", Long.class);
     * app.get(USER, (req, id) -&gt; userPage(id));   // id is a Long
     * a(USER.url(42L), text("Profile"))
     * </pre>
     */
    public <A> JWeb get(com.osmig.Jweb.framework.routing.TypedRoute.Path1<A> route,
                        java.util.function.BiFunction<Request, A, Object> handler) {
        router.get(route.pattern(), req ->
            handler.apply(req, route.parse(req.param(route.paramNames().get(0)))));
        return this;
    }

    /** GET with a two-param typed route. */
    public <A, B> JWeb get(com.osmig.Jweb.framework.routing.TypedRoute.Path2<A, B> route,
                           TriHandler<A, B> handler) {
        router.get(route.pattern(), req -> handler.handle(req,
            route.parseFirst(req.param(route.paramNames().get(0))),
            route.parseSecond(req.param(route.paramNames().get(1)))));
        return this;
    }

    /** POST with a one-param typed route. */
    public <A> JWeb post(com.osmig.Jweb.framework.routing.TypedRoute.Path1<A> route,
                         java.util.function.BiFunction<Request, A, Object> handler) {
        router.post(route.pattern(), req ->
            handler.apply(req, route.parse(req.param(route.paramNames().get(0)))));
        return this;
    }

    /** Handler for two-param typed routes. */
    @FunctionalInterface
    public interface TriHandler<A, B> {
        Object handle(Request request, A first, B second);
    }

    // ==================== Action routes (record binding) ====================

    /**
     * A mutation reachable from a link <em>or</em> a form: registered for
     * GET and POST, its query/form parameters bound to {@code type} (a
     * record) before the handler runs. Enums bind by name, numbers parse,
     * {@code Optional<T>} marks an optional parameter, and {@code @Range},
     * {@code @Length}, {@code @Pattern} validate; anything missing or
     * invalid is answered with a 400 and a message, never an exception page.
     *
     * <pre>{@code
     * enum Sound { ON, OFF }
     * record Pref(Optional<Sound> sound, Optional<Boolean> motion) {}
     *
     * app.action("/act/pref", Pref.class, (req, pref) -> {
     *     Visit v = Session.of(Visit.class, req);
     *     pref.sound().ifPresent(s -> v.soundOn = s == Sound.ON);
     *     return Response.redirect("/about").anchor("access");
     * });
     * }</pre>
     *
     * @param path    the route path
     * @param type    the record the parameters bind to
     * @param handler receives the request and the bound record
     * @return this for chaining
     */
    public <T> JWeb action(String path, Class<T> type, ActionHandler<T> handler) {
        RecordBinder.check(type);
        RouteHandler bound = binding(type, handler);
        router.get(path, bound);
        router.post(path, bound);
        return this;
    }

    /** A GET route whose query parameters bind to a record (see {@link #action}). */
    public <T> JWeb get(String path, Class<T> type, ActionHandler<T> handler) {
        RecordBinder.check(type);
        router.get(path, binding(type, handler));
        return this;
    }

    /** A POST route whose form parameters bind to a record (see {@link #action}). */
    public <T> JWeb post(String path, Class<T> type, ActionHandler<T> handler) {
        RecordBinder.check(type);
        router.post(path, binding(type, handler));
        return this;
    }

    private static <T> RouteHandler binding(Class<T> type, ActionHandler<T> handler) {
        return request -> {
            T params;
            try {
                params = RecordBinder.bind(type, request);
            } catch (BindException e) {
                return com.osmig.Jweb.framework.error.ErrorHandler.toResponse(
                    jweb.JWebException.badRequest(e.getMessage()),
                    request.path(), request.header("Accept"));
            }
            return handler.handle(request, params);
        };
    }

    /**
     * POST route.
     */
    public JWeb post(String path, RouteHandler handler) {
        router.post(path, handler);
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
     * Registers pages using simple map-style syntax. A path may carry
     * {@code :param} segments and a {@code *} wildcard; the page reads them
     * in {@code beforeRender(req)} ({@code req.param("id")}) or with
     * {@code pathParam("id")}.
     *
     * <pre>{@code
     * app.pages(
     *     "/", HomePage.class,
     *     "/about", AboutPage.class,
     *     "/users/:id", UserPage.class
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

    /**
     * Registers one page whose constructor needs the request, so it stays in
     * the page table with the default layout, {@code pageTitle()},
     * {@code styles()}, {@code scripts()} and {@code beforeRender()}:
     *
     * <pre>{@code
     * app.pages("/sandbox", req -> new SandboxPage(req.query("file")));
     * }</pre>
     *
     * @param path the path (may carry {@code :param} segments)
     * @param page the page for a request
     * @return this for chaining
     */
    public JWeb pages(String path, java.util.function.Function<Request, ? extends Template> page) {
        pageRegistry.register(path, page);
        return this;
    }

    /**
     * Registers one page made by a supplier: {@code app.pages("/about", AboutPage::new)}.
     *
     * @param path the path
     * @param page the page supplier
     * @return this for chaining
     */
    public JWeb pages(String path, Supplier<? extends Template> page) {
        pageRegistry.register(path, page);
        return this;
    }

    /**
     * Scans a package (recursively) for Template classes annotated with
     * {@code @Page(path = "...")} and registers each at its declared path,
     * using the current default layout.
     *
     * <pre>{@code
     * app.layout(MainLayout.class)
     *    .scanPages("com.example.app.pages");
     * }</pre>
     *
     * @param basePackage the package to scan
     * @return this for chaining
     */
    @SuppressWarnings("unchecked")
    public JWeb scanPages(String basePackage) {
        var scanner = new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new org.springframework.core.type.filter.AnnotationTypeFilter(
            com.osmig.Jweb.framework.routing.Page.class));
        for (var candidate : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                var annotation = clazz.getAnnotation(com.osmig.Jweb.framework.routing.Page.class);
                if (annotation == null || annotation.path().isEmpty()) continue;
                if (!Template.class.isAssignableFrom(clazz)) {
                    throw new IllegalStateException(
                        "@Page class " + clazz.getName() + " must implement Template");
                }
                pageRegistry.register(annotation.path(), (Class<? extends Template>) clazz);
            } catch (ClassNotFoundException e) {
                com.osmig.Jweb.framework.util.Log.warn(
                    "scanPages could not load class {}", candidate.getBeanClassName());
            }
        }
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

    public Guards getGuards() {
        return guards;
    }
}
