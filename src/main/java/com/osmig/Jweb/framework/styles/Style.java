package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base fluent builder for CSS styles.
 * Uses a self-referential generic type to enable proper method chaining in subclasses.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.CSS.*;
 *   import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 *   import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 *   // For inline styles (via StyleBuilder)
 *   style().display(flex).padding(rem(1)).color(red)
 *
 *   // For CSS rules (via StyleBuilder)
 *   rule(".btn").padding(px(10)).backgroundColor(blue)
 *
 * @param <T> The concrete type (for fluent method chaining)
 */
@SuppressWarnings("unchecked")
public class Style<T extends Style<T>> implements CSSValue {

    protected final Map<String, String> properties = new LinkedHashMap<>();

    public Style() {}

    protected T self() {
        return (T) this;
    }

    // ==================== CSS Variables ====================

    public T var(String name, CSSValue value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value.css());
    }

    public T var(String name, String value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value);
    }

    // ==================== Display & Box Model ====================

    public T display(CSSValue value) { return prop("display", value); }

    public T boxSizing(CSSValue value) { return prop("box-sizing", value); }

    // Width & Height
    public T width(CSSValue value) { return prop("width", value); }
    public T height(CSSValue value) { return prop("height", value); }
    public T minWidth(CSSValue value) { return prop("min-width", value); }
    public T maxWidth(CSSValue value) { return prop("max-width", value); }
    public T minHeight(CSSValue value) { return prop("min-height", value); }
    public T maxHeight(CSSValue value) { return prop("max-height", value); }

    // ==================== Margin ====================

    public T margin(CSSValue all) {
        return prop("margin", all);
    }

    public T margin(CSSValue vertical, CSSValue horizontal) {
        return prop("margin", vertical.css() + " " + horizontal.css());
    }

    public T margin(CSSValue top, CSSValue horizontal, CSSValue bottom) {
        return prop("margin", top.css() + " " + horizontal.css() + " " + bottom.css());
    }

    public T margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("margin", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    public T marginTop(CSSValue value) { return prop("margin-top", value); }
    public T marginRight(CSSValue value) { return prop("margin-right", value); }
    public T marginBottom(CSSValue value) { return prop("margin-bottom", value); }
    public T marginLeft(CSSValue value) { return prop("margin-left", value); }
    public T marginX(CSSValue value) { return marginLeft(value).marginRight(value); }
    public T marginY(CSSValue value) { return marginTop(value).marginBottom(value); }

    // ==================== Padding ====================

    public T padding(CSSValue all) {
        return prop("padding", all);
    }

    public T padding(CSSValue vertical, CSSValue horizontal) {
        return prop("padding", vertical.css() + " " + horizontal.css());
    }

    public T padding(CSSValue top, CSSValue horizontal, CSSValue bottom) {
        return prop("padding", top.css() + " " + horizontal.css() + " " + bottom.css());
    }

    public T padding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("padding", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    public T paddingTop(CSSValue value) { return prop("padding-top", value); }
    public T paddingRight(CSSValue value) { return prop("padding-right", value); }
    public T paddingBottom(CSSValue value) { return prop("padding-bottom", value); }
    public T paddingLeft(CSSValue value) { return prop("padding-left", value); }
    public T paddingX(CSSValue value) { return paddingLeft(value).paddingRight(value); }
    public T paddingY(CSSValue value) { return paddingTop(value).paddingBottom(value); }

    // ==================== Border ====================

    public T border(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border", width.css() + " " + style.css() + " " + color.css());
    }

    public T border(CSSValue value) { return prop("border", value); }

    public T borderWidth(CSSValue value) { return prop("border-width", value); }
    public T borderStyle(CSSValue value) { return prop("border-style", value); }
    public T borderColor(CSSValue value) { return prop("border-color", value); }

    public T borderTop(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-top", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderRight(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-right", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderBottom(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-bottom", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderLeft(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-left", width.css() + " " + style.css() + " " + color.css());
    }

    public T borderRadius(CSSValue value) { return prop("border-radius", value); }

    public T borderRadius(CSSValue topLeft, CSSValue topRight, CSSValue bottomRight, CSSValue bottomLeft) {
        return prop("border-radius", topLeft.css() + " " + topRight.css() + " " + bottomRight.css() + " " + bottomLeft.css());
    }

    public T borderTopLeftRadius(CSSValue value) { return prop("border-top-left-radius", value); }
    public T borderTopRightRadius(CSSValue value) { return prop("border-top-right-radius", value); }
    public T borderBottomRightRadius(CSSValue value) { return prop("border-bottom-right-radius", value); }
    public T borderBottomLeftRadius(CSSValue value) { return prop("border-bottom-left-radius", value); }

    // ==================== Background ====================

    public T background(CSSValue value) { return prop("background", value); }
    public T backgroundColor(CSSValue value) { return prop("background-color", value); }
    public T backgroundImage(CSSValue value) { return prop("background-image", value); }
    public T backgroundSize(CSSValue value) { return prop("background-size", value); }
    public T backgroundSize(CSSValue width, CSSValue height) {
        return prop("background-size", width.css() + " " + height.css());
    }
    public T backgroundPosition(CSSValue value) { return prop("background-position", value); }
    public T backgroundRepeat(CSSValue value) { return prop("background-repeat", value); }
    public T backgroundAttachment(CSSValue value) { return prop("background-attachment", value); }

    // ==================== Typography ====================

    public T color(CSSValue value) { return prop("color", value); }

    public T fontFamily(String value) { return prop("font-family", value); }
    public T fontSize(CSSValue value) { return prop("font-size", value); }
    public T fontWeight(CSSValue value) { return prop("font-weight", value); }
    public T fontWeight(int value) { return prop("font-weight", String.valueOf(value)); }
    public T fontStyle(CSSValue value) { return prop("font-style", value); }

    public T lineHeight(CSSValue value) { return prop("line-height", value); }
    public T lineHeight(double value) { return prop("line-height", String.valueOf(value)); }

    public T letterSpacing(CSSValue value) { return prop("letter-spacing", value); }
    public T wordSpacing(CSSValue value) { return prop("word-spacing", value); }

    public T textAlign(CSSValue value) { return prop("text-align", value); }
    public T textDecoration(CSSValue value) { return prop("text-decoration", value); }
    public T textTransform(CSSValue value) { return prop("text-transform", value); }
    public T textIndent(CSSValue value) { return prop("text-indent", value); }
    public T textShadow(String value) { return prop("text-shadow", value); }

    public T whiteSpace(CSSValue value) { return prop("white-space", value); }
    public T wordBreak(CSSValue value) { return prop("word-break", value); }
    public T overflowWrap(CSSValue value) { return prop("overflow-wrap", value); }

    // ==================== Flexbox ====================

    public T flexDirection(CSSValue value) { return prop("flex-direction", value); }
    public T flexWrap(CSSValue value) { return prop("flex-wrap", value); }
    public T flexFlow(CSSValue direction, CSSValue wrap) {
        return prop("flex-flow", direction.css() + " " + wrap.css());
    }

    public T justifyContent(CSSValue value) { return prop("justify-content", value); }
    public T alignItems(CSSValue value) { return prop("align-items", value); }
    public T alignContent(CSSValue value) { return prop("align-content", value); }
    public T alignSelf(CSSValue value) { return prop("align-self", value); }

    public T flex(CSSValue value) { return prop("flex", value); }
    public T flex(int grow, int shrink, CSSValue basis) {
        return prop("flex", grow + " " + shrink + " " + basis.css());
    }
    public T flexGrow(int value) { return prop("flex-grow", String.valueOf(value)); }
    public T flexShrink(int value) { return prop("flex-shrink", String.valueOf(value)); }
    public T flexBasis(CSSValue value) { return prop("flex-basis", value); }

    public T gap(CSSValue value) { return prop("gap", value); }
    public T gap(CSSValue row, CSSValue column) {
        return prop("gap", row.css() + " " + column.css());
    }
    public T rowGap(CSSValue value) { return prop("row-gap", value); }
    public T columnGap(CSSValue value) { return prop("column-gap", value); }

    public T order(int value) { return prop("order", String.valueOf(value)); }

    // ==================== Grid ====================

    public T gridTemplateColumns(String value) { return prop("grid-template-columns", value); }
    public T gridTemplateRows(String value) { return prop("grid-template-rows", value); }
    public T gridColumn(String value) { return prop("grid-column", value); }
    public T gridRow(String value) { return prop("grid-row", value); }
    public T gridArea(String value) { return prop("grid-area", value); }
    public T gridAutoFlow(CSSValue value) { return prop("grid-auto-flow", value); }
    public T justifyItems(CSSValue value) { return prop("justify-items", value); }
    public T placeItems(CSSValue value) { return prop("place-items", value); }
    public T placeContent(CSSValue value) { return prop("place-content", value); }

    // ==================== Position ====================

    public T position(CSSValue value) { return prop("position", value); }
    public T top(CSSValue value) { return prop("top", value); }
    public T right(CSSValue value) { return prop("right", value); }
    public T bottom(CSSValue value) { return prop("bottom", value); }
    public T left(CSSValue value) { return prop("left", value); }
    public T inset(CSSValue value) { return prop("inset", value); }
    public T inset(CSSValue vertical, CSSValue horizontal) {
        return prop("inset", vertical.css() + " " + horizontal.css());
    }
    public T zIndex(int value) { return prop("z-index", String.valueOf(value)); }

    // ==================== Overflow ====================

    public T overflow(CSSValue value) { return prop("overflow", value); }
    public T overflowX(CSSValue value) { return prop("overflow-x", value); }
    public T overflowY(CSSValue value) { return prop("overflow-y", value); }

    // ==================== Visibility & Opacity ====================

    public T visibility(CSSValue value) { return prop("visibility", value); }
    public T opacity(double value) { return prop("opacity", String.valueOf(value)); }

    // ==================== Cursor & Interaction ====================

    public T cursor(CSSValue value) { return prop("cursor", value); }
    public T pointerEvents(CSSValue value) { return prop("pointer-events", value); }
    public T userSelect(CSSValue value) { return prop("user-select", value); }
    public T resize(CSSValue value) { return prop("resize", value); }

    // ==================== Transform ====================

    public T transform(CSSValue... transforms) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < transforms.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(transforms[i].css());
        }
        return prop("transform", sb.toString());
    }

    public T transformOrigin(String value) { return prop("transform-origin", value); }

    // ==================== Transition ====================

    public T transition(CSSValue property, CSSValue duration, CSSValue timing) {
        return prop("transition", property.css() + " " + duration.css() + " " + timing.css());
    }

    public T transition(CSSValue property, CSSValue duration) {
        return prop("transition", property.css() + " " + duration.css());
    }

    public T transition(CSSValue value) { return prop("transition", value); }
    public T transitionProperty(CSSValue value) { return prop("transition-property", value); }
    public T transitionDuration(CSSValue value) { return prop("transition-duration", value); }
    public T transitionTimingFunction(CSSValue value) { return prop("transition-timing-function", value); }
    public T transitionDelay(CSSValue value) { return prop("transition-delay", value); }

    // ==================== Animation ====================

    public T animation(CSSValue name, CSSValue duration, CSSValue timing) {
        return prop("animation", name.css() + " " + duration.css() + " " + timing.css());
    }
    public T animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay) {
        return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css());
    }
    public T animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount) {
        return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css());
    }
    public T animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction) {
        return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css() + " " + direction.css());
    }
    public T animation(CSSValue name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction, CSSValue fillMode) {
        return prop("animation", name.css() + " " + duration.css() + " " + timing.css() + " " + delay.css() + " " + iterationCount.css() + " " + direction.css() + " " + fillMode.css());
    }
    public T animationName(CSSValue value) { return prop("animation-name", value); }
    public T animationDuration(CSSValue value) { return prop("animation-duration", value); }
    public T animationTimingFunction(CSSValue value) { return prop("animation-timing-function", value); }
    public T animationDelay(CSSValue value) { return prop("animation-delay", value); }
    public T animationIterationCount(CSSValue value) { return prop("animation-iteration-count", value); }
    public T animationDirection(CSSValue value) { return prop("animation-direction", value); }
    public T animationFillMode(CSSValue value) { return prop("animation-fill-mode", value); }

    // ==================== Box Shadow ====================

    public T boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue color) {
        return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + color.css());
    }

    public T boxShadow(CSSValue offsetX, CSSValue offsetY, CSSValue blur, CSSValue spread, CSSValue color) {
        return prop("box-shadow", offsetX.css() + " " + offsetY.css() + " " + blur.css() + " " + spread.css() + " " + color.css());
    }

    public T boxShadow(String value) { return prop("box-shadow", value); }

    // ==================== Outline ====================

    public T outline(CSSValue width, CSSValue style, CSSValue color) {
        return prop("outline", width.css() + " " + style.css() + " " + color.css());
    }

    public T outline(CSSValue value) { return prop("outline", value); }
    public T outlineOffset(CSSValue value) { return prop("outline-offset", value); }

    // ==================== List ====================

    public T listStyle(CSSValue value) { return prop("list-style", value); }
    public T listStyleType(CSSValue value) { return prop("list-style-type", value); }
    public T listStylePosition(CSSValue value) { return prop("list-style-position", value); }

    // ==================== Object Fit ====================

    public T objectFit(CSSValue value) { return prop("object-fit", value); }
    public T objectPosition(String value) { return prop("object-position", value); }

    // ==================== Filter ====================

    public T filter(String value) { return prop("filter", value); }
    public T backdropFilter(String value) { return prop("backdrop-filter", value); }

    public T filter(CSSValue... filters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filters.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(filters[i].css());
        }
        return prop("filter", sb.toString());
    }

    public T backdropFilter(CSSValue... filters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filters.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(filters[i].css());
        }
        return prop("backdrop-filter", sb.toString());
    }

    // ==================== Clip Path ====================

    public T clipPath(CSSValue value) { return prop("clip-path", value); }

    // ==================== Aspect Ratio ====================

    public T aspectRatio(String value) { return prop("aspect-ratio", value); }
    public T aspectRatio(int width, int height) {
        return prop("aspect-ratio", width + " / " + height);
    }

    // ==================== Scroll ====================

    public T scrollBehavior(CSSValue value) { return prop("scroll-behavior", value); }
    public T scrollMargin(CSSValue value) { return prop("scroll-margin", value); }
    public T scrollPadding(CSSValue value) { return prop("scroll-padding", value); }

    // ==================== Table ====================

    public T borderCollapse(CSSValue value) { return prop("border-collapse", value); }
    public T borderSpacing(CSSValue value) { return prop("border-spacing", value); }
    public T tableLayout(CSSValue value) { return prop("table-layout", value); }
    public T verticalAlign(CSSValue value) { return prop("vertical-align", value); }

    // ==================== Font Smoothing (Webkit) ====================

    public T webkitFontSmoothing(CSSValue value) { return prop("-webkit-font-smoothing", value); }
    public T mozOsxFontSmoothing(CSSValue value) { return prop("-moz-osx-font-smoothing", value); }

    // ==================== Background Clip ====================

    public T backgroundClip(CSSValue value) { return prop("background-clip", value); }
    public T webkitBackgroundClip(CSSValue value) { return prop("-webkit-background-clip", value); }
    public T webkitTextFillColor(CSSValue value) { return prop("-webkit-text-fill-color", value); }

    // ==================== Content ====================

    public T content(String value) { return prop("content", "'" + value + "'"); }
    public T content(CSSValue value) { return prop("content", value); }

    // ==================== Logical Properties ====================

    // Margin logical properties
    public T marginInline(CSSValue value) { return prop("margin-inline", value); }
    public T marginInline(CSSValue start, CSSValue end) {
        return prop("margin-inline", start.css() + " " + end.css());
    }
    public T marginInlineStart(CSSValue value) { return prop("margin-inline-start", value); }
    public T marginInlineEnd(CSSValue value) { return prop("margin-inline-end", value); }
    public T marginBlock(CSSValue value) { return prop("margin-block", value); }
    public T marginBlock(CSSValue start, CSSValue end) {
        return prop("margin-block", start.css() + " " + end.css());
    }
    public T marginBlockStart(CSSValue value) { return prop("margin-block-start", value); }
    public T marginBlockEnd(CSSValue value) { return prop("margin-block-end", value); }

    // Padding logical properties
    public T paddingInline(CSSValue value) { return prop("padding-inline", value); }
    public T paddingInline(CSSValue start, CSSValue end) {
        return prop("padding-inline", start.css() + " " + end.css());
    }
    public T paddingInlineStart(CSSValue value) { return prop("padding-inline-start", value); }
    public T paddingInlineEnd(CSSValue value) { return prop("padding-inline-end", value); }
    public T paddingBlock(CSSValue value) { return prop("padding-block", value); }
    public T paddingBlock(CSSValue start, CSSValue end) {
        return prop("padding-block", start.css() + " " + end.css());
    }
    public T paddingBlockStart(CSSValue value) { return prop("padding-block-start", value); }
    public T paddingBlockEnd(CSSValue value) { return prop("padding-block-end", value); }

    // Size logical properties
    public T inlineSize(CSSValue value) { return prop("inline-size", value); }
    public T blockSize(CSSValue value) { return prop("block-size", value); }
    public T minInlineSize(CSSValue value) { return prop("min-inline-size", value); }
    public T maxInlineSize(CSSValue value) { return prop("max-inline-size", value); }
    public T minBlockSize(CSSValue value) { return prop("min-block-size", value); }
    public T maxBlockSize(CSSValue value) { return prop("max-block-size", value); }

    // Position logical properties
    public T insetInline(CSSValue value) { return prop("inset-inline", value); }
    public T insetInline(CSSValue start, CSSValue end) {
        return prop("inset-inline", start.css() + " " + end.css());
    }
    public T insetInlineStart(CSSValue value) { return prop("inset-inline-start", value); }
    public T insetInlineEnd(CSSValue value) { return prop("inset-inline-end", value); }
    public T insetBlock(CSSValue value) { return prop("inset-block", value); }
    public T insetBlock(CSSValue start, CSSValue end) {
        return prop("inset-block", start.css() + " " + end.css());
    }
    public T insetBlockStart(CSSValue value) { return prop("inset-block-start", value); }
    public T insetBlockEnd(CSSValue value) { return prop("inset-block-end", value); }

    // Border logical properties
    public T borderInline(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-inline", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderInlineStart(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-inline-start", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderInlineEnd(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-inline-end", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderBlock(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-block", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderBlockStart(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-block-start", width.css() + " " + style.css() + " " + color.css());
    }
    public T borderBlockEnd(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border-block-end", width.css() + " " + style.css() + " " + color.css());
    }

    // Border radius logical properties
    public T borderStartStartRadius(CSSValue value) { return prop("border-start-start-radius", value); }
    public T borderStartEndRadius(CSSValue value) { return prop("border-start-end-radius", value); }
    public T borderEndStartRadius(CSSValue value) { return prop("border-end-start-radius", value); }
    public T borderEndEndRadius(CSSValue value) { return prop("border-end-end-radius", value); }

    // Text alignment logical
    public T textAlignLast(CSSValue value) { return prop("text-align-last", value); }

    // ==================== Container Queries ====================

    public T containerType(CSSValue value) { return prop("container-type", value); }
    public T containerName(String name) { return prop("container-name", name); }
    public T container(String name, CSSValue type) {
        return prop("container", name + " / " + type.css());
    }

    // ==================== Raw property ====================

    public T prop(String name, CSSValue value) {
        properties.put(name, value.css());
        return self();
    }

    public T prop(String name, String value) {
        properties.put(name, value);
        return self();
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
