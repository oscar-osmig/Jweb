package com.osmig.Jweb.framework.styles;

/**
 * CSS Grid template functions for type-safe grid layouts.
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSSGrid.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 *
 * // Basic grid with equal columns
 * style().gridTemplateColumns(fr(1), fr(1), fr(1))
 *
 * // Responsive grid with repeat
 * style().gridTemplateColumns(repeat(3, fr(1)))
 *
 * // Auto-fill responsive grid
 * style().gridTemplateColumns(repeat(autoFill(), minmax(px(250), fr(1))))
 *
 * // Complex grid
 * style().gridTemplateColumns(px(200), fr(1), minmax(px(100), px(300)))
 * </pre>
 *
 * @see Style#gridTemplateColumns(CSSValue...)
 * @see Style#gridTemplateRows(CSSValue...)
 */
public final class CSSGrid {

    private CSSGrid() {}

    // ==================== Repeat Function ====================

    /**
     * Creates a repeat() function with a fixed count.
     *
     * <p>Example:</p>
     * <pre>
     * repeat(3, fr(1))           // repeat(3, 1fr)
     * repeat(4, px(100))         // repeat(4, 100px)
     * </pre>
     *
     * @param count the number of repetitions
     * @param value the track size to repeat
     * @return a CSSValue for the repeat function
     */
    public static CSSValue repeat(int count, CSSValue value) {
        return () -> "repeat(" + count + ", " + value.css() + ")";
    }

    /**
     * Creates a repeat() function with multiple track sizes.
     *
     * <p>Example:</p>
     * <pre>
     * repeat(2, px(100), fr(1))  // repeat(2, 100px 1fr)
     * </pre>
     *
     * @param count the number of repetitions
     * @param values the track sizes to repeat
     * @return a CSSValue for the repeat function
     */
    public static CSSValue repeat(int count, CSSValue... values) {
        return () -> "repeat(" + count + ", " + joinCss(values) + ")";
    }

    /**
     * Creates a repeat() function with auto-fill or auto-fit.
     *
     * <p>Example:</p>
     * <pre>
     * repeat(autoFill(), minmax(px(250), fr(1)))
     * repeat(autoFit(), px(200))
     * </pre>
     *
     * @param autoKeyword autoFill() or autoFit()
     * @param value the track size to repeat
     * @return a CSSValue for the repeat function
     */
    public static CSSValue repeat(CSSValue autoKeyword, CSSValue value) {
        return () -> "repeat(" + autoKeyword.css() + ", " + value.css() + ")";
    }

    /**
     * Creates a repeat() function with auto-fill/auto-fit and multiple track sizes.
     *
     * @param autoKeyword autoFill() or autoFit()
     * @param values the track sizes to repeat
     * @return a CSSValue for the repeat function
     */
    public static CSSValue repeat(CSSValue autoKeyword, CSSValue... values) {
        return () -> "repeat(" + autoKeyword.css() + ", " + joinCss(values) + ")";
    }

    // ==================== Auto Keywords ====================

    /**
     * Returns the auto-fill keyword for repeat().
     * Creates as many columns/rows as will fit without overflowing.
     * Empty tracks are preserved.
     *
     * <p>Example:</p>
     * <pre>
     * repeat(autoFill(), minmax(px(200), fr(1)))
     * // Creates responsive grid that fills available space
     * </pre>
     *
     * @return the auto-fill keyword
     */
    public static CSSValue autoFill() {
        return () -> "auto-fill";
    }

    /**
     * Returns the auto-fit keyword for repeat().
     * Similar to auto-fill but collapses empty tracks.
     *
     * <p>Example:</p>
     * <pre>
     * repeat(autoFit(), minmax(px(200), fr(1)))
     * // Creates responsive grid, empty columns collapse
     * </pre>
     *
     * @return the auto-fit keyword
     */
    public static CSSValue autoFit() {
        return () -> "auto-fit";
    }

    // ==================== Minmax Function ====================

    /**
     * Creates a minmax() function for flexible track sizing.
     *
     * <p>Example:</p>
     * <pre>
     * minmax(px(100), fr(1))       // At least 100px, grows to fill
     * minmax(minContent(), px(300)) // Content width up to 300px
     * minmax(px(200), maxContent()) // At least 200px, grows with content
     * </pre>
     *
     * @param min the minimum track size
     * @param max the maximum track size
     * @return a CSSValue for the minmax function
     */
    public static CSSValue minmax(CSSValue min, CSSValue max) {
        return () -> "minmax(" + min.css() + ", " + max.css() + ")";
    }

    // ==================== Content Sizing Keywords ====================

    /**
     * Returns the min-content keyword.
     * The intrinsic minimum width of the content.
     *
     * @return the min-content keyword
     */
    public static CSSValue minContent() {
        return () -> "min-content";
    }

