package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * HTML Popover API elements and attributes.
 *
 * <p>The Popover API provides a declarative way to create popover content
 * that appears above other page content. Popovers are automatically managed
 * by the browser (positioning, stacking, focus, dismissal).</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.PopoverElements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Auto popover (closes on click outside or Escape)
 * div(popover("auto"), id("my-popover"),
 *     p("This is popover content")
 * ),
 * button(popoverTarget("my-popover"), "Toggle Popover")
 *
 * // Manual popover (must be explicitly closed)
 * div(popover("manual"), id("menu"),
 *     nav(a(href("/home"), "Home"), a(href("/about"), "About"))
 * ),
 * button(popoverTarget("menu"), popoverTargetAction("show"), "Open Menu"),
 * button(popoverTarget("menu"), popoverTargetAction("hide"), "Close Menu")
 * }</pre>
 */
public final class PopoverElements {
    private PopoverElements() {}

    // ==================== Popover Attributes ====================

    /**
     * Creates a popover attribute.
     *
     * @param type "auto" (light-dismiss) or "manual" (explicit close only)
     * @return the popover Attr
     */
    public static Attr popover(String type) {
        return new Attr("popover", type);
    }

    /**
     * Creates a popover attribute with auto type (default).
     *
     * @return the popover Attr
     */
    public static Attr popover() {
        return new Attr("popover", "");
    }

    /**
     * Creates a popovertarget attribute that links a button to its popover.
     *
     * @param targetId the ID of the popover element
     * @return the popovertarget Attr
     */
    public static Attr popoverTarget(String targetId) {
        return new Attr("popovertarget", targetId);
    }

    /**
     * Creates a popovertargetaction attribute to control button behavior.
     *
     * @param action "toggle" (default), "show", or "hide"
     * @return the popovertargetaction Attr
     */
    public static Attr popoverTargetAction(String action) {
        return new Attr("popovertargetaction", action);
    }

    // ==================== Popover Elements ====================

    /**
     * Creates a div configured as an auto popover.
     *
     * @param id the popover ID
     * @param children the popover content
     * @return a Tag with popover attribute
     */
    public static Tag autoPopover(String id, Object... children) {
        return Tag.create("div", new Attr("id", id), new Attr("popover", "auto"))
            .children(Tag.toVNodes(children).stream()
                .map(vn -> (com.osmig.Jweb.framework.core.Element) () -> vn)
                .toArray(com.osmig.Jweb.framework.core.Element[]::new));
    }

    /**
     * Creates a div configured as a manual popover.
     *
     * @param id the popover ID
     * @param children the popover content
     * @return a Tag with popover="manual" attribute
     */
    public static Tag manualPopover(String id, Object... children) {
        return Tag.create("div", new Attr("id", id), new Attr("popover", "manual"))
            .children(Tag.toVNodes(children).stream()
                .map(vn -> (com.osmig.Jweb.framework.core.Element) () -> vn)
                .toArray(com.osmig.Jweb.framework.core.Element[]::new));
    }

    /**
     * Creates a button that toggles a popover.
     *
     * @param targetId the popover ID to toggle
     * @param children button content
     * @return a Tag button
     */
    public static Tag popoverToggleButton(String targetId, Object... children) {
        return Tag.create("button", new Attr("popovertarget", targetId), children);
    }

    /**
     * Creates a button that shows a popover.
     *
     * @param targetId the popover ID to show
     * @param children button content
     * @return a Tag button
     */
    public static Tag popoverShowButton(String targetId, Object... children) {
        return Tag.create("button",
            new Attr("popovertarget", targetId),
            new Attr("popovertargetaction", "show"),
            children);
    }

    /**
     * Creates a button that hides a popover.
     *
     * @param targetId the popover ID to hide
     * @param children button content
     * @return a Tag button
     */
    public static Tag popoverHideButton(String targetId, Object... children) {
        return Tag.create("button",
            new Attr("popovertarget", targetId),
            new Attr("popovertargetaction", "hide"),
            children);
    }

    // ==================== Popover JS Helpers ====================

    /**
     * Generates JS to show a popover: document.getElementById(id).showPopover()
     *
     * @param id the popover element ID
     * @return the JavaScript string
     */
    public static String showPopover(String id) {
        return "document.getElementById('" + id + "').showPopover()";
    }

    /**
     * Generates JS to hide a popover: document.getElementById(id).hidePopover()
     *
     * @param id the popover element ID
     * @return the JavaScript string
     */
    public static String hidePopover(String id) {
        return "document.getElementById('" + id + "').hidePopover()";
    }

    /**
     * Generates JS to toggle a popover: document.getElementById(id).togglePopover()
     *
     * @param id the popover element ID
     * @return the JavaScript string
     */
    public static String togglePopover(String id) {
        return "document.getElementById('" + id + "').togglePopover()";
    }
}
