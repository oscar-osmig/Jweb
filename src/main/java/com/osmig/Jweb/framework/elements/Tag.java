package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.vdom.VNode;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.Tag} — same class, shorter import. This name
 *             is a compatibility alias only: every element factory returns
 *             {@code jweb.Tag}, so declare variables as {@code jweb.Tag}.
 */
@Deprecated
public class Tag extends jweb.Tag {

    public Tag(String tagName) {
        super(tagName);
    }

    public Tag(String tagName, jweb.Attributes attributes) {
        super(tagName, attributes);
    }

    public Tag(String tagName, List<VNode> children) {
        super(tagName, children);
    }

    public Tag(String tagName, jweb.Attributes attributes, List<VNode> children) {
        super(tagName, attributes, children);
    }

    public Tag(String tagName, Map<String, String> attributes, List<VNode> children) {
        super(tagName, attributes, children);
    }
}
