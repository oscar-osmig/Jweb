package com.osmig.Jweb.framework.js;

import java.util.ArrayList;
import java.util.List;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Promise utilities for advanced async patterns.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSPromise.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Promise.withResolvers pattern
 * deferred().build("promise", "resolve", "reject")
 *
 * // Cancellable promise
 * cancellable(fetch("/api/data").get().toVal())
 *     .controller("ctrl")
 *     .timeout(5000)
 *     .build("result")
 *
 * // Timeout promise
 * timeout(fetch("/api/slow").get().toVal(), 3000)
 *     .onTimeout(call("console.log", "Request timed out"))
 *     .build()
 *
 * // Retry logic
 * retry(variable("apiCall"), 3)
 *     .delay(1000)
 *     .onRetry(callback("attempt").log("Retry attempt:", variable("attempt")))
 *     .build()
 *
 * // Promise combinators
 * promiseAll(
 *     fetch("/api/users").get().toVal(),
 *     fetch("/api/posts").get().toVal()
 * ).then(callback("results").log(variable("results")))
 *
 * // Chained helpers
 * promise(fetch("/api/data").get().toVal())
 *     .then(callback("r").ret(variable("r").dot("json").invoke()))
 *     .catch_(callback("e").log(variable("e")))
 *     .finally_(callback().log("Done"))
 *     .build()
 * </pre>
 */
public final class JSPromise {
    private JSPromise() {}

    // ==================== Promise.withResolvers ====================

    /**
     * Creates a deferred promise using Promise.withResolvers().
     * Returns an object with {promise, resolve, reject}.
     *
     * @return a DeferredBuilder
     */
    public static DeferredBuilder deferred() {
        return new DeferredBuilder();
    }

    /**
     * Builder for deferred promises using Promise.withResolvers().
     */
    public static class DeferredBuilder {
        /**
         * Builds the deferred promise and assigns to variables.
         *
         * @param promiseVar variable name for the promise
         * @param resolveVar variable name for the resolve function
         * @param rejectVar variable name for the reject function
         * @return a Val representing the assignment statements
         */
        public Val build(String promiseVar, String resolveVar, String rejectVar) {
            return new Val(
                "var _d=Promise.withResolvers();" +
                "var " + promiseVar + "=_d.promise;" +
                "var " + resolveVar + "=_d.resolve;" +
                "var " + rejectVar + "=_d.reject"
            );
        }

        /**
         * Returns just the withResolvers() expression.
         */
        public Val toVal() {
            return new Val("Promise.withResolvers()");
        }
    }

    // ==================== Cancellable Promises ====================

    /**
     * Creates a cancellable promise using AbortController.
     *
     * @param promise the promise to make cancellable
     * @return a CancellableBuilder
     */
    public static CancellableBuilder cancellable(Val promise) {
        return new CancellableBuilder(promise);
    }

    /**
     * Builder for cancellable promises.
     */
    public static class CancellableBuilder {
        private final Val promise;
        private String controllerVar;
        private Integer timeoutMs;
        private Func onCancel;

        CancellableBuilder(Val promise) {
            this.promise = promise;
        }

        /**
         * Sets the AbortController variable name.
         */
        public CancellableBuilder controller(String varName) {
            this.controllerVar = varName;
            return this;
        }

        /**
         * Sets auto-cancel timeout.
         */
        public CancellableBuilder timeout(int ms) {
            this.timeoutMs = ms;
            return this;
        }

        /**
         * Sets cancel handler.
         */
        public CancellableBuilder onCancel(Func handler) {
            this.onCancel = handler;
            return this;
        }

        /**
         * Builds the cancellable promise.
         *
         * @param resultVar variable to store the promise
         * @return a Val representing the complete setup
         */
        public Val build(String resultVar) {
            String ctrl = controllerVar != null ? controllerVar : "_ctrl";
            StringBuilder sb = new StringBuilder();

            sb.append("var ").append(ctrl).append("=new AbortController();");

            if (timeoutMs != null) {
                sb.append("setTimeout(function(){").append(ctrl).append(".abort()},").append(timeoutMs).append(");");
            }

            if (onCancel != null) {
                sb.append(ctrl).append(".signal.addEventListener('abort',").append(onCancel.toExpr()).append(");");
            }

            sb.append("var ").append(resultVar).append("=").append(promise.js()).append(";");

            return new Val(sb.toString());
        }
    }

    // ==================== Timeout Promises ====================

