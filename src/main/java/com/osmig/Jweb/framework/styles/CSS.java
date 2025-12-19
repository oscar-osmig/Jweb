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
     * Create a CSS rule using selector builder.
     */
    public static StyleBuilder rule(Selector selector) {
        return new StyleBuilder(selector.build());
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

    // ==================== Selector Builder ====================

    /**
     * Start building a selector.
     */
    public static Selector select() {
        return new Selector();
    }

    /**
     * Universal selector (*).
     */
    public static Selector all() {
        return new Selector().all();
    }

    /**
     * Element/tag type selector.
     */
    public static Selector tag(String tagName) {
        return new Selector().tag(tagName);
    }

    /**
     * Class selector.
     */
    public static Selector cls(String className) {
        return new Selector().cls(className);
    }

    /**
     * ID selector.
     */
    public static Selector id(String idName) {
        return new Selector().id(idName);
    }

    /**
     * Selector builder for complex CSS selectors.
     */
    public static class Selector {
        private final StringBuilder sb = new StringBuilder();

        public Selector all() { sb.append("*"); return this; }
        public Selector tag(String tagName) { sb.append(tagName); return this; }
        public Selector cls(String className) { sb.append(".").append(className); return this; }
        public Selector id(String idName) { sb.append("#").append(idName); return this; }

        /** Pseudo-class (e.g., hover, focus, first-child) */
        public Selector pseudo(String name) { sb.append(":").append(name); return this; }
        public Selector hover() { return pseudo("hover"); }
        public Selector focus() { return pseudo("focus"); }
        public Selector active() { return pseudo("active"); }
        public Selector visited() { return pseudo("visited"); }
        public Selector firstChild() { return pseudo("first-child"); }
        public Selector lastChild() { return pseudo("last-child"); }
        public Selector nthChild(int n) { sb.append(":nth-child(").append(n).append(")"); return this; }
        public Selector nthChild(String pattern) { sb.append(":nth-child(").append(pattern).append(")"); return this; }
        public Selector focusVisible() { return pseudo("focus-visible"); }
        public Selector focusWithin() { return pseudo("focus-within"); }
        public Selector disabled() { return pseudo("disabled"); }
        public Selector enabled() { return pseudo("enabled"); }
        public Selector checked() { return pseudo("checked"); }
        public Selector empty() { return pseudo("empty"); }
        public Selector not(Selector inner) { sb.append(":not(").append(inner.build()).append(")"); return this; }

        /** Pseudo-element (e.g., before, after) */
        public Selector pseudoEl(String name) { sb.append("::").append(name); return this; }
        public Selector before() { return pseudoEl("before"); }
        public Selector after() { return pseudoEl("after"); }
        public Selector placeholder() { return pseudoEl("placeholder"); }
        public Selector selection() { return pseudoEl("selection"); }
        public Selector firstLine() { return pseudoEl("first-line"); }
        public Selector firstLetter() { return pseudoEl("first-letter"); }

        /** Attribute selectors */
        public Selector attr(String name) { sb.append("[").append(name).append("]"); return this; }
        public Selector attr(String name, String value) { sb.append("[").append(name).append("=\"").append(value).append("\"]"); return this; }
        public Selector attrContains(String name, String value) { sb.append("[").append(name).append("*=\"").append(value).append("\"]"); return this; }
        public Selector attrStartsWith(String name, String value) { sb.append("[").append(name).append("^=\"").append(value).append("\"]"); return this; }
        public Selector attrEndsWith(String name, String value) { sb.append("[").append(name).append("$=\"").append(value).append("\"]"); return this; }

        /** Descendant combinator (space) */
        public Selector descendant(String selector) { sb.append(" ").append(selector); return this; }
        public Selector descendant(Selector selector) { sb.append(" ").append(selector.build()); return this; }

        /** Child combinator (>) */
        public Selector child(String selector) { sb.append(" > ").append(selector); return this; }
        public Selector child(Selector selector) { sb.append(" > ").append(selector.build()); return this; }

        /** Adjacent sibling combinator (+) */
        public Selector adjacent(String selector) { sb.append(" + ").append(selector); return this; }
        public Selector adjacent(Selector selector) { sb.append(" + ").append(selector.build()); return this; }

        /** General sibling combinator (~) */
        public Selector sibling(String selector) { sb.append(" ~ ").append(selector); return this; }
        public Selector sibling(Selector selector) { sb.append(" ~ ").append(selector.build()); return this; }

        /** Combine with another selector (comma-separated) */
        public Selector or(String selector) { sb.append(", ").append(selector); return this; }
        public Selector or(Selector selector) { sb.append(", ").append(selector.build()); return this; }

        /** Raw append for edge cases */
        public Selector raw(String s) { sb.append(s); return this; }

        public String build() { return sb.toString(); }
        @Override public String toString() { return build(); }
    }

    // ==================== Transition Builder ====================

    /**
     * Build multiple transitions.
     * Usage: transitions(trans("color", s(0.2), ease), trans("transform", s(0.3), easeOut))
     */
    public static CSSValue transitions(Transition... transitions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < transitions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(transitions[i].build());
        }
        String result = sb.toString();
        return () -> result;
    }

    /**
     * Create a single transition.
     */
    public static Transition trans(CSSValue property, CSSValue duration) {
        return new Transition(property, duration, null, null);
    }

    public static Transition trans(CSSValue property, CSSValue duration, CSSValue timing) {
        return new Transition(property, duration, timing, null);
    }

    public static Transition trans(CSSValue property, CSSValue duration, CSSValue timing, CSSValue delay) {
        return new Transition(property, duration, timing, delay);
    }

    /** Common CSS properties as values for transitions */
    public static final CSSValue propTransform = () -> "transform";
    public static final CSSValue propColor = () -> "color";
    public static final CSSValue propBackground = () -> "background";
    public static final CSSValue propBackgroundColor = () -> "background-color";
    public static final CSSValue propBorderColor = () -> "border-color";
    public static final CSSValue propBoxShadow = () -> "box-shadow";
    public static final CSSValue propOpacity = () -> "opacity";
    public static final CSSValue propWidth = () -> "width";
    public static final CSSValue propHeight = () -> "height";
    public static final CSSValue propTop = () -> "top";
    public static final CSSValue propLeft = () -> "left";
    public static final CSSValue propRight = () -> "right";
    public static final CSSValue propBottom = () -> "bottom";

    public static class Transition {
        private final CSSValue property;
        private final CSSValue duration;
        private final CSSValue timing;
        private final CSSValue delay;

        Transition(CSSValue property, CSSValue duration, CSSValue timing, CSSValue delay) {
            this.property = property;
            this.duration = duration;
            this.timing = timing;
            this.delay = delay;
        }

        public String build() {
            StringBuilder sb = new StringBuilder(property.css());
            sb.append(" ").append(duration.css());
            if (timing != null) sb.append(" ").append(timing.css());
            if (delay != null) sb.append(" ").append(delay.css());
            return sb.toString();
        }
    }

    // ==================== Animation Name ====================

    /**
     * Creates an animation name as a CSSValue for use in animation() calls.
     */
    public static CSSValue anim(String name) {
        return () -> name;
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

    // Scroll behavior
    public static final CSSValue smooth = () -> "smooth";

    // Animation fill mode
    public static final CSSValue forwards = () -> "forwards";
    public static final CSSValue backwards = () -> "backwards";
    public static final CSSValue animationBoth = () -> "both";

    // Animation iteration
    public static final CSSValue infinite = () -> "infinite";

    // Animation direction
    public static final CSSValue alternate = () -> "alternate";
    public static final CSSValue alternateReverse = () -> "alternate-reverse";
    public static final CSSValue reverse = () -> "reverse";

    // Font smoothing
    public static final CSSValue antialiased = () -> "antialiased";
    public static final CSSValue grayscale_ = () -> "grayscale";  // for -moz-osx-font-smoothing

    // Background clip values
    public static final CSSValue paddingBox = () -> "padding-box";
    // text already exists as cursor value, works for background-clip too

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
        public StyleBuilder borderTopLeftRadius(CSSValue value) { return prop("border-top-left-radius", value); }
        public StyleBuilder borderTopRightRadius(CSSValue value) { return prop("border-top-right-radius", value); }
        public StyleBuilder borderBottomRightRadius(CSSValue value) { return prop("border-bottom-right-radius", value); }
        public StyleBuilder borderBottomLeftRadius(CSSValue value) { return prop("border-bottom-left-radius", value); }

        // ==================== Background ====================

        public StyleBuilder background(CSSValue value) { return prop("background", value); }
        public StyleBuilder backgroundColor(CSSValue value) { return prop("background-color", value); }
        public StyleBuilder backgroundImage(CSSValue value) { return prop("background-image", value); }
        public StyleBuilder backgroundSize(CSSValue value) { return prop("background-size", value); }
        public StyleBuilder backgroundSize(CSSValue width, CSSValue height) {
            return prop("background-size", width.css() + " " + height.css());
        }
        public StyleBuilder backgroundPosition(CSSValue value) { return prop("background-position", value); }
        public StyleBuilder backgroundRepeat(CSSValue value) { return prop("background-repeat", value); }
        public StyleBuilder backgroundAttachment(CSSValue value) { return prop("background-attachment", value); }

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
        public StyleBuilder wordSpacing(CSSValue value) { return prop("word-spacing", value); }
        public StyleBuilder textAlign(CSSValue value) { return prop("text-align", value); }
        public StyleBuilder textDecoration(CSSValue value) { return prop("text-decoration", value); }
        public StyleBuilder textTransform(CSSValue value) { return prop("text-transform", value); }
        public StyleBuilder whiteSpace(CSSValue value) { return prop("white-space", value); }
        public StyleBuilder wordBreak(CSSValue value) { return prop("word-break", value); }
        public StyleBuilder overflowWrap(CSSValue value) { return prop("overflow-wrap", value); }
        public StyleBuilder textShadow(String value) { return prop("text-shadow", value); }
        public StyleBuilder textIndent(CSSValue value) { return prop("text-indent", value); }

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
        public StyleBuilder flexFlow(CSSValue direction, CSSValue wrap) {
            return prop("flex-flow", direction.css() + " " + wrap.css());
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
        public StyleBuilder gridAutoFlow(CSSValue value) { return prop("grid-auto-flow", value); }
        public StyleBuilder justifyItems(CSSValue value) { return prop("justify-items", value); }
        public StyleBuilder placeItems(CSSValue value) { return prop("place-items", value); }
        public StyleBuilder placeContent(CSSValue value) { return prop("place-content", value); }

        // ==================== Position ====================

        public StyleBuilder position(CSSValue value) { return prop("position", value); }
        public StyleBuilder top(CSSValue value) { return prop("top", value); }
        public StyleBuilder right(CSSValue value) { return prop("right", value); }
        public StyleBuilder bottom(CSSValue value) { return prop("bottom", value); }
        public StyleBuilder left(CSSValue value) { return prop("left", value); }
        public StyleBuilder inset(CSSValue value) { return prop("inset", value); }
        public StyleBuilder inset(CSSValue vertical, CSSValue horizontal) {
            return prop("inset", vertical.css() + " " + horizontal.css());
        }
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

        public StyleBuilder transition(CSSValue property, CSSValue duration, CSSValue timing) {
            return prop("transition", property.css() + " " + duration.css() + " " + timing.css());
        }
        public StyleBuilder transition(CSSValue property, CSSValue duration) {
            return prop("transition", property.css() + " " + duration.css());
        }
        public StyleBuilder transition(CSSValue value) { return prop("transition", value); }
        public StyleBuilder transitionDuration(CSSValue value) { return prop("transition-duration", value); }
        public StyleBuilder transitionProperty(CSSValue value) { return prop("transition-property", value); }
        public StyleBuilder transitionTimingFunction(CSSValue value) { return prop("transition-timing-function", value); }
        public StyleBuilder transitionDelay(CSSValue value) { return prop("transition-delay", value); }

        // ==================== Animation ====================

        public StyleBuilder animation(CSSValue name, CSSValue duration, CSSValue timing) {
            return prop("animation", name.css() + " " + duration.css() + " " + timing.css());
        }
        public StyleBuilder animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay) {
            return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css());
        }
        public StyleBuilder animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount) {
            return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css());
        }
        public StyleBuilder animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction) {
            return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css() + " " + direction.css());
        }
        public StyleBuilder animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction, CSSValue fillMode) {
            return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css() + " " + direction.css() + " " + fillMode.css());
        }
        public StyleBuilder animationName(CSSValue value) { return prop("animation-name", value); }
        public StyleBuilder animationDuration(CSSValue value) { return prop("animation-duration", value); }
        public StyleBuilder animationTimingFunction(CSSValue value) { return prop("animation-timing-function", value); }
        public StyleBuilder animationDelay(CSSValue value) { return prop("animation-delay", value); }
        public StyleBuilder animationIterationCount(CSSValue value) { return prop("animation-iteration-count", value); }
        public StyleBuilder animationDirection(CSSValue value) { return prop("animation-direction", value); }
        public StyleBuilder animationFillMode(CSSValue value) { return prop("animation-fill-mode", value); }

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
        public StyleBuilder listStylePosition(CSSValue value) { return prop("list-style-position", value); }

        // ==================== Table ====================

        public StyleBuilder borderCollapse(CSSValue value) { return prop("border-collapse", value); }
        public StyleBuilder borderSpacing(CSSValue value) { return prop("border-spacing", value); }
        public StyleBuilder tableLayout(CSSValue value) { return prop("table-layout", value); }
        public StyleBuilder verticalAlign(CSSValue value) { return prop("vertical-align", value); }

        // ==================== Object Fit ====================

        public StyleBuilder objectFit(CSSValue value) { return prop("object-fit", value); }
        public StyleBuilder objectPosition(String value) { return prop("object-position", value); }

        // ==================== Resize ====================

        public StyleBuilder resize(CSSValue value) { return prop("resize", value); }

        // ==================== Scroll Behavior ====================

        public StyleBuilder scrollBehavior(CSSValue value) { return prop("scroll-behavior", value); }

        // ==================== Font Smoothing (Webkit) ====================

        public StyleBuilder webkitFontSmoothing(CSSValue value) { return prop("-webkit-font-smoothing", value); }
        public StyleBuilder mozOsxFontSmoothing(CSSValue value) { return prop("-moz-osx-font-smoothing", value); }

        // ==================== Background Clip ====================

        public StyleBuilder backgroundClip(CSSValue value) { return prop("background-clip", value); }
        public StyleBuilder webkitBackgroundClip(CSSValue value) { return prop("-webkit-background-clip", value); }
        public StyleBuilder webkitTextFillColor(CSSValue value) { return prop("-webkit-text-fill-color", value); }

        // ==================== Content ====================

        public StyleBuilder content(String value) { return prop("content", "'" + value + "'"); }
        public StyleBuilder content(CSSValue value) { return prop("content", value); }

        // ==================== Outline ====================

        public StyleBuilder outlineOffset(CSSValue value) { return prop("outline-offset", value); }

        // ==================== Filter ====================

        public StyleBuilder filter(CSSValue... filters) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < filters.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(filters[i].css());
            }
            return prop("filter", sb.toString());
        }
        public StyleBuilder backdropFilter(CSSValue... filters) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < filters.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(filters[i].css());
            }
            return prop("backdrop-filter", sb.toString());
        }

        // ==================== Clip Path ====================

        public StyleBuilder clipPath(CSSValue value) { return prop("clip-path", value); }

        // ==================== Aspect Ratio ====================

        public StyleBuilder aspectRatio(String value) { return prop("aspect-ratio", value); }
        public StyleBuilder aspectRatio(int width, int height) {
            return prop("aspect-ratio", width + " / " + height);
        }

        // ==================== Scroll ====================

        public StyleBuilder scrollMargin(CSSValue value) { return prop("scroll-margin", value); }
        public StyleBuilder scrollPadding(CSSValue value) { return prop("scroll-padding", value); }

        // ==================== Logical Properties ====================

        // Margin logical properties
        public StyleBuilder marginInline(CSSValue value) { return prop("margin-inline", value); }
        public StyleBuilder marginInline(CSSValue start, CSSValue end) {
            return prop("margin-inline", start.css() + " " + end.css());
        }
        public StyleBuilder marginInlineStart(CSSValue value) { return prop("margin-inline-start", value); }
        public StyleBuilder marginInlineEnd(CSSValue value) { return prop("margin-inline-end", value); }
        public StyleBuilder marginBlock(CSSValue value) { return prop("margin-block", value); }
        public StyleBuilder marginBlock(CSSValue start, CSSValue end) {
            return prop("margin-block", start.css() + " " + end.css());
        }
        public StyleBuilder marginBlockStart(CSSValue value) { return prop("margin-block-start", value); }
        public StyleBuilder marginBlockEnd(CSSValue value) { return prop("margin-block-end", value); }

        // Padding logical properties
        public StyleBuilder paddingInline(CSSValue value) { return prop("padding-inline", value); }
        public StyleBuilder paddingInline(CSSValue start, CSSValue end) {
            return prop("padding-inline", start.css() + " " + end.css());
        }
        public StyleBuilder paddingInlineStart(CSSValue value) { return prop("padding-inline-start", value); }
        public StyleBuilder paddingInlineEnd(CSSValue value) { return prop("padding-inline-end", value); }
        public StyleBuilder paddingBlock(CSSValue value) { return prop("padding-block", value); }
        public StyleBuilder paddingBlock(CSSValue start, CSSValue end) {
            return prop("padding-block", start.css() + " " + end.css());
        }
        public StyleBuilder paddingBlockStart(CSSValue value) { return prop("padding-block-start", value); }
        public StyleBuilder paddingBlockEnd(CSSValue value) { return prop("padding-block-end", value); }

        // Size logical properties
        public StyleBuilder inlineSize(CSSValue value) { return prop("inline-size", value); }
        public StyleBuilder blockSize(CSSValue value) { return prop("block-size", value); }
        public StyleBuilder minInlineSize(CSSValue value) { return prop("min-inline-size", value); }
        public StyleBuilder maxInlineSize(CSSValue value) { return prop("max-inline-size", value); }
        public StyleBuilder minBlockSize(CSSValue value) { return prop("min-block-size", value); }
        public StyleBuilder maxBlockSize(CSSValue value) { return prop("max-block-size", value); }

        // Position logical properties
        public StyleBuilder insetInline(CSSValue value) { return prop("inset-inline", value); }
        public StyleBuilder insetInline(CSSValue start, CSSValue end) {
            return prop("inset-inline", start.css() + " " + end.css());
        }
        public StyleBuilder insetInlineStart(CSSValue value) { return prop("inset-inline-start", value); }
        public StyleBuilder insetInlineEnd(CSSValue value) { return prop("inset-inline-end", value); }
        public StyleBuilder insetBlock(CSSValue value) { return prop("inset-block", value); }
        public StyleBuilder insetBlock(CSSValue start, CSSValue end) {
            return prop("inset-block", start.css() + " " + end.css());
        }
        public StyleBuilder insetBlockStart(CSSValue value) { return prop("inset-block-start", value); }
        public StyleBuilder insetBlockEnd(CSSValue value) { return prop("inset-block-end", value); }

        // Border logical properties
        public StyleBuilder borderInline(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-inline", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderInlineStart(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-inline-start", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderInlineEnd(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-inline-end", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderBlock(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-block", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderBlockStart(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-block-start", width.css() + " " + style.css() + " " + color.css());
        }
        public StyleBuilder borderBlockEnd(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border-block-end", width.css() + " " + style.css() + " " + color.css());
        }

        // Border radius logical properties
        public StyleBuilder borderStartStartRadius(CSSValue value) { return prop("border-start-start-radius", value); }
        public StyleBuilder borderStartEndRadius(CSSValue value) { return prop("border-start-end-radius", value); }
        public StyleBuilder borderEndStartRadius(CSSValue value) { return prop("border-end-start-radius", value); }
        public StyleBuilder borderEndEndRadius(CSSValue value) { return prop("border-end-end-radius", value); }

        // Text alignment logical
        public StyleBuilder textAlignLast(CSSValue value) { return prop("text-align-last", value); }

        // ==================== Container Queries ====================

        public StyleBuilder containerType(CSSValue value) { return prop("container-type", value); }
        public StyleBuilder containerName(String name) { return prop("container-name", name); }
        public StyleBuilder container(String name, CSSValue type) {
            return prop("container", name + " / " + type.css());
        }

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
