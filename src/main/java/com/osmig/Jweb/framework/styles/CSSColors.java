package com.osmig.Jweb.framework.styles;

/**
 * CSS color values and factory methods for creating colors.
 * Provides named color constants and functions for hex, RGB, HSL, and color manipulation.
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 * style().color(hex("#333"))
 *        .background(rgb(255, 100, 50))
 *        .borderColor(rgba(0, 0, 0, 0.5))
 *
 * // Named colors
 * style().backgroundColor(coral).color(white)
 *
 * // Color manipulation
 * style().backgroundColor(lighten(blue, 20))  // 20% lighter blue
 * </pre>
 */
public final class CSSColors {

    private CSSColors() {}

    // ==================== Special Values ====================

    /**
     * Fully transparent color.
     * Equivalent to rgba(0, 0, 0, 0).
     */
    public static final CSSValue transparent = () -> "transparent";

    /**
     * Inherits color from the element's color property.
     * Useful for borders and SVG fills that should match text color.
     */
    public static final CSSValue currentColor = () -> "currentColor";

    // ==================== Basic Colors ====================

    /** White (#fff) */
    public static final CSSValue white = () -> "#fff";
    /** Black (#000) */
    public static final CSSValue black = () -> "#000";
    /** Pure red (#f00) */
    public static final CSSValue red = () -> "#f00";
    /** Pure green (#0f0) */
    public static final CSSValue green = () -> "#0f0";
    /** Pure blue (#00f) */
    public static final CSSValue blue = () -> "#00f";
    /** Yellow (#ff0) */
    public static final CSSValue yellow = () -> "#ff0";
    /** Cyan (#0ff) */
    public static final CSSValue cyan = () -> "#0ff";
    /** Magenta (#f0f) */
    public static final CSSValue magenta = () -> "#f0f";

    // ==================== Grays ====================

    /** Medium gray (#808080) */
    public static final CSSValue gray = () -> "#808080";
    /** Dark gray (#404040) */
    public static final CSSValue darkGray = () -> "#404040";
    /** Light gray (#c0c0c0) */
    public static final CSSValue lightGray = () -> "#c0c0c0";
    /** Silver (#c0c0c0) - same as lightGray */
    public static final CSSValue silver = () -> "#c0c0c0";

    // ==================== Extended Colors ====================

    /** Orange (#ffa500) */
    public static final CSSValue orange = () -> "#ffa500";
    /** Purple (#800080) */
    public static final CSSValue purple = () -> "#800080";
    /** Pink (#ffc0cb) */
    public static final CSSValue pink = () -> "#ffc0cb";
    /** Brown (#a52a2a) */
    public static final CSSValue brown = () -> "#a52a2a";
    /** Navy blue (#000080) */
    public static final CSSValue navy = () -> "#000080";
    /** Teal (#008080) */
    public static final CSSValue teal = () -> "#008080";
    /** Olive (#808000) */
    public static final CSSValue olive = () -> "#808000";
    /** Maroon (#800000) */
    public static final CSSValue maroon = () -> "#800000";
    /** Aqua (#00ffff) - same as cyan */
    public static final CSSValue aqua = () -> "#00ffff";
    /** Lime (#00ff00) */
    public static final CSSValue lime = () -> "#00ff00";
    /** Coral (#ff7f50) */
    public static final CSSValue coral = () -> "#ff7f50";
    /** Crimson (#dc143c) */
    public static final CSSValue crimson = () -> "#dc143c";
    /** Gold (#ffd700) */
    public static final CSSValue gold = () -> "#ffd700";
    /** Indigo (#4b0082) */
    public static final CSSValue indigo = () -> "#4b0082";
    /** Violet (#ee82ee) */
    public static final CSSValue violet = () -> "#ee82ee";
    /** Salmon (#fa8072) */
    public static final CSSValue salmon = () -> "#fa8072";
    /** Turquoise (#40e0d0) */
    public static final CSSValue turquoise = () -> "#40e0d0";
    /** Sky blue (#87ceeb) */
    public static final CSSValue skyBlue = () -> "#87ceeb";
    /** Slate gray (#708090) */
    public static final CSSValue slateGray = () -> "#708090";

    // ==================== Color Functions ====================

