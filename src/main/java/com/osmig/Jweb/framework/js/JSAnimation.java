package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Animation APIs: requestAnimationFrame, transitions, CSS animations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSAnimation.*;
 *
 * animationLoop("animate")
 *     .onFrame(callback("time").log(variable("time")))
 *     .build();
 *
 * transition(getElem("box"), "opacity", "0", "1", 500);
 * </pre>
 */
public final class JSAnimation {
    private JSAnimation() {}

    // ==================== requestAnimationFrame ====================

    /** requestAnimationFrame(callback) */
    public static Val raf(Func callback) {
        return new Val("requestAnimationFrame(" + callback.toExpr() + ")");
    }

    /** cancelAnimationFrame(id) */
    public static String cancelRaf(Val id) {
        return "cancelAnimationFrame(" + id.js() + ")";
    }

    /** cancelAnimationFrame(varName) */
    public static String cancelRaf(String varName) {
        return "cancelAnimationFrame(" + varName + ")";
    }

    /** Creates an animation loop */
    public static AnimationLoop animationLoop(String fnName) {
        return new AnimationLoop(fnName);
    }

    public static class AnimationLoop {
        private final String fnName;
        private Func frameCallback;
        private String stopCondition;

        AnimationLoop(String fnName) { this.fnName = fnName; }

        public AnimationLoop onFrame(Func callback) {
            this.frameCallback = callback;
            return this;
        }

        public AnimationLoop stopWhen(String condition) {
            this.stopCondition = condition;
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(fnName).append("_id;");
            sb.append("function ").append(fnName).append("(time){");
            if (stopCondition != null) {
                sb.append("if(").append(stopCondition).append("){cancelAnimationFrame(").append(fnName).append("_id);return}");
            }
            sb.append("(").append(frameCallback.toExpr()).append(")(time);");
            sb.append(fnName).append("_id=requestAnimationFrame(").append(fnName).append(")}");
            return sb.toString();
        }

        public String start() {
            return fnName + "_id=requestAnimationFrame(" + fnName + ")";
        }

        public String stop() {
            return "cancelAnimationFrame(" + fnName + "_id)";
        }
    }

    // ==================== CSS Transitions ====================

    /** Sets up a CSS transition on an element */
    public static String transition(Val elem, String property, String from, String to, int durationMs) {
        return elem.js() + ".style." + property + "='" + JS.esc(from) + "';" +
               elem.js() + ".style.transition='" + property + " " + durationMs + "ms';" +
               "setTimeout(function(){" + elem.js() + ".style." + property + "='" + JS.esc(to) + "'}," + 0 + ")";
    }

    /** Sets up transition with easing */
    public static String transition(Val elem, String property, String from, String to, int durationMs, String easing) {
        return elem.js() + ".style." + property + "='" + JS.esc(from) + "';" +
               elem.js() + ".style.transition='" + property + " " + durationMs + "ms " + easing + "';" +
               "setTimeout(function(){" + elem.js() + ".style." + property + "='" + JS.esc(to) + "'}," + 0 + ")";
    }

    /** Listens for transition end */
    public static String onTransitionEnd(Val elem, Func callback) {
        return elem.js() + ".addEventListener('transitionend'," + callback.toExpr() + ",{once:true})";
    }

    // ==================== CSS Animations ====================

    /** Adds animation class and removes after completion */
    public static String animate(Val elem, String animationClass) {
        return elem.js() + ".classList.add('" + JS.esc(animationClass) + "');" +
               elem.js() + ".addEventListener('animationend',function(){" +
               "this.classList.remove('" + JS.esc(animationClass) + "')},{once:true})";
    }

    /** Listens for animation end */
    public static String onAnimationEnd(Val elem, Func callback) {
        return elem.js() + ".addEventListener('animationend'," + callback.toExpr() + ",{once:true})";
    }

    // ==================== Easing Functions ====================

    /** Linear interpolation: lerp(a, b, t) */
    public static Val lerp(Val a, Val b, Val t) {
        return new Val("(" + a.js() + "+((" + b.js() + ")-(" + a.js() + "))*(" + t.js() + "))");
    }

    /** Ease-in-out quad function for manual animation */
    public static Val easeInOutQuad(Val t) {
        return new Val("(" + t.js() + "<0.5?2*" + t.js() + "*" + t.js() + ":1-Math.pow(-2*" + t.js() + "+2,2)/2)");
    }
}
