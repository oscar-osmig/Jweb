package com.osmig.Jweb.framework.core;

/**
 * Represents an HTML element that can be rendered.
 * This is the primary interface for building UIs.
 *
 * Elements can be:
 * - HTML tags (div, p, span, etc.)
 * - Text nodes
 * - Templates (reusable components)
 *
 * <p>In application code, prefer importing {@link jweb.Element} — the short
 * alias for this interface. Every framework element implements both.</p>
 */
public interface Element extends jweb.Element {

    /**
     * Views any {@link jweb.Element} as a legacy Element. Framework-built
     * elements already implement both interfaces and pass through untouched;
     * only user types implementing bare {@code jweb.Element} get wrapped.
     */
    static Element of(jweb.Element e) {
        if (e == null) return null;
        return e instanceof Element el ? el : e::toVNode;
    }
}
