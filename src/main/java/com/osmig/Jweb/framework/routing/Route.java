package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.server.Request;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * Represents a route mapping a URL pattern to a handler.
 */
public class Route {

    private final String method;
    private final String path;
    private final Pattern pattern;
    private final List<String> paramNames;
    private final RouteHandler handler;

    public Route(String method, String path, RouteHandler handler) {
        this.method = method.toUpperCase();
        this.path = path;
        this.handler = handler;
        this.paramNames = new ArrayList<>();
        this.pattern = compilePath(path);
    }

    private Pattern compilePath(String path) {
        StringBuilder regex = new StringBuilder("^");
        String[] segments = path.split("/");

        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            regex.append("/");
            if (segment.startsWith(":")) {
                String paramName = segment.substring(1);
                paramNames.add(paramName);
                regex.append("([^/]+)");
            } else if (segment.equals("*")) {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(segment));
            }
        }

        if (path.equals("/")) {
            regex.append("/?");
        } else {
            regex.append("/?");
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    public boolean matches(String method, String path) {
        if (!this.method.equals(method.toUpperCase())) {
            return false;
        }
        return pattern.matcher(path).matches();
    }

    public Map<String, String> extractParams(String path) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = pattern.matcher(path);

        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }
        return params;
    }

    public Object handle(Request request) {
        return handler.handle(request);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public RouteHandler getHandler() { return handler; }
    public List<String> getParamNames() { return Collections.unmodifiableList(paramNames); }

    public static Route get(String path, Supplier<Element> elementSupplier) {
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
}
