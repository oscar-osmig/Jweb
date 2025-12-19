package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unified CSS DSL for inline styles and stylesheet rules.
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 * // Inline style on element
 * div().style(style().padding(px(10)).color(red))
 *
 * // CSS rule for stylesheet
 * rule(".btn")
 *     .padding(px(10))
 *     .backgroundColor(blue)
 *
 * // Multiple rules combined
 * String css = styles(
 *     rule("*").boxSizing(borderBox),
 *     rule("body").margin(zero).fontFamily("system-ui"),
 *     rule(".btn").padding(px(10)).color(white)
 * );
 * </pre>
 */
public final class CSS {

    private CSS() {}

    // ==================== Entry Points ====================

    /**
     * Create an inline style builder.
     */
    public static StyleBuilder style() {
        return new StyleBuilder(null);
    }

    /**
     * Create a CSS rule for a selector.
     */
    public static StyleBuilder rule(String selector) {
        return new StyleBuilder(selector);
    }

    /**
     * Combine multiple CSS rules into a stylesheet string.
     */
    public static String styles(StyleBuilder... rules) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rules.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(rules[i].toRule());
        }
        return sb.toString();
    }

    // ==================== Common Values ====================
    // Note: zero, auto, none, inherit, initial, unset are in CSSUnits
    // Note: transparent, currentColor are in CSSColors

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

    // ==================== Style Builder ====================

    /**
     * Fluent builder for CSS properties.
     * Used for both inline styles and CSS rules.
     */
    public static class StyleBuilder implements CSSValue {
        private final String selector;
        private final Map<String, String> properties = new LinkedHashMap<>();

        StyleBuilder(String selector) {
            this.selector = selector;
        }

        // ==================== Display & Box Model ====================

        public StyleBuilder display(CSSValue value) { return prop("display", value); }
        public StyleBuilder boxSizing(CSSValue value) { return prop("box-sizing", value); }

        public StyleBuilder width(CSSValue value) { return prop("width", value); }
        public StyleBuilder height(CSSValue value) { return prop("height", value); }
        public StyleBuilder minWidth(CSSValue value) { return prop("min-width", value); }
        public StyleBuilder maxWidth(CSSValue value) { return prop("max-width", value); }
        public StyleBuilder minHeight(CSSValue value) { return prop("min-height", value); }
        public StyleBuilder maxHeight(CSSValue value) { return prop("max-height", value); }

        // ==================== Margin ====================

        public StyleBuilder margin(CSSValue all) { return prop("margin", all); }
        public StyleBuilder margin(CSSValue vertical, CSSValue horizontal) {
            return prop("margin", vertical.css() + " " + horizontal.css());
        }
        public StyleBuilder margin(CSSValue top, CSSValue horizontal, CSSValue bottom) {
            return prop("margin", top.css() + " " + horizontal.css() + " " + bottom.css());
        }
        public StyleBuilder margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
            return prop("margin", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
        }
        public StyleBuilder marginTop(CSSValue value) { return prop("margin-top", value); }
        public StyleBuilder marginRight(CSSValue value) { return prop("margin-right", value); }
        public StyleBuilder marginBottom(CSSValue value) { return prop("margin-bottom", value); }
        public StyleBuilder marginLeft(CSSValue value) { return prop("margin-left", value); }
        public StyleBuilder marginX(CSSValue value) { return marginLeft(value).marginRight(value); }
        public StyleBuilder marginY(CSSValue value) { return marginTop(value).marginBottom(value); }

        // ==================== Padding ====================

        public StyleBuilder padding(CSSValue all) { return prop("padding", all); }
        public StyleBuilder padding(CSSValue vertical, CSSValue horizontal) {
            return prop("padding", vertical.css() + " " + horizontal.css());
        }
        public StyleBuilder padding(CSSValue top, CSSValue horizontal, CSSValue bottom) {
            return prop("padding", top.css() + " " + horizontal.css() + " " + bottom.css());
        }
        public StyleBuilder padding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
            return prop("padding", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
        }
        public StyleBuilder paddingTop(CSSValue value) { return prop("padding-top", value); }
        public StyleBuilder paddingRight(CSSValue value) { return prop("padding-right", value); }
        public StyleBuilder paddingBottom(CSSValue value) { return prop("padding-bottom", value); }
        public StyleBuilder paddingLeft(CSSValue value) { return prop("padding-left", value); }
        public StyleBuilder paddingX(CSSValue value) { return paddingLeft(value).paddingRight(value); }
        public StyleBuilder paddingY(CSSValue value) { return paddingTop(value).paddingBottom(value); }

        // ==================== Border ====================

        public StyleBuilder border(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder border(CSSValue value) { return prop("border", value); }
        public StyleBuilder borderWidth(CSSValue value) { return prop("border-width", value); }
        public StyleBuilder borderStyle(CSSValue value) { return prop("border-style", value); }
        public StyleBuilder borderColor(CSSValue value) { return prop("border-color", value); }
        public StyleBuilder borderTop(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-top", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderRight(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-right", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderBottom(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-bottom", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderLeft(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-left", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderRadius(CSSValue value) { return prop("border-radius", value); }
        public StyleBuilder borderRadius(CSSValue topLeft, CSSValue topRight, CSSValue bottomRight, CSSValue bottomLeft) {
            return prop("border-radius", topLeft.css() + " " + topRight.css() + " " + bottomRight.css() + " " + bottomLeft.css());
        }
        public StyleBuilder borderCollapse(CSSValue value) { return prop("border-collapse", value); }

        // ==================== Background ====================

        public StyleBuilder background(CSSValue value) { return prop("background", value); }
        public StyleBuilder backgroundColor(CSSValue value) { return prop("background-color", value); }
        public StyleBuilder backgroundImage(CSSValue value) { return prop("background-image", value); }
        public StyleBuilder backgroundSize(CSSValue value) { return prop("background-size", value); }
        public StyleBuilder backgroundPosition(CSSValue value) { return prop("background-position", value); }
        public StyleBuilder backgroundRepeat(CSSValue value) { return prop("background-repeat", value); }

        // ==================== Typography ====================

        public StyleBuilder color(CSSValue value) { return prop("color", value); }
        public StyleBuilder fontFamily(String value) { return prop("font-family", value); }
        public StyleBuilder fontSize(CSSValue value) { return prop("font-size", value); }
        public StyleBuilder fontWeight(CSSValue value) { return prop("font-weight", value); }
        public StyleBuilder fontWeight(int value) { return prop("font-weight", String.valueOf(value)); }
        public StyleBuilder fontStyle(CSSValue value) { return prop("font-style", value); }
        public StyleBuilder lineHeight(CSSValue value) { return prop("line-height", value); }
        public StyleBuilder lineHeight(double value) { return prop("line-height", String.valueOf(value)); }
        public StyleBuilder letterSpacing(CSSValue value) { return prop("letter-spacing", value); }
        public StyleBuilder textAlign(CSSValue value) { return prop("text-align", value); }
        public StyleBuilder textDecoration(CSSValue value) { return prop("text-decoration", value); }
        public StyleBuilder textTransform(CSSValue value) { return prop("text-transform", value); }
        public StyleBuilder whiteSpace(CSSValue value) { return prop("white-space", value); }
        public StyleBuilder wordBreak(CSSValue value) { return prop("word-break", value); }
        public StyleBuilder textShadow(String value) { return prop("text-shadow", value); }

        // ==================== Flexbox ====================

        public StyleBuilder flexDirection(CSSValue value) { return prop("flex-direction", value); }
        public StyleBuilder flexWrap(CSSValue value) { return prop("flex-wrap", value); }
        public StyleBuilder justifyContent(CSSValue value) { return prop("justify-content", value); }
        public StyleBuilder alignItems(CSSValue value) { return prop("align-items", value); }
        public StyleBuilder alignContent(CSSValue value) { return prop("align-content", value); }
        public StyleBuilder alignSelf(CSSValue value) { return prop("align-self", value); }
        public StyleBuilder flex(CSSValue value) { return prop("flex", value); }
        public StyleBuilder flex(int grow, int shrink, CSSValue basis) {
            return prop("flex", grow + " " + shrink + " " + basis.css());
        }
        public StyleBuilder flexGrow(int value) { return prop("flex-grow", String.valueOf(value)); }
        public StyleBuilder flexShrink(int value) { return prop("flex-shrink", String.valueOf(value)); }
        public StyleBuilder flexBasis(CSSValue value) { return prop("flex-basis", value); }
        public StyleBuilder gap(CSSValue value) { return prop("gap", value); }
        public StyleBuilder gap(CSSValue row, CSSValue column) {
            return prop("gap", row.css() + " " + column.css());
        }
        public StyleBuilder rowGap(CSSValue value) { return prop("row-gap", value); }
        public StyleBuilder columnGap(CSSValue value) { return prop("column-gap", value); }
        public StyleBuilder order(int value) { return prop("order", String.valueOf(value)); }

        // ==================== Grid ====================

        public StyleBuilder gridTemplateColumns(String value) { return prop("grid-template-columns", value); }
        public StyleBuilder gridTemplateRows(String value) { return prop("grid-template-rows", value); }
        public StyleBuilder gridColumn(String value) { return prop("grid-column", value); }
        public StyleBuilder gridRow(String value) { return prop("grid-row", value); }
        public StyleBuilder gridArea(String value) { return prop("grid-area", value); }

        // ==================== Position ====================

        public StyleBuilder position(CSSValue value) { return prop("position", value); }
        public StyleBuilder top(CSSValue value) { return prop("top", value); }
        public StyleBuilder right(CSSValue value) { return prop("right", value); }
        public StyleBuilder bottom(CSSValue value) { return prop("bottom", value); }
        public StyleBuilder left(CSSValue value) { return prop("left", value); }
        public StyleBuilder inset(CSSValue value) { return prop("inset", value); }
        public StyleBuilder zIndex(int value) { return prop("z-index", String.valueOf(value)); }

        // ==================== Overflow ====================

        public StyleBuilder overflow(CSSValue value) { return prop("overflow", value); }
        public StyleBuilder overflowX(CSSValue value) { return prop("overflow-x", value); }
        public StyleBuilder overflowY(CSSValue value) { return prop("overflow-y", value); }

        // ==================== Visibility & Opacity ====================

        public StyleBuilder visibility(CSSValue value) { return prop("visibility", value); }
        public StyleBuilder opacity(double value) { return prop("opacity", String.valueOf(value)); }

        // ==================== Cursor & Interaction ====================

        public StyleBuilder cursor(CSSValue value) { return prop("cursor", value); }
        public StyleBuilder pointerEvents(CSSValue value) { return prop("pointer-events", value); }
        public StyleBuilder userSelect(CSSValue value) { return prop("user-select", value); }

        // ==================== Transform ====================

        public StyleBuilder transform(CSSValue... transforms) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < transforms.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(transforms[i].css());
            }
            return prop("transform", sb.toString());
        }
        public StyleBuilder transformOrigin(String value) { return prop("transform-origin", value); }

        // ==================== Transition ====================

        public StyleBuilder transition(String property, CSSValue duration, CSSValue timing) {
            return prop("transition", property + " " + duration.css() + " " + timing.css());
        }
        public StyleBuilder transition(String property, CSSValue duration) {
            return prop("transition", property + " " + duration.css());
        }
        public StyleBuilder transition(String value) { return prop("transition", value); }

        // ==================== Animation ====================

        public StyleBuilder animation(String value) { return prop("animation", value); }

        // ==================== Box Shadow ====================

        public StyleBuilder boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue color) {
            return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + color.css());
        }
        public StyleBuilder boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue spread, CSSValue color) {
            return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + spread.css() + " " + color.css());
        }
        public StyleBuilder boxShadow(String value) { return prop("box-shadow", value); }

        // ==================== Outline ====================

        public StyleBuilder outline(CSSValue width, CSSValue style, CSSValue color) {
            return prop("outline", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder outline(CSSValue value) { return prop("outline", value); }

        // ==================== List ====================

        public StyleBuilder listStyle(CSSValue value) { return prop("list-style", value); }
        public StyleBuilder listStyleType(CSSValue value) { return prop("list-style-type", value); }

        // ==================== Table ====================

        public StyleBuilder verticalAlign(CSSValue value) { return prop("vertical-align", value); }

        // ==================== Object Fit ====================

        public StyleBuilder objectFit(CSSValue value) { return prop("object-fit", value); }

        // ==================== CSS Variables ====================

        public StyleBuilder var(String name, CSSValue value) {
            String normalized = name.startsWith("--") ? name : "--" + name;
            return prop(normalized, value.css());
        }

        public StyleBuilder var(String name, String value) {
            String normalized = name.startsWith("--") ? name : "--" + name;
            return prop(normalized, value);
        }

        // ==================== Raw property ====================

        public StyleBuilder prop(String name, CSSValue value) {
            properties.put(name, value.css());
            return this;
        }

        public StyleBuilder prop(String name, String value) {
            properties.put(name, value);
            return this;
        }

        // ==================== Build Methods ====================

        /**
         * Build as inline style string (for style attribute).
         */
        public String build() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(";");
            }
            return sb.toString();
        }

        /**
         * Build as CSS rule string (selector { properties }).
         */
        public String toRule() {
            if (selector == null) {
                throw new IllegalStateException("Cannot build rule without selector. Use rule(\"selector\") instead of style()");
            }
            return selector + " { " + build() + " }";
        }

        @Override
        public String css() {
            return build();
        }

        @Override
        public String toString() {
            return selector != null ? toRule() : build();
        }

        public Map<String, String> toMap() {
            return Map.copyOf(properties);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }
    }
}
