package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive CSS Custom Properties (CSS Variables) utilities.
 * Provides type-safe API for defining, referencing, and managing CSS variables
 * including theming, design systems, and scoped variables.
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
 * // Design system helpers
 * String tokens = designSystem()
 *     .spacing(rem(0.25), rem(0.5), rem(1), rem(1.5), rem(2))
 *     .colors("primary", "#007bff", "secondary", "#6c757d")
 *     .borderRadius(px(2), px(4), px(8), px(16))
 *     .build();
 * </pre>
 *
 * @see Style#var(String, CSSValue) for defining variables in styles
 */
public final class CSSVariables {

    private CSSVariables() {}

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
    public static CSSValue var(String name, CSSValue fallback) {
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
    public static CSSValue varChain(String name1, String name2, CSSValue finalFallback) {
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
    public static CSSValue varChain(String name1, String name2, String name3, CSSValue finalFallback) {
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
    public static CSSValue env(String name, CSSValue fallback) {
        return () -> "env(" + name + ", " + fallback.css() + ")";
    }

    // ==================== Variable Naming Helpers ====================

    /**
     * Creates a scoped variable name with a prefix.
     *
     * <p>Example:</p>
     * <pre>
     * String btnColor = scoped("btn", "color");  // "btn-color"
     * rule(".btn").var(btnColor, blue);
     * // Output: .btn { --btn-color: blue; }
     * </pre>
     *
     * @param scope the scope/prefix for the variable
     * @param name the variable name
     * @return the scoped variable name (without "--" prefix)
     */
    public static String scoped(String scope, String name) {
        return scope + "-" + name;
    }

    /**
     * Creates a component-scoped variable name.
     *
     * <p>Example:</p>
     * <pre>
     * String cardPadding = component("card", "padding");  // "card-padding"
     * </pre>
     *
     * @param component the component name
     * @param property the property name
     * @return the component-scoped variable name
     */
    public static String component(String component, String property) {
        return component + "-" + property;
    }

    /**
     * Creates a theme variable name.
     *
     * <p>Example:</p>
     * <pre>
     * String bgColor = theme("background");  // "theme-background"
     * </pre>
     *
     * @param name the theme property name
     * @return the theme variable name
     */
    public static String theme(String name) {
        return "theme-" + name;
    }

    // ==================== Design System Builder ====================

    /**
     * Creates a new design system builder for defining design tokens.
     *
     * <p>Example:</p>
     * <pre>
     * String tokens = designSystem()
     *     .spacing(rem(0.25), rem(0.5), rem(1), rem(1.5))
     *     .colors("primary", blue, "secondary", gray)
     *     .fontSize(rem(0.875), rem(1), rem(1.25), rem(1.5))
     *     .build();
     * </pre>
     *
     * @return a new DesignSystemBuilder instance
     */
    public static DesignSystemBuilder designSystem() {
        return new DesignSystemBuilder();
    }

    /**
     * Builder for creating comprehensive design system tokens.
     */
    public static class DesignSystemBuilder {
        private final Map<String, String> variables = new LinkedHashMap<>();
        private String prefix = "";

        /**
         * Sets a prefix for all generated variable names.
         *
         * @param prefix the prefix to use (e.g., "ds", "theme")
         * @return this builder for chaining
         */
        public DesignSystemBuilder prefix(String prefix) {
            this.prefix = prefix.isEmpty() ? "" : prefix + "-";
            return this;
        }

        /**
         * Defines a spacing scale.
         * Creates variables: --spacing-1, --spacing-2, etc.
         *
         * <p>Example:</p>
         * <pre>
         * .spacing(rem(0.25), rem(0.5), rem(1), rem(1.5), rem(2))
         * // Creates: --spacing-1: 0.25rem; --spacing-2: 0.5rem; etc.
         * </pre>
         *
         * @param values the spacing values in order
         * @return this builder for chaining
         */
        public DesignSystemBuilder spacing(CSSValue... values) {
            for (int i = 0; i < values.length; i++) {
                variables.put(prefix + "spacing-" + (i + 1), values[i].css());
            }
            return this;
        }

        /**
         * Defines named color variables.
         *
         * <p>Example:</p>
         * <pre>
         * .colors("primary", blue, "secondary", gray, "danger", red)
         * // Creates: --primary: blue; --secondary: gray; --danger: red;
         * </pre>
         *
         * @param pairs alternating name-value pairs (name1, color1, name2, color2, ...)
         * @return this builder for chaining
         */
        public DesignSystemBuilder colors(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + pairs[i].toString();
                    String value = pairs[i + 1] instanceof CSSValue
                        ? ((CSSValue) pairs[i + 1]).css()
                        : pairs[i + 1].toString();
                    variables.put(name, value);
                }
            }
            return this;
        }

        /**
         * Defines a color palette with shades.
         *
         * <p>Example:</p>
         * <pre>
         * .colorPalette("blue",
         *     hex("#e3f2fd"), hex("#90caf9"), hex("#42a5f5"),
         *     hex("#1976d2"), hex("#0d47a1"))
         * // Creates: --blue-50, --blue-100, ..., --blue-900
         * </pre>
         *
         * @param name the color name
         * @param shades the color shades (typically 5-9 values)
         * @return this builder for chaining
         */
        public DesignSystemBuilder colorPalette(String name, CSSValue... shades) {
            int[] weights = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900};
            for (int i = 0; i < Math.min(shades.length, weights.length); i++) {
                variables.put(prefix + name + "-" + weights[i], shades[i].css());
            }
            return this;
        }

        /**
         * Defines a font size scale.
         * Creates variables: --font-size-1, --font-size-2, etc.
         *
         * @param sizes the font sizes in order (small to large)
         * @return this builder for chaining
         */
        public DesignSystemBuilder fontSize(CSSValue... sizes) {
            for (int i = 0; i < sizes.length; i++) {
                variables.put(prefix + "font-size-" + (i + 1), sizes[i].css());
            }
            return this;
        }

        /**
         * Defines named font sizes.
         *
         * <p>Example:</p>
         * <pre>
         * .fontSizeNamed("xs", rem(0.75), "sm", rem(0.875), "base", rem(1))
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public DesignSystemBuilder fontSizeNamed(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + "font-size-" + pairs[i].toString();
                    String value = pairs[i + 1] instanceof CSSValue
                        ? ((CSSValue) pairs[i + 1]).css()
                        : pairs[i + 1].toString();
                    variables.put(name, value);
                }
            }
            return this;
        }

        /**
         * Defines a border radius scale.
         * Creates variables: --radius-1, --radius-2, etc.
         *
         * @param radii the border radius values
         * @return this builder for chaining
         */
        public DesignSystemBuilder borderRadius(CSSValue... radii) {
            for (int i = 0; i < radii.length; i++) {
                variables.put(prefix + "radius-" + (i + 1), radii[i].css());
            }
            return this;
        }

        /**
         * Defines a shadow scale.
         * Creates variables: --shadow-1, --shadow-2, etc.
         *
         * @param shadows the box-shadow values
         * @return this builder for chaining
         */
        public DesignSystemBuilder shadows(String... shadows) {
            for (int i = 0; i < shadows.length; i++) {
                variables.put(prefix + "shadow-" + (i + 1), shadows[i]);
            }
            return this;
        }

        /**
         * Defines z-index layers.
         *
         * <p>Example:</p>
         * <pre>
         * .zIndex("base", 0, "dropdown", 1000, "modal", 2000, "tooltip", 3000)
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public DesignSystemBuilder zIndex(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + "z-" + pairs[i].toString();
                    variables.put(name, pairs[i + 1].toString());
                }
            }
            return this;
        }

        /**
         * Defines animation duration values.
         *
         * @param durations the duration values
         * @return this builder for chaining
         */
        public DesignSystemBuilder duration(CSSValue... durations) {
            for (int i = 0; i < durations.length; i++) {
                variables.put(prefix + "duration-" + (i + 1), durations[i].css());
            }
            return this;
        }

        /**
         * Defines animation timing functions.
         *
         * <p>Example:</p>
         * <pre>
         * .easing("ease-out", "cubic-bezier(0, 0, 0.2, 1)",
         *         "bounce", "cubic-bezier(0.68, -0.55, 0.265, 1.55)")
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public DesignSystemBuilder easing(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + "easing-" + pairs[i].toString();
                    variables.put(name, pairs[i + 1].toString());
                }
            }
            return this;
        }

        /**
         * Defines breakpoint values.
         *
         * <p>Example:</p>
         * <pre>
         * .breakpoints("sm", px(640), "md", px(768), "lg", px(1024))
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public DesignSystemBuilder breakpoints(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + "breakpoint-" + pairs[i].toString();
                    String value = pairs[i + 1] instanceof CSSValue
                        ? ((CSSValue) pairs[i + 1]).css()
                        : pairs[i + 1].toString();
                    variables.put(name, value);
                }
            }
            return this;
        }

        /**
         * Defines line height values.
         *
         * @param lineHeights the line height values
         * @return this builder for chaining
         */
        public DesignSystemBuilder lineHeight(CSSValue... lineHeights) {
            for (int i = 0; i < lineHeights.length; i++) {
                variables.put(prefix + "line-height-" + (i + 1), lineHeights[i].css());
            }
            return this;
        }

        /**
         * Defines font family variables.
         *
         * <p>Example:</p>
         * <pre>
         * .fontFamily("sans", "system-ui, sans-serif",
         *             "mono", "Consolas, monospace")
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public DesignSystemBuilder fontFamily(Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = prefix + "font-" + pairs[i].toString();
                    variables.put(name, pairs[i + 1].toString());
                }
            }
            return this;
        }

        /**
         * Adds a custom variable.
         *
         * @param name the variable name
         * @param value the variable value
         * @return this builder for chaining
         */
        public DesignSystemBuilder custom(String name, CSSValue value) {
            variables.put(prefix + name, value.css());
            return this;
        }

        /**
         * Adds a custom variable with string value.
         *
         * @param name the variable name
         * @param value the variable value
         * @return this builder for chaining
         */
        public DesignSystemBuilder custom(String name, String value) {
            variables.put(prefix + name, value);
            return this;
        }

        /**
         * Builds the CSS rule string for :root with all defined variables.
         *
         * <p>Output example:</p>
         * <pre>
         * :root {
         *   --spacing-1: 0.25rem;
         *   --spacing-2: 0.5rem;
         *   --primary: blue;
         * }
         * </pre>
         *
         * @return the formatted CSS rule string
         */
        public String build() {
            return buildForSelector(":root");
        }

        /**
         * Builds the CSS rule string for a specific selector.
         *
         * @param selector the CSS selector (e.g., ":root", ".theme-dark")
         * @return the formatted CSS rule string
         */
        public String buildForSelector(String selector) {
            if (variables.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(selector).append(" {\n");
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                sb.append("  --").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(";\n");
            }
            sb.append("}");
            return sb.toString();
        }

        /**
         * Returns all defined variables as a map.
         *
         * @return map of variable names to values
         */
        public Map<String, String> toMap() {
            return Map.copyOf(variables);
        }
    }

