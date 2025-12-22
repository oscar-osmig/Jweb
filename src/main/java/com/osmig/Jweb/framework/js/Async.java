package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.List;

import static com.osmig.Jweb.framework.js.JS.*;

/**
 * Async JavaScript DSL for Promises, async/await, and fetch.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.Async.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Fetch with fluent API
 * String js = fetch("/api/users")
 *     .get()
 *     .then(callback("response").ret(variable("response").dot("json").invoke()))
 *     .then(callback("data").call("renderUsers", variable("data")))
 *     .catch_(callback("err").call("console.error", variable("err")))
 *     .build();
 *
 * // POST request with body
 * fetch("/api/users")
 *     .post()
 *     .json(obj("name", "John", "email", "john@example.com"))
 *     .then(callback("r").ret(variable("r").dot("json").invoke()))
 *     .then(callback("user").call("onUserCreated", variable("user")))
 *     .build();
 *
 * // Async function
 * asyncFunc("loadData")
 *     .await_("response", fetch("/api/data").get().toVal())
 *     .await_("data", variable("response").dot("json").invoke())
 *     .ret(variable("data"));
 *
 * // Promise.all
 * promiseAll(
 *     fetch("/api/users").get().toVal(),
 *     fetch("/api/posts").get().toVal()
 * ).then(callback("results")
 *     .let_("users", variable("results").at(0))
 *     .let_("posts", variable("results").at(1))
 *     .call("render", variable("users"), variable("posts"))
 * ).build();
 * </pre>
 */
public final class Async {

    private Async() {}

    // ==================== Fetch Builder ====================

    /**
     * Creates a fetch request builder.
     *
     * @param url the URL to fetch
     * @return a FetchBuilder for configuring the request
     */
    public static FetchBuilder fetch(String url) {
        return new FetchBuilder("'" + JS.esc(url) + "'");
    }

    /**
     * Creates a fetch request builder with a dynamic URL.
     *
     * @param urlExpr the URL expression (e.g., variable("apiUrl"))
     * @return a FetchBuilder for configuring the request
     */
    public static FetchBuilder fetch(Val urlExpr) {
        return new FetchBuilder(urlExpr.js());
    }

    /**
     * Fluent builder for fetch requests.
     */
    public static class FetchBuilder {
        private final String url;
        private String method = "GET";
        private final List<String> headers = new ArrayList<>();
        private String body;
        private String credentials;
        private String mode;
        private String cache;
        private String redirect;
        private final List<String> thenChain = new ArrayList<>();
        private String catchHandler;
        private String finallyHandler;

        FetchBuilder(String url) {
            this.url = url;
        }

        // ========== HTTP Methods ==========

        public FetchBuilder get() {
            this.method = "GET";
            return this;
        }

        public FetchBuilder post() {
            this.method = "POST";
            return this;
        }

        public FetchBuilder put() {
            this.method = "PUT";
            return this;
        }

        public FetchBuilder patch() {
            this.method = "PATCH";
            return this;
        }

        public FetchBuilder delete() {
            this.method = "DELETE";
            return this;
        }

        public FetchBuilder method(String method) {
            this.method = method;
            return this;
        }

        // ========== Headers ==========

        public FetchBuilder header(String name, String value) {
            headers.add("'" + JS.esc(name) + "':'" + JS.esc(value) + "'");
            return this;
        }

        public FetchBuilder header(String name, Val value) {
            headers.add("'" + JS.esc(name) + "':" + value.js());
            return this;
        }

        public FetchBuilder contentType(String type) {
            return header("Content-Type", type);
        }

        public FetchBuilder accept(String type) {
            return header("Accept", type);
        }

        public FetchBuilder authorization(String token) {
            return header("Authorization", token);
        }

        public FetchBuilder authorization(Val token) {
            return header("Authorization", token);
        }

        public FetchBuilder bearerToken(String token) {
            return header("Authorization", "Bearer " + token);
        }

        public FetchBuilder bearerToken(Val token) {
            headers.add("'Authorization':'Bearer '+(" + token.js() + ")");
            return this;
        }

        // ========== Body ==========

        /**
         * Sets a JSON body. Automatically sets Content-Type header.
         */
        public FetchBuilder json(Val obj) {
            contentType("application/json");
            this.body = "JSON.stringify(" + obj.js() + ")";
            return this;
        }

        /**
         * Sets a JSON body from key-value pairs.
         */
        public FetchBuilder json(Object... pairs) {
            return json(JS.obj(pairs));
        }

        /**
         * Sets a form data body. Automatically sets Content-Type header.
         */
        public FetchBuilder form(Val formData) {
            contentType("application/x-www-form-urlencoded");
            this.body = formData.js();
            return this;
        }

        /**
         * Sets the request body directly.
         */
        public FetchBuilder body(Val body) {
            this.body = body.js();
            return this;
        }

