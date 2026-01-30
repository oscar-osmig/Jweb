package com.osmig.Jweb.framework.styles;

/**
 * CSS Subgrid DSL for inheriting grid tracks from parent grids.
 *
 * <p>CSS Subgrid allows grid items to inherit their parent grid's tracks,
 * enabling alignment across nested grid layouts.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSSubgrid.*;
 *
 * // Parent grid
 * rule(".grid")
 *     .display("grid")
 *     .prop(gridTemplateColumns("1fr 2fr 1fr"))
 *     .prop(gridTemplateRows("auto auto auto"))
 *
 * // Child inherits parent columns
 * rule(".grid > .item")
 *     .display("grid")
 *     .gridColumn("1 / -1")
 *     .prop(subgridColumns())
 *
 * // Child inherits parent rows
 * rule(".grid > .item")
 *     .display("grid")
 *     .prop(subgridRows())
 *
 * // Child inherits both
 * rule(".grid > .item")
 *     .display("grid")
 *     .prop(subgridBoth())
 * }</pre>
 *
 * @see CSS for creating style rules
 * @see CSSGrid for grid template utilities
 */
public final class CSSSubgrid {

    private CSSSubgrid() {}

    // ==================== Subgrid Values ====================

    /**
     * Creates grid-template-columns: subgrid.
     *
     * @return the property string
     */
    public static String subgridColumns() {
        return "grid-template-columns:subgrid";
    }

    /**
     * Creates grid-template-rows: subgrid.
     *
     * @return the property string
     */
    public static String subgridRows() {
        return "grid-template-rows:subgrid";
    }

    /**
     * Creates both grid-template-columns and grid-template-rows as subgrid.
     *
     * @return the property string (semicolon-separated)
     */
    public static String subgridBoth() {
        return "grid-template-columns:subgrid;grid-template-rows:subgrid";
    }

    // ==================== Grid Template Helpers ====================

    /**
     * Creates a grid-template-columns property.
     *
     * @param value the column template (e.g., "1fr 2fr 1fr", "repeat(3, 1fr)")
     * @return the property string
     */
    public static String gridTemplateColumns(String value) {
        return "grid-template-columns:" + value;
    }

    /**
     * Creates a grid-template-rows property.
     *
     * @param value the row template
     * @return the property string
     */
    public static String gridTemplateRows(String value) {
        return "grid-template-rows:" + value;
    }

    // ==================== Subgrid with Named Lines ====================

    /**
     * Creates grid-template-columns: subgrid with named lines.
     *
     * @param namedLines the named line definitions (e.g., "[header] [main] [footer]")
     * @return the property string
     */
    public static String subgridColumnsNamed(String namedLines) {
        return "grid-template-columns:subgrid " + namedLines;
    }

    /**
     * Creates grid-template-rows: subgrid with named lines.
     *
     * @param namedLines the named line definitions
     * @return the property string
     */
    public static String subgridRowsNamed(String namedLines) {
        return "grid-template-rows:subgrid " + namedLines;
    }

    // ==================== Grid Placement ====================

    /**
     * Creates a grid-column property for spanning columns.
     *
     * @param value the column placement (e.g., "1 / -1", "span 2", "1 / 3")
     * @return the property string
     */
    public static String gridColumn(String value) {
        return "grid-column:" + value;
    }

    /**
     * Creates a grid-row property for spanning rows.
     *
     * @param value the row placement
     * @return the property string
     */
    public static String gridRow(String value) {
        return "grid-row:" + value;
    }

    /**
     * Creates a grid-column that spans all columns.
     *
     * @return the property string "grid-column: 1 / -1"
     */
    public static String gridColumnFull() {
        return "grid-column:1 / -1";
    }

    /**
     * Creates a grid-row that spans all rows.
     *
     * @return the property string "grid-row: 1 / -1"
     */
    public static String gridRowFull() {
        return "grid-row:1 / -1";
    }
}