    // ==================== Theme Builder ====================

    /**
     * Creates a new theme builder for defining light/dark mode variables.
     *
     * <p>Example:</p>
     * <pre>
     * String themes = theme()
     *     .light("background", white, "text", black)
     *     .dark("background", black, "text", white)
     *     .buildBoth();
     * </pre>
     *
     * @return a new ThemeBuilder instance
     */
    public static ThemeBuilder theme() {
        return new ThemeBuilder();
    }

    /**
     * Builder for creating light/dark theme variable sets.
     */
    public static class ThemeBuilder {
        private final Map<String, String> lightVariables = new LinkedHashMap<>();
        private final Map<String, String> darkVariables = new LinkedHashMap<>();
        private String lightSelector = ":root";
        private String darkSelector = "[data-theme='dark']";

        /**
         * Sets the selector for light theme.
         *
         * @param selector the CSS selector (default: ":root")
         * @return this builder for chaining
         */
        public ThemeBuilder lightSelector(String selector) {
            this.lightSelector = selector;
            return this;
        }

        /**
         * Sets the selector for dark theme.
         *
         * @param selector the CSS selector (default: "[data-theme='dark']")
         * @return this builder for chaining
         */
        public ThemeBuilder darkSelector(String selector) {
            this.darkSelector = selector;
            return this;
        }

