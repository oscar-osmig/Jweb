package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for @supports CSS feature queries.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.Supports.*;
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 *
 * // Simple property check
 * String css = supports(display, grid)
 *     .rule(".container", style().display(grid))
 *     .build();
 *
 * // Multiple conditions with AND
 * String css = supports()
 *     .property(display, grid)
 *     .and()
 *     .property(gap, px(10))
 *     .rules(
 *         rule(".grid", style().display(grid).gap(px(10)))
 *     )
 *     .build();
 *
 * // NOT condition
 * String css = supports()
 *     .not()
 *     .property(display, grid)
 *     .rule(".fallback", style().display(flex))
 *     .build();
 *
 * // Selector support check
 * String css = supportsSelector(":has(> img)")
 *     .rule(".card:has(> img)", style().padding(zero))
 *     .build();
 * </pre>
 */
public class Supports {

    private final List<String> conditions = new ArrayList<>();
    private final List<MediaQuery.Rule> rules = new ArrayList<>();
    private String currentOperator = null;

    private Supports() {}

    /**
     * Creates a new @supports builder.
     */
    public static Supports supports() {
        return new Supports();
    }

    /**
     * Creates a @supports rule checking for a CSS property.
     *
     * @param property the CSS property
     * @param value the CSS value
     */
    public static Supports supports(CSSValue property, CSSValue value) {
        return new Supports().property(property, value);
    }

    /**
     * Creates a @supports rule checking for a CSS property by name.
     */
    public static Supports supports(String property, String value) {
        return new Supports().property(property, value);
    }

    /**
     * Creates a @supports selector() check.
     */
    public static Supports supportsSelector(String selector) {
        return new Supports().selector(selector);
    }

    /**
     * Adds a property condition.
     *
     * @param property the CSS property
     * @param value the CSS value
     * @return this for chaining
     */
    public Supports property(CSSValue property, CSSValue value) {
        return property(property.css(), value.css());
    }

    /**
     * Adds a property condition by name.
     */
    public Supports property(String property, String value) {
        String condition = "(" + property + ": " + value + ")";
        if (currentOperator != null) {
            conditions.add(currentOperator);
            currentOperator = null;
        }
        conditions.add(condition);
        return this;
    }

    /**
     * Adds a selector() condition (checks if browser supports a selector).
     */
    public Supports selector(String selectorValue) {
        String condition = "selector(" + selectorValue + ")";
        if (currentOperator != null) {
            conditions.add(currentOperator);
            currentOperator = null;
        }
        conditions.add(condition);
        return this;
    }

    /**
     * Adds an AND operator between conditions.
     */
    public Supports and() {
        currentOperator = "and";
        return this;
    }

    /**
     * Adds an OR operator between conditions.
     */
    public Supports or() {
        currentOperator = "or";
        return this;
    }

    /**
     * Adds a NOT operator for the next condition.
     */
    public Supports not() {
        currentOperator = "not";
        return this;
    }

    /**
     * Adds a grouped condition.
     *
     * @param innerSupports a Supports builder for the grouped condition
     */
    public Supports group(Supports innerSupports) {
        String groupCondition = "(" + innerSupports.buildCondition() + ")";
        if (currentOperator != null) {
            conditions.add(currentOperator);
            currentOperator = null;
        }
        conditions.add(groupCondition);
        return this;
    }

    /**
     * Adds a CSS rule to apply when conditions are met.
     */
    public Supports rule(String selector, Style<?> style) {
        rules.add(new MediaQuery.Rule(selector, style));
        return this;
    }

    /**
     * Adds multiple CSS rules.
     */
    public Supports rules(MediaQuery.Rule... ruleArray) {
        for (MediaQuery.Rule r : ruleArray) {
            rules.add(r);
        }
        return this;
    }

    /**
     * Builds just the condition part (without @supports wrapper).
     */
    public String buildCondition() {
        return String.join(" ", conditions);
    }

    /**
     * Builds the complete @supports rule.
     *
     * @return CSS string
     */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@supports ").append(buildCondition()).append(" {\n");
        for (MediaQuery.Rule r : rules) {
            sb.append("  ").append(r.selector()).append(" { ").append(r.style().build()).append(" }\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    // ==================== Convenience Methods ====================

    /**
     * Check for display: flex support.
     */
    public static Supports supportsFlexbox() {
        return supports("display", "flex");
    }

    /**
     * Check for display: grid support.
     */
    public static Supports supportsGrid() {
        return supports("display", "grid");
    }

    /**
     * Check for CSS custom properties (variables) support.
     */
    public static Supports supportsCustomProperties() {
        return supports("--test", "value");
    }

    /**
     * Check for backdrop-filter support.
     */
    public static Supports supportsBackdropFilter() {
        return supports("backdrop-filter", "blur(1px)");
    }

    /**
     * Check for :has() selector support.
     */
    public static Supports supportsHasSelector() {
        return supportsSelector(":has(*)");
    }

    /**
     * Check for container queries support.
     */
    public static Supports supportsContainerQueries() {
        return supports("container-type", "inline-size");
    }

    /**
     * Check for aspect-ratio support.
     */
    public static Supports supportsAspectRatio() {
        return supports("aspect-ratio", "1/1");
    }

    /**
     * Check for CSS subgrid support.
     */
    public static Supports supportsSubgrid() {
        return supports("grid-template-columns", "subgrid");
    }

    /**
     * Check for CSS gap property support (in flexbox).
     */
    public static Supports supportsFlexGap() {
        return supports().property("display", "flex").and().property("gap", "1rem");
    }

    /**
     * Check for CSS color-mix() function support.
     */
    public static Supports supportsColorMix() {
        return supports("color", "color-mix(in srgb, red 50%, blue)");
    }

    /**
     * Check for CSS :focus-visible selector support.
     */
    public static Supports supportsFocusVisible() {
        return supportsSelector(":focus-visible");
    }

    /**
     * Check for CSS scroll-snap support.
     */
    public static Supports supportsScrollSnap() {
        return supports("scroll-snap-type", "x mandatory");
    }

    /**
     * Check for CSS position: sticky support.
     */
    public static Supports supportsSticky() {
        return supports("position", "sticky");
    }

    /**
     * Check for CSS clamp() function support.
     */
    public static Supports supportsClamp() {
        return supports("font-size", "clamp(1rem, 2vw, 3rem)");
    }

    /**
     * Check for CSS :where() selector support.
     */
    public static Supports supportsWhereSelector() {
        return supportsSelector(":where(*)");
    }

    /**
     * Check for CSS :is() selector support.
     */
    public static Supports supportsIsSelector() {
        return supportsSelector(":is(*)");
    }

    /**
     * Check for CSS logical properties support.
     */
    public static Supports supportsLogicalProperties() {
        return supports("margin-inline-start", "1rem");
    }

    /**
     * Helper to create a rule using the same pattern as MediaQuery.
     */
    public static MediaQuery.Rule makeRule(String selector, Style<?> style) {
        return new MediaQuery.Rule(selector, style);
    }
}
