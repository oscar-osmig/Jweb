package com.osmig.Jweb.framework.styles;

/**
 * CSS @property rule support for registering custom properties with syntax and constraints.
 *
 * <p>The @property at-rule allows you to register custom CSS properties (CSS variables)
 * with explicit syntax, inheritance, and initial values. This enables animations,
 * type-checking, and better browser optimization.</p>
 *
 * <h2>Basic Property Registration</h2>
 * <pre>{@code
 * CSSProperty.register("--main-color")
 *     .syntax("<color>")
 *     .inherits(true)
 *     .initialValue("#3498db")
 *     .build()
 *
 * // Output:
 * // @property --main-color {
 * //   syntax: '<color>';
 * //   inherits: true;
 * //   initial-value: #3498db;
 * // }
 * }</pre>
 *
 * <h2>Numeric Property for Animation</h2>
 * <pre>{@code
 * CSSProperty.register("--progress")
 *     .syntax("<number>")
 *     .inherits(false)
 *     .initialValue("0")
 *     .build()
 *
 * // Now you can animate --progress from 0 to 100
 * }</pre>
 *
 * <h2>Length Property</h2>
 * <pre>{@code
 * CSSProperty.register("--spacing")
 *     .syntax("<length>")
 *     .inherits(true)
 *     .initialValue("1rem")
 *     .build()
 * }</pre>
 *
 * <h2>Percentage Property</h2>
 * <pre>{@code
 * CSSProperty.register("--opacity-level")
 *     .syntax("<percentage>")
 *     .inherits(false)
 *     .initialValue("100%")
 *     .build()
 * }</pre>
 *
 * <h2>Multiple Properties</h2>
 * <pre>{@code
 * String css = CSSProperty.stylesheet(
 *     CSSProperty.register("--primary-hue")
 *         .syntax("<number>")
 *         .inherits(true)
 *         .initialValue("210"),
 *     CSSProperty.register("--primary-saturation")
 *         .syntax("<percentage>")
 *         .inherits(true)
 *         .initialValue("80%")
 * );
 * }</pre>
 *
 * <p><b>Common Syntax Types:</b></p>
 * <ul>
 *   <li>{@code <length>} - CSS length values (px, rem, em, etc.)</li>
 *   <li>{@code <number>} - Numeric values</li>
 *   <li>{@code <percentage>} - Percentage values</li>
 *   <li>{@code <color>} - Color values</li>
 *   <li>{@code <image>} - Image values (url(), gradient, etc.)</li>
 *   <li>{@code <url>} - URL values</li>
 *   <li>{@code <integer>} - Integer values</li>
 *   <li>{@code <angle>} - Angle values (deg, rad, etc.)</li>
 *   <li>{@code <time>} - Time values (s, ms)</li>
 *   <li>{@code <resolution>} - Resolution values (dpi, dpcm)</li>
 *   <li>{@code <transform-function>} - Transform functions</li>
 *   <li>{@code <custom-ident>} - Custom identifiers</li>
 *   <li>{@code *} - Any value (default behavior)</li>
 * </ul>
 *
 * @see CSS for creating style rules
 * @see CSSLayer for cascade layers
 */
public final class CSSProperty {

    private CSSProperty() {}

    /**
     * Starts registering a custom property.
     * Automatically adds "--" prefix if not present.
     *
     * <p>Example:</p>
     * <pre>
     * CSSProperty.register("--theme-color")
     *     .syntax("<color>")
     *     .inherits(true)
     *     .initialValue("blue")
     *     .build()
     * </pre>
     *
     * @param name the custom property name (with or without "--" prefix)
     * @return a PropertyBuilder instance
     */
    public static PropertyBuilder register(String name) {
        String normalized = name.startsWith("--") ? name : "--" + name;
        return new PropertyBuilder(normalized);
    }

    /**
     * Fluent builder for constructing @property rules.
     */
    public static class PropertyBuilder {
        private final String name;
        private String syntax;
        private Boolean inherits;
        private String initialValue;

        PropertyBuilder(String name) {
            this.name = name;
        }

