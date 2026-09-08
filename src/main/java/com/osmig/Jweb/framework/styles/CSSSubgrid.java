package com.osmig.Jweb.framework.styles;

import jweb.Style;

/**
 * CSS Subgrid DSL for inheriting grid tracks from parent grids.
 *
 * <p>CSS Subgrid allows grid items to inherit their parent grid's tracks,
 * enabling alignment across nested grid layouts.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Parent grid
 * rule(".grid")
 *     .display("grid")
 *     .gridTemplateColumns("1fr 2fr 1fr")
 *     .gridTemplateRows("auto auto auto")
 *
 * // Child inherits parent columns
 * rule(".grid > .item")
 *     .display("grid")
 *     .gridColumn("1 / -1")
 *     .apply(subgridColumns())
 *
 * // Child inherits parent rows
 * rule(".grid > .item")
 *     .display("grid")
 *     .apply(subgridRows())
 *
 * // Child inherits both
 * rule(".grid > .item")
 *     .display("grid")
 *     .apply(subgridBoth())
 * }</pre>
 *
 * @see CSS for creating style rules
 * @see CSSGrid for grid template utilities
 */
public class CSSSubgrid extends CSSTextWrap {

    protected CSSSubgrid() {}

    // ==================== Subgrid Values ====================

    /**
     * Creates a grid-template-columns: subgrid declaration.
     *
     * @return a {@code Style} holding {@code grid-template-columns: subgrid}
     */
    public static Style<?> subgridColumns() {
        return CSS.style().prop("grid-template-columns", "subgrid");
    }

    /**
     * Creates a grid-template-rows: subgrid declaration.
     *
     * @return a {@code Style} holding {@code grid-template-rows: subgrid}
     */
    public static Style<?> subgridRows() {
        return CSS.style().prop("grid-template-rows", "subgrid");
    }

    /**
     * Creates both grid-template-columns and grid-template-rows as subgrid.
     *
     * @return a {@code Style} holding both declarations
     */
    public static Style<?> subgridBoth() {
        return CSS.style()
            .prop("grid-template-columns", "subgrid")
            .prop("grid-template-rows", "subgrid");
    }

    // ==================== Subgrid with Named Lines ====================

    /**
     * Creates grid-template-columns: subgrid with named lines.
     *
     * @param namedLines the named line definitions (e.g., "[header] [main] [footer]")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> subgridColumnsNamed(String namedLines) {
        return CSS.style().prop("grid-template-columns", "subgrid " + namedLines);
    }

    /**
     * Creates grid-template-rows: subgrid with named lines.
     *
     * @param namedLines the named line definitions
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> subgridRowsNamed(String namedLines) {
        return CSS.style().prop("grid-template-rows", "subgrid " + namedLines);
    }

    // ==================== Grid Placement ====================

    /**
     * Creates a grid-column declaration that spans all columns.
     *
     * @return a {@code Style} holding {@code grid-column: 1 / -1}
     */
    public static Style<?> gridColumnFull() {
        return CSS.style().prop("grid-column", "1 / -1");
    }

    /**
     * Creates a grid-row declaration that spans all rows.
     *
     * @return a {@code Style} holding {@code grid-row: 1 / -1}
     */
    public static Style<?> gridRowFull() {
        return CSS.style().prop("grid-row", "1 / -1");
    }
}
