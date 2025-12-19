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
}
