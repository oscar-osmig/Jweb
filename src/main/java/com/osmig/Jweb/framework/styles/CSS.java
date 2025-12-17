package com.osmig.Jweb.framework.styles;

/**
 * CSS keyword constants for property values.
 *
 * Usage with static import:
 *   import static com.osmig.Jweb.framework.styles.CSS.*;
 *
 *   style().display(flex)
 *          .justifyContent(center)
 *          .border(px(1), solid, black)
 */
public final class CSS {

    private CSS() {}

    // Display values
    public static final CSSValue block = () -> "block";
    public static final CSSValue inline = () -> "inline";
    public static final CSSValue inlineBlock = () -> "inline-block";
    public static final CSSValue flex = () -> "flex";
    public static final CSSValue inlineFlex = () -> "inline-flex";
    public static final CSSValue grid = () -> "grid";
    public static final CSSValue inlineGrid = () -> "inline-grid";
    public static final CSSValue contents = () -> "contents";
    public static final CSSValue flowRoot = () -> "flow-root";

    // Position values
    public static final CSSValue relative = () -> "relative";
    public static final CSSValue absolute = () -> "absolute";
    public static final CSSValue fixed = () -> "fixed";
    public static final CSSValue sticky = () -> "sticky";
    public static final CSSValue static_ = () -> "static";

    // Flexbox direction
    public static final CSSValue row = () -> "row";
    public static final CSSValue rowReverse = () -> "row-reverse";
    public static final CSSValue column = () -> "column";
    public static final CSSValue columnReverse = () -> "column-reverse";

    // Flexbox wrap
    public static final CSSValue wrap = () -> "wrap";
    public static final CSSValue nowrap = () -> "nowrap";
    public static final CSSValue wrapReverse = () -> "wrap-reverse";

    // Alignment
    public static final CSSValue center = () -> "center";
    public static final CSSValue flexStart = () -> "flex-start";
    public static final CSSValue flexEnd = () -> "flex-end";
    public static final CSSValue start = () -> "start";
    public static final CSSValue end = () -> "end";
    public static final CSSValue spaceBetween = () -> "space-between";
    public static final CSSValue spaceAround = () -> "space-around";
    public static final CSSValue spaceEvenly = () -> "space-evenly";
    public static final CSSValue stretch = () -> "stretch";
    public static final CSSValue baseline = () -> "baseline";

    // Border styles
    public static final CSSValue solid = () -> "solid";
    public static final CSSValue dashed = () -> "dashed";
    public static final CSSValue dotted = () -> "dotted";
    public static final CSSValue double_ = () -> "double";
    public static final CSSValue groove = () -> "groove";
    public static final CSSValue ridge = () -> "ridge";
    public static final CSSValue inset = () -> "inset";
    public static final CSSValue outset = () -> "outset";
    public static final CSSValue hidden = () -> "hidden";

    // Text alignment
    public static final CSSValue left = () -> "left";
    public static final CSSValue right = () -> "right";
    public static final CSSValue justify = () -> "justify";

    // Text decoration
    public static final CSSValue underline = () -> "underline";
    public static final CSSValue overline = () -> "overline";
    public static final CSSValue lineThrough = () -> "line-through";

    // Text transform
    public static final CSSValue uppercase = () -> "uppercase";
    public static final CSSValue lowercase = () -> "lowercase";
    public static final CSSValue capitalize = () -> "capitalize";

    // Font weight
    public static final CSSValue normal = () -> "normal";
    public static final CSSValue bold = () -> "bold";
    public static final CSSValue bolder = () -> "bolder";
    public static final CSSValue lighter = () -> "lighter";

    // Font style
    public static final CSSValue italic = () -> "italic";
    public static final CSSValue oblique = () -> "oblique";

    // Overflow values
    public static final CSSValue visible = () -> "visible";
    public static final CSSValue scroll = () -> "scroll";
    public static final CSSValue clip = () -> "clip";

    // Cursor values
    public static final CSSValue pointer = () -> "pointer";
    public static final CSSValue crosshair = () -> "crosshair";
    public static final CSSValue move = () -> "move";
    public static final CSSValue text = () -> "text";
    public static final CSSValue wait = () -> "wait";
    public static final CSSValue help = () -> "help";
    public static final CSSValue notAllowed = () -> "not-allowed";
    public static final CSSValue grab = () -> "grab";
    public static final CSSValue grabbing = () -> "grabbing";

    // Visibility
    public static final CSSValue collapse = () -> "collapse";

    // White space
    public static final CSSValue pre = () -> "pre";
    public static final CSSValue preWrap = () -> "pre-wrap";
    public static final CSSValue preLine = () -> "pre-line";
    public static final CSSValue breakSpaces = () -> "break-spaces";

    // Word break
    public static final CSSValue breakAll = () -> "break-all";
    public static final CSSValue keepAll = () -> "keep-all";
    public static final CSSValue breakWord = () -> "break-word";

