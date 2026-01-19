package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML5 semantic structure elements: header, footer, nav, main, section, article, aside.
 */
public final class SemanticElements {
    private SemanticElements() {}

    public static Tag header(Object... children) {
        return Tag.create("header", children);
    }

    public static Tag header(Attributes attrs, Object... children) {
        return new Tag("header", attrs, Tag.toVNodes(children));
    }

    public static Tag footer(Object... children) {
        return Tag.create("footer", children);
    }

    public static Tag footer(Attributes attrs, Object... children) {
        return new Tag("footer", attrs, Tag.toVNodes(children));
    }

    public static Tag nav(Object... children) {
        return Tag.create("nav", children);
    }

    public static Tag nav(Attributes attrs, Object... children) {
        return new Tag("nav", attrs, Tag.toVNodes(children));
    }

    public static Tag main(Object... children) {
        return Tag.create("main", children);
    }

    public static Tag main(Attributes attrs, Object... children) {
        return new Tag("main", attrs, Tag.toVNodes(children));
    }

    public static Tag section(Object... children) {
        return Tag.create("section", children);
    }

    public static Tag section(Attributes attrs, Object... children) {
        return new Tag("section", attrs, Tag.toVNodes(children));
    }

    public static Tag article(Object... children) {
        return Tag.create("article", children);
    }

    public static Tag article(Attributes attrs, Object... children) {
        return new Tag("article", attrs, Tag.toVNodes(children));
    }

    public static Tag aside(Object... children) {
        return Tag.create("aside", children);
    }

    public static Tag aside(Attributes attrs, Object... children) {
        return new Tag("aside", attrs, Tag.toVNodes(children));
    }

    public static Tag figure(Object... children) {
        return Tag.create("figure", children);
    }

    public static Tag figure(Attributes attrs, Object... children) {
        return new Tag("figure", attrs, Tag.toVNodes(children));
    }

    public static Tag figcaption(Object... children) {
        return Tag.create("figcaption", children);
    }

    public static Tag hgroup(Object... children) {
        return Tag.create("hgroup", children);
    }

    public static Tag search(Object... children) {
        return Tag.create("search", children);
    }

    public static Tag address(Object... children) {
        return Tag.create("address", children);
    }
}