        /**
         * Defines light theme variables.
         *
         * <p>Example:</p>
         * <pre>
         * .light("background", white, "text", black, "primary", blue)
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public ThemeBuilder light(Object... pairs) {
            addVariables(lightVariables, pairs);
            return this;
        }

        /**
         * Defines dark theme variables.
         *
         * <p>Example:</p>
         * <pre>
         * .dark("background", black, "text", white, "primary", lightBlue)
         * </pre>
         *
         * @param pairs alternating name-value pairs
         * @return this builder for chaining
         */
        public ThemeBuilder dark(Object... pairs) {
            addVariables(darkVariables, pairs);
            return this;
        }

        private void addVariables(Map<String, String> map, Object... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                if (i + 1 < pairs.length) {
                    String name = pairs[i].toString();
                    String value = pairs[i + 1] instanceof CSSValue
                        ? ((CSSValue) pairs[i + 1]).css()
                        : pairs[i + 1].toString();
                    map.put(name, value);
                }
            }
        }

        /**
         * Builds CSS rules for both light and dark themes.
         *
         * @return the formatted CSS rule string
         */
        public String buildBoth() {
            StringBuilder sb = new StringBuilder();

            if (!lightVariables.isEmpty()) {
                sb.append(buildTheme(lightSelector, lightVariables));
            }

            if (!darkVariables.isEmpty()) {
                if (sb.length() > 0) sb.append("\n\n");
                sb.append(buildTheme(darkSelector, darkVariables));
            }

            return sb.toString();
        }