    /**
     * Returns the max-content keyword.
     * The intrinsic preferred width of the content.
     *
     * @return the max-content keyword
     */
    public static CSSValue maxContent() {
        return () -> "max-content";
    }

    /**
     * Creates a fit-content() function.
     * Clamps the size between min-content and the specified maximum.
     *
     * <p>Example:</p>
     * <pre>
     * fitContent(px(300))  // fit-content(300px)
     * fitContent(percent(50))
     * </pre>
     *
     * @param max the maximum size
     * @return a CSSValue for the fit-content function
     */
    public static CSSValue fitContent(CSSValue max) {
        return () -> "fit-content(" + max.css() + ")";
    }

    // ==================== Subgrid ====================

    /**
     * Returns the subgrid keyword for nested grids.
     * Allows a grid item to use its parent's grid tracks.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateColumns(subgrid())
     * </pre>
     *
     * @return the subgrid keyword
     */
    public static CSSValue subgrid() {
        return () -> "subgrid";
    }

    // ==================== Masonry (Experimental) ====================

    /**
     * Returns the masonry keyword for masonry layout.
     * Note: This is an experimental feature and may not be supported in all browsers.
     *
     * @return the masonry keyword
     */
    public static CSSValue masonry() {
        return () -> "masonry";
    }

    // ==================== Grid Line Names ====================

    /**
     * Creates a named grid line.
     *
     * <p>Example:</p>
     * <pre>
     * gridTemplateColumns(lineName("sidebar-start"), px(200), lineName("sidebar-end", "main-start"), fr(1))
     * </pre>
     *
     * @param names one or more names for the line
     * @return a CSSValue for the named line
     */
    public static CSSValue lineName(String... names) {
        return () -> "[" + String.join(" ", names) + "]";
    }

    // ==================== Grid Area ====================

    /**
     * Creates a grid-area value from row and column positions.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridArea(area(1, 1, 3, 2))  // grid-area: 1 / 1 / 3 / 2
     * </pre>
     *
     * @param rowStart the row start line
     * @param colStart the column start line
     * @param rowEnd the row end line
     * @param colEnd the column end line
     * @return a CSSValue for the grid-area
     */
    public static CSSValue area(int rowStart, int colStart, int rowEnd, int colEnd) {
        return () -> rowStart + " / " + colStart + " / " + rowEnd + " / " + colEnd;
    }

    /**
     * Creates a grid-area value with named lines.
     *
     * @param rowStart the row start line name
     * @param colStart the column start line name
     * @param rowEnd the row end line name
     * @param colEnd the column end line name
     * @return a CSSValue for the grid-area
     */
    public static CSSValue area(String rowStart, String colStart, String rowEnd, String colEnd) {
        return () -> rowStart + " / " + colStart + " / " + rowEnd + " / " + colEnd;
    }

    /**
     * Creates a span value for grid positioning.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridColumn(span(2))      // grid-column: span 2
     * style().gridRow(span(3))         // grid-row: span 3
     * </pre>
     *
     * @param count the number of tracks to span
     * @return a CSSValue for the span
     */
    public static CSSValue span(int count) {
        return () -> "span " + count;
    }

    /**
     * Creates a span value with a named line.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridColumn(span("sidebar"))  // grid-column: span sidebar
     * </pre>
     *
     * @param lineName the name of the line to span to
     * @return a CSSValue for the span
     */
    public static CSSValue span(String lineName) {
        return () -> "span " + lineName;
    }

    // ==================== Grid Template Areas ====================

    /**
     * Creates a grid-template-areas value from row definitions.
     *
     * <p>Example:</p>
     * <pre>
     * style().prop("grid-template-areas", templateAreas(
     *     "header header header",
     *     "sidebar main aside",
     *     "footer footer footer"
     * ))
     * </pre>
     *
     * @param rows the area strings for each row
     * @return a CSSValue for grid-template-areas
     */
    public static CSSValue templateAreas(String... rows) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append("\"").append(rows[i]).append("\"");
        }
        String result = sb.toString();
        return () -> result;
    }

    // ==================== Dense Packing ====================

    /**
     * Returns 'row dense' for grid-auto-flow.
     * Enables dense packing algorithm with row direction.
     *
     * @return the row dense value
     */
    public static CSSValue rowDense() {
        return () -> "row dense";
    }

    /**
     * Returns 'column dense' for grid-auto-flow.
     * Enables dense packing algorithm with column direction.
     *
     * @return the column dense value
     */
    public static CSSValue columnDense() {
        return () -> "column dense";
    }

    /**
     * Returns 'dense' for grid-auto-flow.
     * Enables dense packing algorithm.
     *
     * @return the dense value
     */
    public static CSSValue dense() {
        return () -> "dense";
    }

    // ==================== Helper Methods ====================

    private static String joinCss(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }
}
