package com.osmig.Jweb.framework.vdom;

/**
 * Virtual DOM Raw HTML - unescaped HTML content.
 * WARNING: Only use for trusted content!
 */
public record VRaw(String html) implements VNode {

    public VRaw {
        if (html == null) {
            html = "";
        }
    }

    @Override
    public String toHtml() {
        return html;
    }

    @Override
    public VNode copy() {
        return new VRaw(html);
    }
}