    /**
     * Creates a promise that races against a timeout.
     *
     * @param promise the promise to race
     * @param timeoutMs timeout in milliseconds
     * @return a TimeoutBuilder
     */
    public static TimeoutBuilder timeout(Val promise, int timeoutMs) {
        return new TimeoutBuilder(promise, timeoutMs);
    }

    /**
     * Builder for timeout promises.
     */
    public static class TimeoutBuilder {
        private final Val promise;
        private final int timeoutMs;
        private Val onTimeoutHandler;
        private String errorMessage = "Timeout";

        TimeoutBuilder(Val promise, int timeoutMs) {
            this.promise = promise;
            this.timeoutMs = timeoutMs;
        }

        /**
         * Sets custom timeout error message.
         */
        public TimeoutBuilder errorMessage(String message) {
            this.errorMessage = message;
            return this;
        }

        /**
         * Sets handler to call on timeout.
         */
        public TimeoutBuilder onTimeout(Val handler) {
            this.onTimeoutHandler = handler;
            return this;
        }

        /**
         * Builds the timeout promise.
         */
        public Val build() {
            StringBuilder sb = new StringBuilder("Promise.race([");
            sb.append(promise.js()).append(",");
            sb.append("new Promise(function(_,r){setTimeout(function(){");

            if (onTimeoutHandler != null) {
                sb.append(onTimeoutHandler.js()).append(";");
            }

            sb.append("r(new Error('").append(JS.esc(errorMessage)).append("'))");
            sb.append("},").append(timeoutMs).append(")})");
            sb.append("])");

            return new Val(sb.toString());
        }
    }

    // ==================== Retry Logic ====================

    /**
     * Creates a promise retry mechanism.
     *
     * @param promiseFactory function that returns a promise
     * @param maxAttempts maximum retry attempts
     * @return a RetryBuilder
     */
    public static RetryBuilder retry(Val promiseFactory, int maxAttempts) {
        return new RetryBuilder(promiseFactory, maxAttempts);
    }

    /**
     * Builder for retry logic.
     */
    public static class RetryBuilder {
        private final Val promiseFactory;
        private final int maxAttempts;
        private Integer delayMs;
        private Func onRetry;
        private Func shouldRetry;
        private boolean exponentialBackoff = false;

        RetryBuilder(Val promiseFactory, int maxAttempts) {
            this.promiseFactory = promiseFactory;
            this.maxAttempts = maxAttempts;
        }

        /**
         * Sets delay between retries in milliseconds.
         */
        public RetryBuilder delay(int ms) {
            this.delayMs = ms;
            return this;
        }

        /**
         * Sets exponential backoff (delay doubles each retry).
         */
        public RetryBuilder exponentialBackoff() {
            this.exponentialBackoff = true;
            return this;
        }

        /**
         * Sets callback on each retry attempt.
         */
        public RetryBuilder onRetry(Func callback) {
            this.onRetry = callback;
            return this;
        }

        /**
         * Sets condition to determine if should retry.
         */
        public RetryBuilder shouldRetry(Func condition) {
            this.shouldRetry = condition;
            return this;
        }

        /**
         * Builds the retry function and returns it as a Val.
         */
        public Val build() {
            StringBuilder sb = new StringBuilder();
            sb.append("(function(){");
            sb.append("var attempt=0;");
            sb.append("var retry=function(){");
            sb.append("return ").append(promiseFactory.js()).append(".catch(function(err){");
            sb.append("attempt++;");

            if (onRetry != null) {
                sb.append("(").append(onRetry.toExpr()).append(")(attempt);");
            }

            sb.append("if(attempt>=").append(maxAttempts).append(")throw err;");

            if (shouldRetry != null) {
                sb.append("if(!(").append(shouldRetry.toExpr()).append(")(err))throw err;");
            }

            if (delayMs != null && delayMs > 0) {
                sb.append("var delay=").append(delayMs);
                if (exponentialBackoff) {
                    sb.append("*Math.pow(2,attempt-1)");
                }
                sb.append(";");
                sb.append("return new Promise(function(r){setTimeout(r,delay)}).then(retry);");
            } else {
                sb.append("return retry();");
            }

            sb.append("});");
            sb.append("};");
            sb.append("return retry();");
            sb.append("})()");

            return new Val(sb.toString());
        }
    }

    // ==================== Promise Combinators ====================

