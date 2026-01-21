package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.List;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * JavaScript runtime patterns: IIFE, guards, caching, memoization.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.Runtime.*;
 *
 * iife().var_("count", 0).add(myFunc).build()
 * guard("initOnce").add(setupFunc).build()
 * cache("myCache", 60000).set(key, value)
 * </pre>
 */
public final class Runtime {
    private Runtime() {}

    public static IIFE iife() { return new IIFE(); }
    public static Guard guard(String varName) { return new Guard(varName); }
    public static Val globalCache(String name) { return new Val("window." + name + "=window." + name + "||{}"); }
    public static Cache cache(String name, int ttlMs) { return new Cache(name, ttlMs); }
    public static Memoize memoize(String cacheVar) { return new Memoize(cacheVar); }

    // ==================== IIFE ====================

    public static class IIFE {
        private final List<String> parts = new ArrayList<>();

        public IIFE var_(String name, Object value) {
            parts.add("var " + name + "=" + JS.toJs(value));
            return this;
        }

        public IIFE let_(String name, Object value) {
            parts.add("let " + name + "=" + JS.toJs(value));
            return this;
        }

        public IIFE const_(String name, Object value) {
            parts.add("const " + name + "=" + JS.toJs(value));
            return this;
        }

        public IIFE add(Func fn) {
            parts.add(fn.toDecl());
            return this;
        }

        public IIFE call(String fnName, Object... args) {
            parts.add(JS.call(fnName, args).js());
            return this;
        }

        /** @deprecated Use unsafeRaw instead */
        @Deprecated
        public IIFE raw(String js) {
            parts.add(js);
            return this;
        }

        public IIFE unsafeRaw(String js) {
            parts.add(js);
            return this;
        }

        public Val toVal() {
            StringBuilder sb = new StringBuilder("(function(){");
            for (String p : parts) {
                sb.append(p);
                if (!p.endsWith("}") && !p.endsWith(";")) sb.append(";");
            }
            return new Val(sb.append("})()").toString());
        }

        public String build() {
            return toVal().js();
        }
    }

    // ==================== Guard ====================

    public static class Guard {
        private final String varName;
        private final List<String> parts = new ArrayList<>();

        Guard(String varName) { this.varName = varName; }

        public Guard var_(String name, Object value) {
            parts.add("var " + name + "=" + JS.toJs(value));
            return this;
        }

        public Guard let_(String name, Object value) {
            parts.add("let " + name + "=" + JS.toJs(value));
            return this;
        }

        public Guard const_(String name, Object value) {
            parts.add("const " + name + "=" + JS.toJs(value));
            return this;
        }

        public Guard add(Func fn) {
            parts.add(fn.toDecl());
            return this;
        }

        public Guard call(String fnName, Object... args) {
            parts.add(JS.call(fnName, args).js());
            return this;
        }

        /** @deprecated Use unsafeRaw instead */
        @Deprecated
        public Guard raw(String js) {
            parts.add(js);
            return this;
        }

        public Guard unsafeRaw(String js) {
            parts.add(js);
            return this;
        }

        public Val toVal() {
            StringBuilder sb = new StringBuilder();
            sb.append("if(window.").append(varName).append(")return;window.").append(varName).append("=true;");
            for (String p : parts) {
                sb.append(p);
                if (!p.endsWith("}") && !p.endsWith(";")) sb.append(";");
            }
            return new Val(sb.toString());
        }

        public String build() {
            return toVal().js();
        }
    }

    // ==================== Cache ====================

    public static class Cache {
        private final String name;
        private final int ttlMs;

        Cache(String name, int ttlMs) { this.name = name; this.ttlMs = ttlMs; }

        public Val get(String key) {
            return new Val(name + "[" + key + "]");
        }

        public Val isValid(String key) {
            return new Val(name + "[" + key + "]&&Date.now()-" + name + "[" + key + "].time<" + ttlMs);
        }

        public Val set(String key, Val valueExpr) {
            return new Val(name + "[" + key + "]={data:" + valueExpr.js() + ",time:Date.now()}");
        }

        public Val set(String key, String valueExpr) {
            return new Val(name + "[" + key + "]={data:" + valueExpr + ",time:Date.now()}");
        }

        public Val getData(String key) {
            return new Val(name + "[" + key + "].data");
        }

        public Val clear(String key) {
            return new Val("delete " + name + "[" + key + "]");
        }

        public Val clearAll() {
            return new Val(name + "={}");
        }
    }

    // ==================== Memoize ====================

    public static class Memoize {
        private final String cacheVar;

        Memoize(String cacheVar) { this.cacheVar = cacheVar; }

        /** Wraps computation with memoization using key expression */
        public Val wrap(String keyExpr, Val computeExpr) {
            return new Val("(" + cacheVar + "[" + keyExpr + "]!==undefined?" + cacheVar + "[" + keyExpr + "]:" +
                "(" + cacheVar + "[" + keyExpr + "]=" + computeExpr.js() + "))");
        }

        /** Wraps computation with memoization using key expression (string compute) */
        public Val wrap(String keyExpr, String computeExpr) {
            return new Val("(" + cacheVar + "[" + keyExpr + "]!==undefined?" + cacheVar + "[" + keyExpr + "]:" +
                "(" + cacheVar + "[" + keyExpr + "]=" + computeExpr + "))");
        }

        /** Wraps with Func for computation */
        public Val wrap(String keyExpr, Func computeFn) {
            return new Val("(" + cacheVar + "[" + keyExpr + "]!==undefined?" + cacheVar + "[" + keyExpr + "]:" +
                "(" + cacheVar + "[" + keyExpr + "]=(" + computeFn.toExpr() + ")()))");
        }

        /** Wraps with JSON key for multi-arg memoization */
        public Val wrapMultiArg(Val argsExpr, Val computeExpr) {
            return new Val("(function(){var k=JSON.stringify(" + argsExpr.js() + ");" +
                "return " + cacheVar + "[k]!==undefined?" + cacheVar + "[k]:(" + cacheVar + "[k]=" + computeExpr.js() + ")}())");
        }

        /** Clears memoization cache */
        public Val clear() {
            return new Val(cacheVar + "={}");
        }

        /** Clears specific key */
        public Val clear(String keyExpr) {
            return new Val("delete " + cacheVar + "[" + keyExpr + "]");
        }

        /** Checks if key is cached */
        public Val has(String keyExpr) {
            return new Val(cacheVar + "[" + keyExpr + "]!==undefined");
        }
    }
}