    /**
     * Creates a color from a hexadecimal string.
     * Automatically adds "#" prefix if not present.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(hex("#333"))     // 3-digit hex
     * style().color(hex("ff5500"))   // 6-digit hex without #
     * style().color(hex("#ff550080")) // 8-digit hex with alpha
     * </pre>
     *
     * @param value the hex color string (with or without "#")
     * @return a CSSValue for the hex color
     */
    public static CSSValue hex(String value) {
        String normalized = value.startsWith("#") ? value : "#" + value;
        return () -> normalized;
    }

    /**
     * Creates an RGB color.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(rgb(255, 128, 0))  // orange
     * // Output: color: rgb(255, 128, 0);
     * </pre>
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @return a CSSValue for the RGB color
     */
    public static CSSValue rgb(int r, int g, int b) {
        return () -> "rgb(" + r + ", " + g + ", " + b + ")";
    }

    /**
     * Creates an RGBA color with alpha transparency.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundColor(rgba(0, 0, 0, 0.5))  // 50% transparent black
     * // Output: background-color: rgba(0, 0, 0, 0.5);
     * </pre>
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @param a alpha component (0.0 = transparent, 1.0 = opaque)
     * @return a CSSValue for the RGBA color
     */
    public static CSSValue rgba(int r, int g, int b, double a) {
        return () -> "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    /**
     * Creates an HSL color (Hue, Saturation, Lightness).
     * HSL is often more intuitive for color adjustments than RGB.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(hsl(0, 100, 50))    // pure red
     * style().color(hsl(120, 100, 50))  // pure green
     * style().color(hsl(240, 100, 50))  // pure blue
     * </pre>
     *
     * @param h hue (0-360 degrees on the color wheel)
     * @param s saturation (0-100 percent)
     * @param l lightness (0-100 percent, 50 is normal)
     * @return a CSSValue for the HSL color
     */
    public static CSSValue hsl(int h, int s, int l) {
        return () -> "hsl(" + h + ", " + s + "%, " + l + "%)";
    }

    /**
     * Creates an HSLA color with alpha transparency.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundColor(hsla(200, 100, 50, 0.5))  // semi-transparent blue
     * </pre>
     *
     * @param h hue (0-360 degrees)
     * @param s saturation (0-100 percent)
     * @param l lightness (0-100 percent)
     * @param a alpha component (0.0 = transparent, 1.0 = opaque)
     * @return a CSSValue for the HSLA color
     */
    public static CSSValue hsla(int h, int s, int l, double a) {
        return () -> "hsla(" + h + ", " + s + "%, " + l + "%, " + a + ")";
    }

    // ==================== Color Manipulation ====================

    /**
     * Mixes two colors together using CSS color-mix().
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundColor(colorMix(red, blue, 50))  // purple (50% each)
     * // Output: background-color: color-mix(in srgb, red 50%, blue);
     * </pre>
     *
     * @param color1 the first color
     * @param color2 the second color
     * @param percent the percentage of color1 (0-100)
     * @return a CSSValue for the mixed color
     */
    public static CSSValue colorMix(CSSValue color1, CSSValue color2, int percent) {
        return () -> "color-mix(in srgb, " + color1.css() + " " + percent + "%, " + color2.css() + ")";
    }

    /**
     * Lightens a color by mixing it with white.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundColor(lighten(blue, 30))  // 30% lighter blue
     * </pre>
     *
     * @param color the base color
     * @param percent how much to lighten (0-100)
     * @return a CSSValue for the lightened color
     */
    public static CSSValue lighten(CSSValue color, int percent) {
        return colorMix(white, color, 100 - percent);
    }

    /**
     * Darkens a color by mixing it with black.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundColor(darken(red, 20))  // 20% darker red
     * </pre>
     *
     * @param color the base color
     * @param percent how much to darken (0-100)
     * @return a CSSValue for the darkened color
     */
    public static CSSValue darken(CSSValue color, int percent) {
        return colorMix(black, color, 100 - percent);
    }

    /**
     * Creates a light-dark() function that automatically switches between
     * light and dark mode colors based on the color-scheme property.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(lightDark(black, white))
     * // Output: color: light-dark(black, white);
     * // Shows black in light mode, white in dark mode
     * </pre>
     *
     * @param lightColor the color to use in light mode
     * @param darkColor the color to use in dark mode
     * @return a CSSValue for the light-dark() function
     */
    public static CSSValue lightDark(CSSValue lightColor, CSSValue darkColor) {
        return () -> "light-dark(" + lightColor.css() + ", " + darkColor.css() + ")";
    }
}
