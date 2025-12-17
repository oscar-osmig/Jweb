package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for inline CSS styles.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.Styles.*;
 *   import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 *   import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *   import static com.osmig.Jweb.framework.styles.CSS.*;
 *
 *   div(attrs().style(
 *       style()
 *           .display(flex)
 *           .padding(rem(1))
 *           .background(hex("#f5f5f5"))
 *           .border(px(1), solid, gray)
 *   ))
 */
public class Style implements CSSValue {

    private final Map<String, String> properties = new LinkedHashMap<>();

    public Style() {}

    // ==================== CSS Variables ====================

    public Style var(String name, CSSValue value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value.css());
    }

    public Style var(String name, String value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value);
    }

    // ==================== Display & Box Model ====================

    public Style display(CSSValue value) { return prop("display", value); }

    public Style boxSizing(CSSValue value) { return prop("box-sizing", value); }

    // Width & Height
    public Style width(CSSValue value) { return prop("width", value); }
    public Style height(CSSValue value) { return prop("height", value); }
    public Style minWidth(CSSValue value) { return prop("min-width", value); }
    public Style maxWidth(CSSValue value) { return prop("max-width", value); }
    public Style minHeight(CSSValue value) { return prop("min-height", value); }
    public Style maxHeight(CSSValue value) { return prop("max-height", value); }

    // ==================== Margin ====================

    public Style margin(CSSValue all) {
        return prop("margin", all);
    }

    public Style margin(CSSValue vertical, CSSValue horizontal) {
        return prop("margin", vertical.css() + " " + horizontal.css());
    }

    public Style margin(CSSValue top, CSSValue horizontal, CSSValue bottom) {
        return prop("margin", top.css() + " " + horizontal.css() + " " + bottom.css());
    }

    public Style margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("margin", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    public Style marginTop(CSSValue value) { return prop("margin-top", value); }
    public Style marginRight(CSSValue value) { return prop("margin-right", value); }
    public Style marginBottom(CSSValue value) { return prop("margin-bottom", value); }
    public Style marginLeft(CSSValue value) { return prop("margin-left", value); }
    public Style marginX(CSSValue value) { return marginLeft(value).marginRight(value); }
    public Style marginY(CSSValue value) { return marginTop(value).marginBottom(value); }

    // ==================== Padding ====================

    public Style padding(CSSValue all) {
        return prop("padding", all);
    }

    public Style padding(CSSValue vertical, CSSValue horizontal) {
        return prop("padding", vertical.css() + " " + horizontal.css());
    }

    public Style padding(CSSValue top, CSSValue horizontal, CSSValue bottom) {
        return prop("padding", top.css() + " " + horizontal.css() + " " + bottom.css());
    }

    public Style padding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("padding", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    public Style paddingTop(CSSValue value) { return prop("padding-top", value); }
    public Style paddingRight(CSSValue value) { return prop("padding-right", value); }
    public Style paddingBottom(CSSValue value) { return prop("padding-bottom", value); }
    public Style paddingLeft(CSSValue value) { return prop("padding-left", value); }
    public Style paddingX(CSSValue value) { return paddingLeft(value).paddingRight(value); }
    public Style paddingY(CSSValue value) { return paddingTop(value).paddingBottom(value); }

    // ==================== Border ====================

    public Style border(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border", width.css() + " " + style.css() + " " + color.css());
    }

    public Style border(CSSValue value) { return prop("border", value); }

    public Style borderWidth(CSSValue value) { return prop("border-width", value); }
    public Style borderStyle(CSSValue value) { return prop("border-style", value); }
    public Style borderColor(CSSValue value) { return prop("border-color", value); }

    public Style borderTop(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-top", width.css() + " " + style.css() + " " + color.css());
    }
    public Style borderRight(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-right", width.css() + " " + style.css() + " " + color.css());
    }
    public Style borderBottom(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-bottom", width.css() + " " + style.css() + " " + color.css());
    }
    public Style borderLeft(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-left", width.css() + " " + style.css() + " " + color.css());
    }

    public Style borderRadius(CSSValue value) { return prop("border-radius", value); }

    public Style borderRadius(CSSValue topLeft, CSSValue topRight, CSSValue bottomRight, CSSValue bottomLeft) {
        return prop("border-radius", topLeft.css() + " " + topRight.css() + " " + bottomRight.css() + " " + bottomLeft.css());
    }

    public Style borderTopLeftRadius(CSSValue value) { return prop("border-top-left-radius", value); }
    public Style borderTopRightRadius(CSSValue value) { return prop("border-top-right-radius", value); }
    public Style borderBottomRightRadius(CSSValue value) { return prop("border-bottom-right-radius", value); }
    public Style borderBottomLeftRadius(CSSValue value) { return prop("border-bottom-left-radius", value); }

    // ==================== Background ====================

    public Style background(CSSValue value) { return prop("background", value); }
    public Style backgroundColor(CSSValue value) { return prop("background-color", value); }
    public Style backgroundImage(CSSValue value) { return prop("background-image", value); }
    public Style backgroundSize(CSSValue value) { return prop("background-size", value); }
    public Style backgroundPosition(CSSValue value) { return prop("background-position", value); }
    public Style backgroundRepeat(CSSValue value) { return prop("background-repeat", value); }
    public Style backgroundAttachment(CSSValue value) { return prop("background-attachment", value); }

    // ==================== Typography ====================

    public Style color(CSSValue value) { return prop("color", value); }

    public Style fontFamily(String value) { return prop("font-family", value); }
    public Style fontSize(CSSValue value) { return prop("font-size", value); }
    public Style fontWeight(CSSValue value) { return prop("font-weight", value); }
    public Style fontWeight(int value) { return prop("font-weight", String.valueOf(value)); }
    public Style fontStyle(CSSValue value) { return prop("font-style", value); }

    public Style lineHeight(CSSValue value) { return prop("line-height", value); }
    public Style lineHeight(double value) { return prop("line-height", String.valueOf(value)); }

    public Style letterSpacing(CSSValue value) { return prop("letter-spacing", value); }
    public Style wordSpacing(CSSValue value) { return prop("word-spacing", value); }

    public Style textAlign(CSSValue value) { return prop("text-align", value); }
    public Style textDecoration(CSSValue value) { return prop("text-decoration", value); }
    public Style textTransform(CSSValue value) { return prop("text-transform", value); }
    public Style textIndent(CSSValue value) { return prop("text-indent", value); }
    public Style textShadow(String value) { return prop("text-shadow", value); }

    public Style whiteSpace(CSSValue value) { return prop("white-space", value); }
    public Style wordBreak(CSSValue value) { return prop("word-break", value); }
    public Style overflowWrap(CSSValue value) { return prop("overflow-wrap", value); }

    // ==================== Flexbox ====================

    public Style flexDirection(CSSValue value) { return prop("flex-direction", value); }
    public Style flexWrap(CSSValue value) { return prop("flex-wrap", value); }
    public Style flexFlow(CSSValue direction, CSSValue wrap) {
        return prop("flex-flow", direction.css() + " " + wrap.css());
    }

    public Style justifyContent(CSSValue value) { return prop("justify-content", value); }
    public Style alignItems(CSSValue value) { return prop("align-items", value); }
    public Style alignContent(CSSValue value) { return prop("align-content", value); }
    public Style alignSelf(CSSValue value) { return prop("align-self", value); }

    public Style flex(CSSValue value) { return prop("flex", value); }
    public Style flex(int grow, int shrink, CSSValue basis) {
        return prop("flex", grow + " " + shrink + " " + basis.css());
    }
    public Style flexGrow(int value) { return prop("flex-grow", String.valueOf(value)); }
    public Style flexShrink(int value) { return prop("flex-shrink", String.valueOf(value)); }
    public Style flexBasis(CSSValue value) { return prop("flex-basis", value); }

    public Style gap(CSSValue value) { return prop("gap", value); }
    public Style gap(CSSValue row, CSSValue column) {
        return prop("gap", row.css() + " " + column.css());
    }
    public Style rowGap(CSSValue value) { return prop("row-gap", value); }
    public Style columnGap(CSSValue value) { return prop("column-gap", value); }

    public Style order(int value) { return prop("order", String.valueOf(value)); }

    // ==================== Grid ====================

    public Style gridTemplateColumns(String value) { return prop("grid-template-columns", value); }
    public Style gridTemplateRows(String value) { return prop("grid-template-rows", value); }
    public Style gridColumn(String value) { return prop("grid-column", value); }
    public Style gridRow(String value) { return prop("grid-row", value); }
    public Style gridArea(String value) { return prop("grid-area", value); }
    public Style gridAutoFlow(CSSValue value) { return prop("grid-auto-flow", value); }
    public Style justifyItems(CSSValue value) { return prop("justify-items", value); }
    public Style placeItems(CSSValue value) { return prop("place-items", value); }
    public Style placeContent(CSSValue value) { return prop("place-content", value); }

    // ==================== Position ====================

    public Style position(CSSValue value) { return prop("position", value); }
    public Style top(CSSValue value) { return prop("top", value); }
    public Style right(CSSValue value) { return prop("right", value); }
    public Style bottom(CSSValue value) { return prop("bottom", value); }
    public Style left(CSSValue value) { return prop("left", value); }
    public Style inset(CSSValue value) { return prop("inset", value); }
    public Style inset(CSSValue vertical, CSSValue horizontal) {
        return prop("inset", vertical.css() + " " + horizontal.css());
    }
    public Style zIndex(int value) { return prop("z-index", String.valueOf(value)); }

    // ==================== Overflow ====================

    public Style overflow(CSSValue value) { return prop("overflow", value); }
    public Style overflowX(CSSValue value) { return prop("overflow-x", value); }
    public Style overflowY(CSSValue value) { return prop("overflow-y", value); }

    // ==================== Visibility & Opacity ====================

    public Style visibility(CSSValue value) { return prop("visibility", value); }
    public Style opacity(double value) { return prop("opacity", String.valueOf(value)); }

    // ==================== Cursor & Interaction ====================

    public Style cursor(CSSValue value) { return prop("cursor", value); }
    public Style pointerEvents(CSSValue value) { return prop("pointer-events", value); }
    public Style userSelect(CSSValue value) { return prop("user-select", value); }
    public Style resize(CSSValue value) { return prop("resize", value); }

    // ==================== Transform ====================

    public Style transform(CSSValue... transforms) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < transforms.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(transforms[i].css());
        }
        return prop("transform", sb.toString());
    }

    public Style transformOrigin(String value) { return prop("transform-origin", value); }

    // ==================== Transition ====================

    public Style transition(String property, CSSValue duration, CSSValue timing) {
        return prop("transition", property + " " + duration.css() + " " + timing.css());
    }

    public Style transition(String property, CSSValue duration) {
        return prop("transition", property + " " + duration.css());
    }

    public Style transition(String value) { return prop("transition", value); }
    public Style transitionProperty(String value) { return prop("transition-property", value); }
    public Style transitionDuration(CSSValue value) { return prop("transition-duration", value); }
    public Style transitionTimingFunction(CSSValue value) { return prop("transition-timing-function", value); }
    public Style transitionDelay(CSSValue value) { return prop("transition-delay", value); }

    // ==================== Animation ====================

    public Style animation(String value) { return prop("animation", value); }
    public Style animationName(String value) { return prop("animation-name", value); }
    public Style animationDuration(CSSValue value) { return prop("animation-duration", value); }
    public Style animationTimingFunction(CSSValue value) { return prop("animation-timing-function", value); }
    public Style animationDelay(CSSValue value) { return prop("animation-delay", value); }
    public Style animationIterationCount(String value) { return prop("animation-iteration-count", value); }
    public Style animationDirection(String value) { return prop("animation-direction", value); }
    public Style animationFillMode(String value) { return prop("animation-fill-mode", value); }

    // ==================== Box Shadow ====================

    public Style boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue color) {
        return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + color.css());
    }

    public Style boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue spread, CSSValue color) {
        return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + spread.css() + " " + color.css());
    }

    public Style boxShadow(String value) { return prop("box-shadow", value); }

    // ==================== Outline ====================

    public Style outline(CSSValue width, CSSValue style, CSSValue color) {
        return prop("outline", width.css() + " " + style.css() + " " + color.css());
    }

    public Style outline(CSSValue value) { return prop("outline", value); }
    public Style outlineOffset(CSSValue value) { return prop("outline-offset", value); }

    // ==================== List ====================

    public Style listStyle(CSSValue value) { return prop("list-style", value); }
    public Style listStyleType(CSSValue value) { return prop("list-style-type", value); }
    public Style listStylePosition(CSSValue value) { return prop("list-style-position", value); }

    // ==================== Object Fit ====================

    public Style objectFit(CSSValue value) { return prop("object-fit", value); }
    public Style objectPosition(String value) { return prop("object-position", value); }

    // ==================== Filter ====================

    public Style filter(String value) { return prop("filter", value); }
    public Style backdropFilter(String value) { return prop("backdrop-filter", value); }

    // ==================== Aspect Ratio ====================

    public Style aspectRatio(String value) { return prop("aspect-ratio", value); }
    public Style aspectRatio(int width, int height) {
        return prop("aspect-ratio", width + " / " + height);
    }

    // ==================== Scroll ====================

    public Style scrollBehavior(CSSValue value) { return prop("scroll-behavior", value); }
    public Style scrollMargin(CSSValue value) { return prop("scroll-margin", value); }
    public Style scrollPadding(CSSValue value) { return prop("scroll-padding", value); }

    // ==================== Table ====================

    public Style borderCollapse(CSSValue value) { return prop("border-collapse", value); }
    public Style borderSpacing(CSSValue value) { return prop("border-spacing", value); }
    public Style tableLayout(CSSValue value) { return prop("table-layout", value); }
    public Style verticalAlign(CSSValue value) { return prop("vertical-align", value); }

    // ==================== Raw property ====================

    public Style prop(String name, CSSValue value) {
        properties.put(name, value.css());
        return this;
    }

    public Style prop(String name, String value) {
        properties.put(name, value);
        return this;
    }

    // ==================== Build ====================

    @Override
    public String css() {
        return build();
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    public Map<String, String> toMap() {
        return Map.copyOf(properties);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public String toString() {
        return build();
    }
}
