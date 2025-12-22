package com.osmig.Jweb.framework.core;

import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VRaw;

/**
 * Element that renders raw content without escaping.
 *
 * <p>Usage:</p>
 * <pre>
 * // Raw HTML
 * RawContent.html("&lt;h1&gt;Hello&lt;/h1&gt;");
 *
 * // Raw JSON (sets content type hint)
 * RawContent.json("{\"name\": \"John\"}");
 *
 * // Raw text
 * RawContent.text("Plain text content");
 * </pre>
 */
public class RawContent implements Element {

    private final String content;
    private final String contentType;

    private RawContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public VNode toVNode() {
        return new VRaw(content);
    }

    /**
     * Creates raw HTML content.
     */
    public static RawContent html(String content) {
        return new RawContent(content, "text/html");
    }

    /**
     * Creates raw JSON content.
     */
    public static RawContent json(String content) {
        return new RawContent(content, "application/json");
    }

    /**
     * Creates raw text content.
     */
    public static RawContent text(String content) {
        return new RawContent(content, "text/plain");
    }

    /**
     * Creates raw content with custom content type.
     */
    public static RawContent of(String content, String contentType) {
        return new RawContent(content, contentType);
    }

    @Override
    public String toHtml() {
        return content;
    }

    /**
     * Returns the content type hint.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Checks if this is JSON content.
     */
    public boolean isJson() {
        return "application/json".equals(contentType);
    }

    /**
     * Checks if this is HTML content.
     */
    public boolean isHtml() {
        return "text/html".equals(contentType);
    }
}
