package com.osmig.Jweb.framework.elements;

import jweb.Tag;
import jweb.Attributes;

/**
 * The HTML form elements. Every one is {@code name(Object...)}; a lone String
 * is text.
 */
public final class FormElements {
    private FormElements() {}

    // ==================== Core Form Elements ====================

    public static Tag form(Object... children) { return Tag.create("form", children); }
    public static Tag form(Attributes attrs, Object... children) { return new Tag("form", attrs, Tag.toVNodes(children)); }
    public static Tag input(Object... attrs) { return Tag.create("input", attrs); }
    public static Tag input(Attributes attrs) { return new Tag("input", attrs); }
    public static Tag textarea(Object... items) { return Tag.create("textarea", items); }
    public static Tag textarea(Attributes attrs, Object... children) { return new Tag("textarea", attrs, Tag.toVNodes(children)); }
    public static Tag select(Object... children) { return Tag.create("select", children); }
    public static Tag select(Attributes attrs, Object... children) { return new Tag("select", attrs, Tag.toVNodes(children)); }

    /** {@code option(value("us"), "United States")}; a lone String is text (and thus the value). */
    public static Tag option(Object... children) { return Tag.create("option", children); }

    public static Tag optgroup(Object... children) { return Tag.create("optgroup", children); }
    public static Tag optgroup(Attributes attrs, Object... children) { return new Tag("optgroup", attrs, Tag.toVNodes(children)); }
    public static Tag label(Object... children) { return Tag.create("label", children); }
    public static Tag label(Attributes attrs, Object... children) { return new Tag("label", attrs, Tag.toVNodes(children)); }

    public static Tag button(Object... children) { return Tag.create("button", children); }
    public static Tag button(Attributes attrs, Object... children) { return new Tag("button", attrs, Tag.toVNodes(children)); }
    public static Tag fieldset(Object... children) { return Tag.create("fieldset", children); }
    public static Tag legend(Object... children) { return Tag.create("legend", children); }
    public static Tag datalist(Object... children) { return Tag.create("datalist", children); }
    public static Tag output(Object... children) { return Tag.create("output", children); }

    // The xxxInput/field helper family is gone: a form is a record
    // ({@code form(Contact.class)}, see {@link jweb.Form}) and anything outside
    // one is {@code input(type("text"), name("q"), id("q"))}.
}
