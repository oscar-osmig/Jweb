package com.osmig.Jweb.framework.attributes;

/**
 * @deprecated Moved to {@link jweb.Attr} — a record, so this name cannot be a
 *             subtype: only the static factories remain here and they return
 *             {@code jweb.Attr}. Declare variables as {@code jweb.Attr}.
 */
@Deprecated
public final class Attr {

    private Attr() {}

    public static jweb.Attr id(String value) { return jweb.Attr.id(value); }
    public static jweb.Attr class_(String value) { return jweb.Attr.class_(value); }
    public static jweb.Attr style(String value) { return jweb.Attr.style(value); }
    public static jweb.Attr href(String value) { return jweb.Attr.href(value); }
    public static jweb.Attr src(String value) { return jweb.Attr.src(value); }
    public static jweb.Attr alt(String value) { return jweb.Attr.alt(value); }
    public static jweb.Attr type(String value) { return jweb.Attr.type(value); }
    public static jweb.Attr name(String value) { return jweb.Attr.name(value); }
    public static jweb.Attr value(String value) { return jweb.Attr.value(value); }
    public static jweb.Attr placeholder(String value) { return jweb.Attr.placeholder(value); }
    public static jweb.Attr action(String value) { return jweb.Attr.action(value); }
    public static jweb.Attr method(String value) { return jweb.Attr.method(value); }
    public static jweb.Attr target(String value) { return jweb.Attr.target(value); }
    public static jweb.Attr title(String value) { return jweb.Attr.title(value); }
    public static jweb.Attr for_(String value) { return jweb.Attr.for_(value); }
    public static jweb.Attr role(String value) { return jweb.Attr.role(value); }
    public static jweb.Attr disabled() { return jweb.Attr.disabled(); }
    public static jweb.Attr checked() { return jweb.Attr.checked(); }
    public static jweb.Attr required() { return jweb.Attr.required(); }
    public static jweb.Attr readonly() { return jweb.Attr.readonly(); }
    public static jweb.Attr hidden() { return jweb.Attr.hidden(); }
    public static jweb.Attr autofocus() { return jweb.Attr.autofocus(); }
    public static jweb.Attr datetime(String value) { return jweb.Attr.datetime(value); }
    public static jweb.Attr loading(String value) { return jweb.Attr.loading(value); }
    public static jweb.Attr data(String name, String value) { return jweb.Attr.data(name, value); }
    public static jweb.Attr aria(String name, String value) { return jweb.Attr.aria(name, value); }
    public static jweb.Attr attr(String name, String value) { return jweb.Attr.attr(name, value); }
}
