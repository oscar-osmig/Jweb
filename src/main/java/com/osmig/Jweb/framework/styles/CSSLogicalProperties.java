package com.osmig.Jweb.framework.styles;

import jweb.CSSValue;
import jweb.Style;

/**
 * CSS Logical Properties DSL for writing-mode-aware layout properties.
 *
 * <p>Logical properties use flow-relative directions (block/inline) instead
 * of physical directions (top/right/bottom/left), enabling automatic RTL
 * and vertical writing mode support.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Margin using logical properties (works in LTR and RTL)
 * rule(".card")
 *     .apply(marginBlock("1rem"))
 *     .apply(marginInline("auto"))
 *
 * // Padding
 * rule(".section")
 *     .apply(paddingBlock("2rem"))
 *     .apply(paddingInline("1rem"))
 *
 * // Sizing
 * rule(".container")
 *     .apply(inlineSize("100%"))
 *     .apply(maxInlineSize("1200px"))
 *     .apply(blockSize("auto"))
 *
 * // Borders
 * rule(".item")
 *     .apply(borderInlineStart("2px solid blue"))
 *     .apply(borderBlockEnd("1px solid gray"))
 *
 * // Inset (positioning)
 * rule(".overlay")
 *     .position("absolute")
 *     .apply(insetBlock("0"))
 *     .apply(insetInline("0"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public class CSSLogicalProperties extends CSSSubgrid {

    protected CSSLogicalProperties() {}

    // ==================== Sizing ====================

    /**
     * Creates an inline-size declaration (width in horizontal writing modes).
     *
     * @param value the size value
     * @return a {@code Style} holding {@code inline-size: value}
     */
    public static Style<?> inlineSize(String value) {
        return CSS.style().prop("inline-size", value);
    }

    /** {@code CSSValue} overload of {@link #inlineSize(String)}. */
    public static Style<?> inlineSize(CSSValue value) {
        return inlineSize(value.css());
    }

    /**
     * Creates a block-size declaration (height in horizontal writing modes).
     *
     * @param value the size value
     * @return a {@code Style} holding {@code block-size: value}
     */
    public static Style<?> blockSize(String value) {
        return CSS.style().prop("block-size", value);
    }

    /** {@code CSSValue} overload of {@link #blockSize(String)}. */
    public static Style<?> blockSize(CSSValue value) {
        return blockSize(value.css());
    }

    /** Creates min-inline-size. */
    public static Style<?> minInlineSize(String value) {
        return CSS.style().prop("min-inline-size", value);
    }

    /** {@code CSSValue} overload of {@link #minInlineSize(String)}. */
    public static Style<?> minInlineSize(CSSValue value) {
        return minInlineSize(value.css());
    }

    /** Creates max-inline-size. */
    public static Style<?> maxInlineSize(String value) {
        return CSS.style().prop("max-inline-size", value);
    }

    /** {@code CSSValue} overload of {@link #maxInlineSize(String)}. */
    public static Style<?> maxInlineSize(CSSValue value) {
        return maxInlineSize(value.css());
    }

    /** Creates min-block-size. */
    public static Style<?> minBlockSize(String value) {
        return CSS.style().prop("min-block-size", value);
    }

    /** {@code CSSValue} overload of {@link #minBlockSize(String)}. */
    public static Style<?> minBlockSize(CSSValue value) {
        return minBlockSize(value.css());
    }

    /** Creates max-block-size. */
    public static Style<?> maxBlockSize(String value) {
        return CSS.style().prop("max-block-size", value);
    }

    /** {@code CSSValue} overload of {@link #maxBlockSize(String)}. */
    public static Style<?> maxBlockSize(CSSValue value) {
        return maxBlockSize(value.css());
    }

    // ==================== Margin ====================

    /**
     * Creates margin-inline (start and end).
     *
     * @param value the margin value(s)
     * @return a {@code Style} holding {@code margin-inline: value}
     */
    public static Style<?> marginInline(String value) {
        return CSS.style().prop("margin-inline", value);
    }

    /** {@code CSSValue} overload of {@link #marginInline(String)}. */
    public static Style<?> marginInline(CSSValue value) {
        return marginInline(value.css());
    }

    /**
     * Creates margin-inline with separate start and end values.
     *
     * @param start the start margin
     * @param end the end margin
     * @return a {@code Style} holding {@code margin-inline: start end}
     */
    public static Style<?> marginInline(String start, String end) {
        return CSS.style().prop("margin-inline", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #marginInline(String, String)}. */
    public static Style<?> marginInline(CSSValue start, CSSValue end) {
        return marginInline(start.css(), end.css());
    }

    /**
     * Creates margin-block (start and end).
     *
     * @param value the margin value(s)
     * @return a {@code Style} holding {@code margin-block: value}
     */
    public static Style<?> marginBlock(String value) {
        return CSS.style().prop("margin-block", value);
    }

    /** {@code CSSValue} overload of {@link #marginBlock(String)}. */
    public static Style<?> marginBlock(CSSValue value) {
        return marginBlock(value.css());
    }

    /**
     * Creates margin-block with separate start and end values.
     *
     * @param start the start margin
     * @param end the end margin
     * @return a {@code Style} holding {@code margin-block: start end}
     */
    public static Style<?> marginBlock(String start, String end) {
        return CSS.style().prop("margin-block", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #marginBlock(String, String)}. */
    public static Style<?> marginBlock(CSSValue start, CSSValue end) {
        return marginBlock(start.css(), end.css());
    }

    /** Creates margin-inline-start. */
    public static Style<?> marginInlineStart(String value) {
        return CSS.style().prop("margin-inline-start", value);
    }

    /** {@code CSSValue} overload of {@link #marginInlineStart(String)}. */
    public static Style<?> marginInlineStart(CSSValue value) {
        return marginInlineStart(value.css());
    }

    /** Creates margin-inline-end. */
    public static Style<?> marginInlineEnd(String value) {
        return CSS.style().prop("margin-inline-end", value);
    }

    /** {@code CSSValue} overload of {@link #marginInlineEnd(String)}. */
    public static Style<?> marginInlineEnd(CSSValue value) {
        return marginInlineEnd(value.css());
    }

    /** Creates margin-block-start. */
    public static Style<?> marginBlockStart(String value) {
        return CSS.style().prop("margin-block-start", value);
    }

    /** {@code CSSValue} overload of {@link #marginBlockStart(String)}. */
    public static Style<?> marginBlockStart(CSSValue value) {
        return marginBlockStart(value.css());
    }

    /** Creates margin-block-end. */
    public static Style<?> marginBlockEnd(String value) {
        return CSS.style().prop("margin-block-end", value);
    }

    /** {@code CSSValue} overload of {@link #marginBlockEnd(String)}. */
    public static Style<?> marginBlockEnd(CSSValue value) {
        return marginBlockEnd(value.css());
    }

    // ==================== Padding ====================

    /**
     * Creates padding-inline (start and end).
     *
     * @param value the padding value(s)
     * @return a {@code Style} holding {@code padding-inline: value}
     */
    public static Style<?> paddingInline(String value) {
        return CSS.style().prop("padding-inline", value);
    }

    /** {@code CSSValue} overload of {@link #paddingInline(String)}. */
    public static Style<?> paddingInline(CSSValue value) {
        return paddingInline(value.css());
    }

    /**
     * Creates padding-inline with separate start and end values.
     *
     * @param start the start padding
     * @param end the end padding
     * @return a {@code Style} holding {@code padding-inline: start end}
     */
    public static Style<?> paddingInline(String start, String end) {
        return CSS.style().prop("padding-inline", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #paddingInline(String, String)}. */
    public static Style<?> paddingInline(CSSValue start, CSSValue end) {
        return paddingInline(start.css(), end.css());
    }

    /**
     * Creates padding-block (start and end).
     *
     * @param value the padding value(s)
     * @return a {@code Style} holding {@code padding-block: value}
     */
    public static Style<?> paddingBlock(String value) {
        return CSS.style().prop("padding-block", value);
    }

    /** {@code CSSValue} overload of {@link #paddingBlock(String)}. */
    public static Style<?> paddingBlock(CSSValue value) {
        return paddingBlock(value.css());
    }

    /**
     * Creates padding-block with separate start and end values.
     *
     * @param start the start padding
     * @param end the end padding
     * @return a {@code Style} holding {@code padding-block: start end}
     */
    public static Style<?> paddingBlock(String start, String end) {
        return CSS.style().prop("padding-block", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #paddingBlock(String, String)}. */
    public static Style<?> paddingBlock(CSSValue start, CSSValue end) {
        return paddingBlock(start.css(), end.css());
    }

    /** Creates padding-inline-start. */
    public static Style<?> paddingInlineStart(String value) {
        return CSS.style().prop("padding-inline-start", value);
    }

    /** {@code CSSValue} overload of {@link #paddingInlineStart(String)}. */
    public static Style<?> paddingInlineStart(CSSValue value) {
        return paddingInlineStart(value.css());
    }

    /** Creates padding-inline-end. */
    public static Style<?> paddingInlineEnd(String value) {
        return CSS.style().prop("padding-inline-end", value);
    }

    /** {@code CSSValue} overload of {@link #paddingInlineEnd(String)}. */
    public static Style<?> paddingInlineEnd(CSSValue value) {
        return paddingInlineEnd(value.css());
    }

    /** Creates padding-block-start. */
    public static Style<?> paddingBlockStart(String value) {
        return CSS.style().prop("padding-block-start", value);
    }

    /** {@code CSSValue} overload of {@link #paddingBlockStart(String)}. */
    public static Style<?> paddingBlockStart(CSSValue value) {
        return paddingBlockStart(value.css());
    }

    /** Creates padding-block-end. */
    public static Style<?> paddingBlockEnd(String value) {
        return CSS.style().prop("padding-block-end", value);
    }

    /** {@code CSSValue} overload of {@link #paddingBlockEnd(String)}. */
    public static Style<?> paddingBlockEnd(CSSValue value) {
        return paddingBlockEnd(value.css());
    }

    // ==================== Inset (Positioning) ====================

    /**
     * Creates inset-inline (start and end).
     *
     * @param value the inset value(s)
     * @return a {@code Style} holding {@code inset-inline: value}
     */
    public static Style<?> insetInline(String value) {
        return CSS.style().prop("inset-inline", value);
    }

    /** {@code CSSValue} overload of {@link #insetInline(String)}. */
    public static Style<?> insetInline(CSSValue value) {
        return insetInline(value.css());
    }

    /**
     * Creates inset-inline with separate start and end values.
     *
     * @param start the start inset
     * @param end the end inset
     * @return a {@code Style} holding {@code inset-inline: start end}
     */
    public static Style<?> insetInline(String start, String end) {
        return CSS.style().prop("inset-inline", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #insetInline(String, String)}. */
    public static Style<?> insetInline(CSSValue start, CSSValue end) {
        return insetInline(start.css(), end.css());
    }

    /**
     * Creates inset-block (start and end).
     *
     * @param value the inset value(s)
     * @return a {@code Style} holding {@code inset-block: value}
     */
    public static Style<?> insetBlock(String value) {
        return CSS.style().prop("inset-block", value);
    }

    /** {@code CSSValue} overload of {@link #insetBlock(String)}. */
    public static Style<?> insetBlock(CSSValue value) {
        return insetBlock(value.css());
    }

    /**
     * Creates inset-block with separate start and end values.
     *
     * @param start the start inset
     * @param end the end inset
     * @return a {@code Style} holding {@code inset-block: start end}
     */
    public static Style<?> insetBlock(String start, String end) {
        return CSS.style().prop("inset-block", start + " " + end);
    }

    /** {@code CSSValue} overload of {@link #insetBlock(String, String)}. */
    public static Style<?> insetBlock(CSSValue start, CSSValue end) {
        return insetBlock(start.css(), end.css());
    }

    /** Creates inset-inline-start. */
    public static Style<?> insetInlineStart(String value) {
        return CSS.style().prop("inset-inline-start", value);
    }

    /** {@code CSSValue} overload of {@link #insetInlineStart(String)}. */
    public static Style<?> insetInlineStart(CSSValue value) {
        return insetInlineStart(value.css());
    }

    /** Creates inset-inline-end. */
    public static Style<?> insetInlineEnd(String value) {
        return CSS.style().prop("inset-inline-end", value);
    }

    /** {@code CSSValue} overload of {@link #insetInlineEnd(String)}. */
    public static Style<?> insetInlineEnd(CSSValue value) {
        return insetInlineEnd(value.css());
    }

    /** Creates inset-block-start. */
    public static Style<?> insetBlockStart(String value) {
        return CSS.style().prop("inset-block-start", value);
    }

    /** {@code CSSValue} overload of {@link #insetBlockStart(String)}. */
    public static Style<?> insetBlockStart(CSSValue value) {
        return insetBlockStart(value.css());
    }

    /** Creates inset-block-end. */
    public static Style<?> insetBlockEnd(String value) {
        return CSS.style().prop("inset-block-end", value);
    }

    /** {@code CSSValue} overload of {@link #insetBlockEnd(String)}. */
    public static Style<?> insetBlockEnd(CSSValue value) {
        return insetBlockEnd(value.css());
    }

    // ==================== Border ====================

    /** Creates border-inline. */
    public static Style<?> borderInline(String value) {
        return CSS.style().prop("border-inline", value);
    }

    /** {@code CSSValue} overload of {@link #borderInline(String)}. */
    public static Style<?> borderInline(CSSValue value) {
        return borderInline(value.css());
    }

    /** Creates border-block. */
    public static Style<?> borderBlock(String value) {
        return CSS.style().prop("border-block", value);
    }

    /** {@code CSSValue} overload of {@link #borderBlock(String)}. */
    public static Style<?> borderBlock(CSSValue value) {
        return borderBlock(value.css());
    }

    /** Creates border-inline-start. */
    public static Style<?> borderInlineStart(String value) {
        return CSS.style().prop("border-inline-start", value);
    }

    /** {@code CSSValue} overload of {@link #borderInlineStart(String)}. */
    public static Style<?> borderInlineStart(CSSValue value) {
        return borderInlineStart(value.css());
    }

    /** Creates border-inline-end. */
    public static Style<?> borderInlineEnd(String value) {
        return CSS.style().prop("border-inline-end", value);
    }

    /** {@code CSSValue} overload of {@link #borderInlineEnd(String)}. */
    public static Style<?> borderInlineEnd(CSSValue value) {
        return borderInlineEnd(value.css());
    }

    /** Creates border-block-start. */
    public static Style<?> borderBlockStart(String value) {
        return CSS.style().prop("border-block-start", value);
    }

    /** {@code CSSValue} overload of {@link #borderBlockStart(String)}. */
    public static Style<?> borderBlockStart(CSSValue value) {
        return borderBlockStart(value.css());
    }

    /** Creates border-block-end. */
    public static Style<?> borderBlockEnd(String value) {
        return CSS.style().prop("border-block-end", value);
    }

    /** {@code CSSValue} overload of {@link #borderBlockEnd(String)}. */
    public static Style<?> borderBlockEnd(CSSValue value) {
        return borderBlockEnd(value.css());
    }

    // ==================== Border Radius ====================

    /** Creates border-start-start-radius. */
    public static Style<?> borderStartStartRadius(String value) {
        return CSS.style().prop("border-start-start-radius", value);
    }

    /** {@code CSSValue} overload of {@link #borderStartStartRadius(String)}. */
    public static Style<?> borderStartStartRadius(CSSValue value) {
        return borderStartStartRadius(value.css());
    }

    /** Creates border-start-end-radius. */
    public static Style<?> borderStartEndRadius(String value) {
        return CSS.style().prop("border-start-end-radius", value);
    }

    /** {@code CSSValue} overload of {@link #borderStartEndRadius(String)}. */
    public static Style<?> borderStartEndRadius(CSSValue value) {
        return borderStartEndRadius(value.css());
    }

    /** Creates border-end-start-radius. */
    public static Style<?> borderEndStartRadius(String value) {
        return CSS.style().prop("border-end-start-radius", value);
    }

    /** {@code CSSValue} overload of {@link #borderEndStartRadius(String)}. */
    public static Style<?> borderEndStartRadius(CSSValue value) {
        return borderEndStartRadius(value.css());
    }

    /** Creates border-end-end-radius. */
    public static Style<?> borderEndEndRadius(String value) {
        return CSS.style().prop("border-end-end-radius", value);
    }

    /** {@code CSSValue} overload of {@link #borderEndEndRadius(String)}. */
    public static Style<?> borderEndEndRadius(CSSValue value) {
        return borderEndEndRadius(value.css());
    }

    // ==================== Text Alignment ====================

    /**
     * Creates text-align with logical value.
     *
     * @param value "start" or "end"
     * @return a {@code Style} holding {@code text-align: value}
     */
    public static Style<?> textAlignLogical(String value) {
        return CSS.style().prop("text-align", value);
    }

    /** {@code CSSValue} overload of {@link #textAlignLogical(String)}. */
    public static Style<?> textAlignLogical(CSSValue value) {
        return textAlignLogical(value.css());
    }

    /** Text aligned to start of inline direction. */
    public static Style<?> textAlignStart() {
        return textAlignLogical("start");
    }

    /** Text aligned to end of inline direction. */
    public static Style<?> textAlignEnd() {
        return textAlignLogical("end");
    }

    // ==================== Float & Clear ====================

    /** Float to inline start. */
    public static Style<?> floatInlineStart() {
        return CSS.style().prop("float", "inline-start");
    }

    /** Float to inline end. */
    public static Style<?> floatInlineEnd() {
        return CSS.style().prop("float", "inline-end");
    }

    /** Clear inline start. */
    public static Style<?> clearInlineStart() {
        return CSS.style().prop("clear", "inline-start");
    }

    /** Clear inline end. */
    public static Style<?> clearInlineEnd() {
        return CSS.style().prop("clear", "inline-end");
    }

    // ==================== Overflow ====================

    /** Creates overflow-inline. */
    public static Style<?> overflowInline(String value) {
        return CSS.style().prop("overflow-inline", value);
    }

    /** {@code CSSValue} overload of {@link #overflowInline(String)}. */
    public static Style<?> overflowInline(CSSValue value) {
        return overflowInline(value.css());
    }

    /** Creates overflow-block. */
    public static Style<?> overflowBlock(String value) {
        return CSS.style().prop("overflow-block", value);
    }

    /** {@code CSSValue} overload of {@link #overflowBlock(String)}. */
    public static Style<?> overflowBlock(CSSValue value) {
        return overflowBlock(value.css());
    }

    // ==================== Resize ====================

    /** Resize in block direction only. */
    public static Style<?> resizeBlock() {
        return CSS.style().prop("resize", "block");
    }

    /** Resize in inline direction only. */
    public static Style<?> resizeInline() {
        return CSS.style().prop("resize", "inline");
    }
}
