package com.osmig.Jweb.framework.styles;

/**
 * CSS color values and factory methods.
 *
 * Usage with static import:
 *   import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 *   style().color(hex("#333"))
 *          .background(rgb(255, 100, 50))
 *          .borderColor(rgba(0, 0, 0, 0.5))
 */
public final class CSSColors {

    private CSSColors() {}

    // Named colors - most commonly used
    public static final CSSValue transparent = () -> "transparent";
    public static final CSSValue currentColor = () -> "currentColor";

    // Basic colors
    public static final CSSValue white = () -> "#fff";
    public static final CSSValue black = () -> "#000";
    public static final CSSValue red = () -> "#f00";
    public static final CSSValue green = () -> "#0f0";
    public static final CSSValue blue = () -> "#00f";
    public static final CSSValue yellow = () -> "#ff0";
    public static final CSSValue cyan = () -> "#0ff";
    public static final CSSValue magenta = () -> "#f0f";

    // Grays
    public static final CSSValue gray = () -> "#808080";
    public static final CSSValue darkGray = () -> "#404040";
    public static final CSSValue lightGray = () -> "#c0c0c0";
    public static final CSSValue silver = () -> "#c0c0c0";

    // Extended colors
    public static final CSSValue orange = () -> "#ffa500";
    public static final CSSValue purple = () -> "#800080";
    public static final CSSValue pink = () -> "#ffc0cb";
    public static final CSSValue brown = () -> "#a52a2a";
    public static final CSSValue navy = () -> "#000080";
    public static final CSSValue teal = () -> "#008080";
    public static final CSSValue olive = () -> "#808000";
    public static final CSSValue maroon = () -> "#800000";
    public static final CSSValue aqua = () -> "#00ffff";
    public static final CSSValue lime = () -> "#00ff00";
    public static final CSSValue coral = () -> "#ff7f50";
    public static final CSSValue crimson = () -> "#dc143c";
    public static final CSSValue gold = () -> "#ffd700";
    public static final CSSValue indigo = () -> "#4b0082";
    public static final CSSValue violet = () -> "#ee82ee";
    public static final CSSValue salmon = () -> "#fa8072";
    public static final CSSValue turquoise = () -> "#40e0d0";
    public static final CSSValue skyBlue = () -> "#87ceeb";
    public static final CSSValue slateGray = () -> "#708090";

    // Hex color
    public static CSSValue hex(String value) {
        String normalized = value.startsWith("#") ? value : "#" + value;
        return () -> normalized;
    }

    // RGB color
    public static CSSValue rgb(int r, int g, int b) {
        return () -> "rgb(" + r + ", " + g + ", " + b + ")";
    }

    // RGBA color with alpha
    public static CSSValue rgba(int r, int g, int b, double a) {
        return () -> "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    // HSL color
    public static CSSValue hsl(int h, int s, int l) {
        return () -> "hsl(" + h + ", " + s + "%, " + l + "%)";
    }

    // HSLA color with alpha
    public static CSSValue hsla(int h, int s, int l, double a) {
        return () -> "hsla(" + h + ", " + s + "%, " + l + "%, " + a + ")";
    }

    // CSS color-mix() function
    public static CSSValue colorMix(CSSValue color1, CSSValue color2, int percent) {
        return () -> "color-mix(in srgb, " + color1.css() + " " + percent + "%, " + color2.css() + ")";
    }

    // Lighten a color (mix with white)
    public static CSSValue lighten(CSSValue color, int percent) {
        return colorMix(white, color, 100 - percent);
    }

    // Darken a color (mix with black)
    public static CSSValue darken(CSSValue color, int percent) {
        return colorMix(black, color, 100 - percent);
    }
}
