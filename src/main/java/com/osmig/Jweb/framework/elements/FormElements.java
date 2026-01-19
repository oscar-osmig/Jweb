package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML form elements with helper methods for common input types.
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

    public static Tag option(String value, String text) {
        return new Tag("option", new Attributes().value(value), Tag.toVNodes(new Object[]{text}));
    }

    public static Tag optgroup(Attributes attrs, Object... children) { return new Tag("optgroup", attrs, Tag.toVNodes(children)); }
    public static Tag label(Object... children) { return Tag.create("label", children); }
    public static Tag label(Attributes attrs, Object... children) { return new Tag("label", attrs, Tag.toVNodes(children)); }

    public static Tag label(String forId, Object... children) {
        return new Tag("label", new Attributes().for_(forId), Tag.toVNodes(children));
    }

    public static Tag button(Object... children) { return Tag.create("button", children); }
    public static Tag button(Attributes attrs, Object... children) { return new Tag("button", attrs, Tag.toVNodes(children)); }
    public static Tag fieldset(Object... children) { return Tag.create("fieldset", children); }
    public static Tag legend(Object... children) { return Tag.create("legend", children); }
    public static Tag datalist(Object... children) { return Tag.create("datalist", children); }
    public static Tag output(Object... children) { return Tag.create("output", children); }

    // ==================== Input Helpers ====================

    public static Tag textInput(String name) { return input(new Attributes().type("text").name(name).id(name)); }
    public static Tag textInput(String name, String placeholder) { return input(new Attributes().type("text").name(name).id(name).placeholder(placeholder)); }
    public static Tag emailInput(String name) { return input(new Attributes().type("email").name(name).id(name)); }
    public static Tag passwordInput(String name) { return input(new Attributes().type("password").name(name).id(name)); }
    public static Tag numberInput(String name) { return input(new Attributes().type("number").name(name).id(name)); }
    public static Tag checkbox(String name, String value) { return input(new Attributes().type("checkbox").name(name).value(value).id(name)); }
    public static Tag radio(String name, String value) { return input(new Attributes().type("radio").name(name).value(value).id(name + "-" + value)); }
    public static Tag hiddenInput(String name, String value) { return input(new Attributes().type("hidden").name(name).value(value)); }
    public static Tag fileInput(String name) { return input(new Attributes().type("file").name(name).id(name)); }
    public static Tag dateInput(String name) { return input(new Attributes().type("date").name(name).id(name)); }
    public static Tag searchInput(String name, String placeholder) { return input(new Attributes().type("search").name(name).id(name).placeholder(placeholder)); }

    // ==================== Button Helpers ====================

    public static Tag submitButton(String text) { return button(new Attributes().type("submit"), text); }
    public static Tag resetButton(String text) { return button(new Attributes().type("reset"), text); }
}
