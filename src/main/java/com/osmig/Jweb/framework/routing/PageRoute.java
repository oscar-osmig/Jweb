package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.template.Template;
import jweb.Request;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a registered page route. The path may carry {@code :param}
 * segments and a {@code *} wildcard, like a router route.
 *
 * <p>A page is made by a {@link Supplier} (a no-arg class or a lambda) or,
 * when its constructor needs something from the request, by a
 * {@link Function} over the request — {@link #page(Request)} picks the one
 * the route was registered with.</p>
 */
public record PageRoute(
    String path,
    String title,
    Supplier<? extends Template> pageSupplier,
    Class<? extends Template> layoutClass,
    Function<Request, ? extends Template> pageFactory
) {
    public PageRoute(String path, String title, Supplier<? extends Template> pageSupplier) {
        this(path, title, pageSupplier, null, null);
    }

    public PageRoute(String path, String title, Supplier<? extends Template> pageSupplier,
                     Class<? extends Template> layoutClass) {
        this(path, title, pageSupplier, layoutClass, null);
    }

    /** The page for this request — from the factory when there is one, else the supplier. */
    public Template page(Request request) {
        if (pageFactory != null) return pageFactory.apply(request);
        return pageSupplier.get();
    }

    /** True when the page needs a request to be made (it cannot be pre-rendered). */
    public boolean needsRequest() {
        return pageFactory != null;
    }

    /** Whether this route needs pattern matching (has params or a wildcard). */
    public boolean isPattern() {
        return PathPattern.isPattern(path);
    }

    /** A page route matched to a request path, with its captured parameters. */
    public record Match(PageRoute route, Map<String, String> params) {}
}
