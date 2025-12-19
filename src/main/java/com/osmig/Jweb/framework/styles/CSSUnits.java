package com.osmig.Jweb.framework.styles;

/**
 * CSS unit values and factory methods.
 *
 * Usage with static import:
 *   import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 *
 *   style().padding(px(10), rem(1))
 *          .margin(percent(50))
 *          .width(vw(100))
 */
public final class CSSUnits {

    private CSSUnits() {}

    // ==================== Common Constants ====================

    /** Zero value (0) - no unit needed. */
    public static final CSSValue zero = () -> "0";

    /** Auto value - browser calculates the value. */
    public static final CSSValue auto = () -> "auto";

    /** Inherit value - inherits from parent element. */
    public static final CSSValue inherit = () -> "inherit";

    /** Initial value - resets to CSS specification default. */
    public static final CSSValue initial = () -> "initial";

    /** Unset value - acts as inherit or initial depending on property. */
    public static final CSSValue unset = () -> "unset";

    /** None value - typically disables a feature. */
    public static final CSSValue none = () -> "none";

    // ==================== Pixel Units ====================

    /**
     * Creates a pixel (px) value.
     * Pixels are absolute units, 1px = 1/96th of 1 inch.
     *
     * <p>Example:</p>
     * <pre>
     * style().padding(px(10))       // padding: 10px
     * style().width(px(300))        // width: 300px
     * </pre>
     *
     * @param value the pixel amount
     * @return CSSValue representing the pixel value
     */
    public static CSSValue px(int value) {
        return () -> value + "px";
    }

    /**
     * Creates a pixel (px) value with decimal precision.
     *
     * @param value the pixel amount (can be fractional)
     * @return CSSValue representing the pixel value
     */
    public static CSSValue px(double value) {
        return () -> formatNumber(value) + "px";
    }

    // ==================== Relative Units ====================

    /**
     * Creates a rem value (relative to root font size).
     * 1rem = root element's font-size (typically 16px).
     *
     * <p>Example:</p>
     * <pre>
     * style().fontSize(rem(1.5))    // font-size: 1.5rem (24px if root is 16px)
     * style().margin(rem(2))        // margin: 2rem
     * </pre>
     *
     * @param value the rem amount
     * @return CSSValue representing the rem value
     */
    public static CSSValue rem(double value) {
        return () -> formatNumber(value) + "rem";
    }

    /**
     * Creates a rem value (relative to root font size).
     *
     * @param value the rem amount
     * @return CSSValue representing the rem value
     */
    public static CSSValue rem(int value) {
        return () -> value + "rem";
    }

    /**
     * Creates an em value (relative to parent font size).
     * 1em = parent element's font-size.
     *
     * <p>Example:</p>
     * <pre>
     * style().fontSize(em(1.2))     // 1.2x parent's font size
     * style().letterSpacing(em(0.05))
     * </pre>
     *
     * @param value the em amount
     * @return CSSValue representing the em value
     */
    public static CSSValue em(double value) {
        return () -> formatNumber(value) + "em";
    }

    /**
     * Creates an em value (relative to parent font size).
     *
     * @param value the em amount
     * @return CSSValue representing the em value
     */
    public static CSSValue em(int value) {
        return () -> value + "em";
    }

    // ==================== Percentage ====================

    /**
     * Creates a percentage (%) value.
     * Relative to parent element's corresponding property.
     *
     * <p>Example:</p>
     * <pre>
     * style().width(percent(50))    // width: 50%
     * style().height(percent(100))  // height: 100%
     * </pre>
     *
     * @param value the percentage amount
     * @return CSSValue representing the percentage
     */
    public static CSSValue percent(double value) {
        return () -> formatNumber(value) + "%";
    }

    /**
     * Creates a percentage (%) value.
     *
     * @param value the percentage amount
     * @return CSSValue representing the percentage
     */
    public static CSSValue percent(int value) {
        return () -> value + "%";
    }

