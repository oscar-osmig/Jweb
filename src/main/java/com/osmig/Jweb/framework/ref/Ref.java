package com.osmig.Jweb.framework.ref;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ref for referencing DOM elements.
 *
 * <p>Refs provide a way to get a reference to a DOM element for imperative operations
 * like focusing, scrolling, or measuring. Since JWeb is server-rendered, refs work
 * by generating unique IDs that can be used client-side.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * Ref inputRef = Ref.create();
 *
 * form(
 *     input(attrs().ref(inputRef).type("text").name("search")),
 *     button(attrs().onClick(e -> {
 *         // Generate JS to focus the input
 *         e.eval("document.getElementById('" + inputRef.id() + "').focus()");
 *     }), "Focus")
 * )
 * }</pre>
 *
 * <h2>Named Refs</h2>
 * <pre>{@code
 * Ref modalRef = Ref.create("main-modal");
 *
 * div(attrs().ref(modalRef).class_("modal"),
 *     // modal content
 * )
 *
 * // Later, use the ID
 * button(onClick(e -> openModal(modalRef.id())), "Open Modal")
 * }</pre>
 *
 * <h2>Multiple Refs</h2>
 * <pre>{@code
 * Ref emailRef = Ref.create();
 * Ref passwordRef = Ref.create();
 *
 * form(
 *     input(attrs().ref(emailRef).type("email")),
 *     input(attrs().ref(passwordRef).type("password")),
 *     button(type("submit"), "Login")
 * )
 * }</pre>
 *
 * <h2>Using with JavaScript</h2>
 * <pre>{@code
 * Ref scrollTargetRef = Ref.create();
 *
 * div(
 *     button(attrs().onClick("scrollToElement('" + scrollTargetRef.id() + "')"),
 *         "Scroll to section"
 *     ),
 *     // ... more content
 *     section(attrs().ref(scrollTargetRef),
 *         h2("Target Section")
 *     )
 * )
 * }</pre>
 *
 * @see com.osmig.Jweb.framework.attributes.Attributes#ref(Ref)
 */
public final class Ref {

    private static final AtomicInteger counter = new AtomicInteger(0);

    private final String id;

    private Ref(String id) {
        this.id = id;
    }

    /**
     * Creates a new ref with an auto-generated ID.
     *
     * @return a new Ref
     */
    public static Ref create() {
        return new Ref("jweb-ref-" + counter.incrementAndGet());
    }

    /**
     * Creates a new ref with a custom ID prefix.
     *
     * <p>The final ID will be: prefix-{unique number}</p>
     *
     * @param prefix the ID prefix
     * @return a new Ref
     */
    public static Ref create(String prefix) {
        return new Ref(prefix + "-" + counter.incrementAndGet());
    }

    /**
     * Creates a ref with an exact ID (no suffix added).
     *
     * <p>Use this when you need a specific, predictable ID.</p>
     *
     * @param id the exact ID to use
     * @return a new Ref
     */
    public static Ref of(String id) {
        return new Ref(id);
    }

    /**
     * Gets the element ID for this ref.
     *
     * @return the HTML id attribute value
     */
    public String id() {
        return id;
    }

    /**
     * Gets a JavaScript selector for this element.
     *
     * @return document.getElementById('id') expression
     */
    public String selector() {
        return "document.getElementById('" + id + "')";
    }

    /**
     * Gets a CSS selector for this element.
     *
     * @return #id selector
     */
    public String cssSelector() {
        return "#" + id;
    }

    /**
     * Generates JavaScript to focus this element.
     *
     * @return focus() call
     */
    public String focus() {
        return selector() + ".focus()";
    }

    /**
     * Generates JavaScript to blur this element.
     *
     * @return blur() call
     */
    public String blur() {
        return selector() + ".blur()";
    }

    /**
     * Generates JavaScript to scroll this element into view.
     *
     * @return scrollIntoView() call
     */
    public String scrollIntoView() {
        return selector() + ".scrollIntoView({behavior:'smooth'})";
    }

    /**
     * Generates JavaScript to scroll this element into view with options.
     *
     * @param behavior "smooth" or "instant"
     * @param block "start", "center", "end", or "nearest"
     * @return scrollIntoView() call with options
     */
    public String scrollIntoView(String behavior, String block) {
        return selector() + ".scrollIntoView({behavior:'" + behavior + "',block:'" + block + "'})";
    }

    /**
     * Generates JavaScript to click this element.
     *
     * @return click() call
     */
    public String click() {
        return selector() + ".click()";
    }

    /**
     * Generates JavaScript to get a property value.
     *
     * @param property the property name
     * @return property access expression
     */
    public String get(String property) {
        return selector() + "." + property;
    }

    /**
     * Generates JavaScript to set a property value.
     *
     * @param property the property name
     * @param value the value (will be quoted if string)
     * @return property assignment expression
     */
    public String set(String property, String value) {
        return selector() + "." + property + "='" + escapeJs(value) + "'";
    }

    /**
     * Generates JavaScript to set a numeric property value.
     *
     * @param property the property name
     * @param value the numeric value
     * @return property assignment expression
     */
    public String set(String property, Number value) {
        return selector() + "." + property + "=" + value;
    }

    /**
     * Generates JavaScript to add a CSS class.
     *
     * @param className the class to add
     * @return classList.add() call
     */
    public String addClass(String className) {
        return selector() + ".classList.add('" + className + "')";
    }

    /**
     * Generates JavaScript to remove a CSS class.
     *
     * @param className the class to remove
     * @return classList.remove() call
     */
    public String removeClass(String className) {
        return selector() + ".classList.remove('" + className + "')";
    }

    /**
     * Generates JavaScript to toggle a CSS class.
     *
     * @param className the class to toggle
     * @return classList.toggle() call
     */
    public String toggleClass(String className) {
        return selector() + ".classList.toggle('" + className + "')";
    }

    /**
     * Generates JavaScript to set a style property.
     *
     * @param property the CSS property (camelCase)
     * @param value the value
     * @return style assignment
     */
    public String setStyle(String property, String value) {
        return selector() + ".style." + property + "='" + escapeJs(value) + "'";
    }

    /**
     * Generates JavaScript to set an attribute.
     *
     * @param name the attribute name
     * @param value the value
     * @return setAttribute() call
     */
    public String setAttribute(String name, String value) {
        return selector() + ".setAttribute('" + name + "','" + escapeJs(value) + "')";
    }

    /**
     * Generates JavaScript to remove an attribute.
     *
     * @param name the attribute name
     * @return removeAttribute() call
     */
    public String removeAttribute(String name) {
        return selector() + ".removeAttribute('" + name + "')";
    }

    private static String escapeJs(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;
        return id.equals(ref.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
