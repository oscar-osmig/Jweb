package jweb.css;

import jweb.CSSValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A design system's tokens as CSS custom properties.
 *
 * <p>A theme is written once, emitted through a page's stylesheet, and read
 * everywhere as {@link CSSValue}s — so the value lives in exactly one place and
 * a dark-mode swap is a value change, not a second set of rules.</p>
 *
 * <pre>{@code
 * import jweb.css.Theme;
 * import static jweb.Css.*;
 *
 * public static final Theme TOKENS = Theme.light()
 *     .color("primary", hex("#4f46e5"))
 *     .color("text",    hex("#1e293b"))
 *     .color("bg",      hex("#ffffff"))
 *     .space("4",       rem(1))
 *     .radius("md",     px(6))
 *     .dark(Theme.dark()
 *         .color("text", hex("#e2e8f0"))
 *         .color("bg",   hex("#0f172a")));
 *
 * // read a token anywhere
 * style().color(Theme.color("text")).padding(Theme.space("4"))
 *
 * // emit it from the layout
 * public Stylesheet styles() { return stylesheet().add(TOKENS); }
 * }</pre>
 *
 * <p>{@link #css()} emits three things: the light values on {@code :root}, the
 * dark values under {@code prefers-color-scheme: dark} (guarded so an explicit
 * light choice still wins), and the same dark values under
 * {@code [data-theme=dark]} so a toggle can override the system setting in
 * both directions.</p>
 */
public final class Theme {

    /** Custom property name (without {@code --}) to value, in declaration order. */
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private Theme darkTokens;

    private Theme() {}

    // ==================== Building ====================

    /** Starts a theme — the values that apply by default. */
    public static Theme light() { return new Theme(); }

    /**
     * Starts a set of dark-scheme overrides, to be handed to
     * {@link #dark(Theme)}. Only the tokens that actually change need to be
     * listed.
     *
     * @return an empty theme
     */
    public static Theme dark() { return new Theme(); }

    /** Starts a theme with no light/dark connotation. */
    public static Theme tokens() { return new Theme(); }

    /**
     * Attaches the dark-scheme overrides.
     *
     * @param overrides the tokens that change in dark mode
     * @return this for chaining
     */
    public Theme dark(Theme overrides) {
        this.darkTokens = overrides;
        return this;
    }

    /** Defines {@code --color-<name>}. */
    public Theme color(String name, CSSValue value) { return token("color-" + name, value); }
    /** Defines {@code --space-<name>}. */
    public Theme space(String name, CSSValue value) { return token("space-" + name, value); }
    /** Defines {@code --radius-<name>}. */
    public Theme radius(String name, CSSValue value) { return token("radius-" + name, value); }
    /** Defines {@code --font-<name>}. */
    public Theme font(String name, CSSValue value) { return token("font-" + name, value); }
    /** Defines {@code --text-<name>} (a font size on the type scale). */
    public Theme text(String name, CSSValue value) { return token("text-" + name, value); }
    /** Defines {@code --shadow-<name>}. */
    public Theme shadow(String name, CSSValue value) { return token("shadow-" + name, value); }
    /** Defines {@code --gradient-<name>}. */
    public Theme gradient(String name, CSSValue value) { return token("gradient-" + name, value); }

    /**
     * Defines a custom property under its exact name — the escape hatch for a
     * token that belongs to none of the named groups.
     *
     * @param name the property name, with or without the leading {@code --}
     * @param value the value
     * @return this for chaining
     */
    public Theme token(String name, CSSValue value) {
        tokens.put(strip(name), value.css());
        return this;
    }

    /**
     * Defines a custom property with a plain string value.
     *
     * @param name the property name, with or without the leading {@code --}
     * @param value the value
     * @return this for chaining
     */
    public Theme token(String name, String value) {
        tokens.put(strip(name), value);
        return this;
    }

    // ==================== Reading ====================

    /** Reads {@code --color-<name>} as {@code var(--color-<name>)}. */
    public static CSSValue color(String name) { return var("color-" + name); }
    /** Reads {@code --space-<name>}. */
    public static CSSValue space(String name) { return var("space-" + name); }
    /** Reads {@code --radius-<name>}. */
    public static CSSValue radius(String name) { return var("radius-" + name); }
    /** Reads {@code --font-<name>}. */
    public static CSSValue font(String name) { return var("font-" + name); }
    /** Reads {@code --text-<name>}. */
    public static CSSValue text(String name) { return var("text-" + name); }
    /** Reads {@code --shadow-<name>}. */
    public static CSSValue shadow(String name) { return var("shadow-" + name); }
    /** Reads {@code --gradient-<name>}. */
    public static CSSValue gradient(String name) { return var("gradient-" + name); }

    /**
     * Reads any custom property as {@code var(--name)}.
     *
     * @param name the property name, with or without the leading {@code --}
     * @return the {@code var()} reference
     */
    public static CSSValue var(String name) {
        String property = "--" + strip(name);
        return () -> "var(" + property + ")";
    }

    /**
     * Reads a custom property with a fallback for when it is not defined.
     *
     * @param name the property name
     * @param fallback the value to use when the property is missing
     * @return the {@code var()} reference
     */
    public static CSSValue var(String name, CSSValue fallback) {
        String property = "--" + strip(name);
        return () -> "var(" + property + ", " + fallback.css() + ")";
    }

    /**
     * The literal value a token was defined with — for the rare place that
     * needs the colour itself rather than a reference to it (a {@code <meta
     * name="theme-color">}, say).
     *
     * @param name the token name
     * @return the value, or null when this theme does not define it
     */
    public String valueOf(String name) {
        return tokens.get(strip(name));
    }

    // ==================== Emitting ====================

    /**
     * The theme as CSS: the {@code :root} block, then the dark overrides under
     * {@code prefers-color-scheme} and under {@code [data-theme=dark]}.
     *
     * @return the CSS text
     */
    public String css() {
        StringBuilder sb = new StringBuilder();
        if (!tokens.isEmpty()) sb.append(":root{").append(declarations(tokens)).append("}");
        if (darkTokens != null && !darkTokens.tokens.isEmpty()) {
            String dark = declarations(darkTokens.tokens);
            sb.append("@media (prefers-color-scheme: dark){:root:not([data-theme=light]){")
              .append(dark).append("}}");
            sb.append(":root[data-theme=dark]{").append(dark).append("}");
        }
        return sb.toString();
    }

    /** The theme's light values only, as a {@code :root} block. */
    public String rootCss() {
        return tokens.isEmpty() ? "" : ":root{" + declarations(tokens) + "}";
    }

    private static String declarations(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((name, value) -> sb.append("--").append(name).append(":").append(value).append(";"));
        return sb.toString();
    }

    private static String strip(String name) {
        return name.startsWith("--") ? name.substring(2) : name;
    }

    @Override
    public String toString() {
        return css();
    }
}
