package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML form elements with helper methods for common input types.
 *
 * <p>The {@code xxxInput} helpers here are thin delegates to the canonical
 * implementations in {@link Elements}, so every entry point produces byte-identical
 * markup. See {@link Elements} for the id policy (id defaults to name).</p>
 */
@SuppressWarnings("deprecation")
public final class FormElements {
    private FormElements() {}

    // ==================== Core Form Elements ====================

    public static Tag form(Object... children) { return Tag.create("form", children); }
    public static Tag form(Attributes attrs, Object... children) { return new Tag("form", attrs, Tag.toVNodes(children)); }
    public static Tag input(Object... attrs) { return Tag.create("input", attrs); }
    public static Tag input(Attributes attrs) { return new Tag("input", attrs); }
    public static Tag textarea(Object... items) { return Tag.create("textarea", items); }
    /** {@code textarea("Hello")} renders {@code <textarea>Hello</textarea>} — a lone String is text. */
    public static Tag textarea(String text) { return Tag.create("textarea", TextElement.of(text)); }
    public static Tag textarea(Attributes attrs, Object... children) { return new Tag("textarea", attrs, Tag.toVNodes(children)); }
    public static Tag select(Object... children) { return Tag.create("select", children); }
    public static Tag select(Attributes attrs, Object... children) { return new Tag("select", attrs, Tag.toVNodes(children)); }

    public static Tag option(String value, String text) {
        return new Tag("option", new Attributes().value(value), Tag.toVNodes(new Object[]{text}));
    }

    /** Option whose value equals its visible text. */
    public static Tag option(String valueAndText) {
        return option(valueAndText, valueAndText);
    }

    public static Tag optgroup(Object... children) { return Tag.create("optgroup", children); }
    /** {@code optgroup("Cars")} renders {@code <optgroup>Cars</optgroup>}. */
    public static Tag optgroup(String text) { return Tag.create("optgroup", TextElement.of(text)); }
    public static Tag optgroup(Attributes attrs, Object... children) { return new Tag("optgroup", attrs, Tag.toVNodes(children)); }
    public static Tag label(Object... children) { return Tag.create("label", children); }
    /** {@code label("Email:")} renders {@code <label>Email:</label>} — a lone String is text. */
    public static Tag label(String text) { return Tag.create("label", TextElement.of(text)); }
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
    // One canonical implementation lives in Elements; these delegate so both
    // entry points always agree. ID POLICY: id defaults to name (so
    // label(name, ...) pairs with it); radio ids are "name-value" because a
    // radio group shares one name; hiddenInput sets no id.

    /** Text input; id defaults to name. */
    public static Tag textInput(String name) { return Elements.textInput(name); }
    /** Text input with placeholder; id defaults to name. */
    public static Tag textInput(String name, String placeholder) { return Elements.textInput(name, placeholder); }
    /** Email input; id defaults to name. */
    public static Tag emailInput(String name) { return Elements.emailInput(name); }
    /** Password input; id defaults to name. */
    public static Tag passwordInput(String name) { return Elements.passwordInput(name); }
    /** Number input; id defaults to name. */
    public static Tag numberInput(String name) { return Elements.numberInput(name); }
    /** Checkbox; id defaults to name. */
    public static Tag checkbox(String name, String value) { return Elements.checkbox(name, value); }
    /** Radio button; id is {@code name-value}. */
    public static Tag radio(String name, String value) { return Elements.radio(name, value); }
    /** Hidden input; no id. */
    public static Tag hiddenInput(String name, String value) { return Elements.hiddenInput(name, value); }
    /** File input; id defaults to name. */
    public static Tag fileInput(String name) { return Elements.fileInput(name); }
    /** Date input; id defaults to name. */
    public static Tag dateInput(String name) { return Elements.dateInput(name); }
    /** Search input; id defaults to name. */
    public static Tag searchInput(String name, String placeholder) { return Elements.searchInput(name, placeholder); }

    // ==================== Button Helpers ====================

    /** @deprecated Use {@code button(type("submit"), text)} instead. */
    @Deprecated
    public static Tag submitButton(String text) { return button(new Attributes().type("submit"), text); }
    /** @deprecated Use {@code button(type("reset"), text)} instead. */
    @Deprecated
    public static Tag resetButton(String text) { return button(new Attributes().type("reset"), text); }
}
