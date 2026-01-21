package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Page Visibility API for detecting tab visibility.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSVisibility.*;
 *
 * // Check visibility
 * if_(isHidden(), pauseVideo())
 *
 * // Listen for changes
 * onVisibilityChange(callback()
 *     .if_(isHidden(), call("pauseVideo"))
 *     .else_(call("resumeVideo"))
 * )
 *
 * // Get visibility state
 * visibilityState()  // "visible", "hidden"
 * </pre>
 */
public final class JSVisibility {
    private JSVisibility() {}

    // ==================== Visibility State ====================

    /** Gets visibility state: "visible", "hidden" */
    public static Val visibilityState() {
        return new Val("document.visibilityState");
    }

    /** Checks if page is hidden */
    public static Val isHidden() {
        return new Val("document.hidden");
    }

    /** Checks if page is visible */
    public static Val isVisible() {
        return new Val("(!document.hidden)");
    }

    // ==================== Event Listeners ====================

    /** Listens for visibility changes */
    public static Val onVisibilityChange(Func handler) {
        return new Val("document.addEventListener('visibilitychange'," + handler.toExpr() + ")");
    }

    /** Listens for visibility changes with raw code */
    public static Val onVisibilityChange(String code) {
        return new Val("document.addEventListener('visibilitychange',function(){" + code + "})");
    }

    /** Removes visibility change listener */
    public static Val removeVisibilityListener(Val handler) {
        return new Val("document.removeEventListener('visibilitychange'," + handler.js() + ")");
    }

    // ==================== Conditional Execution ====================

    /** Executes callback when page becomes visible */
    public static Val onVisible(Func handler) {
        return new Val("document.addEventListener('visibilitychange',function(){if(!document.hidden)(" + handler.toExpr() + ")()})");
    }

    /** Executes callback when page becomes hidden */
    public static Val onHidden(Func handler) {
        return new Val("document.addEventListener('visibilitychange',function(){if(document.hidden)(" + handler.toExpr() + ")()})");
    }

    /** Executes only when visible, defers when hidden */
    public static Val whenVisible(Func action) {
        return new Val("(document.hidden?document.addEventListener('visibilitychange',function h(){if(!document.hidden){document.removeEventListener('visibilitychange',h);(" + action.toExpr() + ")()}}):(" + action.toExpr() + ")())");
    }

    // ==================== Utilities ====================

    /** Creates visibility-aware interval (pauses when hidden) */
    public static Val visibilityAwareInterval(Func callback, int ms) {
        return new Val("(function(){var id,run=function(){id=setInterval(" + callback.toExpr() + "," + ms + ")};if(!document.hidden)run();document.addEventListener('visibilitychange',function(){document.hidden?clearInterval(id):run()});return{clear:function(){clearInterval(id)}}}())");
    }

    /** Gets time since page became hidden (ms), or 0 if visible */
    public static Val hiddenDuration() {
        return new Val("(document.hidden?Date.now()-(window._hiddenAt||Date.now()):0)");
    }

    /** Tracks hidden time (call once on page load) */
    public static Val trackHiddenTime() {
        return new Val("document.addEventListener('visibilitychange',function(){document.hidden?window._hiddenAt=Date.now():delete window._hiddenAt})");
    }
}
