package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTML tag element.
 */
public class Tag implements Element {

    private final String tagName;
    private final Map<String, String> attributes;
    private final List<VNode> children;

    public Tag(String tagName) {
        this(tagName, Map.of(), List.of());
    }

    public Tag(String tagName, Attributes attributes) {
        this(tagName, attributes.toMap(), List.of());
    }

    public Tag(String tagName, List<VNode> children) {
        this(tagName, Map.of(), children);
    }

    public Tag(String tagName, Attributes attributes, List<VNode> children) {
        this(tagName, attributes.toMap(), children);
    }

    public Tag(String tagName, Map<String, String> attributes, List<VNode> children) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public VNode toVNode() {
        return VElement.of(tagName, attributes, children);
    }

    public Tag attr(String name, String value) {
        Map<String, String> newAttrs = new LinkedHashMap<>(attributes);
        newAttrs.put(name, value);
        return new Tag(tagName, newAttrs, children);
    }

    public Tag id(String id) { return attr("id", id); }
    public Tag class_(String className) { return attr("class", className); }
    public Tag style(String style) { return attr("style", style); }

    public String getTagName() { return tagName; }
    public Map<String, String> getAttributes() { return attributes; }
    public List<VNode> getChildren() { return children; }

    public static VNode toVNode(Object child) {
        if (child == null) return new VText("");
        if (child instanceof VNode vnode) return vnode;
        if (child instanceof Element element) return element.toVNode();
        if (child instanceof String text) return new VText(text);
        return new VText(child.toString());
    }

    public static List<VNode> toVNodes(Object... children) {
        List<VNode> nodes = new ArrayList<>();
        for (Object child : children) {
            if (child == null) continue;
            if (child instanceof Attr) continue;

            if (child instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (!(item instanceof Attr)) {
                        nodes.add(toVNode(item));
                    }
                }
            } else {
                nodes.add(toVNode(child));
            }
        }
        return nodes;
    }

    public static Map<String, String> extractAttrs(Object... items) {
        Map<String, String> attrs = new LinkedHashMap<>();
        for (Object item : items) {
            if (item instanceof Attr attr) {
                attrs.put(attr.name(), attr.value());
            } else if (item instanceof Iterable<?> iterable) {
                for (Object subItem : iterable) {
                    if (subItem instanceof Attr attr) {
                        attrs.put(attr.name(), attr.value());
                    }
                }
            }
        }
        return attrs;
    }

    /**
     * Creates a Tag from mixed Attr objects and children.
     * This enables: div(class_("foo"), id("bar"), p("text"))
     */
    public static Tag create(String tagName, Object... items) {
        Map<String, String> attrs = extractAttrs(items);
        List<VNode> children = toVNodes(items);
        return new Tag(tagName, attrs, children);
    }
}
