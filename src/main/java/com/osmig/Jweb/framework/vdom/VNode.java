package com.osmig.Jweb.framework.vdom;

/**
 * Virtual DOM Node - the foundation of JWeb's rendering system.
 */
public sealed interface VNode permits VElement, VText, VRaw, VFragment {

    /**
     * Converts this virtual node to HTML.
     */
    String toHtml();

    /**
     * Returns the node ID (for diffing).
     */
    default String getId() {
        return null;
    }

    /**
     * Creates a deep copy of this node.
     */
    VNode copy();
}
