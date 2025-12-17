package com.osmig.Jweb.framework.core;

/**
 * Represents an HTML element that can be rendered.
 * This is the primary interface for building UIs.
 *
 * Elements can be:
 * - HTML tags (div, p, span, etc.)
 * - Text nodes
 * - Templates (reusable components)
 */
public interface Element extends Renderable {

    /**
     * Renders this element to HTML.
     */
    default String toHtml() {
        return toVNode().toHtml();
    }
}
