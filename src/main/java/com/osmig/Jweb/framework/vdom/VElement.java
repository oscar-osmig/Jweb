package com.osmig.Jweb.framework.vdom;

import java.util.*;

/**
 * Virtual DOM Element - an HTML tag with attributes and children.
 */
public final class VElement implements VNode {

    private final String tag;
    private final Map<String, String> attributes;
    private final List<VNode> children;
    private final boolean selfClosing;

    private static final Set<String> VOID_ELEMENTS = Set.of(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr"
    );

    private VElement(String tag, Map<String, String> attributes, List<VNode> children) {
        this.tag = tag.toLowerCase();
        this.attributes = attributes == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        this.selfClosing = VOID_ELEMENTS.contains(this.tag);

        if (selfClosing && children != null && !children.isEmpty()) {
            throw new IllegalArgumentException(
                "Void element <" + tag + "> cannot have children"
            );
        }

        this.children = selfClosing || children == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(children));
    }

    public static VElement of(String tag) {
        return new VElement(tag, null, null);
    }

    public static VElement of(String tag, List<VNode> children) {
        return new VElement(tag, null, children);
    }

    public static VElement of(String tag, Map<String, String> attributes, List<VNode> children) {
        return new VElement(tag, attributes, children);
    }

    public VElement withAttribute(String name, String value) {
        Map<String, String> newAttrs = new LinkedHashMap<>(this.attributes);
        newAttrs.put(name, value);
        return new VElement(this.tag, newAttrs, this.children);
    }

    public VElement withChild(VNode child) {
        if (selfClosing) {
            throw new IllegalArgumentException("Void element <" + tag + "> cannot have children");
        }
        List<VNode> newChildren = new ArrayList<>(this.children);
        newChildren.add(child);
        return new VElement(this.tag, this.attributes, newChildren);
    }

    @Override
    public String toHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<").append(tag);

        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            String name = attr.getKey();
            String value = attr.getValue();

            if (value == null) {
                html.append(" ").append(name);
            } else {
                html.append(" ").append(name).append("=\"")
                    .append(escapeAttribute(value)).append("\"");
            }
        }

        if (selfClosing) {
            html.append(">");
            return html.toString();
        }

        html.append(">");

        for (VNode child : children) {
            html.append(child.toHtml());
        }

        html.append("</").append(tag).append(">");
        return html.toString();
    }

    @Override
    public String getId() {
        return attributes.get("id");
    }

    @Override
    public VNode copy() {
        List<VNode> copiedChildren = children.stream()
            .map(VNode::copy)
            .toList();
        return new VElement(tag, new LinkedHashMap<>(attributes), copiedChildren);
    }

    public String getTag() { return tag; }
    public Map<String, String> getAttributes() { return attributes; }
    public List<VNode> getChildren() { return children; }
    public boolean isSelfClosing() { return selfClosing; }

    private static String escapeAttribute(String value) {
        if (value == null) return "";
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
