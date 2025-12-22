package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;

/**
 * Fluent builder for input elements.
 *
 * <p>Usage:</p>
 * <pre>
 * // Text input
 * Input.text("email").placeholder("Enter email").required()
 *
 * // Password input
 * Input.password("password").placeholder("Password").minLength(8)
 *
 * // Other types
 * Input.email("email").required()
 * Input.number("age").min(0).max(120)
 * Input.checkbox("agree").checked()
 * Input.radio("gender", "male")
 * Input.file("avatar").accept("image/*")
 * Input.hidden("csrf", token)
 * </pre>
 */
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

    private Input() {}

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
     * Creates a radio input.
     */
    public static Input radio(String name, String value) {
        Input input = new Input();
        input.type = "radio";
        input.name = name;
        input.value = value;
        input.id = name + "_" + value;
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
     * Sets step value (for number, range).
     */
    public Input step(int value) {
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
     * Sets minimum length.
     */
    public Input minLength(int length) {
        this.minLength = length;
        return this;
    }

    /**
     * Sets maximum length.
     */
    public Input maxLength(int length) {
        this.maxLength = length;
        return this;
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

    // ==================== Build ====================

    @Override
    public VNode toVNode() {
        Tag input = new Tag("input");

        input.attr("type", type);
        if (name != null) input.attr("name", name);
        if (id != null) input.attr("id", id);
        if (className != null) input.attr("class", className);
        if (value != null) input.attr("value", value);
        if (placeholder != null) input.attr("placeholder", placeholder);
        if (required) input.attr("required", "");
        if (disabled) input.attr("disabled", "");
        if (readonly) input.attr("readonly", "");
        if (checked) input.attr("checked", "");
        if (autofocus) input.attr("autofocus", "");
        if (autocomplete != null) input.attr("autocomplete", autocomplete);
        if (pattern != null) input.attr("pattern", pattern);
        if (min != null) input.attr("min", min);
        if (max != null) input.attr("max", max);
        if (step != null) input.attr("step", step);
        if (minLength != null) input.attr("minlength", String.valueOf(minLength));
        if (maxLength != null) input.attr("maxlength", String.valueOf(maxLength));
        if (accept != null) input.attr("accept", accept);
        if (multiple) input.attr("multiple", "");
        if (form != null) input.attr("form", form);

        return input.toVNode();
    }
}
