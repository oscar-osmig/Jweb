package jweb;

/**
 * Represents any CSS value (units, colors, keywords, variables).
 * All CSS values can be converted to their string representation.
 *
 * <pre>{@code
 * import jweb.CSSValue;
 * import static jweb.Css.*;
 *
 * CSSValue accent = hsl(220, 90, 56);
 * }</pre>
 */
@FunctionalInterface
public interface CSSValue {
    String css();

    /**
     * Wraps any CSS text as a typed value, for the positions that take a
     * {@code CSSValue} rather than a String (multi-argument shorthands,
     * design-token constants):
     *
     * <pre>{@code
     * style().margin(CSSValue.of("calc(100% - 2rem)"), auto)
     * static final CSSValue BRAND = CSSValue.of("oklch(70% 0.15 200)");
     * }</pre>
     *
     * <p>Every single-value property also accepts a plain String directly,
     * so this is only needed where the type is required.</p>
     */
    static CSSValue of(String css) {
        return () -> css;
    }

    // ==================== Arithmetic ====================
    // vh(100).minus(px(50)) -> calc(100vh - 50px); nested calcs flatten.

    /** {@code calc(this + other)}. */
    default CSSValue plus(CSSValue other) {
        return calc(inner(this) + " + " + inner(other));
    }

    /** {@code calc(this - other)}. */
    default CSSValue minus(CSSValue other) {
        return calc(inner(this) + " - " + inner(other));
    }

    /** {@code calc(this * factor)}. */
    default CSSValue times(double factor) {
        return calc(inner(this) + " * " + number(factor));
    }

    /** {@code calc(this / divisor)}. */
    default CSSValue div(double divisor) {
        return calc(inner(this) + " / " + number(divisor));
    }

    private static CSSValue calc(String expression) {
        return () -> "calc(" + expression + ")";
    }

    /** A whole-valued double prints without the ".0". */
    private static String number(double n) {
        return n == Math.rint(n) && !Double.isInfinite(n) ? String.valueOf((long) n) : String.valueOf(n);
    }

    /** The expression inside a calc(), or the value itself. */
    private static String inner(CSSValue value) {
        String css = value.css();
        if (css.startsWith("calc(") && css.endsWith(")")) {
            return css.substring(5, css.length() - 1);
        }
        return css;
    }
}
