package com.osmig.Jweb.framework.styles;

import jweb.CSSValue;
import jweb.Style;

/**
 * CSS Scroll Snap DSL for creating scroll-snapping containers and children.
 *
 * <p>Scroll Snap provides control over scroll positions, enabling elements to
 * snap into place after scrolling for carousel-like and paginated layouts.</p>
 *
 * <h2>Horizontal Snap Container</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Container: snap on X axis, mandatory
 * rule(".carousel")
 *     .apply(snapTypeX("mandatory"))
 *     .apply(snapPadding("0 20px"))
 *     .display("flex")
 *     .overflowX("auto")
 *
 * // Children: snap to start
 * rule(".carousel > .slide")
 *     .apply(snapAlign("start"))
 *     .apply(snapStop("always"))
 * }</pre>
 *
 * <h2>Vertical Page Snap</h2>
 * <pre>{@code
 * rule(".pages")
 *     .apply(snapTypeY("mandatory"))
 *     .height("100vh")
 *     .overflowY("auto")
 *
 * rule(".pages > section")
 *     .apply(snapAlign("start"))
 *     .height("100vh")
 * }</pre>
 *
 * <p>Note the naming: {@code snapPadding}/{@code snapMargin} emit the real
 * {@code scroll-padding}/{@code scroll-margin} properties — there are no
 * {@code scroll-snap-padding} or {@code scroll-snap-margin} properties.</p>
 *
 * @see CSS for creating style rules
 */
public class CSSScrollSnap extends CSSMasking {

    protected CSSScrollSnap() {}

    // ==================== Scroll Snap Type ====================

    /**
     * Creates a scroll-snap-type declaration for both axes.
     * @param value "none", "x mandatory", "y proximity", "both mandatory", etc.
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapType(String value) {
        return CSS.style().prop("scroll-snap-type", value);
    }

    /** {@code CSSValue} overload of {@link #snapType(String)}. */
    public static Style<?> snapType(CSSValue value) {
        return snapType(value.css());
    }

    /**
     * Creates scroll-snap-type for horizontal axis.
     * @param strictness "mandatory" or "proximity"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapTypeX(String strictness) {
        return snapType("x " + strictness);
    }

    /**
     * Creates scroll-snap-type for vertical axis.
     * @param strictness "mandatory" or "proximity"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapTypeY(String strictness) {
        return snapType("y " + strictness);
    }

    /**
     * Creates scroll-snap-type for both axes.
     * @param strictness "mandatory" or "proximity"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapTypeBoth(String strictness) {
        return snapType("both " + strictness);
    }

    /**
     * Creates scroll-snap-type for block axis.
     * @param strictness "mandatory" or "proximity"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapTypeBlock(String strictness) {
        return snapType("block " + strictness);
    }

    /**
     * Creates scroll-snap-type for inline axis.
     * @param strictness "mandatory" or "proximity"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapTypeInline(String strictness) {
        return snapType("inline " + strictness);
    }

    /** Disables scroll snapping. */
    public static Style<?> snapTypeNone() {
        return snapType("none");
    }

    // ==================== Scroll Snap Align ====================

    /**
     * Creates a scroll-snap-align declaration.
     * @param value "none", "start", "end", "center"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapAlign(String value) {
        return CSS.style().prop("scroll-snap-align", value);
    }

    /** {@code CSSValue} overload of {@link #snapAlign(String)}. */
    public static Style<?> snapAlign(CSSValue value) {
        return snapAlign(value.css());
    }

    /**
     * Creates scroll-snap-align with separate block and inline values.
     * @param block block alignment
     * @param inline inline alignment
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapAlign(String block, String inline) {
        return snapAlign(block + " " + inline);
    }

    /** {@code CSSValue} overload of {@link #snapAlign(String, String)}. */
    public static Style<?> snapAlign(CSSValue block, CSSValue inline) {
        return snapAlign(block.css(), inline.css());
    }

    /** Snaps to start edge. */
    public static Style<?> snapAlignStart() {
        return snapAlign("start");
    }

    /** Snaps to center. */
    public static Style<?> snapAlignCenter() {
        return snapAlign("center");
    }

    /** Snaps to end edge. */
    public static Style<?> snapAlignEnd() {
        return snapAlign("end");
    }

    /** No snap alignment. */
    public static Style<?> snapAlignNone() {
        return snapAlign("none");
    }

    // ==================== Scroll Snap Stop ====================

    /**
     * Creates a scroll-snap-stop declaration.
     * @param value "normal" or "always"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapStop(String value) {
        return CSS.style().prop("scroll-snap-stop", value);
    }

    /** {@code CSSValue} overload of {@link #snapStop(String)}. */
    public static Style<?> snapStop(CSSValue value) {
        return snapStop(value.css());
    }

    /** Normal snap stop (can skip snap points during fast scroll). */
    public static Style<?> snapStopNormal() {
        return snapStop("normal");
    }

    /** Always stops at this snap point (cannot be skipped). */
    public static Style<?> snapStopAlways() {
        return snapStop("always");
    }

    // ==================== Scroll Padding ====================

    /**
     * Creates a scroll-padding declaration (applied to scroll container).
     * @param value the padding value(s)
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapPadding(String value) {
        return CSS.style().prop("scroll-padding", value);
    }

    /** {@code CSSValue} overload of {@link #snapPadding(String)}. */
    public static Style<?> snapPadding(CSSValue value) {
        return snapPadding(value.css());
    }

