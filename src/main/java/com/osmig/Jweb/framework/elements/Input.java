package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for input elements.
 *
 * <p>Usage:</p>
 * <pre>
 * // Text input
 * Input.text("email").placeholder("Enter email").required()
 *
 * // Password input
 * Input.password("password").placeholder("Password").minlength(8)
 *
 * // Other types
 * Input.email("email").required()
 * Input.number("age").min(0).max(120)
 * Input.checkbox("agree").checked()
 * Input.radio("gender", "male")
 * Input.file("avatar").accept("image/*")
 * Input.hidden("csrf", token)
 *
 * // Anything this builder does not model — escape hatches, no rewrite needed
 * Input.text("q").attr("list", "suggestions").data("role", "search").aria("label", "Search")
 * Input.text("q").toTag().onInput(e -&gt; ...)      // continue with the full Tag API
 * </pre>
 *
 * <p>ID POLICY: every factory sets {@code id = name} so
 * {@code label(name, ...)} pairs with the control. The two documented
 * variations are {@link #radio(String, String)}, whose id is
 * {@code name-value} because a radio group shares one name, and
 * {@link #hidden(String, String)}, which sets no id. Override with
 * {@link #id(String)}.</p>
 *
 * @deprecated Replaced by {@code jweb.Input} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class Input implements Element {

    private String type = "text";
    private String name;
    private String id;
    private String className;
    private String value;
    private String placeholder;
    private boolean required;
    private boolean disabled;
    private boolean readonly;
    private boolean checked;
    private boolean autofocus;
    private String autocomplete;
    private String pattern;
    private String min;
    private String max;
    private String step;
    private Integer minLength;
    private Integer maxLength;
    private String accept;
    private boolean multiple;
    private String form;
    /** Anything this builder does not model: attr()/data()/aria() land here. */
    private final Map<String, String> extra = new LinkedHashMap<>();

    protected Input() {}

    // ==================== Factory Methods ====================

    /**
     * Creates a text input.
     */
    public static Input text(String name) {
        Input input = new Input();
        input.type = "text";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a password input.
     */
    public static Input password(String name) {
        Input input = new Input();
        input.type = "password";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates an email input.
     */
    public static Input email(String name) {
        Input input = new Input();
        input.type = "email";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a number input.
     */
    public static Input number(String name) {
        Input input = new Input();
        input.type = "number";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a tel (phone) input.
     */
    public static Input tel(String name) {
        Input input = new Input();
        input.type = "tel";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a URL input.
     */
    public static Input url(String name) {
        Input input = new Input();
        input.type = "url";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a search input.
     */
    public static Input search(String name) {
        Input input = new Input();
        input.type = "search";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a date input.
     */
    public static Input date(String name) {
        Input input = new Input();
        input.type = "date";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a time input.
     */
    public static Input time(String name) {
        Input input = new Input();
        input.type = "time";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a datetime-local input.
     */
    public static Input datetime(String name) {
        Input input = new Input();
        input.type = "datetime-local";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a month input.
     */
    public static Input month(String name) {
        Input input = new Input();
        input.type = "month";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a week input.
     */
    public static Input week(String name) {
        Input input = new Input();
        input.type = "week";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a color input.
     */
    public static Input color(String name) {
        Input input = new Input();
        input.type = "color";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a range (slider) input.
     */
    public static Input range(String name) {
        Input input = new Input();
        input.type = "range";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a checkbox input.
     */
    public static Input checkbox(String name) {
        Input input = new Input();
        input.type = "checkbox";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a radio input. A radio group shares one name, so the id is
     * {@code name-value} (the same scheme {@code Elements.radio} uses).
     */
    public static Input radio(String name, String value) {
        Input input = new Input();
        input.type = "radio";
        input.name = name;
        input.value = value;
        input.id = Elements.radioId(name, value);
        return input;
    }

    /**
     * Creates a file input.
     */
    public static Input file(String name) {
        Input input = new Input();
        input.type = "file";
        input.name = name;
        input.id = name;
        return input;
    }

    /**
     * Creates a hidden input.
     */
    public static Input hidden(String name, String value) {
        Input input = new Input();
        input.type = "hidden";
        input.name = name;
        input.value = value;
        return input;
    }

    /**
     * Creates a custom type input.
     */
    public static Input of(String type, String name) {
        Input input = new Input();
        input.type = type;
        input.name = name;
        input.id = name;
        return input;
    }

    // ==================== Attribute Setters ====================

    /**
     * Sets the input ID (defaults to name).
     */
    public Input id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the CSS class.
     */
    public Input class_(String className) {
        this.className = className;
        return this;
    }

    /**
     * Sets the value.
     */
    public Input value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Sets the placeholder text.
     */
    public Input placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    /**
     * Marks as required.
     */
    public Input required() {
        this.required = true;
        return this;
    }

    /**
     * Marks as disabled.
     */
    public Input disabled() {
        this.disabled = true;
        return this;
    }

    /**
     * Marks as readonly.
     */
    public Input readonly() {
        this.readonly = true;
        return this;
    }

    /**
     * Marks as checked (for checkbox/radio).
     */
    public Input checked() {
        this.checked = true;
        return this;
    }

    /**
     * Conditionally marks as checked.
     */
    public Input checked(boolean checked) {
        this.checked = checked;
        return this;
    }

    /**
     * Sets autofocus.
     */
    public Input autofocus() {
        this.autofocus = true;
        return this;
    }

    /**
     * Sets autocomplete value.
     */
    public Input autocomplete(String value) {
        this.autocomplete = value;
        return this;
    }

    /**
     * Disables autocomplete.
     */
    public Input noAutocomplete() {
        this.autocomplete = "off";
        return this;
    }

    /**
     * Sets a regex pattern for validation.
     */
    public Input pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Sets minimum value (for number, date, etc.).
     */
    public Input min(int value) {
        this.min = String.valueOf(value);
        return this;
    }

    /**
     * Sets minimum value (for date, etc.).
     */
    public Input min(String value) {
        this.min = value;
        return this;
    }

    /**
     * Sets maximum value.
     */
    public Input max(int value) {
        this.max = String.valueOf(value);
        return this;
    }

    /**
     * Sets maximum value (for date, etc.).
     */
    public Input max(String value) {
        this.max = value;
        return this;
    }

    /**
     * Sets step value (for number, range). Renders {@code step="2"}, not {@code "2.0"}.
     */
    public Input step(int value) {
        this.step = String.valueOf(value);
        return this;
    }

    /**
     * Sets a fractional step value (mirrors {@code Attributes.step(double)}).
     */
    public Input step(double value) {
        this.step = String.valueOf(value);
        return this;
    }

    /**
     * Sets step value.
     */
    public Input step(String value) {
        this.step = value;
        return this;
    }

    /**
     * Sets the minlength attribute (exact HTML spelling).
     */
    public Input minlength(int length) {
        this.minLength = length;
        return this;
    }

    /**
     * Sets the maxlength attribute (exact HTML spelling).
     */
    public Input maxlength(int length) {
        this.maxLength = length;
        return this;
    }

    /**
     * Sets minimum length.
     *
     * @deprecated Use {@link #minlength(int)} — attribute helpers use the exact
     *             HTML spelling.
     */
    @Deprecated
    public Input minLength(int length) {
        return minlength(length);
    }

    /**
     * Sets maximum length.
     *
     * @deprecated Use {@link #maxlength(int)} — attribute helpers use the exact
     *             HTML spelling.
     */
    @Deprecated
    public Input maxLength(int length) {
        return maxlength(length);
    }

    /**
     * Sets accepted file types (for file input).
     */
    public Input accept(String accept) {
        this.accept = accept;
        return this;
    }

    /**
     * Allows multiple file selection.
     */
    public Input multiple() {
        this.multiple = true;
        return this;
    }

    /**
     * Associates with a form by ID.
     */
    public Input form(String formId) {
        this.form = formId;
        return this;
    }

    // ==================== Escape Hatches ====================

    /**
     * Sets any HTML attribute this builder does not model — no rewrite to
     * {@code input(attrs()...)} needed.
     *
     * @param name the attribute name
     * @param value the value (null for a bare boolean attribute)
     * @return this for chaining
     */
    public Input attr(String name, String value) {
        extra.put(name, value);
        return this;
    }

    /**
     * Sets a {@code data-*} attribute.
     *
     * @param name the data name (without the "data-" prefix)
     * @param value the value
     * @return this for chaining
     */
    public Input data(String name, String value) {
        return attr("data-" + name, value);
    }

    /**
     * Sets an {@code aria-*} attribute.
     *
     * @param name the aria name (without the "aria-" prefix)
     * @param value the value
     * @return this for chaining
     */
    public Input aria(String name, String value) {
        return attr("aria-" + name, value);
    }

    // ==================== Build ====================

    /**
     * Materialises this builder as a {@link Tag}, so the full Tag API
     * (event handlers, styles, {@code addClass}, …) stays available:
     *
     * <pre>
     * Input.text("q").required().toTag().onInput(e -&gt; search(e.value()))
     * </pre>
     *
     * @return an {@code <input>} Tag carrying every attribute set here
     */
    public Tag toTag() {
        Tag input = new Tag("input");

        input.attr("type", type);
        if (name != null) input.attr("name", name);
        if (id != null) input.attr("id", id);
        if (className != null) input.attr("class", className);
        if (value != null) input.attr("value", value);
        if (placeholder != null) input.attr("placeholder", placeholder);
        if (required) input.required();
        if (disabled) input.disabled();
        if (readonly) input.readonly();
        if (checked) input.checked();
        if (autofocus) input.autofocus();
        if (autocomplete != null) input.attr("autocomplete", autocomplete);
        if (pattern != null) input.attr("pattern", pattern);
        if (min != null) input.attr("min", min);
        if (max != null) input.attr("max", max);
        if (step != null) input.attr("step", step);
        if (minLength != null) input.attr("minlength", String.valueOf(minLength));
        if (maxLength != null) input.attr("maxlength", String.valueOf(maxLength));
        if (accept != null) input.attr("accept", accept);
        if (multiple) input.attr("multiple", null);
        if (form != null) input.attr("form", form);
        extra.forEach(input::attr);

        return input;
    }

    @Override
    public VNode toVNode() {
        return toTag().toVNode();
    }
}
