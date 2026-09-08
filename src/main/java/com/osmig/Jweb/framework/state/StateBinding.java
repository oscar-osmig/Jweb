package com.osmig.Jweb.framework.state;

import jweb.Attributes;
import jweb.state.Bound;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The attribute bindings between reactive state and DOM elements. Each one
 * renders the state's current value on the server and a {@code data-state-*}
 * attribute the client runtime uses to patch the element on every change:
 *
 * <pre>{@code
 * span(bind(count))                       // text = the value
 * input(bindInput(name))                  // value = the value, two-way
 * button(bindAttr(busy, "disabled"), "Go") // attribute present while truthy
 * li(bindClass(active, "on"), "Home")     // class present while truthy
 * }</pre>
 *
 * <p>The client-side contract lives in {@code JWebRuntime.initLive}.</p>
 */
public final class StateBinding {

    private StateBinding() {}

    /**
     * Binds an element's text to a state: renders the value as the element's
     * content when it is the only content, and patches the text on change.
     *
     * @param state the state to bind
     * @return the {@code data-state-bind} attribute carrying the value
     */
    public static Bound bind(State<?> state) {
        return new Bound(state);
    }

    /**
     * Binds an input's value to a state, both ways: the input renders with
     * the state's value (a Boolean state checks a checkbox) and every
     * keystroke sends {@code setState} back to the server.
     *
     * @param state the state to bind
     * @return attributes with data-state-bind, data-state-input and the value
     */
    public static Attributes bindInput(State<?> state) {
        Attributes attrs = new Attributes()
            .set("data-state-bind", state.getId())
            .set("data-state-input", "true");
        Object value = state.get();
        if (value instanceof Boolean b) {
            if (b) attrs.set("checked", null);
        } else if (value != null) {
            attrs.set("value", String.valueOf(value));
        }
        return attrs;
    }

    /**
     * Binds an attribute's presence to a state's truthiness: {@code true},
     * a non-zero number, a non-empty string or collection set the attribute
     * (a {@code true} sets it bare, anything else sets it to the value);
     * {@code null}, {@code false}, {@code 0} and {@code ""} remove it.
     *
     * <pre>{@code
     * button(bindAttr(saving, "disabled"), "Save")
     * input(bindAttr(query, "value"))
     * }</pre>
     *
     * @param state     the state to watch
     * @param attribute the attribute name
     * @return the binding attribute, plus the attribute itself when truthy now
     */
    public static Attributes bindAttr(State<?> state, String attribute) {
        Attributes attrs = new Attributes().set("data-state-attr", attribute + "=" + state.getId());
        Object value = state.get();
        if (truthy(value)) {
            attrs.set(attribute, Boolean.TRUE.equals(value) ? null : String.valueOf(value));
        }
        return attrs;
    }

    /**
     * Binds a class's presence to a state's truthiness (same rules as
     * {@link #bindAttr}). Put it before any {@code class_(...)} on the same
     * element: other classes stay, and the bound class is (re)applied by the
     * runtime as soon as it initializes.
     *
     * <pre>{@code
     * li(bindClass(active, "on"), class_("tab"), "Home")
     * }</pre>
     *
     * @param state     the state to watch
     * @param className the class to toggle
     * @return the binding attribute, plus the class when truthy now
     */
    public static Attributes bindClass(State<?> state, String className) {
        Attributes attrs = new Attributes().set("data-state-class", className + "=" + state.getId());
        if (truthy(state.get())) {
            attrs.set("class", className);
        }
        return attrs;
    }

    /** The truthiness rule both sides of the wire agree on. */
    static boolean truthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.doubleValue() != 0;
        if (value instanceof CharSequence s) return !s.isEmpty();
        if (value instanceof Collection<?> c) return !c.isEmpty();
        if (value instanceof Map<?, ?> m) return !m.isEmpty();
        if (value instanceof Optional<?> o) return o.isPresent();
        return true;
    }
}
