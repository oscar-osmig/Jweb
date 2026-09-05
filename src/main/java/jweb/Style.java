package jweb;

import com.osmig.Jweb.framework.styles.CSS;
import com.osmig.Jweb.framework.styles.CSSColors;
import com.osmig.Jweb.framework.styles.CSSUnits;

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
public class Style<T extends Style<T>> implements com.osmig.Jweb.framework.styles.CSSValue {

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

    // ==================== Size Shortcuts ====================

    /**
     * Sets both width and height to the same value.
     * Useful for squares and circles.
     *
     * <p>Example:</p>
     * <pre>
     * style().size(px(100))  // width: 100px; height: 100px;
     * </pre>
     *
     * @param value the width and height value
     * @return this builder for chaining
     */
    public T size(CSSValue value) {
        return width(value).height(value);
    }

    /**
     * Sets width and height independently.
     *
     * <p>Example:</p>
     * <pre>
     * style().size(px(200), px(100))  // width: 200px; height: 100px;
     * </pre>
     *
     * @param w the width value
     * @param h the height value
     * @return this builder for chaining
     */
    public T size(CSSValue w, CSSValue h) {
        return width(w).height(h);
    }

    /**
     * Sets both min-width and min-height to the same value.
     *
     * @param value the minimum size value
     * @return this builder for chaining
     *
     * @deprecated Use {@code .minWidth(v).minHeight(v)}.
     */
    @Deprecated
    public T minSize(CSSValue value) {
        return minWidth(value).minHeight(value);
    }

    /**
     * Sets both max-width and max-height to the same value.
     *
     * @param value the maximum size value
     * @return this builder for chaining
     *
     * @deprecated Use {@code .maxWidth(v).maxHeight(v)}.
     */
    @Deprecated
    public T maxSize(CSSValue value) {
        return maxWidth(value).maxHeight(value);
    }

    /**
     * Sets min-width and max-width range.
     * Useful for responsive elements.
     *
     * <p>Example:</p>
     * <pre>
     * style().widthRange(px(200), px(800))  // min-width: 200px; max-width: 800px;
     * </pre>
     *
     * @param min the minimum width
     * @param max the maximum width
     * @return this builder for chaining
     *
     * @deprecated Use {@code .minWidth(min).maxWidth(max)}.
     */
    @Deprecated
    public T widthRange(CSSValue min, CSSValue max) {
        return minWidth(min).maxWidth(max);
    }

    /**
     * Sets min-height and max-height range.
     *
     * @param min the minimum height
     * @param max the maximum height
     * @return this builder for chaining
     *
     * @deprecated Use {@code .minHeight(min).maxHeight(max)}.
     */
    @Deprecated
    public T heightRange(CSSValue min, CSSValue max) {
        return minHeight(min).maxHeight(max);
    }

    /**
     * Sets 100vw width (full viewport width).
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .width("100vw")}.
     */
    @Deprecated
    public T fullViewportWidth() {
        return prop("width", "100vw");
    }

