package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS flexbox properties.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StyleFlex<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Flex Container ====================

    default T flexDirection(CSSValue value) { return prop("flex-direction", value); }
    default T flexWrap(CSSValue value) { return prop("flex-wrap", value); }

    default T flexFlow(CSSValue direction, CSSValue wrap) {
        return prop("flex-flow", () -> direction.css() + " " + wrap.css());
    }

    default T justifyContent(CSSValue value) { return prop("justify-content", value); }
    default T alignItems(CSSValue value) { return prop("align-items", value); }
    default T alignContent(CSSValue value) { return prop("align-content", value); }

    default T gap(CSSValue value) { return prop("gap", value); }

    default T gap(CSSValue row, CSSValue column) {
        return prop("gap", () -> row.css() + " " + column.css());
    }

    default T rowGap(CSSValue value) { return prop("row-gap", value); }
    default T columnGap(CSSValue value) { return prop("column-gap", value); }

    // ==================== Flex Items ====================

    default T alignSelf(CSSValue value) { return prop("align-self", value); }
    default T flex(CSSValue value) { return prop("flex", value); }

    default T flex(int grow, int shrink, CSSValue basis) {
        return prop("flex", () -> grow + " " + shrink + " " + basis.css());
    }

    default T flexGrow(int value) { return prop("flex-grow", () -> String.valueOf(value)); }
    default T flexShrink(int value) { return prop("flex-shrink", () -> String.valueOf(value)); }
    default T flexBasis(CSSValue value) { return prop("flex-basis", value); }
    default T order(int value) { return prop("order", () -> String.valueOf(value)); }

    // ==================== Convenience Presets ====================

    default T flex() { return prop("display", () -> "flex"); }
    default T flexCol() { return flex().flexDirection(() -> "column"); }
    default T flexRow() { return flex().flexDirection(() -> "row"); }

    default T flexCenter() {
        return flex().justifyContent(() -> "center").alignItems(() -> "center");
    }

    default T flexBetween() {
        return flex().justifyContent(() -> "space-between").alignItems(() -> "center");
    }
}
