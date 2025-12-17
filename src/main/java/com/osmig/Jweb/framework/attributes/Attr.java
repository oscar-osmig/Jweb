package com.osmig.Jweb.framework.attributes;

/**
 * Represents a single HTML attribute.
 *
 * Usage: div(class_("container"), id("main"), ...)
 */
public record Attr(String name, String value) {

    public static Attr id(String value) { return new Attr("id", value); }
    public static Attr class_(String value) { return new Attr("class", value); }
    public static Attr style(String value) { return new Attr("style", value); }
    public static Attr href(String value) { return new Attr("href", value); }
    public static Attr src(String value) { return new Attr("src", value); }
    public static Attr alt(String value) { return new Attr("alt", value); }
    public static Attr type(String value) { return new Attr("type", value); }
    public static Attr name(String value) { return new Attr("name", value); }
    public static Attr value(String value) { return new Attr("value", value); }
    public static Attr placeholder(String value) { return new Attr("placeholder", value); }
    public static Attr action(String value) { return new Attr("action", value); }
    public static Attr method(String value) { return new Attr("method", value); }
    public static Attr target(String value) { return new Attr("target", value); }
    public static Attr title(String value) { return new Attr("title", value); }
    public static Attr for_(String value) { return new Attr("for", value); }
    public static Attr role(String value) { return new Attr("role", value); }
    public static Attr disabled() { return new Attr("disabled", null); }
    public static Attr checked() { return new Attr("checked", null); }
    public static Attr required() { return new Attr("required", null); }
    public static Attr readonly() { return new Attr("readonly", null); }
    public static Attr hidden() { return new Attr("hidden", null); }
    public static Attr autofocus() { return new Attr("autofocus", null); }
    public static Attr data(String name, String value) { return new Attr("data-" + name, value); }
    public static Attr aria(String name, String value) { return new Attr("aria-" + name, value); }
    public static Attr attr(String name, String value) { return new Attr(name, value); }
}
