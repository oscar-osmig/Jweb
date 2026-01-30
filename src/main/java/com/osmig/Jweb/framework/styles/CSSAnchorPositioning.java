package com.osmig.Jweb.framework.styles;

/**
 * CSS Anchor Positioning DSL for positioning elements relative to anchors.
 *
 * <p>CSS Anchor Positioning allows elements to be positioned relative to
 * other "anchor" elements, even when they are not in the same stacking
 * context or parent. This is useful for tooltips, popovers, and dropdowns.</p>
 *
 * <h2>Basic Anchor Positioning</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSAnchorPositioning.*;
 *
 * // Define an anchor
 * rule(".anchor-element")
 *     .prop(anchorName("--my-anchor"))
 *
 * // Position relative to anchor
 * rule(".positioned-element")
 *     .prop(positionAnchor("--my-anchor"))
 *     .position("absolute")
 *     .prop(top(anchor("--my-anchor", "bottom")))
 *     .prop(left(anchor("--my-anchor", "left")))
 * }</pre>
 *
 * <h2>Position Area</h2>
 * <pre>{@code
 * // Using position-area for simpler positioning
 * rule(".tooltip")
 *     .prop(positionAnchor("--trigger"))
 *     .prop(positionArea("top"))
 * }</pre>
 *
 * <h2>Fallback Positioning</h2>
 * <pre>{@code
 * // Define fallback positions
 * String css = positionFallback("--tooltip-fallback",
 *     tryTactic("flip-block"),
 *     tryTactic("flip-inline"),
 *     tryTactic("flip-block", "flip-inline")
 * );
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public final class CSSAnchorPositioning {

    private CSSAnchorPositioning() {}

    // ==================== Anchor Definition ====================

    /**
     * Creates an anchor-name property value.
     *
     * @param name the dashed-ident anchor name (e.g., "--my-anchor")
     * @return a property string "anchor-name: name"
     */
    public static String anchorName(String name) {
        return "anchor-name:" + name;
    }

    /**
     * Creates a position-anchor property value (links positioned element to its anchor).
     *
     * @param name the dashed-ident anchor name
     * @return a property string "position-anchor: name"
     */
    public static String positionAnchor(String name) {
        return "position-anchor:" + name;
    }

    // ==================== anchor() Function ====================

    /**
     * Creates an anchor() function value for use in inset properties.
     * References an anchor element's edge.
     *
     * @param anchorName the anchor name
     * @param side the anchor side: "top", "right", "bottom", "left", "center",
     *             "start", "end", "self-start", "self-end"
     * @return the anchor() function string
     */
    public static String anchor(String anchorName, String side) {
        return "anchor(" + anchorName + " " + side + ")";
    }

    /**
     * Creates an anchor() function with fallback.
     *
     * @param anchorName the anchor name
     * @param side the anchor side
     * @param fallback the fallback value if anchor is unavailable
     * @return the anchor() function string
     */
    public static String anchor(String anchorName, String side, String fallback) {
        return "anchor(" + anchorName + " " + side + "," + fallback + ")";
    }

    /**
     * Creates an anchor() function using the default anchor.
     *
     * @param side the anchor side
     * @return the anchor() function string
     */
    public static String anchor(String side) {
        return "anchor(" + side + ")";
    }

    // ==================== anchor-size() Function ====================

    /**
     * Creates an anchor-size() function value for sizing relative to anchor.
     *
     * @param anchorName the anchor name
     * @param dimension "width", "height", "block", "inline", "self-block", "self-inline"
     * @return the anchor-size() function string
     */
    public static String anchorSize(String anchorName, String dimension) {
        return "anchor-size(" + anchorName + " " + dimension + ")";
    }

    /**
     * Creates an anchor-size() function with fallback.
     *
     * @param anchorName the anchor name
     * @param dimension the dimension
     * @param fallback the fallback value
     * @return the anchor-size() function string
     */
    public static String anchorSize(String anchorName, String dimension, String fallback) {
        return "anchor-size(" + anchorName + " " + dimension + "," + fallback + ")";
    }

    /**
     * Creates an anchor-size() using default anchor.
     *
     * @param dimension the dimension
     * @return the anchor-size() function string
     */
    public static String anchorSizeDefault(String dimension) {
        return "anchor-size(" + dimension + ")";
    }

    // ==================== Position Area ====================

    /**
     * Creates a position-area property value for simplified anchor positioning.
     *
     * @param area the position area value, e.g., "top", "bottom", "left", "right",
     *             "top left", "bottom right", "center", "span-all"
     * @return a property string "position-area: area"
     */
    public static String positionArea(String area) {
        return "position-area:" + area;
    }

    // ==================== Inset Properties with anchor() ====================

    /**
     * Creates a top property with anchor() value.
     *
     * @param anchorValue the anchor() function string
     * @return a property string "top: anchor()"
     */
    public static String top(String anchorValue) {
        return "top:" + anchorValue;
    }

    /**
     * Creates a right property with anchor() value.
     *
     * @param anchorValue the anchor() function string
     * @return a property string "right: anchor()"
     */
    public static String right(String anchorValue) {
        return "right:" + anchorValue;
    }

    /**
     * Creates a bottom property with anchor() value.
     *
     * @param anchorValue the anchor() function string
     * @return a property string "bottom: anchor()"
     */
    public static String bottom(String anchorValue) {
        return "bottom:" + anchorValue;
    }

    /**
     * Creates a left property with anchor() value.
     *
     * @param anchorValue the anchor() function string
     * @return a property string "left: anchor()"
     */
    public static String left(String anchorValue) {
        return "left:" + anchorValue;
    }

    // ==================== Position Fallback ====================

    /**
     * Creates a @position-fallback rule with try tactics.
     *
     * @param name the dashed-ident name for the fallback
     * @param tactics the try tactics
     * @return the CSS @position-fallback block
     */
    public static String positionFallback(String name, String... tactics) {
        StringBuilder sb = new StringBuilder("@position-fallback ").append(name).append(" {\n");
        for (String tactic : tactics) {
            sb.append("  ").append(tactic).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates a position-try-fallbacks property value.
     *
     * @param fallbacks the fallback values (e.g., "flip-block", "--my-fallback")
     * @return a property string "position-try-fallbacks: values"
     */
    public static String positionTryFallbacks(String... fallbacks) {
        return "position-try-fallbacks:" + String.join(",", fallbacks);
    }

    /**
     * Creates a @position-try block.
     *
     * @param name the dashed-ident name
     * @param properties CSS property declarations
     * @return the CSS @position-try block
     */
    public static String positionTry(String name, String... properties) {
        StringBuilder sb = new StringBuilder("@position-try ").append(name).append(" {\n");
        for (String prop : properties) {
            sb.append("  ").append(prop).append(";\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates a try-tactic value for position fallbacks.
     *
     * @param tactics one or more tactics: "flip-block", "flip-inline", "flip-start"
     * @return the try-tactic CSS string
     */
    public static String tryTactic(String... tactics) {
        return "@try { " + String.join(" ", tactics) + "; }";
    }

    // ==================== Position Visibility ====================

    /**
     * Creates a position-visibility property value.
     *
     * @param value "always", "anchors-visible", or "no-overflow"
     * @return a property string "position-visibility: value"
     */
    public static String positionVisibility(String value) {
        return "position-visibility:" + value;
    }

    // ==================== Convenience Methods ====================

    /**
     * Positions an element above its anchor.
     *
     * @return the position-area property for top positioning
     */
    public static String positionAbove() {
        return positionArea("top");
    }

    /**
     * Positions an element below its anchor.
     *
     * @return the position-area property for bottom positioning
     */
    public static String positionBelow() {
        return positionArea("bottom");
    }

    /**
     * Positions an element to the left of its anchor.
     *
     * @return the position-area property for left positioning
     */
    public static String positionLeft() {
        return positionArea("left");
    }

    /**
     * Positions an element to the right of its anchor.
     *
     * @return the position-area property for right positioning
     */
    public static String positionRight() {
        return positionArea("right");
    }
}
