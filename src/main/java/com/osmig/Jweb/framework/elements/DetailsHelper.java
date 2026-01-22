package com.osmig.Jweb.framework.elements;

/**
 * Helper methods for working with HTML details elements.
 *
 * Provides JavaScript snippets for common details operations:
 * - open() - Expand details element
 * - close() - Collapse details element
 * - toggle() - Toggle open/closed state
 *
 * <p>Example usage:</p>
 * <pre>
 * // Details with programmatic control
 * details(attrs().id("faq-1"),
 *     summary("What is JWeb?"),
 *     p("JWeb is a pure Java web framework...")
 * )
 *
 * // Buttons to control details
 * button(attrs().onclick(DetailsHelper.open("faq-1")), "Expand")
 * button(attrs().onclick(DetailsHelper.close("faq-1")), "Collapse")
 * button(attrs().onclick(DetailsHelper.toggle("faq-1")), "Toggle")
 *
 * // Exclusive accordion (only one open at a time)
 * button(attrs().onclick(DetailsHelper.closeAll("faq")), "Collapse All")
 * </pre>
 */
public final class DetailsHelper {
    private DetailsHelper() {}

    /**
     * Generates JavaScript to open (expand) a details element.
     *
     * @param detailsId the ID of the details element
     * @return JavaScript code to open the details
     */
    public static String open(String detailsId) {
        return String.format("document.getElementById('%s').open=true", escapeId(detailsId));
    }

    /**
     * Generates JavaScript to close (collapse) a details element.
     *
     * @param detailsId the ID of the details element
     * @return JavaScript code to close the details
     */
    public static String close(String detailsId) {
        return String.format("document.getElementById('%s').open=false", escapeId(detailsId));
    }

    /**
     * Generates JavaScript to toggle a details element's open state.
     *
     * @param detailsId the ID of the details element
     * @return JavaScript code to toggle the details
     */
    public static String toggle(String detailsId) {
        return String.format(
            "(function(d){d.open=!d.open})(document.getElementById('%s'))",
            escapeId(detailsId)
        );
    }

    /**
     * Generates JavaScript to check if a details element is open.
     *
     * @param detailsId the ID of the details element
     * @return JavaScript expression that evaluates to true if details is open
     */
    public static String isOpen(String detailsId) {
        return String.format("document.getElementById('%s').open", escapeId(detailsId));
    }

    /**
     * Generates JavaScript to open one details element and close all others with a shared name.
     * Useful for creating exclusive accordions.
     *
     * @param detailsId the ID of the details element to open
     * @param groupName the name attribute value shared by accordion items
     * @return JavaScript code to implement exclusive accordion behavior
     */
    public static String openExclusive(String detailsId, String groupName) {
        return String.format(
            "(function(){document.querySelectorAll('details[name=\"%s\"]').forEach(d=>d.open=false);" +
            "document.getElementById('%s').open=true})()",
            escapeName(groupName), escapeId(detailsId)
        );
    }

    /**
     * Generates JavaScript to close all details elements with a given name attribute.
     * Useful for "collapse all" functionality in accordions.
     *
     * @param groupName the name attribute value shared by accordion items
     * @return JavaScript code to close all details in the group
     */
    public static String closeAll(String groupName) {
        return String.format(
            "document.querySelectorAll('details[name=\"%s\"]').forEach(d=>d.open=false)",
            escapeName(groupName)
        );
    }

    /**
     * Generates JavaScript to open all details elements with a given name attribute.
     * Useful for "expand all" functionality in accordions.
     *
     * @param groupName the name attribute value shared by accordion items
     * @return JavaScript code to open all details in the group
     */
    public static String openAll(String groupName) {
        return String.format(
            "document.querySelectorAll('details[name=\"%s\"]').forEach(d=>d.open=true)",
            escapeName(groupName)
        );
    }

    /**
     * Generates JavaScript to close all details elements matching a selector.
     *
     * @param selector CSS selector for details elements
     * @return JavaScript code to close all matching details
     */
    public static String closeAllBySelector(String selector) {
        return String.format(
            "document.querySelectorAll('%s').forEach(d=>d.open=false)",
            escapeSelector(selector)
        );
    }

    /**
     * Generates JavaScript to open all details elements matching a selector.
     *
     * @param selector CSS selector for details elements
     * @return JavaScript code to open all matching details
     */
    public static String openAllBySelector(String selector) {
        return String.format(
            "document.querySelectorAll('%s').forEach(d=>d.open=true)",
            escapeSelector(selector)
        );
    }

    // ==================== Utility Methods ====================

    private static String escapeId(String id) {
        if (id == null) return "";
        return id.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static String escapeName(String name) {
        if (name == null) return "";
        // Escape quotes for use in CSS selector
        return name.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeSelector(String selector) {
        if (selector == null) return "";
        // Escape single quotes for JavaScript string
        return selector.replace("\\", "\\\\").replace("'", "\\'");
    }
}
