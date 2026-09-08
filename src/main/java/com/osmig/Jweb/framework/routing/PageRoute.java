package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.template.Template;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a registered page route. The path may carry {@code :param}
 * segments and a {@code *} wildcard, like a router route.
 */
public record PageRoute(
    String path,
    String title,
    Supplier<? extends Template> pageSupplier,
    Class<? extends Template> layoutClass
) {
    public PageRoute(String path, String title, Supplier<? extends Template> pageSupplier) {
        this(path, title, pageSupplier, null);
    }

    /** Whether this route needs pattern matching (has params or a wildcard). */
    public boolean isPattern() {
        return PathPattern.isPattern(path);
    }

    /** A page route matched to a request path, with its captured parameters. */
    public record Match(PageRoute route, Map<String, String> params) {}
}
