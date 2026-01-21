package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS effects: transform, transition, animation, shadow, filter.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StyleEffects<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Transform ====================

    default T transform(CSSValue... transforms) {
        return prop("transform", () -> joinValues(transforms));
    }

    default T transformOrigin(String value) { return prop("transform-origin", () -> value); }

    // ==================== Transition ====================

    default T transition(CSSValue property, CSSValue duration, CSSValue timing) {
        return prop("transition", () -> property.css() + " " + duration.css() + " " + timing.css());
    }

    default T transition(CSSValue property, CSSValue duration) {
        return prop("transition", () -> property.css() + " " + duration.css());
    }

    default T transition(CSSValue value) { return prop("transition", value); }
    default T transitionProperty(CSSValue value) { return prop("transition-property", value); }
    default T transitionDuration(CSSValue value) { return prop("transition-duration", value); }
    default T transitionTimingFunction(CSSValue value) { return prop("transition-timing-function", value); }
    default T transitionDelay(CSSValue value) { return prop("transition-delay", value); }

    // ==================== Animation ====================

    default T animation(CSSValue value) { return prop("animation", value); }

    default T animation(CSSValue name, CSSValue duration, CSSValue timing) {
        return prop("animation", () -> name.css() + " " + duration.css() + " " + timing.css());
    }

    default T animationName(CSSValue value) { return prop("animation-name", value); }
    default T animationDuration(CSSValue value) { return prop("animation-duration", value); }
    default T animationTimingFunction(CSSValue value) { return prop("animation-timing-function", value); }
    default T animationDelay(CSSValue value) { return prop("animation-delay", value); }
    default T animationIterationCount(CSSValue value) { return prop("animation-iteration-count", value); }
    default T animationDirection(CSSValue value) { return prop("animation-direction", value); }
    default T animationFillMode(CSSValue value) { return prop("animation-fill-mode", value); }
    default T animationTimeline(CSSValue value) { return prop("animation-timeline", value); }

    // ==================== Shadows ====================

    default T boxShadow(CSSValue x, CSSValue y, CSSValue blur, CSSValue color) {
        return prop("box-shadow", () -> x.css() + " " + y.css() + " " + blur.css() + " " + color.css());
    }

    default T boxShadow(String value) { return prop("box-shadow", () -> value); }

    // ==================== Filter ====================

    default T filter(CSSValue... filters) {
        return prop("filter", () -> joinValues(filters));
    }

    default T backdropFilter(CSSValue... filters) {
        return prop("backdrop-filter", () -> joinValues(filters));
    }

    default T opacity(double value) { return prop("opacity", () -> String.valueOf(value)); }

    // ==================== Clip ====================

    default T clipPath(CSSValue value) { return prop("clip-path", value); }

    // ==================== Helper ====================

    private static String joinValues(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }
}
