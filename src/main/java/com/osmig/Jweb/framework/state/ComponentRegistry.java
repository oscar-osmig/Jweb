package com.osmig.Jweb.framework.state;

import com.osmig.Jweb.framework.template.Template;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registry that keeps component instances alive for server-side re-rendering.
 *
 * <p>When a page with interactive components is rendered, the component
 * instances are stored here so they can be re-rendered when state changes.</p>
 */
public final class ComponentRegistry {

    private static final AtomicLong idCounter = new AtomicLong(0);

    // Context ID -> Component ID -> Component instance
    private static final Map<String, Map<String, ComponentEntry>> contextComponents = new ConcurrentHashMap<>();

    private ComponentRegistry() {}

    /**
     * Generates a unique component ID.
     */
    public static String generateId() {
        return "c_" + idCounter.incrementAndGet();
    }

    /**
     * Registers a component for the current context.
     */
    public static void register(String contextId, String componentId, Template component) {
        contextComponents
            .computeIfAbsent(contextId, k -> new ConcurrentHashMap<>())
            .put(componentId, new ComponentEntry(component, componentId));
    }

    /**
     * Gets all components for a context.
     */
    public static Map<String, ComponentEntry> getComponents(String contextId) {
        return contextComponents.get(contextId);
    }

    /**
     * Gets a specific component.
     */
    public static ComponentEntry getComponent(String contextId, String componentId) {
        Map<String, ComponentEntry> components = contextComponents.get(contextId);
        return components != null ? components.get(componentId) : null;
    }

    /**
     * Clears all components for a context.
     */
    public static void clearContext(String contextId) {
        contextComponents.remove(contextId);
    }

    /**
     * Clears all components.
     */
    public static void clearAll() {
        contextComponents.clear();
    }

    /**
     * Entry holding a component and its metadata.
     */
    public static class ComponentEntry {
        private final Template component;
        private final String componentId;
        private String lastHtml;

        public ComponentEntry(Template component, String componentId) {
            this.component = component;
            this.componentId = componentId;
        }

        public Template getComponent() {
            return component;
        }

        public String getComponentId() {
            return componentId;
        }

        public String getLastHtml() {
            return lastHtml;
        }

        public void setLastHtml(String html) {
            this.lastHtml = html;
        }
    }
}
