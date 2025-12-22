package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Design token system with dark mode support.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.Theme.*;
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 *
 * // Create a theme
 * Theme theme = Theme.create()
 *     .color("primary", "#4F46E5")
 *     .color("primary-hover", "#4338CA")
 *     .color("background", "#ffffff")
 *     .color("text", "#18181b")
 *     .spacing("sm", "0.5rem")
 *     .spacing("md", "1rem")
 *     .spacing("lg", "2rem")
 *     .radius("sm", "0.25rem")
 *     .radius("md", "0.5rem")
 *     .shadow("sm", "0 1px 2px rgba(0,0,0,0.05)")
 *     .dark()
 *         .color("background", "#18181b")
 *         .color("text", "#fafafa")
 *     .build();
 *
 * // Use theme tokens in styles
 * div().style(style()
 *     .backgroundColor(theme.color("background"))
 *     .padding(theme.spacing("md"))
 *     .borderRadius(theme.radius("sm"))
 * )
 *
 * // Generate CSS variables
 * String css = theme.toCss();
 * // :root { --color-primary: #4F46E5; ... }
 * // @media (prefers-color-scheme: dark) { :root { --color-background: #18181b; ... } }
 * </pre>
 */
public class Theme {

    private final Map<String, String> colors = new LinkedHashMap<>();
    private final Map<String, String> spacing = new LinkedHashMap<>();
    private final Map<String, String> radii = new LinkedHashMap<>();
    private final Map<String, String> shadows = new LinkedHashMap<>();
    private final Map<String, String> fontSizes = new LinkedHashMap<>();
    private final Map<String, String> fontWeights = new LinkedHashMap<>();
    private final Map<String, String> lineHeights = new LinkedHashMap<>();
    private final Map<String, String> breakpoints = new LinkedHashMap<>();
    private final Map<String, String> transitions = new LinkedHashMap<>();
    private final Map<String, String> zIndices = new LinkedHashMap<>();
    private final Map<String, String> custom = new LinkedHashMap<>();

    // Dark mode overrides
    private final Map<String, String> darkColors = new LinkedHashMap<>();
    private final Map<String, String> darkCustom = new LinkedHashMap<>();

    private String name = "default";

    private Theme() {}

    // ==================== Factory ====================

    /**
     * Creates a new theme builder.
     */
    public static Theme create() {
        return new Theme();
    }

    /**
     * Creates a new theme builder with a name.
     */
    public static Theme create(String name) {
        Theme theme = new Theme();
        theme.name = name;
        return theme;
    }

