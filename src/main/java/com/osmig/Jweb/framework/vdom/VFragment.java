package com.osmig.Jweb.framework.vdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Virtual DOM Fragment - groups elements without a wrapper.
 */
public record VFragment(List<VNode> children) implements VNode {

    public VFragment {
        children = children == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(children));
    }

    public VFragment(VNode... children) {
        this(children == null ? Collections.emptyList() : List.of(children));
    }

    @Override
    public String toHtml() {
        StringBuilder html = new StringBuilder();
        for (VNode child : children) {
            html.append(child.toHtml());
        }
        return html.toString();
    }

    @Override
    public VNode copy() {
        List<VNode> copiedChildren = children.stream()
            .map(VNode::copy)
            .toList();
        return new VFragment(copiedChildren);
    }
}
