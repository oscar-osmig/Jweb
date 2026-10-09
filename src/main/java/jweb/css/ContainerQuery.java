package jweb.css;

import jweb.CSSValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for CSS Container Queries.
 *
 * Container queries allow styling based on the size of a container element,
 * not just the viewport like media queries.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.ContainerQuery.*;
 *   import static com.osmig.Jweb.framework.styles.Styles.*;
 *
 *   // Define container on parent (in your styles)
 *   style().containerType(inlineSize).containerName("card")
 *
 *   // Query the container
 *   container("card").minWidth(px(400)).rules(
 *       rule(".card-content", style().display(flex))
 *   )
 *
 *   // Anonymous container query
 *   container().minWidth(px(300)).rules(
 *       rule(".item", style().flexDirection(row))
 *   )
 */
public class ContainerQuery {

    private String containerName;
    private final List<String> conditions = new ArrayList<>();
    private final Map<String, jweb.Style<?>> rules = new LinkedHashMap<>();

    protected ContainerQuery() {}

    private ContainerQuery(String name) {
        this.containerName = name;
    }

    public static ContainerQuery container() {
        return new ContainerQuery();
    }

    public static ContainerQuery container(String name) {
        return new ContainerQuery(name);
    }

    // ==================== Size Conditions ====================

    public ContainerQuery minWidth(jweb.CSSValue value) {
        conditions.add("(min-width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxWidth(jweb.CSSValue value) {
        conditions.add("(max-width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery width(jweb.CSSValue value) {
        conditions.add("(width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery minHeight(jweb.CSSValue value) {
        conditions.add("(min-height: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxHeight(jweb.CSSValue value) {
        conditions.add("(max-height: " + value.css() + ")");
        return this;
    }

    public ContainerQuery height(jweb.CSSValue value) {
        conditions.add("(height: " + value.css() + ")");
        return this;
    }

    // ==================== Inline/Block Size ====================

    public ContainerQuery minInlineSize(jweb.CSSValue value) {
        conditions.add("(min-inline-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxInlineSize(jweb.CSSValue value) {
        conditions.add("(max-inline-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery minBlockSize(jweb.CSSValue value) {
        conditions.add("(min-block-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxBlockSize(jweb.CSSValue value) {
        conditions.add("(max-block-size: " + value.css() + ")");
        return this;
    }

    // ==================== Aspect Ratio ====================

    public ContainerQuery minAspectRatio(String ratio) {
        conditions.add("(min-aspect-ratio: " + ratio + ")");
        return this;
    }

    public ContainerQuery maxAspectRatio(String ratio) {
        conditions.add("(max-aspect-ratio: " + ratio + ")");
        return this;
    }

    public ContainerQuery aspectRatio(String ratio) {
        conditions.add("(aspect-ratio: " + ratio + ")");
        return this;
    }

    // ==================== Orientation ====================

    public ContainerQuery portrait() {
        conditions.add("(orientation: portrait)");
        return this;
    }

    public ContainerQuery landscape() {
        conditions.add("(orientation: landscape)");
        return this;
    }

    // ==================== Style Queries ====================

    /**
     * A style query — matches on the computed value of a custom property on
     * the container: {@code @container style(--theme: dark)}.
     *
     * <p>Example:</p>
     * <pre>
     * container("card").style("--variant", "featured")
     *     .rule(".title", style().fontWeight(700))
     * </pre>
     *
     * @param customProperty the custom property name (the {@code --} is added
     *     for you)
     * @param value the value it must have
     * @return this builder for chaining
     */
    public ContainerQuery style(String customProperty, String value) {
        String name = customProperty.startsWith("--") ? customProperty : "--" + customProperty;
        conditions.add("style(" + name + ": " + value + ")");
        return this;
    }

    /**
     * A style query with a typed value — see {@link #style(String, String)}.
     *
     * @param customProperty the custom property name
     * @param value the value it must have
     * @return this builder for chaining
     */
    public ContainerQuery style(String customProperty, CSSValue value) {
        return style(customProperty, value.css());
    }

    // ==================== Custom Condition ====================

    public ContainerQuery condition(String condition) {
        conditions.add(condition);
        return this;
    }

    // ==================== Rules ====================

    public ContainerQuery rule(String selector, jweb.Style<?> style) {
        rules.put(selector, style);
        return this;
    }

    /** {@link #rule(String, jweb.Style)} from a typed selector ({@code CARD.hover()}). */
    public ContainerQuery rule(Selector selector, jweb.Style<?> style) {
        return rule(selector.build(), style);
    }

    /**
     * Adds multiple CSS rules to this container query.
     *
     * <p>Example:</p>
     * <pre>
     * container("card").minWidth(px(400)).rules(
     *     Rule.of(".card-content", style().display("flex"))
     * )
     * </pre>
     *
     * @param ruleArray the rules to add
     * @return this builder for chaining
     */
    public ContainerQuery rules(Rule... ruleArray) {
        for (Rule r : ruleArray) {
            rules.put(r.selector(), r.style());
        }
        return this;
    }

    // ==================== Build ====================

    /**
     * Just the {@code @container …} condition line, without its rules — what
     * {@code style().at(container("card").minWidth(px(400)), …)} wraps a
     * generated class in.
     *
     * @return the at-rule prelude
     */
    public String query() {
        StringBuilder sb = new StringBuilder("@container ");
        if (containerName != null && !containerName.isEmpty()) {
            sb.append(containerName).append(" ");
        }
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append(" and ");
            sb.append(conditions.get(i));
        }
        return sb.toString().stripTrailing();
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@container ");

        // Add container name if specified
        if (containerName != null && !containerName.isEmpty()) {
            sb.append(containerName).append(" ");
        }

        // Join conditions
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append(" and ");
            sb.append(conditions.get(i));
        }

        sb.append(" {\n");

        for (Map.Entry<String, jweb.Style<?>> entry : rules.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" {\n");
            for (Map.Entry<String, String> prop : entry.getValue().toMap().entrySet()) {
                sb.append("    ").append(prop.getKey()).append(": ").append(prop.getValue()).append(";\n");
            }
            sb.append("  }\n");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    // ==================== Container Type Constants ====================

    /** For querying inline-axis dimensions (width in horizontal writing modes) */
    public static final CSSValue inlineSize = () -> "inline-size";

    /** For querying both dimensions */
    public static final CSSValue size = () -> "size";

    /** Establish a query container without enabling size queries */
    public static final CSSValue normal = () -> "normal";
}
