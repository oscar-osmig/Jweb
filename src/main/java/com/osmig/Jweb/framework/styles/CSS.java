package com.osmig.Jweb.framework.styles;

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
     * Extends Style to inherit all CSS property methods.
     * Used for both inline styles and CSS rules.
     */
    public static class StyleBuilder extends Style<StyleBuilder> {
        private final String selector;

        StyleBuilder(String selector) {
            this.selector = selector;
        }

        @Override
        protected StyleBuilder self() {
            return this;
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
        public String toString() {
            return selector != null ? toRule() : build();
        }
    }
}