    /** Creates scroll-padding-top. */
    public static Style<?> snapPaddingTop(String value) {
        return CSS.style().prop("scroll-padding-top", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingTop(String)}. */
    public static Style<?> snapPaddingTop(CSSValue value) {
        return snapPaddingTop(value.css());
    }

    /** Creates scroll-padding-right. */
    public static Style<?> snapPaddingRight(String value) {
        return CSS.style().prop("scroll-padding-right", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingRight(String)}. */
    public static Style<?> snapPaddingRight(CSSValue value) {
        return snapPaddingRight(value.css());
    }

    /** Creates scroll-padding-bottom. */
    public static Style<?> snapPaddingBottom(String value) {
        return CSS.style().prop("scroll-padding-bottom", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingBottom(String)}. */
    public static Style<?> snapPaddingBottom(CSSValue value) {
        return snapPaddingBottom(value.css());
    }

    /** Creates scroll-padding-left. */
    public static Style<?> snapPaddingLeft(String value) {
        return CSS.style().prop("scroll-padding-left", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingLeft(String)}. */
    public static Style<?> snapPaddingLeft(CSSValue value) {
        return snapPaddingLeft(value.css());
    }

    /** Creates scroll-padding-inline. */
    public static Style<?> snapPaddingInline(String value) {
        return CSS.style().prop("scroll-padding-inline", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingInline(String)}. */
    public static Style<?> snapPaddingInline(CSSValue value) {
        return snapPaddingInline(value.css());
    }

    /** Creates scroll-padding-block. */
    public static Style<?> snapPaddingBlock(String value) {
        return CSS.style().prop("scroll-padding-block", value);
    }

    /** {@code CSSValue} overload of {@link #snapPaddingBlock(String)}. */
    public static Style<?> snapPaddingBlock(CSSValue value) {
        return snapPaddingBlock(value.css());
    }

    // ==================== Scroll Margin ====================

    /**
     * Creates a scroll-margin declaration (applied to snap children).
     * @param value the margin value(s)
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> snapMargin(String value) {
        return CSS.style().prop("scroll-margin", value);
    }

    /** {@code CSSValue} overload of {@link #snapMargin(String)}. */
    public static Style<?> snapMargin(CSSValue value) {
        return snapMargin(value.css());
    }

    /** Creates scroll-margin-top. */
    public static Style<?> snapMarginTop(String value) {
        return CSS.style().prop("scroll-margin-top", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginTop(String)}. */
    public static Style<?> snapMarginTop(CSSValue value) {
        return snapMarginTop(value.css());
    }

    /** Creates scroll-margin-right. */
    public static Style<?> snapMarginRight(String value) {
        return CSS.style().prop("scroll-margin-right", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginRight(String)}. */
    public static Style<?> snapMarginRight(CSSValue value) {
        return snapMarginRight(value.css());
    }

    /** Creates scroll-margin-bottom. */
    public static Style<?> snapMarginBottom(String value) {
        return CSS.style().prop("scroll-margin-bottom", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginBottom(String)}. */
    public static Style<?> snapMarginBottom(CSSValue value) {
        return snapMarginBottom(value.css());
    }

    /** Creates scroll-margin-left. */
    public static Style<?> snapMarginLeft(String value) {
        return CSS.style().prop("scroll-margin-left", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginLeft(String)}. */
    public static Style<?> snapMarginLeft(CSSValue value) {
        return snapMarginLeft(value.css());
    }

    /** Creates scroll-margin-inline. */
    public static Style<?> snapMarginInline(String value) {
        return CSS.style().prop("scroll-margin-inline", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginInline(String)}. */
    public static Style<?> snapMarginInline(CSSValue value) {
        return snapMarginInline(value.css());
    }

    /** Creates scroll-margin-block. */
    public static Style<?> snapMarginBlock(String value) {
        return CSS.style().prop("scroll-margin-block", value);
    }

    /** {@code CSSValue} overload of {@link #snapMarginBlock(String)}. */
    public static Style<?> snapMarginBlock(CSSValue value) {
        return snapMarginBlock(value.css());
    }

    // ==================== Overscroll Behavior ====================

    /**
     * Creates an overscroll-behavior declaration.
     * @param value "auto", "contain", or "none"
     * @return a {@code Style} holding the declaration
     */
    public static Style<?> overscrollBehavior(String value) {
        return CSS.style().prop("overscroll-behavior", value);
    }

    /** {@code CSSValue} overload of {@link #overscrollBehavior(String)}. */
    public static Style<?> overscrollBehavior(CSSValue value) {
        return overscrollBehavior(value.css());
    }

    /** Creates overscroll-behavior-x. */
    public static Style<?> overscrollBehaviorX(String value) {
        return CSS.style().prop("overscroll-behavior-x", value);
    }

    /** {@code CSSValue} overload of {@link #overscrollBehaviorX(String)}. */
    public static Style<?> overscrollBehaviorX(CSSValue value) {
        return overscrollBehaviorX(value.css());
    }

    /** Creates overscroll-behavior-y. */
    public static Style<?> overscrollBehaviorY(String value) {
        return CSS.style().prop("overscroll-behavior-y", value);
    }

    /** {@code CSSValue} overload of {@link #overscrollBehaviorY(String)}. */
    public static Style<?> overscrollBehaviorY(CSSValue value) {
        return overscrollBehaviorY(value.css());
    }
}
