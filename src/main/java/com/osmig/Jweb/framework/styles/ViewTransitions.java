package com.osmig.Jweb.framework.styles;

/**
 * Builder for the {@code @view-transition} at-rule — the cross-document
 * opt-in that lets the browser animate between full (multi-page) navigations
 * the same way {@code document.startViewTransition()} animates a
 * same-document DOM swap. Without this rule, cross-document navigations
 * never trigger a view transition, no matter what {@code ::view-transition-*}
 * pseudo-elements are styled.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.ViewTransitions.*;
 *
 * stylesheet().add(viewTransitions())
 * // Output: @view-transition{navigation:auto}
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.css.ViewTransitions} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class ViewTransitions {

    private String navigation = "auto";

    protected ViewTransitions() {}

    /**
     * Starts the {@code @view-transition} at-rule, defaulting to
     * {@code navigation: auto} — the setting that actually opts the document
     * into cross-document view transitions.
     *
     * @return a new ViewTransitions builder
     */
    public static ViewTransitions viewTransitions() {
        return new ViewTransitions();
    }

    /**
     * Sets the {@code navigation} descriptor.
     *
     * @param value "auto" (opt in) or "none" (opt out, the default)
     * @return this builder for chaining
     */
    public ViewTransitions navigation(String value) {
        this.navigation = value;
        return this;
    }

    /**
     * Builds the {@code @view-transition} at-rule.
     *
     * @return the minified at-rule, e.g. {@code @view-transition{navigation:auto}}
     */
    public String build() {
        return "@view-transition{navigation:" + navigation + "}";
    }

    @Override
    public String toString() {
        return build();
    }
}
