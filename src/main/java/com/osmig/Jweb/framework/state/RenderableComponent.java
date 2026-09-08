package com.osmig.Jweb.framework.state;

import java.util.Set;

/**
 * A region of the page that can be re-rendered on the server when state
 * changes — a {@link LiveRegion} in practice.
 */
@FunctionalInterface
public interface RenderableComponent {

    /**
     * Renders the component to HTML.
     *
     * @return the HTML string
     */
    String render();

    /**
     * The ids of the states this component reads, or {@code null} when it
     * should re-render on every change.
     */
    default Set<String> dependsOn() {
        return null;
    }

    /** Whether a change to any of {@code changedStateIds} affects this component. */
    default boolean affectedBy(Set<String> changedStateIds) {
        Set<String> deps = dependsOn();
        if (deps == null) return true;
        for (String id : deps) {
            if (changedStateIds.contains(id)) return true;
        }
        return false;
    }
}
