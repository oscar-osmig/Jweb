package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Web Animations API for programmatic animations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSWebAnimations.*;
 *
 * // Animate an element
 * animate(getElem("box"))
 *     .keyframes(
 *         keyframe().opacity(0).transform("translateX(-100px)"),
 *         keyframe().opacity(1).transform("translateX(0)")
 *     )
 *     .duration(500)
 *     .easing("ease-out")
 *     .build()
 *
 * // Control animation
 * pause(variable("anim"))
 * play(variable("anim"))
 * reverse(variable("anim"))
 * </pre>
 */
public final class JSWebAnimations {
    private JSWebAnimations() {}

    // ==================== Animation Creation ====================

    /** Creates an animation builder for an element */
    public static AnimationBuilder animate(Val element) {
        return new AnimationBuilder(element);
    }

    /** Creates an animation builder for element by ID */
    public static AnimationBuilder animate(String elementId) {
        return new AnimationBuilder(new Val("document.getElementById('" + JS.esc(elementId) + "')"));
    }

    // ==================== Animation Controls ====================

    /** Plays animation */
    public static Val play(Val animation) { return new Val(animation.js() + ".play()"); }

    /** Pauses animation */
    public static Val pause(Val animation) { return new Val(animation.js() + ".pause()"); }

    /** Reverses animation */
    public static Val reverse(Val animation) { return new Val(animation.js() + ".reverse()"); }

    /** Cancels animation */
    public static Val cancel(Val animation) { return new Val(animation.js() + ".cancel()"); }

    /** Finishes animation */
    public static Val finish(Val animation) { return new Val(animation.js() + ".finish()"); }

    /** Commits animation styles */
    public static Val commitStyles(Val animation) { return new Val(animation.js() + ".commitStyles()"); }

    /** Persists animation */
    public static Val persist(Val animation) { return new Val(animation.js() + ".persist()"); }

    // ==================== Animation Properties ====================

    /** Gets playback rate */
    public static Val playbackRate(Val animation) { return new Val(animation.js() + ".playbackRate"); }

    /** Sets playback rate */
    public static Val setPlaybackRate(Val animation, double rate) { return new Val(animation.js() + ".playbackRate=" + rate); }

    /** Gets current time */
    public static Val currentTime(Val animation) { return new Val(animation.js() + ".currentTime"); }

    /** Sets current time */
    public static Val setCurrentTime(Val animation, Val time) { return new Val(animation.js() + ".currentTime=" + time.js()); }

    /** Gets play state */
    public static Val playState(Val animation) { return new Val(animation.js() + ".playState"); }

    /** Gets finished promise */
    public static Val finished(Val animation) { return new Val(animation.js() + ".finished"); }

    /** Gets ready promise */
    public static Val ready(Val animation) { return new Val(animation.js() + ".ready"); }

    // ==================== Event Handlers ====================

    /** Sets onfinish handler */
    public static Val onFinish(Val animation, Func handler) { return new Val(animation.js() + ".onfinish=" + handler.toExpr()); }

    /** Sets oncancel handler */
    public static Val onCancel(Val animation, Func handler) { return new Val(animation.js() + ".oncancel=" + handler.toExpr()); }

    /** Sets onremove handler */
    public static Val onRemove(Val animation, Func handler) { return new Val(animation.js() + ".onremove=" + handler.toExpr()); }

    // ==================== Get Animations ====================

    /** Gets all animations on element */
    public static Val getAnimations(Val element) { return new Val(element.js() + ".getAnimations()"); }

    /** Gets all document animations */
    public static Val getAllAnimations() { return new Val("document.getAnimations()"); }

    // ==================== High-Resolution Time ====================

    /** Gets performance.now() */
    public static Val performanceNow() { return new Val("performance.now()"); }

    // ==================== Keyframe Builder ====================

    /** Creates a keyframe */
    public static KeyframeBuilder keyframe() { return new KeyframeBuilder(); }

    /** Creates a keyframe at specific offset (0-1) */
    public static KeyframeBuilder keyframe(double offset) { return new KeyframeBuilder().offset(offset); }

    public static class KeyframeBuilder {
        private final StringBuilder props = new StringBuilder("{");
        private boolean hasProps = false;

        public KeyframeBuilder offset(double offset) { return addProp("offset", String.valueOf(offset)); }
        public KeyframeBuilder easing(String easing) { return addProp("easing", "'" + JS.esc(easing) + "'"); }

