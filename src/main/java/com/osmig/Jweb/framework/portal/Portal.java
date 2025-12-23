package com.osmig.Jweb.framework.portal;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portal for rendering elements outside their parent DOM hierarchy.
 *
 * <p>Portals allow you to render children into a different location in the DOM tree,
 * which is useful for modals, tooltips, dropdowns, and other overlay components
 * that need to visually "break out" of their container.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // In your layout - define where portals will render
 * body(
 *     div(id("app"),
 *         nav(...),
 *         main(children)
 *     ),
 *     Portal.outlet("modals"),    // Modals render here
 *     Portal.outlet("tooltips")   // Tooltips render here
 * )
 *
 * // In your component - send content to portal
 * Portal.to("modals",
 *     div(class_("modal"),
 *         h2("Confirm Action"),
 *         p("Are you sure?"),
 *         button(onClick(e -> close()), "OK")
 *     )
 * )
 * }</pre>
 *
 * <h2>Modal Example</h2>
 * <pre>{@code
 * public class Modal implements Template {
 *     private final String title;
 *     private final Element content;
 *     private final Runnable onClose;
 *
 *     public Element render() {
 *         return Portal.to("modals",
 *             div(class_("modal-backdrop"), onClick(e -> onClose.run()),
 *                 div(class_("modal-content"), onClick(e -> e.stopPropagation()),
 *                     h2(title),
 *                     content,
 *                     button(onClick(e -> onClose.run()), "Close")
 *                 )
 *             )
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h2>Conditional Portal</h2>
 * <pre>{@code
 * when(showModal).then(
 *     Portal.to("modals", myModal())
 * ).end()
 * }</pre>
 *
 * @see Element
 */
public final class Portal {

    // Thread-local storage for portal content during a single render
    private static final ThreadLocal<Map<String, List<Element>>> portalContent =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    private Portal() {}

    // ==================== Rendering to Portals ====================

    /**
     * Sends content to a named portal outlet.
     *
     * <p>The content will be rendered at the Portal.outlet() location with the matching name.</p>
     *
     * @param name the portal outlet name
     * @param content the content to render in the portal
     * @return an empty element (the content renders at the outlet)
     */
    public static Element to(String name, Element content) {
        portalContent.get()
            .computeIfAbsent(name, k -> new ArrayList<>())
            .add(content);

        // Return empty - content renders at outlet
        return () -> new VFragment(List.of());
    }

    /**
     * Sends multiple elements to a portal.
     *
     * @param name the portal outlet name
     * @param content the elements to render
     * @return an empty element
     */
    public static Element to(String name, Element... content) {
        List<Element> list = portalContent.get()
            .computeIfAbsent(name, k -> new ArrayList<>());
        Collections.addAll(list, content);
        return () -> new VFragment(List.of());
    }

    // ==================== Portal Outlets ====================

    /**
     * Creates a portal outlet where content will be rendered.
     *
     * <p>Place this in your layout where you want portal content to appear.</p>
     *
     * @param name the unique name for this outlet
     * @return an element that renders all content sent to this portal
     */
    public static Element outlet(String name) {
        return new PortalOutlet(name);
    }

    /**
     * Creates a portal outlet wrapped in a container div.
     *
     * @param name the unique name for this outlet
     * @param id the HTML id for the container
     * @return a div element containing the portal content
     */
    public static Element outlet(String name, String id) {
        return new PortalOutletWithContainer(name, id);
    }

    // ==================== Cleanup ====================

    /**
     * Clears all portal content for the current thread.
     * Called automatically at the end of request processing.
     */
    public static void clear() {
        portalContent.get().clear();
    }

    /**
     * Clears content for a specific portal.
     *
     * @param name the portal name to clear
     */
    public static void clear(String name) {
        portalContent.get().remove(name);
    }

    // ==================== Internal Classes ====================

    private static class PortalOutlet implements Element {
        private final String name;

        PortalOutlet(String name) {
            this.name = name;
        }

        @Override
        public VNode toVNode() {
            List<Element> content = portalContent.get().get(name);
            if (content == null || content.isEmpty()) {
                return new VFragment(List.of());
            }

            List<VNode> nodes = content.stream()
                .map(Element::toVNode)
                .toList();

            return new VFragment(nodes);
        }
    }

    private static class PortalOutletWithContainer implements Element {
        private final String name;
        private final String id;

        PortalOutletWithContainer(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public VNode toVNode() {
            List<Element> content = portalContent.get().get(name);
            List<VNode> children = (content == null || content.isEmpty())
                ? List.of()
                : content.stream().map(Element::toVNode).toList();

            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("id", id);

            return VElement.of("div", attrs, children);
        }
    }

    // ==================== Convenience Methods ====================

    /**
     * Creates a modal portal (shortcut for Portal.to("modals", ...)).
     *
     * @param content the modal content
     * @return an empty element (content renders at modal outlet)
     */
    public static Element modal(Element content) {
        return to("modals", content);
    }

    /**
     * Creates a tooltip portal.
     *
     * @param content the tooltip content
     * @return an empty element
     */
    public static Element tooltip(Element content) {
        return to("tooltips", content);
    }

    /**
     * Creates a dropdown portal.
     *
     * @param content the dropdown content
     * @return an empty element
     */
    public static Element dropdown(Element content) {
        return to("dropdowns", content);
    }

    /**
     * Creates a toast/notification portal.
     *
     * @param content the toast content
     * @return an empty element
     */
    public static Element toast(Element content) {
        return to("toasts", content);
    }
}