    // Object fit
    public static final CSSValue fill = () -> "fill";
    public static final CSSValue contain = () -> "contain";
    public static final CSSValue cover = () -> "cover";
    public static final CSSValue scaleDown = () -> "scale-down";

    // Background size
    public static final CSSValue bgContain = () -> "contain";
    public static final CSSValue bgCover = () -> "cover";

    // Background repeat
    public static final CSSValue repeat = () -> "repeat";
    public static final CSSValue repeatX = () -> "repeat-x";
    public static final CSSValue repeatY = () -> "repeat-y";
    public static final CSSValue noRepeat = () -> "no-repeat";

    // Background attachment
    public static final CSSValue local = () -> "local";

    // List style
    public static final CSSValue disc = () -> "disc";
    public static final CSSValue circle = () -> "circle";
    public static final CSSValue square = () -> "square";
    public static final CSSValue decimal = () -> "decimal";
    public static final CSSValue lowerAlpha = () -> "lower-alpha";
    public static final CSSValue upperAlpha = () -> "upper-alpha";
    public static final CSSValue lowerRoman = () -> "lower-roman";
    public static final CSSValue upperRoman = () -> "upper-roman";

    // Transition timing functions
    public static final CSSValue ease = () -> "ease";
    public static final CSSValue easeIn = () -> "ease-in";
    public static final CSSValue easeOut = () -> "ease-out";
    public static final CSSValue easeInOut = () -> "ease-in-out";
    public static final CSSValue linear = () -> "linear";

    // Box sizing
    public static final CSSValue borderBox = () -> "border-box";
    public static final CSSValue contentBox = () -> "content-box";

    // Resize
    public static final CSSValue both = () -> "both";
    public static final CSSValue horizontal = () -> "horizontal";
    public static final CSSValue vertical = () -> "vertical";

    // Pointer events
    public static final CSSValue all = () -> "all";

    // User select
    public static final CSSValue selectNone = () -> "none";
    public static final CSSValue selectText = () -> "text";
    public static final CSSValue selectAll = () -> "all";

