package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base fluent builder for CSS styles.
 * Provides methods for all common CSS properties with type-safe values.
 * Uses a self-referential generic type to enable proper method chaining in subclasses.
 *
 * <p>Usage with static imports:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 * // For inline styles (via StyleBuilder)
 * div().style(style()
 *     .display(flex)
 *     .padding(rem(1))
 *     .color(red)
 * )
 *
 * // For CSS rules (via StyleBuilder)
 * rule(".btn")
 *     .padding(px(10), px(20))
 *     .backgroundColor(blue)
 *     .color(white)
 *     .borderRadius(px(4))
 *     .cursor(pointer)
 *     .transition(propAll, s(0.2), ease)
 * </pre>
 *
 * <p>This class provides methods for:</p>
 * <ul>
 *   <li><b>Box Model:</b> margin, padding, border, width, height</li>
 *   <li><b>Flexbox:</b> display(flex), flexDirection, justifyContent, alignItems, gap</li>
 *   <li><b>Grid:</b> gridTemplateColumns, gridTemplateRows, gridArea</li>
 *   <li><b>Positioning:</b> position, top/right/bottom/left, inset, zIndex</li>
 *   <li><b>Typography:</b> color, fontSize, fontWeight, lineHeight, textAlign</li>
 *   <li><b>Background:</b> background, backgroundColor, backgroundImage</li>
 *   <li><b>Effects:</b> transform, transition, animation, boxShadow, filter</li>
 *   <li><b>Logical Properties:</b> marginInline, paddingBlock, insetInline, etc.</li>
 * </ul>
 *
 * @param <T> The concrete type (for fluent method chaining)
 * @see CSS#style() for creating inline styles
 * @see CSS#rule(String) for creating CSS rules
 */
@SuppressWarnings("unchecked")
public class Style<T extends Style<T>> implements CSSValue {

    /** Stores CSS property-value pairs in insertion order. */
    protected final Map<String, String> properties = new LinkedHashMap<>();

    /** Creates a new empty Style builder. */
    public Style() {}

    /**
     * Returns this instance cast to the concrete type T.
     * Used internally for fluent method chaining.
     *
     * @return this instance as type T
     */
    protected T self() {
        return (T) this;
    }

    // ==================== CSS Variables ====================

