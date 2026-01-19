package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML document structure elements: html, head, body, title, meta, link, script.
 */
public final class DocumentElements {
    private DocumentElements() {}

    public static Tag html(Object... children) {
        return Tag.create("html", children);
    }

    public static Tag html(Attributes attrs, Object... children) {
        return new Tag("html", attrs, Tag.toVNodes(children));
    }

    public static Tag head(Object... children) {
        return Tag.create("head", children);
    }

    public static Tag body(Object... children) {
        return Tag.create("body", children);
    }

    public static Tag body(Attributes attrs, Object... children) {
        return new Tag("body", attrs, Tag.toVNodes(children));
    }

    public static Tag title(String text) {
        return Tag.create("title", text);
    }

    public static Tag meta(Object... attrs) {
        return Tag.create("meta", attrs);
    }

    public static Tag meta(Attributes attrs) {
        return new Tag("meta", attrs);
    }

    public static Tag meta(String name, String content) {
        return new Tag("meta", new Attributes().name(name).set("content", content));
    }

    public static Tag link(Object... attrs) {
        return Tag.create("link", attrs);
    }

    public static Tag link(Attributes attrs) {
        return new Tag("link", attrs);
    }

    public static Tag css(String href) {
        return new Tag("link", new Attributes().set("rel", "stylesheet").href(href));
    }

    public static Tag script(String src) {
        return new Tag("script", new Attributes().src(src));
    }

    public static Tag inlineScript(String code) {
        return Tag.create("script", TextElement.raw(code));
    }

    public static Tag style(String css) {
        return Tag.create("style", TextElement.raw(css));
    }

    public static Tag noscript(Object... children) {
        return Tag.create("noscript", children);
    }

    public static Tag base(Attributes attrs) {
        return new Tag("base", attrs);
    }
}
