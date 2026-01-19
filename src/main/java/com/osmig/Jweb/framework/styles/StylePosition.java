package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS positioning properties.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StylePosition<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Position ====================

    default T position(CSSValue value) { return prop("position", value); }
    default T top(CSSValue value) { return prop("top", value); }
    default T right(CSSValue value) { return prop("right", value); }
    default T bottom(CSSValue value) { return prop("bottom", value); }
    default T left(CSSValue value) { return prop("left", value); }

    default T inset(CSSValue value) { return prop("inset", value); }

    default T inset(CSSValue vertical, CSSValue horizontal) {
        return prop("inset", () -> vertical.css() + " " + horizontal.css());
    }

    default T insetInline(CSSValue value) { return prop("inset-inline", value); }
    default T insetBlock(CSSValue value) { return prop("inset-block", value); }

    default T zIndex(int value) { return prop("z-index", () -> String.valueOf(value)); }

    // ==================== Display ====================

    default T display(CSSValue value) { return prop("display", value); }
    default T visibility(CSSValue value) { return prop("visibility", value); }

    // ==================== Overflow ====================

    default T overflow(CSSValue value) { return prop("overflow", value); }
    default T overflowX(CSSValue value) { return prop("overflow-x", value); }
    default T overflowY(CSSValue value) { return prop("overflow-y", value); }

    // ==================== Convenience Presets ====================

    default T absolute() { return position(() -> "absolute").inset(() -> "0"); }
    default T relative() { return position(() -> "relative"); }
    default T fixed() { return position(() -> "fixed"); }
    default T sticky() { return position(() -> "sticky"); }

    default T full() { return prop("width", () -> "100%").prop("height", () -> "100%"); }
    default T fullWidth() { return prop("width", () -> "100%"); }
    default T fullHeight() { return prop("height", () -> "100%"); }
}
