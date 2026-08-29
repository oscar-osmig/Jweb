package com.osmig.Jweb.framework.elements;

/**
 * Helper methods for working with HTML dialog elements.
 *
 * Provides JavaScript snippets for common dialog operations:
 * - showModal() - Show dialog as modal (with backdrop)
 * - show() - Show dialog as non-modal
 * - close() - Close dialog
 * - close(returnValue) - Close dialog with a return value
 *
 * <p>Example usage:</p>
 * <pre>
 * // Dialog element
 * dialog(attrs().id("confirm-dialog"),
 *     h2("Confirm"),
 *     p("Are you sure?"),
 *     button(attrs().onclick(DialogHelper.close("confirm-dialog")), "Cancel"),
 *     button(attrs().onclick(DialogHelper.close("confirm-dialog", "confirmed")), "OK")
 * )
 *
 * // Button to open modal
 * button(attrs().onclick(DialogHelper.showModal("confirm-dialog")), "Open Dialog")
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.el.DialogHelper} — shorter import, same behavior. Existing code keeps working.
 */
@Deprecated
public class DialogHelper {
    protected DialogHelper() {}

    /**
     * Generates JavaScript to show a dialog as a modal (with backdrop).
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript code to show the dialog
     */
    public static com.osmig.Jweb.framework.js.Actions.Action showModal(String dialogId) {
        return () -> String.format("document.getElementById('%s').showModal()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to show a dialog as non-modal (without backdrop).
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript code to show the dialog
     */
    public static com.osmig.Jweb.framework.js.Actions.Action show(String dialogId) {
        return () -> String.format("document.getElementById('%s').show()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to close a dialog.
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript code to close the dialog
     */
    public static com.osmig.Jweb.framework.js.Actions.Action close(String dialogId) {
        return () -> String.format("document.getElementById('%s').close()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to close a dialog with a return value.
     * The return value can be accessed via dialog.returnValue.
     *
     * @param dialogId the ID of the dialog element
     * @param returnValue the return value to set
     * @return an Action: JavaScript code to close the dialog with return value
     */
    public static com.osmig.Jweb.framework.js.Actions.Action close(String dialogId, String returnValue) {
        return () -> String.format("document.getElementById('%s').close('%s')",
            escapeId(dialogId), escapeValue(returnValue));
    }

    /**
     * Generates JavaScript to toggle a dialog's modal state.
     * Opens if closed, closes if open.
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript code to toggle the dialog
     */
    public static com.osmig.Jweb.framework.js.Actions.Action toggle(String dialogId) {
        return () -> String.format(
            "(function(d){d.open?d.close():d.showModal()})(document.getElementById('%s'))",
            escapeId(dialogId)
        );
    }

    /**
     * Generates JavaScript to close a dialog on backdrop click.
     * Attach this to the dialog's onclick event.
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript code to close on backdrop click
     */
    public static com.osmig.Jweb.framework.js.Actions.Action closeOnBackdropClick(String dialogId) {
        return () -> String.format(
            "if(event.target.id==='%s')this.close()",
            escapeId(dialogId)
        );
    }

    /**
     * Generates JavaScript to get the dialog's return value.
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript expression that evaluates to the return value
     */
    public static com.osmig.Jweb.framework.js.Actions.Action getReturnValue(String dialogId) {
        return () -> String.format("document.getElementById('%s').returnValue", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to check if a dialog is open.
     *
     * @param dialogId the ID of the dialog element
     * @return an Action: JavaScript expression that evaluates to true if dialog is open
     */
    public static com.osmig.Jweb.framework.js.Actions.Action isOpen(String dialogId) {
        return () -> String.format("document.getElementById('%s').open", escapeId(dialogId));
    }

    // ==================== Utility Methods ====================

    private static String escapeId(String id) {
        if (id == null) return "";
        // Escape single quotes and backslashes for JavaScript string safety
        return id.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static String escapeValue(String value) {
        if (value == null) return "";
        // Escape single quotes and backslashes for JavaScript string safety
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
