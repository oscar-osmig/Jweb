package com.osmig.Jweb.framework.styles;

/**
 * The {@code var()} and {@code env()} reference functions.
 *
 * <p>Design tokens themselves live in {@link jweb.css.Theme} — one token
 * builder, custom-property based, with a dark-scheme block. The three extra
 * naming schemes that used to live here ({@code designSystem()},
 * {@code theme()}, and the {@code scoped}/{@code component} string helpers)
 * are gone.</p>
 *
 * <p>CSS Variables enable dynamic styling with runtime value changes,
 * theming support, and cleaner design token management.</p>
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSSVariables.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 *
 * // Define variables in :root
 * rule(":root")
 *     .var("primary-color", blue)
 *     .var("spacing-base", rem(1))
 *     .var("border-radius", px(4));
 *
 * // Use variables with var()
 * style().color(var("primary-color"))
 *        .padding(var("spacing-base"))
 *        .borderRadius(var("border-radius"));
 *
 * // With fallback
 * style().color(var("theme-color", blue));
 *
 * // Nested fallbacks
 * style().color(var("custom-color", var("primary-color", red)));
 *
 * // Design tokens
 * Theme.light().color("primary", hex("#4f46e5")).space("4", rem(1));
 * style().color(Theme.color("primary")).padding(Theme.space("4"));
 * </pre>
 *
 * @see Style#var(String, CSSValue) for defining variables in styles
 *
 * @deprecated Replaced by {@code jweb.Css} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class CSSVariables extends CSSAnimations {

    protected CSSVariables() {}

    // ==================== Variable Reference Functions ====================

    /**
     * References a CSS custom property (variable).
     *
     * <p>Example:</p>
     * <pre>
     * style().color(var("primary-color"))
     * // Output: color: var(--primary-color);
     * </pre>
     *
     * @param name the variable name (without "--" prefix)
     * @return CSSValue representing var(--name)
     */
    public static CSSValue var(String name) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ")";
    }

    /**
     * References a CSS custom property with a fallback value.
     * The fallback is used if the variable is not defined.
     *
     * <p>Example:</p>
     * <pre>
     * style().color(var("theme-color", blue))
     * // Output: color: var(--theme-color, blue);
     * </pre>
     *
     * @param name the variable name
     * @param fallback the fallback value if variable is not defined
     * @return CSSValue representing var(--name, fallback)
     */
    public static CSSValue var(String name, jweb.CSSValue fallback) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ", " + fallback.css() + ")";
    }

    /**
     * References a CSS custom property with a string fallback.
     *
     * @param name the variable name
     * @param fallback the fallback value as a string
     * @return CSSValue representing var(--name, fallback)
     */
    public static CSSValue var(String name, String fallback) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return () -> "var(" + normalized + ", " + fallback + ")";
    }

    /**
     * Creates a nested var() reference with multiple fallback levels.
     *
     * <p>Example:</p>
     * <pre>
     * // Try custom-color, then primary-color, then default to red
     * style().color(varChain("custom-color", "primary-color", red))
     * // Output: color: var(--custom-color, var(--primary-color, red));
     * </pre>
     *
     * @param names variable names to try in order
     * @param finalFallback the final fallback value
     * @return CSSValue with nested var() fallbacks
     */
    public static CSSValue varChain(String name1, String name2, jweb.CSSValue finalFallback) {
        return var(name1, var(name2, finalFallback));
    }

    /**
     * Creates a nested var() reference with multiple levels.
     *
     * @param name1 first variable to try
     * @param name2 second variable to try
     * @param name3 third variable to try
     * @param finalFallback final fallback value
     * @return CSSValue with nested var() fallbacks
     */
    public static CSSValue varChain(String name1, String name2, String name3, jweb.CSSValue finalFallback) {
        return var(name1, var(name2, var(name3, finalFallback)));
    }

    // ==================== Environment Variables ====================

    /**
     * References a CSS environment variable (safe areas, etc.).
     * Environment variables are provided by the browser/OS.
     *
     * <p>Common environment variables:</p>
     * <ul>
     *   <li>safe-area-inset-top</li>
     *   <li>safe-area-inset-bottom</li>
     *   <li>safe-area-inset-left</li>
     *   <li>safe-area-inset-right</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>
     * style().paddingTop(env("safe-area-inset-top"))
     * // Output: padding-top: env(safe-area-inset-top);
     * </pre>
     *
     * @param name the environment variable name
     * @return CSSValue representing env(name)
     */
    public static CSSValue env(String name) {
        return () -> "env(" + name + ")";
    }

    /**
     * References a CSS environment variable with fallback.
     *
     * <p>Example:</p>
     * <pre>
     * style().paddingTop(env("safe-area-inset-top", px(20)))
     * // Output: padding-top: env(safe-area-inset-top, 20px);
     * </pre>
     *
     * @param name the environment variable name
     * @param fallback the fallback value
     * @return CSSValue representing env(name, fallback)
     */
    public static CSSValue env(String name, jweb.CSSValue fallback) {
        return () -> "env(" + name + ", " + fallback.css() + ")";
    }

}
