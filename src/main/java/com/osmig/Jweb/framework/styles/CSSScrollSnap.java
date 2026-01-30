package com.osmig.Jweb.framework.styles;

/**
 * CSS Scroll Snap DSL for creating scroll-snapping containers and children.
 *
 * <p>Scroll Snap provides control over scroll positions, enabling elements to
 * snap into place after scrolling for carousel-like and paginated layouts.</p>
 *
 * <h2>Horizontal Snap Container</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSScrollSnap.*;
 *
 * // Container: snap on X axis, mandatory
 * rule(".carousel")
 *     .prop(snapTypeX("mandatory"))
 *     .prop(snapPadding("0 20px"))
 *     .display("flex")
 *     .overflowX("auto")
 *
 * // Children: snap to start
 * rule(".carousel > .slide")
 *     .prop(snapAlign("start"))
 *     .prop(snapStop("always"))
 * }</pre>
 *
 * <h2>Vertical Page Snap</h2>
 * <pre>{@code
 * rule(".pages")
 *     .prop(snapTypeY("mandatory"))
 *     .height("100vh")
 *     .overflowY("auto")
 *
 * rule(".pages > section")
 *     .prop(snapAlign("start"))
 *     .height("100vh")
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public final class CSSScrollSnap {

    private CSSScrollSnap() {}

    // ==================== Scroll Snap Type ====================

    /**
     * Creates a scroll-snap-type property for both axes.
     *
     * @param value "none", "x mandatory", "y proximity", "both mandatory", etc.
     * @return the property string
     */
    public static String snapType(String value) {
        return "scroll-snap-type:" + value;
    }

    /**
     * Creates scroll-snap-type for horizontal axis.
     *
     * @param strictness "mandatory" or "proximity"
     * @return the property string
     */
    public static String snapTypeX(String strictness) {
        return "scroll-snap-type:x " + strictness;
    }

    /**
     * Creates scroll-snap-type for vertical axis.
     *
     * @param strictness "mandatory" or "proximity"
     * @return the property string
     */
    public static String snapTypeY(String strictness) {
        return "scroll-snap-type:y " + strictness;
    }

    /**
     * Creates scroll-snap-type for both axes.
     *
     * @param strictness "mandatory" or "proximity"
     * @return the property string
     */
    public static String snapTypeBoth(String strictness) {
        return "scroll-snap-type:both " + strictness;
    }

    /**
     * Creates scroll-snap-type for block axis.
     *
     * @param strictness "mandatory" or "proximity"
     * @return the property string
     */
    public static String snapTypeBlock(String strictness) {
        return "scroll-snap-type:block " + strictness;
    }

    /**
     * Creates scroll-snap-type for inline axis.
     *
     * @param strictness "mandatory" or "proximity"
     * @return the property string
     */
    public static String snapTypeInline(String strictness) {
        return "scroll-snap-type:inline " + strictness;
    }

    /** Disables scroll snapping. */
    public static String snapTypeNone() {
        return "scroll-snap-type:none";
    }

    // ==================== Scroll Snap Align ====================

    /**
     * Creates a scroll-snap-align property.
     *
     * @param value "none", "start", "end", "center"
     * @return the property string
     */
    public static String snapAlign(String value) {
        return "scroll-snap-align:" + value;
    }

    /**
     * Creates scroll-snap-align with separate block and inline values.
     *
     * @param block block alignment
     * @param inline inline alignment
     * @return the property string
     */
    public static String snapAlign(String block, String inline) {
        return "scroll-snap-align:" + block + " " + inline;
    }

    /** Snaps to start edge. */
    public static String snapAlignStart() {
        return "scroll-snap-align:start";
    }

    /** Snaps to center. */
    public static String snapAlignCenter() {
        return "scroll-snap-align:center";
    }

    /** Snaps to end edge. */
    public static String snapAlignEnd() {
        return "scroll-snap-align:end";
    }

    /** No snap alignment. */
    public static String snapAlignNone() {
        return "scroll-snap-align:none";
    }

    // ==================== Scroll Snap Stop ====================

    /**
     * Creates a scroll-snap-stop property.
     *
     * @param value "normal" or "always"
     * @return the property string
     */
    public static String snapStop(String value) {
        return "scroll-snap-stop:" + value;
    }

    /** Normal snap stop (can skip snap points during fast scroll). */
    public static String snapStopNormal() {
        return "scroll-snap-stop:normal";
    }

    /** Always stops at this snap point (cannot be skipped). */
    public static String snapStopAlways() {
        return "scroll-snap-stop:always";
    }

    // ==================== Scroll Padding ====================

    /**
     * Creates a scroll-padding property (applied to scroll container).
     *
     * @param value the padding value(s)
     * @return the property string
     */
    public static String snapPadding(String value) {
        return "scroll-padding:" + value;
    }

    /** Creates scroll-padding-top. */
    public static String snapPaddingTop(String value) {
        return "scroll-padding-top:" + value;
    }

    /** Creates scroll-padding-right. */
    public static String snapPaddingRight(String value) {
        return "scroll-padding-right:" + value;
    }

    /** Creates scroll-padding-bottom. */
    public static String snapPaddingBottom(String value) {
        return "scroll-padding-bottom:" + value;
    }

    /** Creates scroll-padding-left. */
    public static String snapPaddingLeft(String value) {
        return "scroll-padding-left:" + value;
    }

    /** Creates scroll-padding-inline. */
    public static String snapPaddingInline(String value) {
        return "scroll-padding-inline:" + value;
    }

    /** Creates scroll-padding-block. */
    public static String snapPaddingBlock(String value) {
        return "scroll-padding-block:" + value;
    }

    // ==================== Scroll Margin ====================

    /**
     * Creates a scroll-margin property (applied to snap children).
     *
     * @param value the margin value(s)
     * @return the property string
     */
    public static String snapMargin(String value) {
        return "scroll-margin:" + value;
    }

    /** Creates scroll-margin-top. */
    public static String snapMarginTop(String value) {
        return "scroll-margin-top:" + value;
    }

    /** Creates scroll-margin-right. */
    public static String snapMarginRight(String value) {
        return "scroll-margin-right:" + value;
    }

    /** Creates scroll-margin-bottom. */
    public static String snapMarginBottom(String value) {
        return "scroll-margin-bottom:" + value;
    }

    /** Creates scroll-margin-left. */
    public static String snapMarginLeft(String value) {
        return "scroll-margin-left:" + value;
    }

    /** Creates scroll-margin-inline. */
    public static String snapMarginInline(String value) {
        return "scroll-margin-inline:" + value;
    }

    /** Creates scroll-margin-block. */
    public static String snapMarginBlock(String value) {
        return "scroll-margin-block:" + value;
    }

    // ==================== Overscroll Behavior ====================

    /**
     * Creates an overscroll-behavior property.
     *
     * @param value "auto", "contain", or "none"
     * @return the property string
     */
    public static String overscrollBehavior(String value) {
        return "overscroll-behavior:" + value;
    }

    /** Creates overscroll-behavior-x. */
    public static String overscrollBehaviorX(String value) {
        return "overscroll-behavior-x:" + value;
    }

    /** Creates overscroll-behavior-y. */
    public static String overscrollBehaviorY(String value) {
        return "overscroll-behavior-y:" + value;
    }
}
