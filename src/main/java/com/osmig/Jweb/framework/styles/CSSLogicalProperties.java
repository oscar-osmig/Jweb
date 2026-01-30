package com.osmig.Jweb.framework.styles;

/**
 * CSS Logical Properties DSL for writing-mode-aware layout properties.
 *
 * <p>Logical properties use flow-relative directions (block/inline) instead
 * of physical directions (top/right/bottom/left), enabling automatic RTL
 * and vertical writing mode support.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSLogicalProperties.*;
 *
 * // Margin using logical properties (works in LTR and RTL)
 * rule(".card")
 *     .prop(marginBlock("1rem"))
 *     .prop(marginInline("auto"))
 *
 * // Padding
 * rule(".section")
 *     .prop(paddingBlock("2rem"))
 *     .prop(paddingInline("1rem"))
 *
 * // Sizing
 * rule(".container")
 *     .prop(inlineSize("100%"))
 *     .prop(maxInlineSize("1200px"))
 *     .prop(blockSize("auto"))
 *
 * // Borders
 * rule(".item")
 *     .prop(borderInlineStart("2px solid blue"))
 *     .prop(borderBlockEnd("1px solid gray"))
 *
 * // Inset (positioning)
 * rule(".overlay")
 *     .position("absolute")
 *     .prop(insetBlock("0"))
 *     .prop(insetInline("0"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public final class CSSLogicalProperties {

    private CSSLogicalProperties() {}

    // ==================== Sizing ====================

    /**
     * Creates an inline-size property (width in horizontal writing modes).
     *
     * @param value the size value
     * @return the property string
     */
    public static String inlineSize(String value) {
        return "inline-size:" + value;
    }

    /**
     * Creates a block-size property (height in horizontal writing modes).
     *
     * @param value the size value
     * @return the property string
     */
    public static String blockSize(String value) {
        return "block-size:" + value;
    }

    /** Creates min-inline-size. */
    public static String minInlineSize(String value) {
        return "min-inline-size:" + value;
    }

    /** Creates max-inline-size. */
    public static String maxInlineSize(String value) {
        return "max-inline-size:" + value;
    }

    /** Creates min-block-size. */
    public static String minBlockSize(String value) {
        return "min-block-size:" + value;
    }

    /** Creates max-block-size. */
    public static String maxBlockSize(String value) {
        return "max-block-size:" + value;
    }

    // ==================== Margin ====================

    /**
     * Creates margin-inline (start and end).
     *
     * @param value the margin value(s)
     * @return the property string
     */
    public static String marginInline(String value) {
        return "margin-inline:" + value;
    }

    /**
     * Creates margin-inline with separate start and end values.
     *
     * @param start the start margin
     * @param end the end margin
     * @return the property string
     */
    public static String marginInline(String start, String end) {
        return "margin-inline:" + start + " " + end;
    }

    /**
     * Creates margin-block (start and end).
     *
     * @param value the margin value(s)
     * @return the property string
     */
    public static String marginBlock(String value) {
        return "margin-block:" + value;
    }

    /**
     * Creates margin-block with separate start and end values.
     *
     * @param start the start margin
     * @param end the end margin
     * @return the property string
     */
    public static String marginBlock(String start, String end) {
        return "margin-block:" + start + " " + end;
    }

    /** Creates margin-inline-start. */
    public static String marginInlineStart(String value) {
        return "margin-inline-start:" + value;
    }

    /** Creates margin-inline-end. */
    public static String marginInlineEnd(String value) {
        return "margin-inline-end:" + value;
    }

    /** Creates margin-block-start. */
    public static String marginBlockStart(String value) {
        return "margin-block-start:" + value;
    }

    /** Creates margin-block-end. */
    public static String marginBlockEnd(String value) {
        return "margin-block-end:" + value;
    }

    // ==================== Padding ====================

    /**
     * Creates padding-inline (start and end).
     *
     * @param value the padding value(s)
     * @return the property string
     */
    public static String paddingInline(String value) {
        return "padding-inline:" + value;
    }

    /**
     * Creates padding-inline with separate start and end values.
     *
     * @param start the start padding
     * @param end the end padding
     * @return the property string
     */
    public static String paddingInline(String start, String end) {
        return "padding-inline:" + start + " " + end;
    }

    /**
     * Creates padding-block (start and end).
     *
     * @param value the padding value(s)
     * @return the property string
     */
    public static String paddingBlock(String value) {
        return "padding-block:" + value;
    }

    /**
     * Creates padding-block with separate start and end values.
     *
     * @param start the start padding
     * @param end the end padding
     * @return the property string
     */
    public static String paddingBlock(String start, String end) {
        return "padding-block:" + start + " " + end;
    }

    /** Creates padding-inline-start. */
    public static String paddingInlineStart(String value) {
        return "padding-inline-start:" + value;
    }

    /** Creates padding-inline-end. */
    public static String paddingInlineEnd(String value) {
        return "padding-inline-end:" + value;
    }

    /** Creates padding-block-start. */
    public static String paddingBlockStart(String value) {
        return "padding-block-start:" + value;
    }

    /** Creates padding-block-end. */
    public static String paddingBlockEnd(String value) {
        return "padding-block-end:" + value;
    }

    // ==================== Inset (Positioning) ====================

    /**
     * Creates inset-inline (start and end).
     *
     * @param value the inset value(s)
     * @return the property string
     */
    public static String insetInline(String value) {
        return "inset-inline:" + value;
    }

    /**
     * Creates inset-inline with separate start and end values.
     *
     * @param start the start inset
     * @param end the end inset
     * @return the property string
     */
    public static String insetInline(String start, String end) {
        return "inset-inline:" + start + " " + end;
    }

    /**
     * Creates inset-block (start and end).
     *
     * @param value the inset value(s)
     * @return the property string
     */
    public static String insetBlock(String value) {
        return "inset-block:" + value;
    }

    /**
     * Creates inset-block with separate start and end values.
     *
     * @param start the start inset
     * @param end the end inset
     * @return the property string
     */
    public static String insetBlock(String start, String end) {
        return "inset-block:" + start + " " + end;
    }

    /** Creates inset-inline-start. */
    public static String insetInlineStart(String value) {
        return "inset-inline-start:" + value;
    }

    /** Creates inset-inline-end. */
    public static String insetInlineEnd(String value) {
        return "inset-inline-end:" + value;
    }

    /** Creates inset-block-start. */
    public static String insetBlockStart(String value) {
        return "inset-block-start:" + value;
    }

    /** Creates inset-block-end. */
    public static String insetBlockEnd(String value) {
        return "inset-block-end:" + value;
    }

    // ==================== Border ====================

    /** Creates border-inline. */
    public static String borderInline(String value) {
        return "border-inline:" + value;
    }

    /** Creates border-block. */
    public static String borderBlock(String value) {
        return "border-block:" + value;
    }

    /** Creates border-inline-start. */
    public static String borderInlineStart(String value) {
        return "border-inline-start:" + value;
    }

    /** Creates border-inline-end. */
    public static String borderInlineEnd(String value) {
        return "border-inline-end:" + value;
    }

    /** Creates border-block-start. */
    public static String borderBlockStart(String value) {
        return "border-block-start:" + value;
    }

    /** Creates border-block-end. */
    public static String borderBlockEnd(String value) {
        return "border-block-end:" + value;
    }

    // ==================== Border Radius ====================

    /** Creates border-start-start-radius. */
    public static String borderStartStartRadius(String value) {
        return "border-start-start-radius:" + value;
    }

    /** Creates border-start-end-radius. */
    public static String borderStartEndRadius(String value) {
        return "border-start-end-radius:" + value;
    }

    /** Creates border-end-start-radius. */
    public static String borderEndStartRadius(String value) {
        return "border-end-start-radius:" + value;
    }

    /** Creates border-end-end-radius. */
    public static String borderEndEndRadius(String value) {
        return "border-end-end-radius:" + value;
    }

    // ==================== Text Alignment ====================

    /**
     * Creates text-align with logical value.
     *
     * @param value "start" or "end"
     * @return the property string
     */
    public static String textAlignLogical(String value) {
        return "text-align:" + value;
    }

    /** Text aligned to start of inline direction. */
    public static String textAlignStart() {
        return "text-align:start";
    }

    /** Text aligned to end of inline direction. */
    public static String textAlignEnd() {
        return "text-align:end";
    }

    // ==================== Float & Clear ====================

    /** Float to inline start. */
    public static String floatInlineStart() {
        return "float:inline-start";
    }

    /** Float to inline end. */
    public static String floatInlineEnd() {
        return "float:inline-end";
    }

    /** Clear inline start. */
    public static String clearInlineStart() {
        return "clear:inline-start";
    }

    /** Clear inline end. */
    public static String clearInlineEnd() {
        return "clear:inline-end";
    }

    // ==================== Overflow ====================

    /** Creates overflow-inline. */
    public static String overflowInline(String value) {
        return "overflow-inline:" + value;
    }

    /** Creates overflow-block. */
    public static String overflowBlock(String value) {
        return "overflow-block:" + value;
    }

    // ==================== Resize ====================

    /** Resize in block direction only. */
    public static String resizeBlock() {
        return "resize:block";
    }

    /** Resize in inline direction only. */
    public static String resizeInline() {
        return "resize:inline";
    }
}
