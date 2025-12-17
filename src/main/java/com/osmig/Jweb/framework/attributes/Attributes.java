package com.osmig.Jweb.framework.attributes;

import com.osmig.Jweb.framework.styles.Style;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for HTML attributes.
 *
 * Usage: attrs().id("myId").class_("btn").style("color: red")
 */
public class Attributes {

    private final Map<String, String> attributes = new LinkedHashMap<>();

    public Attributes() {}

    public Attributes(Map<String, String> initial) {
        if (initial != null) {
            this.attributes.putAll(initial);
        }
    }

    public Attributes id(String value) { return attr("id", value); }
    public Attributes class_(String value) { return attr("class", value); }
    public Attributes addClass(String className) {
        String existing = attributes.get("class");
        if (existing == null || existing.isBlank()) {
            return attr("class", className);
        }
        return attr("class", existing + " " + className);
    }
    public Attributes style(String value) { return attr("style", value); }
    public Attributes style(Style style) { return attr("style", style.build()); }
    public Attributes title(String value) { return attr("title", value); }
    public Attributes href(String value) { return attr("href", value); }
    public Attributes target(String value) { return attr("target", value); }
    public Attributes targetBlank() {
        return attr("target", "_blank").attr("rel", "noopener noreferrer");
    }
    public Attributes src(String value) { return attr("src", value); }
    public Attributes alt(String value) { return attr("alt", value); }
    public Attributes width(String value) { return attr("width", value); }
    public Attributes height(String value) { return attr("height", value); }
    public Attributes type(String value) { return attr("type", value); }
    public Attributes name(String value) { return attr("name", value); }
    public Attributes value(String value) { return attr("value", value); }
    public Attributes placeholder(String value) { return attr("placeholder", value); }
    public Attributes action(String value) { return attr("action", value); }
    public Attributes method(String value) { return attr("method", value); }
    public Attributes for_(String value) { return attr("for", value); }
    public Attributes disabled() { return attr("disabled", null); }
    public Attributes disabled(boolean isDisabled) { return isDisabled ? disabled() : this; }
    public Attributes checked() { return attr("checked", null); }
    public Attributes checked(boolean isChecked) { return isChecked ? checked() : this; }
    public Attributes required() { return attr("required", null); }
    public Attributes readonly() { return attr("readonly", null); }
    public Attributes hidden() { return attr("hidden", null); }
    public Attributes hidden(boolean isHidden) { return isHidden ? hidden() : this; }
    public Attributes autofocus() { return attr("autofocus", null); }
    public Attributes data(String name, String value) { return attr("data-" + name, value); }
    public Attributes aria(String name, String value) { return attr("aria-" + name, value); }
    public Attributes role(String value) { return attr("role", value); }
    public Attributes colspan(int value) { return attr("colspan", String.valueOf(value)); }
    public Attributes rowspan(int value) { return attr("rowspan", String.valueOf(value)); }

    public Attributes attr(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    public Attributes attrs(Map<String, String> attrs) {
        this.attributes.putAll(attrs);
        return this;
    }

    public Map<String, String> build() { return Map.copyOf(attributes); }
    public Map<String, String> toMap() { return attributes; }
    public boolean isEmpty() { return attributes.isEmpty(); }
    public String get(String name) { return attributes.get(name); }
}