    /**
     * Defines a CSS custom property (variable).
     * Automatically adds "--" prefix if not present.
     *
     * <p>Example:</p>
     * <pre>
     * style().var("primary-color", blue)
     * // Output: --primary-color: blue;
     * </pre>
     *
     * @param name the variable name (with or without "--" prefix)
     * @param value the CSS value to assign
     * @return this builder for chaining
     */
    public T var(String name, CSSValue value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value.css());
    }

    /**
     * Defines a CSS custom property (variable) with a string value.
     *
     * @param name the variable name
     * @param value the string value to assign
     * @return this builder for chaining
     */
    public T var(String name, String value) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return prop(normalized, value);
    }

    // ==================== Display & Box Model ====================

    /** Sets display property. @param value flex, grid, block, inline, none, etc. */
    public T display(CSSValue value) { return prop("display", value); }

    /** Sets box-sizing property. @param value borderBox or contentBox */
    public T boxSizing(CSSValue value) { return prop("box-sizing", value); }

    // ========== Width & Height ==========

    /** Sets width. @param value use px(), rem(), percent(), etc. */
    public T width(CSSValue value) { return prop("width", value); }
    /** Sets height. */
    public T height(CSSValue value) { return prop("height", value); }
    /** Sets minimum width. */
    public T minWidth(CSSValue value) { return prop("min-width", value); }
    /** Sets maximum width. */
    public T maxWidth(CSSValue value) { return prop("max-width", value); }
    /** Sets minimum height. */
    public T minHeight(CSSValue value) { return prop("min-height", value); }
    /** Sets maximum height. */
    public T maxHeight(CSSValue value) { return prop("max-height", value); }

    // ==================== Margin ====================

    /**
     * Sets margin on all sides.
     * @param all margin value for all sides
     */
    public T margin(CSSValue all) {
        return prop("margin", all);
    }

    /**
     * Sets margin with vertical and horizontal values.
     * @param vertical top and bottom margin
     * @param horizontal left and right margin
     */
    public T margin(CSSValue vertical, CSSValue horizontal) {
        return prop("margin", vertical.css() + " " + horizontal.css());
    }

    /**
     * Sets margin with 3 values (top, horizontal, bottom).
     * @param top top margin
     * @param horizontal left and right margin
     * @param bottom bottom margin
     */
    public T margin(CSSValue top, CSSValue horizontal, CSSValue bottom) {
        return prop("margin", top.css() + " " + horizontal.css() + " " + bottom.css());
    }

    /**
     * Sets margin with 4 values (top, right, bottom, left).
     * @param top top margin
     * @param right right margin
     * @param bottom bottom margin
     * @param left left margin
     */
    public T margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("margin", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    /** Sets top margin. */
    public T marginTop(CSSValue value) { return prop("margin-top", value); }
    /** Sets right margin. */
    public T marginRight(CSSValue value) { return prop("margin-right", value); }
    /** Sets bottom margin. */
    public T marginBottom(CSSValue value) { return prop("margin-bottom", value); }
    /** Sets left margin. */
    public T marginLeft(CSSValue value) { return prop("margin-left", value); }
    /** Sets left and right margin. */
    public T marginX(CSSValue value) { return marginLeft(value).marginRight(value); }
    /** Sets top and bottom margin. */
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

    /**
     * Sets grid-template-columns with a raw string value.
     * Prefer the type-safe overload with CSSValue... for better IDE support.
     *
     * @param value the grid template columns value
     * @deprecated Use {@link #gridTemplateColumns(CSSValue...)} for type-safe grid templates
     */
    @Deprecated
    public T gridTemplateColumns(String value) { return prop("grid-template-columns", value); }

    /**
     * Sets grid-template-columns with type-safe CSSValues.
     *
     * <p>Example:</p>
     * <pre>
     * import static com.osmig.Jweb.framework.styles.CSSGrid.*;
     * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
     *
     * // Fixed columns
     * style().gridTemplateColumns(px(200), fr(1), px(200))
     *
     * // Responsive grid
     * style().gridTemplateColumns(repeat(autoFill(), minmax(px(250), fr(1))))
     *
     * // Equal columns
     * style().gridTemplateColumns(repeat(3, fr(1)))
     * </pre>
     *
     * @param columns the column track sizes
     * @return this builder for chaining
     * @see CSSGrid#repeat(int, CSSValue)
     * @see CSSGrid#minmax(CSSValue, CSSValue)
     */
    public T gridTemplateColumns(CSSValue... columns) {
        return prop("grid-template-columns", joinCssValues(columns));
    }

    /**
     * Sets grid-template-rows with a raw string value.
     * Prefer the type-safe overload with CSSValue... for better IDE support.
     *
     * @param value the grid template rows value
     * @deprecated Use {@link #gridTemplateRows(CSSValue...)} for type-safe grid templates
     */
    @Deprecated
    public T gridTemplateRows(String value) { return prop("grid-template-rows", value); }

    /**
     * Sets grid-template-rows with type-safe CSSValues.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateRows(auto, fr(1), auto)
     * style().gridTemplateRows(px(60), fr(1), px(40))
     * </pre>
     *
     * @param rows the row track sizes
     * @return this builder for chaining
     */
    public T gridTemplateRows(CSSValue... rows) {
        return prop("grid-template-rows", joinCssValues(rows));
    }

    /**
     * Sets grid-column with a string value.
     * @deprecated Use {@link #gridColumn(CSSValue)} for type-safe positioning
     */
    @Deprecated
    public T gridColumn(String value) { return prop("grid-column", value); }

    /**
     * Sets grid-column with a type-safe value.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridColumn(span(2))  // span 2 columns
     * </pre>
     *
     * @param value the grid-column value
     * @return this builder for chaining
     */
    public T gridColumn(CSSValue value) { return prop("grid-column", value); }

    /**
     * Sets grid-column with start and end positions.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridColumn(1, 3)  // from line 1 to line 3
     * </pre>
     *
     * @param start the start line
     * @param end the end line
     * @return this builder for chaining
     */
    public T gridColumn(int start, int end) {
        return prop("grid-column", start + " / " + end);
    }

    /**
     * Sets grid-row with a string value.
     * @deprecated Use {@link #gridRow(CSSValue)} for type-safe positioning
     */
    @Deprecated
    public T gridRow(String value) { return prop("grid-row", value); }

    /**
     * Sets grid-row with a type-safe value.
     *
     * @param value the grid-row value
     * @return this builder for chaining
     */
    public T gridRow(CSSValue value) { return prop("grid-row", value); }

    /**
     * Sets grid-row with start and end positions.
     *
     * @param start the start line
     * @param end the end line
     * @return this builder for chaining
     */
    public T gridRow(int start, int end) {
        return prop("grid-row", start + " / " + end);
    }

    /**
     * Sets grid-area with a string value.
     * @deprecated Use {@link #gridArea(CSSValue)} for type-safe areas
     */
    @Deprecated
    public T gridArea(String value) { return prop("grid-area", value); }

    /**
     * Sets grid-area with a type-safe value.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridArea(area(1, 1, 3, 2))  // row-start / col-start / row-end / col-end
     * </pre>
     *
     * @param value the grid-area value
     * @return this builder for chaining
     * @see CSSGrid#area(int, int, int, int)
     */
    public T gridArea(CSSValue value) { return prop("grid-area", value); }

    /**
     * Sets grid-template-areas for named grid areas.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateAreas(
     *     "header header header",
     *     "sidebar main aside",
     *     "footer footer footer"
     * )
     * </pre>
     *
     * @param rows the area strings for each row
     * @return this builder for chaining
     */
    public T gridTemplateAreas(String... rows) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append("\"").append(rows[i]).append("\"");
        }
        return prop("grid-template-areas", sb.toString());
    }

    /**
     * Sets grid-auto-columns for implicit column sizing.
     *
     * @param value the auto column size
     * @return this builder for chaining
     */
    public T gridAutoColumns(CSSValue value) { return prop("grid-auto-columns", value); }

    /**
     * Sets grid-auto-rows for implicit row sizing.
     *
     * @param value the auto row size
     * @return this builder for chaining
     */
    public T gridAutoRows(CSSValue value) { return prop("grid-auto-rows", value); }

    public T gridAutoFlow(CSSValue value) { return prop("grid-auto-flow", value); }
    public T justifyItems(CSSValue value) { return prop("justify-items", value); }
    public T placeItems(CSSValue value) { return prop("place-items", value); }
    public T placeContent(CSSValue value) { return prop("place-content", value); }
    public T placeSelf(CSSValue value) { return prop("place-self", value); }

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

    // ==================== Convenience Presets ====================

    /**
     * Preset: display: flex
     * Shorthand for .display(flex)
     *
     * @return this builder for chaining
     */
    public T flex() { return display(() -> "flex"); }

    /**
     * Preset: display: flex; flex-direction: column
     * Common pattern for vertical layouts.
     *
     * @return this builder for chaining
     */
    public T flexCol() { return display(() -> "flex").flexDirection(() -> "column"); }

    /**
     * Preset: display: flex; flex-direction: row
     * Common pattern for horizontal layouts.
     *
     * @return this builder for chaining
     */
    public T flexRow() { return display(() -> "flex").flexDirection(() -> "row"); }

    /**
     * Preset: display: flex; justify-content: center; align-items: center
     * Centers children both horizontally and vertically.
     *
     * @return this builder for chaining
     */
    public T flexCenter() {
        return display(() -> "flex")
            .justifyContent(() -> "center")
            .alignItems(() -> "center");
    }

    /**
     * Preset: display: flex; justify-content: space-between; align-items: center
     * Common pattern for navbars and headers.
     *
     * @return this builder for chaining
     */
    public T flexBetween() {
        return display(() -> "flex")
            .justifyContent(() -> "space-between")
            .alignItems(() -> "center");
    }

    /**
     * Preset: display: grid with equal columns.
     *
     * <p>Example:</p>
     * <pre>
     * style().grid(3)  // 3 equal columns: grid-template-columns: repeat(3, 1fr)
     * </pre>
     *
     * @param columns number of equal-width columns
     * @return this builder for chaining
     */
    public T grid(int columns) {
        return display(() -> "grid")
            .prop("grid-template-columns", "repeat(" + columns + ", 1fr)");
    }

    /**
     * Preset: display: grid with equal columns and gap.
     *
     * <p>Example:</p>
     * <pre>
     * style().grid(3, rem(1))  // 3 columns with 1rem gap
     * </pre>
     *
     * @param columns number of equal-width columns
     * @param gapValue the gap between grid items
     * @return this builder for chaining
     */
    public T grid(int columns, CSSValue gapValue) {
        return display(() -> "grid")
            .prop("grid-template-columns", "repeat(" + columns + ", 1fr)")
            .gap(gapValue);
    }

    /**
     * Preset: width: 100%; height: 100%
     * Makes element fill its container.
     *
     * @return this builder for chaining
     */
    public T full() {
        return width(() -> "100%").height(() -> "100%");
    }

    /**
     * Preset: width: 100%
     *
     * @return this builder for chaining
     */
    public T fullWidth() { return width(() -> "100%"); }

    /**
     * Preset: height: 100%
     *
     * @return this builder for chaining
     */
    public T fullHeight() { return height(() -> "100%"); }

    /**
     * Preset: position: absolute; inset: 0
     * Positions element to fill its positioned parent.
     *
     * @return this builder for chaining
     */
    public T absolute() {
        return position(() -> "absolute").inset(() -> "0");
    }

    /**
     * Preset: position: relative
     *
     * @return this builder for chaining
     */
    public T relative() { return position(() -> "relative"); }

    /**
     * Preset: position: fixed
     *
     * @return this builder for chaining
     */
    public T fixed() { return position(() -> "fixed"); }

    /**
     * Preset: position: sticky
     *
     * @return this builder for chaining
     */
    public T sticky() { return position(() -> "sticky"); }

    /**
     * Preset: text-align: center
     *
     * @return this builder for chaining
     */
    public T textCenter() { return textAlign(() -> "center"); }

    /**
     * Preset: font-weight: bold (700)
     *
     * @return this builder for chaining
     */
    public T bold() { return fontWeight(700); }

    /**
     * Preset: cursor: pointer
     *
     * @return this builder for chaining
     */
    public T clickable() { return cursor(() -> "pointer"); }

    /**
     * Preset: overflow: hidden
     *
     * @return this builder for chaining
     */
    public T truncate() {
        return overflow(() -> "hidden")
            .prop("text-overflow", "ellipsis")
            .whiteSpace(() -> "nowrap");
    }

    /**
     * Preset: user-select: none
     * Prevents text selection.
     *
     * @return this builder for chaining
     */
    public T noSelect() { return userSelect(() -> "none"); }

    /**
     * Preset: border-radius with rounded corners.
     *
     * @param value the border-radius value
     * @return this builder for chaining
     */
    public T rounded(CSSValue value) { return borderRadius(value); }

    /**
     * Preset: margin: 0 auto (centers block element horizontally)
     *
     * @return this builder for chaining
     */
    public T centerX() { return margin(() -> "0", () -> "auto"); }

    // ==================== Scroll Snap ====================

    /**
     * Sets scroll-snap-type for scroll containers.
     *
     * <p>Example:</p>
     * <pre>
     * style().scrollSnapType(xMandatory)  // Horizontal mandatory snap
     * style().scrollSnapType(yProximity)  // Vertical proximity snap
     * style().scrollSnapType(bothMandatory)
     * </pre>
     *
     * @param value the scroll-snap-type value
     * @return this builder for chaining
     */
    public T scrollSnapType(CSSValue value) { return prop("scroll-snap-type", value); }

    /**
     * Sets scroll-snap-align for snap targets.
     *
     * <p>Example:</p>
     * <pre>
     * style().scrollSnapAlign(start)   // Snap to start
     * style().scrollSnapAlign(center)  // Snap to center
     * style().scrollSnapAlign(end)     // Snap to end
     * </pre>
     *
     * @param value the scroll-snap-align value
     * @return this builder for chaining
     */
    public T scrollSnapAlign(CSSValue value) { return prop("scroll-snap-align", value); }

    /**
     * Sets scroll-snap-stop.
     *
     * @param value "normal" or "always"
     * @return this builder for chaining
     */
    public T scrollSnapStop(CSSValue value) { return prop("scroll-snap-stop", value); }

    /**
     * Sets scroll-padding (for scroll containers).
     *
     * @param value the scroll padding value
     * @return this builder for chaining
     */
    public T scrollPadding(CSSValue value) { return prop("scroll-padding", value); }

    /**
     * Sets scroll-padding with individual values.
     */
    public T scrollPadding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("scroll-padding", top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    /**
     * Sets scroll-margin (for snap targets).
     *
     * @param value the scroll margin value
     * @return this builder for chaining
     */
    public T scrollMargin(CSSValue value) { return prop("scroll-margin", value); }

    /**
     * Sets scroll-behavior for smooth scrolling.
     *
     * @param value "auto" or "smooth"
     * @return this builder for chaining
     */
    public T scrollBehavior(CSSValue value) { return prop("scroll-behavior", value); }

    /**
     * Sets overscroll-behavior.
     *
     * @param value the overscroll behavior
     * @return this builder for chaining
     */
    public T overscrollBehavior(CSSValue value) { return prop("overscroll-behavior", value); }

    /**
     * Sets overscroll-behavior for x axis.
     */
    public T overscrollBehaviorX(CSSValue value) { return prop("overscroll-behavior-x", value); }

    /**
     * Sets overscroll-behavior for y axis.
     */
    public T overscrollBehaviorY(CSSValue value) { return prop("overscroll-behavior-y", value); }

    // ==================== Text Wrap ====================

    /**
     * Sets text-wrap for better text layout.
     *
     * <p>Example:</p>
     * <pre>
     * style().textWrap(balance)  // Balances line lengths
     * style().textWrap(pretty)   // Prettier line breaks
     * style().textWrap(stable)   // Stable during editing
     * </pre>
     *
     * @param value the text-wrap value
     * @return this builder for chaining
     */
    public T textWrap(CSSValue value) { return prop("text-wrap", value); }

    /**
     * Sets text-wrap-mode.
     *
     * @param value "wrap" or "nowrap"
     * @return this builder for chaining
     */
    public T textWrapMode(CSSValue value) { return prop("text-wrap-mode", value); }

    /**
     * Sets text-wrap-style.
     *
     * @param value "auto", "balance", "pretty", or "stable"
     * @return this builder for chaining
     */
    public T textWrapStyle(CSSValue value) { return prop("text-wrap-style", value); }

    // ==================== View Transitions ====================

    /**
     * Sets view-transition-name for View Transitions API.
     *
     * <p>Example:</p>
     * <pre>
     * style().viewTransitionName("header")
     * style().viewTransitionName("card-1")
     * </pre>
     *
     * @param name the transition name
     * @return this builder for chaining
     */
    public T viewTransitionName(String name) { return prop("view-transition-name", name); }

    /**
     * Sets view-transition-name to "none".
     *
     * @return this builder for chaining
     */
    public T viewTransitionNone() { return prop("view-transition-name", "none"); }

    // ==================== Scroll-Driven Animations ====================

    /**
     * Sets animation-timeline for scroll-driven animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(scrollTimeline())
     * style().animationTimeline(viewTimeline())
     * </pre>
     *
     * @param value the animation timeline
     * @return this builder for chaining
     */
    public T animationTimeline(CSSValue value) { return prop("animation-timeline", value); }

    /**
     * Sets animation-range for scroll-driven animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationRange("entry", "exit")
     * style().animationRange("cover 0%", "cover 100%")
     * </pre>
     *
     * @param start the start range
     * @param end the end range
     * @return this builder for chaining
     */
    public T animationRange(String start, String end) {
        return prop("animation-range", start + " " + end);
    }

    /**
     * Sets animation-range with a single value.
     *
     * @param value the animation range
     * @return this builder for chaining
     */
    public T animationRange(CSSValue value) { return prop("animation-range", value); }

    /**
     * Sets timeline-scope.
     *
     * @param name the timeline name
     * @return this builder for chaining
     */
    public T timelineScope(String name) { return prop("timeline-scope", name); }

    /**
     * Sets scroll-timeline for creating named scroll timelines.
     *
     * @param value the scroll timeline value
     * @return this builder for chaining
     */
    public T scrollTimeline(CSSValue value) { return prop("scroll-timeline", value); }

    /**
     * Sets scroll-timeline-name.
     *
     * @param name the timeline name
     * @return this builder for chaining
     */
    public T scrollTimelineName(String name) { return prop("scroll-timeline-name", name); }

    /**
     * Sets scroll-timeline-axis.
     *
     * @param value "block", "inline", "x", or "y"
     * @return this builder for chaining
     */
    public T scrollTimelineAxis(CSSValue value) { return prop("scroll-timeline-axis", value); }

    /**
     * Sets view-timeline for creating named view timelines.
     *
     * @param value the view timeline value
     * @return this builder for chaining
     */
    public T viewTimeline(CSSValue value) { return prop("view-timeline", value); }

    /**
     * Sets view-timeline-name.
     *
     * @param name the timeline name
     * @return this builder for chaining
     */
    public T viewTimelineName(String name) { return prop("view-timeline-name", name); }

    /**
     * Sets view-timeline-axis.
     *
     * @param value "block", "inline", "x", or "y"
     * @return this builder for chaining
     */
    public T viewTimelineAxis(CSSValue value) { return prop("view-timeline-axis", value); }

    /**
     * Sets view-timeline-inset.
     *
     * @param value the inset value
     * @return this builder for chaining
     */
    public T viewTimelineInset(CSSValue value) { return prop("view-timeline-inset", value); }

    // ==================== Container Queries ====================

    /**
     * Sets container-type for container queries.
     *
     * <p>Example:</p>
     * <pre>
     * style().containerType(inlineSize)
     * style().containerType(size)
     * </pre>
     *
     * @param value the container type
     * @return this builder for chaining
     */
    public T containerType(CSSValue value) { return prop("container-type", value); }

    /**
     * Sets container-name for named container queries.
     *
     * @param name the container name
     * @return this builder for chaining
     */
    public T containerName(String name) { return prop("container-name", name); }

    /**
     * Sets container shorthand.
     *
     * @param name the container name
     * @param type the container type
     * @return this builder for chaining
     */
    public T container(String name, CSSValue type) {
        return prop("container", name + " / " + type.css());
    }

    // ==================== Accent Color ====================

    /**
     * Sets accent-color for form controls.
     *
     * <p>Example:</p>
     * <pre>
     * style().accentColor(blue)
     * style().accentColor(hex("#ff6b6b"))
     * </pre>
     *
     * @param value the accent color
     * @return this builder for chaining
     */
    public T accentColor(CSSValue value) { return prop("accent-color", value); }

    // ==================== Color Scheme ====================

    /**
     * Sets color-scheme for light/dark mode.
     *
     * <p>Example:</p>
     * <pre>
     * style().colorScheme(light)
     * style().colorScheme(dark)
     * style().colorScheme(lightDark)
     * </pre>
     *
     * @param value the color scheme
     * @return this builder for chaining
     */
    public T colorScheme(CSSValue value) { return prop("color-scheme", value); }

    // ==================== Forced Colors ====================

    /**
     * Sets forced-color-adjust.
     *
     * @param value "auto" or "none"
     * @return this builder for chaining
     */
    public T forcedColorAdjust(CSSValue value) { return prop("forced-color-adjust", value); }

    // ==================== Print ====================

    /**
     * Sets print-color-adjust.
     *
     * @param value "economy" or "exact"
     * @return this builder for chaining
     */
    public T printColorAdjust(CSSValue value) { return prop("print-color-adjust", value); }

    // ==================== Multi-Column Layout ====================

    /**
     * Sets columns shorthand for multi-column layout.
     *
     * <p>Example:</p>
     * <pre>
     * style().columns(px(200), num(3))  // columns: 200px 3
     * </pre>
     *
     * @param width the column width
     * @param count the column count
     * @return this builder for chaining
     */
    public T columns(CSSValue width, CSSValue count) {
        return prop("columns", width.css() + " " + count.css());
    }

    /** Sets columns with single value. */
    public T columns(CSSValue value) { return prop("columns", value); }

    /** Sets column-count. @param value number of columns or "auto" */
    public T columnCount(CSSValue value) { return prop("column-count", value); }

    /** Sets column-count with integer. */
    public T columnCount(int value) { return prop("column-count", String.valueOf(value)); }

    /** Sets column-width. */
    public T columnWidth(CSSValue value) { return prop("column-width", value); }

    /** Sets column-gap. */
    public T columnGapMulti(CSSValue value) { return prop("column-gap", value); }

    /** Sets column-rule shorthand (width, style, color). */
    public T columnRule(CSSValue width, CSSValue style, CSSValue color) {
        return prop("column-rule", width.css() + " " + style.css() + " " + color.css());
    }

    /** Sets column-rule with single value. */
    public T columnRule(CSSValue value) { return prop("column-rule", value); }

    /** Sets column-rule-width. */
    public T columnRuleWidth(CSSValue value) { return prop("column-rule-width", value); }

    /** Sets column-rule-style. */
    public T columnRuleStyle(CSSValue value) { return prop("column-rule-style", value); }

    /** Sets column-rule-color. */
    public T columnRuleColor(CSSValue value) { return prop("column-rule-color", value); }

    /** Sets column-span. @param value "none" or "all" */
    public T columnSpan(CSSValue value) { return prop("column-span", value); }

    /** Sets column-fill. @param value "auto" or "balance" */
    public T columnFill(CSSValue value) { return prop("column-fill", value); }

    // ==================== Float & Clear ====================

    /**
     * Sets float property.
     *
     * <p>Example:</p>
     * <pre>
     * style().float_(left)   // float: left
     * style().float_(right)  // float: right
     * </pre>
     *
     * @param value left, right, none, inline-start, inline-end
     * @return this builder for chaining
     */
    public T float_(CSSValue value) { return prop("float", value); }

    /**
     * Sets clear property.
     *
     * <p>Example:</p>
     * <pre>
     * style().clear(both)  // clear: both
     * </pre>
     *
     * @param value left, right, both, none, inline-start, inline-end
     * @return this builder for chaining
     */
    public T clear(CSSValue value) { return prop("clear", value); }

    // ==================== Text Decoration (Extended) ====================

    /** Sets text-decoration-line. @param value underline, overline, line-through, none */
    public T textDecorationLine(CSSValue value) { return prop("text-decoration-line", value); }

    /** Sets text-decoration-color. */
    public T textDecorationColor(CSSValue value) { return prop("text-decoration-color", value); }

    /** Sets text-decoration-style. @param value solid, double, dotted, dashed, wavy */
    public T textDecorationStyle(CSSValue value) { return prop("text-decoration-style", value); }

    /** Sets text-decoration-thickness. */
    public T textDecorationThickness(CSSValue value) { return prop("text-decoration-thickness", value); }

    /** Sets text-underline-offset. */
    public T textUnderlineOffset(CSSValue value) { return prop("text-underline-offset", value); }

    /** Sets text-underline-position. @param value auto, under, left, right */
    public T textUnderlinePosition(CSSValue value) { return prop("text-underline-position", value); }

    /** Sets text-decoration-skip-ink. @param value auto, none, all */
    public T textDecorationSkipInk(CSSValue value) { return prop("text-decoration-skip-ink", value); }

    // ==================== Text Emphasis ====================

    /** Sets text-emphasis shorthand. */
    public T textEmphasis(CSSValue value) { return prop("text-emphasis", value); }

    /** Sets text-emphasis-style. @param value none, filled, open, dot, circle, etc. */
    public T textEmphasisStyle(CSSValue value) { return prop("text-emphasis-style", value); }

    /** Sets text-emphasis-color. */
    public T textEmphasisColor(CSSValue value) { return prop("text-emphasis-color", value); }

    /** Sets text-emphasis-position. @param value over right, under left, etc. */
    public T textEmphasisPosition(String value) { return prop("text-emphasis-position", value); }

    // ==================== Font Features ====================

    /**
     * Sets font-variant shorthand.
     *
     * @param value font variant value
     * @return this builder for chaining
     */
    public T fontVariant(CSSValue value) { return prop("font-variant", value); }

    /** Sets font-variant-caps. @param value small-caps, all-small-caps, petite-caps, etc. */
    public T fontVariantCaps(CSSValue value) { return prop("font-variant-caps", value); }

    /** Sets font-variant-numeric. @param value lining-nums, oldstyle-nums, tabular-nums, etc. */
    public T fontVariantNumeric(CSSValue value) { return prop("font-variant-numeric", value); }

    /** Sets font-variant-ligatures. @param value common-ligatures, no-common-ligatures, etc. */
    public T fontVariantLigatures(CSSValue value) { return prop("font-variant-ligatures", value); }

    /** Sets font-variant-alternates. */
    public T fontVariantAlternates(CSSValue value) { return prop("font-variant-alternates", value); }

    /** Sets font-variant-east-asian. @param value jis78, jis83, simplified, traditional, etc. */
    public T fontVariantEastAsian(CSSValue value) { return prop("font-variant-east-asian", value); }

    /** Sets font-variant-position. @param value normal, sub, super */
    public T fontVariantPosition(CSSValue value) { return prop("font-variant-position", value); }

    /**
     * Sets font-feature-settings for OpenType features.
     *
     * <p>Example:</p>
     * <pre>
     * style().fontFeatureSettings("'liga' 1, 'calt' 1")
     * style().fontFeatureSettings("'smcp'")
     * </pre>
     *
     * @param value OpenType feature settings
     * @return this builder for chaining
     */
    public T fontFeatureSettings(String value) { return prop("font-feature-settings", value); }

    /**
     * Sets font-variation-settings for variable fonts.
     *
     * <p>Example:</p>
     * <pre>
     * style().fontVariationSettings("'wght' 600, 'wdth' 100")
     * </pre>
     *
     * @param value font variation axis settings
     * @return this builder for chaining
     */
    public T fontVariationSettings(String value) { return prop("font-variation-settings", value); }

    /** Sets font-optical-sizing. @param value auto or none */
    public T fontOpticalSizing(CSSValue value) { return prop("font-optical-sizing", value); }

    /** Sets font-kerning. @param value auto, normal, none */
    public T fontKerning(CSSValue value) { return prop("font-kerning", value); }

    /** Sets font-stretch. @param value normal, condensed, expanded, etc. */
    public T fontStretch(CSSValue value) { return prop("font-stretch", value); }

    /** Sets font-size-adjust. */
    public T fontSizeAdjust(CSSValue value) { return prop("font-size-adjust", value); }

    // ==================== Writing Modes & Direction ====================

    /**
     * Sets writing-mode for vertical/horizontal text.
     *
     * <p>Example:</p>
     * <pre>
     * style().writingMode(verticalRl)  // vertical text, right-to-left
     * style().writingMode(verticalLr)  // vertical text, left-to-right
     * </pre>
     *
     * @param value horizontal-tb, vertical-rl, vertical-lr, sideways-rl, sideways-lr
     * @return this builder for chaining
     */
    public T writingMode(CSSValue value) { return prop("writing-mode", value); }

    /**
     * Sets direction for text direction.
     *
     * @param value ltr or rtl
     * @return this builder for chaining
     */
    public T direction(CSSValue value) { return prop("direction", value); }

    /** Sets text-orientation. @param value mixed, upright, sideways */
    public T textOrientation(CSSValue value) { return prop("text-orientation", value); }

    /** Sets unicode-bidi. @param value normal, embed, isolate, bidi-override, etc. */
    public T unicodeBidi(CSSValue value) { return prop("unicode-bidi", value); }

    // ==================== CSS Masking ====================

    /** Sets mask shorthand. */
    public T mask(CSSValue value) { return prop("mask", value); }

    /** Sets mask-image. */
    public T maskImage(CSSValue value) { return prop("mask-image", value); }

    /** Sets mask-mode. @param value alpha, luminance, match-source */
    public T maskMode(CSSValue value) { return prop("mask-mode", value); }

    /** Sets mask-repeat. */
    public T maskRepeat(CSSValue value) { return prop("mask-repeat", value); }

    /** Sets mask-position. */
    public T maskPosition(CSSValue value) { return prop("mask-position", value); }

    /** Sets mask-clip. */
    public T maskClip(CSSValue value) { return prop("mask-clip", value); }

    /** Sets mask-origin. */
    public T maskOrigin(CSSValue value) { return prop("mask-origin", value); }

    /** Sets mask-size. */
    public T maskSize(CSSValue value) { return prop("mask-size", value); }

    /** Sets mask-composite. @param value add, subtract, intersect, exclude */
    public T maskComposite(CSSValue value) { return prop("mask-composite", value); }

    /** Sets mask-type. @param value luminance, alpha */
    public T maskType(CSSValue value) { return prop("mask-type", value); }

    // ==================== CSS Shapes ====================

    /**
     * Sets shape-outside for text wrapping around shapes.
     *
     * <p>Example:</p>
     * <pre>
     * style().shapeOutside(circleClip(percent(50)))
     * style().shapeOutside(url("/images/shape.png"))
     * </pre>
     *
     * @param value shape function, image, or box value
     * @return this builder for chaining
     */
    public T shapeOutside(CSSValue value) { return prop("shape-outside", value); }

    /** Sets shape-margin. */
    public T shapeMargin(CSSValue value) { return prop("shape-margin", value); }

    /** Sets shape-image-threshold (0-1 for transparency threshold). */
    public T shapeImageThreshold(double value) { return prop("shape-image-threshold", String.valueOf(value)); }

    // ==================== Compositing & Blending ====================

    /**
     * Sets mix-blend-mode for blending with background.
     *
     * <p>Example:</p>
     * <pre>
     * style().mixBlendMode(multiply)
     * style().mixBlendMode(overlay)
     * </pre>
     *
     * @param value blend mode (multiply, screen, overlay, darken, lighten, etc.)
     * @return this builder for chaining
     */
    public T mixBlendMode(CSSValue value) { return prop("mix-blend-mode", value); }

    /**
     * Sets background-blend-mode.
     *
     * @param value blend mode for background layers
     * @return this builder for chaining
     */
    public T backgroundBlendMode(CSSValue value) { return prop("background-blend-mode", value); }

    /**
     * Sets isolation for stacking context.
     *
     * @param value auto or isolate
     * @return this builder for chaining
     */
    public T isolation(CSSValue value) { return prop("isolation", value); }

    // ==================== Performance Hints ====================

    /**
     * Sets will-change to hint browser about upcoming changes.
     *
     * <p>Example:</p>
     * <pre>
     * style().willChange(propTransform)
     * style().willChange("transform, opacity")
     * </pre>
     *
     * @param value properties that will change
     * @return this builder for chaining
     */
    public T willChange(CSSValue value) { return prop("will-change", value); }

    /** Sets will-change with string value. */
    public T willChange(String value) { return prop("will-change", value); }

    /**
     * Sets contain for layout containment.
     *
     * <p>Example:</p>
     * <pre>
     * style().contain(strict)
     * style().contain(layoutPaint)
     * </pre>
     *
     * @param value none, strict, content, size, layout, style, paint
     * @return this builder for chaining
     */
    public T contain(CSSValue value) { return prop("contain", value); }

    /** Sets content-visibility for rendering optimization. @param value visible, auto, hidden */
    public T contentVisibility(CSSValue value) { return prop("content-visibility", value); }

    /** Sets contain-intrinsic-size for content-visibility. */
    public T containIntrinsicSize(CSSValue value) { return prop("contain-intrinsic-size", value); }

    /** Sets contain-intrinsic-size with width and height. */
    public T containIntrinsicSize(CSSValue width, CSSValue height) {
        return prop("contain-intrinsic-size", width.css() + " " + height.css());
    }

    // ==================== Print & Page Break ====================

    /** Sets page-break-before. @param value auto, always, avoid, left, right */
    public T pageBreakBefore(CSSValue value) { return prop("page-break-before", value); }

    /** Sets page-break-after. @param value auto, always, avoid, left, right */
    public T pageBreakAfter(CSSValue value) { return prop("page-break-after", value); }

    /** Sets page-break-inside. @param value auto, avoid */
    public T pageBreakInside(CSSValue value) { return prop("page-break-inside", value); }

    /** Sets break-before (modern replacement for page-break-before). */
    public T breakBefore(CSSValue value) { return prop("break-before", value); }

    /** Sets break-after (modern replacement for page-break-after). */
    public T breakAfter(CSSValue value) { return prop("break-after", value); }

    /** Sets break-inside (modern replacement for page-break-inside). */
    public T breakInside(CSSValue value) { return prop("break-inside", value); }

    /** Sets orphans (minimum lines at bottom of page). */
    public T orphans(int value) { return prop("orphans", String.valueOf(value)); }

    /** Sets widows (minimum lines at top of page). */
    public T widows(int value) { return prop("widows", String.valueOf(value)); }

    // ==================== Misc Properties ====================

    /** Sets quotes for q element. @param value e.g., "\"\\201C\" \"\\201D\" \"\\2018\" \"\\2019\"" */
    public T quotes(String value) { return prop("quotes", value); }

    /** Sets tab-size for tab character width. */
    public T tabSize(CSSValue value) { return prop("tab-size", value); }

    /** Sets tab-size with integer (number of spaces). */
    public T tabSize(int value) { return prop("tab-size", String.valueOf(value)); }

    /** Sets hyphens. @param value none, manual, auto */
    public T hyphens(CSSValue value) { return prop("hyphens", value); }

    /** Sets hyphenate-character. */
    public T hyphenateCharacter(String value) { return prop("hyphenate-character", value); }

    /** Sets caret-color for text cursor. */
    public T caretColor(CSSValue value) { return prop("caret-color", value); }

    /** Sets appearance for form control styling. @param value none, auto, menulist-button, etc. */
    public T appearance(CSSValue value) { return prop("appearance", value); }

    /** Sets touch-action. @param value auto, none, pan-x, pan-y, manipulation, etc. */
    public T touchAction(CSSValue value) { return prop("touch-action", value); }

    /** Sets scroll-snap-margin-* for individual sides. */
    public T scrollMarginTop(CSSValue value) { return prop("scroll-margin-top", value); }
    public T scrollMarginRight(CSSValue value) { return prop("scroll-margin-right", value); }
    public T scrollMarginBottom(CSSValue value) { return prop("scroll-margin-bottom", value); }
    public T scrollMarginLeft(CSSValue value) { return prop("scroll-margin-left", value); }

    /** Sets scroll-padding-* for individual sides. */
    public T scrollPaddingTop(CSSValue value) { return prop("scroll-padding-top", value); }
    public T scrollPaddingRight(CSSValue value) { return prop("scroll-padding-right", value); }
    public T scrollPaddingBottom(CSSValue value) { return prop("scroll-padding-bottom", value); }
    public T scrollPaddingLeft(CSSValue value) { return prop("scroll-padding-left", value); }

    /** Sets image-rendering. @param value auto, crisp-edges, pixelated */
    public T imageRendering(CSSValue value) { return prop("image-rendering", value); }

    /** Sets counter-reset. */
    public T counterReset(String value) { return prop("counter-reset", value); }

    /** Sets counter-increment. */
    public T counterIncrement(String value) { return prop("counter-increment", value); }

    /** Sets counter-set. */
    public T counterSet(String value) { return prop("counter-set", value); }

    /** Sets text-overflow. @param value clip, ellipsis */
    public T textOverflow(CSSValue value) { return prop("text-overflow", value); }

    /** Sets all property to reset all properties. @param value initial, inherit, unset, revert */
    public T all(CSSValue value) { return prop("all", value); }

    // ==================== Raw Property ====================

    /**
     * Sets any CSS property by name. Use for properties not covered by other methods.
     *
     * <p>Example:</p>
     * <pre>
     * style().prop("appearance", "none")
     * style().prop("scroll-snap-type", "x mandatory")
     * </pre>
     *
     * @param name the CSS property name (e.g., "display", "margin-top")
     * @param value the CSS value
     * @return this builder for chaining
     */
    public T prop(String name, CSSValue value) {
        properties.put(name, value.css());
        return self();
    }

    /**
     * Sets any CSS property by name with a string value.
     * Prefer using the type-safe overload with CSSValue when possible.
     *
     * @param name the CSS property name
     * @param value the CSS value as a string
     * @return this builder for chaining
     * @deprecated Use type-safe methods or {@link #unsafeProp(String, String)} to make the escape explicit
     */
    @Deprecated
    public T prop(String name, String value) {
        properties.put(name, value);
        return self();
    }

    /**
     * Sets any CSS property by name with an unvalidated string value.
     * Use this when the DSL doesn't provide a type-safe method for a CSS property.
     *
     * <p>Example:</p>
     * <pre>
     * style().unsafeProp("scroll-snap-type", "x mandatory")
     * style().unsafeProp("container-type", "inline-size")
     * </pre>
     *
     * @param name the CSS property name
     * @param value the CSS value as a string
     * @return this builder for chaining
     */
    public T unsafeProp(String name, String value) {
        properties.put(name, value);
        return self();
    }

    // ==================== Build ====================

    /**
     * Returns this style as a CSS string (for use as a CSSValue).
     *
     * @return the CSS properties as a formatted string
     */
    @Override
    public String css() {
        return build();
    }

    /**
     * Builds the CSS properties string.
     *
     * <p>Example output:</p>
     * <pre>
     * "display: flex; padding: 10px; color: red;"
     * </pre>
     *
     * @return the CSS properties as a formatted string
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
     * Returns the CSS properties as a map.
     * Useful for iteration or inspection.
     *
     * @return an immutable copy of the property map
     */
    public Map<String, String> toMap() {
        return Map.copyOf(properties);
    }

    /**
     * Checks if this style has any properties.
     *
     * @return true if no properties have been set
     */
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public String toString() {
        return build();
    }

    // ==================== Helper Methods ====================

    /**
     * Joins multiple CSSValue objects with spaces.
     * Used for properties that accept multiple values like grid-template-columns.
     */
    private static String joinCssValues(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }
}
