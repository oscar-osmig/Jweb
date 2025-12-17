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

    // Common constants
    public static final CSSValue zero = () -> "0";
    public static final CSSValue auto = () -> "auto";
    public static final CSSValue inherit = () -> "inherit";
    public static final CSSValue initial = () -> "initial";
    public static final CSSValue unset = () -> "unset";
    public static final CSSValue none = () -> "none";

    // Pixel units
    public static CSSValue px(int value) {
        return () -> value + "px";
    }

    public static CSSValue px(double value) {
        return () -> formatNumber(value) + "px";
    }

    // Rem units (relative to root font size)
    public static CSSValue rem(double value) {
        return () -> formatNumber(value) + "rem";
    }

    public static CSSValue rem(int value) {
        return () -> value + "rem";
    }

    // Em units (relative to parent font size)
    public static CSSValue em(double value) {
        return () -> formatNumber(value) + "em";
    }

    public static CSSValue em(int value) {
        return () -> value + "em";
    }

    // Percentage
    public static CSSValue percent(double value) {
        return () -> formatNumber(value) + "%";
    }

    public static CSSValue percent(int value) {
        return () -> value + "%";
    }

    // Viewport units
    public static CSSValue vh(double value) {
        return () -> formatNumber(value) + "vh";
    }

    public static CSSValue vh(int value) {
        return () -> value + "vh";
    }

    public static CSSValue vw(double value) {
        return () -> formatNumber(value) + "vw";
    }

    public static CSSValue vw(int value) {
        return () -> value + "vw";
    }

    public static CSSValue vmin(double value) {
        return () -> formatNumber(value) + "vmin";
    }

    public static CSSValue vmax(double value) {
        return () -> formatNumber(value) + "vmax";
    }

    // Character units
    public static CSSValue ch(double value) {
        return () -> formatNumber(value) + "ch";
    }

    // Time units (for transitions/animations)
    public static CSSValue ms(int value) {
        return () -> value + "ms";
    }

    public static CSSValue s(double value) {
        return () -> formatNumber(value) + "s";
    }

    // Angle units (for transforms)
    public static CSSValue deg(double value) {
        return () -> formatNumber(value) + "deg";
    }

    public static CSSValue rad(double value) {
        return () -> formatNumber(value) + "rad";
    }

    public static CSSValue turn(double value) {
        return () -> formatNumber(value) + "turn";
    }

    // Fraction units (for grid)
    public static CSSValue fr(double value) {
        return () -> formatNumber(value) + "fr";
    }

    public static CSSValue fr(int value) {
        return () -> value + "fr";
    }

    // Raw number (for line-height, flex-grow, etc.)
    public static CSSValue num(double value) {
        return () -> formatNumber(value);
    }

    public static CSSValue num(int value) {
        return () -> String.valueOf(value);
    }

    // CSS calc() function
    public static CSSValue calc(String expression) {
        return () -> "calc(" + expression + ")";
    }

    // CSS min(), max(), clamp()
    public static CSSValue min(CSSValue... values) {
        return () -> "min(" + joinValues(values) + ")";
    }

    public static CSSValue max(CSSValue... values) {
        return () -> "max(" + joinValues(values) + ")";
    }

    public static CSSValue clamp(CSSValue min, CSSValue preferred, CSSValue max) {
        return () -> "clamp(" + min.css() + ", " + preferred.css() + ", " + max.css() + ")";
    }

    // Helper to format numbers without unnecessary decimals
    private static String formatNumber(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    // Helper to join multiple values
    private static String joinValues(CSSValue[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(values[i].css());
        }
        return sb.toString();
    }
}
