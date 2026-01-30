package com.osmig.Jweb.framework.styles;

/**
 * CSS Masking and Clipping DSL for applying masks and clip paths to elements.
 *
 * <p>CSS Masking provides two main approaches: mask-image (alpha/luminance masking)
 * and clip-path (vector-based clipping). Both can create complex visual effects.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSMasking.*;
 *
 * // Gradient mask (fade out bottom)
 * rule(".fade-bottom")
 *     .prop(maskImage("linear-gradient(black 60%, transparent)"))
 *
 * // Image mask
 * rule(".masked")
 *     .prop(maskImage("url('mask.svg')"))
 *     .prop(maskSize("cover"))
 *     .prop(maskRepeat("no-repeat"))
 *
 * // Clip path - circle
 * rule(".circle")
 *     .prop(clipCircle("50%"))
 *
 * // Clip path - polygon (triangle)
 * rule(".triangle")
 *     .prop(clipPolygon("50% 0%", "0% 100%", "100% 100%"))
 *
 * // Clip path - inset (rounded rectangle)
 * rule(".rounded-clip")
 *     .prop(clipInset("10px", "8px"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public final class CSSMasking {

    private CSSMasking() {}

    // ==================== mask-image ====================

    /**
     * Creates a mask-image property.
     *
     * @param value the mask image (e.g., "url('mask.svg')", "linear-gradient(...)")
     * @return the property string (with -webkit- prefix for compatibility)
     */
    public static String maskImage(String value) {
        return "-webkit-mask-image:" + value + ";mask-image:" + value;
    }

    /** No mask image. */
    public static String maskImageNone() {
        return "-webkit-mask-image:none;mask-image:none";
    }

    // ==================== mask-mode ====================

    /**
     * Creates a mask-mode property.
     *
     * @param value "alpha", "luminance", or "match-source"
     * @return the property string
     */
    public static String maskMode(String value) {
        return "-webkit-mask-mode:" + value + ";mask-mode:" + value;
    }

    /** Uses alpha channel for masking. */
    public static String maskModeAlpha() {
        return maskMode("alpha");
    }

    /** Uses luminance for masking. */
    public static String maskModeLuminance() {
        return maskMode("luminance");
    }

    // ==================== mask-position ====================

    /**
     * Creates a mask-position property.
     *
     * @param value the position (e.g., "center", "top right", "50% 50%")
     * @return the property string
     */
    public static String maskPosition(String value) {
        return "-webkit-mask-position:" + value + ";mask-position:" + value;
    }

    // ==================== mask-size ====================

    /**
     * Creates a mask-size property.
     *
     * @param value the size (e.g., "cover", "contain", "100px 200px", "50%")
     * @return the property string
     */
    public static String maskSize(String value) {
        return "-webkit-mask-size:" + value + ";mask-size:" + value;
    }

    // ==================== mask-repeat ====================

    /**
     * Creates a mask-repeat property.
     *
     * @param value "repeat", "no-repeat", "repeat-x", "repeat-y", "space", "round"
     * @return the property string
     */
    public static String maskRepeat(String value) {
        return "-webkit-mask-repeat:" + value + ";mask-repeat:" + value;
    }

    // ==================== mask-origin ====================

    /**
     * Creates a mask-origin property.
     *
     * @param value "border-box", "padding-box", "content-box", "fill-box", "stroke-box", "view-box"
     * @return the property string
     */
    public static String maskOrigin(String value) {
        return "-webkit-mask-origin:" + value + ";mask-origin:" + value;
    }

    // ==================== mask-clip ====================

    /**
     * Creates a mask-clip property.
     *
     * @param value "border-box", "padding-box", "content-box", "fill-box", "stroke-box", "view-box", "no-clip"
     * @return the property string
     */
    public static String maskClip(String value) {
        return "-webkit-mask-clip:" + value + ";mask-clip:" + value;
    }

    // ==================== mask-composite ====================

    /**
     * Creates a mask-composite property.
     *
     * @param value "add", "subtract", "intersect", "exclude"
     * @return the property string
     */
    public static String maskComposite(String value) {
        return "-webkit-mask-composite:" + value + ";mask-composite:" + value;
    }

    // ==================== clip-path ====================

    /**
     * Creates a clip-path property.
     *
     * @param value the clip path value
     * @return the property string
     */
    public static String clipPath(String value) {
        return "clip-path:" + value;
    }

    /** No clip path. */
    public static String clipPathNone() {
        return "clip-path:none";
    }

    // ==================== clip-path Shapes ====================

    /**
     * Creates a circle clip path.
     *
     * @param radius the circle radius (e.g., "50%", "100px")
     * @return the property string
     */
    public static String clipCircle(String radius) {
        return "clip-path:circle(" + radius + ")";
    }

    /**
     * Creates a circle clip path with position.
     *
     * @param radius the circle radius
     * @param position the center position (e.g., "at 50% 50%", "at center")
     * @return the property string
     */
    public static String clipCircle(String radius, String position) {
        return "clip-path:circle(" + radius + " at " + position + ")";
    }

    /**
     * Creates an ellipse clip path.
     *
     * @param rx horizontal radius
     * @param ry vertical radius
     * @return the property string
     */
    public static String clipEllipse(String rx, String ry) {
        return "clip-path:ellipse(" + rx + " " + ry + ")";
    }

    /**
     * Creates an ellipse clip path with position.
     *
     * @param rx horizontal radius
     * @param ry vertical radius
     * @param position center position
     * @return the property string
     */
    public static String clipEllipse(String rx, String ry, String position) {
        return "clip-path:ellipse(" + rx + " " + ry + " at " + position + ")";
    }

    /**
     * Creates an inset clip path (rounded rectangle).
     *
     * @param inset the inset values (e.g., "10px", "10px 20px", "5% 10% 15% 20%")
     * @return the property string
     */
    public static String clipInset(String inset) {
        return "clip-path:inset(" + inset + ")";
    }

    /**
     * Creates an inset clip path with border-radius.
     *
     * @param inset the inset values
     * @param borderRadius the border radius
     * @return the property string
     */
    public static String clipInset(String inset, String borderRadius) {
        return "clip-path:inset(" + inset + " round " + borderRadius + ")";
    }

    /**
     * Creates a polygon clip path.
     *
     * @param points the polygon points (e.g., "50% 0%", "0% 100%", "100% 100%")
     * @return the property string
     */
    public static String clipPolygon(String... points) {
        return "clip-path:polygon(" + String.join(",", points) + ")";
    }

    /**
     * Creates a path() clip path using SVG path data.
     *
     * @param svgPath the SVG path data string
     * @return the property string
     */
    public static String clipSvgPath(String svgPath) {
        return "clip-path:path('" + svgPath + "')";
    }

    /**
     * Creates a clip-path referencing an SVG clipPath element.
     *
     * @param url the URL to the SVG clipPath (e.g., "#myClip", "clip.svg#myClip")
     * @return the property string
     */
    public static String clipUrl(String url) {
        return "clip-path:url(" + url + ")";
    }

    // ==================== Common Shapes ====================

    /** Clips to a triangle pointing up. */
    public static String clipTriangleUp() {
        return clipPolygon("50% 0%", "0% 100%", "100% 100%");
    }

    /** Clips to a triangle pointing down. */
    public static String clipTriangleDown() {
        return clipPolygon("0% 0%", "100% 0%", "50% 100%");
    }

    /** Clips to a triangle pointing left. */
    public static String clipTriangleLeft() {
        return clipPolygon("100% 0%", "0% 50%", "100% 100%");
    }

    /** Clips to a triangle pointing right. */
    public static String clipTriangleRight() {
        return clipPolygon("0% 0%", "100% 50%", "0% 100%");
    }

    /** Clips to a diamond shape. */
    public static String clipDiamond() {
        return clipPolygon("50% 0%", "100% 50%", "50% 100%", "0% 50%");
    }

    /** Clips to a pentagon. */
    public static String clipPentagon() {
        return clipPolygon("50% 0%", "100% 38%", "82% 100%", "18% 100%", "0% 38%");
    }

    /** Clips to a hexagon. */
    public static String clipHexagon() {
        return clipPolygon("25% 0%", "75% 0%", "100% 50%", "75% 100%", "25% 100%", "0% 50%");
    }

    /** Clips to a star shape. */
    public static String clipStar() {
        return clipPolygon(
            "50% 0%", "61% 35%", "98% 35%", "68% 57%",
            "79% 91%", "50% 70%", "21% 91%", "32% 57%",
            "2% 35%", "39% 35%"
        );
    }
}
