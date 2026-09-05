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
}
