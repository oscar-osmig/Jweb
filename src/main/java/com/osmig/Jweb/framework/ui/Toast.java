package com.osmig.Jweb.framework.ui;

import com.osmig.Jweb.framework.core.Element;

import java.util.ArrayList;
import java.util.List;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/**
 * Toast notification system for displaying temporary messages.
 *
 * <p>Setup - Add container and script to your page:</p>
 * <pre>
 * body(
 *     // ... your content ...
 *     Toast.container(),
 *     Toast.script()
 * )
 * </pre>
 *
 * <p>Show toasts from JavaScript:</p>
 * <pre>
 * // From Java-generated JS
 * button(attrs().onClick("Toast.success('Item saved!')"), text("Save"))
 *
 * // Different types
 * Toast.showJs("success", "Operation completed!")
 * Toast.showJs("error", "Something went wrong")
 * Toast.showJs("warning", "Please check your input")
 * Toast.showJs("info", "New update available")
 *
 * // With options
 * Toast.showJs("success", "Saved!", 5000)  // 5 second duration
 * </pre>
 *
 * <p>Server-side toast (shown on page load):</p>
 * <pre>
 * Toast.initial(Type.SUCCESS, "Welcome back!")
 * </pre>
 */
public final class Toast {

    private Toast() {}

    // ==================== Types ====================

    public enum Type {
        SUCCESS("#16a34a", "#dcfce7", "#15803d", "\u2713"),  // checkmark
        ERROR("#dc2626", "#fee2e2", "#b91c1c", "\u2717"),    // x mark
        WARNING("#ca8a04", "#fef3c7", "#a16207", "\u26A0"),  // warning triangle
        INFO("#0891b2", "#cffafe", "#0e7490", "\u2139");     // info circle

        final String iconColor;
        final String bgColor;
        final String borderColor;
        final String icon;

        Type(String iconColor, String bgColor, String borderColor, String icon) {
            this.iconColor = iconColor;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.icon = icon;
        }
    }

    public enum Position {
        TOP_RIGHT("top:1rem;right:1rem"),
        TOP_LEFT("top:1rem;left:1rem"),
        TOP_CENTER("top:1rem;left:50%;transform:translateX(-50%)"),
        BOTTOM_RIGHT("bottom:1rem;right:1rem"),
        BOTTOM_LEFT("bottom:1rem;left:1rem"),
        BOTTOM_CENTER("bottom:1rem;left:50%;transform:translateX(-50%)");

        final String css;

        Position(String css) {
            this.css = css;
        }
    }

    // ==================== Container ====================

    /**
     * Creates the toast container element.
     * Add this once to your page, typically at the end of the body.
     */
    public static Element container() {
        return container(Position.TOP_RIGHT);
    }

