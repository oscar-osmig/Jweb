package com.osmig.Jweb.framework.styles;

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
    private final Map<String, Style<?>> rules = new LinkedHashMap<>();

    private ContainerQuery() {}

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

    public ContainerQuery minWidth(CSSValue value) {
        conditions.add("(min-width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxWidth(CSSValue value) {
        conditions.add("(max-width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery width(CSSValue value) {
        conditions.add("(width: " + value.css() + ")");
        return this;
    }

    public ContainerQuery minHeight(CSSValue value) {
        conditions.add("(min-height: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxHeight(CSSValue value) {
        conditions.add("(max-height: " + value.css() + ")");
        return this;
    }

    public ContainerQuery height(CSSValue value) {
        conditions.add("(height: " + value.css() + ")");
        return this;
    }

    // ==================== Inline/Block Size ====================

    public ContainerQuery minInlineSize(CSSValue value) {
        conditions.add("(min-inline-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxInlineSize(CSSValue value) {
        conditions.add("(max-inline-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery minBlockSize(CSSValue value) {
        conditions.add("(min-block-size: " + value.css() + ")");
        return this;
    }

    public ContainerQuery maxBlockSize(CSSValue value) {
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

    // ==================== Custom Condition ====================

    public ContainerQuery condition(String condition) {
        conditions.add(condition);
        return this;
    }

    // ==================== Rules ====================

    public ContainerQuery rule(String selector, Style<?> style) {
        rules.put(selector, style);
        return this;
    }

    public ContainerQuery rules(MediaQuery.Rule... ruleArray) {
        for (MediaQuery.Rule r : ruleArray) {
            rules.put(r.selector(), r.style());
        }
        return this;
    }

    // ==================== Build ====================

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

        for (Map.Entry<String, Style<?>> entry : rules.entrySet()) {
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
