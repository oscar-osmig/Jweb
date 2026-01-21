package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Clipboard API for copy/paste operations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSClipboard.*;
 *
 * // Copy text to clipboard
 * copyText(variable("textToCopy"))
 *     .then(callback().log(str("Copied!")))
 *     .catch_(callback("err").log(variable("err")))
 *
 * // Read text from clipboard
 * readText()
 *     .then(callback("text").call("handlePaste", variable("text")))
 *
 * // Copy element's text content
 * copyElementText("myElement")
 * </pre>
 */
public final class JSClipboard {
    private JSClipboard() {}

    // ==================== Write Operations ====================

    /** Copies text to clipboard (returns Promise) */
    public static ClipboardPromise copyText(Val text) {
        return new ClipboardPromise("navigator.clipboard.writeText(" + text.js() + ")");
    }

    /** Copies text to clipboard */
    public static ClipboardPromise copyText(String text) {
        return new ClipboardPromise("navigator.clipboard.writeText('" + JS.esc(text) + "')");
    }

    /** Copies element's text content to clipboard */
    public static ClipboardPromise copyElementText(String elementId) {
        return new ClipboardPromise("navigator.clipboard.writeText(document.getElementById('" +
            JS.esc(elementId) + "').textContent)");
    }

    /** Copies element's value (for inputs) to clipboard */
    public static ClipboardPromise copyElementValue(String elementId) {
        return new ClipboardPromise("navigator.clipboard.writeText(document.getElementById('" +
            JS.esc(elementId) + "').value)");
    }

    /** Writes ClipboardItem (for images, etc.) */
    public static ClipboardPromise write(Val clipboardItems) {
        return new ClipboardPromise("navigator.clipboard.write(" + clipboardItems.js() + ")");
    }

    // ==================== Read Operations ====================

    /** Reads text from clipboard (returns Promise) */
    public static ClipboardPromise readText() {
        return new ClipboardPromise("navigator.clipboard.readText()");
    }

    /** Reads clipboard items (for images, etc.) */
    public static ClipboardPromise read() {
        return new ClipboardPromise("navigator.clipboard.read()");
    }

    // ==================== Clipboard Item ====================

    /** Creates a ClipboardItem for writing */
    public static Val clipboardItem(String mimeType, Val blob) {
        return new Val("new ClipboardItem({'" + JS.esc(mimeType) + "':" + blob.js() + "})");
    }

    // ==================== Fallback (older browsers) ====================

    /** Fallback copy using execCommand (deprecated but wider support) */
    public static Val copyFallback(Val text) {
        return new Val("(function(){var t=document.createElement('textarea');t.value=" + text.js() +
            ";t.style.position='fixed';t.style.opacity='0';document.body.appendChild(t);" +
            "t.select();document.execCommand('copy');document.body.removeChild(t)}())");
    }

    /** Copy with fallback - tries modern API first, falls back to execCommand */
    public static Val copyWithFallback(Val text) {
        return new Val("(navigator.clipboard?navigator.clipboard.writeText(" + text.js() +
            "):Promise.resolve((function(){var t=document.createElement('textarea');t.value=" + text.js() +
            ";t.style.position='fixed';t.style.opacity='0';document.body.appendChild(t);" +
            "t.select();document.execCommand('copy');document.body.removeChild(t)}())))");
    }

    // ==================== Permission Check ====================

    /** Checks clipboard read permission */
    public static Val canRead() {
        return new Val("navigator.permissions.query({name:'clipboard-read'})");
    }

    /** Checks clipboard write permission */
    public static Val canWrite() {
        return new Val("navigator.permissions.query({name:'clipboard-write'})");
    }

    // ==================== Promise Builder ====================

    public static class ClipboardPromise {
        private final String base;
        private final StringBuilder chain = new StringBuilder();

        ClipboardPromise(String base) { this.base = base; }

        public ClipboardPromise then(Func handler) {
            chain.append(".then(").append(handler.toExpr()).append(")");
            return this;
        }

        public ClipboardPromise catch_(Func handler) {
            chain.append(".catch(").append(handler.toExpr()).append(")");
            return this;
        }

        public ClipboardPromise finally_(Func handler) {
            chain.append(".finally(").append(handler.toExpr()).append(")");
            return this;
        }

        public Val toVal() {
            return new Val(base + chain);
        }

        public String build() { return toVal().js(); }
    }
}