    // ==================== Viewport Units ====================

    /**
     * Creates a viewport height (vh) value.
     * 1vh = 1% of viewport height.
     *
     * <p>Example:</p>
     * <pre>
     * style().minHeight(vh(100))    // min-height: 100vh (full viewport height)
     * </pre>
     *
     * @param value the vh amount (0-100 for full viewport)
     * @return CSSValue representing the vh value
     */
    public static CSSValue vh(double value) {
        return () -> formatNumber(value) + "vh";
    }

    /**
     * Creates a viewport height (vh) value.
     *
     * @param value the vh amount
     * @return CSSValue representing the vh value
     */
    public static CSSValue vh(int value) {
        return () -> value + "vh";
    }

    /**
     * Creates a viewport width (vw) value.
     * 1vw = 1% of viewport width.
     *
     * <p>Example:</p>
     * <pre>
     * style().width(vw(100))        // width: 100vw (full viewport width)
     * style().fontSize(vw(5))       // responsive font size
     * </pre>
     *
     * @param value the vw amount (0-100 for full viewport)
     * @return CSSValue representing the vw value
     */
    public static CSSValue vw(double value) {
        return () -> formatNumber(value) + "vw";
    }

    /**
     * Creates a viewport width (vw) value.
     *
     * @param value the vw amount
     * @return CSSValue representing the vw value
     */
    public static CSSValue vw(int value) {
        return () -> value + "vw";
    }

    /**
     * Creates a vmin value (smaller of vw or vh).
     * Useful for maintaining aspect ratios.
     *
     * @param value the vmin amount
     * @return CSSValue representing the vmin value
     */
    public static CSSValue vmin(double value) {
        return () -> formatNumber(value) + "vmin";
    }

    /**
     * Creates a vmax value (larger of vw or vh).
     *
     * @param value the vmax amount
     * @return CSSValue representing the vmax value
     */
    public static CSSValue vmax(double value) {
        return () -> formatNumber(value) + "vmax";
    }

    // ==================== Character Units ====================

    /**
     * Creates a ch value (width of "0" character).
     * Useful for sizing text containers.
     *
     * <p>Example:</p>
     * <pre>
     * style().maxWidth(ch(60))      // ~60 characters wide
     * </pre>
     *
     * @param value the ch amount
     * @return CSSValue representing the ch value
     */
    public static CSSValue ch(double value) {
        return () -> formatNumber(value) + "ch";
    }

    // ==================== Time Units ====================

    /**
     * Creates a milliseconds (ms) value.
     * Used for transitions and animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().transitionDuration(ms(300))  // 300 milliseconds
     * style().animationDelay(ms(100))
     * </pre>
     *
     * @param value the milliseconds amount
     * @return CSSValue representing the ms value
     */
    public static CSSValue ms(int value) {
        return () -> value + "ms";
    }

    /**
     * Creates a seconds (s) value.
     * Used for transitions and animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().transition(propAll, s(0.3), ease)  // 0.3 seconds
     * style().animationDuration(s(2))
     * </pre>
     *
     * @param value the seconds amount
     * @return CSSValue representing the s value
     */
    public static CSSValue s(double value) {
        return () -> formatNumber(value) + "s";
    }

    // ==================== Angle Units ====================

    /**
     * Creates a degrees (deg) value.
     * Used for rotations and gradients.
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(rotate(deg(45)))         // rotate 45 degrees
     * linearGradient(deg(90), red, blue)         // gradient direction
     * </pre>
     *
     * @param value the degrees (0-360)
     * @return CSSValue representing the deg value
     */
    public static CSSValue deg(double value) {
        return () -> formatNumber(value) + "deg";
    }

    /**
     * Creates a radians (rad) value.
     * Used for rotations (2π rad = 360°).
     *
     * @param value the radians amount
     * @return CSSValue representing the rad value
     */
    public static CSSValue rad(double value) {
        return () -> formatNumber(value) + "rad";
    }