        // Common animatable properties
        public KeyframeBuilder opacity(double val) { return addProp("opacity", String.valueOf(val)); }
        public KeyframeBuilder transform(String val) { return addProp("transform", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder translate(String val) { return addProp("translate", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder rotate(String val) { return addProp("rotate", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder scale(String val) { return addProp("scale", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder width(String val) { return addProp("width", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder height(String val) { return addProp("height", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder color(String val) { return addProp("color", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder backgroundColor(String val) { return addProp("backgroundColor", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder borderColor(String val) { return addProp("borderColor", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder borderRadius(String val) { return addProp("borderRadius", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder boxShadow(String val) { return addProp("boxShadow", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder margin(String val) { return addProp("margin", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder padding(String val) { return addProp("padding", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder top(String val) { return addProp("top", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder left(String val) { return addProp("left", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder right(String val) { return addProp("right", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder bottom(String val) { return addProp("bottom", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder filter(String val) { return addProp("filter", "'" + JS.esc(val) + "'"); }
        public KeyframeBuilder clipPath(String val) { return addProp("clipPath", "'" + JS.esc(val) + "'"); }

        /** Adds custom CSS property */
        public KeyframeBuilder prop(String name, String value) { return addProp(name, "'" + JS.esc(value) + "'"); }
        public KeyframeBuilder prop(String name, double value) { return addProp(name, String.valueOf(value)); }

        private KeyframeBuilder addProp(String name, String value) {
            if (hasProps) props.append(",");
            props.append(name).append(":").append(value);
            hasProps = true;
            return this;
        }

        String build() { return props.append("}").toString(); }
    }

    // ==================== Animation Builder ====================

    public static class AnimationBuilder {
        private final Val element;
        private final StringBuilder keyframes = new StringBuilder("[");
        private boolean hasKeyframes = false;
        private int duration = 300;
        private String easing = "ease";
        private int delay = 0;
        private int endDelay = 0;
        private Object iterations = 1;
        private String direction = "normal";
        private String fill = "none";
        private Func onFinishFunc, onCancelFunc;

        AnimationBuilder(Val element) { this.element = element; }

        public AnimationBuilder keyframes(KeyframeBuilder... frames) {
            for (KeyframeBuilder frame : frames) {
                if (hasKeyframes) keyframes.append(",");
                keyframes.append(frame.build());
                hasKeyframes = true;
            }
            return this;
        }

        public AnimationBuilder duration(int ms) { this.duration = ms; return this; }
        public AnimationBuilder easing(String easing) { this.easing = easing; return this; }
        public AnimationBuilder delay(int ms) { this.delay = ms; return this; }
        public AnimationBuilder endDelay(int ms) { this.endDelay = ms; return this; }
        public AnimationBuilder iterations(int count) { this.iterations = count; return this; }
        public AnimationBuilder infinite() { this.iterations = "Infinity"; return this; }
        public AnimationBuilder direction(String dir) { this.direction = dir; return this; }
        public AnimationBuilder alternate() { this.direction = "alternate"; return this; }
        public AnimationBuilder fill(String fill) { this.fill = fill; return this; }
        public AnimationBuilder fillForwards() { this.fill = "forwards"; return this; }
        public AnimationBuilder fillBackwards() { this.fill = "backwards"; return this; }
        public AnimationBuilder fillBoth() { this.fill = "both"; return this; }
        public AnimationBuilder onFinish(Func handler) { this.onFinishFunc = handler; return this; }
        public AnimationBuilder onCancel(Func handler) { this.onCancelFunc = handler; return this; }

        /** Builds and assigns to variable */
        public Val build(String varName) {
            StringBuilder sb = new StringBuilder("var ").append(varName).append("=").append(buildAnimation());
            if (onFinishFunc != null) sb.append(";").append(varName).append(".onfinish=").append(onFinishFunc.toExpr());
            if (onCancelFunc != null) sb.append(";").append(varName).append(".oncancel=").append(onCancelFunc.toExpr());
            return new Val(sb.toString());
        }

        /** Builds the animation */
        public Val build() {
            if (onFinishFunc != null || onCancelFunc != null) {
                StringBuilder sb = new StringBuilder("(function(){var a=").append(buildAnimation()).append(";");
                if (onFinishFunc != null) sb.append("a.onfinish=").append(onFinishFunc.toExpr()).append(";");
                if (onCancelFunc != null) sb.append("a.oncancel=").append(onCancelFunc.toExpr()).append(";");
                sb.append("return a}())");
                return new Val(sb.toString());
            }
            return new Val(buildAnimation());
        }

        private String buildAnimation() {
            StringBuilder sb = new StringBuilder(element.js()).append(".animate(");
            sb.append(keyframes).append("],{");
            sb.append("duration:").append(duration);
            sb.append(",easing:'").append(JS.esc(easing)).append("'");
            if (delay != 0) sb.append(",delay:").append(delay);
            if (endDelay != 0) sb.append(",endDelay:").append(endDelay);
            sb.append(",iterations:").append(iterations);
            if (!"normal".equals(direction)) sb.append(",direction:'").append(direction).append("'");
            if (!"none".equals(fill)) sb.append(",fill:'").append(fill).append("'");
            return sb.append("})").toString();
        }
    }
}