    /**
     * Sets 100vh height (full viewport height).
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .height("100vh")}.
     */
    @Deprecated
    public T fullViewportHeight() {
        return prop("height", "100vh");
    }

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
    public T backgroundPosition(CSSValue x, CSSValue y) {
        return prop("background-position", x.css() + " " + y.css());
    }
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
    /** {@code flex: <n>} from a number: {@code .flex(1)}. */
    public T flex(double value) {
        return prop("flex", value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value));
    }
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
     * Sets grid-template-columns from a plain CSS string.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateColumns("repeat(3, 1fr)")
     * style().gridTemplateColumns("200px 1fr 200px")
     * </pre>
     *
     * @param value the grid template columns value
     * @return this builder for chaining
     */
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
     * Sets grid-template-rows from a plain CSS string.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateRows("auto 1fr auto")
     * </pre>
     *
     * @param value the grid template rows value
     * @return this builder for chaining
     */
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
     * Sets grid-column from a plain CSS string, e.g. {@code "1 / 3"} or {@code "span 2"}.
     *
     * @param value the grid-column value
     * @return this builder for chaining
     */
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
     * Sets grid-row from a plain CSS string, e.g. {@code "1 / 3"} or {@code "span 2"}.
     *
     * @param value the grid-row value
     * @return this builder for chaining
     */
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
     * Sets grid-area from a plain CSS string, e.g. {@code "header"} or {@code "1 / 1 / 3 / 2"}.
     *
     * @param value the grid-area value
     * @return this builder for chaining
     */
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

    /** {@code transition: all <duration>} — animate every animatable property. */
    public T transitionAll(CSSValue duration) {
        return prop("transition", "all " + duration.css());
    }

    /** Transition color, background-color and border-color together. */
    public T transitionColors(CSSValue duration) {
        String d = duration.css();
        return prop("transition", "color " + d + ", background-color " + d + ", border-color " + d);
    }

    /** {@code transition: background-color <duration>} */
    public T transitionBackground(CSSValue duration) {
        return prop("transition", "background-color " + duration.css());
    }

    /** {@code transition: transform <duration>} */
    public T transitionTransform(CSSValue duration) {
        return prop("transition", "transform " + duration.css());
    }

    /** {@code transition: opacity <duration>} */
    public T transitionOpacity(CSSValue duration) {
        return prop("transition", "opacity " + duration.css());
    }

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

    // The keyframes name is a plain String — no anim("...") wrapper needed:
    //   style().animation("gradientShift", s(3), linear, s(0), infinite)
    public T animation(String name, CSSValue duration) {
        return prop("animation", name + " " + duration.css());
    }
    public T animation(String name, CSSValue duration, CSSValue timing) {
        return animation(CSSValue.of(name), duration, timing);
    }
    public T animation(String name, CSSValue duration, CSSValue timing, CSSValue delay) {
        return animation(CSSValue.of(name), duration, timing, delay);
    }
    public T animation(String name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount) {
        return animation(CSSValue.of(name), duration, timing, delay, iterationCount);
    }
    public T animation(String name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction) {
        return animation(CSSValue.of(name), duration, timing, delay, iterationCount, direction);
    }
    public T animation(String name, CSSValue duration, CSSValue timing, CSSValue delay, CSSValue iterationCount, CSSValue direction, CSSValue fillMode) {
        return animation(CSSValue.of(name), duration, timing, delay, iterationCount, direction, fillMode);
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

    // ==================== Box Shadow Presets ====================

    /**
     * Extra-small shadow preset.
     * Equivalent to: box-shadow: 0 1px 2px rgba(0,0,0,0.05)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 1px 2px rgba(0,0,0,0.05)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowXs() { return prop("box-shadow", "0 1px 2px rgba(0,0,0,0.05)"); }

    /**
     * Small shadow preset.
     * Equivalent to: box-shadow: 0 1px 3px rgba(0,0,0,0.1), 0 1px 2px rgba(0,0,0,0.06)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 1px 3px rgba(0,0,0,0.1),0 1px 2px rgba(0,0,0,0.06)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowSm() { return prop("box-shadow", "0 1px 3px rgba(0,0,0,0.1),0 1px 2px rgba(0,0,0,0.06)"); }

    /**
     * Default shadow preset.
     * Equivalent to: box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 4px 6px -1px rgba(0,0,0,0.1),0 2px 4px -1px rgba(0,0,0,0.06)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadow() { return prop("box-shadow", "0 4px 6px -1px rgba(0,0,0,0.1),0 2px 4px -1px rgba(0,0,0,0.06)"); }

    /**
     * Medium shadow preset.
     * Equivalent to: box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 10px 15px -3px rgba(0,0,0,0.1),0 4px 6px -2px rgba(0,0,0,0.05)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowMd() { return prop("box-shadow", "0 10px 15px -3px rgba(0,0,0,0.1),0 4px 6px -2px rgba(0,0,0,0.05)"); }

    /**
     * Large shadow preset.
     * Equivalent to: box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 20px 25px -5px rgba(0,0,0,0.1),0 10px 10px -5px rgba(0,0,0,0.04)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowLg() { return prop("box-shadow", "0 20px 25px -5px rgba(0,0,0,0.1),0 10px 10px -5px rgba(0,0,0,0.04)"); }

    /**
     * Extra-large shadow preset.
     * Equivalent to: box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("0 25px 50px -12px rgba(0,0,0,0.25)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowXl() { return prop("box-shadow", "0 25px 50px -12px rgba(0,0,0,0.25)"); }

    /**
     * Inner shadow preset.
     * Equivalent to: box-shadow: inset 0 2px 4px rgba(0,0,0,0.06)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("inset 0 2px 4px rgba(0,0,0,0.06)")} — a design opinion, not a CSS feature.
     */
    @Deprecated
    public T shadowInner() { return prop("box-shadow", "inset 0 2px 4px rgba(0,0,0,0.06)"); }

    /**
     * No shadow (removes shadow).
     * Equivalent to: box-shadow: none
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .boxShadow("none")}.
     */
    @Deprecated
    public T shadowNone() { return prop("box-shadow", "none"); }

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
        // Safari still needs the -webkit- prefix
        properties.put("-webkit-backdrop-filter", sb.toString());
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

    /**
     * Sets {@code content} to a quoted string literal.
     *
     * <p><b>Asymmetry warning:</b> unlike every other {@code String} overload in
     * this class, this one does <em>not</em> pass the value through verbatim — it
     * wraps it in single quotes, because a bare word is almost never what
     * {@code content} wants. For an unquoted value (a keyword, {@code attr(...)},
     * {@code counter(...)}, {@code url(...)}, or a quoted-string you built
     * yourself) use {@link #content(CSSValue)} or
     * {@link #prop(String, String) prop("content", ...)}.</p>
     *
     * <pre>
     * style().content("→")                       // content: '→';
     * style().content(attrContent("data-label"))  // content: attr(data-label);
     * style().prop("content", "none")             // content: none;
     * </pre>
     *
     * @param value the text to quote
     * @return this builder for chaining
     */
    public T content(String value) { return prop("content", "'" + value + "'"); }

    /** Sets {@code content} to a value verbatim (keyword, {@code attr()}, {@code url()}, …). */
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

    /** {@code border-inline} as one value, e.g. {@code "1px solid red"}. */
    public T borderInline(CSSValue value) { return prop("border-inline", value); }
    /** {@code border-inline} as one value, e.g. {@code "1px solid red"}. */
    public T borderInline(String value) { return prop("border-inline", value); }
    /** {@code border-inline-start} as one value. */
    public T borderInlineStart(CSSValue value) { return prop("border-inline-start", value); }
    /** {@code border-inline-start} as one value. */
    public T borderInlineStart(String value) { return prop("border-inline-start", value); }
    /** {@code border-inline-end} as one value. */
    public T borderInlineEnd(CSSValue value) { return prop("border-inline-end", value); }
    /** {@code border-inline-end} as one value. */
    public T borderInlineEnd(String value) { return prop("border-inline-end", value); }
    /** {@code border-block} as one value. */
    public T borderBlock(CSSValue value) { return prop("border-block", value); }
    /** {@code border-block} as one value. */
    public T borderBlock(String value) { return prop("border-block", value); }
    /** {@code border-block-start} as one value. */
    public T borderBlockStart(CSSValue value) { return prop("border-block-start", value); }
    /** {@code border-block-start} as one value. */
    public T borderBlockStart(String value) { return prop("border-block-start", value); }
    /** {@code border-block-end} as one value. */
    public T borderBlockEnd(CSSValue value) { return prop("border-block-end", value); }
    /** {@code border-block-end} as one value. */
    public T borderBlockEnd(String value) { return prop("border-block-end", value); }

    // Overflow logical properties
    /** Sets overflow-block (the block-axis counterpart of overflow-y). */
    public T overflowBlock(CSSValue value) { return prop("overflow-block", value); }
    /** Sets overflow-block. */
    public T overflowBlock(String value) { return prop("overflow-block", value); }
    /** Sets overflow-inline (the inline-axis counterpart of overflow-x). */
    public T overflowInline(CSSValue value) { return prop("overflow-inline", value); }
    /** Sets overflow-inline. */
    public T overflowInline(String value) { return prop("overflow-inline", value); }

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
     *
     * @deprecated Use {@code .position("relative")}.
     */
    @Deprecated
    public T relative() { return position(() -> "relative"); }

    /**
     * Preset: position: fixed
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .position("fixed")}.
     */
    @Deprecated
    public T fixed() { return position(() -> "fixed"); }

    /**
     * Preset: position: sticky
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .position("sticky")}.
     */
    @Deprecated
    public T sticky() { return position(() -> "sticky"); }

    /**
     * Preset: text-align: center
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .textAlign("center")}.
     */
    @Deprecated
    public T textCenter() { return textAlign(() -> "center"); }

    /**
     * Preset: font-weight: bold (700)
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .fontWeight(700)}.
     */
    @Deprecated
    public T bold() { return fontWeight(700); }

    /**
     * Preset: cursor: pointer
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .cursor("pointer")}.
     */
    @Deprecated
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
     *
     * @deprecated Use {@code .userSelect("none")}.
     */
    @Deprecated
    public T noSelect() { return userSelect(() -> "none"); }

    /**
     * Preset: border-radius with rounded corners.
     *
     * @param value the border-radius value
     * @return this builder for chaining
     *
     * @deprecated Use {@link #borderRadius(CSSValue)} — this is a pure alias.
     */
    @Deprecated
    public T rounded(CSSValue value) { return borderRadius(value); }

    // ==================== Border Radius Presets ====================

    /**
     * No border-radius (removes rounding).
     * Equivalent to: border-radius: 0
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius("0")}.
     */
    @Deprecated
    public T roundedNone() { return prop("border-radius", "0"); }

    /**
     * Extra-small border-radius (2px).
     * For subtle rounding on small elements.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(2))}.
     */
    @Deprecated
    public T roundedXs() { return prop("border-radius", "2px"); }

    /**
     * Small border-radius (4px).
     * For buttons and small cards.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(4))}.
     */
    @Deprecated
    public T roundedSm() { return prop("border-radius", "4px"); }

    /**
     * Default border-radius (6px).
     * For cards and containers.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(6))}.
     */
    @Deprecated
    public T roundedMd() { return prop("border-radius", "6px"); }

    /**
     * Large border-radius (8px).
     * For modals and larger elements.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(8))}.
     */
    @Deprecated
    public T roundedLg() { return prop("border-radius", "8px"); }

    /**
     * Extra-large border-radius (12px).
     * For prominent elements.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(12))}.
     */
    @Deprecated
    public T roundedXl() { return prop("border-radius", "12px"); }

    /**
     * 2XL border-radius (16px).
     * For very rounded elements.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(16))}.
     */
    @Deprecated
    public T rounded2xl() { return prop("border-radius", "16px"); }

    /**
     * 3XL border-radius (24px).
     * For pill-shaped elements.
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(24))}.
     */
    @Deprecated
    public T rounded3xl() { return prop("border-radius", "24px"); }

    /**
     * Full border-radius (9999px).
     * Creates perfect circles (when width = height) or pills (when width > height).
     *
     * @return this builder for chaining
     *
     * @deprecated Use {@code .borderRadius(px(9999))}.
     */
    @Deprecated
    public T roundedFull() { return prop("border-radius", "9999px"); }

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

    /**
     * Sets white-space-collapse — how white space inside the element collapses.
     *
     * @param value "collapse", "preserve", "preserve-breaks", "preserve-spaces" or "break-spaces"
     * @return this builder for chaining
     */
    public T whiteSpaceCollapse(CSSValue value) { return prop("white-space-collapse", value); }

    /** Sets white-space-collapse. @param value the white-space-collapse value */
    public T whiteSpaceCollapse(String value) { return prop("white-space-collapse", value); }

    /**
     * Clamps text to {@code lines} lines and ellipsises the overflow.
     *
     * <p>Emits the standard {@code line-clamp} plus the {@code -webkit-box}
     * fallback quartet that every current browser still needs:</p>
     * <pre>
     * display: -webkit-box;
     * -webkit-box-orient: vertical;
     * -webkit-line-clamp: 3;
     * line-clamp: 3;
     * overflow: hidden;
     * </pre>
     *
     * @param lines the maximum number of lines to show
     * @return this builder for chaining
     */
    public T lineClamp(int lines) {
        properties.put("display", "-webkit-box");
        properties.put("-webkit-box-orient", "vertical");
        properties.put("-webkit-line-clamp", String.valueOf(lines));
        properties.put("line-clamp", String.valueOf(lines));
        properties.put("overflow", "hidden");
        return self();
    }

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

    // ==================== Anchor Positioning ====================

    /**
     * Sets anchor-name — names this element so others can anchor to it.
     *
     * <p>Example:</p>
     * <pre>
     * rule(".menu-button").anchorName("--menu")
     * </pre>
     *
     * @param name the dashed-ident anchor name (e.g. {@code "--menu"})
     * @return this builder for chaining
     */
    public T anchorName(String name) { return prop("anchor-name", name); }

    /** Sets anchor-name. @param value the dashed-ident anchor name */
    public T anchorName(CSSValue value) { return prop("anchor-name", value); }

    /**
     * Sets position-anchor — links this positioned element to a named anchor.
     *
     * @param name the dashed-ident anchor name (e.g. {@code "--menu"})
     * @return this builder for chaining
     */
    public T positionAnchor(String name) { return prop("position-anchor", name); }

    /** Sets position-anchor. @param value the dashed-ident anchor name */
    public T positionAnchor(CSSValue value) { return prop("position-anchor", value); }

    /**
     * Sets position-area — places this element in a region around its anchor.
     *
     * @param value e.g. {@code "top"}, {@code "bottom right"}, {@code "span-all"}
     * @return this builder for chaining
     */
    public T positionArea(String value) { return prop("position-area", value); }

    /** Sets position-area. @param value the position-area value */
    public T positionArea(CSSValue value) { return prop("position-area", value); }

    /**
     * Sets position-visibility — when an anchored element stays visible.
     *
     * @param value {@code "always"}, {@code "anchors-visible"} or {@code "no-overflow"}
     * @return this builder for chaining
     */
    public T positionVisibility(String value) { return prop("position-visibility", value); }

    /** Sets position-visibility. @param value the position-visibility value */
    public T positionVisibility(CSSValue value) { return prop("position-visibility", value); }

    /**
     * Sets position-try-fallbacks — the ordered fallback positions to try
     * when the anchored element would overflow.
     *
     * <p>Example:</p>
     * <pre>
     * style().positionTryFallbacks("flip-block, flip-inline")
     * style().positionTryFallbacks("--my-fallback")
     * </pre>
     *
     * @param value a comma-separated fallback list
     * @return this builder for chaining
     */
    public T positionTryFallbacks(String value) { return prop("position-try-fallbacks", value); }

    /** Sets position-try-fallbacks. @param value the fallback list */
    public T positionTryFallbacks(CSSValue value) { return prop("position-try-fallbacks", value); }

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

    // ==================== String Value Overloads (CSS parity) ====================
    //
    // Every single-value property setter also accepts a plain CSS string, so
    // anything you can write in a stylesheet you can write here verbatim:
    //
    //     style().display("flex")
    //            .cursor("copy")
    //            .margin("0 auto")
    //            .transition("color .2s ease, transform .3s ease-out")
    //            .gridTemplateColumns("repeat(3, 1fr)")
    //
    // The value is passed through verbatim. The one exception is
    // content(String), which quotes its argument (see its javadoc).
    //
    // These coexist with the typed CSSValue overloads (and with the int/double
    // ones): the constants in Css stay available as autocomplete sugar, they
    // are simply no longer mandatory.

    /** {@code display: <value>} from a plain CSS string. */
    public T display(String value) { return prop("display", value); }

    /** {@code box-sizing: <value>} from a plain CSS string. */
    public T boxSizing(String value) { return prop("box-sizing", value); }

    /** {@code width: <value>} from a plain CSS string. */
    public T width(String value) { return prop("width", value); }

    /** {@code height: <value>} from a plain CSS string. */
    public T height(String value) { return prop("height", value); }

    /** {@code min-width: <value>} from a plain CSS string. */
    public T minWidth(String value) { return prop("min-width", value); }

    /** {@code max-width: <value>} from a plain CSS string. */
    public T maxWidth(String value) { return prop("max-width", value); }

    /** {@code min-height: <value>} from a plain CSS string. */
    public T minHeight(String value) { return prop("min-height", value); }

    /** {@code max-height: <value>} from a plain CSS string. */
    public T maxHeight(String value) { return prop("max-height", value); }

    /** {@code margin: <value>} from a plain CSS string. */
    public T margin(String value) { return prop("margin", value); }

    /** {@code margin-top: <value>} from a plain CSS string. */
    public T marginTop(String value) { return prop("margin-top", value); }

    /** {@code margin-right: <value>} from a plain CSS string. */
    public T marginRight(String value) { return prop("margin-right", value); }

    /** {@code margin-bottom: <value>} from a plain CSS string. */
    public T marginBottom(String value) { return prop("margin-bottom", value); }

    /** {@code margin-left: <value>} from a plain CSS string. */
    public T marginLeft(String value) { return prop("margin-left", value); }

    /** {@code padding: <value>} from a plain CSS string. */
    public T padding(String value) { return prop("padding", value); }

    /** {@code padding-top: <value>} from a plain CSS string. */
    public T paddingTop(String value) { return prop("padding-top", value); }

    /** {@code padding-right: <value>} from a plain CSS string. */
    public T paddingRight(String value) { return prop("padding-right", value); }

    /** {@code padding-bottom: <value>} from a plain CSS string. */
    public T paddingBottom(String value) { return prop("padding-bottom", value); }

    /** {@code padding-left: <value>} from a plain CSS string. */
    public T paddingLeft(String value) { return prop("padding-left", value); }

    /** {@code border: <value>} from a plain CSS string. */
    public T border(String value) { return prop("border", value); }

    /** {@code border-width: <value>} from a plain CSS string. */
    public T borderWidth(String value) { return prop("border-width", value); }

    /** {@code border-style: <value>} from a plain CSS string. */
    public T borderStyle(String value) { return prop("border-style", value); }

    /** {@code border-color: <value>} from a plain CSS string. */
    public T borderColor(String value) { return prop("border-color", value); }

    /** {@code border-radius: <value>} from a plain CSS string. */
    public T borderRadius(String value) { return prop("border-radius", value); }

    /** {@code border-top-left-radius: <value>} from a plain CSS string. */
    public T borderTopLeftRadius(String value) { return prop("border-top-left-radius", value); }

    /** {@code border-top-right-radius: <value>} from a plain CSS string. */
    public T borderTopRightRadius(String value) { return prop("border-top-right-radius", value); }

    /** {@code border-bottom-right-radius: <value>} from a plain CSS string. */
    public T borderBottomRightRadius(String value) { return prop("border-bottom-right-radius", value); }

    /** {@code border-bottom-left-radius: <value>} from a plain CSS string. */
    public T borderBottomLeftRadius(String value) { return prop("border-bottom-left-radius", value); }

    /** {@code background: <value>} from a plain CSS string. */
    public T background(String value) { return prop("background", value); }

    /** {@code background-color: <value>} from a plain CSS string. */
    public T backgroundColor(String value) { return prop("background-color", value); }

    /** {@code background-image: <value>} from a plain CSS string. */
    public T backgroundImage(String value) { return prop("background-image", value); }

    /** {@code background-size: <value>} from a plain CSS string. */
    public T backgroundSize(String value) { return prop("background-size", value); }

    /** {@code background-position: <value>} from a plain CSS string. */
    public T backgroundPosition(String value) { return prop("background-position", value); }

    /** {@code background-repeat: <value>} from a plain CSS string. */
    public T backgroundRepeat(String value) { return prop("background-repeat", value); }

    /** {@code background-attachment: <value>} from a plain CSS string. */
    public T backgroundAttachment(String value) { return prop("background-attachment", value); }

    /** {@code color: <value>} from a plain CSS string. */
    public T color(String value) { return prop("color", value); }

    /** {@code font-size: <value>} from a plain CSS string. */
    public T fontSize(String value) { return prop("font-size", value); }

    /** {@code font-weight: <value>} from a plain CSS string. */
    public T fontWeight(String value) { return prop("font-weight", value); }

    /** {@code font-style: <value>} from a plain CSS string. */
    public T fontStyle(String value) { return prop("font-style", value); }

    /** {@code line-height: <value>} from a plain CSS string. */
    public T lineHeight(String value) { return prop("line-height", value); }

    /** {@code letter-spacing: <value>} from a plain CSS string. */
    public T letterSpacing(String value) { return prop("letter-spacing", value); }

    /** {@code word-spacing: <value>} from a plain CSS string. */
    public T wordSpacing(String value) { return prop("word-spacing", value); }

    /** {@code text-align: <value>} from a plain CSS string. */
    public T textAlign(String value) { return prop("text-align", value); }

    /** {@code text-decoration: <value>} from a plain CSS string. */
    public T textDecoration(String value) { return prop("text-decoration", value); }

    /** {@code text-transform: <value>} from a plain CSS string. */
    public T textTransform(String value) { return prop("text-transform", value); }

    /** {@code text-indent: <value>} from a plain CSS string. */
    public T textIndent(String value) { return prop("text-indent", value); }

    /** {@code white-space: <value>} from a plain CSS string. */
    public T whiteSpace(String value) { return prop("white-space", value); }

    /** {@code word-break: <value>} from a plain CSS string. */
    public T wordBreak(String value) { return prop("word-break", value); }

    /** {@code overflow-wrap: <value>} from a plain CSS string. */
    public T overflowWrap(String value) { return prop("overflow-wrap", value); }

    /** {@code flex-direction: <value>} from a plain CSS string. */
    public T flexDirection(String value) { return prop("flex-direction", value); }

    /** {@code flex-wrap: <value>} from a plain CSS string. */
    public T flexWrap(String value) { return prop("flex-wrap", value); }

    /** {@code justify-content: <value>} from a plain CSS string. */
    public T justifyContent(String value) { return prop("justify-content", value); }

    /** {@code align-items: <value>} from a plain CSS string. */
    public T alignItems(String value) { return prop("align-items", value); }

    /** {@code align-content: <value>} from a plain CSS string. */
    public T alignContent(String value) { return prop("align-content", value); }

    /** {@code align-self: <value>} from a plain CSS string. */
    public T alignSelf(String value) { return prop("align-self", value); }

    /** {@code flex: <value>} from a plain CSS string. */
    public T flex(String value) { return prop("flex", value); }

    /** {@code flex-basis: <value>} from a plain CSS string. */
    public T flexBasis(String value) { return prop("flex-basis", value); }

    /** {@code gap: <value>} from a plain CSS string. */
    public T gap(String value) { return prop("gap", value); }

    /** {@code row-gap: <value>} from a plain CSS string. */
    public T rowGap(String value) { return prop("row-gap", value); }

    /** {@code column-gap: <value>} from a plain CSS string. */
    public T columnGap(String value) { return prop("column-gap", value); }

    /** {@code grid-auto-columns: <value>} from a plain CSS string. */
    public T gridAutoColumns(String value) { return prop("grid-auto-columns", value); }

    /** {@code grid-auto-rows: <value>} from a plain CSS string. */
    public T gridAutoRows(String value) { return prop("grid-auto-rows", value); }

    /** {@code grid-auto-flow: <value>} from a plain CSS string. */
    public T gridAutoFlow(String value) { return prop("grid-auto-flow", value); }

    /** {@code justify-items: <value>} from a plain CSS string. */
    public T justifyItems(String value) { return prop("justify-items", value); }

    /** {@code place-items: <value>} from a plain CSS string. */
    public T placeItems(String value) { return prop("place-items", value); }

    /** {@code place-content: <value>} from a plain CSS string. */
    public T placeContent(String value) { return prop("place-content", value); }

    /** {@code place-self: <value>} from a plain CSS string. */
    public T placeSelf(String value) { return prop("place-self", value); }

    /** {@code position: <value>} from a plain CSS string. */
    public T position(String value) { return prop("position", value); }

    /** {@code top: <value>} from a plain CSS string. */
    public T top(String value) { return prop("top", value); }

    /** {@code right: <value>} from a plain CSS string. */
    public T right(String value) { return prop("right", value); }

    /** {@code bottom: <value>} from a plain CSS string. */
    public T bottom(String value) { return prop("bottom", value); }

    /** {@code left: <value>} from a plain CSS string. */
    public T left(String value) { return prop("left", value); }

    /** {@code inset: <value>} from a plain CSS string. */
    public T inset(String value) { return prop("inset", value); }

    /** {@code overflow: <value>} from a plain CSS string. */
    public T overflow(String value) { return prop("overflow", value); }

    /** {@code overflow-x: <value>} from a plain CSS string. */
    public T overflowX(String value) { return prop("overflow-x", value); }

    /** {@code overflow-y: <value>} from a plain CSS string. */
    public T overflowY(String value) { return prop("overflow-y", value); }

    /** {@code visibility: <value>} from a plain CSS string. */
    public T visibility(String value) { return prop("visibility", value); }

    /** {@code cursor: <value>} from a plain CSS string. */
    public T cursor(String value) { return prop("cursor", value); }

    /** {@code pointer-events: <value>} from a plain CSS string. */
    public T pointerEvents(String value) { return prop("pointer-events", value); }

    /** {@code user-select: <value>} from a plain CSS string. */
    public T userSelect(String value) { return prop("user-select", value); }

    /** {@code resize: <value>} from a plain CSS string. */
    public T resize(String value) { return prop("resize", value); }

    /** {@code transition: <value>} from a plain CSS string. */
    public T transition(String value) { return prop("transition", value); }

    /** {@code transition-property: <value>} from a plain CSS string. */
    public T transitionProperty(String value) { return prop("transition-property", value); }

    /** {@code transition-duration: <value>} from a plain CSS string. */
    public T transitionDuration(String value) { return prop("transition-duration", value); }

    /** {@code transition-timing-function: <value>} from a plain CSS string. */
    public T transitionTimingFunction(String value) { return prop("transition-timing-function", value); }

    /** {@code transition-delay: <value>} from a plain CSS string. */
    public T transitionDelay(String value) { return prop("transition-delay", value); }

    /** {@code animation-name: <value>} from a plain CSS string. */
    public T animationName(String value) { return prop("animation-name", value); }

    /** {@code animation-duration: <value>} from a plain CSS string. */
    public T animationDuration(String value) { return prop("animation-duration", value); }

    /** {@code animation-timing-function: <value>} from a plain CSS string. */
    public T animationTimingFunction(String value) { return prop("animation-timing-function", value); }

    /** {@code animation-delay: <value>} from a plain CSS string. */
    public T animationDelay(String value) { return prop("animation-delay", value); }

    /** {@code animation-iteration-count: <value>} from a plain CSS string. */
    public T animationIterationCount(String value) { return prop("animation-iteration-count", value); }

    /** {@code animation-direction: <value>} from a plain CSS string. */
    public T animationDirection(String value) { return prop("animation-direction", value); }

    /** {@code animation-fill-mode: <value>} from a plain CSS string. */
    public T animationFillMode(String value) { return prop("animation-fill-mode", value); }

    /** {@code outline: <value>} from a plain CSS string. */
    public T outline(String value) { return prop("outline", value); }

    /** {@code outline-offset: <value>} from a plain CSS string. */
    public T outlineOffset(String value) { return prop("outline-offset", value); }

    /** {@code list-style: <value>} from a plain CSS string. */
    public T listStyle(String value) { return prop("list-style", value); }

    /** {@code list-style-type: <value>} from a plain CSS string. */
    public T listStyleType(String value) { return prop("list-style-type", value); }

    /** {@code list-style-position: <value>} from a plain CSS string. */
    public T listStylePosition(String value) { return prop("list-style-position", value); }

    /** {@code object-fit: <value>} from a plain CSS string. */
    public T objectFit(String value) { return prop("object-fit", value); }

    /** {@code clip-path: <value>} from a plain CSS string. */
    public T clipPath(String value) { return prop("clip-path", value); }

    /** {@code border-collapse: <value>} from a plain CSS string. */
    public T borderCollapse(String value) { return prop("border-collapse", value); }

    /** {@code border-spacing: <value>} from a plain CSS string. */
    public T borderSpacing(String value) { return prop("border-spacing", value); }

    /** {@code table-layout: <value>} from a plain CSS string. */
    public T tableLayout(String value) { return prop("table-layout", value); }

    /** {@code vertical-align: <value>} from a plain CSS string. */
    public T verticalAlign(String value) { return prop("vertical-align", value); }

    /** {@code -webkit-font-smoothing: <value>} from a plain CSS string. */
    public T webkitFontSmoothing(String value) { return prop("-webkit-font-smoothing", value); }

    /** {@code -moz-osx-font-smoothing: <value>} from a plain CSS string. */
    public T mozOsxFontSmoothing(String value) { return prop("-moz-osx-font-smoothing", value); }

    /** {@code background-clip: <value>} from a plain CSS string. */
    public T backgroundClip(String value) { return prop("background-clip", value); }

    /** {@code -webkit-background-clip: <value>} from a plain CSS string. */
    public T webkitBackgroundClip(String value) { return prop("-webkit-background-clip", value); }

    /** {@code -webkit-text-fill-color: <value>} from a plain CSS string. */
    public T webkitTextFillColor(String value) { return prop("-webkit-text-fill-color", value); }

    /** {@code margin-inline: <value>} from a plain CSS string. */
    public T marginInline(String value) { return prop("margin-inline", value); }

    /** {@code margin-inline-start: <value>} from a plain CSS string. */
    public T marginInlineStart(String value) { return prop("margin-inline-start", value); }

    /** {@code margin-inline-end: <value>} from a plain CSS string. */
    public T marginInlineEnd(String value) { return prop("margin-inline-end", value); }

    /** {@code margin-block: <value>} from a plain CSS string. */
    public T marginBlock(String value) { return prop("margin-block", value); }

    /** {@code margin-block-start: <value>} from a plain CSS string. */
    public T marginBlockStart(String value) { return prop("margin-block-start", value); }

    /** {@code margin-block-end: <value>} from a plain CSS string. */
    public T marginBlockEnd(String value) { return prop("margin-block-end", value); }

    /** {@code padding-inline: <value>} from a plain CSS string. */
    public T paddingInline(String value) { return prop("padding-inline", value); }

    /** {@code padding-inline-start: <value>} from a plain CSS string. */
    public T paddingInlineStart(String value) { return prop("padding-inline-start", value); }

    /** {@code padding-inline-end: <value>} from a plain CSS string. */
    public T paddingInlineEnd(String value) { return prop("padding-inline-end", value); }

    /** {@code padding-block: <value>} from a plain CSS string. */
    public T paddingBlock(String value) { return prop("padding-block", value); }

    /** {@code padding-block-start: <value>} from a plain CSS string. */
    public T paddingBlockStart(String value) { return prop("padding-block-start", value); }

    /** {@code padding-block-end: <value>} from a plain CSS string. */
    public T paddingBlockEnd(String value) { return prop("padding-block-end", value); }

    /** {@code inline-size: <value>} from a plain CSS string. */
    public T inlineSize(String value) { return prop("inline-size", value); }

    /** {@code block-size: <value>} from a plain CSS string. */
    public T blockSize(String value) { return prop("block-size", value); }

    /** {@code min-inline-size: <value>} from a plain CSS string. */
    public T minInlineSize(String value) { return prop("min-inline-size", value); }

    /** {@code max-inline-size: <value>} from a plain CSS string. */
    public T maxInlineSize(String value) { return prop("max-inline-size", value); }

    /** {@code min-block-size: <value>} from a plain CSS string. */
    public T minBlockSize(String value) { return prop("min-block-size", value); }

    /** {@code max-block-size: <value>} from a plain CSS string. */
    public T maxBlockSize(String value) { return prop("max-block-size", value); }

    /** {@code inset-inline: <value>} from a plain CSS string. */
    public T insetInline(String value) { return prop("inset-inline", value); }

    /** {@code inset-inline-start: <value>} from a plain CSS string. */
    public T insetInlineStart(String value) { return prop("inset-inline-start", value); }

    /** {@code inset-inline-end: <value>} from a plain CSS string. */
    public T insetInlineEnd(String value) { return prop("inset-inline-end", value); }

    /** {@code inset-block: <value>} from a plain CSS string. */
    public T insetBlock(String value) { return prop("inset-block", value); }

    /** {@code inset-block-start: <value>} from a plain CSS string. */
    public T insetBlockStart(String value) { return prop("inset-block-start", value); }

    /** {@code inset-block-end: <value>} from a plain CSS string. */
    public T insetBlockEnd(String value) { return prop("inset-block-end", value); }

    /** {@code border-start-start-radius: <value>} from a plain CSS string. */
    public T borderStartStartRadius(String value) { return prop("border-start-start-radius", value); }

    /** {@code border-start-end-radius: <value>} from a plain CSS string. */
    public T borderStartEndRadius(String value) { return prop("border-start-end-radius", value); }

    /** {@code border-end-start-radius: <value>} from a plain CSS string. */
    public T borderEndStartRadius(String value) { return prop("border-end-start-radius", value); }

    /** {@code border-end-end-radius: <value>} from a plain CSS string. */
    public T borderEndEndRadius(String value) { return prop("border-end-end-radius", value); }

    /** {@code text-align-last: <value>} from a plain CSS string. */
    public T textAlignLast(String value) { return prop("text-align-last", value); }

    /** {@code scroll-snap-type: <value>} from a plain CSS string. */
    public T scrollSnapType(String value) { return prop("scroll-snap-type", value); }

    /** {@code scroll-snap-align: <value>} from a plain CSS string. */
    public T scrollSnapAlign(String value) { return prop("scroll-snap-align", value); }

    /** {@code scroll-snap-stop: <value>} from a plain CSS string. */
    public T scrollSnapStop(String value) { return prop("scroll-snap-stop", value); }

    /** {@code scroll-padding: <value>} from a plain CSS string. */
    public T scrollPadding(String value) { return prop("scroll-padding", value); }

    /** {@code scroll-margin: <value>} from a plain CSS string. */
    public T scrollMargin(String value) { return prop("scroll-margin", value); }

    /** {@code scroll-behavior: <value>} from a plain CSS string. */
    public T scrollBehavior(String value) { return prop("scroll-behavior", value); }

    /** {@code overscroll-behavior: <value>} from a plain CSS string. */
    public T overscrollBehavior(String value) { return prop("overscroll-behavior", value); }

    /** {@code overscroll-behavior-x: <value>} from a plain CSS string. */
    public T overscrollBehaviorX(String value) { return prop("overscroll-behavior-x", value); }

    /** {@code overscroll-behavior-y: <value>} from a plain CSS string. */
    public T overscrollBehaviorY(String value) { return prop("overscroll-behavior-y", value); }

    /** {@code text-wrap: <value>} from a plain CSS string. */
    public T textWrap(String value) { return prop("text-wrap", value); }

    /** {@code text-wrap-mode: <value>} from a plain CSS string. */
    public T textWrapMode(String value) { return prop("text-wrap-mode", value); }

    /** {@code text-wrap-style: <value>} from a plain CSS string. */
    public T textWrapStyle(String value) { return prop("text-wrap-style", value); }

    /** {@code animation-timeline: <value>} from a plain CSS string. */
    public T animationTimeline(String value) { return prop("animation-timeline", value); }

    /** {@code animation-range: <value>} from a plain CSS string. */
    public T animationRange(String value) { return prop("animation-range", value); }

    /** {@code scroll-timeline: <value>} from a plain CSS string. */
    public T scrollTimeline(String value) { return prop("scroll-timeline", value); }

    /** {@code scroll-timeline-axis: <value>} from a plain CSS string. */
    public T scrollTimelineAxis(String value) { return prop("scroll-timeline-axis", value); }

    /** {@code view-timeline: <value>} from a plain CSS string. */
    public T viewTimeline(String value) { return prop("view-timeline", value); }

    /** {@code view-timeline-axis: <value>} from a plain CSS string. */
    public T viewTimelineAxis(String value) { return prop("view-timeline-axis", value); }

    /** {@code view-timeline-inset: <value>} from a plain CSS string. */
    public T viewTimelineInset(String value) { return prop("view-timeline-inset", value); }

    /** {@code container-type: <value>} from a plain CSS string. */
    public T containerType(String value) { return prop("container-type", value); }

    /** {@code accent-color: <value>} from a plain CSS string. */
    public T accentColor(String value) { return prop("accent-color", value); }

    /** {@code color-scheme: <value>} from a plain CSS string. */
    public T colorScheme(String value) { return prop("color-scheme", value); }

    /** {@code forced-color-adjust: <value>} from a plain CSS string. */
    public T forcedColorAdjust(String value) { return prop("forced-color-adjust", value); }

    /** {@code print-color-adjust: <value>} from a plain CSS string. */
    public T printColorAdjust(String value) { return prop("print-color-adjust", value); }

    /** {@code columns: <value>} from a plain CSS string. */
    public T columns(String value) { return prop("columns", value); }

    /** {@code column-count: <value>} from a plain CSS string. */
    public T columnCount(String value) { return prop("column-count", value); }

    /** {@code column-width: <value>} from a plain CSS string. */
    public T columnWidth(String value) { return prop("column-width", value); }

    /** {@code column-rule: <value>} from a plain CSS string. */
    public T columnRule(String value) { return prop("column-rule", value); }

    /** {@code column-rule-width: <value>} from a plain CSS string. */
    public T columnRuleWidth(String value) { return prop("column-rule-width", value); }

    /** {@code column-rule-style: <value>} from a plain CSS string. */
    public T columnRuleStyle(String value) { return prop("column-rule-style", value); }

    /** {@code column-rule-color: <value>} from a plain CSS string. */
    public T columnRuleColor(String value) { return prop("column-rule-color", value); }

    /** {@code column-span: <value>} from a plain CSS string. */
    public T columnSpan(String value) { return prop("column-span", value); }

    /** {@code column-fill: <value>} from a plain CSS string. */
    public T columnFill(String value) { return prop("column-fill", value); }

    /** {@code float: <value>} from a plain CSS string. */
    public T float_(String value) { return prop("float", value); }

    /** {@code clear: <value>} from a plain CSS string. */
    public T clear(String value) { return prop("clear", value); }

    /** {@code text-decoration-line: <value>} from a plain CSS string. */
    public T textDecorationLine(String value) { return prop("text-decoration-line", value); }

    /** {@code text-decoration-color: <value>} from a plain CSS string. */
    public T textDecorationColor(String value) { return prop("text-decoration-color", value); }

    /** {@code text-decoration-style: <value>} from a plain CSS string. */
    public T textDecorationStyle(String value) { return prop("text-decoration-style", value); }

    /** {@code text-decoration-thickness: <value>} from a plain CSS string. */
    public T textDecorationThickness(String value) { return prop("text-decoration-thickness", value); }

    /** {@code text-underline-offset: <value>} from a plain CSS string. */
    public T textUnderlineOffset(String value) { return prop("text-underline-offset", value); }

    /** {@code text-underline-position: <value>} from a plain CSS string. */
    public T textUnderlinePosition(String value) { return prop("text-underline-position", value); }

    /** {@code text-decoration-skip-ink: <value>} from a plain CSS string. */
    public T textDecorationSkipInk(String value) { return prop("text-decoration-skip-ink", value); }

    /** {@code text-emphasis: <value>} from a plain CSS string. */
    public T textEmphasis(String value) { return prop("text-emphasis", value); }

    /** {@code text-emphasis-style: <value>} from a plain CSS string. */
    public T textEmphasisStyle(String value) { return prop("text-emphasis-style", value); }

    /** {@code text-emphasis-color: <value>} from a plain CSS string. */
    public T textEmphasisColor(String value) { return prop("text-emphasis-color", value); }

    /** {@code font-variant: <value>} from a plain CSS string. */
    public T fontVariant(String value) { return prop("font-variant", value); }

    /** {@code font-variant-caps: <value>} from a plain CSS string. */
    public T fontVariantCaps(String value) { return prop("font-variant-caps", value); }

    /** {@code font-variant-numeric: <value>} from a plain CSS string. */
    public T fontVariantNumeric(String value) { return prop("font-variant-numeric", value); }

    /** {@code font-variant-ligatures: <value>} from a plain CSS string. */
    public T fontVariantLigatures(String value) { return prop("font-variant-ligatures", value); }

    /** {@code font-variant-alternates: <value>} from a plain CSS string. */
    public T fontVariantAlternates(String value) { return prop("font-variant-alternates", value); }

    /** {@code font-variant-east-asian: <value>} from a plain CSS string. */
    public T fontVariantEastAsian(String value) { return prop("font-variant-east-asian", value); }

    /** {@code font-variant-position: <value>} from a plain CSS string. */
    public T fontVariantPosition(String value) { return prop("font-variant-position", value); }

    /** {@code font-optical-sizing: <value>} from a plain CSS string. */
    public T fontOpticalSizing(String value) { return prop("font-optical-sizing", value); }

    /** {@code font-kerning: <value>} from a plain CSS string. */
    public T fontKerning(String value) { return prop("font-kerning", value); }

    /** {@code font-stretch: <value>} from a plain CSS string. */
    public T fontStretch(String value) { return prop("font-stretch", value); }

    /** {@code font-size-adjust: <value>} from a plain CSS string. */
    public T fontSizeAdjust(String value) { return prop("font-size-adjust", value); }

    /** {@code writing-mode: <value>} from a plain CSS string. */
    public T writingMode(String value) { return prop("writing-mode", value); }

    /** {@code direction: <value>} from a plain CSS string. */
    public T direction(String value) { return prop("direction", value); }

    /** {@code text-orientation: <value>} from a plain CSS string. */
    public T textOrientation(String value) { return prop("text-orientation", value); }

    /** {@code unicode-bidi: <value>} from a plain CSS string. */
    public T unicodeBidi(String value) { return prop("unicode-bidi", value); }

    /** {@code mask: <value>} from a plain CSS string. */
    public T mask(String value) { return prop("mask", value); }

    /** {@code mask-image: <value>} from a plain CSS string. */
    public T maskImage(String value) { return prop("mask-image", value); }

    /** {@code mask-mode: <value>} from a plain CSS string. */
    public T maskMode(String value) { return prop("mask-mode", value); }

    /** {@code mask-repeat: <value>} from a plain CSS string. */
    public T maskRepeat(String value) { return prop("mask-repeat", value); }

    /** {@code mask-position: <value>} from a plain CSS string. */
    public T maskPosition(String value) { return prop("mask-position", value); }

    /** {@code mask-clip: <value>} from a plain CSS string. */
    public T maskClip(String value) { return prop("mask-clip", value); }

    /** {@code mask-origin: <value>} from a plain CSS string. */
    public T maskOrigin(String value) { return prop("mask-origin", value); }

    /** {@code mask-size: <value>} from a plain CSS string. */
    public T maskSize(String value) { return prop("mask-size", value); }

    /** {@code mask-composite: <value>} from a plain CSS string. */
    public T maskComposite(String value) { return prop("mask-composite", value); }

    /** {@code mask-type: <value>} from a plain CSS string. */
    public T maskType(String value) { return prop("mask-type", value); }

    /** {@code shape-outside: <value>} from a plain CSS string. */
    public T shapeOutside(String value) { return prop("shape-outside", value); }

    /** {@code shape-margin: <value>} from a plain CSS string. */
    public T shapeMargin(String value) { return prop("shape-margin", value); }

    /** {@code mix-blend-mode: <value>} from a plain CSS string. */
    public T mixBlendMode(String value) { return prop("mix-blend-mode", value); }

    /** {@code background-blend-mode: <value>} from a plain CSS string. */
    public T backgroundBlendMode(String value) { return prop("background-blend-mode", value); }

    /** {@code isolation: <value>} from a plain CSS string. */
    public T isolation(String value) { return prop("isolation", value); }

    /** {@code contain: <value>} from a plain CSS string. */
    public T contain(String value) { return prop("contain", value); }

    /** {@code content-visibility: <value>} from a plain CSS string. */
    public T contentVisibility(String value) { return prop("content-visibility", value); }

    /** {@code contain-intrinsic-size: <value>} from a plain CSS string. */
    public T containIntrinsicSize(String value) { return prop("contain-intrinsic-size", value); }

    /** {@code page-break-before: <value>} from a plain CSS string. */
    public T pageBreakBefore(String value) { return prop("page-break-before", value); }

    /** {@code page-break-after: <value>} from a plain CSS string. */
    public T pageBreakAfter(String value) { return prop("page-break-after", value); }

    /** {@code page-break-inside: <value>} from a plain CSS string. */
    public T pageBreakInside(String value) { return prop("page-break-inside", value); }

    /** {@code break-before: <value>} from a plain CSS string. */
    public T breakBefore(String value) { return prop("break-before", value); }

    /** {@code break-after: <value>} from a plain CSS string. */
    public T breakAfter(String value) { return prop("break-after", value); }

    /** {@code break-inside: <value>} from a plain CSS string. */
    public T breakInside(String value) { return prop("break-inside", value); }

    /** {@code tab-size: <value>} from a plain CSS string. */
    public T tabSize(String value) { return prop("tab-size", value); }

    /** {@code hyphens: <value>} from a plain CSS string. */
    public T hyphens(String value) { return prop("hyphens", value); }

    /** {@code caret-color: <value>} from a plain CSS string. */
    public T caretColor(String value) { return prop("caret-color", value); }

    /** {@code appearance: <value>} from a plain CSS string. */
    public T appearance(String value) { return prop("appearance", value); }

    /** {@code touch-action: <value>} from a plain CSS string. */
    public T touchAction(String value) { return prop("touch-action", value); }

    /** {@code scroll-margin-top: <value>} from a plain CSS string. */
    public T scrollMarginTop(String value) { return prop("scroll-margin-top", value); }

    /** {@code scroll-margin-right: <value>} from a plain CSS string. */
    public T scrollMarginRight(String value) { return prop("scroll-margin-right", value); }

    /** {@code scroll-margin-bottom: <value>} from a plain CSS string. */
    public T scrollMarginBottom(String value) { return prop("scroll-margin-bottom", value); }

    /** {@code scroll-margin-left: <value>} from a plain CSS string. */
    public T scrollMarginLeft(String value) { return prop("scroll-margin-left", value); }

    /** {@code scroll-padding-top: <value>} from a plain CSS string. */
    public T scrollPaddingTop(String value) { return prop("scroll-padding-top", value); }

    /** {@code scroll-padding-right: <value>} from a plain CSS string. */
    public T scrollPaddingRight(String value) { return prop("scroll-padding-right", value); }

    /** {@code scroll-padding-bottom: <value>} from a plain CSS string. */
    public T scrollPaddingBottom(String value) { return prop("scroll-padding-bottom", value); }

    /** {@code scroll-padding-left: <value>} from a plain CSS string. */
    public T scrollPaddingLeft(String value) { return prop("scroll-padding-left", value); }

    /** {@code image-rendering: <value>} from a plain CSS string. */
    public T imageRendering(String value) { return prop("image-rendering", value); }

    /** {@code text-overflow: <value>} from a plain CSS string. */
    public T textOverflow(String value) { return prop("text-overflow", value); }

    /** {@code all: <value>} from a plain CSS string. */
    public T all(String value) { return prop("all", value); }

    // --- multi-property shorthands (String forms) ---

    /** Sets left and right margin from a plain CSS string. */
    public T marginX(String value) { return marginLeft(value).marginRight(value); }
    /** Sets top and bottom margin from a plain CSS string. */
    public T marginY(String value) { return marginTop(value).marginBottom(value); }
    /** Sets left and right padding from a plain CSS string. */
    public T paddingX(String value) { return paddingLeft(value).paddingRight(value); }
    /** Sets top and bottom padding from a plain CSS string. */
    public T paddingY(String value) { return paddingTop(value).paddingBottom(value); }

    /**
     * {@code animation: <value>} from a plain CSS string — the whole shorthand
     * in one go, matching what you would write in a stylesheet.
     *
     * <pre>
     * style().animation("spin 2s linear infinite")
     * </pre>
     *
     * @param value the animation shorthand
     * @return this builder for chaining
     */
    public T animation(String value) { return prop("animation", value); }

    // --- numeric properties also accept a string (keywords, var(), calc()) ---

    /** {@code opacity: <value>} from a string, e.g. {@code "var(--o)"}. */
    public T opacity(String value) { return prop("opacity", value); }
    /** {@code z-index: <value>} from a string, e.g. {@code "auto"}. */
    public T zIndex(String value) { return prop("z-index", value); }
    /** {@code order: <value>} from a string. */
    public T order(String value) { return prop("order", value); }
    /** {@code flex-grow: <value>} from a string. */
    public T flexGrow(String value) { return prop("flex-grow", value); }
    /** {@code flex-shrink: <value>} from a string. */
    public T flexShrink(String value) { return prop("flex-shrink", value); }
    /** {@code orphans: <value>} from a string. */
    public T orphans(String value) { return prop("orphans", value); }
    /** {@code widows: <value>} from a string. */
    public T widows(String value) { return prop("widows", value); }
    /** {@code shape-image-threshold: <value>} from a string. */
    public T shapeImageThreshold(String value) { return prop("shape-image-threshold", value); }


    // ==================== Composition ====================

    /**
     * Merges another style's properties into this one — the composition
     * primitive for shared style fragments. Later properties win, so call
     * order reads naturally.
     *
     * <p>Example — define a fragment once, reuse everywhere:</p>
     * <pre>
     * static Style&lt;?&gt; brandGradient() {
     *     return style().background(BRAND_GRADIENT)
     *                   .backgroundSize(percent(300), percent(100))
     *                   .animation(anim("shift"), s(3), linear, s(0), infinite);
     * }
     *
     * button(attrs().style().padding(SP_3).apply(brandGradient()).done(), ...)
     * </pre>
     *
     * @param fragment the style whose properties to merge in
     * @return this builder for chaining
     */
    public T apply(Style<?> fragment) {
        properties.putAll(fragment.properties);
        return self();
    }

    // ==================== Pseudo-element / Mask Helpers ====================

    /**
     * Visually hides an element while keeping it readable by screen readers
     * (the standard "sr-only" pattern).
     */
    public T srOnly() {
        properties.put("position", "absolute");
        properties.put("width", "1px");
        properties.put("height", "1px");
        properties.put("padding", "0");
        properties.put("margin", "-1px");
        properties.put("overflow", "hidden");
        properties.put("clip", "rect(0, 0, 0, 0)");
        properties.put("white-space", "nowrap");
        properties.put("border", "0");
        return self();
    }

    /** Sets {@code content: ''} — required for ::before/::after pseudo-elements. */
    public T content() {
        properties.put("content", "''");
        return self();
    }

    /**
     * Masks an element so only its padding ring is visible — the standard
     * trick for gradient borders: paint a gradient background, set the
     * border thickness as padding, and call this.
     *
     * <p>Example (animated gradient border overlay):</p>
     * <pre>
     * style().position(absolute).inset(zero)
     *        .borderRadius(px(12)).padding(px(2))
     *        .background(myGradient)
     *        .borderMask()
     * </pre>
     *
     * <p>Emits the cross-browser mask-composite pair (webkit xor / standard
     * exclude).</p>
     */
    public T borderMask() {
        String mask = "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)";
        properties.put("-webkit-mask", mask);
        properties.put("mask", mask);
        properties.put("-webkit-mask-composite", "xor");
        properties.put("mask-composite", "exclude");
        return self();
    }

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
     * Sets a CSS property from a combined {@code "property:value"} string,
     * as returned by the property-string CSS modules (CSSAnchorPositioning,
     * CSSScrollSnap, CSSTextWrap, CSSSubgrid, CSSMasking,
     * CSSLogicalProperties).
     *
     * <p>Example:</p>
     * <pre>
     * style().prop(anchorName("--menu"))       // "anchor-name:--menu"
     *        .prop(scrollSnapTypeX("mandatory"))
     * </pre>
     *
     * @param propertyAndValue a {@code "property:value"} string
     * @return this builder for chaining
     * @throws IllegalArgumentException if the string has no colon
     */
    public T prop(String propertyAndValue) {
        int colon = propertyAndValue.indexOf(':');
        if (colon <= 0) {
            throw new IllegalArgumentException(
                "Expected \"property:value\" but got: " + propertyAndValue);
        }
        properties.put(
            propertyAndValue.substring(0, colon).trim(),
            propertyAndValue.substring(colon + 1).trim());
        return self();
    }

    /**
     * Sets any CSS property by name with a plain string value — the blessed
     * raw escape hatch for anything the typed methods don't cover.
     *
     * <p>There is nothing unsafe about it: a {@code CSSValue} is itself just a
     * {@code () -> String} lambda, so this is exactly as expressive (and no
     * more injectable) than the typed overload.</p>
     *
     * <p>Example:</p>
     * <pre>
     * style().prop("container-type", "inline-size")
     *        .prop("scroll-snap-type", "x mandatory")
     *        .prop("-moz-osx-font-smoothing", "grayscale")
     * </pre>
     *
     * @param name the CSS property name (e.g., "display", "margin-top")
     * @param value the CSS value as a string
     * @return this builder for chaining
     */
    public T prop(String name, String value) {
        properties.put(name, value);
        return self();
    }

    /**
     * Sets any CSS property by name with an unvalidated string value.
     *
     * @param name the CSS property name
     * @param value the CSS value as a string
     * @return this builder for chaining
     * @deprecated Use {@link #prop(String, String)} instead — identical behaviour,
     *             and "unsafe" was never accurate.
     */
    @Deprecated
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
        // Insertion-ordered on purpose: MediaQuery, ContainerQuery and Keyframes
        // iterate this to emit their declarations, and in CSS the order of
        // declarations decides which one wins. Map.copyOf leaves the order
        // unspecified, which made those blocks non-deterministic.
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(properties));
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
