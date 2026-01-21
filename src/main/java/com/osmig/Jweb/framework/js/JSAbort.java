package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * AbortController API for cancellable async operations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSAbort.*;
 *
 * // Create controller and use with fetch
 * abortController().build("ctrl")
 *
 * fetch("/api/data")
 *     .signal(variable("ctrl"))
 *     .get()
 *
 * // Abort after timeout
 * abortAfterTimeout(5000).build("ctrl")
 *
 * // Cancel request
 * abort(variable("ctrl"))
 * </pre>
 */
public final class JSAbort {
    private JSAbort() {}

    // ==================== Controller Creation ====================

    /** Creates an AbortController */
    public static AbortControllerBuilder abortController() {
        return new AbortControllerBuilder();
    }

    /** Creates controller with timeout (auto-abort) */
    public static Val abortAfterTimeout(int timeoutMs) {
        return new Val("AbortSignal.timeout(" + timeoutMs + ")");
    }

    /** Creates already aborted signal with reason */
    public static Val abortedSignal(String reason) {
        return new Val("AbortSignal.abort('" + JS.esc(reason) + "')");
    }

    /** Creates already aborted signal with dynamic reason */
    public static Val abortedSignal(Val reason) {
        return new Val("AbortSignal.abort(" + reason.js() + ")");
    }

    /** Creates signal that aborts when any of the signals abort */
    public static Val anySignal(Val... signals) {
        StringBuilder sb = new StringBuilder("AbortSignal.any([");
        for (int i = 0; i < signals.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(signals[i].js());
        }
        return new Val(sb.append("])").toString());
    }

    // ==================== Operations ====================

    /** Aborts the controller */
    public static Val abort(Val controller) {
        return new Val(controller.js() + ".abort()");
    }

    /** Aborts with reason */
    public static Val abort(Val controller, String reason) {
        return new Val(controller.js() + ".abort('" + JS.esc(reason) + "')");
    }

    /** Aborts with dynamic reason */
    public static Val abort(Val controller, Val reason) {
        return new Val(controller.js() + ".abort(" + reason.js() + ")");
    }

    /** Gets the signal from controller */
    public static Val signal(Val controller) {
        return new Val(controller.js() + ".signal");
    }

    // ==================== Signal Properties ====================

    /** Checks if signal is aborted */
    public static Val isAborted(Val signal) {
        return new Val(signal.js() + ".aborted");
    }

    /** Gets abort reason */
    public static Val reason(Val signal) {
        return new Val(signal.js() + ".reason");
    }

    /** Throws if aborted */
    public static Val throwIfAborted(Val signal) {
        return new Val(signal.js() + ".throwIfAborted()");
    }

    /** Adds abort event listener */
    public static Val onAbort(Val signal, Func handler) {
        return new Val(signal.js() + ".addEventListener('abort'," + handler.toExpr() + ")");
    }

    /** Adds abort event listener with raw code */
    public static Val onAbort(Val signal, String code) {
        return new Val(signal.js() + ".addEventListener('abort',function(){" + code + "})");
    }

    // ==================== Builder ====================

    public static class AbortControllerBuilder {
        private Func onAbortFunc;
        private String onAbortCode;
        private Integer timeout;

        /** Sets abort handler */
        public AbortControllerBuilder onAbort(Func handler) {
            this.onAbortFunc = handler;
            return this;
        }

        /** Sets abort handler with raw code */
        public AbortControllerBuilder onAbort(String code) {
            this.onAbortCode = code;
            return this;
        }

        /** Sets auto-abort timeout */
        public AbortControllerBuilder timeout(int ms) {
            this.timeout = ms;
            return this;
        }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(varName).append("=new AbortController();");

            if (onAbortFunc != null) {
                sb.append(varName).append(".signal.addEventListener('abort',").append(onAbortFunc.toExpr()).append(");");
            } else if (onAbortCode != null) {
                sb.append(varName).append(".signal.addEventListener('abort',function(){").append(onAbortCode).append("});");
            }

            if (timeout != null) {
                sb.append("setTimeout(function(){").append(varName).append(".abort()},").append(timeout).append(");");
            }

            return new Val(sb.toString());
        }

        /** Returns just the controller expression */
        public Val toVal() {
            return new Val("new AbortController()");
        }
    }
}
