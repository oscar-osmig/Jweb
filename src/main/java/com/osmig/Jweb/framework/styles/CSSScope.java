package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * CSS @scope rule support for scoping styles to specific DOM subtrees.
 *
 * <p>The @scope rule allows you to scope CSS rules to a specific element subtree,
 * optionally with a lower boundary that limits how deep the styles apply.</p>
 *
 * <h2>Basic Scope</h2>
 * <pre>{@code
 * CSSScope.scope(".card")
 *     .rule(rule("h2").fontSize(rem(1.5)))
 *     .rule(rule("p").color(gray))
 *     .build()
 *
 * // Output:
 * // @scope (.card) {
 * //   h2 { font-size: 1.5rem; }
 * //   p { color: gray; }
 * // }
 * }</pre>
 *
 * <h2>Scope with Lower Boundary</h2>
 * <pre>{@code
 * CSSScope.scope(".article").to(".aside")
 *     .rule(rule("p").lineHeight(1.6))
 *     .build()
 *
 * // Output:
 * // @scope (.article) to (.aside) {
 * //   p { line-height: 1.6; }
 * // }
 * // Styles apply to <p> within .article but not within .aside
 * }</pre>
 *
 * <h2>Multiple Root Selectors</h2>
 * <pre>{@code
 * CSSScope.scope(".card, .panel")
 *     .rule(rule("& > header").fontWeight(bold))
 *     .build()
 * }</pre>
 *
 * <h2>Scoping Reference (&)</h2>
 * <pre>{@code
 * CSSScope.scope(".tabs")
 *     .rule(rule("& > button").padding(rem(0.5)))
 *     .rule(rule("& > button:hover").backgroundColor(lightGray))
 *     .build()
 * }</pre>
 *
 * @see CSS for creating style rules
 * @see CSSLayer for cascade layers
 */
public final class CSSScope {

    private CSSScope() {}

    /**
     * Creates a new @scope rule with the given root selector.
     *
     * <p>Example:</p>
     * <pre>
     * CSSScope.scope(".card")
     *     .rule(rule("h2").fontSize(rem(1.5)))
     *     .build()
     * </pre>
     *
     * @param root the root selector for the scope
     * @return a ScopeBuilder instance
     */
    public static ScopeBuilder scope(String root) {
        return new ScopeBuilder(root, null);
    }

    /**
     * Fluent builder for constructing @scope rules.
     */
    public static class ScopeBuilder {
        private final String root;
        private String lowerBoundary;
        private final List<CSS.StyleBuilder> rules = new ArrayList<>();

        ScopeBuilder(String root, String lowerBoundary) {
            this.root = root;
            this.lowerBoundary = lowerBoundary;
        }

        /**
         * Sets the lower boundary (exclusion) selector for this scope.
         * Styles won't apply to elements within this boundary.
         *
         * <p>Example:</p>
         * <pre>
         * scope(".article").to(".sidebar")
         *     .rule(rule("p").color(black))
         *     .build()
         * // Styles apply to p in .article but not in nested .sidebar
         * </pre>
         *
         * @param selector the lower boundary selector
         * @return this builder
         */
        public ScopeBuilder to(String selector) {
            this.lowerBoundary = selector;
            return this;
        }

        /**
         * Adds a CSS rule to this scope.
         *
         * <p>Example:</p>
         * <pre>
         * .rule(rule("h2").fontSize(rem(1.5)))
         * </pre>
         *
         * @param rule the StyleBuilder rule
         * @return this builder
         */
        public ScopeBuilder rule(CSS.StyleBuilder rule) {
            rules.add(rule);
            return this;
        }

        /**
         * Adds multiple CSS rules to this scope.
         *
         * @param ruleArray the StyleBuilder rules
         * @return this builder
         */
        public ScopeBuilder rules(CSS.StyleBuilder... ruleArray) {
            for (CSS.StyleBuilder rule : ruleArray) {
                rules.add(rule);
            }
            return this;
        }

        /**
         * Adds raw CSS to this scope.
         *
         * @param css the raw CSS string
         * @return this builder
         */
        public ScopeBuilder css(String css) {
            // Create a wrapper to hold the raw CSS
            rules.add(new CSS.StyleBuilder(null) {
                @Override
                public String toRule() {
                    return css;
                }
            });
            return this;
        }

        /**
         * Builds the complete @scope block.
         *
         * @return the CSS string
         */
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("@scope (").append(root).append(")");

            if (lowerBoundary != null) {
                sb.append(" to (").append(lowerBoundary).append(")");
            }

            sb.append(" {\n");

            for (CSS.StyleBuilder rule : rules) {
                String ruleStr = rule.toRule();
                // Indent each line
                for (String line : ruleStr.split("\n")) {
                    if (!line.trim().isEmpty()) {
                        sb.append("  ").append(line).append("\n");
                    }
                }
            }

            sb.append("}");
            return sb.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }

    /**
     * Creates a scope with implicit root (using :scope selector).
     *
     * <p>Example:</p>
     * <pre>
     * CSSScope.implicit()
     *     .rule(rule(":scope > div").padding(rem(1)))
     *     .build()
     * </pre>
     *
     * @return a ScopeBuilder with implicit scope
     */
    public static ScopeBuilder implicit() {
        return new ScopeBuilder(":scope", null);
    }

    /**
     * Combines multiple scope definitions into a single stylesheet.
     *
     * @param scopes the scope definitions
     * @return the combined CSS string
     */
    public static String stylesheet(ScopeBuilder... scopes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(scopes[i].build());
        }
        return sb.toString();
    }
}
