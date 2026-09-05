package com.osmig.Jweb.framework.ref;

import com.osmig.Jweb.framework.js.Actions.Action;
import com.osmig.Jweb.framework.js.JS;
import com.osmig.Jweb.framework.js.JS.Val;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A reference to a DOM element for imperative, client-side operations —
 * focusing, scrolling, toggling a class. JWeb is server-rendered, so a ref is
 * a unique id plus a set of {@link Action}s that target it; those plug into any
 * event handler like every other Action:
 *
 * <pre>{@code
 * Ref search = Ref.create();
 *
 * form(
 *     input(ref(search), type("text"), name("q")),
 *     button(onClick(search.focus()), "Focus the search box")
 * )
 * }</pre>
 *
 * <p>{@code Ref.create("main-modal")} prefixes the generated id;
 * {@code Ref.of("existing-id")} targets an element you named yourself.
 * {@link #get(String)} is a JS expression ({@link Val}) for reading a
 * property, so it composes with the rest of the JS DSL.</p>
 *
 * @see com.osmig.Jweb.framework.attributes.Attributes#ref(Ref)
 */
public final class Ref {

    private static final AtomicInteger counter = new AtomicInteger(0);

    private final String id;

    private Ref(String id) {
        this.id = id;
    }

    /** A ref with an auto-generated id ({@code jweb-ref-N}). */
    public static Ref create() {
        return new Ref("jweb-ref-" + counter.incrementAndGet());
    }

    /** A ref whose id is {@code prefix-N}. */
    public static Ref create(String prefix) {
        return new Ref(prefix + "-" + counter.incrementAndGet());
    }

    /** A ref to an element with exactly this id. */
    public static Ref of(String id) {
        return new Ref(id);
    }

    /** The element id this ref carries. */
    public String id() {
        return id;
    }

    /** The JS expression that resolves the element: {@code document.getElementById('id')}. */
    public String selector() {
        return "document.getElementById('" + id + "')";
    }

    /** The CSS selector for this element: {@code #id}. */
    public String cssSelector() {
        return "#" + id;
    }

    /** Focus the element. */
    public Action focus() {
        return action(".focus()");
    }

    /** Blur the element. */
    public Action blur() {
        return action(".blur()");
    }

    /** Smooth-scroll the element into view. */
    public Action scrollIntoView() {
        return action(".scrollIntoView({behavior:'smooth'})");
    }

    /**
     * Scroll the element into view with options.
     *
     * @param behavior "smooth" or "instant"
     * @param block "start", "center", "end", or "nearest"
     */
    public Action scrollIntoView(String behavior, String block) {
        return action(".scrollIntoView({behavior:'" + behavior + "',block:'" + block + "'})");
    }

    /** Click the element. */
    public Action click() {
        return action(".click()");
    }

    /** Read a property of the element — a JS expression: {@code ref.get("value")}. */
    public Val get(String property) {
        return JS.expr(selector() + "." + property);
    }

    /** Set a String property: {@code ref.set("value", "hello")}. */
    public Action set(String property, String value) {
        return action("." + property + "='" + escapeJs(value) + "'");
    }

    /** Set a numeric property. */
    public Action set(String property, Number value) {
        return action("." + property + "=" + value);
    }

    /** Add a CSS class. */
    public Action addClass(String className) {
        return action(".classList.add('" + className + "')");
    }

    /** Remove a CSS class. */
    public Action removeClass(String className) {
        return action(".classList.remove('" + className + "')");
    }

    /** Toggle a CSS class. */
    public Action toggleClass(String className) {
        return action(".classList.toggle('" + className + "')");
    }

    /** Set an inline style property (camelCase name). */
    public Action setStyle(String property, String value) {
        return action(".style." + property + "='" + escapeJs(value) + "'");
    }

    /** Set an attribute. */
    public Action setAttribute(String name, String value) {
        return action(".setAttribute('" + name + "','" + escapeJs(value) + "')");
    }

    /** Remove an attribute. */
    public Action removeAttribute(String name) {
        return action(".removeAttribute('" + name + "')");
    }

    private Action action(String tail) {
        String js = selector() + tail;
        return () -> js;
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
