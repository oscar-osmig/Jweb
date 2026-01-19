package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML list elements: ul, ol, li, dl, dt, dd, menu.
 */
public final class ListElements {
    private ListElements() {}

    // Unordered list
    public static Tag ul(Object... children) {
        return Tag.create("ul", children);
    }

    public static Tag ul(Attributes attrs, Object... children) {
        return new Tag("ul", attrs, Tag.toVNodes(children));
    }

    // Ordered list
    public static Tag ol(Object... children) {
        return Tag.create("ol", children);
    }

    public static Tag ol(Attributes attrs, Object... children) {
        return new Tag("ol", attrs, Tag.toVNodes(children));
    }

    // List item
    public static Tag li(Object... children) {
        return Tag.create("li", children);
    }

    public static Tag li(Attributes attrs, Object... children) {
        return new Tag("li", attrs, Tag.toVNodes(children));
    }

    // Description list
    public static Tag dl(Object... children) {
        return Tag.create("dl", children);
    }

    public static Tag dl(Attributes attrs, Object... children) {
        return new Tag("dl", attrs, Tag.toVNodes(children));
    }

    // Description term
    public static Tag dt(Object... children) {
        return Tag.create("dt", children);
    }

    // Description details
    public static Tag dd(Object... children) {
        return Tag.create("dd", children);
    }

    // Menu (semantic list for interactive items)
    public static Tag menu(Object... children) {
        return Tag.create("menu", children);
    }
}
