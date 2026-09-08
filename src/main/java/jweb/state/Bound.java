package jweb.state;

import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VText;

/**
 * What {@code bind(state)} returns: the {@code data-state-bind} attribute the
 * client runtime patches on every change, <em>plus</em> the state's current
 * value as the element's text when nothing else was given:
 *
 * <pre>{@code
 * span(bind(clicks))               // <span data-state-bind="state_1">3</span>
 * span(bind(clicks), clicks.get()) // the explicit form still works, once
 * p("Clicks: ", span(bind(clicks)))
 * }</pre>
 *
 * <p>An element that has other children keeps them and takes only the
 * attribute, so existing two-argument code renders exactly as before.
 * ({@code bind} is for text content — a void element like {@code input}
 * wants {@code bindInput} instead.)</p>
 */
public final class Bound extends jweb.Attributes {

    private final com.osmig.Jweb.framework.state.State<?> state;

    /** Internal — obtain instances via {@code bind(state)}, not this ctor. */
    public Bound(com.osmig.Jweb.framework.state.State<?> state) {
        this.state = state;
        set("data-state-bind", state.getId());
    }

    /** The bound state. */
    public com.osmig.Jweb.framework.state.State<?> state() {
        return state;
    }

    /** The text an otherwise-empty element shows: the state's value now. */
    public VNode valueNode() {
        Object value = state.get();
        return new VText(value == null ? "" : String.valueOf(value));
    }
}
