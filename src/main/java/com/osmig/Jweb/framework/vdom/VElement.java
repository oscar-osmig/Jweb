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

        // Raw on<type>="js" attributes (set("onclick", ...), Ref helpers,
        // cached elements built outside a request) can never run under the
        // nonce CSP, so — like the nonce stamping below — the serializer
        // rewrites them per render: the JS registers with the page's
        // definitions (ClientActions) and a data-jweb-act<type> attribute
        // the runtime delegates to is written instead. Skipped when the
        // element opts out via data-jweb-inline (error pages), when the
        // runtime doesn't delegate the type, or when there is no render
        // context to deliver definitions through.
        boolean keepInline = attributes.containsKey("data-jweb-inline");

        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            String name = attr.getKey();
            String value = attr.getValue();

            if (!keepInline && value != null && name.length() > 2
                    && (name.charAt(0) == 'o' || name.charAt(0) == 'O')
                    && (name.charAt(1) == 'n' || name.charAt(1) == 'N')) {
                // HTML attribute names are case-insensitive: onClick == onclick
                String type = name.substring(2).toLowerCase(Locale.ROOT);
                if (com.osmig.Jweb.framework.js.ClientActions.isDelegatedEvent(type)) {
                    String id = com.osmig.Jweb.framework.js.ClientActions.register(value);
                    if (id != null) {
                        name = "data-jweb-act" + type;
                        value = id;
                    }
                }
            }

            if (value == null) {
                html.append(" ").append(name);
            } else {
                html.append(" ").append(name).append("=\"")
                    .append(escapeAttribute(value)).append("\"");
            }
        }

        // Stamp the per-request CSP nonce so inline scripts survive the
        // nonce-based Content-Security-Policy (injected markup won't have it)
        if ("script".equals(tag) && !attributes.containsKey("nonce")) {
            String nonce = com.osmig.Jweb.framework.security.CspNonce.current();
            if (nonce != null) {
                html.append(" nonce=\"").append(nonce).append("\"");
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
                case '\'' -> escaped.append("&#x27;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
