package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.server.Request;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Manages route registration and matching.
 */
public class Router {

    private final List<Route> routes = new ArrayList<>();

    public Router addRoute(Route route) {
        routes.add(route);
        return this;
    }

    public Router get(String path, Supplier<Element> handler) {
        return addRoute(Route.get(path, handler));
    }

    public Router get(String path, RouteHandler handler) {
        return addRoute(Route.get(path, handler));
    }

    public Router getElement(String path, Function<Request, Element> handler) {
        RouteHandler routeHandler = req -> handler.apply(req);
        return addRoute(Route.get(path, routeHandler));
    }

    public Router post(String path, RouteHandler handler) {
        return addRoute(Route.post(path, handler));
    }

    public Router put(String path, RouteHandler handler) {
        return addRoute(Route.put(path, handler));
    }

    public Router delete(String path, RouteHandler handler) {
        return addRoute(Route.delete(path, handler));
    }

    public Router patch(String path, RouteHandler handler) {
        return addRoute(Route.patch(path, handler));
    }

    public Optional<RouteMatch> match(String method, String path) {
        // HEAD is served by GET handlers (the container strips the body)
        String effective = "HEAD".equalsIgnoreCase(method) ? "GET" : method;
        for (Route route : routes) {
            if (route.matches(effective, path)) {
                Map<String, String> params = route.extractParams(path);
                return Optional.of(new RouteMatch(route, params));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the HTTP methods that have a route at this path.
     * Empty when no route matches the path at all (a true 404);
     * non-empty when the path exists but was requested with the
     * wrong method (a 405).
     */
    public Set<String> allowedMethods(String path) {
        Set<String> methods = new TreeSet<>();
        for (Route route : routes) {
            if (route.matchesPath(path)) {
                methods.add(route.getMethod());
            }
        }
        return methods;
    }

    public List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public record RouteMatch(Route route, Map<String, String> params) {
        public Object handle(Request request) {
            request.setPathParams(params);
            return route.handle(request);
        }
    }
}
