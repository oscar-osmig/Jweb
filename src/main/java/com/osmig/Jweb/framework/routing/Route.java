package com.osmig.Jweb.framework.routing;

import jweb.RouteHandler;
import jweb.Request;

import java.util.function.Supplier;
import java.util.List;
import java.util.Map;

/**
 * Represents a route mapping a URL pattern to a handler.
 */
public class Route {

    private final String method;
    private final String path;
    private final PathPattern pattern;
    private final RouteHandler handler;

    public Route(String method, String path, RouteHandler handler) {
        this.method = method.toUpperCase();
        this.path = path;
        this.handler = handler;
        this.pattern = PathPattern.compile(path);
    }

    public boolean matches(String method, String path) {
        if (!this.method.equals(method.toUpperCase())) {
            return false;
        }
        return pattern.matches(path);
    }

    /** True when the path matches regardless of HTTP method (used for 405s). */
    public boolean matchesPath(String path) {
        return pattern.matches(path);
    }

    public Map<String, String> extractParams(String path) {
        return pattern.extract(path);
    }

    public Object handle(Request request) {
        return handler.handle(request);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public RouteHandler getHandler() { return handler; }
    public List<String> getParamNames() { return pattern.paramNames(); }

    public static Route get(String path, Supplier<? extends jweb.Element> elementSupplier) {
        return new Route("GET", path, req -> elementSupplier.get());
    }

    public static Route get(String path, RouteHandler handler) {
        return new Route("GET", path, handler);
    }

    public static Route post(String path, RouteHandler handler) {
        return new Route("POST", path, handler);
    }

    public static Route put(String path, RouteHandler handler) {
        return new Route("PUT", path, handler);
    }

    public static Route delete(String path, RouteHandler handler) {
        return new Route("DELETE", path, handler);
    }

    public static Route patch(String path, RouteHandler handler) {
        return new Route("PATCH", path, handler);
    }
}
