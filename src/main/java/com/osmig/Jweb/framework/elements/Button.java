package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;

/**
 * Fluent builder for button elements.
 *
 * <p>Usage:</p>
 * <pre>
 * // Submit button
 * Button.submit("Login")
 *
 * // Regular button
 * Button.of("Click Me").id("myBtn").class_("btn btn-primary")
 *
 * // Button with icon (using raw HTML or child elements)
 * Button.submit("Save").class_("btn")
 *
 * // Reset button
 * Button.reset("Clear Form")
 *
 * // Disabled button
 * Button.submit("Processing...").disabled()
 * </pre>
 */
public class Button implements Element {

    private String type = "button";
    private String text;
    private String id;
    private String className;
    private String name;
    private String value;
    private boolean disabled;
    private boolean autofocus;
    private String form;
    private String formAction;
    private String formMethod;

    private Button() {}

    // ==================== Factory Methods ====================

    /**
     * Creates a button with text.
     */
    public static Button of(String text) {
        Button button = new Button();
        button.text = text;
        button.type = "button";
        return button;
    }

    /**
     * Creates a submit button.
     */
    public static Button submit(String text) {
        Button button = new Button();
        button.text = text;
        button.type = "submit";
        return button;
    }

    /**
     * Creates a reset button.
     */
    public static Button reset(String text) {
        Button button = new Button();
        button.text = text;
        button.type = "reset";
        return button;
    }

    /**
     * Creates an empty button builder.
     */
    public static Button create() {
        return new Button();
    }

    // ==================== Type Setters ====================

    /**
     * Sets type to submit.
     */
    public Button submit() {
        this.type = "submit";
        return this;
    }

    /**
     * Sets type to reset.
     */
    public Button reset() {
        this.type = "reset";
        return this;
    }

    /**
     * Sets type to button (default).
     */
    public Button button() {
        this.type = "button";
        return this;
    }

    // ==================== Attribute Setters ====================

    /**
     * Sets the button text.
     */
    public Button text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the button ID.
     */
    public Button id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the CSS class.
     */
    public Button class_(String className) {
        this.className = className;
        return this;
    }

    /**
     * Sets the name attribute.
     */
    public Button name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the value attribute.
     */
    public Button value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Marks as disabled.
     */
    public Button disabled() {
        this.disabled = true;
        return this;
    }

    /**
     * Conditionally disables.
     */
    public Button disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Sets autofocus.
     */
    public Button autofocus() {
        this.autofocus = true;
        return this;
    }

    /**
     * Associates with a form by ID.
     */
    public Button form(String formId) {
        this.form = formId;
        return this;
    }

    /**
     * Overrides the form's action URL.
     */
    public Button formAction(String action) {
        this.formAction = action;
        return this;
    }

    /**
     * Overrides the form's method.
     */
    public Button formMethod(String method) {
        this.formMethod = method;
        return this;
    }

    // ==================== Build ====================

    @Override
    public VNode toVNode() {
        Tag button = new Tag("button");

        button.attr("type", type);
        if (id != null) button.attr("id", id);
        if (className != null) button.attr("class", className);
        if (name != null) button.attr("name", name);
        if (value != null) button.attr("value", value);
        if (disabled) button.attr("disabled", "");
        if (autofocus) button.attr("autofocus", "");
        if (form != null) button.attr("form", form);
        if (formAction != null) button.attr("formaction", formAction);
        if (formMethod != null) button.attr("formmethod", formMethod);

        if (text != null) {
            button.text(text);
        }

        return button.toVNode();
    }
}
