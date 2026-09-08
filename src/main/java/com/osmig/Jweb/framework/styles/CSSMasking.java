package com.osmig.Jweb.framework.styles;

import jweb.CSSValue;
import jweb.Style;

/**
 * CSS Masking and Clipping DSL for applying masks and clip paths to elements.
 *
 * <p>CSS Masking provides two main approaches: mask-image (alpha/luminance masking)
 * and clip-path (vector-based clipping). Both can create complex visual effects.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Gradient mask (fade out bottom)
 * rule(".fade-bottom")
 *     .apply(maskImage("linear-gradient(black 60%, transparent)"))
 *
 * // Image mask
 * rule(".masked")
 *     .apply(maskImage("url('mask.svg')"))
 *     .apply(maskSize("cover"))
 *     .apply(maskRepeat("no-repeat"))
 *
 * // Clip path - circle
 * rule(".circle")
 *     .apply(clipCircle("50%"))
 *
 * // Clip path - polygon (triangle)
 * rule(".triangle")
 *     .apply(clipPolygon("50% 0%", "0% 100%", "100% 100%"))
 *
 * // Clip path - inset (rounded rectangle)
 * rule(".rounded-clip")
 *     .apply(clipInset("10px", "8px"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public class CSSMasking extends CSSLogicalProperties {

    protected CSSMasking() {}

    // ==================== mask-image ====================

    /**
     * Creates a mask-image declaration.
     *
     * @param value the mask image (e.g., "url('mask.svg')", "linear-gradient(...)")
     * @return a {@code Style} holding the declaration (with -webkit- prefix for compatibility)
     */
    public static Style<?> maskImage(String value) {
        return CSS.style().prop("-webkit-mask-image", value).prop("mask-image", value);
    }

    /** {@code CSSValue} overload of {@link #maskImage(String)}. */
    public static Style<?> maskImage(CSSValue value) {
        return maskImage(value.css());
    }

    /** No mask image. */
    public static Style<?> maskImageNone() {
        return maskImage("none");
    }

    // ==================== mask-mode ====================

    /**
     * Creates a mask-mode declaration.
     *
     * @param value "alpha", "luminance", or "match-source"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskMode(String value) {
        return CSS.style().prop("-webkit-mask-mode", value).prop("mask-mode", value);
    }

    /** {@code CSSValue} overload of {@link #maskMode(String)}. */
    public static Style<?> maskMode(CSSValue value) {
        return maskMode(value.css());
    }

    /** Uses alpha channel for masking. */
    public static Style<?> maskModeAlpha() {
        return maskMode("alpha");
    }

    /** Uses luminance for masking. */
    public static Style<?> maskModeLuminance() {
        return maskMode("luminance");
    }

    // ==================== mask-position ====================

    /**
     * Creates a mask-position declaration.
     *
     * @param value the position (e.g., "center", "top right", "50% 50%")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskPosition(String value) {
        return CSS.style().prop("-webkit-mask-position", value).prop("mask-position", value);
    }

    /** {@code CSSValue} overload of {@link #maskPosition(String)}. */
    public static Style<?> maskPosition(CSSValue value) {
        return maskPosition(value.css());
    }

    // ==================== mask-size ====================

    /**
     * Creates a mask-size declaration.
     *
     * @param value the size (e.g., "cover", "contain", "100px 200px", "50%")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskSize(String value) {
        return CSS.style().prop("-webkit-mask-size", value).prop("mask-size", value);
    }

    /** {@code CSSValue} overload of {@link #maskSize(String)}. */
    public static Style<?> maskSize(CSSValue value) {
        return maskSize(value.css());
    }

    // ==================== mask-repeat ====================

    /**
     * Creates a mask-repeat declaration.
     *
     * @param value "repeat", "no-repeat", "repeat-x", "repeat-y", "space", "round"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskRepeat(String value) {
        return CSS.style().prop("-webkit-mask-repeat", value).prop("mask-repeat", value);
    }

    /** {@code CSSValue} overload of {@link #maskRepeat(String)}. */
    public static Style<?> maskRepeat(CSSValue value) {
        return maskRepeat(value.css());
    }

    // ==================== mask-origin ====================

    /**
     * Creates a mask-origin declaration.
     *
     * @param value "border-box", "padding-box", "content-box", "fill-box", "stroke-box", "view-box"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskOrigin(String value) {
        return CSS.style().prop("-webkit-mask-origin", value).prop("mask-origin", value);
    }

    /** {@code CSSValue} overload of {@link #maskOrigin(String)}. */
    public static Style<?> maskOrigin(CSSValue value) {
        return maskOrigin(value.css());
    }

    // ==================== mask-clip ====================

    /**
     * Creates a mask-clip declaration.
     *
     * @param value "border-box", "padding-box", "content-box", "fill-box", "stroke-box", "view-box", "no-clip"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskClip(String value) {
        return CSS.style().prop("-webkit-mask-clip", value).prop("mask-clip", value);
    }

    /** {@code CSSValue} overload of {@link #maskClip(String)}. */
    public static Style<?> maskClip(CSSValue value) {
        return maskClip(value.css());
    }

    // ==================== mask-composite ====================

    /**
     * Creates a mask-composite declaration.
     *
     * @param value "add", "subtract", "intersect", "exclude"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> maskComposite(String value) {
        return CSS.style().prop("-webkit-mask-composite", value).prop("mask-composite", value);
    }

    /** {@code CSSValue} overload of {@link #maskComposite(String)}. */
    public static Style<?> maskComposite(CSSValue value) {
        return maskComposite(value.css());
    }

    // ==================== clip-path ====================

    /**
     * Creates a clip-path declaration.
     *
     * @param value the clip path value
     * @return a {@code Style} holding {@code clip-path: value}
     */
    public static Style<?> clipPath(String value) {
        return CSS.style().prop("clip-path", value);
    }

    /** {@code CSSValue} overload of {@link #clipPath(String)}. */
    public static Style<?> clipPath(CSSValue value) {
        return clipPath(value.css());
    }

    /** No clip path. */
    public static Style<?> clipPathNone() {
        return clipPath("none");
    }

    // ==================== clip-path Shapes ====================

    /**
     * Creates a circle clip path.
     *
     * @param radius the circle radius (e.g., "50%", "100px")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipCircle(String radius) {
        return clipPath("circle(" + radius + ")");
    }

    /**
     * Creates a circle clip path with position.
     *
     * @param radius the circle radius
     * @param position the center position (e.g., "at 50% 50%", "at center")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipCircle(String radius, String position) {
        return clipPath("circle(" + radius + " at " + position + ")");
    }

    /**
     * Creates an ellipse clip path.
     *
     * @param rx horizontal radius
     * @param ry vertical radius
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipEllipse(String rx, String ry) {
        return clipPath("ellipse(" + rx + " " + ry + ")");
    }

    /**
     * Creates an ellipse clip path with position.
     *
     * @param rx horizontal radius
     * @param ry vertical radius
     * @param position center position
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipEllipse(String rx, String ry, String position) {
        return clipPath("ellipse(" + rx + " " + ry + " at " + position + ")");
    }

    /**
     * Creates an inset clip path (rounded rectangle).
     *
     * @param inset the inset values (e.g., "10px", "10px 20px", "5% 10% 15% 20%")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipInset(String inset) {
        return clipPath("inset(" + inset + ")");
    }

    /**
     * Creates an inset clip path with border-radius.
     *
     * @param inset the inset values
     * @param borderRadius the border radius
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipInset(String inset, String borderRadius) {
        return clipPath("inset(" + inset + " round " + borderRadius + ")");
    }

    /**
     * Creates a polygon clip path.
     *
     * @param points the polygon points (e.g., "50% 0%", "0% 100%", "100% 100%")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipPolygon(String... points) {
        return clipPath("polygon(" + String.join(",", points) + ")");
    }

    /**
     * Creates a path() clip path using SVG path data.
     *
     * @param svgPath the SVG path data string
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipSvgPath(String svgPath) {
        return clipPath("path('" + svgPath + "')");
    }

    /**
     * Creates a clip-path referencing an SVG clipPath element.
     *
     * @param url the URL to the SVG clipPath (e.g., "#myClip", "clip.svg#myClip")
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> clipUrl(String url) {
        return clipPath("url(" + url + ")");
    }

    // ==================== Common Shapes ====================

    /** Clips to a triangle pointing up. */
    public static Style<?> clipTriangleUp() {
        return clipPolygon("50% 0%", "0% 100%", "100% 100%");
    }

    /** Clips to a triangle pointing down. */
    public static Style<?> clipTriangleDown() {
        return clipPolygon("0% 0%", "100% 0%", "50% 100%");
    }

    /** Clips to a triangle pointing left. */
    public static Style<?> clipTriangleLeft() {
        return clipPolygon("100% 0%", "0% 50%", "100% 100%");
    }

    /** Clips to a triangle pointing right. */
    public static Style<?> clipTriangleRight() {
        return clipPolygon("0% 0%", "100% 50%", "0% 100%");
    }

    /** Clips to a diamond shape. */
    public static Style<?> clipDiamond() {
        return clipPolygon("50% 0%", "100% 50%", "50% 100%", "0% 50%");
    }

    /** Clips to a pentagon. */
    public static Style<?> clipPentagon() {
        return clipPolygon("50% 0%", "100% 38%", "82% 100%", "18% 100%", "0% 38%");
    }

    /** Clips to a hexagon. */
    public static Style<?> clipHexagon() {
        return clipPolygon("25% 0%", "75% 0%", "100% 50%", "75% 100%", "25% 100%", "0% 50%");
    }

    /** Clips to a star shape. */
    public static Style<?> clipStar() {
        return clipPolygon(
            "50% 0%", "61% 35%", "98% 35%", "68% 57%",
            "79% 91%", "50% 70%", "21% 91%", "32% 57%",
            "2% 35%", "39% 35%"
        );
    }
}
