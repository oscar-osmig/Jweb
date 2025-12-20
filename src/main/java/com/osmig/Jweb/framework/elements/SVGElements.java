package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;

/**
 * SVG element factory methods for type-safe SVG creation.
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.elements.SVGElements.*;
 *
 * // Simple icon
 * svg(viewBox("0 0 24 24"), width(24), height(24),
 *     path(d("M12 2L2 7l10 5 10-5-10-5z"))
 * )
 *
 * // Circle with styling
 * svg(viewBox("0 0 100 100"),
 *     circle(cx(50), cy(50), r(40), fill("blue"), stroke("black"))
 * )
 *
 * // Complex shape
 * svg(viewBox("0 0 200 200"),
 *     g(transform("translate(100, 100)"),
 *         rect(x(-50), y(-50), width(100), height(100), fill("red")),
 *         text(textAnchor("middle"), "Hello")
 *     )
 * )
 * </pre>
 *
 * @see Elements for HTML elements
 */
public final class SVGElements {

    private SVGElements() {}

    // ==================== Container Elements ====================

    /**
     * Creates an SVG root element.
     *
     * <p>Example:</p>
     * <pre>
     * svg(viewBox("0 0 100 100"), width(100), height(100),
     *     circle(cx(50), cy(50), r(40))
     * )
     * </pre>
     */
    public static Tag svg(Object... items) {
        return Tag.create("svg", items).attr("xmlns", "http://www.w3.org/2000/svg");
    }

    /**
     * Creates a group element for grouping related elements.
     */
    public static Tag g(Object... items) {
        return Tag.create("g", items);
    }

    /**
     * Creates a definitions element for reusable elements.
     */
    public static Tag defs(Object... items) {
        return Tag.create("defs", items);
    }

    /**
     * Creates a symbol element for defining reusable graphics.
     */
    public static Tag symbol(Object... items) {
        return Tag.create("symbol", items);
    }

    /**
     * Creates a use element to reference another element.
     */
    public static Tag use(Object... items) {
        return Tag.create("use", items);
    }

    // ==================== Shape Elements ====================

    /**
     * Creates a path element - the most versatile SVG shape.
     *
     * <p>Example:</p>
     * <pre>
     * path(d("M10 10 L90 10 L90 90 L10 90 Z"), fill("none"), stroke("black"))
     * </pre>
     */
    public static Tag path(Object... items) {
        return Tag.create("path", items);
    }

    /**
     * Creates a rectangle element.
     *
     * <p>Example:</p>
     * <pre>
     * rect(x(10), y(10), width(80), height(60), fill("blue"))
     * </pre>
     */
    public static Tag rect(Object... items) {
        return Tag.create("rect", items);
    }

    /**
     * Creates a circle element.
     *
     * <p>Example:</p>
     * <pre>
     * circle(cx(50), cy(50), r(40), fill("red"))
     * </pre>
     */
    public static Tag circle(Object... items) {
        return Tag.create("circle", items);
    }

    /**
     * Creates an ellipse element.
     *
     * <p>Example:</p>
     * <pre>
     * ellipse(cx(50), cy(50), rx(40), ry(20), fill("green"))
     * </pre>
     */
    public static Tag ellipse(Object... items) {
        return Tag.create("ellipse", items);
    }

    /**
     * Creates a line element.
     *
     * <p>Example:</p>
     * <pre>
     * line(x1(10), y1(10), x2(90), y2(90), stroke("black"))
     * </pre>
     */
    public static Tag line(Object... items) {
        return Tag.create("line", items);
    }

    /**
     * Creates a polyline element (connected lines without closing).
     *
     * <p>Example:</p>
     * <pre>
     * polyline(points("10,10 50,90 90,10"), fill("none"), stroke("blue"))
     * </pre>
     */
    public static Tag polyline(Object... items) {
        return Tag.create("polyline", items);
    }

    /**
     * Creates a polygon element (closed shape).
     *
     * <p>Example:</p>
     * <pre>
     * polygon(points("50,10 90,90 10,90"), fill("yellow"))
     * </pre>
     */
    public static Tag polygon(Object... items) {
        return Tag.create("polygon", items);
    }

    // ==================== Text Elements ====================