        /**
         * Sets the request body as a string.
         */
        public FetchBuilder body(String body) {
            this.body = "'" + JS.esc(body) + "'";
            return this;
        }

        // ========== Options ==========

        /**
         * Sets credentials mode.
         * @param mode "omit", "same-origin", or "include"
         */
        public FetchBuilder credentials(String mode) {
            this.credentials = mode;
            return this;
        }

        public FetchBuilder withCredentials() {
            return credentials("include");
        }

        public FetchBuilder sameOriginCredentials() {
            return credentials("same-origin");
        }

        /**
         * Sets CORS mode.
         * @param mode "cors", "no-cors", or "same-origin"
         */
        public FetchBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public FetchBuilder cors() {
            return mode("cors");
        }

        public FetchBuilder noCors() {
            return mode("no-cors");
        }

        /**
         * Sets cache mode.
         * @param mode "default", "no-cache", "reload", "force-cache", "only-if-cached"
         */
        public FetchBuilder cache(String mode) {
            this.cache = mode;
            return this;
        }

        public FetchBuilder noCache() {
            return cache("no-cache");
        }

        /**
         * Sets redirect mode.
         * @param mode "follow", "error", "manual"
         */
        public FetchBuilder redirect(String mode) {
            this.redirect = mode;
            return this;
        }

        // ========== Promise Chain ==========

        /**
         * Adds a then handler to the promise chain.
         */
        public FetchBuilder then(Func handler) {
            thenChain.add(".then(" + handler.toExpr() + ")");
            return this;
        }

        /**
         * Adds a catch handler for errors.
         */
        public FetchBuilder catch_(Func handler) {
            this.catchHandler = ".catch(" + handler.toExpr() + ")";
            return this;
        }

        /**
         * Adds a finally handler.
         */
        public FetchBuilder finally_(Func handler) {
            this.finallyHandler = ".finally(" + handler.toExpr() + ")";
            return this;
        }

        // ========== Common Patterns ==========

        /**
         * Adds .then(r => r.json()) to parse JSON response.
         */
        public FetchBuilder json() {
            thenChain.add(".then(function(r){return r.json()})");
            return this;
        }

        /**
         * Adds .then(r => r.text()) to get text response.
         */
        public FetchBuilder text() {
            thenChain.add(".then(function(r){return r.text()})");
            return this;
        }

        /**
         * Adds .then(r => r.blob()) to get blob response.
         */
        public FetchBuilder blob() {
            thenChain.add(".then(function(r){return r.blob()})");
            return this;
        }

        /**
         * Adds error checking for non-OK responses.
         */
        public FetchBuilder checkOk() {
            thenChain.add(".then(function(r){if(!r.ok)throw new Error('HTTP '+r.status);return r})");
            return this;
        }

        // ========== Build ==========

        /**
         * Builds the fetch call as a statement.
         */
        public String build() {
            return toVal().js();
        }

        /**
         * Returns the fetch call as a Val for use in other expressions.
         */
        public Val toVal() {
            StringBuilder sb = new StringBuilder("fetch(").append(url);

            // Build options object if needed
            List<String> options = new ArrayList<>();
            if (!"GET".equals(method)) {
                options.add("method:'" + method + "'");
            }
            if (!headers.isEmpty()) {
                options.add("headers:{" + String.join(",", headers) + "}");
            }
            if (body != null) {
                options.add("body:" + body);
            }
            if (credentials != null) {
                options.add("credentials:'" + credentials + "'");
            }
            if (mode != null) {
                options.add("mode:'" + mode + "'");
            }
            if (cache != null) {
                options.add("cache:'" + cache + "'");
            }
            if (redirect != null) {
                options.add("redirect:'" + redirect + "'");
            }

            if (!options.isEmpty()) {
                sb.append(",{").append(String.join(",", options)).append("}");
            }

            sb.append(")");

            // Add promise chain
            for (String then : thenChain) {
                sb.append(then);
            }
            if (catchHandler != null) {
                sb.append(catchHandler);
            }
            if (finallyHandler != null) {
                sb.append(finallyHandler);
            }

            return new Val(sb.toString());
        }
    }

    // ==================== Async Functions ====================

    /**
     * Creates an async function.
     *
     * @param name the function name
     * @param params the function parameters
     * @return an AsyncFunc builder
     */
    public static AsyncFunc asyncFunc(String name, String... params) {
        return new AsyncFunc(name, params);
    }

    /**
     * Creates an anonymous async function (async callback).
     *
     * @param params the function parameters
     * @return an AsyncFunc builder
     */
    public static AsyncFunc asyncCallback(String... params) {
        return new AsyncFunc(null, params);
    }

    /**
     * Builder for async functions.
     */
    public static class AsyncFunc {
        private final String name;
        private final String[] params;
        private final List<String> body = new ArrayList<>();