        /**
         * Builds CSS rule for light theme only.
         *
         * @return the formatted CSS rule string
         */
        public String buildLight() {
            return buildTheme(lightSelector, lightVariables);
        }

        /**
         * Builds CSS rule for dark theme only.
         *
         * @return the formatted CSS rule string
         */
        public String buildDark() {
            return buildTheme(darkSelector, darkVariables);
        }

        private String buildTheme(String selector, Map<String, String> vars) {
            if (vars.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(selector).append(" {\n");
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                sb.append("  --").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(";\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // ==================== Common Variable Patterns ====================

    /**
     * Creates a CSS variable reference for a color with fallback.
     * Convenience method for theming colors.
     *
     * @param colorName the color variable name (e.g., "primary", "background")
     * @param fallback the fallback color
     * @return CSSValue for var(--color-name, fallback)
     */
    public static CSSValue themeColor(String colorName, CSSValue fallback) {
        return var("theme-" + colorName, fallback);
    }

    /**
     * Creates a CSS variable reference for spacing with fallback.
     *
     * @param level the spacing level (1-9 typically)
     * @param fallback the fallback spacing value
     * @return CSSValue for var(--spacing-N, fallback)
     */
    public static CSSValue spacing(int level, CSSValue fallback) {
        return var("spacing-" + level, fallback);
    }

    /**
     * Creates a CSS variable reference for border radius with fallback.
     *
     * @param level the radius level (1-4 typically)
     * @param fallback the fallback radius value
     * @return CSSValue for var(--radius-N, fallback)
     */
    public static CSSValue radius(int level, CSSValue fallback) {
        return var("radius-" + level, fallback);
    }

    /**
     * Creates a CSS variable reference for font size with fallback.
     *
     * @param level the font size level
     * @param fallback the fallback size value
     * @return CSSValue for var(--font-size-N, fallback)
     */
    public static CSSValue fontSizeVar(int level, CSSValue fallback) {
        return var("font-size-" + level, fallback);
    }

    /**
     * Creates a CSS variable reference for shadow with fallback.
     *
     * @param level the shadow level (1-5 typically)
     * @param fallback the fallback shadow value
     * @return CSSValue for var(--shadow-N, fallback)
     */
    public static CSSValue shadow(int level, String fallback) {
        return var("shadow-" + level, fallback);
    }
}
