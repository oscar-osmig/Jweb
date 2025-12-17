package com.osmig.Jweb.framework.attributes;

/**
 * Static helper for creating Attributes.
 *
 * Usage: import static com.osmig.Jweb.framework.attributes.Attrs.*;
 *        div(attrs().class_("container"), ...)
 */
public final class Attrs {

    private Attrs() {}

    public static Attributes attrs() {
        return new Attributes();
    }

    public static Attributes id(String id) {
        return new Attributes().id(id);
    }

    public static Attributes class_(String className) {
        return new Attributes().class_(className);
    }

    public static Attributes inlineStyle(String style) {
        return new Attributes().style(style);
    }
}
