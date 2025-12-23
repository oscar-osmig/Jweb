package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * CSS Nesting support for writing hierarchical CSS rules.
 *
 * <p>CSS Nesting allows you to nest selectors inside other selectors,
 * similar to SASS/SCSS but using native CSS syntax. This provides cleaner,
 * more organized stylesheets.</p>
 *
 * <h2>Basic Nesting</h2>
 * <pre>{@code
 * CSSNested.rule(".card")
 *     .prop("padding", "1rem")
 *     .prop("border", "1px solid gray")
 *     .nest(".card-header")
 *         .prop("font-weight", "bold")
 *         .prop("border-bottom", "1px solid gray")
 *     .parent()
 *     .nest(".card-body")
 *         .prop("padding", "0.5rem")
 *     .parent()
 *     .build()
 *
 * // Output:
 * // .card {
 * //   padding: 1rem;
 * //   border: 1px solid gray;
 * //
 * //   .card-header {
 * //     font-weight: bold;
 * //     border-bottom: 1px solid gray;
 * //   }
 * //
 * //   .card-body {
 * //     padding: 0.5rem;
 * //   }
 * // }
 * }</pre>
 *
 * <h2>Nesting with &amp; (Parent Selector)</h2>
 * <pre>{@code
 * CSSNested.rule(".btn")
 *     .prop("padding", "0.5rem 1rem")
 *     .nest("&:hover")
 *         .prop("background-color", "darkblue")
 *     .parent()
 *     .nest("&:active")
 *         .prop("transform", "scale(0.98)")
 *     .parent()
 *     .nest("&.primary")
 *         .prop("background-color", "blue")
 *     .parent()
 *     .build()
 *
 * // Output:
 * // .btn {
 * //   padding: 0.5rem 1rem;
 * //
 * //   &:hover { background-color: darkblue; }
 * //   &:active { transform: scale(0.98); }
 * //   &.primary { background-color: blue; }
 * // }
 * }</pre>
 *
 * <h2>Deep Nesting</h2>
 * <pre>{@code
 * CSSNested.rule("nav")
 *     .nest("ul")
 *         .prop("list-style", "none")
 *         .nest("li")
 *             .prop("display", "inline-block")
 *             .nest("a")
 *                 .prop("color", "blue")
 *                 .nest("&:hover")
 *                     .prop("text-decoration", "underline")
 *                 .parent()
 *             .parent()
 *         .parent()
 *     .parent()
 *     .build()
 * }</pre>
 *
 * <h2>Media Query Nesting</h2>
 * <pre>{@code
 * CSSNested.rule(".container")
 *     .prop("width", "100%")
 *     .media("(min-width: 768px)")
 *         .prop("max-width", "720px")
 *     .parent()
 *     .media("(min-width: 1024px)")
 *         .prop("max-width", "960px")
 *     .parent()
 *     .build()
 * }</pre>
 *
 * @see CSS for creating flat style rules
 * @see CSSLayer for cascade layers
 */
public final class CSSNested {

    private CSSNested() {}

    /**
     * Creates a new nested CSS rule starting with the given selector.
     *
     * @param selector the root selector
     * @return a NestedRule builder
     */
    public static NestedRule rule(String selector) {
        return new NestedRule(selector, null, 0);
    }

    /**
     * Creates a nested rule from a Selector builder.
     *
     * @param selector the selector builder
     * @return a NestedRule builder
     */
    public static NestedRule rule(CSS.Selector selector) {
        return new NestedRule(selector.build(), null, 0);
    }

    /**
     * Fluent builder for nested CSS rules.
     */
    public static class NestedRule {
        private final String selector;
        private final NestedRule parent;
        private final int depth;
        private final List<String> properties = new ArrayList<>();
        private final List<NestedRule> children = new ArrayList<>();
        private final List<String> atRules = new ArrayList<>();

        NestedRule(String selector, NestedRule parent, int depth) {
            this.selector = selector;
            this.parent = parent;
            this.depth = depth;
        }

        // ==================== Properties ====================

        /**
         * Adds a CSS property to this rule.
         *
         * @param name the property name
         * @param value the property value
         * @return this builder
         */
        public NestedRule prop(String name, String value) {
            properties.add(name + ": " + value);
            return this;
        }

        /**
         * Adds a CSS property using CSSValue.
         *
         * @param name the property name
         * @param value the CSS value
         * @return this builder
         */
        public NestedRule prop(String name, CSSValue value) {
            properties.add(name + ": " + value.css());
            return this;
        }

        /**
         * Adds properties from a StyleBuilder.
         *
         * @param style the style builder
         * @return this builder
         */
        public NestedRule style(CSS.StyleBuilder style) {
            // Extract the raw properties from the style builder
            String built = style.build();
            if (!built.isEmpty()) {
                // Split by semicolon and add each property
                for (String prop : built.split(";")) {
                    prop = prop.trim();
                    if (!prop.isEmpty()) {
                        properties.add(prop);
                    }
                }
            }
            return this;
        }

        // ==================== Nesting ====================