    /**
     * Creates a text element.
     *
     * <p>Example:</p>
     * <pre>
     * text(x(50), y(50), textAnchor("middle"), "Hello World")
     * </pre>
     */
    public static Tag text(Object... items) {
        return Tag.create("text", items);
    }

    /**
     * Creates a tspan element for text positioning within text.
     */
    public static Tag tspan(Object... items) {
        return Tag.create("tspan", items);
    }

    /**
     * Creates a textPath element for text on a path.
     */
    public static Tag textPath(Object... items) {
        return Tag.create("textPath", items);
    }

    // ==================== Gradient & Pattern Elements ====================

    /**
     * Creates a linearGradient element.
     *
     * <p>Example:</p>
     * <pre>
     * linearGradient(id("gradient1"), x1("0%"), y1("0%"), x2("100%"), y2("0%"),
     *     stop(offset("0%"), stopColor("red")),
     *     stop(offset("100%"), stopColor("blue"))
     * )
     * </pre>
     */
    public static Tag linearGradient(Object... items) {
        return Tag.create("linearGradient", items);
    }

    /**
     * Creates a radialGradient element.
     */
    public static Tag radialGradient(Object... items) {
        return Tag.create("radialGradient", items);
    }

    /**
     * Creates a stop element for gradients.
     */
    public static Tag stop(Object... items) {
        return Tag.create("stop", items);
    }

    /**
     * Creates a pattern element.
     */
    public static Tag pattern(Object... items) {
        return Tag.create("pattern", items);
    }

    // ==================== Filter Elements ====================

    /**
     * Creates a filter element.
     */
    public static Tag filter(Object... items) {
        return Tag.create("filter", items);
    }

    /**
     * Creates a feGaussianBlur filter primitive.
     */
    public static Tag feGaussianBlur(Object... items) {
        return Tag.create("feGaussianBlur", items);
    }

    /**
     * Creates a feDropShadow filter primitive.
     */
    public static Tag feDropShadow(Object... items) {
        return Tag.create("feDropShadow", items);
    }

    /**
     * Creates a feColorMatrix filter primitive.
     */
    public static Tag feColorMatrix(Object... items) {
        return Tag.create("feColorMatrix", items);
    }

    // ==================== Clip & Mask Elements ====================

    /**
     * Creates a clipPath element.
     */
    public static Tag clipPath(Object... items) {
        return Tag.create("clipPath", items);
    }

    /**
     * Creates a mask element.
     */
    public static Tag mask(Object... items) {
        return Tag.create("mask", items);
    }

    // ==================== Image Element ====================

    /**
     * Creates an image element for embedding raster images.
     */
    public static Tag image(Object... items) {
        return Tag.create("image", items);
    }

    // ==================== Animation Elements ====================

    /**
     * Creates an animate element for SVG animation.
     */
    public static Tag animate(Object... items) {
        return Tag.create("animate", items);
    }

    /**
     * Creates an animateTransform element.
     */
    public static Tag animateTransform(Object... items) {
        return Tag.create("animateTransform", items);
    }

    /**
     * Creates an animateMotion element.
     */
    public static Tag animateMotion(Object... items) {
        return Tag.create("animateMotion", items);
    }

    // ==================== Common Attributes ====================

    /** Path data attribute: d="M0 0 L10 10" */
    public static Attr d(String pathData) {
        return new Attr("d", pathData);
    }

    /** ViewBox attribute: viewBox="0 0 100 100" */
    public static Attr viewBox(String value) {
        return new Attr("viewBox", value);
    }

    /** ViewBox attribute with numeric values */
    public static Attr viewBox(int minX, int minY, int width, int height) {
        return new Attr("viewBox", minX + " " + minY + " " + width + " " + height);
    }

    /** Width attribute for SVG elements */
    public static Attr width(int value) {
        return new Attr("width", String.valueOf(value));
    }

    /** Width attribute with string value */
    public static Attr width(String value) {
        return new Attr("width", value);
    }

    /** Height attribute for SVG elements */
    public static Attr height(int value) {
        return new Attr("height", String.valueOf(value));
    }

    /** Height attribute with string value */
    public static Attr height(String value) {
        return new Attr("height", value);
    }

    // ==================== Position Attributes ====================

