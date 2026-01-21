package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Fullscreen API for entering/exiting fullscreen mode.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSFullscreen.*;
 *
 * // Enter fullscreen
 * requestFullscreen(getElem("video"))
 *
 * // Exit fullscreen
 * exitFullscreen()
 *
 * // Toggle fullscreen
 * toggleFullscreen(getElem("container"))
 *
 * // Check state
 * if_(isFullscreen(), showExitButton())
 * </pre>
 */
public final class JSFullscreen {
    private JSFullscreen() {}

    // ==================== Fullscreen Operations ====================

    /** Requests fullscreen for an element */
    public static Val requestFullscreen(Val element) {
        return new Val(element.js() + ".requestFullscreen()");
    }

    /** Requests fullscreen for element by ID */
    public static Val requestFullscreen(String elementId) {
        return new Val("document.getElementById('" + JS.esc(elementId) + "').requestFullscreen()");
    }

    /** Requests fullscreen with options */
    public static Val requestFullscreen(Val element, String navigationUI) {
        return new Val(element.js() + ".requestFullscreen({navigationUI:'" + JS.esc(navigationUI) + "'})");
    }

    /** Exits fullscreen mode */
    public static Val exitFullscreen() {
        return new Val("document.exitFullscreen()");
    }

    /** Toggles fullscreen for an element */
    public static Val toggleFullscreen(Val element) {
        return new Val("(document.fullscreenElement?" + "document.exitFullscreen():" + element.js() + ".requestFullscreen())");
    }

    /** Toggles fullscreen for element by ID */
    public static Val toggleFullscreen(String elementId) {
        return new Val("(document.fullscreenElement?document.exitFullscreen():document.getElementById('" + JS.esc(elementId) + "').requestFullscreen())");
    }

    // ==================== State ====================

    /** Gets current fullscreen element (or null) */
    public static Val fullscreenElement() {
        return new Val("document.fullscreenElement");
    }

    /** Checks if in fullscreen mode */
    public static Val isFullscreen() {
        return new Val("(!!document.fullscreenElement)");
    }

    /** Checks if specific element is fullscreen */
    public static Val isElementFullscreen(Val element) {
        return new Val("(document.fullscreenElement===" + element.js() + ")");
    }

    /** Checks if fullscreen is enabled/allowed */
    public static Val fullscreenEnabled() {
        return new Val("document.fullscreenEnabled");
    }

    // ==================== Event Listeners ====================

    /** Listens for fullscreen changes */
    public static Val onFullscreenChange(Func handler) {
        return new Val("document.addEventListener('fullscreenchange'," + handler.toExpr() + ")");
    }

    /** Listens for fullscreen changes with raw code */
    public static Val onFullscreenChange(String code) {
        return new Val("document.addEventListener('fullscreenchange',function(){" + code + "})");
    }

    /** Listens for fullscreen errors */
    public static Val onFullscreenError(Func handler) {
        return new Val("document.addEventListener('fullscreenerror'," + handler.toExpr() + ")");
    }

    /** Listens for fullscreen errors with raw code */
    public static Val onFullscreenError(String code) {
        return new Val("document.addEventListener('fullscreenerror',function(){" + code + "})");
    }

    /** Removes fullscreen change listener */
    public static Val removeFullscreenListener(Val handler) {
        return new Val("document.removeEventListener('fullscreenchange'," + handler.js() + ")");
    }

    // ==================== Element-Specific Listeners ====================

    /** Listens for fullscreen changes on specific element */
    public static Val onElementFullscreenChange(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('fullscreenchange'," + handler.toExpr() + ")");
    }

    /** Listens for fullscreen errors on specific element */
    public static Val onElementFullscreenError(Val element, Func handler) {
        return new Val(element.js() + ".addEventListener('fullscreenerror'," + handler.toExpr() + ")");
    }

    // ==================== Utilities ====================

    /** Enters fullscreen when condition is true, exits when false */
    public static Val syncFullscreen(Val element, Val condition) {
        return new Val("(" + condition.js() + "&&!document.fullscreenElement?" + element.js() + ".requestFullscreen():!" + condition.js() + "&&document.fullscreenElement?document.exitFullscreen():null)");
    }

    /** Creates a fullscreen button for an element */
    public static Val fullscreenButton(Val button, Val targetElement) {
        return new Val(button.js() + ".addEventListener('click',function(){" + targetElement.js() + ".requestFullscreen().catch(function(){})})");
    }

    /** Checks if fullscreen API is supported */
    public static Val isSupported() {
        return new Val("(document.fullscreenEnabled!==undefined)");
    }
}
