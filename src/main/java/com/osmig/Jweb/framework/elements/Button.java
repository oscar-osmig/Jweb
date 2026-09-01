package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.js.Actions.Action;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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
 * // Click handlers — server handler or Actions DSL, mirroring attrs()
 * Button.of("Save").onClick(e -&gt; save())
 * Button.of("Show").onClick(show("panel"))
 *
 * // Reset button
 * Button.reset("Clear Form")
 *
 * // Disabled button
 * Button.submit("Processing...").disabled()
 *
 * // Anything this builder does not model — no rewrite needed
 * Button.of("More").attr("popovertarget", "menu").data("role", "menu").aria("expanded", "false")
 * Button.of("More").toTag().addClass("wide")   // continue with the full Tag API
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
    /** Anything this builder does not model: attr()/data()/aria()/on* land here. */
    private final Map<String, String> extra = new LinkedHashMap<>();

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

    // ==================== Event Handlers ====================

    /**
     * Registers a click handler — the same contract as
     * {@code attrs().onClick(Consumer)}.
     *
     * @param handler the handler to execute on click
     * @return this for chaining
     */
    public Button onClick(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("click", handler);
        return attr("data-jweb-onclick", eh.getId());
    }

    /**
     * Sets a click handler from the JavaScript Actions DSL — the same contract
     * as {@code attrs().onClick(Action)}.
     *
     * @param action the action to execute on click
     * @return this for chaining
     */
    public Button onClick(Action action) {
        return attr("onclick", action.inline());
    }

    /**
     * Registers a handler for any DOM event type.
     *
     * @param eventType the DOM event type (click, focus, …)
     * @param handler the handler to execute
     * @return this for chaining
     */
    public Button on(String eventType, Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register(eventType, handler);
        return attr("data-jweb-on" + eventType, eh.getId());
    }

    /**
     * Sets a handler for any DOM event type from the Actions DSL.
     *
     * @param eventType the DOM event type
     * @param action the action to execute
     * @return this for chaining
     */
    public Button on(String eventType, Action action) {
        return attr("on" + eventType, action.inline());
    }

    // ==================== Escape Hatches ====================

    /**
     * Sets any HTML attribute this builder does not model — no rewrite to
     * {@code button(attrs()...)} needed.
     *
     * @param name the attribute name
     * @param value the value (null for a bare boolean attribute)
     * @return this for chaining
     */
    public Button attr(String name, String value) {
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
    public Button data(String name, String value) {
        return attr("data-" + name, value);
    }

    /**
     * Sets an {@code aria-*} attribute.
     *
     * @param name the aria name (without the "aria-" prefix)
     * @param value the value
     * @return this for chaining
     */
    public Button aria(String name, String value) {
        return attr("aria-" + name, value);
    }

    // ==================== Build ====================

    /**
     * Materialises this builder as a {@link Tag}, so the full Tag API stays
     * available instead of dead-ending here.
     *
     * @return a {@code <button>} Tag carrying every attribute and the text
     */
    public Tag toTag() {
        Tag button = new Tag("button");

        button.attr("type", type);
        if (id != null) button.attr("id", id);
        if (className != null) button.attr("class", className);
        if (name != null) button.attr("name", name);
        if (value != null) button.attr("value", value);
        if (disabled) button.disabled();
        if (autofocus) button.autofocus();
        if (form != null) button.attr("form", form);
        if (formAction != null) button.attr("formaction", formAction);
        if (formMethod != null) button.attr("formmethod", formMethod);
        extra.forEach(button::attr);

        if (text != null) {
            button.text(text);
        }

        return button;
    }

    @Override
    public VNode toVNode() {
        return toTag().toVNode();
    }
}
