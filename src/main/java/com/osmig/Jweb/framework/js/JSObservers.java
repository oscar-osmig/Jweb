package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Browser observer APIs: IntersectionObserver, MutationObserver, ResizeObserver.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSObservers.*;
 *
 * intersection()
 *     .onIntersect(callback("entries").log(variable("entries")))
 *     .threshold(0.5)
 *     .observe("myElement", "io");
 * </pre>
 */
public final class JSObservers {
    private JSObservers() {}

    public static IntersectionBuilder intersection() { return new IntersectionBuilder(); }
    public static MutationBuilder mutation() { return new MutationBuilder(); }
    public static ResizeBuilder resize() { return new ResizeBuilder(); }

    /** Disconnects an observer */
    public static Val disconnect(String varName) {
        return new Val(varName + ".disconnect()");
    }

    /** Unobserves specific element */
    public static Val unobserve(String observerVar, String elementId) {
        return new Val(observerVar + ".unobserve(document.getElementById('" + JS.esc(elementId) + "'))");
    }

    // ==================== IntersectionObserver ====================

    public static class IntersectionBuilder {
        private Func callback;
        private String callbackCode;
        private String root;
        private String rootMargin;
        private String threshold;

        public IntersectionBuilder onIntersect(Func callback) {
            this.callback = callback;
            this.callbackCode = null;
            return this;
        }

        public IntersectionBuilder onIntersect(String code) {
            this.callbackCode = code;
            this.callback = null;
            return this;
        }

        public IntersectionBuilder root(String selector) {
            this.root = "document.querySelector('" + JS.esc(selector) + "')";
            return this;
        }

        public IntersectionBuilder rootMargin(String margin) {
            this.rootMargin = margin;
            return this;
        }

        public IntersectionBuilder threshold(double... values) {
            if (values.length == 1) {
                this.threshold = String.valueOf(values[0]);
            } else {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(values[i]);
                }
                this.threshold = sb.append("]").toString();
            }
            return this;
        }

        private String buildOptions() {
            StringBuilder opts = new StringBuilder("{");
            boolean hasOpts = false;
            if (root != null) { opts.append("root:").append(root); hasOpts = true; }
            if (rootMargin != null) { if (hasOpts) opts.append(","); opts.append("rootMargin:'").append(rootMargin).append("'"); hasOpts = true; }
            if (threshold != null) { if (hasOpts) opts.append(","); opts.append("threshold:").append(threshold); }
            return opts.append("}").toString();
        }

        private String buildCallback() {
            if (callback != null) return callback.toExpr();
            return "function(entries,observer){" + (callbackCode != null ? callbackCode : "") + "}";
        }

        /** Creates observer and observes element, returns as Val */
        public Val observe(String elementId, String varName) {
            return new Val("var " + varName + "=new IntersectionObserver(" + buildCallback() + "," + buildOptions() + ");" +
                varName + ".observe(document.getElementById('" + JS.esc(elementId) + "'))");
        }

        /** Builds standalone code block */
        public String build(String elementId, String varName) {
            return observe(elementId, varName).js();
        }
    }

    // ==================== MutationObserver ====================

    public static class MutationBuilder {
        private Func callback;
        private String callbackCode;
        private boolean childList, attributes, characterData, subtree;
        private String attributeFilter;

        public MutationBuilder onMutate(Func callback) {
            this.callback = callback;
            this.callbackCode = null;
            return this;
        }

        public MutationBuilder onMutate(String code) {
            this.callbackCode = code;
            this.callback = null;
            return this;
        }

        public MutationBuilder childList() { this.childList = true; return this; }
        public MutationBuilder attributes() { this.attributes = true; return this; }
        public MutationBuilder characterData() { this.characterData = true; return this; }
        public MutationBuilder subtree() { this.subtree = true; return this; }

        public MutationBuilder attributeFilter(String... attrs) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < attrs.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("'").append(JS.esc(attrs[i])).append("'");
            }
            this.attributeFilter = sb.append("]").toString();
            return this;
        }

        private String buildOptions() {
            StringBuilder opts = new StringBuilder("{");
            if (childList) opts.append("childList:true,");
            if (attributes) opts.append("attributes:true,");
            if (characterData) opts.append("characterData:true,");
            if (subtree) opts.append("subtree:true,");
            if (attributeFilter != null) opts.append("attributeFilter:").append(attributeFilter).append(",");
            if (opts.charAt(opts.length() - 1) == ',') opts.setLength(opts.length() - 1);
            return opts.append("}").toString();
        }

        private String buildCallback() {
            if (callback != null) return callback.toExpr();
            return "function(mutations,observer){" + (callbackCode != null ? callbackCode : "") + "}";
        }

        /** Creates observer and observes element, returns as Val */
        public Val observe(String elementId, String varName) {
            return new Val("var " + varName + "=new MutationObserver(" + buildCallback() + ");" +
                varName + ".observe(document.getElementById('" + JS.esc(elementId) + "')," + buildOptions() + ")");
        }

        /** Builds standalone code block */
        public String build(String elementId, String varName) {
            return observe(elementId, varName).js();
        }
    }

    // ==================== ResizeObserver ====================

    public static class ResizeBuilder {
        private Func callback;
        private String callbackCode;

        public ResizeBuilder onResize(Func callback) {
            this.callback = callback;
            this.callbackCode = null;
            return this;
        }

        public ResizeBuilder onResize(String code) {
            this.callbackCode = code;
            this.callback = null;
            return this;
        }

        private String buildCallback() {
            if (callback != null) return callback.toExpr();
            return "function(entries,observer){" + (callbackCode != null ? callbackCode : "") + "}";
        }

        /** Creates observer and observes element, returns as Val */
        public Val observe(String elementId, String varName) {
            return new Val("var " + varName + "=new ResizeObserver(" + buildCallback() + ");" +
                varName + ".observe(document.getElementById('" + JS.esc(elementId) + "'))");
        }

        /** Builds standalone code block */
        public String build(String elementId, String varName) {
            return observe(elementId, varName).js();
        }
    }
}
