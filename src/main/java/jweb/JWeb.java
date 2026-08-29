package jweb;

import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.core.Page;
import com.osmig.Jweb.framework.routing.Route;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.routing.TypedRoute;
import com.osmig.Jweb.framework.server.Request;
import com.osmig.Jweb.framework.template.Template;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JWeb - A minimalist Java web framework.
 *
 * <p>Build simple web applications entirely in Java with a clean, fluent API.</p>
 *
 * <pre>{@code
 * import jweb.JWeb;
 * import jweb.JWebRoutes;
 *
 * @Component
 * public class Routes implements JWebRoutes {
 *     @Override
 *     public void configure(JWeb app) {
 *         app.layout(MainLayout.class)
 *            .pages(
 *                "/", HomePage.class,
 *                "/about", AboutPage.class
 *            );
 *     }
 * }
 * }</pre>
 *
 * <p>This is the short-import entry point; it extends
 * {@link com.osmig.Jweb.framework.JWeb} and adds nothing but tighter return
 * types, so both spellings are interchangeable.</p>
 */
public class JWeb extends com.osmig.Jweb.framework.JWeb {

    protected JWeb() {}

    /**
     * Creates a new JWeb application.
     */
    public static JWeb create() {
        return new JWeb();
    }

    // ==================== Middleware ====================

    @Override
    public JWeb use(Middleware middleware) {
        super.use(middleware);
        return this;
    }

    @Override
    public JWeb useIf(boolean condition, Middleware middleware) {
        super.useIf(condition, middleware);
        return this;
    }

    @Override
    public JWeb use(String pathPrefix, Middleware middleware) {
        super.use(pathPrefix, middleware);
        return this;
    }

    @Override
    public JWeb useForMethods(List<String> methods, Middleware middleware) {
        super.useForMethods(methods, middleware);
        return this;
    }

    // ==================== Routes ====================

    @Override
    public JWeb get(String path, Supplier<? extends Element> handler) {
        super.get(path, handler);
        return this;
    }

    @Override
    public JWeb get(String path, RouteHandler handler) {
        super.get(path, handler);
        return this;
    }

    @Override
    public JWeb route(String path, Supplier<? extends Page> pageSupplier) {
        super.route(path, pageSupplier);
        return this;
    }

    @Override
    public JWeb route(String path, RouteHandler handler) {
        super.route(path, handler);
        return this;
    }

    @Override
    public JWeb get(TypedRoute.Path0 route, RouteHandler handler) {
        super.get(route, handler);
        return this;
    }

    @Override
    public <A> JWeb get(TypedRoute.Path1<A> route, BiFunction<Request, A, Object> handler) {
        super.get(route, handler);
        return this;
    }

    @Override
    public <A, B> JWeb get(TypedRoute.Path2<A, B> route, TriHandler<A, B> handler) {
        super.get(route, handler);
        return this;
    }

    @Override
    public <A> JWeb post(TypedRoute.Path1<A> route, BiFunction<Request, A, Object> handler) {
        super.post(route, handler);
        return this;
    }

    @Override
    public JWeb post(String path, RouteHandler handler) {
        super.post(path, handler);
        return this;
    }

    @Override
    public JWeb post(String path, Function<Request, Object> handler) {
        super.post(path, handler);
        return this;
    }

    @Override
    public JWeb put(String path, RouteHandler handler) {
        super.put(path, handler);
        return this;
    }

    @Override
    public JWeb delete(String path, RouteHandler handler) {
        super.delete(path, handler);
        return this;
    }

    @Override
    public JWeb addRoute(Route route) {
        super.addRoute(route);
        return this;
    }

    // ==================== Pages ====================

    @Override
    public JWeb layout(Class<? extends Template> layoutClass) {
        super.layout(layoutClass);
        return this;
    }

    @Override
    public JWeb pages(Object... pathsAndPages) {
        super.pages(pathsAndPages);
        return this;
    }

    @Override
    public JWeb scanPages(String basePackage) {
        super.scanPages(basePackage);
        return this;
    }
}