    /**
     * Creates a preset theme with common defaults.
     */
    public static Theme preset() {
        return create()
            // Colors - Neutral palette
            .color("white", "#ffffff")
            .color("black", "#000000")
            .color("gray-50", "#fafafa")
            .color("gray-100", "#f4f4f5")
            .color("gray-200", "#e4e4e7")
            .color("gray-300", "#d4d4d8")
            .color("gray-400", "#a1a1aa")
            .color("gray-500", "#71717a")
            .color("gray-600", "#52525b")
            .color("gray-700", "#3f3f46")
            .color("gray-800", "#27272a")
            .color("gray-900", "#18181b")
            .color("gray-950", "#09090b")

            // Primary color
            .color("primary-50", "#eef2ff")
            .color("primary-100", "#e0e7ff")
            .color("primary-200", "#c7d2fe")
            .color("primary-300", "#a5b4fc")
            .color("primary-400", "#818cf8")
            .color("primary-500", "#6366f1")
            .color("primary-600", "#4f46e5")
            .color("primary-700", "#4338ca")
            .color("primary-800", "#3730a3")
            .color("primary-900", "#312e81")

            // Semantic colors
            .color("background", "#ffffff")
            .color("foreground", "#18181b")
            .color("muted", "#f4f4f5")
            .color("muted-foreground", "#71717a")
            .color("border", "#e4e4e7")
            .color("ring", "#4f46e5")

            .color("success", "#22c55e")
            .color("warning", "#f59e0b")
            .color("error", "#ef4444")
            .color("info", "#3b82f6")

            // Spacing scale
            .spacing("0", "0")
            .spacing("px", "1px")
            .spacing("0.5", "0.125rem")
            .spacing("1", "0.25rem")
            .spacing("1.5", "0.375rem")
            .spacing("2", "0.5rem")
            .spacing("2.5", "0.625rem")
            .spacing("3", "0.75rem")
            .spacing("3.5", "0.875rem")
            .spacing("4", "1rem")
            .spacing("5", "1.25rem")
            .spacing("6", "1.5rem")
            .spacing("7", "1.75rem")
            .spacing("8", "2rem")
            .spacing("9", "2.25rem")
            .spacing("10", "2.5rem")
            .spacing("11", "2.75rem")
            .spacing("12", "3rem")
            .spacing("14", "3.5rem")
            .spacing("16", "4rem")
            .spacing("20", "5rem")
            .spacing("24", "6rem")
            .spacing("28", "7rem")
            .spacing("32", "8rem")
            .spacing("36", "9rem")
            .spacing("40", "10rem")
            .spacing("44", "11rem")
            .spacing("48", "12rem")
            .spacing("52", "13rem")
            .spacing("56", "14rem")
            .spacing("60", "15rem")
            .spacing("64", "16rem")
            .spacing("72", "18rem")
            .spacing("80", "20rem")
            .spacing("96", "24rem")

            // Border radius
            .radius("none", "0")
            .radius("sm", "0.125rem")
            .radius("DEFAULT", "0.25rem")
            .radius("md", "0.375rem")
            .radius("lg", "0.5rem")
            .radius("xl", "0.75rem")
            .radius("2xl", "1rem")
            .radius("3xl", "1.5rem")
            .radius("full", "9999px")

            // Shadows
            .shadow("sm", "0 1px 2px 0 rgb(0 0 0 / 0.05)")
            .shadow("DEFAULT", "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)")
            .shadow("md", "0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)")
            .shadow("lg", "0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)")
            .shadow("xl", "0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)")
            .shadow("2xl", "0 25px 50px -12px rgb(0 0 0 / 0.25)")
            .shadow("inner", "inset 0 2px 4px 0 rgb(0 0 0 / 0.05)")
            .shadow("none", "0 0 #0000")

            // Font sizes
            .fontSize("xs", "0.75rem")
            .fontSize("sm", "0.875rem")
            .fontSize("base", "1rem")
            .fontSize("lg", "1.125rem")
            .fontSize("xl", "1.25rem")
            .fontSize("2xl", "1.5rem")
            .fontSize("3xl", "1.875rem")
            .fontSize("4xl", "2.25rem")
            .fontSize("5xl", "3rem")
            .fontSize("6xl", "3.75rem")
            .fontSize("7xl", "4.5rem")
            .fontSize("8xl", "6rem")
            .fontSize("9xl", "8rem")

            // Font weights
            .fontWeight("thin", "100")
            .fontWeight("extralight", "200")
            .fontWeight("light", "300")
            .fontWeight("normal", "400")
            .fontWeight("medium", "500")
            .fontWeight("semibold", "600")
            .fontWeight("bold", "700")
            .fontWeight("extrabold", "800")
            .fontWeight("black", "900")

            // Line heights
            .lineHeight("none", "1")
            .lineHeight("tight", "1.25")
            .lineHeight("snug", "1.375")
            .lineHeight("normal", "1.5")
            .lineHeight("relaxed", "1.625")
            .lineHeight("loose", "2")

            // Breakpoints
            .breakpoint("sm", "640px")
            .breakpoint("md", "768px")
            .breakpoint("lg", "1024px")
            .breakpoint("xl", "1280px")
            .breakpoint("2xl", "1536px")

            // Transitions
            .transition("none", "none")
            .transition("all", "all 150ms cubic-bezier(0.4, 0, 0.2, 1)")
            .transition("DEFAULT", "color, background-color, border-color, text-decoration-color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter 150ms cubic-bezier(0.4, 0, 0.2, 1)")
            .transition("colors", "color, background-color, border-color, text-decoration-color, fill, stroke 150ms cubic-bezier(0.4, 0, 0.2, 1)")
            .transition("opacity", "opacity 150ms cubic-bezier(0.4, 0, 0.2, 1)")
            .transition("shadow", "box-shadow 150ms cubic-bezier(0.4, 0, 0.2, 1)")
            .transition("transform", "transform 150ms cubic-bezier(0.4, 0, 0.2, 1)")

            // Z-index scale
            .zIndex("0", "0")
            .zIndex("10", "10")
            .zIndex("20", "20")
            .zIndex("30", "30")
            .zIndex("40", "40")
            .zIndex("50", "50")
            .zIndex("auto", "auto")

            // Dark mode defaults
            .dark()
                .color("background", "#09090b")
                .color("foreground", "#fafafa")
                .color("muted", "#27272a")
                .color("muted-foreground", "#a1a1aa")
                .color("border", "#27272a")
            .build();
    }

