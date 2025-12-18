package com.osmig.Jweb.framework.state;

/**
 * Interface for components that can be re-rendered when state changes.
 */
@FunctionalInterface
public interface RenderableComponent {

    /**
     * Renders the component to HTML.
     *
     * @return the HTML string
     */
    String render();
}