    /**
     * Creates Promise.all() with multiple promises.
     * Resolves when all promises resolve, rejects if any reject.
     */
    public static PromiseChainBuilder promiseAll(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.all([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseChainBuilder(sb.toString());
    }

    /**
     * Creates Promise.race() with multiple promises.
     * Resolves/rejects with the first settled promise.
     */
    public static PromiseChainBuilder promiseRace(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.race([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseChainBuilder(sb.toString());
    }

    /**
     * Creates Promise.allSettled() with multiple promises.
     * Resolves when all promises settle (resolve or reject).
     */
    public static PromiseChainBuilder promiseAllSettled(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.allSettled([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseChainBuilder(sb.toString());
    }

    /**
     * Creates Promise.any() with multiple promises.
     * Resolves with the first resolved promise, rejects if all reject.
     */
    public static PromiseChainBuilder promiseAny(Val... promises) {
        StringBuilder sb = new StringBuilder("Promise.any([");
        for (int i = 0; i < promises.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(promises[i].js());
        }
        sb.append("])");
        return new PromiseChainBuilder(sb.toString());
    }

    /**
     * Creates Promise.resolve() with a value.
     */
    public static PromiseChainBuilder promiseResolve(Object value) {
        return new PromiseChainBuilder("Promise.resolve(" + JS.toJs(value) + ")");
    }

    /**
     * Creates Promise.reject() with an error.
     */
    public static PromiseChainBuilder promiseReject(Object error) {
        return new PromiseChainBuilder("Promise.reject(" + JS.toJs(error) + ")");
    }

    // ==================== Promise Chaining Helpers ====================

    /**
     * Wraps a promise for chaining helpers.
     *
     * @param promise the promise to wrap
     * @return a PromiseChainBuilder
     */
    public static PromiseChainBuilder promise(Val promise) {
        return new PromiseChainBuilder(promise.js());
    }

    /**
     * Builder for promise chains with .then, .catch, .finally.
     */
    public static class PromiseChainBuilder {
        private final String base;
        private final List<String> chain = new ArrayList<>();

        PromiseChainBuilder(String base) {
            this.base = base;
        }

        /**
         * Adds a then handler to the promise chain.
         */
        public PromiseChainBuilder then(Func handler) {
            chain.add(".then(" + handler.toExpr() + ")");
            return this;
        }

        /**
         * Adds a then handler with raw code.
         */
        public PromiseChainBuilder then(String code) {
            chain.add(".then(function(v){" + code + "})");
            return this;
        }

        /**
         * Adds a catch handler for errors.
         */
        public PromiseChainBuilder catch_(Func handler) {
            chain.add(".catch(" + handler.toExpr() + ")");
            return this;
        }

        /**
         * Adds a catch handler with raw code.
         */
        public PromiseChainBuilder catch_(String code) {
            chain.add(".catch(function(e){" + code + "})");
            return this;
        }

        /**
         * Adds a finally handler.
         */
        public PromiseChainBuilder finally_(Func handler) {
            chain.add(".finally(" + handler.toExpr() + ")");
            return this;
        }

        /**
         * Adds a finally handler with raw code.
         */
        public PromiseChainBuilder finally_(String code) {
            chain.add(".finally(function(){" + code + "})");
            return this;
        }

        /**
         * Adds .then(r => r.json()) to parse JSON response.
         */
        public PromiseChainBuilder json() {
            chain.add(".then(function(r){return r.json()})");
            return this;
        }

        /**
         * Adds .then(r => r.text()) to get text response.
         */
        public PromiseChainBuilder text() {
            chain.add(".then(function(r){return r.text()})");
            return this;
        }

        /**
         * Adds .then(r => r.blob()) to get blob response.
         */
        public PromiseChainBuilder blob() {
            chain.add(".then(function(r){return r.blob()})");
            return this;
        }

        /**
         * Adds a tap handler that doesn't modify the value.
         */
        public PromiseChainBuilder tap(Func handler) {
            chain.add(".then(function(v){(" + handler.toExpr() + ")(v);return v})");
            return this;
        }

        /**
         * Builds the promise chain as a statement.
         */
        public String build() {
            return toVal().js();
        }

        /**
         * Returns the promise chain as a Val for use in other expressions.
         */
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

    /**
     * Creates a promise that resolves on an event with a value extractor.
     *
     * @param element the element to listen on
     * @param event the event type
     * @param extractor function to extract value from event
     * @return a Val representing the promise
     */
    public static Val onceEvent(Val element, String event, Func extractor) {
        return new Val("new Promise(function(r){" + element.js() + ".addEventListener('" + JS.esc(event) +
            "',function(e){r((" + extractor.toExpr() + ")(e))},{once:true})})");
    }

    // ==================== Promise Constructor ====================

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
     * Creates a new Promise with default parameter names.
     */
    public static PromiseExecutorBuilder newPromise() {
        return new PromiseExecutorBuilder("resolve", "reject");
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

        /**
         * Calls resolve with a value.
         */
        public PromiseExecutorBuilder resolve(Object value) {
            body.add(resolveParam + "(" + JS.toJs(value) + ")");
            return this;
        }

        /**
         * Calls reject with an error.
         */
        public PromiseExecutorBuilder reject(Object error) {
            body.add(rejectParam + "(" + JS.toJs(error) + ")");
            return this;
        }

        /**
         * Adds an if statement.
         */
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

        /**
         * Adds setTimeout to delay execution.
         */
        public PromiseExecutorBuilder setTimeout(int ms, Object... stmts) {
            StringBuilder sb = new StringBuilder("setTimeout(function(){");
            for (Object s : stmts) {
                if (s instanceof String str) {
                    sb.append(str);
                    if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
                } else {
                    sb.append(JS.toJs(s)).append(";");
                }
            }
            sb.append("},").append(ms).append(")");
            body.add(sb.toString());
            return this;
        }

        /**
         * Adds raw JavaScript.
         */
        public PromiseExecutorBuilder unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        /**
         * Builds the promise.
         */
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

    // ==================== Promise Utilities ====================

    /**
     * Creates a promise that resolves after all promises in array resolve.
     */
    public static Val promiseAllArray(Val promisesArray) {
        return new Val("Promise.all(" + promisesArray.js() + ")");
    }

    /**
     * Creates a promise that races promises in array.
     */
    public static Val promiseRaceArray(Val promisesArray) {
        return new Val("Promise.race(" + promisesArray.js() + ")");
    }

    /**
     * Creates a promise that waits for all promises to settle.
     */
    public static Val promiseAllSettledArray(Val promisesArray) {
        return new Val("Promise.allSettled(" + promisesArray.js() + ")");
    }

    /**
     * Creates a promise that resolves with first resolved promise.
     */
    public static Val promiseAnyArray(Val promisesArray) {
        return new Val("Promise.any(" + promisesArray.js() + ")");
    }

    // ==================== Advanced Patterns ====================

    /**
     * Creates a debounced promise that only executes the last call.
     *
     * @param promiseFactory function that creates a promise
     * @param delayMs debounce delay in milliseconds
     * @param timerVar variable name for the timer
     * @return a Val representing the debounced promise wrapper
     */
    public static Val debouncePromise(Val promiseFactory, int delayMs, String timerVar) {
        StringBuilder sb = new StringBuilder("(function(){");
        sb.append("clearTimeout(").append(timerVar).append(");");
        sb.append("return new Promise(function(resolve,reject){");
        sb.append(timerVar).append("=setTimeout(function(){");
        sb.append(promiseFactory.js()).append(".then(resolve).catch(reject)");
        sb.append("},").append(delayMs).append(")");
        sb.append("})");
        sb.append("})()");
        return new Val(sb.toString());
    }

    /**
     * Creates a throttled promise that limits execution rate.
     *
     * @param promiseFactory function that creates a promise
     * @param delayMs minimum delay between executions
     * @param lastCallVar variable name to track last call
     * @return a Val representing the throttled promise wrapper
     */
    public static Val throttlePromise(Val promiseFactory, int delayMs, String lastCallVar) {
        StringBuilder sb = new StringBuilder("(function(){");
        sb.append("var now=Date.now();");
        sb.append("if(!").append(lastCallVar).append("||now-").append(lastCallVar).append(">=").append(delayMs).append("){");
        sb.append(lastCallVar).append("=now;");
        sb.append("return ").append(promiseFactory.js()).append(";");
        sb.append("}");
        sb.append("return Promise.resolve();");
        sb.append("})()");
        return new Val(sb.toString());
    }

    /**
     * Creates a memoized promise that caches the result.
     *
     * @param promiseFactory function that creates a promise
     * @param cacheVar variable name for the cache
     * @return a Val representing the memoized promise wrapper
     */
    public static Val memoizePromise(Val promiseFactory, String cacheVar) {
        StringBuilder sb = new StringBuilder("(function(){");
        sb.append("if(").append(cacheVar).append(")return Promise.resolve(").append(cacheVar).append(");");
        sb.append("return ").append(promiseFactory.js()).append(".then(function(v){");
        sb.append(cacheVar).append("=v;return v");
        sb.append("})");
        sb.append("})()");
        return new Val(sb.toString());
    }
}
