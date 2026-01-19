package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS grid properties.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StyleGrid<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Grid Container ====================

    default T gridTemplateColumns(CSSValue... columns) {
        return prop("grid-template-columns", () -> joinValues(columns));
    }

    default T gridTemplateRows(CSSValue... rows) {
        return prop("grid-template-rows", () -> joinValues(rows));
    }

    default T gridTemplateAreas(String... rows) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append("\"").append(rows[i]).append("\"");
        }
        String areas = sb.toString();
        return prop("grid-template-areas", () -> areas);
    }

    default T gridAutoColumns(CSSValue value) { return prop("grid-auto-columns", value); }
    default T gridAutoRows(CSSValue value) { return prop("grid-auto-rows", value); }
    default T gridAutoFlow(CSSValue value) { return prop("grid-auto-flow", value); }

    default T justifyItems(CSSValue value) { return prop("justify-items", value); }
    default T placeItems(CSSValue value) { return prop("place-items", value); }
    default T placeContent(CSSValue value) { return prop("place-content", value); }

    // ==================== Grid Items ====================

    default T gridColumn(CSSValue value) { return prop("grid-column", value); }

    default T gridColumn(int start, int end) {
        return prop("grid-column", () -> start + " / " + end);
    }

    default T gridRow(CSSValue value) { return prop("grid-row", value); }

    default T gridRow(int start, int end) {
        return prop("grid-row", () -> start + " / " + end);
    }

    default T gridArea(CSSValue value) { return prop("grid-area", value); }
    default T placeSelf(CSSValue value) { return prop("place-self", value); }

    // ==================== Convenience Preset ====================

    default T grid(int columns) {
        return prop("display", () -> "grid")
            .prop("grid-template-columns", () -> "repeat(" + columns + ", 1fr)");
    }

    default T grid(int columns, CSSValue gapValue) {
        return grid(columns).prop("gap", gapValue);
    }

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
