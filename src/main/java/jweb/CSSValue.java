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
}