        AsyncFunc(String name, String... params) {
            this.name = name;
            this.params = params;
        }

        public AsyncFunc let_(String name, Object value) {
            body.add("let " + name + "=" + JS.toJs(value));
            return this;
        }

        public AsyncFunc const_(String name, Object value) {
            body.add("const " + name + "=" + JS.toJs(value));
            return this;
        }

        public AsyncFunc set(String name, Object value) {
            body.add(name + "=" + JS.toJs(value));
            return this;
        }

        /**
         * Awaits an expression and assigns to a variable.
         * @param varName the variable to assign the result to
         * @param expr the expression to await
         */
        public AsyncFunc await_(String varName, Val expr) {
            body.add("const " + varName + "=await " + expr.js());
            return this;
        }

        /**
         * Awaits an expression without assignment.
         */
        public AsyncFunc await_(Val expr) {
            body.add("await " + expr.js());
            return this;
        }

        public AsyncFunc call(String fn, Object... args) {
            body.add(JS.call(fn, args).js());
            return this;
        }

        public AsyncFunc log(Object... args) {
            body.add(JS.call("console.log", args).js());
            return this;
        }

        public AsyncFunc ret() {
            body.add("return");
            return this;
        }

        public AsyncFunc ret(Object value) {
            body.add("return " + JS.toJs(value));
            return this;
        }

        /**
         * Adds try/catch block.
         */
        public TryBuilder try_() {
            return new TryBuilder(this);
        }

        /**
         * Adds if statement.
         */
        public AsyncFunc if_(Val condition, Object... thenStmts) {
            StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
            for (Object s : thenStmts) appendStmt(sb, s);
            sb.append("}");
            body.add(sb.toString());
            return this;
        }

        /**
         * Adds raw JavaScript.
         */
        public AsyncFunc unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        String toExpr() {
            StringBuilder sb = new StringBuilder("async function(");
            sb.append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        String toDecl() {
            StringBuilder sb = new StringBuilder("async function ");
            if (name != null) sb.append(name);
            sb.append("(").append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof Stmt st) sb.append(st.code).append(";");
            else if (s instanceof Val val) sb.append(val.js()).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }

        /**
         * Inner try/catch builder for async functions.
         */
        public class TryBuilder {
            private final AsyncFunc parent;
            private final List<String> tryStmts = new ArrayList<>();
            private String catchVar;
            private final List<String> catchStmts = new ArrayList<>();
            private final List<String> finallyStmts = new ArrayList<>();

            TryBuilder(AsyncFunc parent) {
                this.parent = parent;
            }

            public TryBuilder await_(String varName, Val expr) {
                tryStmts.add("const " + varName + "=await " + expr.js());
                return this;
            }

            public TryBuilder await_(Val expr) {
                tryStmts.add("await " + expr.js());
                return this;
            }

            public TryBuilder let_(String name, Object value) {
                tryStmts.add("let " + name + "=" + JS.toJs(value));
                return this;
            }

            public TryBuilder call(String fn, Object... args) {
                tryStmts.add(JS.call(fn, args).js());
                return this;
            }

            public TryBuilder ret(Object value) {
                tryStmts.add("return " + JS.toJs(value));
                return this;
            }

            public CatchBuilder catch_(String varName) {
                this.catchVar = varName;
                return new CatchBuilder(this);
            }

            AsyncFunc finish() {
                StringBuilder sb = new StringBuilder("try{");
                for (String stmt : tryStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");

                if (catchVar != null) {
                    sb.append("catch(").append(catchVar).append("){");
                    for (String stmt : catchStmts) {
                        sb.append(stmt);
                        if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                    }
                    sb.append("}");
                }

                if (!finallyStmts.isEmpty()) {
                    sb.append("finally{");
                    for (String stmt : finallyStmts) {
                        sb.append(stmt);
                        if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                    }
                    sb.append("}");
                }

                parent.body.add(sb.toString());
                return parent;
            }

            public class CatchBuilder {
                private final TryBuilder parent;

                CatchBuilder(TryBuilder parent) {
                    this.parent = parent;
                }

                public CatchBuilder call(String fn, Object... args) {
                    parent.catchStmts.add(JS.call(fn, args).js());
                    return this;
                }

                public CatchBuilder log(Object... args) {
                    return call("console.error", args);
                }

                public CatchBuilder ret(Object value) {
                    parent.catchStmts.add("return " + JS.toJs(value));
                    return this;
                }

                public FinallyBuilder finally_() {
                    return new FinallyBuilder(parent);
                }

                public AsyncFunc endTry() {
                    return parent.finish();
                }
            }

            public class FinallyBuilder {
                private final TryBuilder parent;

                FinallyBuilder(TryBuilder parent) {
                    this.parent = parent;
                }

                public FinallyBuilder call(String fn, Object... args) {
                    parent.finallyStmts.add(JS.call(fn, args).js());
                    return this;
                }

                public AsyncFunc endTry() {
                    return parent.finish();
                }
            }
        }
    }

