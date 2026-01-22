package com.osmig.Jweb.framework.elements;

/**
 * Helper methods for working with HTML dialog elements.
 *
 * Provides JavaScript snippets for common dialog operations:
 * - showModal() - Show dialog as modal (with backdrop)
 * - show() - Show dialog as non-modal
 * - close() - Close dialog
 * - close(returnValue) - Close dialog with return value
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
 */
public final class DialogHelper {
    private DialogHelper() {}

    /**
     * Generates JavaScript to show a dialog as a modal (with backdrop).
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript code to show the dialog
     */
    public static String showModal(String dialogId) {
        return String.format("document.getElementById('%s').showModal()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to show a dialog as non-modal (without backdrop).
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript code to show the dialog
     */
    public static String show(String dialogId) {
        return String.format("document.getElementById('%s').show()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to close a dialog.
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript code to close the dialog
     */
    public static String close(String dialogId) {
        return String.format("document.getElementById('%s').close()", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to close a dialog with a return value.
     * The return value can be accessed via dialog.returnValue.
     *
     * @param dialogId the ID of the dialog element
     * @param returnValue the return value to set
     * @return JavaScript code to close the dialog with return value
     */
    public static String close(String dialogId, String returnValue) {
        return String.format("document.getElementById('%s').close('%s')",
            escapeId(dialogId), escapeValue(returnValue));
    }

    /**
     * Generates JavaScript to toggle a dialog's modal state.
     * Opens if closed, closes if open.
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript code to toggle the dialog
     */
    public static String toggle(String dialogId) {
        return String.format(
            "(function(d){d.open?d.close():d.showModal()})(document.getElementById('%s'))",
            escapeId(dialogId)
        );
    }

    /**
     * Generates JavaScript to close a dialog on backdrop click.
     * Attach this to the dialog's onclick event.
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript code to close on backdrop click
     */
    public static String closeOnBackdropClick(String dialogId) {
        return String.format(
            "if(event.target.id==='%s')this.close()",
            escapeId(dialogId)
        );
    }

    /**
     * Generates JavaScript to get the dialog's return value.
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript expression that evaluates to the return value
     */
    public static String getReturnValue(String dialogId) {
        return String.format("document.getElementById('%s').returnValue", escapeId(dialogId));
    }

    /**
     * Generates JavaScript to check if a dialog is open.
     *
     * @param dialogId the ID of the dialog element
     * @return JavaScript expression that evaluates to true if dialog is open
     */
    public static String isOpen(String dialogId) {
        return String.format("document.getElementById('%s').open", escapeId(dialogId));
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
