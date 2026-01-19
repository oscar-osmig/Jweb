package com.osmig.Jweb.framework.attributes;

/**
 * Static helper for creating Attributes.
 *
 * @deprecated Use {@code El.attrs()} instead.
 *             Import: {@code import static com.osmig.Jweb.framework.elements.El.*;}
 */
@Deprecated(since = "2.0", forRemoval = true)
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
