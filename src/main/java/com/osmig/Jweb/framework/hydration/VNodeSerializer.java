package com.osmig.Jweb.framework.hydration;

import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VRaw;
import com.osmig.Jweb.framework.vdom.VText;

import java.util.Map;

/**
 * Serializes VNode trees to JSON for client-side hydration.
 *
 * <p>The JSON format is:</p>
 * <pre>
 * // VElement
 * {
 *   "type": "element",
 *   "tag": "div",
 *   "attrs": {"id": "main", "class": "container"},
 *   "children": [...]
 * }
 *
 * // VText
 * {"type": "text", "content": "Hello World"}
 *
 * // VRaw
 * {"type": "raw", "html": "<b>Bold</b>"}
 *
 * // VFragment
 * {"type": "fragment", "children": [...]}
 * </pre>
 */
public final class VNodeSerializer {

    private VNodeSerializer() {
        // Static utility class
    }

    /**
     * Serializes a VNode to JSON.
     *
     * @param node the node to serialize
     * @return JSON string representation
     */
    public static String toJson(VNode node) {
        if (node == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        serialize(node, sb);
        return sb.toString();
    }

    private static void serialize(VNode node, StringBuilder sb) {
        switch (node) {
            case VElement element -> serializeElement(element, sb);
            case VText text -> serializeText(text, sb);
            case VRaw raw -> serializeRaw(raw, sb);
            case VFragment fragment -> serializeFragment(fragment, sb);
        }
    }

    private static void serializeElement(VElement element, StringBuilder sb) {
        sb.append("{\"type\":\"element\",\"tag\":\"").append(escapeJson(element.getTag())).append("\"");

        // Attributes
        Map<String, String> attrs = element.getAttributes();
        if (!attrs.isEmpty()) {
            sb.append(",\"attrs\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
                if (entry.getValue() == null) {
                    sb.append("true");  // Boolean attributes
                } else {
                    sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
                }
                first = false;
            }
            sb.append("}");
        }

        // Children
        var children = element.getChildren();
        if (!children.isEmpty()) {
            sb.append(",\"children\":[");
            boolean first = true;
            for (VNode child : children) {
                if (!first) sb.append(",");
                serialize(child, sb);
                first = false;
            }
            sb.append("]");
        }

        sb.append("}");
    }

    private static void serializeText(VText text, StringBuilder sb) {
        sb.append("{\"type\":\"text\",\"content\":\"").append(escapeJson(text.content())).append("\"}");
    }

    private static void serializeRaw(VRaw raw, StringBuilder sb) {
        sb.append("{\"type\":\"raw\",\"html\":\"").append(escapeJson(raw.html())).append("\"}");
    }

    private static void serializeFragment(VFragment fragment, StringBuilder sb) {
        sb.append("{\"type\":\"fragment\",\"children\":[");
        boolean first = true;
        for (VNode child : fragment.children()) {
            if (!first) sb.append(",");
            serialize(child, sb);
            first = false;
        }
        sb.append("]}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Pretty-prints a VNode to JSON with indentation.
     *
     * @param node the node to serialize
     * @return formatted JSON string
     */
    public static String toPrettyJson(VNode node) {
        return toPrettyJson(node, 0);
    }

    private static String toPrettyJson(VNode node, int indent) {
        if (node == null) return "null";

        String pad = "  ".repeat(indent);
        String childPad = "  ".repeat(indent + 1);

        return switch (node) {
            case VElement element -> {
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                sb.append(childPad).append("\"type\": \"element\",\n");
                sb.append(childPad).append("\"tag\": \"").append(element.getTag()).append("\"");

                if (!element.getAttributes().isEmpty()) {
                    sb.append(",\n").append(childPad).append("\"attrs\": {");
                    boolean first = true;
                    for (Map.Entry<String, String> e : element.getAttributes().entrySet()) {
                        if (!first) sb.append(",");
                        sb.append("\n").append(childPad).append("  \"").append(e.getKey()).append("\": ");
                        sb.append(e.getValue() == null ? "true" : "\"" + escapeJson(e.getValue()) + "\"");
                        first = false;
                    }
                    sb.append("\n").append(childPad).append("}");
                }

                if (!element.getChildren().isEmpty()) {
                    sb.append(",\n").append(childPad).append("\"children\": [\n");
                    boolean first = true;
                    for (VNode child : element.getChildren()) {
                        if (!first) sb.append(",\n");
                        sb.append(childPad).append("  ").append(toPrettyJson(child, indent + 2).trim());
                        first = false;
                    }
                    sb.append("\n").append(childPad).append("]");
                }

                sb.append("\n").append(pad).append("}");
                yield sb.toString();
            }
            case VText text -> "{\"type\": \"text\", \"content\": \"" + escapeJson(text.content()) + "\"}";
            case VRaw raw -> "{\"type\": \"raw\", \"html\": \"" + escapeJson(raw.html()) + "\"}";
            case VFragment fragment -> {
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                sb.append(childPad).append("\"type\": \"fragment\",\n");
                sb.append(childPad).append("\"children\": [\n");
                boolean first = true;
                for (VNode child : fragment.children()) {
                    if (!first) sb.append(",\n");
                    sb.append(childPad).append("  ").append(toPrettyJson(child, indent + 2).trim());
                    first = false;
                }
                sb.append("\n").append(childPad).append("]\n");
                sb.append(pad).append("}");
                yield sb.toString();
            }
        };
    }
}
