package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating global stylesheets.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.Stylesheet.*;
 *
 * String css = stylesheet()
 *     .rule("*", style().boxSizing(borderBox))
 *     .rule("body", style().margin(zero).fontFamily("system-ui"))
 *     .rule(".container", style().maxWidth(px(1200)).margin(zero, auto))
 *     .rule("a", style().color(PRIMARY).textDecoration(none))
 *     .rule("a:hover", style().textDecoration(underline))
 *     .mediaQuery(mobile(),
 *         rule(".container", style().padding(SPACE_SM)))
 *     .build();
 * </pre>
 */
public class Stylesheet {

    private final List<String> rules = new ArrayList<>();

    private Stylesheet() {}

    /**
     * Creates a new stylesheet builder.
     */
    public static Stylesheet stylesheet() {
        return new Stylesheet();
    }

    /**
     * Creates a CSS rule (selector + style).
     */
    public static Rule cssRule(String selector, Style<?> style) {
        return new Rule(selector, style);
    }

    /**
     * Adds a CSS rule.
     *
     * @param selector the CSS selector (e.g., "body", ".class", "#id", "h1, h2, h3")
     * @param style the styles to apply
     * @return this for chaining
     */
    public Stylesheet rule(String selector, Style<?> style) {
        rules.add(selector + " { " + style.build() + " }");
        return this;
    }

    /**
     * Adds a CSS rule using a Rule object.
     */
    public Stylesheet rule(Rule rule) {
        rules.add(rule.build());
        return this;
    }

    /**
     * Adds multiple rules.
     */
    public Stylesheet rules(Rule... ruleArray) {
        for (Rule r : ruleArray) {
            rules.add(r.build());
        }
        return this;
    }

    /**
     * Adds a media query with nested rules.
     *
     * @param mediaQuery the media query builder
     * @param nestedRules rules to include in the media query
     * @return this for chaining
     */
    public Stylesheet mediaQuery(MediaQuery mediaQuery, Rule... nestedRules) {
        for (Rule r : nestedRules) {
            mediaQuery.rule(r.selector, r.style);
        }
        rules.add(mediaQuery.build());
        return this;
    }

    /**
     * Adds a pre-built media query string.
     */
    public Stylesheet mediaQuery(String mediaQueryCss) {
        rules.add(mediaQueryCss);
        return this;
    }

    /**
     * Adds raw CSS.
     *
     * @param css raw CSS string
     * @return this for chaining
     */
    public Stylesheet raw(String css) {
        rules.add(css);
        return this;
    }

    /**
     * Adds a CSS comment.
     */
    public Stylesheet comment(String comment) {
        rules.add("/* " + comment + " */");
        return this;
    }

    /**
     * Adds a CSS variable definition at :root level.
     */
    public Stylesheet variable(String name, CSSValue value) {
        return variable(name, value.css());
    }

    /**
     * Adds a CSS variable definition at :root level.
     */
    public Stylesheet variable(String name, String value) {
        String varName = name.startsWith("--") ? name : "--" + name;
        rules.add(":root { " + varName + ": " + value + "; }");
        return this;
    }

    /**
     * Adds multiple CSS variables at :root level.
     */
    public Stylesheet variables(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Variables must be provided in name-value pairs");
        }
        StringBuilder sb = new StringBuilder(":root { ");
        for (int i = 0; i < pairs.length; i += 2) {
            String name = pairs[i].startsWith("--") ? pairs[i] : "--" + pairs[i];
            sb.append(name).append(": ").append(pairs[i + 1]).append("; ");
        }
        sb.append("}");
        rules.add(sb.toString());
        return this;
    }

    /**
     * Adds keyframe animations.
     */
    public Stylesheet keyframes(Keyframes keyframes) {
        rules.add(keyframes.build());
        return this;
    }

    /**
     * Adds a font-face declaration.
     */
    public Stylesheet fontFace(FontFace fontFace) {
        rules.add(fontFace.build());
        return this;
    }

    /**
     * Adds a @supports rule.
     */
    public Stylesheet supports(Supports supports) {
        rules.add(supports.build());
        return this;
    }

    /**
     * Builds the complete stylesheet.
     *
     * @return CSS string
     */
    public String build() {
        return String.join("\n", rules);
    }

    /**
     * Builds the stylesheet wrapped in a style tag.
     */
    public String toStyleTag() {
        return "<style>\n" + build() + "\n</style>";
    }

    @Override
    public String toString() {
        return build();
    }

    /**
     * Represents a single CSS rule (selector + styles).
     */
    public static class Rule {
        private final String selector;
        private final Style<?> style;

        public Rule(String selector, Style<?> style) {
            this.selector = selector;
            this.style = style;
        }

        public String build() {
            return selector + " { " + style.build() + " }";
        }
    }
}
