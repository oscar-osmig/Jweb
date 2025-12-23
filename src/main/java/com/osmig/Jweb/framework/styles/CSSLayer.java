package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.List;

/**
 * CSS Cascade Layers (@layer) support for controlling CSS specificity order.
 *
 * <p>CSS Cascade Layers allow you to control the order in which CSS rules are
 * applied, independent of specificity. This is useful for managing third-party
 * CSS, reset styles, and component libraries.</p>
 *
 * <h2>Layer Order Declaration</h2>
 * <pre>{@code
 * // Declare layer order (later layers win)
 * CSSLayer.order("reset", "base", "components", "utilities")
 * // Output: @layer reset, base, components, utilities;
 * }</pre>
 *
 * <h2>Layer with Rules</h2>
 * <pre>{@code
 * CSSLayer.layer("base",
 *     rule("*").boxSizing(borderBox),
 *     rule("body").margin(zero).fontFamily("system-ui")
 * )
 * // Output:
 * // @layer base {
 * //   * { box-sizing: border-box; }
 * //   body { margin: 0; font-family: system-ui; }
 * // }
 * }</pre>
 *
 * <h2>Anonymous Layer</h2>
 * <pre>{@code
 * CSSLayer.anonymous(
 *     rule(".btn").padding(px(10))
 * )
 * // Output:
 * // @layer {
 * //   .btn { padding: 10px; }
 * // }
 * }</pre>
 *
 * <h2>Nested Layers</h2>
 * <pre>{@code
 * CSSLayer.layer("framework.reset",
 *     rule("*").margin(zero)
 * )
 * // Output:
 * // @layer framework.reset {
 * //   * { margin: 0; }
 * // }
 * }</pre>
 *
 * <h2>Import into Layer</h2>
 * <pre>{@code
 * CSSLayer.importInto("vendor", "/css/library.css")
 * // Output: @import url('/css/library.css') layer(vendor);
 * }</pre>
 *
 * @see CSS for creating style rules
 * @see MediaQuery for media queries
 */
public final class CSSLayer {

    private CSSLayer() {}

    // ==================== Layer Order ====================

    /**
     * Declares the order of cascade layers.
     * Later layers have higher priority regardless of specificity.
     *
     * <p>Example:</p>
     * <pre>
     * CSSLayer.order("reset", "base", "components", "utilities")
     * // Output: @layer reset, base, components, utilities;
     * </pre>
     *
     * @param layers layer names in priority order (first = lowest priority)
     * @return the @layer declaration string
     */
    public static String order(String... layers) {
        return "@layer " + String.join(", ", layers) + ";";
    }

    /**
     * Declares a single layer (for forward declaration).
     *
     * @param name the layer name
     * @return the @layer declaration string
     */
    public static String declare(String name) {
        return "@layer " + name + ";";
    }

    // ==================== Layer with Rules ====================

    /**
     * Creates a named layer with CSS rules.
     *
     * <p>Example:</p>
     * <pre>
     * CSSLayer.layer("base",
     *     rule("*").boxSizing(borderBox),
     *     rule("body").margin(zero)
     * )
     * </pre>
     *
     * @param name the layer name
     * @param rules the CSS rules for this layer
     * @return the complete @layer block
     */
    public static String layer(String name, CSS.StyleBuilder... rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("@layer ").append(name).append(" {\n");
        for (CSS.StyleBuilder rule : rules) {
            sb.append("  ").append(rule.toRule()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates a named layer with raw CSS content.
     *
     * @param name the layer name
     * @param css the raw CSS content
     * @return the complete @layer block
     */
    public static String layer(String name, String css) {
        return "@layer " + name + " {\n" + css + "\n}";
    }

    /**
     * Creates an anonymous layer with CSS rules.
     * Anonymous layers are useful for one-off style encapsulation.
     *
     * @param rules the CSS rules
     * @return the complete @layer block
     */
    public static String anonymous(CSS.StyleBuilder... rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("@layer {\n");
        for (CSS.StyleBuilder rule : rules) {
            sb.append("  ").append(rule.toRule()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates an anonymous layer with raw CSS content.
     *
     * @param css the raw CSS content
     * @return the complete @layer block
     */
    public static String anonymous(String css) {
        return "@layer {\n" + css + "\n}";
    }

    // ==================== Import into Layer ====================

    /**
     * Creates an @import that loads CSS into a specific layer.
     *
     * <p>Example:</p>
     * <pre>
     * CSSLayer.importInto("vendor", "/css/bootstrap.css")
     * // Output: @import url('/css/bootstrap.css') layer(vendor);
     * </pre>
     *
     * @param layerName the layer to import into
     * @param url the CSS file URL
     * @return the @import statement
     */
    public static String importInto(String layerName, String url) {
        return "@import url('" + url + "') layer(" + layerName + ");";
    }

    /**
     * Creates an @import with media query into a specific layer.
     *
     * @param layerName the layer to import into
     * @param url the CSS file URL
     * @param mediaQuery the media query condition
     * @return the @import statement
     */
    public static String importInto(String layerName, String url, String mediaQuery) {
        return "@import url('" + url + "') layer(" + layerName + ") " + mediaQuery + ";";
    }

    // ==================== Layer Builder ====================

    /**
     * Creates a fluent layer builder for more complex layer definitions.
     *
     * <p>Example:</p>
     * <pre>
     * CSSLayer.named("components")
     *     .rule(rule(".btn").padding(px(10)))
     *     .rule(rule(".card").border(px(1), solid, gray))
     *     .build()
     * </pre>
     *
     * @param name the layer name
     * @return a LayerBuilder instance
     */
    public static LayerBuilder named(String name) {
        return new LayerBuilder(name);
    }

    /**
     * Fluent builder for constructing CSS layers.
     */
    public static class LayerBuilder {
        private final String name;
        private final List<String> rules = new ArrayList<>();

        LayerBuilder(String name) {
            this.name = name;
        }

        /**
         * Adds a CSS rule to this layer.
         *
         * @param rule the StyleBuilder rule
         * @return this builder
         */
        public LayerBuilder rule(CSS.StyleBuilder rule) {
            rules.add(rule.toRule());
            return this;
        }

        /**
         * Adds raw CSS to this layer.
         *
         * @param css the raw CSS string
         * @return this builder
         */
        public LayerBuilder css(String css) {
            rules.add(css);
            return this;
        }

        /**
         * Adds a media query block to this layer.
         *
         * @param mediaQuery the complete media query block
         * @return this builder
         */
        public LayerBuilder media(String mediaQuery) {
            rules.add(mediaQuery);
            return this;
        }

        /**
         * Builds the complete @layer block.
         *
         * @return the CSS string
         */
        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("@layer ").append(name).append(" {\n");
            for (String rule : rules) {
                // Indent each line
                for (String line : rule.split("\n")) {
                    sb.append("  ").append(line).append("\n");
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

    // ==================== Utility Methods ====================

    /**
     * Combines multiple layer definitions into a single stylesheet.
     *
     * @param layers the layer definitions
     * @return the combined CSS string
     */
    public static String stylesheet(String... layers) {
        return String.join("\n\n", layers);
    }
}