    /** X coordinate */
    public static Attr x(int value) {
        return new Attr("x", String.valueOf(value));
    }

    /** X coordinate with string value */
    public static Attr x(String value) {
        return new Attr("x", value);
    }

    /** Y coordinate */
    public static Attr y(int value) {
        return new Attr("y", String.valueOf(value));
    }

    /** Y coordinate with string value */
    public static Attr y(String value) {
        return new Attr("y", value);
    }

    /** Circle/ellipse center X */
    public static Attr cx(int value) {
        return new Attr("cx", String.valueOf(value));
    }

    /** Circle/ellipse center X with string value */
    public static Attr cx(String value) {
        return new Attr("cx", value);
    }

    /** Circle/ellipse center Y */
    public static Attr cy(int value) {
        return new Attr("cy", String.valueOf(value));
    }

    /** Circle/ellipse center Y with string value */
    public static Attr cy(String value) {
        return new Attr("cy", value);
    }

    /** Circle radius */
    public static Attr r(int value) {
        return new Attr("r", String.valueOf(value));
    }

    /** Circle radius with string value */
    public static Attr r(String value) {
        return new Attr("r", value);
    }

    /** Ellipse X radius */
    public static Attr rx(int value) {
        return new Attr("rx", String.valueOf(value));
    }

    /** Ellipse Y radius */
    public static Attr ry(int value) {
        return new Attr("ry", String.valueOf(value));
    }

    /** Line start X */
    public static Attr x1(int value) {
        return new Attr("x1", String.valueOf(value));
    }

    /** Line start X with string value */
    public static Attr x1(String value) {
        return new Attr("x1", value);
    }

    /** Line start Y */
    public static Attr y1(int value) {
        return new Attr("y1", String.valueOf(value));
    }

    /** Line start Y with string value */
    public static Attr y1(String value) {
        return new Attr("y1", value);
    }

    /** Line end X */
    public static Attr x2(int value) {
        return new Attr("x2", String.valueOf(value));
    }

    /** Line end X with string value */
    public static Attr x2(String value) {
        return new Attr("x2", value);
    }

    /** Line end Y */
    public static Attr y2(int value) {
        return new Attr("y2", String.valueOf(value));
    }

    /** Line end Y with string value */
    public static Attr y2(String value) {
        return new Attr("y2", value);
    }

    // ==================== Styling Attributes ====================

    /** Fill color */
    public static Attr fill(String color) {
        return new Attr("fill", color);
    }

    /** Stroke color */
    public static Attr stroke(String color) {
        return new Attr("stroke", color);
    }

    /** Stroke width */
    public static Attr strokeWidth(int value) {
        return new Attr("stroke-width", String.valueOf(value));
    }

    /** Stroke width with string value */
    public static Attr strokeWidth(String value) {
        return new Attr("stroke-width", value);
    }

    /** Stroke line cap */
    public static Attr strokeLinecap(String value) {
        return new Attr("stroke-linecap", value);
    }

    /** Stroke line join */
    public static Attr strokeLinejoin(String value) {
        return new Attr("stroke-linejoin", value);
    }

    /** Stroke dash array */
    public static Attr strokeDasharray(String value) {
        return new Attr("stroke-dasharray", value);
    }

    /** Stroke dash offset */
    public static Attr strokeDashoffset(String value) {
        return new Attr("stroke-dashoffset", value);
    }

    /** Opacity */
    public static Attr opacity(double value) {
        return new Attr("opacity", String.valueOf(value));
    }

    /** Fill opacity */
    public static Attr fillOpacity(double value) {
        return new Attr("fill-opacity", String.valueOf(value));
    }

    /** Stroke opacity */
    public static Attr strokeOpacity(double value) {
        return new Attr("stroke-opacity", String.valueOf(value));
    }

    /** Fill rule */
    public static Attr fillRule(String value) {
        return new Attr("fill-rule", value);
    }

    // ==================== Transform Attribute ====================

    /** Transform attribute */
    public static Attr transform(String value) {
        return new Attr("transform", value);
    }

    /** Translate transform */
    public static String translate(int x, int y) {
        return "translate(" + x + ", " + y + ")";
    }