    // ==================== Token Setters ====================

    public Theme color(String name, String value) {
        colors.put(name, value);
        return this;
    }

    public Theme spacing(String name, String value) {
        spacing.put(name, value);
        return this;
    }

    public Theme radius(String name, String value) {
        radii.put(name, value);
        return this;
    }

    public Theme shadow(String name, String value) {
        shadows.put(name, value);
        return this;
    }

    public Theme fontSize(String name, String value) {
        fontSizes.put(name, value);
        return this;
    }

    public Theme fontWeight(String name, String value) {
        fontWeights.put(name, value);
        return this;
    }

    public Theme lineHeight(String name, String value) {
        lineHeights.put(name, value);
        return this;
    }

    public Theme breakpoint(String name, String value) {
        breakpoints.put(name, value);
        return this;
    }

    public Theme transition(String name, String value) {
        transitions.put(name, value);
        return this;
    }

    public Theme zIndex(String name, String value) {
        zIndices.put(name, value);
        return this;
    }

    public Theme custom(String name, String value) {
        custom.put(name, value);
        return this;
    }

    // ==================== Dark Mode ====================

    /**
     * Starts defining dark mode overrides.
     * Chain .color(), .custom() etc. to define dark mode values.
     * Call .build() to return to the main theme.
     */
    public DarkModeBuilder dark() {
        return new DarkModeBuilder(this);
    }

    public static class DarkModeBuilder {
        private final Theme theme;

        DarkModeBuilder(Theme theme) {
            this.theme = theme;
        }

        public DarkModeBuilder color(String name, String value) {
            theme.darkColors.put(name, value);
            return this;
        }

        public DarkModeBuilder custom(String name, String value) {
            theme.darkCustom.put(name, value);
            return this;
        }

        public Theme build() {
            return theme;
        }
    }

    // ==================== Token Getters (CSSValue) ====================

    /**
     * Gets a color token as a CSS variable reference.
     * @param name the color name
     * @return CSSValue for use in style methods: var(--color-name)
     */
    public CSSValue color(String name) {
        return () -> "var(--color-" + name + ")";
    }

    /**
     * Gets a spacing token as a CSS variable reference.
     */
    public CSSValue spacing(String name) {
        return () -> "var(--spacing-" + name + ")";
    }

    /**
     * Gets a radius token as a CSS variable reference.
     */
    public CSSValue radius(String name) {
        return () -> "var(--radius-" + name + ")";
    }

    /**
     * Gets a shadow token as a CSS variable reference.
     */
    public CSSValue shadow(String name) {
        return () -> "var(--shadow-" + name + ")";
    }

    /**
     * Gets a font-size token as a CSS variable reference.
     */
    public CSSValue fontSize(String name) {
        return () -> "var(--font-size-" + name + ")";
    }

    /**
     * Gets a font-weight token as a CSS variable reference.
     */
    public CSSValue fontWeight(String name) {
        return () -> "var(--font-weight-" + name + ")";
    }

    /**
     * Gets a line-height token as a CSS variable reference.
     */
    public CSSValue lineHeight(String name) {
        return () -> "var(--line-height-" + name + ")";
    }

    /**
     * Gets a breakpoint value (raw, for media queries).
     */
    public String breakpoint(String name) {
        return breakpoints.getOrDefault(name, "0");
    }

    /**
     * Gets a transition token as a CSS variable reference.
     */
    public CSSValue transition(String name) {
        return () -> "var(--transition-" + name + ")";
    }

