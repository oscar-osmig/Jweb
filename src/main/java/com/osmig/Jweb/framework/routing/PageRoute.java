package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.template.Template;

import java.util.function.Supplier;

/**
 * Represents a registered page route.
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
}
