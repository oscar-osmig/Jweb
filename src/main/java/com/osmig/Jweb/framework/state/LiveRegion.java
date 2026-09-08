package com.osmig.Jweb.framework.state;

import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A region of the page that re-renders on the server when the state it
 * depends on changes — what {@code live(state, s -> element)} returns.
 *
 * <p>On render the body's root element gets a {@code data-live="live_N"}
 * attribute (a non-element body is wrapped in a {@code span}). The region
 * registers with the page's {@link StateManager.StateContext}; when an event
 * changes one of its states, {@code JWebSocketHandler} re-renders the body
 * with the new values and ships the HTML as a {@code domUpdate} patch, which
 * the runtime morphs into the existing element — unchanged nodes, focus,
 * scroll position and in-progress input all survive.</p>
 *
 * <p>Regions with no dependencies re-render on every change (the legacy
 * {@code useComponent} behaviour).</p>
 */
public final class LiveRegion implements jweb.Element, RenderableComponent {

    // Ids for regions rendered outside any context (static export): unique
    // per JVM, never patched.
    private static final AtomicLong DETACHED = new AtomicLong();

    private final String id;
    private final Set<String> dependencies;
    private final Supplier<? extends jweb.Element> body;
    private final boolean idAttribute;

    private LiveRegion(String explicitId, Supplier<? extends jweb.Element> body, State<?>... deps) {
        this.body = body;
        this.idAttribute = explicitId != null;
        Set<String> ids = new LinkedHashSet<>();
        for (State<?> dep : deps) ids.add(dep.getId());
        this.dependencies = ids.isEmpty() ? null : Set.copyOf(ids);

        StateManager.StateContext context = StateManager.getContext();
        if (explicitId != null) {
            this.id = explicitId;
        } else if (context != null) {
            this.id = context.nextLiveId();
        } else {
            this.id = "live_x" + DETACHED.incrementAndGet();
        }
        if (context != null) {
            context.registerComponent(id, this);
        }
    }

    /**
     * A region that re-renders whenever one of {@code deps} changes; with no
     * deps, on every change.
     */
    public static LiveRegion of(Supplier<? extends jweb.Element> body, State<?>... deps) {
        return new LiveRegion(null, body, deps);
    }

    /**
     * A region with an author-chosen DOM id — the {@code useComponent}
     * shape: renders a {@code <div id=...>} wrapper around the body and
     * re-renders on every change.
     */
    public static LiveRegion withId(String id, Supplier<? extends jweb.Element> body) {
        return new LiveRegion(id, body);
    }

    /** The region id (the {@code data-live} attribute value). */
    public String id() {
        return id;
    }

    @Override
    public Set<String> dependsOn() {
        return dependencies;
    }

    @Override
    public VNode toVNode() {
        if (idAttribute) {
            Map<String, String> attrs = new java.util.LinkedHashMap<>();
            attrs.put("id", id);
            attrs.put("data-live", id);
            return VElement.of("div", attrs, List.of(bodyNode()));
        }
        VNode node = bodyNode();
        if (node instanceof VElement element) {
            return element.withAttribute("data-live", id);
        }
        return VElement.of("span", Map.of("data-live", id), List.of(node));
    }

    private VNode bodyNode() {
        jweb.Element element = body.get();
        return element == null ? VElement.of("span") : element.toVNode();
    }

    /** The region's current HTML — what a domUpdate patch carries. */
    @Override
    public String render() {
        return toVNode().toHtml();
    }
}