    /**
     * Gets a z-index token as a CSS variable reference.
     */
    public CSSValue zIndex(String name) {
        return () -> "var(--z-" + name + ")";
    }

    /**
     * Gets a custom token as a CSS variable reference.
     */
    public CSSValue token(String name) {
        return () -> "var(--" + name + ")";
    }

    // ==================== CSS Generation ====================

    /**
     * Generates CSS with all theme variables.
     * Includes :root block and dark mode media query.
     */
    public String toCss() {
        StringBuilder sb = new StringBuilder();

        // Root variables
        sb.append(":root {\n");

        // Colors
        for (Map.Entry<String, String> e : colors.entrySet()) {
            sb.append("  --color-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Spacing
        for (Map.Entry<String, String> e : spacing.entrySet()) {
            sb.append("  --spacing-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Radii
        for (Map.Entry<String, String> e : radii.entrySet()) {
            sb.append("  --radius-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Shadows
        for (Map.Entry<String, String> e : shadows.entrySet()) {
            sb.append("  --shadow-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Font sizes
        for (Map.Entry<String, String> e : fontSizes.entrySet()) {
            sb.append("  --font-size-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Font weights
        for (Map.Entry<String, String> e : fontWeights.entrySet()) {
            sb.append("  --font-weight-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Line heights
        for (Map.Entry<String, String> e : lineHeights.entrySet()) {
            sb.append("  --line-height-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Transitions
        for (Map.Entry<String, String> e : transitions.entrySet()) {
            sb.append("  --transition-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Z-indices
        for (Map.Entry<String, String> e : zIndices.entrySet()) {
            sb.append("  --z-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        // Custom
        for (Map.Entry<String, String> e : custom.entrySet()) {
            sb.append("  --").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
        }

        sb.append("}\n");

        // Dark mode overrides
        if (!darkColors.isEmpty() || !darkCustom.isEmpty()) {
            sb.append("\n@media (prefers-color-scheme: dark) {\n");
            sb.append("  :root {\n");

            for (Map.Entry<String, String> e : darkColors.entrySet()) {
                sb.append("    --color-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
            }

            for (Map.Entry<String, String> e : darkCustom.entrySet()) {
                sb.append("    --").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
            }

            sb.append("  }\n");
            sb.append("}\n");
        }

        return sb.toString();
    }

    /**
     * Generates CSS for a dark mode toggle class.
     * Use with JavaScript to toggle dark mode manually.
     *
     * @param className the class name to use (e.g., "dark")
     */
    public String toDarkModeClassCss(String className) {
        StringBuilder sb = new StringBuilder();

        if (!darkColors.isEmpty() || !darkCustom.isEmpty()) {
            sb.append(".").append(className).append(" {\n");

            for (Map.Entry<String, String> e : darkColors.entrySet()) {
                sb.append("  --color-").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
            }

            for (Map.Entry<String, String> e : darkCustom.entrySet()) {
                sb.append("  --").append(e.getKey()).append(": ").append(e.getValue()).append(";\n");
            }

            sb.append("}\n");
        }

        return sb.toString();
    }

    /**
     * Generates complete CSS including base styles and dark mode class.
     */
    public String toFullCss() {
        return toCss() + "\n" + toDarkModeClassCss("dark");
    }

    /**
     * Creates a style element with the theme CSS.
     */
    public com.osmig.Jweb.framework.core.Element toStyleElement() {
        return com.osmig.Jweb.framework.elements.Elements.style(toFullCss());
    }

    // ==================== Utilities ====================

    /**
     * Gets the theme name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets raw color value (not as CSS variable).
     */
    public String getColorValue(String name) {
        return colors.get(name);
    }

    /**
     * Gets raw spacing value.
     */
    public String getSpacingValue(String name) {
        return spacing.get(name);
    }

    /**
     * Checks if a color token exists.
     */
    public boolean hasColor(String name) {
        return colors.containsKey(name);
    }

    /**
     * Gets all color names.
     */
    public java.util.Set<String> getColorNames() {
        return colors.keySet();
    }

    @Override
    public String toString() {
        return toCss();
    }
}
