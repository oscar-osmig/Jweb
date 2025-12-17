package com.osmig.Jweb.framework.core;

import com.osmig.Jweb.framework.vdom.VNode;

/**
 * Base interface for anything that can be rendered to HTML.
 */
public interface Renderable {
    /**
     * Converts this renderable to a VNode for rendering.
     */
    VNode toVNode();
}
