package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * URL and URLSearchParams API for URL manipulation.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSUrl.*;
 *
 * // Parse current URL
 * currentUrl().searchParams()
 *
 * // Build URL with params
 * urlBuilder("/api/users")
 *     .param("page", 1)
 *     .param("filter", variable("searchTerm"))
 *     .toVal()
 *
 * // Parse query string
 * searchParams(variable("location").dot("search"))
 * </pre>
 */
public final class JSUrl {
    private JSUrl() {}

    // ==================== URL Creation ====================

    /** Creates a URL object from string */
    public static Val url(String urlString) {
        return new Val("new URL('" + JS.esc(urlString) + "')");
    }

    /** Creates a URL object from expression */
    public static Val url(Val urlExpr) {
        return new Val("new URL(" + urlExpr.js() + ")");
    }

    /** Creates a URL with base */
    public static Val url(String path, String base) {
        return new Val("new URL('" + JS.esc(path) + "','" + JS.esc(base) + "')");
    }

    /** Gets current page URL */
    public static Val currentUrl() {
        return new Val("new URL(location.href)");
    }

    /** Gets current origin */
    public static Val origin() {
        return new Val("location.origin");
    }

    // ==================== URL Properties ====================

    /** Gets href: url.href */
    public static Val href(Val url) { return new Val(url.js() + ".href"); }

    /** Gets protocol: url.protocol */
    public static Val protocol(Val url) { return new Val(url.js() + ".protocol"); }

    /** Gets host: url.host */
    public static Val host(Val url) { return new Val(url.js() + ".host"); }

    /** Gets hostname: url.hostname */
    public static Val hostname(Val url) { return new Val(url.js() + ".hostname"); }

    /** Gets port: url.port */
    public static Val port(Val url) { return new Val(url.js() + ".port"); }

    /** Gets pathname: url.pathname */
    public static Val pathname(Val url) { return new Val(url.js() + ".pathname"); }

    /** Gets search (query string with ?): url.search */
    public static Val search(Val url) { return new Val(url.js() + ".search"); }

    /** Gets hash: url.hash */
    public static Val hash(Val url) { return new Val(url.js() + ".hash"); }

    /** Gets searchParams: url.searchParams */
    public static Val searchParams(Val url) { return new Val(url.js() + ".searchParams"); }

    // ==================== URLSearchParams ====================

    /** Creates URLSearchParams from query string */
    public static Val params(String queryString) {
        return new Val("new URLSearchParams('" + JS.esc(queryString) + "')");
    }

    /** Creates URLSearchParams from expression */
    public static Val params(Val queryExpr) {
        return new Val("new URLSearchParams(" + queryExpr.js() + ")");
    }

    /** Creates empty URLSearchParams */
    public static Val params() {
        return new Val("new URLSearchParams()");
    }

    /** Gets param value: params.get(name) */
    public static Val getParam(Val params, String name) {
        return new Val(params.js() + ".get('" + JS.esc(name) + "')");
    }

    /** Gets all param values: params.getAll(name) */
    public static Val getAllParams(Val params, String name) {
        return new Val(params.js() + ".getAll('" + JS.esc(name) + "')");
    }

    /** Checks if param exists: params.has(name) */
    public static Val hasParam(Val params, String name) {
        return new Val(params.js() + ".has('" + JS.esc(name) + "')");
    }

    /** Sets param: params.set(name, value) */
    public static Val setParam(Val params, String name, Val value) {
        return new Val(params.js() + ".set('" + JS.esc(name) + "'," + value.js() + ")");
    }

    /** Appends param: params.append(name, value) */
    public static Val appendParam(Val params, String name, Val value) {
        return new Val(params.js() + ".append('" + JS.esc(name) + "'," + value.js() + ")");
    }

    /** Deletes param: params.delete(name) */
    public static Val deleteParam(Val params, String name) {
        return new Val(params.js() + ".delete('" + JS.esc(name) + "')");
    }

    /** Converts to string: params.toString() */
    public static Val paramsToString(Val params) {
        return new Val(params.js() + ".toString()");
    }

    // ==================== URL Builder ====================

    public static UrlBuilder urlBuilder(String basePath) {
        return new UrlBuilder(basePath);
    }

    public static class UrlBuilder {
        private final String basePath;
        private final StringBuilder params = new StringBuilder();
        private boolean hasParams = false;

        UrlBuilder(String basePath) { this.basePath = basePath; }

        public UrlBuilder param(String name, Object value) {
            if (hasParams) params.append("+'&"); else params.append("+'?");
            params.append(JS.esc(name)).append("='+encodeURIComponent(").append(JS.toJs(value)).append(")");
            hasParams = true;
            return this;
        }

        public UrlBuilder paramIf(Val condition, String name, Val value) {
            params.append("+(" + condition.js() + "?'" + (hasParams ? "&" : "?") + JS.esc(name) + "='+encodeURIComponent(" + value.js() + "):'')");
            hasParams = true;
            return this;
        }

        public Val toVal() {
            return new Val("'" + JS.esc(basePath) + "'" + params);
        }

        public String build() { return toVal().js(); }
    }

    // ==================== Convenience ====================

    /** Gets query param from current URL */
    public static Val getQueryParam(String name) {
        return new Val("new URLSearchParams(location.search).get('" + JS.esc(name) + "')");
    }

    /** Encodes URI component */
    public static Val encode(Val value) {
        return new Val("encodeURIComponent(" + value.js() + ")");
    }

    /** Decodes URI component */
    public static Val decode(Val value) {
        return new Val("decodeURIComponent(" + value.js() + ")");
    }
}
