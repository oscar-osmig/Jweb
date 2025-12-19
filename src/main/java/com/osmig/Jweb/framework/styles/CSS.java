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
     * Creates a new inline style builder.
     * Use this for applying styles directly to elements.
     *
     * <p>Example:</p>
     * <pre>
     * div().style(style().padding(px(10)).color(red))
     * </pre>
     *
     * @return a new StyleBuilder instance for inline styles
     * @see #rule(String) for creating CSS rules with selectors
     */
    public static StyleBuilder style() {
        return new StyleBuilder(null);
    }

    /**
     * Creates a CSS rule builder for a given selector.
     * Use this for building stylesheet rules.
     *
     * <p>Example:</p>
     * <pre>
     * rule(".btn").padding(px(10)).backgroundColor(blue)
     * // Output: .btn { padding: 10px; background-color: blue; }
     * </pre>
     *
     * @param selector the CSS selector (e.g., ".class", "#id", "div")
     * @return a new StyleBuilder instance for the CSS rule
     * @see #rule(Selector) for using the fluent selector builder
     */
    public static StyleBuilder rule(String selector) {
        return new StyleBuilder(selector);
    }

    /**
     * Creates a CSS rule builder using a fluent selector.
     *
     * <p>Example:</p>
     * <pre>
     * rule(cls("btn").hover()).backgroundColor(darkBlue)
     * // Output: .btn:hover { background-color: darkblue; }
     * </pre>
     *
     * @param selector a Selector builder instance
     * @return a new StyleBuilder instance for the CSS rule
     * @see #select() for creating selector builders
     */
    public static StyleBuilder rule(Selector selector) {
        return new StyleBuilder(selector.build());
    }

    /**
     * Combines multiple CSS rules into a complete stylesheet string.
     *
     * <p>Example:</p>
     * <pre>
     * String css = styles(
     *     rule("*").boxSizing(borderBox),
     *     rule("body").margin(zero).fontFamily("system-ui"),
     *     rule(".btn").padding(px(10)).color(white)
     * );
     * // Output:
     * // * { box-sizing: border-box; }
     * // body { margin: 0; font-family: system-ui; }
     * // .btn { padding: 10px; color: white; }
     * </pre>
     *
     * @param rules the StyleBuilder rules to combine
     * @return a formatted CSS stylesheet string
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
     * Starts building a CSS selector from scratch.
     *
     * <p>Example:</p>
     * <pre>
     * select().tag("div").cls("container").hover()
     * // Output: div.container:hover
     * </pre>
     *
     * @return a new Selector builder instance
     */
    public static Selector select() {
        return new Selector();
    }

    /**
     * Creates a universal selector (*) that matches all elements.
     *
     * <p>Example:</p>
     * <pre>
     * rule(all()).boxSizing(borderBox)
     * // Output: * { box-sizing: border-box; }
     * </pre>
     *
     * @return a Selector starting with the universal selector
     */
    public static Selector all() {
        return new Selector().all();
    }

    /**
     * Creates an element/tag type selector.
     *
     * <p>Example:</p>
     * <pre>
     * rule(tag("button")).cursor(pointer)
     * // Output: button { cursor: pointer; }
     * </pre>
     *
     * @param tagName the HTML tag name (e.g., "div", "button", "p")
     * @return a Selector starting with the tag selector
     */
    public static Selector tag(String tagName) {
        return new Selector().tag(tagName);
    }

    /**
     * Creates a class selector.
     *
     * <p>Example:</p>
     * <pre>
     * rule(cls("btn").hover()).backgroundColor(darkBlue)
     * // Output: .btn:hover { background-color: darkblue; }
     * </pre>
     *
     * @param className the class name (without the leading dot)
     * @return a Selector starting with the class selector
     */
    public static Selector cls(String className) {
        return new Selector().cls(className);
    }

    /**
     * Creates an ID selector.
     *
     * <p>Example:</p>
     * <pre>
     * rule(id("header")).position(fixed)
     * // Output: #header { position: fixed; }
     * </pre>
     *
     * @param idName the element ID (without the leading hash)
     * @return a Selector starting with the ID selector
     */
    public static Selector id(String idName) {
        return new Selector().id(idName);
    }

    /**
     * Fluent builder for constructing complex CSS selectors.
     * Supports pseudo-classes, pseudo-elements, attribute selectors,
     * and combinators (descendant, child, sibling).
     *
     * <p>Example:</p>
     * <pre>
     * // Complex selector: .nav > li:hover > a
     * cls("nav").child("li").hover().child("a")
     *
     * // Multiple selectors: h1, h2, h3
     * tag("h1").or(tag("h2")).or(tag("h3"))
     *
     * // Attribute selector: input[type="text"]
     * tag("input").attr("type", "text")
     * </pre>
     */
    public static class Selector {
        private final StringBuilder sb = new StringBuilder();

        /** Appends the universal selector (*). */
        public Selector all() { sb.append("*"); return this; }

        /** Appends a tag/element selector. @param tagName the HTML tag name */
        public Selector tag(String tagName) { sb.append(tagName); return this; }

        /** Appends a class selector. @param className the class name (no leading dot) */
        public Selector cls(String className) { sb.append(".").append(className); return this; }

        /** Appends an ID selector. @param idName the ID (no leading hash) */
        public Selector id(String idName) { sb.append("#").append(idName); return this; }

        // ========== Pseudo-classes ==========

        /** Appends a pseudo-class. @param name the pseudo-class name */
        public Selector pseudo(String name) { sb.append(":").append(name); return this; }

        /** Appends :hover pseudo-class (mouse over). */
        public Selector hover() { return pseudo("hover"); }

        /** Appends :focus pseudo-class (focused element). */
        public Selector focus() { return pseudo("focus"); }

        /** Appends :active pseudo-class (being clicked). */
        public Selector active() { return pseudo("active"); }

        /** Appends :visited pseudo-class (visited links). */
        public Selector visited() { return pseudo("visited"); }

        /** Appends :first-child pseudo-class. */
        public Selector firstChild() { return pseudo("first-child"); }

        /** Appends :last-child pseudo-class. */
        public Selector lastChild() { return pseudo("last-child"); }

        /** Appends :nth-child(n) pseudo-class. @param n the child index (1-based) */
        public Selector nthChild(int n) { sb.append(":nth-child(").append(n).append(")"); return this; }

        /** Appends :nth-child() with a pattern. @param pattern e.g., "2n", "odd", "3n+1" */
        public Selector nthChild(String pattern) { sb.append(":nth-child(").append(pattern).append(")"); return this; }

        /** Appends :focus-visible pseudo-class (keyboard focus). */
        public Selector focusVisible() { return pseudo("focus-visible"); }

        /** Appends :focus-within pseudo-class (contains focused element). */
        public Selector focusWithin() { return pseudo("focus-within"); }

        /** Appends :disabled pseudo-class. */
        public Selector disabled() { return pseudo("disabled"); }

        /** Appends :enabled pseudo-class. */
        public Selector enabled() { return pseudo("enabled"); }

        /** Appends :checked pseudo-class (checked inputs). */
        public Selector checked() { return pseudo("checked"); }

        /** Appends :empty pseudo-class (no children). */
        public Selector empty() { return pseudo("empty"); }

        /** Appends :not() pseudo-class. @param inner the selector to negate */
        public Selector not(Selector inner) { sb.append(":not(").append(inner.build()).append(")"); return this; }

        // ========== Pseudo-elements ==========

        /** Appends a pseudo-element. @param name the pseudo-element name */
        public Selector pseudoEl(String name) { sb.append("::").append(name); return this; }

        /** Appends ::before pseudo-element. */
        public Selector before() { return pseudoEl("before"); }

        /** Appends ::after pseudo-element. */
        public Selector after() { return pseudoEl("after"); }

        /** Appends ::placeholder pseudo-element. */
        public Selector placeholder() { return pseudoEl("placeholder"); }

        /** Appends ::selection pseudo-element (selected text). */
        public Selector selection() { return pseudoEl("selection"); }

        /** Appends ::first-line pseudo-element. */
        public Selector firstLine() { return pseudoEl("first-line"); }

        /** Appends ::first-letter pseudo-element. */
        public Selector firstLetter() { return pseudoEl("first-letter"); }

        // ========== Attribute Selectors ==========

        /** Appends [attr] (has attribute). @param name the attribute name */
        public Selector attr(String name) { sb.append("[").append(name).append("]"); return this; }

        /** Appends [attr="value"]. @param name the attribute name @param value the exact value */
        public Selector attr(String name, String value) { sb.append("[").append(name).append("=\"").append(value).append("\"]"); return this; }

        /** Appends [attr*="value"] (contains). @param name the attribute name @param value the substring */
        public Selector attrContains(String name, String value) { sb.append("[").append(name).append("*=\"").append(value).append("\"]"); return this; }

        /** Appends [attr^="value"] (starts with). @param name the attribute name @param value the prefix */
        public Selector attrStartsWith(String name, String value) { sb.append("[").append(name).append("^=\"").append(value).append("\"]"); return this; }

        /** Appends [attr$="value"] (ends with). @param name the attribute name @param value the suffix */
        public Selector attrEndsWith(String name, String value) { sb.append("[").append(name).append("$=\"").append(value).append("\"]"); return this; }

        // ========== Combinators ==========

        /** Appends descendant combinator (space). Matches any descendant. */
        public Selector descendant(String selector) { sb.append(" ").append(selector); return this; }

        /** Appends descendant combinator (space). Matches any descendant. */
        public Selector descendant(Selector selector) { sb.append(" ").append(selector.build()); return this; }

        /** Appends child combinator (&gt;). Matches direct children only. */
        public Selector child(String selector) { sb.append(" > ").append(selector); return this; }

        /** Appends child combinator (&gt;). Matches direct children only. */
        public Selector child(Selector selector) { sb.append(" > ").append(selector.build()); return this; }

        /** Appends adjacent sibling combinator (+). Matches immediately following sibling. */
        public Selector adjacent(String selector) { sb.append(" + ").append(selector); return this; }

        /** Appends adjacent sibling combinator (+). Matches immediately following sibling. */
        public Selector adjacent(Selector selector) { sb.append(" + ").append(selector.build()); return this; }

        /** Appends general sibling combinator (~). Matches any following sibling. */
        public Selector sibling(String selector) { sb.append(" ~ ").append(selector); return this; }

        /** Appends general sibling combinator (~). Matches any following sibling. */
        public Selector sibling(Selector selector) { sb.append(" ~ ").append(selector.build()); return this; }

        /** Combines with another selector (comma-separated grouping). */
        public Selector or(String selector) { sb.append(", ").append(selector); return this; }

        /** Combines with another selector (comma-separated grouping). */
        public Selector or(Selector selector) { sb.append(", ").append(selector.build()); return this; }

        /** Appends raw CSS string for edge cases not covered by builder methods. */
        public Selector raw(String s) { sb.append(s); return this; }

        /** Builds and returns the complete selector string. @return the CSS selector */
        public String build() { return sb.toString(); }

        @Override public String toString() { return build(); }
    }

    // ==================== Transition Builder ====================

    /**
     * Builds multiple CSS transitions into a single value.
     * Use with {@code style().transition()} for multi-property transitions.
     *
     * <p>Example:</p>
     * <pre>
     * style().transition(transitions(
     *     trans(propColor, s(0.2), ease),
     *     trans(propTransform, s(0.3), easeOut)
     * ))
     * // Output: transition: color 0.2s ease, transform 0.3s ease-out;
     * </pre>
     *
     * @param transitions the individual transitions to combine
     * @return a CSSValue containing all transitions comma-separated
     * @see #trans(CSSValue, CSSValue) for creating individual transitions
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
     * Creates a transition for a property with duration.
     *
     * <p>Example:</p>
     * <pre>
     * trans(propOpacity, s(0.3))
     * // Output: opacity 0.3s
     * </pre>
     *
     * @param property the CSS property to animate (use propXxx constants)
     * @param duration the transition duration (use s() or ms())
     * @return a Transition builder
     */
    public static Transition trans(CSSValue property, CSSValue duration) {
        return new Transition(property, duration, null, null);
    }

    /**
     * Creates a transition with timing function.
     *
     * <p>Example:</p>
     * <pre>
     * trans(propTransform, s(0.3), easeOut)
     * // Output: transform 0.3s ease-out
     * </pre>
     *
     * @param property the CSS property to animate
     * @param duration the transition duration
     * @param timing the timing function (ease, easeIn, easeOut, linear, etc.)
     * @return a Transition builder
     */
    public static Transition trans(CSSValue property, CSSValue duration, CSSValue timing) {
        return new Transition(property, duration, timing, null);
    }

    /**
     * Creates a transition with timing function and delay.
     *
     * <p>Example:</p>
     * <pre>
     * trans(propColor, s(0.3), ease, ms(100))
     * // Output: color 0.3s ease 100ms
     * </pre>
     *
     * @param property the CSS property to animate
     * @param duration the transition duration
     * @param timing the timing function
     * @param delay the delay before transition starts
     * @return a Transition builder
     */
    public static Transition trans(CSSValue property, CSSValue duration, CSSValue timing, CSSValue delay) {
        return new Transition(property, duration, timing, delay);
    }

    // ========== Transition Property Constants ==========

    /** Property name: transform - for use with transitions. */
    public static final CSSValue propTransform = () -> "transform";
    /** Property name: color - for use with transitions. */
    public static final CSSValue propColor = () -> "color";
    /** Property name: background - for use with transitions. */
    public static final CSSValue propBackground = () -> "background";
    /** Property name: background-color - for use with transitions. */
    public static final CSSValue propBackgroundColor = () -> "background-color";
    /** Property name: border-color - for use with transitions. */
    public static final CSSValue propBorderColor = () -> "border-color";
    /** Property name: box-shadow - for use with transitions. */
    public static final CSSValue propBoxShadow = () -> "box-shadow";
    /** Property name: opacity - for use with transitions. */
    public static final CSSValue propOpacity = () -> "opacity";
    /** Property name: width - for use with transitions. */
    public static final CSSValue propWidth = () -> "width";
    /** Property name: height - for use with transitions. */
    public static final CSSValue propHeight = () -> "height";
    /** Property name: top - for use with transitions. */
    public static final CSSValue propTop = () -> "top";
    /** Property name: left - for use with transitions. */
    public static final CSSValue propLeft = () -> "left";
    /** Property name: right - for use with transitions. */
    public static final CSSValue propRight = () -> "right";
    /** Property name: bottom - for use with transitions. */
    public static final CSSValue propBottom = () -> "bottom";

    /**
     * Represents a single CSS transition definition.
     * Contains property, duration, optional timing function, and optional delay.
     */
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

        /** Builds the transition string (e.g., "color 0.3s ease"). */
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
     * Creates an animation name reference for use with animation properties.
     * The name should match a {@code @keyframes} rule defined in your styles.
     *
     * <p>Example:</p>
     * <pre>
     * style().animation(anim("fadeIn"), s(0.5), easeOut)
     * // Output: animation: fadeIn 0.5s ease-out;
     * </pre>
     *
     * @param name the keyframes animation name
     * @return a CSSValue containing the animation name
     * @see Keyframes for creating keyframes animations
     */
    public static CSSValue anim(String name) {
        return () -> name;
    }

    // ==================== Common Values ====================
    // Note: zero, auto, none, inherit, initial, unset are in CSSUnits
    // Note: transparent, currentColor are in CSSColors

    // ========== Display Values ==========
    // Use with: style().display(flex)

    /** Display: block - element starts on new line, takes full width. */
    public static final CSSValue block = () -> "block";
    /** Display: inline - element flows with text, no line break. */
    public static final CSSValue inline = () -> "inline";
    /** Display: inline-block - inline but respects width/height. */
    public static final CSSValue inlineBlock = () -> "inline-block";
    /** Display: flex - enables flexbox layout. */
    public static final CSSValue flex = () -> "flex";
    /** Display: inline-flex - inline flexbox container. */
    public static final CSSValue inlineFlex = () -> "inline-flex";
    /** Display: grid - enables CSS Grid layout. */
    public static final CSSValue grid = () -> "grid";
    /** Display: inline-grid - inline grid container. */
    public static final CSSValue inlineGrid = () -> "inline-grid";
    /** Display: contents - element's children promoted to parent. */
    public static final CSSValue contents = () -> "contents";
    /** Display: flow-root - creates new block formatting context. */
    public static final CSSValue flowRoot = () -> "flow-root";

    // ========== Position Values ==========
    // Use with: style().position(absolute)

    /** Position: relative - positioned relative to normal position. */
    public static final CSSValue relative = () -> "relative";
    /** Position: absolute - positioned relative to nearest positioned ancestor. */
    public static final CSSValue absolute = () -> "absolute";
    /** Position: fixed - positioned relative to viewport. */
    public static final CSSValue fixed = () -> "fixed";
    /** Position: sticky - toggles between relative and fixed based on scroll. */
    public static final CSSValue sticky = () -> "sticky";
    /** Position: static - default positioning. */
    public static final CSSValue static_ = () -> "static";

    // ========== Flexbox Direction ==========
    // Use with: style().flexDirection(column)

    /** Flex-direction: row - items flow horizontally (default). */
    public static final CSSValue row = () -> "row";
    /** Flex-direction: row-reverse - items flow horizontally, reversed. */
    public static final CSSValue rowReverse = () -> "row-reverse";
    /** Flex-direction: column - items flow vertically. */
    public static final CSSValue column = () -> "column";
    /** Flex-direction: column-reverse - items flow vertically, reversed. */
    public static final CSSValue columnReverse = () -> "column-reverse";

    // ========== Flexbox Wrap ==========
    // Use with: style().flexWrap(wrap)

    /** Flex-wrap: wrap - items wrap to next line. */
    public static final CSSValue wrap = () -> "wrap";
    /** Flex-wrap: nowrap - items stay on one line (default). */
    public static final CSSValue nowrap = () -> "nowrap";
    /** Flex-wrap: wrap-reverse - items wrap to previous line. */
    public static final CSSValue wrapReverse = () -> "wrap-reverse";

    // ========== Alignment Values ==========
    // Use with: style().justifyContent(center), style().alignItems(flexStart)

    /** Alignment: center - items centered on axis. */
    public static final CSSValue center = () -> "center";
    /** Alignment: flex-start - items aligned to start. */
    public static final CSSValue flexStart = () -> "flex-start";
    /** Alignment: flex-end - items aligned to end. */
    public static final CSSValue flexEnd = () -> "flex-end";
    /** Alignment: start - items aligned to start (grid). */
    public static final CSSValue start = () -> "start";
    /** Alignment: end - items aligned to end (grid). */
    public static final CSSValue end = () -> "end";
    /** Alignment: space-between - equal space between items. */
    public static final CSSValue spaceBetween = () -> "space-between";
    /** Alignment: space-around - equal space around items. */
    public static final CSSValue spaceAround = () -> "space-around";
    /** Alignment: space-evenly - equal space everywhere. */
    public static final CSSValue spaceEvenly = () -> "space-evenly";
    /** Alignment: stretch - items stretch to fill container. */
    public static final CSSValue stretch = () -> "stretch";
    /** Alignment: baseline - items aligned to text baseline. */
    public static final CSSValue baseline = () -> "baseline";

    // ========== Border Styles ==========
    // Use with: style().border(px(1), solid, gray)

    /** Border-style: solid - continuous line. */
    public static final CSSValue solid = () -> "solid";
    /** Border-style: dashed - series of dashes. */
    public static final CSSValue dashed = () -> "dashed";
    /** Border-style: dotted - series of dots. */
    public static final CSSValue dotted = () -> "dotted";
    /** Border-style: double - two parallel lines. */
    public static final CSSValue double_ = () -> "double";
    /** Border-style: groove - carved appearance. */
    public static final CSSValue groove = () -> "groove";
    /** Border-style: ridge - extruded appearance. */
    public static final CSSValue ridge = () -> "ridge";
    /** Border-style: inset - embedded appearance. */
    public static final CSSValue inset = () -> "inset";
    /** Border-style: outset - raised appearance. */
    public static final CSSValue outset = () -> "outset";
    /** Border-style: hidden - same as none but affects table borders. */
    public static final CSSValue hidden = () -> "hidden";

    // ========== Text Alignment ==========
    // Use with: style().textAlign(center)

    /** Text-align: left. */
    public static final CSSValue left = () -> "left";
    /** Text-align: right. */
    public static final CSSValue right = () -> "right";
    /** Text-align: justify - stretches lines to fill width. */
    public static final CSSValue justify = () -> "justify";

    // ========== Text Decoration ==========
    // Use with: style().textDecoration(underline)

    /** Text-decoration: underline. */
    public static final CSSValue underline = () -> "underline";
    /** Text-decoration: overline. */
    public static final CSSValue overline = () -> "overline";
    /** Text-decoration: line-through (strikethrough). */
    public static final CSSValue lineThrough = () -> "line-through";

    // ========== Text Transform ==========
    // Use with: style().textTransform(uppercase)

    /** Text-transform: uppercase. */
    public static final CSSValue uppercase = () -> "uppercase";
    /** Text-transform: lowercase. */
    public static final CSSValue lowercase = () -> "lowercase";
    /** Text-transform: capitalize. */
    public static final CSSValue capitalize = () -> "capitalize";

    // ========== Font Weight ==========
    // Use with: style().fontWeight(bold)

    /** Font-weight: normal (400). */
    public static final CSSValue normal = () -> "normal";
    /** Font-weight: bold (700). */
    public static final CSSValue bold = () -> "bold";
    /** Font-weight: bolder (relative to parent). */
    public static final CSSValue bolder = () -> "bolder";
    /** Font-weight: lighter (relative to parent). */
    public static final CSSValue lighter = () -> "lighter";

    // ========== Font Style ==========
    // Use with: style().fontStyle(italic)

    /** Font-style: italic. */
    public static final CSSValue italic = () -> "italic";
    /** Font-style: oblique (slanted). */
    public static final CSSValue oblique = () -> "oblique";

    // ========== Overflow Values ==========
    // Use with: style().overflow(hidden)

    /** Overflow: visible - content overflows element box. */
    public static final CSSValue visible = () -> "visible";
    /** Overflow: scroll - always show scrollbars. */
    public static final CSSValue scroll = () -> "scroll";
    /** Overflow: clip - clips at padding box, no scrolling. */
    public static final CSSValue clip = () -> "clip";

    // ========== Cursor Values ==========
    // Use with: style().cursor(pointer)

    /** Cursor: pointer - hand icon (for clickable elements). */
    public static final CSSValue pointer = () -> "pointer";
    /** Cursor: crosshair. */
    public static final CSSValue crosshair = () -> "crosshair";
    /** Cursor: move - indicates movable element. */
    public static final CSSValue move = () -> "move";
    /** Cursor: text - I-beam for text selection. */
    public static final CSSValue text = () -> "text";
    /** Cursor: wait - hourglass/spinner. */
    public static final CSSValue wait = () -> "wait";
    /** Cursor: help - question mark. */
    public static final CSSValue help = () -> "help";
    /** Cursor: not-allowed - prohibited action. */
    public static final CSSValue notAllowed = () -> "not-allowed";
    /** Cursor: grab - open hand. */
    public static final CSSValue grab = () -> "grab";
    /** Cursor: grabbing - closed hand (dragging). */
    public static final CSSValue grabbing = () -> "grabbing";

    // ========== Visibility ==========

    /** Visibility: collapse - for table rows/columns. */
    public static final CSSValue collapse = () -> "collapse";

    // ========== White Space ==========
    // Use with: style().whiteSpace(pre)

    /** White-space: pre - preserves whitespace and newlines. */
    public static final CSSValue pre = () -> "pre";
    /** White-space: pre-wrap - preserves whitespace, wraps lines. */
    public static final CSSValue preWrap = () -> "pre-wrap";
    /** White-space: pre-line - collapses spaces, preserves newlines. */
    public static final CSSValue preLine = () -> "pre-line";
    /** White-space: break-spaces - preserves whitespace, wraps on any space. */
    public static final CSSValue breakSpaces = () -> "break-spaces";

    // ========== Word Break ==========
    // Use with: style().wordBreak(breakAll)

    /** Word-break: break-all - break anywhere to prevent overflow. */
    public static final CSSValue breakAll = () -> "break-all";
    /** Word-break: keep-all - no breaks within words (CJK). */
    public static final CSSValue keepAll = () -> "keep-all";
    /** Overflow-wrap: break-word - break long words if needed. */
    public static final CSSValue breakWord = () -> "break-word";

    // ========== Object Fit ==========
    // Use with: style().objectFit(cover)

    /** Object-fit: fill - stretches to fill (default). */
    public static final CSSValue fill = () -> "fill";
    /** Object-fit: contain - scales to fit, maintains aspect ratio. */
    public static final CSSValue contain = () -> "contain";
    /** Object-fit: cover - scales to cover, may clip. */
    public static final CSSValue cover = () -> "cover";
    /** Object-fit: scale-down - uses contain or none, whichever is smaller. */
    public static final CSSValue scaleDown = () -> "scale-down";

    // ========== Background Size ==========
    // Use with: style().backgroundSize(bgCover)

    /** Background-size: contain. */
    public static final CSSValue bgContain = () -> "contain";
    /** Background-size: cover. */
    public static final CSSValue bgCover = () -> "cover";

    // ========== Background Repeat ==========
    // Use with: style().backgroundRepeat(noRepeat)

    /** Background-repeat: repeat (default). */
    public static final CSSValue repeat = () -> "repeat";
    /** Background-repeat: repeat-x - horizontal only. */
    public static final CSSValue repeatX = () -> "repeat-x";
    /** Background-repeat: repeat-y - vertical only. */
    public static final CSSValue repeatY = () -> "repeat-y";
    /** Background-repeat: no-repeat. */
    public static final CSSValue noRepeat = () -> "no-repeat";

    // ========== Background Attachment ==========

    /** Background-attachment: local - scrolls with content. */
    public static final CSSValue local = () -> "local";

    // ========== List Style ==========
    // Use with: style().listStyleType(disc)

    /** List-style-type: disc - filled circle (default for ul). */
    public static final CSSValue disc = () -> "disc";
    /** List-style-type: circle - hollow circle. */
    public static final CSSValue circle = () -> "circle";
    /** List-style-type: square. */
    public static final CSSValue square = () -> "square";
    /** List-style-type: decimal - numbers (default for ol). */
    public static final CSSValue decimal = () -> "decimal";
    /** List-style-type: lower-alpha - a, b, c... */
    public static final CSSValue lowerAlpha = () -> "lower-alpha";
    /** List-style-type: upper-alpha - A, B, C... */
    public static final CSSValue upperAlpha = () -> "upper-alpha";
    /** List-style-type: lower-roman - i, ii, iii... */
    public static final CSSValue lowerRoman = () -> "lower-roman";
    /** List-style-type: upper-roman - I, II, III... */
    public static final CSSValue upperRoman = () -> "upper-roman";

    // ========== Timing Functions ==========
    // Use with: style().transition(propAll, s(0.3), ease)

    /** Timing: ease - slow start, fast middle, slow end (default). */
    public static final CSSValue ease = () -> "ease";
    /** Timing: ease-in - slow start. */
    public static final CSSValue easeIn = () -> "ease-in";
    /** Timing: ease-out - slow end. */
    public static final CSSValue easeOut = () -> "ease-out";
    /** Timing: ease-in-out - slow start and end. */
    public static final CSSValue easeInOut = () -> "ease-in-out";
    /** Timing: linear - constant speed. */
    public static final CSSValue linear = () -> "linear";

    // ========== Box Sizing ==========
    // Use with: style().boxSizing(borderBox)

    /** Box-sizing: border-box - width/height includes padding and border. */
    public static final CSSValue borderBox = () -> "border-box";
    /** Box-sizing: content-box - width/height is content only (default). */
    public static final CSSValue contentBox = () -> "content-box";

    // ========== Resize ==========
    // Use with: style().resize(both)

    /** Resize: both - user can resize horizontally and vertically. */
    public static final CSSValue both = () -> "both";
    /** Resize: horizontal - user can resize horizontally only. */
    public static final CSSValue horizontal = () -> "horizontal";
    /** Resize: vertical - user can resize vertically only. */
    public static final CSSValue vertical = () -> "vertical";

    // ========== Pointer Events ==========

    /** Pointer-events: all - responds to all pointer events. */
    public static final CSSValue all = () -> "all";

    // ========== User Select ==========
    // Use with: style().userSelect(selectNone)

    /** User-select: none - text cannot be selected. */
    public static final CSSValue selectNone = () -> "none";
    /** User-select: text - text can be selected. */
    public static final CSSValue selectText = () -> "text";
    /** User-select: all - click selects entire element. */
    public static final CSSValue selectAll = () -> "all";

    // ========== Scroll Behavior ==========

    /** Scroll-behavior: smooth - animated scrolling. */
    public static final CSSValue smooth = () -> "smooth";

    // ========== Animation Fill Mode ==========
    // Use with: style().animationFillMode(forwards)

    /** Animation-fill-mode: forwards - retains final keyframe styles. */
    public static final CSSValue forwards = () -> "forwards";
    /** Animation-fill-mode: backwards - applies first keyframe during delay. */
    public static final CSSValue backwards = () -> "backwards";
    /** Animation-fill-mode: both - applies both forwards and backwards. */
    public static final CSSValue animationBoth = () -> "both";

    // ========== Animation Iteration ==========

    /** Animation-iteration-count: infinite - animation loops forever. */
    public static final CSSValue infinite = () -> "infinite";

    // ========== Animation Direction ==========
    // Use with: style().animationDirection(alternate)

    /** Animation-direction: alternate - alternates between forward and reverse. */
    public static final CSSValue alternate = () -> "alternate";
    /** Animation-direction: alternate-reverse - starts in reverse, alternates. */
    public static final CSSValue alternateReverse = () -> "alternate-reverse";
    /** Animation-direction: reverse - plays animation backwards. */
    public static final CSSValue reverse = () -> "reverse";

    // ========== Font Smoothing ==========

    /** For -webkit-font-smoothing: antialiased. */
    public static final CSSValue antialiased = () -> "antialiased";
    /** For -moz-osx-font-smoothing: grayscale. */
    public static final CSSValue grayscale_ = () -> "grayscale";

    // ========== Background Clip ==========

    /** Background-clip: padding-box. */
    public static final CSSValue paddingBox = () -> "padding-box";

    // ==================== CSS Functions ====================

    /**
     * References a CSS custom property (CSS variable).
     * Automatically adds "--" prefix if not present.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(var("primary-color"))
     * // Output: color: var(--primary-color);
     * </pre>
     *
     * @param name the variable name (with or without "--" prefix)
     * @return a CSSValue containing the var() function
     */
    public static CSSValue var(String name) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ")";
    }

    /**
     * References a CSS variable with a fallback value.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(var("primary-color", blue))
     * // Output: color: var(--primary-color, blue);
     * </pre>
     *
     * @param name the variable name
     * @param fallback the fallback value if variable is not defined
     * @return a CSSValue containing the var() function with fallback
     */
    public static CSSValue var(String name, CSSValue fallback) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ", " + fallback.css() + ")";
    }

    /**
     * Creates a url() function for referencing external resources.
     * Used for background images, fonts, cursors, etc.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundImage(url("/images/hero.jpg"))
     * // Output: background-image: url('/images/hero.jpg');
     * </pre>
     *
     * @param path the URL or path to the resource
     * @return a CSSValue containing the url() function
     */
    public static CSSValue url(String path) {
        return () -> "url('" + path + "')";
    }

    /**
     * Creates a custom cubic-bezier timing function.
     * Define custom easing curves for transitions and animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().transition(propAll, s(0.3), cubicBezier(0.4, 0, 0.2, 1))
     * // Output: transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
     * </pre>
     *
     * @param x1 first control point X (0-1)
     * @param y1 first control point Y
     * @param x2 second control point X (0-1)
     * @param y2 second control point Y
     * @return a CSSValue containing the cubic-bezier() function
     */
    public static CSSValue cubicBezier(double x1, double y1, double x2, double y2) {
        return () -> "cubic-bezier(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
    }

    // ========== Transform Functions ==========

    /**
     * Creates a translate() transform (move X and Y).
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(translate(px(10), px(20)))
     * // Output: transform: translate(10px, 20px);
     * </pre>
     *
     * @param x horizontal offset
     * @param y vertical offset
     * @return a CSSValue for the transform
     */
    public static CSSValue translate(CSSValue x, CSSValue y) {
        return () -> "translate(" + x.css() + ", " + y.css() + ")";
    }

    /**
     * Creates a translateX() transform (move horizontally).
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(translateX(px(-100)))
     * // Output: transform: translateX(-100px);
     * </pre>
     *
     * @param x horizontal offset
     * @return a CSSValue for the transform
     */
    public static CSSValue translateX(CSSValue x) {
        return () -> "translateX(" + x.css() + ")";
    }

    /**
     * Creates a translateY() transform (move vertically).
     *
     * @param y vertical offset
     * @return a CSSValue for the transform
     */
    public static CSSValue translateY(CSSValue y) {
        return () -> "translateY(" + y.css() + ")";
    }

    /**
     * Creates a uniform scale() transform.
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(scale(1.5))
     * // Output: transform: scale(1.5);
     * </pre>
     *
     * @param value scale factor (1 = normal, 2 = double size)
     * @return a CSSValue for the transform
     */
    public static CSSValue scale(double value) {
        return () -> "scale(" + value + ")";
    }

    /**
     * Creates a scale() transform with different X and Y factors.
     *
     * @param x horizontal scale factor
     * @param y vertical scale factor
     * @return a CSSValue for the transform
     */
    public static CSSValue scale(double x, double y) {
        return () -> "scale(" + x + ", " + y + ")";
    }

    /**
     * Creates a scaleX() transform (horizontal scaling).
     *
     * @param x horizontal scale factor
     * @return a CSSValue for the transform
     */
    public static CSSValue scaleX(double x) {
        return () -> "scaleX(" + x + ")";
    }

    /**
     * Creates a scaleY() transform (vertical scaling).
     *
     * @param y vertical scale factor
     * @return a CSSValue for the transform
     */
    public static CSSValue scaleY(double y) {
        return () -> "scaleY(" + y + ")";
    }

    /**
     * Creates a rotate() transform.
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(rotate(deg(45)))
     * // Output: transform: rotate(45deg);
     * </pre>
     *
     * @param angle rotation angle (use deg(), rad(), or turn())
     * @return a CSSValue for the transform
     */
    public static CSSValue rotate(CSSValue angle) {
        return () -> "rotate(" + angle.css() + ")";
    }

    /**
     * Creates a skew() transform (shear X and Y).
     *
     * @param x horizontal skew angle
     * @param y vertical skew angle
     * @return a CSSValue for the transform
     */
    public static CSSValue skew(CSSValue x, CSSValue y) {
        return () -> "skew(" + x.css() + ", " + y.css() + ")";
    }

    /**
     * Creates a skewX() transform (horizontal shear).
     *
     * @param x horizontal skew angle
     * @return a CSSValue for the transform
     */
    public static CSSValue skewX(CSSValue x) {
        return () -> "skewX(" + x.css() + ")";
    }

    /**
     * Creates a skewY() transform (vertical shear).
     *
     * @param y vertical skew angle
     * @return a CSSValue for the transform
     */
    public static CSSValue skewY(CSSValue y) {
        return () -> "skewY(" + y.css() + ")";
    }

    // ========== Gradient Functions ==========

    /**
     * Creates a linear-gradient() background.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(linearGradient(red, blue))
     * // Output: background: linear-gradient(red, blue);
     * </pre>
     *
     * @param stops color stops (colors from CSSColors)
     * @return a CSSValue for the gradient
     */
    public static CSSValue linearGradient(CSSValue... stops) {
        return () -> "linear-gradient(" + joinCss(stops) + ")";
    }

    /**
     * Creates a linear-gradient() with direction.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(linearGradient("to right", red, blue))
     * // Output: background: linear-gradient(to right, red, blue);
     *
     * style().background(linearGradient("45deg", red, blue))
     * // Output: background: linear-gradient(45deg, red, blue);
     * </pre>
     *
     * @param direction the direction ("to right", "45deg", etc.)
     * @param stops color stops
     * @return a CSSValue for the gradient
     */
    public static CSSValue linearGradient(String direction, CSSValue... stops) {
        return () -> "linear-gradient(" + direction + ", " + joinCss(stops) + ")";
    }

    /**
     * Creates a radial-gradient() background.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(radialGradient(white, black))
     * // Output: background: radial-gradient(white, black);
     * </pre>
     *
     * @param stops color stops
     * @return a CSSValue for the gradient
     */
    public static CSSValue radialGradient(CSSValue... stops) {
        return () -> "radial-gradient(" + joinCss(stops) + ")";
    }

    /**
     * Creates a radial-gradient() with shape specification.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(radialGradient("circle at center", red, blue))
     * // Output: background: radial-gradient(circle at center, red, blue);
     * </pre>
     *
     * @param shape the shape ("circle", "ellipse", "circle at center", etc.)
     * @param stops color stops
     * @return a CSSValue for the gradient
     */
    public static CSSValue radialGradient(String shape, CSSValue... stops) {
        return () -> "radial-gradient(" + shape + ", " + joinCss(stops) + ")";
    }

    /**
     * Creates a conic-gradient() background.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(conicGradient(red, yellow, green, blue, red))
     * // Output: background: conic-gradient(red, yellow, green, blue, red);
     * </pre>
     *
     * @param stops color stops
     * @return a CSSValue for the gradient
     */
    public static CSSValue conicGradient(CSSValue... stops) {
        return () -> "conic-gradient(" + joinCss(stops) + ")";
    }

    /**
     * Creates a conic-gradient() with starting angle.
     *
     * <p>Example:</p>
     * <pre>
     * style().background(conicGradient("from 90deg", red, blue))
     * // Output: background: conic-gradient(from 90deg, red, blue);
     * </pre>
     *
     * @param from the starting position ("from 90deg", "from 0deg at center", etc.)
     * @param stops color stops
     * @return a CSSValue for the gradient
     */
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
     * Creates a circle() clip-path function.
     *
     * <p>Example:</p>
     * <pre>
     * style().clipPath(circleClip(percent(50)))
     * // Output: clip-path: circle(50%);
     * </pre>
     *
     * @param radius the circle radius
     * @return a CSSValue for the clip-path
     */
    public static CSSValue circleClip(CSSValue radius) {
        return () -> "circle(" + radius.css() + ")";
    }

    /**
     * Creates a circle() clip-path at a specific position.
     *
     * <p>Example:</p>
     * <pre>
     * style().clipPath(circleClip(percent(50), "center"))
     * // Output: clip-path: circle(50% at center);
     * </pre>
     *
     * @param radius the circle radius
     * @param position the center position (e.g., "center", "top left")
     * @return a CSSValue for the clip-path
     */
    public static CSSValue circleClip(CSSValue radius, String position) {
        return () -> "circle(" + radius.css() + " at " + position + ")";
    }

    /**
     * Creates an ellipse() clip-path function.
     *
     * <p>Example:</p>
     * <pre>
     * style().clipPath(ellipseClip(percent(50), percent(30)))
     * // Output: clip-path: ellipse(50% 30%);
     * </pre>
     *
     * @param radiusX horizontal radius
     * @param radiusY vertical radius
     * @return a CSSValue for the clip-path
     */
    public static CSSValue ellipseClip(CSSValue radiusX, CSSValue radiusY) {
        return () -> "ellipse(" + radiusX.css() + " " + radiusY.css() + ")";
    }

    /**
     * Creates a polygon() clip-path function.
     *
     * <p>Example:</p>
     * <pre>
     * // Triangle
     * style().clipPath(polygon("50% 0%", "0% 100%", "100% 100%"))
     *
     * // Rectangle
     * style().clipPath(polygon("0 0", "100% 0", "100% 100%", "0 100%"))
     * </pre>
     *
     * @param points the polygon vertices as "x y" pairs
     * @return a CSSValue for the clip-path
     */
    public static CSSValue polygon(String... points) {
        return () -> "polygon(" + String.join(", ", points) + ")";
    }

    /**
     * Creates an inset() clip-path function.
     *
     * <p>Example:</p>
     * <pre>
     * style().clipPath(insetClip(px(10)))
     * // Output: clip-path: inset(10px);
     * </pre>
     *
     * @param all the inset amount from all edges
     * @return a CSSValue for the clip-path
     */
    public static CSSValue insetClip(CSSValue all) {
        return () -> "inset(" + all.css() + ")";
    }

    /**
     * Creates an inset() clip-path with rounded corners.
     *
     * <p>Example:</p>
     * <pre>
     * style().clipPath(insetClip(px(10), px(5)))
     * // Output: clip-path: inset(10px round 5px);
     * </pre>
     *
     * @param all the inset amount from all edges
     * @param borderRadius the corner radius
     * @return a CSSValue for the clip-path
     */
    public static CSSValue insetClip(CSSValue all, CSSValue borderRadius) {
        return () -> "inset(" + all.css() + " round " + borderRadius.css() + ")";
    }

    // ==================== Content Functions ====================

    /**
     * Creates an attr() function for the CSS content property.
     * Retrieves an attribute value from the element.
     *
     * <p>Example:</p>
     * <pre>
     * // CSS: .tooltip::after { content: attr(data-tooltip); }
     * rule(cls("tooltip").after()).content(attrContent("data-tooltip"))
     * </pre>
     *
     * @param attributeName the attribute to retrieve
     * @return a CSSValue for the content property
     */
    public static CSSValue attrContent(String attributeName) {
        return () -> "attr(" + attributeName + ")";
    }

    /**
     * Creates a counter() function for CSS counters.
     *
     * <p>Example:</p>
     * <pre>
     * // Display section number
     * rule(cls("section").before()).content(counter("section"))
     * </pre>
     *
     * @param name the counter name
     * @return a CSSValue for the content property
     */
    public static CSSValue counter(String name) {
        return () -> "counter(" + name + ")";
    }

    /**
     * Creates a counter() function with a specific style.
     *
     * <p>Example:</p>
     * <pre>
     * // Roman numeral counter: I, II, III...
     * rule(cls("chapter").before()).content(counter("chapter", "upper-roman"))
     * </pre>
     *
     * @param name the counter name
     * @param style the counter style ("decimal", "lower-alpha", "upper-roman", etc.)
     * @return a CSSValue for the content property
     */
    public static CSSValue counter(String name, String style) {
        return () -> "counter(" + name + ", " + style + ")";
    }

    // ========== Helper Methods ==========

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
     * Extends {@link Style} to inherit all CSS property methods.
     * Used for both inline styles (via {@link #style()}) and CSS rules (via {@link #rule(String)}).
     *
     * <p>For inline styles:</p>
     * <pre>
     * div().style(style().padding(px(10)).color(red))
     * </pre>
     *
     * <p>For CSS rules:</p>
     * <pre>
     * rule(".btn").padding(px(10)).backgroundColor(blue).toRule()
     * // Output: .btn { padding: 10px; background-color: blue; }
     * </pre>
     *
     * @see #style() for inline styles
     * @see #rule(String) for CSS rules
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
         * Builds the CSS rule as a complete rule string with selector.
         * Only works when created via {@link CSS#rule(String)}.
         *
         * <p>Example:</p>
         * <pre>
         * rule(".btn").padding(px(10)).toRule()
         * // Output: .btn { padding: 10px; }
         * </pre>
         *
         * @return the complete CSS rule string
         * @throws IllegalStateException if called on an inline style (created via style())
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