    /** Rotate transform */
    public static String rotate(int degrees) {
        return "rotate(" + degrees + ")";
    }

    /** Rotate transform around point */
    public static String rotate(int degrees, int cx, int cy) {
        return "rotate(" + degrees + ", " + cx + ", " + cy + ")";
    }

    /** Scale transform */
    public static String scale(double factor) {
        return "scale(" + factor + ")";
    }

    /** Scale transform with separate X and Y */
    public static String scale(double x, double y) {
        return "scale(" + x + ", " + y + ")";
    }

    /** Skew X transform */
    public static String skewX(int degrees) {
        return "skewX(" + degrees + ")";
    }

    /** Skew Y transform */
    public static String skewY(int degrees) {
        return "skewY(" + degrees + ")";
    }

    // ==================== Text Attributes ====================

    /** Text anchor */
    public static Attr textAnchor(String value) {
        return new Attr("text-anchor", value);
    }

    /** Dominant baseline */
    public static Attr dominantBaseline(String value) {
        return new Attr("dominant-baseline", value);
    }

    /** Font size */
    public static Attr fontSize(int value) {
        return new Attr("font-size", String.valueOf(value));
    }

    /** Font size with string value */
    public static Attr fontSize(String value) {
        return new Attr("font-size", value);
    }

    /** Font family */
    public static Attr fontFamily(String value) {
        return new Attr("font-family", value);
    }

    /** Font weight */
    public static Attr fontWeight(String value) {
        return new Attr("font-weight", value);
    }

    // ==================== Other Attributes ====================

    /** Points for polyline/polygon */
    public static Attr points(String value) {
        return new Attr("points", value);
    }

    /** Preserve aspect ratio */
    public static Attr preserveAspectRatio(String value) {
        return new Attr("preserveAspectRatio", value);
    }

    /** Gradient offset (for stops) */
    public static Attr offset(String value) {
        return new Attr("offset", value);
    }

    /** Stop color (for gradient stops) */
    public static Attr stopColor(String value) {
        return new Attr("stop-color", value);
    }

    /** Stop opacity (for gradient stops) */
    public static Attr stopOpacity(double value) {
        return new Attr("stop-opacity", String.valueOf(value));
    }

    /** Href attribute for use/image elements */
    public static Attr href(String value) {
        return new Attr("href", value);
    }

    /** XLink href (for compatibility) */
    public static Attr xlinkHref(String value) {
        return new Attr("xlink:href", value);
    }

    /** Clip path reference */
    public static Attr clipPathRef(String id) {
        return new Attr("clip-path", "url(#" + id + ")");
    }

    /** Mask reference */
    public static Attr maskRef(String id) {
        return new Attr("mask", "url(#" + id + ")");
    }

    /** Filter reference */
    public static Attr filterRef(String id) {
        return new Attr("filter", "url(#" + id + ")");
    }

    /** Standard deviation for blur filter */
    public static Attr stdDeviation(double value) {
        return new Attr("stdDeviation", String.valueOf(value));
    }

    /** Path length for animation control */
    public static Attr pathLength(int value) {
        return new Attr("pathLength", String.valueOf(value));
    }

    // ==================== Animation Attributes ====================

    /** Animation attribute name */
    public static Attr attributeName(String value) {
        return new Attr("attributeName", value);
    }

    /** Animation from value */
    public static Attr from(String value) {
        return new Attr("from", value);
    }

    /** Animation to value */
    public static Attr to(String value) {
        return new Attr("to", value);
    }

    /** Animation duration */
    public static Attr dur(String value) {
        return new Attr("dur", value);
    }

    /** Animation repeat count */
    public static Attr repeatCount(String value) {
        return new Attr("repeatCount", value);
    }

    /** Animation begin time */
    public static Attr begin(String value) {
        return new Attr("begin", value);
    }

    /** Animation type (for animateTransform) */
    public static Attr type(String value) {
        return new Attr("type", value);
    }

    /** Animation values */
    public static Attr values(String value) {
        return new Attr("values", value);
    }

    /** Key times for animation */
    public static Attr keyTimes(String value) {
        return new Attr("keyTimes", value);
    }

    /** Calc mode for animation */
    public static Attr calcMode(String value) {
        return new Attr("calcMode", value);
    }
}
