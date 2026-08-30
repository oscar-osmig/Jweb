package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * History API DSL for browser navigation and URL manipulation.
 *
 * <p>Provides type-safe wrappers for the History API including pushState,
 * replaceState, popstate handling, navigation guards, and URL query
 * parameter manipulation.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSHistory.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Push a new history entry — argument order matches the platform:
 * // history.pushState(state, title, url), with the dead title omitted.
 * pushState(obj("page", str("dashboard")), "/dashboard")
 * pushState("/users/42")                        // URL only
 *
 * // Replace current history entry
 * replaceState(obj("page", str("login")), "/login")
 *
 * // Listen for back/forward navigation
 * onPopState(callback("e")
 *     .log(v("e").dot("state")))
 *
 * // Navigate back/forward
 * back()
 * forward()
 * go(-2)
 *
 * // Navigation guard (confirm before leaving)
 * navigationGuard(callback("e")
 *     .return_(str("You have unsaved changes.")))
 *
 * // Hash change handling
 * onHashChange(callback("e")
 *     .log(currentHash()))
 *
 * // Query parameter manipulation
 * getQueryParam("search")
 * setQueryParam("page", "2")
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.js.JSHistory} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class JSHistory {
    protected JSHistory() {}

    // ==================== Navigation ====================

    /** Navigates back one entry: history.back() */
    public static Val back() {
        return new Val("history.back()");
    }

    /** Navigates forward one entry: history.forward() */
    public static Val forward() {
        return new Val("history.forward()");
    }

    /**
     * Navigates relative to current position: history.go(delta)
     *
     * @param delta negative goes back, positive goes forward
     * @return a Val representing the go operation
     */
    public static Val go(int delta) {
        return new Val("history.go(" + delta + ")");
    }

    /** Gets the number of entries in the session history: history.length */
    public static Val historyLength() {
        return new Val("history.length");
    }

    /** Gets the current state object: history.state */
    public static Val historyState() {
        return new Val("history.state");
    }

    /** Gets the current scroll restoration mode: history.scrollRestoration */
    public static Val scrollRestoration() {
        return new Val("history.scrollRestoration");
    }

    /**
     * Sets scroll restoration mode.
     *
     * @param mode "auto" or "manual"
     * @return a Val representing the assignment
     */
    public static Val setScrollRestoration(String mode) {
        return new Val("history.scrollRestoration='" + JS.esc(mode) + "'");
    }

    // ==================== pushState / replaceState ====================
    //
    // Argument order matches the platform: history.pushState(state, title, url).
    // The `title` argument is ignored by every browser and is not exposed.

    /**
     * {@code history.pushState(null, '', url)} — a new history entry with no state.
     *
     * @param url the URL for the new entry
     */
    public static Val pushState(String url) {
        return new Val("history.pushState(null,'','" + JS.esc(url) + "')");
    }

    /**
     * {@code history.pushState(state, '', url)}.
     *
     * <pre>
     * pushState(obj("page", str("dashboard")), "/dashboard")
     * </pre>
     *
     * @param state the state object
     * @param url the URL for the new entry
     */
    public static Val pushState(Val state, String url) {
        return new Val("history.pushState(" + state.js() + ",'','" + JS.esc(url) + "')");
    }

    /**
     * {@code history.pushState(state, '', url)} with a computed URL.
     *
     * @param state the state object
     * @param url the URL expression
     */
    public static Val pushState(Val state, Val url) {
        return new Val("history.pushState(" + state.js() + ",''," + url.js() + ")");
    }

    /**
     * {@code history.replaceState(null, '', url)} — replace with no state.
     *
     * @param url the replacement URL
     */
    public static Val replaceState(String url) {
        return new Val("history.replaceState(null,'','" + JS.esc(url) + "')");
    }

    /**
     * {@code history.replaceState(state, '', url)}.
     *
     * @param state the state object
     * @param url the replacement URL
     */
    public static Val replaceState(Val state, String url) {
        return new Val("history.replaceState(" + state.js() + ",'','" + JS.esc(url) + "')");
    }

    /**
     * {@code history.replaceState(state, '', url)} with a computed URL.
     *
     * @param state the state object
     * @param url the URL expression
     */
    public static Val replaceState(Val state, Val url) {
        return new Val("history.replaceState(" + state.js() + ",''," + url.js() + ")");
    }

    // ==================== Event Listeners ====================

    /**
     * Listens for popstate events (back/forward navigation).
     * The callback receives the PopStateEvent with a .state property.
     *
     * @param handler callback receiving the PopStateEvent
     * @return a Val representing the event listener
     */
    public static Val onPopState(Func handler) {
        return new Val("window.addEventListener('popstate'," + handler.toExpr() + ")");
    }

    /**
     * Listens for hash change events.
     * The callback receives the HashChangeEvent with .oldURL and .newURL.
     *
     * @param handler callback receiving the HashChangeEvent
     * @return a Val representing the event listener
     */
    public static Val onHashChange(Func handler) {
        return new Val("window.addEventListener('hashchange'," + handler.toExpr() + ")");
    }

    /**
     * Removes a popstate listener.
     *
     * @param handler the handler reference to remove
     * @return a Val representing the removeEventListener call
     */
    public static Val offPopState(Val handler) {
        return new Val("window.removeEventListener('popstate'," + handler.js() + ")");
    }

    /**
     * Removes a hashchange listener.
     *
     * @param handler the handler reference to remove
     * @return a Val representing the removeEventListener call
     */
    public static Val offHashChange(Val handler) {
        return new Val("window.removeEventListener('hashchange'," + handler.js() + ")");
    }

    // ==================== Navigation Guards ====================

    /**
     * Sets a navigation guard using beforeunload event.
     * Return a string from the callback to show a confirmation dialog.
     *
     * @param handler callback that returns a message string to block, or nothing to allow
     * @return a Val representing the guard installation
     */
    public static Val navigationGuard(Func handler) {
        return new Val("window.addEventListener('beforeunload',function(e){var m=(" + handler.toExpr()
            + ")(e);if(m){e.preventDefault();e.returnValue=m;}})");
    }

    /**
     * Sets a simple navigation guard with a fixed message.
     *
     * @param message the confirmation message
     * @return a Val representing the guard
     */
    public static Val navigationGuard(String message) {
        return new Val("window.addEventListener('beforeunload',function(e){e.preventDefault();e.returnValue='" + JS.esc(message) + "';})");
    }

    /**
     * Conditional navigation guard -- only blocks when condition is true.
     *
     * @param condition expression that evaluates to true to block navigation
     * @param message the confirmation message
     * @return a Val representing the conditional guard
     */
    public static Val navigationGuardWhen(Val condition, String message) {
        return new Val("window.addEventListener('beforeunload',function(e){if(" + condition.js()
            + "){e.preventDefault();e.returnValue='" + JS.esc(message) + "';}})");
    }

    // ==================== Hash Manipulation ====================

    /** Gets the current hash (without #): location.hash.slice(1) */
    public static Val currentHash() {
        return new Val("location.hash.slice(1)");
    }

    /**
     * Sets the location hash.
     *
     * @param hash the hash value (without #)
     * @return a Val representing the hash assignment
     */
    public static Val setHash(String hash) {
        return new Val("location.hash='" + JS.esc(hash) + "'");
    }

    /**
     * Sets the location hash from expression.
     *
     * @param hash the hash expression
     * @return a Val representing the hash assignment
     */
    public static Val setHash(Val hash) {
        return new Val("location.hash=" + hash.js());
    }

    // ==================== URL & Query Parameters ====================

    /** Gets the current pathname: location.pathname */
    public static Val currentPath() {
        return new Val("location.pathname");
    }

    /** Gets the current full URL: location.href */
    public static Val currentUrl() {
        return new Val("location.href");
    }

    /** Gets the current origin: location.origin */
    public static Val currentOrigin() {
        return new Val("location.origin");
    }

    /** Gets the current search string: location.search */
    public static Val currentSearch() {
        return new Val("location.search");
    }

    /**
     * Gets a query parameter value from the current URL.
     *
     * @param name the parameter name
     * @return a Val representing the parameter value (or null)
     *
     * @deprecated Use {@code JSUrl.getQueryParam(...)} — query-string
     *     reading lives in one module.
     */
    @Deprecated
    public static Val getQueryParam(String name) {
        return new Val("new URLSearchParams(location.search).get('" + JS.esc(name) + "')");
    }

    /**
     * Gets a query parameter value with dynamic name.
     *
     * @param name the parameter name expression
     * @return a Val representing the parameter value
     *
     * @deprecated Use {@code JSUrl.getQueryParam(...)} — query-string
     *     reading lives in one module.
     */
    @Deprecated
    public static Val getQueryParam(Val name) {
        return new Val("new URLSearchParams(location.search).get(" + name.js() + ")");
    }

    /**
     * Gets all values for a query parameter.
     *
     * @param name the parameter name
     * @return a Val representing the array of values
     */
    public static Val getQueryParams(String name) {
        return new Val("new URLSearchParams(location.search).getAll('" + JS.esc(name) + "')");
    }

    /**
     * Checks if a query parameter exists.
     *
     * @param name the parameter name
     * @return a Val representing the boolean result
     */
    public static Val hasQueryParam(String name) {
        return new Val("new URLSearchParams(location.search).has('" + JS.esc(name) + "')");
    }

    /**
     * Sets a query parameter and updates the URL via pushState.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return a Val representing the operation
     */
    public static Val setQueryParam(String name, String value) {
        return new Val("(function(){var p=new URLSearchParams(location.search);p.set('" + JS.esc(name)
            + "','" + JS.esc(value) + "');history.pushState(null,'',location.pathname+'?'+p.toString());}())");
    }

    /**
     * Sets a query parameter with dynamic value.
     *
     * @param name the parameter name
     * @param value the parameter value expression
     * @return a Val representing the operation
     */
    public static Val setQueryParam(String name, Val value) {
        return new Val("(function(){var p=new URLSearchParams(location.search);p.set('" + JS.esc(name)
            + "'," + value.js() + ");history.pushState(null,'',location.pathname+'?'+p.toString());}())");
    }

    /**
     * Removes a query parameter and updates the URL via pushState.
     *
     * @param name the parameter name to remove
     * @return a Val representing the operation
     */
    public static Val removeQueryParam(String name) {
        return new Val("(function(){var p=new URLSearchParams(location.search);p.delete('" + JS.esc(name)
            + "');var s=p.toString();history.pushState(null,'',location.pathname+(s?'?'+s:''));}())");
    }

    /**
     * Gets all query parameters as an object.
     *
     * @return a Val representing the parameters object
     */
    public static Val queryParamsObject() {
        return new Val("Object.fromEntries(new URLSearchParams(location.search))");
    }

    // ==================== Back/Forward Detection ====================

    // Note: there is deliberately no detectDirection(...) helper. Detecting
    // back-vs-forward by comparing history.length cannot work — the length does
    // not change on back/forward navigation, so the check always reported
    // "forward". Track a position counter in your own pushState state object
    // and compare it in onPopState instead.
}