    /**
     * Creates a turn value.
     * 1turn = 360 degrees.
     *
     * <p>Example:</p>
     * <pre>
     * style().transform(rotate(turn(0.5)))  // rotate 180 degrees
     * </pre>
     *
     * @param value the turns (1 = full rotation)
     * @return CSSValue representing the turn value
     */
    public static CSSValue turn(double value) {
        return () -> formatNumber(value) + "turn";
    }

    // ==================== Grid Units ====================

    /**
     * Creates a fraction (fr) value for CSS Grid.
     * Represents a fraction of available space.
     *
     * <p>Example:</p>
     * <pre>
     * style().gridTemplateColumns("1fr 2fr 1fr")  // 1:2:1 ratio
     * </pre>
     *
     * @param value the fraction amount
     * @return CSSValue representing the fr value
     */
    public static CSSValue fr(double value) {
        return () -> formatNumber(value) + "fr";
    }

    /**
     * Creates a fraction (fr) value for CSS Grid.
     *
     * @param value the fraction amount
     * @return CSSValue representing the fr value
     */
    public static CSSValue fr(int value) {
        return () -> value + "fr";
    }

    // ==================== Raw Numbers ====================

    /**
     * Creates a unitless number value.
     * Used for line-height, flex-grow, opacity, etc.
     *
     * <p>Example:</p>
     * <pre>
     * style().lineHeight(num(1.5))   // line-height: 1.5
     * style().flexGrow(num(1))       // flex-grow: 1
     * </pre>
     *
     * @param value the numeric value
     * @return CSSValue representing the number
     */
    public static CSSValue num(double value) {
        return () -> formatNumber(value);
    }

    /**
     * Creates a unitless number value.
     *
     * @param value the numeric value
     * @return CSSValue representing the number
     */
    public static CSSValue num(int value) {
        return () -> String.valueOf(value);
    }

    // ==================== CSS Functions ====================

    /**
     * Creates a CSS calc() expression.
     * Allows mathematical operations in CSS.
     *
     * <p>Example:</p>
     * <pre>
     * style().width(calc("100% - 20px"))
     * style().height(calc("100vh - 60px"))
     * </pre>
     *
     * @param expression the calc expression (without "calc()")
     * @return CSSValue representing the calc function
     */
    public static CSSValue calc(String expression) {
        return () -> "calc(" + expression + ")";
    }

    /**
     * Creates a CSS min() function.
     * Returns the smallest value.
     *
     * <p>Example:</p>
     * <pre>
     * style().width(min(px(300), percent(100)))  // whichever is smaller
     * </pre>
     *
     * @param values the values to compare
     * @return CSSValue representing the min function
     */
    public static CSSValue min(CSSValue... values) {
        return () -> "min(" + joinValues(values) + ")";
    }

    /**
     * Creates a CSS max() function.
     * Returns the largest value.
     *
     * <p>Example:</p>
     * <pre>
     * style().width(max(px(200), percent(50)))
     * </pre>
     *
     * @param values the values to compare
     * @return CSSValue representing the max function
     */
    public static CSSValue max(CSSValue... values) {
        return () -> "max(" + joinValues(values) + ")";
    }

    /**
     * Creates a CSS clamp() function.
     * Clamps a value between a min and max.
     *
     * <p>Example:</p>
     * <pre>
     * style().fontSize(clamp(rem(1), vw(4), rem(2)))  // responsive font size
     * style().width(clamp(px(200), percent(50), px(500)))
     * </pre>
     *
     * @param min the minimum value
     * @param preferred the preferred value
     * @param max the maximum value
     * @return CSSValue representing the clamp function
     */
    public static CSSValue clamp(CSSValue min, CSSValue preferred, CSSValue max) {
        return () -> "clamp(" + min.css() + ", " + preferred.css() + ", " + max.css() + ")";
    }

    // ==================== Helper Methods ====================

    private static String formatNumber(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    private static String joinValues(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }
}
