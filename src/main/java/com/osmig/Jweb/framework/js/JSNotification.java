package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Notification API for browser notifications.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSNotification.*;
 *
 * // Request permission
 * requestPermission()
 *     .then(callback("p").if_(variable("p").eq("granted"), call("showWelcome")))
 *
 * // Show notification
 * notification("New Message")
 *     .body("You have a new message from John")
 *     .icon("/img/icon.png")
 *     .onClick(callback().call("openMessages"))
 *     .build()
 *
 * // Check permission
 * if_(hasPermission(), showNotification())
 * </pre>
 */
public final class JSNotification {
    private JSNotification() {}

    // ==================== Permission ====================

    /** Requests notification permission (returns Promise) */
    public static NotificationPromise requestPermission() {
        return new NotificationPromise("Notification.requestPermission()");
    }

    /** Gets current permission: "granted", "denied", or "default" */
    public static Val permission() {
        return new Val("Notification.permission");
    }

    /** Checks if permission is granted */
    public static Val hasPermission() {
        return new Val("(Notification.permission==='granted')");
    }

    /** Checks if permission is denied */
    public static Val isDenied() {
        return new Val("(Notification.permission==='denied')");
    }

    /** Checks if notifications are supported */
    public static Val isSupported() {
        return new Val("('Notification' in window)");
    }

    // ==================== Notification Creation ====================

    /** Creates a notification builder */
    public static NotificationBuilder notification(String title) {
        return new NotificationBuilder(title);
    }

    /** Creates a notification builder with dynamic title */
    public static NotificationBuilder notification(Val title) {
        return new NotificationBuilder(title);
    }

    /** Closes a notification */
    public static Val close(Val notification) {
        return new Val(notification.js() + ".close()");
    }

    // ==================== Notification Properties ====================

    /** Gets notification title */
    public static Val title(Val notif) { return new Val(notif.js() + ".title"); }

    /** Gets notification body */
    public static Val body(Val notif) { return new Val(notif.js() + ".body"); }

    /** Gets notification tag */
    public static Val tag(Val notif) { return new Val(notif.js() + ".tag"); }

    /** Gets notification data */
    public static Val data(Val notif) { return new Val(notif.js() + ".data"); }

    // ==================== Promise Builder ====================

    public static class NotificationPromise {
        private final String base;
        private final StringBuilder chain = new StringBuilder();

        NotificationPromise(String base) { this.base = base; }

        public NotificationPromise then(Func handler) {
            chain.append(".then(").append(handler.toExpr()).append(")");
            return this;
        }

        public NotificationPromise catch_(Func handler) {
            chain.append(".catch(").append(handler.toExpr()).append(")");
            return this;
        }

        public Val toVal() { return new Val(base + chain); }
        public String build() { return toVal().js(); }
    }

    // ==================== Notification Builder ====================

    public static class NotificationBuilder {
        private final String title;
        private final boolean isDynamic;
        private String body, icon, badge, image, tag, lang, dir;
        private Val bodyExpr, dataExpr;
        private boolean requireInteraction, renotify, silent;
        private Func onClickFunc, onCloseFunc, onErrorFunc, onShowFunc;
        private String onClickCode, onCloseCode, onErrorCode, onShowCode;

        NotificationBuilder(String title) { this.title = "'" + JS.esc(title) + "'"; this.isDynamic = false; }
        NotificationBuilder(Val title) { this.title = title.js(); this.isDynamic = true; }

        public NotificationBuilder body(String body) { this.body = body; return this; }
        public NotificationBuilder body(Val body) { this.bodyExpr = body; return this; }
        public NotificationBuilder icon(String icon) { this.icon = icon; return this; }
        public NotificationBuilder badge(String badge) { this.badge = badge; return this; }
        public NotificationBuilder image(String image) { this.image = image; return this; }
        public NotificationBuilder tag(String tag) { this.tag = tag; return this; }
        public NotificationBuilder lang(String lang) { this.lang = lang; return this; }
        public NotificationBuilder dir(String dir) { this.dir = dir; return this; }
        public NotificationBuilder data(Val data) { this.dataExpr = data; return this; }
        public NotificationBuilder requireInteraction(boolean req) { this.requireInteraction = req; return this; }
        public NotificationBuilder renotify(boolean ren) { this.renotify = ren; return this; }
        public NotificationBuilder silent(boolean sil) { this.silent = sil; return this; }

