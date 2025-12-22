package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for form elements.
 *
 * <p>Usage:</p>
 * <pre>
 * // Chained builder style
 * Form.action("/submit").post()
 *     .add(Input.text("email").placeholder("Email"))
 *     .add(Input.password("password").placeholder("Password"))
 *     .add(Button.submit("Login"))
 *
 * // Or start with method
 * Form.post("/login")
 *     .add(Input.text("username"))
 *     .add(Button.submit("Submit"))
 * </pre>
 */
public class Form implements Element {

    private String action;
    private String method = "get";
    private String id;
    private String className;
    private String enctype;
    private boolean novalidate;
    private String target;
    private String autocomplete;
    private final List<Element> children = new ArrayList<>();

    private Form() {}

    // ==================== Factory Methods ====================

    /**
     * Creates a form with the specified action URL.
     */
    public static Form action(String action) {
        Form form = new Form();
        form.action = action;
        return form;
    }

    /**
     * Creates a GET form with the specified action.
     */
    public static Form get(String action) {
        return action(action).get();
    }

    /**
     * Creates a POST form with the specified action.
     */
    public static Form post(String action) {
        return action(action).post();
    }

    /**
     * Creates an empty form builder.
     */
    public static Form create() {
        return new Form();
    }

    // ==================== Method Setters ====================

    /**
     * Sets method to GET.
     */
    public Form get() {
        this.method = "get";
        return this;
    }

    /**
     * Sets method to POST.
     */
    public Form post() {
        this.method = "post";
        return this;
    }

    /**
     * Sets method to PUT (for REST APIs with form submission).
     */
    public Form put() {
        this.method = "put";
        return this;
    }

    /**
     * Sets method to DELETE.
     */
    public Form delete() {
        this.method = "delete";
        return this;
    }

    // ==================== Attribute Setters ====================

    /**
     * Sets the action URL.
     */
    public Form setAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Sets the form ID.
     */
    public Form id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the form CSS class.
     */
    public Form class_(String className) {
        this.className = className;
        return this;
    }

    /**
     * Sets enctype to multipart/form-data (for file uploads).
     */
    public Form multipart() {
        this.enctype = "multipart/form-data";
        return this;
    }

    /**
     * Sets custom enctype.
     */
    public Form enctype(String enctype) {
        this.enctype = enctype;
        return this;
    }

    /**
     * Disables browser validation.
     */
    public Form novalidate() {
        this.novalidate = true;
        return this;
    }

    /**
     * Sets the target attribute.
     */
    public Form target(String target) {
        this.target = target;
        return this;
    }

    /**
     * Sets autocomplete on/off.
     */
    public Form autocomplete(String value) {
        this.autocomplete = value;
        return this;
    }

    /**
     * Disables autocomplete.
     */
    public Form noAutocomplete() {
        this.autocomplete = "off";
        return this;
    }

    // ==================== Children ====================

    /**
     * Adds a child element to the form.
     */
    public Form add(Element child) {
        if (child != null) {
            children.add(child);
        }
        return this;
    }

    /**
     * Adds multiple child elements.
     */
    public Form add(Element... elements) {
        for (Element el : elements) {
            if (el != null) {
                children.add(el);
            }
        }
        return this;
    }

    // ==================== Build ====================

    @Override
    public VNode toVNode() {
        Tag form = new Tag("form");

        if (action != null) form.attr("action", action);
        if (method != null) form.attr("method", method);
        if (id != null) form.attr("id", id);
        if (className != null) form.attr("class", className);
        if (enctype != null) form.attr("enctype", enctype);
        if (novalidate) form.attr("novalidate", "");
        if (target != null) form.attr("target", target);
        if (autocomplete != null) form.attr("autocomplete", autocomplete);

        form.children(children);

        return form.toVNode();
    }
}
