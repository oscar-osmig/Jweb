package com.osmig.Jweb.framework.state;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Utility for binding state to DOM elements.
 *
 * <p>Usage:</p>
 * <pre>
 * // Bind state to display its value
 * span(StateBinding.bind(count), text(String.valueOf(count.get())))
 *
 * // The element will auto-update when state changes
 * </pre>
 */
public final class StateBinding {

    private StateBinding() {}

    /**
     * Creates attributes that bind an element to a state.
     * The element's text content will be updated when state changes.
     *
     * @param state the state to bind
     * @return attributes with data-state-bind attribute
     */
    public static Attributes bind(State<?> state) {
        return new Attributes().set("data-state-bind", state.getId());
    }

    /**
     * Creates attributes that bind an input element to a state.
     * The input's value will be updated when state changes.
     *
     * @param state the state to bind
     * @return attributes with data-state-bind attribute
     */
    public static Attributes bindInput(State<?> state) {
        return new Attributes()
            .set("data-state-bind", state.getId())
            .set("data-state-input", "true");
    }
}