        public NotificationBuilder onClick(Func handler) { this.onClickFunc = handler; return this; }
        public NotificationBuilder onClick(String code) { this.onClickCode = code; return this; }
        public NotificationBuilder onClose(Func handler) { this.onCloseFunc = handler; return this; }
        public NotificationBuilder onClose(String code) { this.onCloseCode = code; return this; }
        public NotificationBuilder onError(Func handler) { this.onErrorFunc = handler; return this; }
        public NotificationBuilder onError(String code) { this.onErrorCode = code; return this; }
        public NotificationBuilder onShow(Func handler) { this.onShowFunc = handler; return this; }
        public NotificationBuilder onShow(String code) { this.onShowCode = code; return this; }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder("var ").append(varName).append("=").append(buildNotification());
            addEventListeners(sb, varName);
            return new Val(sb.toString());
        }

        /** Builds the notification */
        public Val build() {
            if (hasEventHandlers()) {
                StringBuilder sb = new StringBuilder("(function(){var n=").append(buildNotification()).append(";");
                addEventListeners(sb, "n");
                sb.append("return n}())");
                return new Val(sb.toString());
            }
            return new Val(buildNotification());
        }

        private String buildNotification() {
            StringBuilder sb = new StringBuilder("new Notification(").append(title);
            if (hasOptions()) {
                sb.append(",{");
                boolean first = true;
                if (body != null) { sb.append("body:'").append(JS.esc(body)).append("'"); first = false; }
                else if (bodyExpr != null) { sb.append("body:").append(bodyExpr.js()); first = false; }
                if (icon != null) { if (!first) sb.append(","); sb.append("icon:'").append(JS.esc(icon)).append("'"); first = false; }
                if (badge != null) { if (!first) sb.append(","); sb.append("badge:'").append(JS.esc(badge)).append("'"); first = false; }
                if (image != null) { if (!first) sb.append(","); sb.append("image:'").append(JS.esc(image)).append("'"); first = false; }
                if (tag != null) { if (!first) sb.append(","); sb.append("tag:'").append(JS.esc(tag)).append("'"); first = false; }
                if (lang != null) { if (!first) sb.append(","); sb.append("lang:'").append(JS.esc(lang)).append("'"); first = false; }
                if (dir != null) { if (!first) sb.append(","); sb.append("dir:'").append(JS.esc(dir)).append("'"); first = false; }
                if (dataExpr != null) { if (!first) sb.append(","); sb.append("data:").append(dataExpr.js()); first = false; }
                if (requireInteraction) { if (!first) sb.append(","); sb.append("requireInteraction:true"); first = false; }
                if (renotify) { if (!first) sb.append(","); sb.append("renotify:true"); first = false; }
                if (silent) { if (!first) sb.append(","); sb.append("silent:true"); }
                sb.append("}");
            }
            return sb.append(")").toString();
        }

        private boolean hasOptions() {
            return body != null || bodyExpr != null || icon != null || badge != null || image != null ||
                   tag != null || lang != null || dir != null || dataExpr != null ||
                   requireInteraction || renotify || silent;
        }

        private boolean hasEventHandlers() {
            return onClickFunc != null || onClickCode != null || onCloseFunc != null || onCloseCode != null ||
                   onErrorFunc != null || onErrorCode != null || onShowFunc != null || onShowCode != null;
        }

        private void addEventListeners(StringBuilder sb, String varName) {
            if (onClickFunc != null) sb.append(varName).append(".onclick=").append(onClickFunc.toExpr()).append(";");
            else if (onClickCode != null) sb.append(varName).append(".onclick=function(){").append(onClickCode).append("};");
            if (onCloseFunc != null) sb.append(varName).append(".onclose=").append(onCloseFunc.toExpr()).append(";");
            else if (onCloseCode != null) sb.append(varName).append(".onclose=function(){").append(onCloseCode).append("};");
            if (onErrorFunc != null) sb.append(varName).append(".onerror=").append(onErrorFunc.toExpr()).append(";");
            else if (onErrorCode != null) sb.append(varName).append(".onerror=function(){").append(onErrorCode).append("};");
            if (onShowFunc != null) sb.append(varName).append(".onshow=").append(onShowFunc.toExpr()).append(";");
            else if (onShowCode != null) sb.append(varName).append(".onshow=function(){").append(onShowCode).append("};");
        }
    }
}