        /**
         * Sets the syntax descriptor for this property.
         * Defines what type of values this property accepts.
         *
         * <p>Common values:</p>
         * <ul>
         *   <li>"&lt;color&gt;" - color values</li>
         *   <li>"&lt;length&gt;" - length values (px, rem, etc.)</li>
         *   <li>"&lt;number&gt;" - numeric values</li>
         *   <li>"&lt;percentage&gt;" - percentage values</li>
         *   <li>"&lt;integer&gt;" - integer values</li>
         *   <li>"&lt;angle&gt;" - angle values</li>
         *   <li>"*" - any value (default)</li>
         * </ul>
         *
         * @param syntax the syntax descriptor
         * @return this builder
         */
        public PropertyBuilder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        /**
         * Sets whether this property inherits from parent elements.
         *
         * @param inherits true if the property should inherit, false otherwise
         * @return this builder
         */
        public PropertyBuilder inherits(boolean inherits) {
            this.inherits = inherits;
            return this;
        }

        /**
         * Sets the initial value for this property.
         * Required if syntax is not "*".
         *
         * @param value the initial value
         * @return this builder
         */
        public PropertyBuilder initialValue(String value) {
            this.initialValue = value;
            return this;
        }

        /**
         * Sets the initial value for this property using a CSSValue.
         *
         * @param value the initial value
         * @return this builder
         */
        public PropertyBuilder initialValue(CSSValue value) {
            this.initialValue = value.css();
            return this;
        }

        /**
         * Builds the complete @property rule.
         *
         * @return the CSS string
         * @throws IllegalStateException if required fields are missing
         */
        public String build() {
            if (syntax == null) {
                throw new IllegalStateException("@property requires a syntax descriptor");
            }
            if (inherits == null) {
                throw new IllegalStateException("@property requires inherits to be set");
            }
            if (initialValue == null && !syntax.equals("*")) {
                throw new IllegalStateException("@property with syntax '" + syntax + "' requires an initial-value");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("@property ").append(name).append(" {\n");
            sb.append("  syntax: '").append(syntax).append("';\n");
            sb.append("  inherits: ").append(inherits ? "true" : "false").append(";\n");
            if (initialValue != null) {
                sb.append("  initial-value: ").append(initialValue).append(";\n");
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }

    // ==================== Common Syntax Helpers ====================

    /**
     * Creates a syntax for color values.
     * Equivalent to syntax("<color>").
     *
     * @return a PropertyBuilder with color syntax
     */
    public static PropertyBuilder color(String name) {
        return register(name).syntax("<color>");
    }

    /**
     * Creates a syntax for length values.
     * Equivalent to syntax("<length>").
     *
     * @return a PropertyBuilder with length syntax
     */
    public static PropertyBuilder length(String name) {
        return register(name).syntax("<length>");
    }

    /**
     * Creates a syntax for number values.
     * Equivalent to syntax("<number>").
     *
     * @return a PropertyBuilder with number syntax
     */
    public static PropertyBuilder number(String name) {
        return register(name).syntax("<number>");
    }

    /**
     * Creates a syntax for percentage values.
     * Equivalent to syntax("<percentage>").
     *
     * @return a PropertyBuilder with percentage syntax
     */
    public static PropertyBuilder percentage(String name) {
        return register(name).syntax("<percentage>");
    }

    /**
     * Creates a syntax for integer values.
     * Equivalent to syntax("<integer>").
     *
     * @return a PropertyBuilder with integer syntax
     */
    public static PropertyBuilder integer(String name) {
        return register(name).syntax("<integer>");
    }

    /**
     * Creates a syntax for angle values.
     * Equivalent to syntax("<angle>").
     *
     * @return a PropertyBuilder with angle syntax
     */
    public static PropertyBuilder angle(String name) {
        return register(name).syntax("<angle>");
    }

    /**
     * Creates a syntax for time values.
     * Equivalent to syntax("<time>").
     *
     * @return a PropertyBuilder with time syntax
     */
    public static PropertyBuilder time(String name) {
        return register(name).syntax("<time>");
    }

    /**
     * Creates a syntax for image values.
     * Equivalent to syntax("<image>").
     *
     * @return a PropertyBuilder with image syntax
     */
    public static PropertyBuilder image(String name) {
        return register(name).syntax("<image>");
    }

    // ==================== Utility Methods ====================

    /**
     * Combines multiple property registrations into a single stylesheet.
     *
     * @param properties the property builders
     * @return the combined CSS string
     */
    public static String stylesheet(PropertyBuilder... properties) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < properties.length; i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(properties[i].build());
        }
        return sb.toString();
    }
}