        /**
         * Creates a nested child rule.
         *
         * @param childSelector the child selector (can use &amp; for parent reference)
         * @return the child NestedRule
         */
        public NestedRule nest(String childSelector) {
            NestedRule child = new NestedRule(childSelector, this, depth + 1);
            children.add(child);
            return child;
        }

        /**
         * Creates a nested child rule from a Selector.
         *
         * @param childSelector the selector builder
         * @return the child NestedRule
         */
        public NestedRule nest(CSS.Selector childSelector) {
            return nest(childSelector.build());
        }

        /**
         * Returns to the parent rule.
         *
         * @return the parent NestedRule
         * @throws IllegalStateException if called on root rule
         */
        public NestedRule parent() {
            if (parent == null) {
                throw new IllegalStateException("Already at root level");
            }
            return parent;
        }

        /**
         * Returns to the root rule.
         *
         * @return the root NestedRule
         */
        public NestedRule root() {
            NestedRule current = this;
            while (current.parent != null) {
                current = current.parent;
            }
            return current;
        }

        // ==================== At-Rules ====================

        /**
         * Creates a nested @media rule.
         *
         * @param query the media query (without @media)
         * @return a child NestedRule for the media content
         */
        public NestedRule media(String query) {
            NestedRule mediaRule = new NestedRule("@media " + query, this, depth + 1);
            children.add(mediaRule);
            return mediaRule;
        }

        /**
         * Creates a nested @supports rule.
         *
         * @param condition the supports condition
         * @return a child NestedRule
         */
        public NestedRule supports(String condition) {
            NestedRule supportsRule = new NestedRule("@supports " + condition, this, depth + 1);
            children.add(supportsRule);
            return supportsRule;
        }

        /**
         * Creates a nested @container rule.
         *
         * @param query the container query
         * @return a child NestedRule
         */
        public NestedRule container(String query) {
            NestedRule containerRule = new NestedRule("@container " + query, this, depth + 1);
            children.add(containerRule);
            return containerRule;
        }

        // ==================== Build ====================

        /**
         * Builds the complete nested CSS.
         *
         * @return the CSS string
         */
        public String build() {
            return buildWithIndent(0);
        }

        private String buildWithIndent(int indent) {
            StringBuilder sb = new StringBuilder();
            String indentStr = "  ".repeat(indent);

            // Start the rule
            sb.append(indentStr).append(selector).append(" {\n");

            // Add properties
            for (String prop : properties) {
                sb.append(indentStr).append("  ").append(prop).append(";\n");
            }

            // Add children (nested rules)
            for (NestedRule child : children) {
                sb.append("\n");
                sb.append(child.buildWithIndent(indent + 1));
            }

            // Close the rule
            sb.append(indentStr).append("}\n");

            return sb.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }

    // ==================== Convenience Methods ====================

    /**
     * Creates multiple nested rules and combines them.
     *
     * @param rules the nested rules
     * @return combined CSS string
     */
    public static String stylesheet(NestedRule... rules) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rules.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(rules[i].build());
        }
        return sb.toString();
    }

    /**
     * Creates a BEM-style component block.
     *
     * <p>Example:</p>
     * <pre>
     * CSSNested.block("card")
     *     .prop("padding", "1rem")
     *     .element("header")
     *         .prop("font-weight", "bold")
     *     .parent()
     *     .element("body")
     *         .prop("padding", "0.5rem")
     *     .parent()
     *     .modifier("featured")
     *         .prop("border", "2px solid gold")
     *     .parent()
     *     .build()
     *
     * // Creates:
     * // .card { padding: 1rem; }
     * // .card__header { font-weight: bold; }
     * // .card__body { padding: 0.5rem; }
     * // .card--featured { border: 2px solid gold; }
     * </pre>
     *
     * @param blockName the BEM block name
     * @return a BEMBlock builder
     */
    public static BEMBlock block(String blockName) {
        return new BEMBlock(blockName);
    }

    /**
     * BEM (Block Element Modifier) style builder.
     */
    public static class BEMBlock {
        private final String blockName;
        private final NestedRule root;
        private NestedRule current;

        BEMBlock(String blockName) {
            this.blockName = blockName;
            this.root = new NestedRule("." + blockName, null, 0);
            this.current = root;
        }

        /**
         * Adds a property to the current selector.
         */
        public BEMBlock prop(String name, String value) {
            current.prop(name, value);
            return this;
        }

        /**
         * Adds a property using CSSValue.
         */
        public BEMBlock prop(String name, CSSValue value) {
            current.prop(name, value);
            return this;
        }

        /**
         * Creates a BEM element (block__element).
         */
        public BEMBlock element(String elementName) {
            current = root.nest("&__" + elementName);
            return this;
        }

        /**
         * Creates a BEM modifier (block--modifier).
         */
        public BEMBlock modifier(String modifierName) {
            current = root.nest("&--" + modifierName);
            return this;
        }

        /**
         * Returns to the block level.
         */
        public BEMBlock parent() {
            current = root;
            return this;
        }

        /**
         * Builds the complete CSS.
         */
        public String build() {
            return root.build();
        }

        @Override
        public String toString() {
            return build();
        }
    }
}
