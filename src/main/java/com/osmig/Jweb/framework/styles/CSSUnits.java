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

    // ==================== Raw String Value ====================

    /**
     * Wraps a raw string as a CSSValue.
     * Use this when the DSL doesn't provide a type-safe method for a CSS value.
     *
     * <p>Example:</p>
     * <pre>
     * // For animation names
     * style().animation(raw("fadeIn"), s(0.3), ease)
     *
     * // For complex background-size
     * style().backgroundSize(raw("300% 100%"))
     *
     * // For any custom value
     * style().prop("grid-template-columns", raw("1fr auto 1fr"))
     * </pre>
     *
     * @param value the raw CSS value string
     * @return CSSValue wrapping the string
     */
    public static CSSValue raw(String value) {
        return () -> value;
    }

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

    // ==================== Advanced Color Functions ====================
    // Note: Basic rgb(), rgba(), hsl(), hsla() are in CSSColors.java
    // This section contains advanced/modern color functions not in CSSColors

    /**
     * Creates an hwb() color value (Hue, Whiteness, Blackness).
     *
     * <p>Example:</p>
     * <pre>
     * style().color(hwb(200, 10, 20))
     * </pre>
     *
     * @param h hue (0-360)
     * @param w whiteness (0-100)
     * @param b blackness (0-100)
     * @return CSSValue representing the hwb color
     */
    public static CSSValue hwb(int h, int w, int b) {
        return () -> "hwb(" + h + " " + w + "% " + b + "%)";
    }

    /**
     * Creates an hwb() color value with alpha.
     *
     * @param h hue (0-360)
     * @param w whiteness (0-100)
     * @param b blackness (0-100)
     * @param a alpha (0-1)
     * @return CSSValue representing the hwb color
     */
    public static CSSValue hwb(int h, int w, int b, double a) {
        return () -> "hwb(" + h + " " + w + "% " + b + "% / " + formatNumber(a) + ")";
    }

    /**
     * Creates a lab() color value (CIE Lab).
     *
     * <p>Example:</p>
     * <pre>
     * style().color(lab(50, 80, -80))  // purple
     * </pre>
     *
     * @param l lightness (0-100)
     * @param a a-axis (green-red, typically -128 to 127)
     * @param b b-axis (blue-yellow, typically -128 to 127)
     * @return CSSValue representing the lab color
     */
    public static CSSValue lab(double l, double a, double b) {
        return () -> "lab(" + formatNumber(l) + "% " + formatNumber(a) + " " + formatNumber(b) + ")";
    }

    /**
     * Creates an lch() color value (CIE LCH).
     *
     * <p>Example:</p>
     * <pre>
     * style().color(lch(50, 100, 270))  // blue
     * </pre>
     *
     * @param l lightness (0-100)
     * @param c chroma (0-150+)
     * @param h hue (0-360)
     * @return CSSValue representing the lch color
     */
    public static CSSValue lch(double l, double c, double h) {
        return () -> "lch(" + formatNumber(l) + "% " + formatNumber(c) + " " + formatNumber(h) + ")";
    }

    /**
     * Creates an oklab() color value (perceptually uniform).
     *
     * @param l lightness (0-1)
     * @param a a-axis (-0.4 to 0.4)
     * @param b b-axis (-0.4 to 0.4)
     * @return CSSValue representing the oklab color
     */
    public static CSSValue oklab(double l, double a, double b) {
        return () -> "oklab(" + formatNumber(l) + " " + formatNumber(a) + " " + formatNumber(b) + ")";
    }

    /**
     * Creates an oklch() color value (perceptually uniform cylindrical).
     *
     * <p>Example:</p>
     * <pre>
     * style().color(oklch(0.7, 0.15, 200))  // nice blue
     * </pre>
     *
     * @param l lightness (0-1)
     * @param c chroma (0-0.4+)
     * @param h hue (0-360)
     * @return CSSValue representing the oklch color
     */
    public static CSSValue oklch(double l, double c, double h) {
        return () -> "oklch(" + formatNumber(l) + " " + formatNumber(c) + " " + formatNumber(h) + ")";
    }

    /**
     * Creates a color-mix() function for mixing two colors.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(colorMix("srgb", red, blue, 50))  // purple
     * style().color(colorMix("oklch", hex("#ff0000"), hex("#0000ff"), 30))
     * </pre>
     *
     * @param colorSpace color space (srgb, oklch, oklab, etc.)
     * @param color1 first color
     * @param color2 second color
     * @param percent percentage of first color (0-100)
     * @return CSSValue representing the mixed color
     */
    public static CSSValue colorMix(String colorSpace, CSSValue color1, CSSValue color2, int percent) {
        return () -> "color-mix(in " + colorSpace + ", " + color1.css() + " " + percent + "%, " + color2.css() + ")";
    }

    /**
     * Creates a color-mix() with equal mixing.
     */
    public static CSSValue colorMix(String colorSpace, CSSValue color1, CSSValue color2) {
        return () -> "color-mix(in " + colorSpace + ", " + color1.css() + ", " + color2.css() + ")";
    }

    /**
     * Creates a light-dark() function for automatic theme colors.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(lightDark(black, white))  // black in light mode, white in dark
     * </pre>
     *
     * @param lightColor color for light mode
     * @param darkColor color for dark mode
     * @return CSSValue representing the theme-aware color
     */
    public static CSSValue lightDark(CSSValue lightColor, CSSValue darkColor) {
        return () -> "light-dark(" + lightColor.css() + ", " + darkColor.css() + ")";
    }

    /**
     * Creates a color() function for wide-gamut colors.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(color("display-p3", 1, 0.5, 0))  // P3 orange
     * </pre>
     *
     * @param colorSpace color space (srgb, display-p3, a98-rgb, etc.)
     * @param c1 first channel
     * @param c2 second channel
     * @param c3 third channel
     * @return CSSValue representing the color
     */
    public static CSSValue color(String colorSpace, double c1, double c2, double c3) {
        return () -> "color(" + colorSpace + " " + formatNumber(c1) + " " + formatNumber(c2) + " " + formatNumber(c3) + ")";
    }

    // ==================== Repeating Gradients ====================

    /**
     * Creates a repeating-linear-gradient().
     *
     * <p>Example:</p>
     * <pre>
     * style().background(repeatingLinearGradient("45deg", red, blue, px(20)))
     * </pre>
     *
     * @param direction gradient direction
     * @param stops color stops
     * @return CSSValue representing the gradient
     */
    public static CSSValue repeatingLinearGradient(String direction, CSSValue... stops) {
        return () -> "repeating-linear-gradient(" + direction + ", " + joinValues(stops) + ")";
    }

    /**
     * Creates a repeating-linear-gradient() without direction.
     */
    public static CSSValue repeatingLinearGradient(CSSValue... stops) {
        return () -> "repeating-linear-gradient(" + joinValues(stops) + ")";
    }

    /**
     * Creates a repeating-radial-gradient().
     *
     * <p>Example:</p>
     * <pre>
     * style().background(repeatingRadialGradient("circle", red, blue, px(20)))
     * </pre>
     *
     * @param shape gradient shape
     * @param stops color stops
     * @return CSSValue representing the gradient
     */
    public static CSSValue repeatingRadialGradient(String shape, CSSValue... stops) {
        return () -> "repeating-radial-gradient(" + shape + ", " + joinValues(stops) + ")";
    }

    /**
     * Creates a repeating-radial-gradient() without shape.
     */
    public static CSSValue repeatingRadialGradient(CSSValue... stops) {
        return () -> "repeating-radial-gradient(" + joinValues(stops) + ")";
    }

    /**
     * Creates a repeating-conic-gradient().
     *
     * <p>Example:</p>
     * <pre>
     * style().background(repeatingConicGradient("from 0deg", red, blue))
     * </pre>
     *
     * @param from starting position
     * @param stops color stops
     * @return CSSValue representing the gradient
     */
    public static CSSValue repeatingConicGradient(String from, CSSValue... stops) {
        return () -> "repeating-conic-gradient(" + from + ", " + joinValues(stops) + ")";
    }

    /**
     * Creates a repeating-conic-gradient() without position.
     */
    public static CSSValue repeatingConicGradient(CSSValue... stops) {
        return () -> "repeating-conic-gradient(" + joinValues(stops) + ")";
    }

    // ==================== CSS Math Functions ====================

    /**
     * Creates a CSS abs() function.
     *
     * @param value the value to get absolute value of
     * @return CSSValue representing abs(value)
     */
    public static CSSValue abs(CSSValue value) {
        return () -> "abs(" + value.css() + ")";
    }

    /**
     * Creates a CSS sign() function.
     *
     * @param value the value to get sign of (-1, 0, 1)
     * @return CSSValue representing sign(value)
     */
    public static CSSValue sign(CSSValue value) {
        return () -> "sign(" + value.css() + ")";
    }

    /**
     * Creates a CSS round() function.
     *
     * @param strategy rounding strategy (nearest, up, down, to-zero)
     * @param value the value to round
     * @param interval the rounding interval
     * @return CSSValue representing round()
     */
    public static CSSValue round(String strategy, CSSValue value, CSSValue interval) {
        return () -> "round(" + strategy + ", " + value.css() + ", " + interval.css() + ")";
    }

    /**
     * Creates a CSS round() function with default nearest rounding.
     */
    public static CSSValue round(CSSValue value, CSSValue interval) {
        return () -> "round(" + value.css() + ", " + interval.css() + ")";
    }

    /**
     * Creates a CSS mod() function (remainder).
     *
     * @param a dividend
     * @param b divisor
     * @return CSSValue representing mod(a, b)
     */
    public static CSSValue mod(CSSValue a, CSSValue b) {
        return () -> "mod(" + a.css() + ", " + b.css() + ")";
    }

    /**
     * Creates a CSS rem() function (remainder with dividend's sign).
     *
     * @param a dividend
     * @param b divisor
     * @return CSSValue representing rem(a, b)
     */
    public static CSSValue cssRem(CSSValue a, CSSValue b) {
        return () -> "rem(" + a.css() + ", " + b.css() + ")";
    }

    /**
     * Creates a CSS sin() function.
     *
     * @param angle the angle (use deg() or rad())
     * @return CSSValue representing sin(angle)
     */
    public static CSSValue sin(CSSValue angle) {
        return () -> "sin(" + angle.css() + ")";
    }

    /**
     * Creates a CSS cos() function.
     *
     * @param angle the angle
     * @return CSSValue representing cos(angle)
     */
    public static CSSValue cos(CSSValue angle) {
        return () -> "cos(" + angle.css() + ")";
    }

    /**
     * Creates a CSS tan() function.
     *
     * @param angle the angle
     * @return CSSValue representing tan(angle)
     */
    public static CSSValue tan(CSSValue angle) {
        return () -> "tan(" + angle.css() + ")";
    }

    /**
     * Creates a CSS asin() function (arc sine).
     *
     * @param value the value (-1 to 1)
     * @return CSSValue representing asin(value)
     */
    public static CSSValue asin(CSSValue value) {
        return () -> "asin(" + value.css() + ")";
    }

    /**
     * Creates a CSS acos() function (arc cosine).
     *
     * @param value the value (-1 to 1)
     * @return CSSValue representing acos(value)
     */
    public static CSSValue acos(CSSValue value) {
        return () -> "acos(" + value.css() + ")";
    }

    /**
     * Creates a CSS atan() function (arc tangent).
     *
     * @param value the value
     * @return CSSValue representing atan(value)
     */
    public static CSSValue atan(CSSValue value) {
        return () -> "atan(" + value.css() + ")";
    }

    /**
     * Creates a CSS atan2() function (2-argument arc tangent).
     *
     * @param y the y coordinate
     * @param x the x coordinate
     * @return CSSValue representing atan2(y, x)
     */
    public static CSSValue atan2(CSSValue y, CSSValue x) {
        return () -> "atan2(" + y.css() + ", " + x.css() + ")";
    }

    /**
     * Creates a CSS pow() function (exponentiation).
     *
     * @param base the base value
     * @param exponent the exponent
     * @return CSSValue representing pow(base, exponent)
     */
    public static CSSValue pow(CSSValue base, CSSValue exponent) {
        return () -> "pow(" + base.css() + ", " + exponent.css() + ")";
    }

    /**
     * Creates a CSS sqrt() function.
     *
     * @param value the value
     * @return CSSValue representing sqrt(value)
     */
    public static CSSValue sqrt(CSSValue value) {
        return () -> "sqrt(" + value.css() + ")";
    }

    /**
     * Creates a CSS hypot() function (hypotenuse).
     *
     * @param values the values
     * @return CSSValue representing hypot(values...)
     */
    public static CSSValue hypot(CSSValue... values) {
        return () -> "hypot(" + joinValues(values) + ")";
    }

    /**
     * Creates a CSS log() function (natural logarithm).
     *
     * @param value the value
     * @return CSSValue representing log(value)
     */
    public static CSSValue log(CSSValue value) {
        return () -> "log(" + value.css() + ")";
    }

    /**
     * Creates a CSS log() function with base.
     *
     * @param value the value
     * @param base the logarithm base
     * @return CSSValue representing log(value, base)
     */
    public static CSSValue log(CSSValue value, CSSValue base) {
        return () -> "log(" + value.css() + ", " + base.css() + ")";
    }

    /**
     * Creates a CSS exp() function (e^value).
     *
     * @param value the exponent
     * @return CSSValue representing exp(value)
     */
    public static CSSValue exp(CSSValue value) {
        return () -> "exp(" + value.css() + ")";
    }

    // ==================== CSS Environment Variables ====================

    /**
     * Creates an env() function for environment variables.
     *
     * <p>Example:</p>
     * <pre>
     * style().paddingTop(env("safe-area-inset-top"))
     * style().paddingBottom(env("safe-area-inset-bottom", px(20)))
     * </pre>
     *
     * @param name environment variable name
     * @return CSSValue representing env(name)
     */
    public static CSSValue env(String name) {
        return () -> "env(" + name + ")";
    }

    /**
     * Creates an env() function with fallback.
     *
     * @param name environment variable name
     * @param fallback fallback value
     * @return CSSValue representing env(name, fallback)
     */
    public static CSSValue env(String name, CSSValue fallback) {
        return () -> "env(" + name + ", " + fallback.css() + ")";
    }

    // ==================== CSS Image Functions ====================

    /**
     * Creates an image-set() function for responsive images.
     *
     * <p>Example:</p>
     * <pre>
     * style().backgroundImage(imageSet("url('img.png') 1x", "url('img@2x.png') 2x"))
     * </pre>
     *
     * @param images the image variants
     * @return CSSValue representing image-set()
     */
    public static CSSValue imageSet(String... images) {
        return () -> "image-set(" + String.join(", ", images) + ")";
    }

    // ==================== Animation Steps ====================

    /**
     * Creates a steps() timing function.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimingFunction(steps(5, "end"))
     * </pre>
     *
     * @param count number of steps
     * @param position jump position (start, end, jump-start, jump-end, jump-none, jump-both)
     * @return CSSValue representing steps()
     */
    public static CSSValue steps(int count, String position) {
        return () -> "steps(" + count + ", " + position + ")";
    }

    /**
     * Creates a steps() timing function with default end position.
     *
     * @param count number of steps
     * @return CSSValue representing steps()
     */
    public static CSSValue steps(int count) {
        return () -> "steps(" + count + ")";
    }

    // ==================== Dynamic Viewport Units ====================

    /** Creates a dynamic viewport height (dvh) value. */
    public static CSSValue dvh(double value) {
        return () -> formatNumber(value) + "dvh";
    }

    /** Creates a dynamic viewport width (dvw) value. */
    public static CSSValue dvw(double value) {
        return () -> formatNumber(value) + "dvw";
    }

    /** Creates a small viewport height (svh) value. */
    public static CSSValue svh(double value) {
        return () -> formatNumber(value) + "svh";
    }

    /** Creates a small viewport width (svw) value. */
    public static CSSValue svw(double value) {
        return () -> formatNumber(value) + "svw";
    }

    /** Creates a large viewport height (lvh) value. */
    public static CSSValue lvh(double value) {
        return () -> formatNumber(value) + "lvh";
    }

    /** Creates a large viewport width (lvw) value. */
    public static CSSValue lvw(double value) {
        return () -> formatNumber(value) + "lvw";
    }

    /** Creates a dynamic viewport inline (dvi) value. */
    public static CSSValue dvi(double value) {
        return () -> formatNumber(value) + "dvi";
    }

    /** Creates a dynamic viewport block (dvb) value. */
    public static CSSValue dvb(double value) {
        return () -> formatNumber(value) + "dvb";
    }

    // ==================== Container Query Units ====================

    /** Creates a container query width (cqw) value - 1% of container width. */
    public static CSSValue cqw(double value) {
        return () -> formatNumber(value) + "cqw";
    }

    /** Creates a container query height (cqh) value - 1% of container height. */
    public static CSSValue cqh(double value) {
        return () -> formatNumber(value) + "cqh";
    }

    /** Creates a container query inline (cqi) value. */
    public static CSSValue cqi(double value) {
        return () -> formatNumber(value) + "cqi";
    }

    /** Creates a container query block (cqb) value. */
    public static CSSValue cqb(double value) {
        return () -> formatNumber(value) + "cqb";
    }

    /** Creates a container query min (cqmin) value. */
    public static CSSValue cqmin(double value) {
        return () -> formatNumber(value) + "cqmin";
    }

    /** Creates a container query max (cqmax) value. */
    public static CSSValue cqmax(double value) {
        return () -> formatNumber(value) + "cqmax";
    }

    // ==================== Font-relative Units ====================

    /** Creates an ex value (height of lowercase 'x'). */
    public static CSSValue ex(double value) {
        return () -> formatNumber(value) + "ex";
    }

    /** Creates a cap value (height of capital letters). */
    public static CSSValue cap(double value) {
        return () -> formatNumber(value) + "cap";
    }

    /** Creates an ic value (width of CJK ideograph). */
    public static CSSValue ic(double value) {
        return () -> formatNumber(value) + "ic";
    }

    /** Creates an lh value (line height of element). */
    public static CSSValue lh(double value) {
        return () -> formatNumber(value) + "lh";
    }

    /** Creates an rlh value (line height of root element). */
    public static CSSValue rlh(double value) {
        return () -> formatNumber(value) + "rlh";
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
