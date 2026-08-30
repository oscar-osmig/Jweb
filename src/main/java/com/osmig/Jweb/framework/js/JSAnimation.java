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
 *
 * @deprecated Replaced by {@code jweb.js.JSAnimation} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class JSAnimation {
    protected JSAnimation() {}

    // ==================== requestAnimationFrame ====================

    /** requestAnimationFrame(callback) */
    public static Val raf(Func callback) {
        return new Val("requestAnimationFrame(" + callback.toExpr() + ")");
    }

    /** cancelAnimationFrame(id) */
    public static Val cancelRaf(Val id) {
        return new Val("cancelAnimationFrame(" + id.js() + ")");
    }

    /** cancelAnimationFrame(varName) */
    public static Val cancelRaf(String varName) {
        return new Val("cancelAnimationFrame(" + varName + ")");
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

        public Val build() {
            StringBuilder sb = new StringBuilder();
            sb.append("var ").append(fnName).append("_id;");
            sb.append("function ").append(fnName).append("(time){");
            if (stopCondition != null) {
                sb.append("if(").append(stopCondition).append("){cancelAnimationFrame(").append(fnName).append("_id);return}");
            }
            sb.append("(").append(frameCallback.toExpr()).append(")(time);");
            sb.append(fnName).append("_id=requestAnimationFrame(").append(fnName).append(")}");
            return new Val(sb.toString());
        }

        public Val start() {
            return new Val(fnName + "_id=requestAnimationFrame(" + fnName + ")");
        }

        public Val stop() {
            return new Val("cancelAnimationFrame(" + fnName + "_id)");
        }
    }

    // ==================== CSS Transitions ====================

    /**
     * The DOM style-object key for a property: {@code background-color} and
     * {@code backgroundColor} both become {@code backgroundColor}.
     */
    static String styleKey(String property) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < property.length(); i++) {
            char c = property.charAt(i);
            if (c == '-' && i + 1 < property.length()) {
                sb.append(Character.toUpperCase(property.charAt(++i)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * The CSS name for a property: {@code backgroundColor} and
     * {@code background-color} both become {@code background-color}.
     * Needed because the {@code transition} shorthand is CSS, not JS — the
     * camelCase spelling is silently invalid there.
     */
    static String cssName(String property) {
        StringBuilder sb = new StringBuilder();
        for (char c : property.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append('-').append(Character.toLowerCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Sets up a CSS transition on an element. The property may be written in
     * either CSS ({@code background-color}) or DOM ({@code backgroundColor})
     * spelling — each half of the emitted code gets the form it needs.
     */
    public static Val transition(Val elem, String property, String from, String to, int durationMs) {
        return transition(elem, property, from, to, durationMs, null);
    }

    /** Sets up transition with easing. */
    public static Val transition(Val elem, String property, String from, String to, int durationMs, String easing) {
        String key = styleKey(property);
        String css = cssName(property);
        return new Val(elem.js() + ".style." + key + "='" + JS.esc(from) + "';" +
               elem.js() + ".style.transition='" + css + " " + durationMs + "ms"
               + (easing != null ? " " + easing : "") + "';" +
               "setTimeout(function(){" + elem.js() + ".style." + key + "='" + JS.esc(to) + "'},0)");
    }

    /** Listens for transition end */
    public static Val onTransitionEnd(Val elem, Func callback) {
        return new Val(elem.js() + ".addEventListener('transitionend'," + callback.toExpr() + ",{once:true})");
    }

    // ==================== CSS Animations ====================

    /** Adds animation class and removes after completion */
    public static Val animate(Val elem, String animationClass) {
        return new Val(elem.js() + ".classList.add('" + JS.esc(animationClass) + "');" +
               elem.js() + ".addEventListener('animationend',function(){" +
               "this.classList.remove('" + JS.esc(animationClass) + "')},{once:true})");
    }

    /** Listens for animation end */
    public static Val onAnimationEnd(Val elem, Func callback) {
        return new Val(elem.js() + ".addEventListener('animationend'," + callback.toExpr() + ",{once:true})");
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