    // ==================== Promise Utilities ====================

    /**
     * Creates Promise.all() with multiple promises.
     */
    public static PromiseBuilder promiseAll(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.all([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseBuilder(sb.toString());
    }

    /**
     * Creates Promise.race() with multiple promises.
     */
    public static PromiseBuilder promiseRace(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.race([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseBuilder(sb.toString());
    }

    /**
     * Creates Promise.allSettled() with multiple promises.
     */
    public static PromiseBuilder promiseAllSettled(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.allSettled([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseBuilder(sb.toString());
    }

    /**
     * Creates Promise.any() with multiple promises.
     */
    public static PromiseBuilder promiseAny(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.any([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseBuilder(sb.toString());
    }

    /**
     * Creates Promise.resolve() with a value.
     */
    public static Val promiseResolve(Object value) {
        return new Val("Promise.resolve(" + JS.toJs(value) + ")");
    }

    /**
     * Creates Promise.reject() with an error.
     */
    public static Val promiseReject(Object error) {
        return new Val("Promise.reject(" + JS.toJs(error) + ")");
    }

    /**
     * Creates a new Promise with executor function.
     *
     * @param resolveParam name of resolve parameter (usually "resolve")
     * @param rejectParam name of reject parameter (usually "reject")
     * @return a PromiseExecutorBuilder
     */
    public static PromiseExecutorBuilder newPromise(String resolveParam, String rejectParam) {
        return new PromiseExecutorBuilder(resolveParam, rejectParam);
    }

    /**
     * Builder for Promise executor functions.
     */
    public static class PromiseExecutorBuilder {
        private final String resolveParam;
        private final String rejectParam;
        private final List<String> body = new ArrayList<>();

        PromiseExecutorBuilder(String resolveParam, String rejectParam) {
            this.resolveParam = resolveParam;
            this.rejectParam = rejectParam;
        }

        public PromiseExecutorBuilder resolve(Object value) {
            body.add(resolveParam + "(" + JS.toJs(value) + ")");
            return this;
        }

        public PromiseExecutorBuilder reject(Object error) {
            body.add(rejectParam + "(" + JS.toJs(error) + ")");
            return this;
        }

        public PromiseExecutorBuilder if_(Val condition, Object... thenStmts) {
            StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
            for (Object s : thenStmts) {
                if (s instanceof String str) {
                    sb.append(str);
                    if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
                } else {
                    sb.append(JS.toJs(s)).append(";");
                }
            }
            sb.append("}");
            body.add(sb.toString());
            return this;
        }

        public PromiseExecutorBuilder setTimeout(int ms) {
            return this;
        }

        public PromiseExecutorBuilder unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("new Promise(function(");
            sb.append(resolveParam).append(",").append(rejectParam).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith(";") && !s.endsWith("}")) sb.append(";");
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for promise chains.
     */
    public static class PromiseBuilder {
        private final String base;
        private final List<String> chain = new ArrayList<>();

        PromiseBuilder(String base) {
            this.base = base;
        }

        public PromiseBuilder then(Func handler) {
            chain.add(".then(" + handler.toExpr() + ")");
            return this;
        }

        public PromiseBuilder catch_(Func handler) {
            chain.add(".catch(" + handler.toExpr() + ")");
            return this;
        }

        public PromiseBuilder finally_(Func handler) {
            chain.add(".finally(" + handler.toExpr() + ")");
            return this;
        }

        public String build() {
            return toVal().js();
        }

        public Val toVal() {
            StringBuilder sb = new StringBuilder(base);
            for (String c : chain) {
                sb.append(c);
            }
            return new Val(sb.toString());
        }
    }

    // ==================== Delay/Sleep ====================

    /**
     * Creates a delay using setTimeout wrapped in a Promise.
     * @param ms milliseconds to delay
     * @return a Val representing the delay promise
     */
    public static Val delay(int ms) {
        return new Val("new Promise(function(r){setTimeout(r," + ms + ")})");
    }

    /**
     * Creates a sleep function that can be awaited.
     * @param ms milliseconds to sleep
     * @return a Val representing the sleep promise
     */
    public static Val sleep(int ms) {
        return delay(ms);
    }

    // ==================== Event to Promise ====================

    /**
     * Creates a promise that resolves on an event.
     *
     * @param element the element to listen on
     * @param event the event type
     * @return a Val representing the promise
     */
    public static Val onceEvent(Val element, String event) {
        return new Val("new Promise(function(r){" + element.js() + ".addEventListener('" + JS.esc(event) + "',r,{once:true})})");
    }
}