    /**
     * Creates the toast container at a specific position.
     */
    public static Element container(Position position) {
        String[] parts = position.css.split(";");
        var containerAttrs = attrs().id("toast-container");
        var style = containerAttrs.style()
                .position(() -> "fixed")
                .prop("z-index", "9999")
                .flex()
                .flexDirection(() -> "column")
                .gap(rem(0.5));

        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                style.prop(kv[0].trim(), kv[1].trim());
            }
        }

        return div(style.done());
    }

    // ==================== Script ====================

    /**
     * Returns the JavaScript required for the toast system.
     * Include this once in your page.
     */
    public static Element toastScript() {
        return inlineScript(
            "var Toast={" +
            "container:function(){return document.getElementById('toast-container')}," +
            "show:function(type,msg,duration){" +
            "var t=this,c=t.container();if(!c)return;" +
            "duration=duration||3000;" +
            "var colors={success:['#16a34a','#dcfce7','#15803d','\\u2713']," +
            "error:['#dc2626','#fee2e2','#b91c1c','\\u2717']," +
            "warning:['#ca8a04','#fef3c7','#a16207','\\u26A0']," +
            "info:['#0891b2','#cffafe','#0e7490','\\u2139']};" +
            "var col=colors[type]||colors.info;" +
            "var el=document.createElement('div');" +
            "el.style.cssText='display:flex;align-items:center;gap:0.75rem;padding:0.75rem 1rem;" +
            "background:'+col[1]+';border:1px solid '+col[2]+';border-radius:8px;" +
            "box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);min-width:250px;max-width:400px;" +
            "animation:toastIn 0.3s ease-out;font-size:0.875rem;color:#374151';" +
            "var icon=document.createElement('span');" +
            "icon.textContent=col[3];" +
            "icon.style.cssText='color:'+col[0]+';font-size:1.25rem;flex-shrink:0';" +
            "var text=document.createElement('span');" +
            "text.textContent=msg;text.style.flex='1';" +
            "var close=document.createElement('button');" +
            "close.textContent='\\u00D7';" +
            "close.style.cssText='background:none;border:none;font-size:1.25rem;color:#6b7280;" +
            "cursor:pointer;padding:0;line-height:1';" +
            "close.onclick=function(){t.dismiss(el)};" +
            "el.appendChild(icon);el.appendChild(text);el.appendChild(close);" +
            "c.appendChild(el);" +
            "if(duration>0)setTimeout(function(){t.dismiss(el)},duration);" +
            "return el}," +
            "dismiss:function(el){" +
            "if(!el||!el.parentNode)return;" +
            "el.style.animation='toastOut 0.2s ease-in forwards';" +
            "setTimeout(function(){if(el.parentNode)el.parentNode.removeChild(el)},200)}," +
            "success:function(msg,dur){return this.show('success',msg,dur)}," +
            "error:function(msg,dur){return this.show('error',msg,dur)}," +
            "warning:function(msg,dur){return this.show('warning',msg,dur)}," +
            "info:function(msg,dur){return this.show('info',msg,dur)}," +
            "promise:function(promise,msgs){" +
            "var t=this,el=t.show('info',msgs.loading||'Loading...',0);" +
            "promise.then(function(r){t.dismiss(el);t.success(msgs.success||'Success!');return r})" +
            ".catch(function(e){t.dismiss(el);t.error(msgs.error||'Error occurred');throw e});return promise}" +
            "};"
        );
    }

    /**
     * Returns CSS keyframes for toast animations.
     * Include this in your styles.
     */
    public static Element styles() {
        return style(
            "@keyframes toastIn{from{opacity:0;transform:translateX(100%)}to{opacity:1;transform:translateX(0)}}" +
            "@keyframes toastOut{from{opacity:1;transform:translateX(0)}to{opacity:0;transform:translateX(100%)}}"
        );
    }

    /**
     * Returns both styles and script combined.
     */
    public static Element init() {
        return fragment(styles(), toastScript());
    }

    /**
     * Returns container, styles, and script - everything needed.
     */
    public static Element setup() {
        return setup(Position.TOP_RIGHT);
    }

    /**
     * Returns container, styles, and script at a specific position.
     */
    public static Element setup(Position position) {
        return fragment(container(position), styles(), toastScript());
    }

    // ==================== JavaScript Helpers ====================

    /**
     * Returns JavaScript to show a toast.
     *
     * @param type the toast type (success, error, warning, info)
     * @param message the message to display
     * @return JavaScript code string
     */
    public static String showJs(String type, String message) {
        return "Toast." + type + "('" + escapeJs(message) + "')";
    }

    /**
     * Returns JavaScript to show a toast with custom duration.
     *
     * @param type the toast type
     * @param message the message to display
     * @param durationMs duration in milliseconds (0 = no auto-dismiss)
     * @return JavaScript code string
     */
    public static String showJs(String type, String message, int durationMs) {
        return "Toast." + type + "('" + escapeJs(message) + "'," + durationMs + ")";
    }

    /**
     * Returns JavaScript to show a success toast.
     */
    public static String successJs(String message) {
        return showJs("success", message);
    }

    /**
     * Returns JavaScript to show an error toast.
     */
    public static String errorJs(String message) {
        return showJs("error", message);
    }

    /**
     * Returns JavaScript to show a warning toast.
     */
    public static String warningJs(String message) {
        return showJs("warning", message);
    }

    /**
     * Returns JavaScript to show an info toast.
     */
    public static String infoJs(String message) {
        return showJs("info", message);
    }

    /**
     * Returns JavaScript for promise-based toast.
     * Shows loading while promise is pending, then success/error based on result.
     *
     * @param promiseExpr JavaScript expression that evaluates to a Promise
     * @param loadingMsg message shown while loading
     * @param successMsg message shown on success
     * @param errorMsg message shown on error
     */
    public static String promiseJs(String promiseExpr, String loadingMsg, String successMsg, String errorMsg) {
        return "Toast.promise(" + promiseExpr + ",{loading:'" + escapeJs(loadingMsg) +
               "',success:'" + escapeJs(successMsg) + "',error:'" + escapeJs(errorMsg) + "'})";
    }

    // ==================== Initial Toasts ====================

    /**
     * Creates a toast that shows immediately when the page loads.
     * Useful for showing server-side messages (e.g., after redirect).
     */
    public static Element initial(Type type, String message) {
        return initial(type, message, 3000);
    }

    /**
     * Creates a toast that shows immediately when the page loads with custom duration.
     */
    public static Element initial(Type type, String message, int durationMs) {
        String typeName = type.name().toLowerCase();
        return inlineScript(
            "document.addEventListener('DOMContentLoaded',function(){" +
            "Toast." + typeName + "('" + escapeJs(message) + "'," + durationMs + ")});"
        );
    }

    /**
     * Creates multiple initial toasts.
     */
    public static Element initial(ToastMessage... messages) {
        StringBuilder sb = new StringBuilder("document.addEventListener('DOMContentLoaded',function(){");
        for (ToastMessage msg : messages) {
            sb.append("Toast.").append(msg.type.name().toLowerCase())
              .append("('").append(escapeJs(msg.message)).append("',").append(msg.duration).append(");");
        }
        sb.append("});");
        return inlineScript(sb.toString());
    }

    /**
     * Message configuration for initial toasts.
     */
    public record ToastMessage(Type type, String message, int duration) {
        public ToastMessage(Type type, String message) {
            this(type, message, 3000);
        }

        public static ToastMessage success(String message) {
            return new ToastMessage(Type.SUCCESS, message);
        }

        public static ToastMessage error(String message) {
            return new ToastMessage(Type.ERROR, message);
        }

        public static ToastMessage warning(String message) {
            return new ToastMessage(Type.WARNING, message);
        }

        public static ToastMessage info(String message) {
            return new ToastMessage(Type.INFO, message);
        }
    }

    // ==================== Builder ====================

    /**
     * Creates a custom toast builder for advanced configuration.
     *
     * <p>Example:</p>
     * <pre>
     * Toast.builder()
     *     .type(Type.SUCCESS)
     *     .message("Item saved successfully!")
     *     .duration(5000)
     *     .action("Undo", "handleUndo()")
     *     .build()
     * </pre>
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Type type = Type.INFO;
        private String message = "";
        private int duration = 3000;
        private String actionLabel;
        private String actionJs;
        private String id;

        public Builder type(Type type) { this.type = type; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder duration(int ms) { this.duration = ms; return this; }
        public Builder persistent() { this.duration = 0; return this; }
        public Builder action(String label, String onClickJs) {
            this.actionLabel = label;
            this.actionJs = onClickJs;
            return this;
        }
        public Builder id(String id) { this.id = id; return this; }

        /**
         * Builds JavaScript to show this toast.
         */
        public String build() {
            if (actionLabel != null) {
                // Custom toast with action button - more complex JS
                return "(function(){var el=Toast.show('" + type.name().toLowerCase() + "','" +
                       escapeJs(message) + "'," + duration + ");" +
                       "var btn=document.createElement('button');" +
                       "btn.textContent='" + escapeJs(actionLabel) + "';" +
                       "btn.style.cssText='background:" + type.iconColor + ";color:white;" +
                       "border:none;padding:0.25rem 0.5rem;border-radius:4px;font-size:0.75rem;cursor:pointer';" +
                       "btn.onclick=function(){" + actionJs + ";Toast.dismiss(el)};" +
                       "el.insertBefore(btn,el.lastChild)})()";
            }
            return showJs(type.name().toLowerCase(), message, duration);
        }

        /**
         * Builds an initial toast element (shows on page load).
         */
        public Element buildInitial() {
            return inlineScript("document.addEventListener('DOMContentLoaded',function(){" + build() + "});");
        }
    }

    // ==================== Utilities ====================

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
