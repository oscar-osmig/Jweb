package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VRaw;
import com.osmig.Jweb.framework.vdom.VText;

/**
 * Represents a text element (escaped or raw HTML).
 */
public class TextElement implements Element {

    private final String content;
    private final boolean raw;

    private TextElement(String content, boolean raw) {
        this.content = content != null ? content : "";
        this.raw = raw;
    }

    /**
     * Creates an escaped text element.
     */
    public static TextElement of(String content) {
        return new TextElement(content, false);
    }

    /**
     * Creates a raw (unescaped) HTML element.
     * WARNING: Only use with trusted content!
     */
    public static TextElement raw(String html) {
        return new TextElement(html, true);
    }

    @Override
    public VNode toVNode() {
        return raw ? new VRaw(content) : new VText(content);
    }

    public String getContent() { return content; }
    public boolean isRaw() { return raw; }
}