    // CSS Variable reference
    public static CSSValue var(String name) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ")";
    }

    public static CSSValue var(String name, CSSValue fallback) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ", " + fallback.css() + ")";
    }

    // URL function (for backgrounds)
    public static CSSValue url(String path) {
        return () -> "url('" + path + "')";
    }

    // Custom cubic-bezier
    public static CSSValue cubicBezier(double x1, double y1, double x2, double y2) {
        return () -> "cubic-bezier(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
    }

    // Transform functions
    public static CSSValue translate(CSSValue x, CSSValue y) {
        return () -> "translate(" + x.css() + ", " + y.css() + ")";
    }

    public static CSSValue translateX(CSSValue x) {
        return () -> "translateX(" + x.css() + ")";
    }

    public static CSSValue translateY(CSSValue y) {
        return () -> "translateY(" + y.css() + ")";
    }

    public static CSSValue scale(double value) {
        return () -> "scale(" + value + ")";
    }

    public static CSSValue scale(double x, double y) {
        return () -> "scale(" + x + ", " + y + ")";
    }

    public static CSSValue scaleX(double x) {
        return () -> "scaleX(" + x + ")";
    }

    public static CSSValue scaleY(double y) {
        return () -> "scaleY(" + y + ")";
    }

    public static CSSValue rotate(CSSValue angle) {
        return () -> "rotate(" + angle.css() + ")";
    }

    public static CSSValue skew(CSSValue x, CSSValue y) {
        return () -> "skew(" + x.css() + ", " + y.css() + ")";
    }

    public static CSSValue skewX(CSSValue x) {
        return () -> "skewX(" + x.css() + ")";
    }

    public static CSSValue skewY(CSSValue y) {
        return () -> "skewY(" + y.css() + ")";
    }

    // Gradient functions
    public static CSSValue linearGradient(CSSValue... stops) {
        return () -> "linear-gradient(" + joinCss(stops) + ")";
    }

    public static CSSValue linearGradient(String direction, CSSValue... stops) {
        return () -> "linear-gradient(" + direction + ", " + joinCss(stops) + ")";
    }

    public static CSSValue radialGradient(CSSValue... stops) {
        return () -> "radial-gradient(" + joinCss(stops) + ")";
    }

    public static CSSValue radialGradient(String shape, CSSValue... stops) {
        return () -> "radial-gradient(" + shape + ", " + joinCss(stops) + ")";
    }

    public static CSSValue conicGradient(CSSValue... stops) {
        return () -> "conic-gradient(" + joinCss(stops) + ")";
    }

    public static CSSValue conicGradient(String from, CSSValue... stops) {
        return () -> "conic-gradient(" + from + ", " + joinCss(stops) + ")";
    }

    // ==================== Filter Functions ====================

    /**
     * Applies a Gaussian blur.
     * Example: blur(px(5)) -> blur(5px)
     */
    public static CSSValue blur(CSSValue radius) {
        return () -> "blur(" + radius.css() + ")";
    }

    /**
     * Adjusts brightness.
     * Values: 0 = black, 1 = normal, >1 = brighter
     * Example: brightness(1.5) -> brightness(1.5)
     */
    public static CSSValue brightness(double value) {
        return () -> "brightness(" + formatNumber(value) + ")";
    }

    /**
     * Adjusts contrast.
     * Values: 0 = gray, 1 = normal, >1 = more contrast
     * Example: contrast(1.2) -> contrast(1.2)
     */
    public static CSSValue contrast(double value) {
        return () -> "contrast(" + formatNumber(value) + ")";
    }

    /**
     * Applies a drop shadow.
     * Example: dropShadow(px(2), px(2), px(4), rgba(0,0,0,0.5))
     */
    public static CSSValue dropShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue color) {
        return () -> "drop-shadow(" + offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + color.css() + ")";
    }

    /**
     * Applies a drop shadow without blur.
     */
    public static CSSValue dropShadow(CSSValue offsetX, CSSValue offsetY, CSSValue color) {
        return () -> "drop-shadow(" + offsetX.css() + " " + offsetY.css() + " " + color.css() + ")";
    }

    /**
     * Converts to grayscale.
     * Values: 0 = normal, 1 = fully grayscale
     * Example: grayscale(1) -> grayscale(1)
     */
    public static CSSValue grayscale(double value) {
        return () -> "grayscale(" + formatNumber(value) + ")";
    }

    /**
     * Rotates the hue.
     * Example: hueRotate(deg(90)) -> hue-rotate(90deg)
     */
    public static CSSValue hueRotate(CSSValue angle) {
        return () -> "hue-rotate(" + angle.css() + ")";
    }

    /**
     * Inverts colors.
     * Values: 0 = normal, 1 = fully inverted
     * Example: invert(1) -> invert(1)
     */
    public static CSSValue invert(double value) {
        return () -> "invert(" + formatNumber(value) + ")";
    }

    /**
     * Adjusts opacity via filter.
     * Values: 0 = transparent, 1 = opaque
     * Example: opacity(0.5) -> opacity(0.5)
     */
    public static CSSValue filterOpacity(double value) {
        return () -> "opacity(" + formatNumber(value) + ")";
    }

    /**
     * Adjusts saturation.
     * Values: 0 = desaturated, 1 = normal, >1 = super-saturated
     * Example: saturate(2) -> saturate(2)
     */
    public static CSSValue saturate(double value) {
        return () -> "saturate(" + formatNumber(value) + ")";
    }

    /**
     * Applies sepia tone.
     * Values: 0 = normal, 1 = fully sepia
     * Example: sepia(0.8) -> sepia(0.8)
     */
    public static CSSValue sepia(double value) {
        return () -> "sepia(" + formatNumber(value) + ")";
    }

    // ==================== Clip Path Functions ====================

    /**
     * Creates a circle clip-path.
     * Example: circleClip(percent(50)) -> circle(50%)
     */
    public static CSSValue circleClip(CSSValue radius) {
        return () -> "circle(" + radius.css() + ")";
    }

    /**
     * Creates a circle clip-path at a position.
     * Example: circleClip(percent(50), "center") -> circle(50% at center)
     */
    public static CSSValue circleClip(CSSValue radius, String position) {
        return () -> "circle(" + radius.css() + " at " + position + ")";
    }

    /**
     * Creates an ellipse clip-path.
     * Example: ellipseClip(percent(50), percent(30)) -> ellipse(50% 30%)
     */
    public static CSSValue ellipseClip(CSSValue radiusX, CSSValue radiusY) {
        return () -> "ellipse(" + radiusX.css() + " " + radiusY.css() + ")";
    }

    /**
     * Creates a polygon clip-path.
     * Example: polygon("0 0", "100% 0", "100% 100%", "0 100%")
     */
    public static CSSValue polygon(String... points) {
        return () -> "polygon(" + String.join(", ", points) + ")";
    }

    /**
     * Creates an inset clip-path.
     * Example: insetClip(px(10)) -> inset(10px)
     */
    public static CSSValue insetClip(CSSValue all) {
        return () -> "inset(" + all.css() + ")";
    }

    /**
     * Creates an inset clip-path with rounded corners.
     */
    public static CSSValue insetClip(CSSValue all, CSSValue borderRadius) {
        return () -> "inset(" + all.css() + " round " + borderRadius.css() + ")";
    }

    // ==================== Content Functions ====================

    /**
     * Creates an attr() function for CSS content property.
     * Example: attrContent("data-label") -> attr(data-label)
     */
    public static CSSValue attrContent(String attributeName) {
        return () -> "attr(" + attributeName + ")";
    }

    /**
     * Creates a counter() function.
     * Example: counter("section") -> counter(section)
     */
    public static CSSValue counter(String name) {
        return () -> "counter(" + name + ")";
    }

    /**
     * Creates a counter() function with style.
     * Example: counter("section", "decimal") -> counter(section, decimal)
     */
    public static CSSValue counter(String name, String style) {
        return () -> "counter(" + name + ", " + style + ")";
    }

    // Helper method
    private static String joinCss(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }

    private static String formatNumber(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }
}
