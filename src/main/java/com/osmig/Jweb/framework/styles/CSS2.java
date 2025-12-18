package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified CSS DSL with direct property methods.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSS2.*;
 *
 * // Single rule
 * String css = rule(".btn")
 *     .padding(px(10))
 *     .color(red)
 *     .backgroundColor(blue)
 *     .build();
 *
 * // Multiple rules
 * String css = styles(
 *     rule("*").boxSizing(borderBox),
 *     rule("body").margin(zero).fontFamily("system-ui"),
 *     rule(".container").maxWidth(px(1200)).margin(zero, auto),
 *     rule("a").color(blue).textDecoration(none),
 *     rule("a:hover").textDecoration(underline)
 * );
 * </pre>
 */
public class CSS2 {

    private CSS2() {}

    /**
     * Create a CSS rule for a selector.
     */
    public static CSSRule rule(String selector) {
        return new CSSRule(selector);
    }

    /**
     * Combine multiple CSS rules into a stylesheet.
     */
    public static String styles(CSSRule... rules) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rules.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(rules[i].build());
        }
        return sb.toString();
    }

    /**
     * A CSS rule with fluent property methods.
     */
    public static class CSSRule {
        private final String selector;
        private final Map<String, String> properties = new LinkedHashMap<>();

        CSSRule(String selector) {
            this.selector = selector;
        }

        // ==================== Display & Box Model ====================

        public CSSRule display(CSSValue value) { return prop("display", value); }
        public CSSRule boxSizing(CSSValue value) { return prop("box-sizing", value); }

        // Width & Height
        public CSSRule width(CSSValue value) { return prop("width", value); }
        public CSSRule height(CSSValue value) { return prop("height", value); }
        public CSSRule minWidth(CSSValue value) { return prop("min-width", value); }
        public CSSRule maxWidth(CSSValue value) { return prop("max-width", value); }
        public CSSRule minHeight(CSSValue value) { return prop("min-height", value); }
        public CSSRule maxHeight(CSSValue value) { return prop("max-height", value); }

        // ==================== Margin ====================

        public CSSRule margin(CSSValue all) { return prop("margin", all); }
        public CSSRule margin(CSSValue vertical, CSSValue horizontal) {
            return prop("margin", vertical.css() + " " + horizontal.css());
        }
        public CSSRule margin(CSSValue top, CSSValue horizontal, CSSValue bottom) {
            return prop("margin", top.css() + " " + horizontal.css() + " " + bottom.css());
        }
        public CSSRule margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
            return prop("margin", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
        }
        public CSSRule marginTop(CSSValue value) { return prop("margin-top", value); }
        public CSSRule marginRight(CSSValue value) { return prop("margin-right", value); }
        public CSSRule marginBottom(CSSValue value) { return prop("margin-bottom", value); }
        public CSSRule marginLeft(CSSValue value) { return prop("margin-left", value); }

        // ==================== Padding ====================

        public CSSRule padding(CSSValue all) { return prop("padding", all); }
        public CSSRule padding(CSSValue vertical, CSSValue horizontal) {
            return prop("padding", vertical.css() + " " + horizontal.css());
        }
        public CSSRule padding(CSSValue top, CSSValue horizontal, CSSValue bottom) {
            return prop("padding", top.css() + " " + horizontal.css() + " " + bottom.css());
        }
        public CSSRule padding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
            return prop("padding", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
        }
        public CSSRule paddingTop(CSSValue value) { return prop("padding-top", value); }
        public CSSRule paddingRight(CSSValue value) { return prop("padding-right", value); }
        public CSSRule paddingBottom(CSSValue value) { return prop("padding-bottom", value); }
        public CSSRule paddingLeft(CSSValue value) { return prop("padding-left", value); }

        // ==================== Border ====================

        public CSSRule border(CSSValue width, CSSValue style, CSSValue color) {
            return prop("border", width.css() + " " + style.css() + " " + color.css());
        }
        public CSSRule border(CSSValue value) { return prop("border", value); }
        public CSSRule borderWidth(CSSValue value) { return prop("border-width", value); }
        public CSSRule borderStyle(CSSValue value) { return prop("border-style", value); }
        public CSSRule borderColor(CSSValue value) { return prop("border-color", value); }
        public CSSRule borderRadius(CSSValue value) { return prop("border-radius", value); }
        public CSSRule borderRadius(CSSValue topLeft, CSSValue topRight, CSSValue bottomRight, CSSValue bottomLeft) {
            return prop("border-radius", topLeft.css() + " " + topRight.css() + " " + bottomRight.css() + " " + bottomLeft.css());
        }

        // ==================== Background ====================

        public CSSRule background(CSSValue value) { return prop("background", value); }
        public CSSRule backgroundColor(CSSValue value) { return prop("background-color", value); }
        public CSSRule backgroundImage(CSSValue value) { return prop("background-image", value); }
        public CSSRule backgroundSize(CSSValue value) { return prop("background-size", value); }
        public CSSRule backgroundPosition(CSSValue value) { return prop("background-position", value); }
        public CSSRule backgroundRepeat(CSSValue value) { return prop("background-repeat", value); }

        // ==================== Typography ====================

        public CSSRule color(CSSValue value) { return prop("color", value); }
        public CSSRule fontFamily(String value) { return prop("font-family", value); }
        public CSSRule fontSize(CSSValue value) { return prop("font-size", value); }
        public CSSRule fontWeight(CSSValue value) { return prop("font-weight", value); }
        public CSSRule fontWeight(int value) { return prop("font-weight", String.valueOf(value)); }
        public CSSRule fontStyle(CSSValue value) { return prop("font-style", value); }
        public CSSRule lineHeight(CSSValue value) { return prop("line-height", value); }
        public CSSRule lineHeight(double value) { return prop("line-height", String.valueOf(value)); }
        public CSSRule letterSpacing(CSSValue value) { return prop("letter-spacing", value); }
        public CSSRule textAlign(CSSValue value) { return prop("text-align", value); }
        public CSSRule textDecoration(CSSValue value) { return prop("text-decoration", value); }
        public CSSRule textTransform(CSSValue value) { return prop("text-transform", value); }
        public CSSRule whiteSpace(CSSValue value) { return prop("white-space", value); }

        // ==================== Flexbox ====================

        public CSSRule flexDirection(CSSValue value) { return prop("flex-direction", value); }
        public CSSRule flexWrap(CSSValue value) { return prop("flex-wrap", value); }
        public CSSRule justifyContent(CSSValue value) { return prop("justify-content", value); }
        public CSSRule alignItems(CSSValue value) { return prop("align-items", value); }
        public CSSRule alignContent(CSSValue value) { return prop("align-content", value); }
        public CSSRule alignSelf(CSSValue value) { return prop("align-self", value); }
        public CSSRule flex(CSSValue value) { return prop("flex", value); }
        public CSSRule flex(int grow, int shrink, CSSValue basis) {
            return prop("flex", grow + " " + shrink + " " + basis.css());
        }
        public CSSRule flexGrow(int value) { return prop("flex-grow", String.valueOf(value)); }
        public CSSRule flexShrink(int value) { return prop("flex-shrink", String.valueOf(value)); }
        public CSSRule flexBasis(CSSValue value) { return prop("flex-basis", value); }
        public CSSRule gap(CSSValue value) { return prop("gap", value); }
        public CSSRule gap(CSSValue row, CSSValue column) {
            return prop("gap", row.css() + " " + column.css());
        }
        public CSSRule order(int value) { return prop("order", String.valueOf(value)); }

        // ==================== Grid ====================

        public CSSRule gridTemplateColumns(String value) { return prop("grid-template-columns", value); }
        public CSSRule gridTemplateRows(String value) { return prop("grid-template-rows", value); }
        public CSSRule gridColumn(String value) { return prop("grid-column", value); }
        public CSSRule gridRow(String value) { return prop("grid-row", value); }
        public CSSRule gridArea(String value) { return prop("grid-area", value); }

        // ==================== Position ====================

        public CSSRule position(CSSValue value) { return prop("position", value); }
        public CSSRule top(CSSValue value) { return prop("top", value); }
        public CSSRule right(CSSValue value) { return prop("right", value); }
        public CSSRule bottom(CSSValue value) { return prop("bottom", value); }
        public CSSRule left(CSSValue value) { return prop("left", value); }
        public CSSRule inset(CSSValue value) { return prop("inset", value); }
        public CSSRule zIndex(int value) { return prop("z-index", String.valueOf(value)); }

        // ==================== Overflow ====================

        public CSSRule overflow(CSSValue value) { return prop("overflow", value); }
        public CSSRule overflowX(CSSValue value) { return prop("overflow-x", value); }
        public CSSRule overflowY(CSSValue value) { return prop("overflow-y", value); }

        // ==================== Visibility & Opacity ====================

        public CSSRule visibility(CSSValue value) { return prop("visibility", value); }
        public CSSRule opacity(double value) { return prop("opacity", String.valueOf(value)); }

        // ==================== Cursor & Interaction ====================

        public CSSRule cursor(CSSValue value) { return prop("cursor", value); }
        public CSSRule pointerEvents(CSSValue value) { return prop("pointer-events", value); }
        public CSSRule userSelect(CSSValue value) { return prop("user-select", value); }

        // ==================== Transform ====================

        public CSSRule transform(CSSValue... transforms) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < transforms.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(transforms[i].css());
            }
            return prop("transform", sb.toString());
        }

        // ==================== Transition ====================

        public CSSRule transition(String property, CSSValue duration, CSSValue timing) {
            return prop("transition", property + " " + duration.css() + " " + timing.css());
        }
        public CSSRule transition(String property, CSSValue duration) {
            return prop("transition", property + " " + duration.css());
        }
        public CSSRule transition(String value) { return prop("transition", value); }

        // ==================== Animation ====================

        public CSSRule animation(String value) { return prop("animation", value); }

        // ==================== Box Shadow ====================

        public CSSRule boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue color) {
            return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + color.css());
        }
        public CSSRule boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue spread, CSSValue color) {
            return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + spread.css() + " " + color.css());
        }
        public CSSRule boxShadow(String value) { return prop("box-shadow", value); }

        // ==================== List ====================

        public CSSRule listStyle(CSSValue value) { return prop("list-style", value); }
        public CSSRule listStyleType(CSSValue value) { return prop("list-style-type", value); }

        // ==================== Table ====================

        public CSSRule borderCollapse(CSSValue value) { return prop("border-collapse", value); }
        public CSSRule verticalAlign(CSSValue value) { return prop("vertical-align", value); }

        // ==================== Object Fit ====================

        public CSSRule objectFit(CSSValue value) { return prop("object-fit", value); }

        // ==================== Outline ====================

        public CSSRule outline(CSSValue width, CSSValue style, CSSValue color) {
            return prop("outline", width.css() + " " + style.css() + " " + color.css());
        }
        public CSSRule outline(CSSValue value) { return prop("outline", value); }

        // ==================== Raw property ====================

        public CSSRule prop(String name, CSSValue value) {
            properties.put(name, value.css());
            return this;
        }

        public CSSRule prop(String name, String value) {
            properties.put(name, value);
            return this;
        }

        // ==================== Build ====================

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append(selector).append(" { ");
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }
}
