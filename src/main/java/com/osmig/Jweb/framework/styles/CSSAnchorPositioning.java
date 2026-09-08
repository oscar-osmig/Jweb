package com.osmig.Jweb.framework.styles;

/**
 * CSS Anchor Positioning DSL for positioning elements relative to anchors.
 *
 * <p>CSS Anchor Positioning allows elements to be positioned relative to
 * other "anchor" elements, even when they are not in the same stacking
 * context or parent. This is useful for tooltips, popovers, and dropdowns.</p>
 *
 * <p>The root of the modern-CSS chain reachable through {@code jweb.Css.*} —
 * its declaration factories return {@link jweb.Style}, so they compose with
 * {@link jweb.Style#apply(jweb.Style)}, and its {@code anchor()} /
 * {@code anchorSize()} function helpers return {@link jweb.CSSValue}, so they
 * feed straight into any typed property.</p>
 *
 * <h2>Basic Anchor Positioning</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Define an anchor
 * rule(".anchor-element")
 *     .apply(anchorName("--my-anchor"))
 *
 * // Position relative to anchor
 * rule(".positioned-element")
 *     .apply(positionAnchor("--my-anchor"))
 *     .position("absolute")
 *     .top(anchor("--my-anchor", "bottom"))
 *     .left(anchor("--my-anchor", "left"))
 * }</pre>
 *
 * <h2>Position Area</h2>
 * <pre>{@code
 * // Using position-area for simpler positioning
 * rule(".tooltip")
 *     .apply(positionAnchor("--trigger"))
 *     .apply(positionArea("top"))
 * }</pre>
 *
 * <h2>Fallback Positioning</h2>
 * <pre>{@code
 * rule(".tooltip").apply(positionTryFallbacks("flip-block, flip-inline"));
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public class CSSAnchorPositioning {

    protected CSSAnchorPositioning() {}

    // ==================== Anchor Definition ====================

    /**
     * Creates an anchor-name declaration.
     *
     * @param name the dashed-ident anchor name (e.g., "--my-anchor")
     * @return a {@code Style} holding {@code anchor-name: name}
     */
    public static jweb.Style<?> anchorName(String name) {
        return CSS.style().prop("anchor-name", name);
    }

    /**
     * Creates an anchor-name declaration from a {@code CSSValue}.
     *
     * @param name the anchor name
     * @return a {@code Style} holding {@code anchor-name: name}
     */
    public static jweb.Style<?> anchorName(jweb.CSSValue name) {
        return anchorName(name.css());
    }

    /**
     * Creates a position-anchor declaration (links positioned element to its anchor).
     *
     * @param name the dashed-ident anchor name
     * @return a {@code Style} holding {@code position-anchor: name}
     */
    public static jweb.Style<?> positionAnchor(String name) {
        return CSS.style().prop("position-anchor", name);
    }

    /**
     * Creates a position-anchor declaration from a {@code CSSValue}.
     *
     * @param name the anchor name
     * @return a {@code Style} holding {@code position-anchor: name}
     */
    public static jweb.Style<?> positionAnchor(jweb.CSSValue name) {
        return positionAnchor(name.css());
    }

    // ==================== anchor() Function ====================

    /**
     * Creates an anchor() function value for use in inset properties.
     * References an anchor element's edge.
     *
     * @param anchorName the anchor name
     * @param side the anchor side: "top", "right", "bottom", "left", "center",
     *             "start", "end", "self-start", "self-end"
     * @return the anchor() function value
     */
    public static jweb.CSSValue anchor(String anchorName, String side) {
        return () -> "anchor(" + anchorName + " " + side + ")";
    }

    /**
     * Creates an anchor() function with fallback.
     *
     * @param anchorName the anchor name
     * @param side the anchor side
     * @param fallback the fallback value if anchor is unavailable
     * @return the anchor() function value
     */
    public static jweb.CSSValue anchor(String anchorName, String side, String fallback) {
        return () -> "anchor(" + anchorName + " " + side + "," + fallback + ")";
    }

    /**
     * Creates an anchor() function using the default anchor.
     *
     * @param side the anchor side
     * @return the anchor() function value
     */
    public static jweb.CSSValue anchor(String side) {
        return () -> "anchor(" + side + ")";
    }

    // ==================== anchor-size() Function ====================

    /**
     * Creates an anchor-size() function value for sizing relative to anchor.
     *
     * @param anchorName the anchor name
     * @param dimension "width", "height", "block", "inline", "self-block", "self-inline"
     * @return the anchor-size() function value
     */
    public static jweb.CSSValue anchorSize(String anchorName, String dimension) {
        return () -> "anchor-size(" + anchorName + " " + dimension + ")";
    }

    /**
     * Creates an anchor-size() function with fallback.
     *
     * @param anchorName the anchor name
     * @param dimension the dimension
     * @param fallback the fallback value
     * @return the anchor-size() function value
     */
    public static jweb.CSSValue anchorSize(String anchorName, String dimension, String fallback) {
        return () -> "anchor-size(" + anchorName + " " + dimension + "," + fallback + ")";
    }

    /**
     * Creates an anchor-size() using default anchor.
     *
     * @param dimension the dimension
     * @return the anchor-size() function value
     */
    public static jweb.CSSValue anchorSizeDefault(String dimension) {
        return () -> "anchor-size(" + dimension + ")";
    }

    // ==================== Position Area ====================

    /**
     * Creates a position-area declaration for simplified anchor positioning.
     *
     * @param area the position area value, e.g., "top", "bottom", "left", "right",
     *             "top left", "bottom right", "center", "span-all"
     * @return a {@code Style} holding {@code position-area: area}
     */
    public static jweb.Style<?> positionArea(String area) {
        return CSS.style().prop("position-area", area);
    }

    /**
     * Creates a position-area declaration from a {@code CSSValue}.
     *
     * @param area the position area value
     * @return a {@code Style} holding {@code position-area: area}
     */
    public static jweb.Style<?> positionArea(jweb.CSSValue area) {
        return positionArea(area.css());
    }

    // ==================== Position Fallback ====================

    // @position-fallback and @try were dropped from the spec before shipping —
    // positionFallback()/tryTactic() have been removed. Use
    // positionTryFallbacks(...) plus positionTry(name, style) below.

    /**
     * Creates a position-try-fallbacks declaration.
     *
     * @param fallbacks the fallback values (e.g., "flip-block", "--my-fallback")
     * @return a {@code Style} holding {@code position-try-fallbacks: values}
     */
    public static jweb.Style<?> positionTryFallbacks(String... fallbacks) {
        return CSS.style().prop("position-try-fallbacks", String.join(",", fallbacks));
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
     * Creates a {@code @position-try} block from a Style.
     *
     * <p>Example:</p>
     * <pre>
     * positionTry("--flip-up", style().bottom("anchor(top)").top("auto"))
     * // @position-try --flip-up {
     * //   bottom: anchor(top); top: auto;
     * // }
     * </pre>
     *
     * @param name the dashed-ident name (e.g. {@code "--flip-up"})
     * @param style the position overrides to try
     * @return the CSS @position-try block
     */
    public static String positionTry(String name, jweb.Style<?> style) {
        return "@position-try " + name + " {\n  " + style.build() + "\n}";
    }

    // ==================== Position Visibility ====================

    /**
     * Creates a position-visibility declaration.
     *
     * @param value "always", "anchors-visible", or "no-overflow"
     * @return a {@code Style} holding {@code position-visibility: value}
     */
    public static jweb.Style<?> positionVisibility(String value) {
        return CSS.style().prop("position-visibility", value);
    }

    /**
     * Creates a position-visibility declaration from a {@code CSSValue}.
     *
     * @param value the position-visibility value
     * @return a {@code Style} holding {@code position-visibility: value}
     */
    public static jweb.Style<?> positionVisibility(jweb.CSSValue value) {
        return positionVisibility(value.css());
    }

    // ==================== Convenience Methods ====================

    /**
     * Positions an element above its anchor.
     *
     * @return the position-area declaration for top positioning
     */
    public static jweb.Style<?> positionAbove() {
        return positionArea("top");
    }

    /**
     * Positions an element below its anchor.
     *
     * @return the position-area declaration for bottom positioning
     */
    public static jweb.Style<?> positionBelow() {
        return positionArea("bottom");
    }

    /**
     * Positions an element to the left of its anchor.
     *
     * @return the position-area declaration for left positioning
     */
    public static jweb.Style<?> positionLeft() {
        return positionArea("left");
    }

    /**
     * Positions an element to the right of its anchor.
     *
     * @return the position-area declaration for right positioning
     */
    public static jweb.Style<?> positionRight() {
        return positionArea("right");
    }
}
