package com.osmig.Jweb.framework.styles;

import static com.osmig.Jweb.framework.styles.CSSUnits.*;

/**
 * Comprehensive CSS Animation utilities for the Jweb framework.
 * Provides type-safe builders for CSS animations, transitions, timing functions, and pre-built animation presets.
 *
 * <p><b>Usage with static import:</b></p>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSSAnimations.*;
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * </pre>
 *
 * <p><b>Animation Property Builders:</b></p>
 * <pre>
 * // Simple animation
 * style().animation(animName("fadeIn"), s(1), ease)
 *
 * // Full animation with all properties
 * style().animation(
 *     animName("bounce"),
 *     duration(s(2)),
 *     timingEaseInOut,
 *     delay(ms(500)),
 *     iterationCount(3),
 *     directionAlternate,
 *     fillModeForwards
 * )
 * </pre>
 *
 * <p><b>Keyframes Builder:</b></p>
 * <pre>
 * // Use existing Keyframes class for @keyframes definitions
 * Keyframes.keyframes("fadeIn")
 *     .from(style().opacity(0))
 *     .to(style().opacity(1))
 * </pre>
 *
 * <p><b>Pre-built Animations</b> — every one names a {@code @keyframes} block
 * that {@link Keyframes} can emit, so remember to add the keyframes to your
 * stylesheet:</p>
 * <pre>
 * stylesheet().keyframes(Keyframes.fadeIn());
 *
 * style().animation(fadeIn(s(1)))
 * style().animation(fadeOut(s(0.5)))
 * style().animation(slideInLeft(s(0.6)))
 * style().animation(zoomIn(s(0.5)))
 * style().animation(pulse(s(1.5)).iterationCount(infinite))
 * style().animation(spin(s(2)).iterationCount(infinite))
 * </pre>
 *
 * <p><b>Transition Utilities:</b></p>
 * <pre>
 * // Simple transition
 * style().transition(trans(propAll, s(0.3), ease))
 *
 * // Multiple transitions
 * style().transition(transitions(
 *     trans(propColor, s(0.2), easeOut),
 *     trans(propTransform, s(0.3), easeInOut)
 * ))
 *
 * // Transition with behavior
 * style().transitionProperty(propAll)
 *        .transitionDuration(s(0.3))
 *        .transitionTimingFunction(cubicBezier(0.4, 0, 0.2, 1))
 *        .transitionBehavior(allowDiscrete)
 * </pre>
 *
 * <p><b>Scroll-Driven Animations:</b></p>
 * <pre>
 * // Scroll-triggered animation
 * style().animation(fadeIn(s(1)))
 *        .animationTimeline(scrollTimeline())
 *        .animationRange("entry", "exit")
 *
 * // View-based animation
 * style().animation(slideInLeft(s(1)))
 *        .animationTimeline(viewTimeline())
 *        .animationRange("cover 0%", "cover 100%")
 * </pre>
 *
 * @see Keyframes for @keyframes rule builder
 * @see CSS for animation property methods
 * @see Style for inline animation styles
 *
 * @deprecated Replaced by {@code jweb.Css} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class CSSAnimations extends CSSGrid {

    protected CSSAnimations() {}

    // ==================== Animation Name ====================

    /**
     * Creates an animation name reference.
     * Use this to reference a keyframes animation by name.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationName(animName("fadeIn"))
     * // Output: animation-name: fadeIn;
     * </pre>
     *
     * @param name the animation name matching a @keyframes rule
     * @return CSSValue for the animation name
     *
     * @deprecated Use {@code CSS.anim(name)} (or {@code raw(name)}) — this is a pure alias for it.
     */
    @Deprecated
    public static CSSValue animName(String name) {
        return () -> name;
    }

    // ==================== Timing Functions ====================

    /**
     * Standard ease timing function - slow start, fast middle, slow end (default).
     * Equivalent to cubic-bezier(0.25, 0.1, 0.25, 1).
     *
     * @deprecated Use the bare {@code ease} constant.
     */
    @Deprecated
    public static final CSSValue timingEase = () -> "ease";

    /**
     * Linear timing function - constant speed throughout.
     * Equivalent to cubic-bezier(0, 0, 1, 1).
     *
     * @deprecated Use the bare {@code linear} constant.
     */
    @Deprecated
    public static final CSSValue timingLinear = () -> "linear";

    /**
     * Ease-in timing function - slow start, then accelerates.
     * Equivalent to cubic-bezier(0.42, 0, 1, 1).
     *
     * @deprecated Use the bare {@code easeIn} constant.
     */
    @Deprecated
    public static final CSSValue timingEaseIn = () -> "ease-in";

    /**
     * Ease-out timing function - fast start, then decelerates.
     * Equivalent to cubic-bezier(0, 0, 0.58, 1).
     *
     * @deprecated Use the bare {@code easeOut} constant.
     */
    @Deprecated
    public static final CSSValue timingEaseOut = () -> "ease-out";

    /**
     * Ease-in-out timing function - slow start and end.
     * Equivalent to cubic-bezier(0.42, 0, 0.58, 1).
     *
     * @deprecated Use the bare {@code easeInOut} constant.
     */
    @Deprecated
    public static final CSSValue timingEaseInOut = () -> "ease-in-out";

    /**
     * Creates a custom cubic-bezier timing function.
     * Define custom easing curves with control points.
     *
     * <p>Example:</p>
     * <pre>
     * // Material Design easing
     * timingCubicBezier(0.4, 0, 0.2, 1)  // Fast out, slow in
     *
     * // Bounce effect
     * timingCubicBezier(0.68, -0.55, 0.265, 1.55)
     * </pre>
     *
     * @param x1 first control point X (0-1)
     * @param y1 first control point Y (can be outside 0-1 for bounce effects)
     * @param x2 second control point X (0-1)
     * @param y2 second control point Y (can be outside 0-1 for bounce effects)
     * @return CSSValue for cubic-bezier() function
     *
     * @deprecated Use {@code cubicBezier(x1, y1, x2, y2)} — byte-identical output.
     */
    @Deprecated
    public static CSSValue timingCubicBezier(double x1, double y1, double x2, double y2) {
        return () -> "cubic-bezier(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
    }

    /**
     * Creates a steps() timing function for frame-by-frame animations.
     *
     * <p>Example:</p>
     * <pre>
     * // 5 equal steps, jump at end
     * style().animationTimingFunction(timingSteps(5, "end"))
     *
     * // Sprite sheet animation
     * style().animationTimingFunction(timingSteps(10, "start"))
     * </pre>
     *
     * @param count number of steps (intervals)
     * @param position jump position: "start", "end", "jump-start", "jump-end", "jump-none", "jump-both"
     * @return CSSValue for steps() function
     *
     * @deprecated Use {@link CSSUnits#steps(int, String)} — byte-identical output.
     */
    @Deprecated
    public static CSSValue timingSteps(int count, String position) {
        return () -> "steps(" + count + ", " + position + ")";
    }

    /**
     * Creates a steps() timing function with default "end" position.
     *
     * @param count number of steps
     * @return CSSValue for steps() function
     *
     * @deprecated Use {@link CSSUnits#steps(int)} — byte-identical output.
     */
    @Deprecated
    public static CSSValue timingSteps(int count) {
        return () -> "steps(" + count + ")";
    }

    /**
     * Step-start timing - jumps immediately to the end state.
     * Equivalent to steps(1, start).
     *
     * @deprecated Use the bare {@code stepStart} constant.
     */
    @Deprecated
    public static final CSSValue timingStepStart = () -> "step-start";

    /**
     * Step-end timing - holds the start state until the end.
     * Equivalent to steps(1, end).
     *
     * @deprecated Use the bare {@code stepEnd} constant.
     */
    @Deprecated
    public static final CSSValue timingStepEnd = () -> "step-end";

    /** Step-start timing — jumps immediately to the end state. Equivalent to {@code steps(1, start)}. */
    public static final CSSValue stepStart = () -> "step-start";

    /** Step-end timing — holds the start state until the end. Equivalent to {@code steps(1, end)}. */
    public static final CSSValue stepEnd = () -> "step-end";

    // ==================== Duration & Delay ====================

    /**
     * Creates a duration value for animations.
     * Alias for clarity when used with animation properties.
     *
     * @param value time value (use s() or ms())
     * @return CSSValue for duration
     *
     * @deprecated Identity wrapper — pass the time value straight through, e.g. {@code .animationDuration(s(2))}.
     */
    @Deprecated
    public static CSSValue duration(jweb.CSSValue value) {
        return value::css;
    }

    /**
     * Creates a delay value for animations.
     * Alias for clarity when used with animation properties.
     *
     * @param value time value (use s() or ms())
     * @return CSSValue for delay
     *
     * @deprecated Identity wrapper — pass the time value straight through, e.g. {@code .animationDelay(ms(300))}.
     */
    @Deprecated
    public static CSSValue delay(jweb.CSSValue value) {
        return value::css;
    }

    // ==================== Iteration Count ====================

    /**
     * Animation runs infinitely.
     *
     * @deprecated Use the bare {@code infinite} constant.
     */
    @Deprecated
    public static final CSSValue iterationInfinite = () -> "infinite";

    /**
     * Creates an iteration count for animations.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationIterationCount(iterationCount(3))  // Runs 3 times
     * style().animationIterationCount(iterationCount(2.5))  // Runs 2.5 times
     * </pre>
     *
     * @param count number of times to run (can be fractional)
     * @return CSSValue for iteration count
     */
    public static CSSValue iterationCount(double count) {
        if (count == (int) count) {
            return () -> String.valueOf((int) count);
        }
        return () -> String.valueOf(count);
    }

    /**
     * Creates an iteration count for animations.
     *
     * @param count number of times to run
     * @return CSSValue for iteration count
     */
    public static CSSValue iterationCount(int count) {
        return () -> String.valueOf(count);
    }

    // ==================== Animation Direction ====================

    /**
     * Animation plays normally (forward) each cycle.
     *
     * @deprecated Use the bare {@code normal} constant.
     */
    @Deprecated
    public static final CSSValue directionNormal = () -> "normal";

    /**
     * Animation plays in reverse each cycle.
     *
     * @deprecated Use the bare {@code reverse} constant.
     */
    @Deprecated
    public static final CSSValue directionReverse = () -> "reverse";

    /**
     * Animation alternates direction each cycle (forward, then backward).
     *
     * @deprecated Use the bare {@code alternate} constant.
     */
    @Deprecated
    public static final CSSValue directionAlternate = () -> "alternate";

    /**
     * Animation alternates direction, starting in reverse.
     *
     * @deprecated Use the bare {@code alternateReverse} constant.
     */
    @Deprecated
    public static final CSSValue directionAlternateReverse = () -> "alternate-reverse";

    // ==================== Fill Mode ====================

    /**
     * Animation does not apply styles before or after execution.
     *
     * @deprecated Use the bare {@code none} constant.
     */
    @Deprecated
    public static final CSSValue fillModeNone = () -> "none";

    /**
     * Animation retains styles from the last keyframe after completion.
     *
     * @deprecated Use the bare {@code forwards} constant.
     */
    @Deprecated
    public static final CSSValue fillModeForwards = () -> "forwards";

    /**
     * Animation applies styles from the first keyframe during delay period.
     *
     * @deprecated Use the bare {@code backwards} constant.
     */
    @Deprecated
    public static final CSSValue fillModeBackwards = () -> "backwards";

    /**
     * Animation applies both forwards and backwards fill modes.
     *
     * @deprecated Use the bare {@code both} constant.
     */
    @Deprecated
    public static final CSSValue fillModeBoth = () -> "both";

    // ==================== Play State ====================

    /**
     * Animation is currently running.
     *
     * @deprecated Use the bare {@code running} constant.
     */
    @Deprecated
    public static final CSSValue playStateRunning = () -> "running";

    /**
     * Animation is currently paused.
     *
     * @deprecated Use the bare {@code paused} constant.
     */
    @Deprecated
    public static final CSSValue playStatePaused = () -> "paused";

    /** {@code animation-play-state: running} — the animation is playing. */
    public static final CSSValue running = () -> "running";

    /** {@code animation-play-state: paused} — the animation is frozen. */
    public static final CSSValue paused = () -> "paused";

    // ==================== Timeline (Scroll-Driven Animations) ====================

    /**
     * Creates a scroll() timeline for scroll-driven animations.
     * Binds animation progress to scroll position.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(scrollTimeline())
     * // Animation progresses as user scrolls
     * </pre>
     *
     * @return CSSValue for scroll() timeline
     */
    public static CSSValue scrollTimeline() {
        return () -> "scroll()";
    }

    /**
     * Creates a scroll() timeline with axis specification.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(scrollTimeline("block"))  // Vertical scroll
     * style().animationTimeline(scrollTimeline("inline")) // Horizontal scroll
     * style().animationTimeline(scrollTimeline("y"))      // Y-axis
     * style().animationTimeline(scrollTimeline("x"))      // X-axis
     * </pre>
     *
     * @param axis scroll axis: "block", "inline", "x", "y"
     * @return CSSValue for scroll() timeline
     */
    public static CSSValue scrollTimeline(String axis) {
        return () -> "scroll(" + axis + ")";
    }

    /**
     * Creates a scroll() timeline with scroller and axis.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(scrollTimeline("nearest", "block"))
     * style().animationTimeline(scrollTimeline("root", "inline"))
     * </pre>
     *
     * @param scroller scroller element: "nearest", "root", "self"
     * @param axis scroll axis: "block", "inline", "x", "y"
     * @return CSSValue for scroll() timeline
     */
    public static CSSValue scrollTimeline(String scroller, String axis) {
        return () -> "scroll(" + scroller + " " + axis + ")";
    }

    /**
     * Creates a view() timeline for view-based animations.
     * Binds animation progress to element's visibility in viewport.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(viewTimeline())
     * // Animation progresses as element enters/exits viewport
     * </pre>
     *
     * @return CSSValue for view() timeline
     */
    public static CSSValue viewTimeline() {
        return () -> "view()";
    }

    /**
     * Creates a view() timeline with axis specification.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationTimeline(viewTimeline("block"))  // Vertical view
     * style().animationTimeline(viewTimeline("inline")) // Horizontal view
     * </pre>
     *
     * @param axis view axis: "block", "inline", "x", "y"
     * @return CSSValue for view() timeline
     */
    public static CSSValue viewTimeline(String axis) {
        return () -> "view(" + axis + ")";
    }

    /**
     * Creates a named timeline reference.
     *
     * @param name the timeline name
     * @return CSSValue for timeline reference
     */
    public static CSSValue timeline(String name) {
        return () -> name;
    }

    // ==================== Animation Range ====================

    /**
     * Creates an animation range for scroll-driven animations.
     * Defines when the animation starts and ends relative to scroll position.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationRange(animRange("entry", "exit"))
     * style().animationRange(animRange("cover 0%", "cover 100%"))
     * style().animationRange(animRange("contain 25%", "contain 75%"))
     * </pre>
     *
     * @param start start position (e.g., "entry", "cover 0%", "100px")
     * @param end end position (e.g., "exit", "cover 100%", "500px")
     * @return CSSValue for animation-range
     */
    public static CSSValue animRange(String start, String end) {
        return () -> start + " " + end;
    }

    /**
     * Common animation range: entry (element entering viewport).
     */
    public static final CSSValue rangeEntry = () -> "entry";

    /**
     * Common animation range: exit (element leaving viewport).
     */
    public static final CSSValue rangeExit = () -> "exit";

    /**
     * Common animation range: cover (element covering viewport).
     */
    public static final CSSValue rangeCover = () -> "cover";

    /**
     * Common animation range: contain (element contained in viewport).
     */
    public static final CSSValue rangeContain = () -> "contain";

    // ==================== Transition Behavior ====================

    /**
     * Allow discrete transitions for properties that don't normally animate smoothly.
     * Enables transitions for properties like display, content-visibility.
     *
     * <p>Example:</p>
     * <pre>
     * style().transitionProperty(propDisplay)
     *        .transitionDuration(s(0.3))
     *        .transitionBehavior(allowDiscrete)
     * </pre>
     */
    public static final CSSValue allowDiscrete = () -> "allow-discrete";

    // ==================== Animation Composition ====================

    /**
     * Creates multiple animations to run simultaneously.
     * Combines animation names with commas.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationName(composeAnimations("fadeIn", "slideUp", "pulse"))
     * // Runs all three animations together
     * </pre>
     *
     * @param names animation names to compose
     * @return CSSValue for multiple animations
     */
    public static CSSValue composeAnimations(String... names) {
        return () -> String.join(", ", names);
    }

    // ==================== Pre-built Animation Builders ====================

    /**
     * Animation builder for chaining animation properties.
     * Provides a fluent API for building complex animation definitions.
     */
    public static class AnimationBuilder implements CSSValue {
        private final String name;
        private jweb.CSSValue duration;
        private jweb.CSSValue timingFunction;
        private jweb.CSSValue delay;
        private jweb.CSSValue iterationCount;
        private jweb.CSSValue direction;
        private jweb.CSSValue fillMode;
        private jweb.CSSValue playState;

        AnimationBuilder(String name, jweb.CSSValue duration) {
            this.name = name;
            this.duration = duration;
        }

        /**
         * Sets the timing function for this animation.
         *
         * @param timing timing function (ease, linear, etc.)
         * @return this builder for chaining
         */
        public AnimationBuilder timing(jweb.CSSValue timing) {
            this.timingFunction = timing;
            return this;
        }

        /**
         * Sets the delay before animation starts.
         *
         * @param delay delay value (use s() or ms())
         * @return this builder for chaining
         */
        public AnimationBuilder delay(jweb.CSSValue delay) {
            this.delay = delay;
            return this;
        }

        /**
         * Sets how many times the animation runs.
         *
         * @param count iteration count or iterationInfinite
         * @return this builder for chaining
         */
        public AnimationBuilder iterationCount(jweb.CSSValue count) {
            this.iterationCount = count;
            return this;
        }

        /**
         * Sets the animation direction.
         *
         * @param direction direction value (directionNormal, directionAlternate, etc.)
         * @return this builder for chaining
         */
        public AnimationBuilder direction(jweb.CSSValue direction) {
            this.direction = direction;
            return this;
        }

        /**
         * Sets the fill mode.
         *
         * @param fillMode fill mode value (fillModeForwards, fillModeBoth, etc.)
         * @return this builder for chaining
         */
        public AnimationBuilder fillMode(jweb.CSSValue fillMode) {
            this.fillMode = fillMode;
            return this;
        }

        /**
         * Sets the play state.
         *
         * @param state play state (playStateRunning, playStatePaused)
         * @return this builder for chaining
         */
        public AnimationBuilder playState(jweb.CSSValue state) {
            this.playState = state;
            return this;
        }

        // There is deliberately no timeline() here: animation-timeline is not part
        // of the `animation` shorthand, so a builder value could never be emitted.
        // Set it as its own property instead: style().animationTimeline(scrollTimeline()).

        @Override
        public String css() {
            StringBuilder sb = new StringBuilder(name);
            if (duration != null) sb.append(" ").append(duration.css());
            if (timingFunction != null) sb.append(" ").append(timingFunction.css());
            if (delay != null) sb.append(" ").append(delay.css());
            if (iterationCount != null) sb.append(" ").append(iterationCount.css());
            if (direction != null) sb.append(" ").append(direction.css());
            if (fillMode != null) sb.append(" ").append(fillMode.css());
            if (playState != null) sb.append(" ").append(playState.css());
            return sb.toString();
        }
    }

    // ==================== Fade Animations ====================

    /**
     * Creates a fade-in animation (opacity 0 to 1).
     *
     * <p>Example:</p>
     * <pre>
     * style().animation(fadeIn(s(1)))
     * style().animation(fadeIn(ms(500)).timing(timingEaseOut))
     * </pre>
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder fadeIn(jweb.CSSValue duration) {
        return new AnimationBuilder("fadeIn", duration);
    }

    /**
     * Creates a fade-out animation (opacity 1 to 0).
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder fadeOut(jweb.CSSValue duration) {
        return new AnimationBuilder("fadeOut", duration);
    }





    // ==================== Slide Animations ====================

    /**
     * Creates a slide-in animation from left.
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder slideInLeft(jweb.CSSValue duration) {
        return new AnimationBuilder("slideInLeft", duration);
    }

    /**
     * Creates a slide-in animation from right.
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder slideInRight(jweb.CSSValue duration) {
        return new AnimationBuilder("slideInRight", duration);
    }







    // ==================== Scale Animations ====================

    /**
     * Creates a zoom-in animation (scale 0 to 1).
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder zoomIn(jweb.CSSValue duration) {
        return new AnimationBuilder("zoomIn", duration);
    }

    /**
     * Creates a zoom-out animation (scale 1 to 0).
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder zoomOut(jweb.CSSValue duration) {
        return new AnimationBuilder("zoomOut", duration);
    }



    /**
     * Creates a pulse animation (subtle scale up and down).
     * Typically used with iterationInfinite.
     *
     * <p>Example:</p>
     * <pre>
     * style().animation(pulse(s(1.5)).iterationCount(iterationInfinite))
     * </pre>
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder pulse(jweb.CSSValue duration) {
        return new AnimationBuilder("pulse", duration);
    }


    /**
     * Creates a bounce animation (vertical bouncing).
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder bounce(jweb.CSSValue duration) {
        return new AnimationBuilder("bounce", duration);
    }



    // ==================== Rotate Animations ====================

    /**
     * Creates a 360-degree rotation animation.
     * Commonly used with iterationInfinite for loading spinners.
     *
     * <p>Example:</p>
     * <pre>
     * style().animation(rotate360(s(2)).iterationCount(iterationInfinite).timing(timingLinear))
     * </pre>
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     *
     * @deprecated Use {@link #spin(jweb.CSSValue)} — this builder emits the animation name {@code spin} (the name {@code Keyframes.spin()} defines), not {@code rotate360}.
     */
    @Deprecated
    public static AnimationBuilder rotate360(jweb.CSSValue duration) {
        return new AnimationBuilder("spin", duration);
    }

    /**
     * A 360° rotation — backed by {@code Keyframes.spin()}.
     *
     * <p>Example:</p>
     * <pre>
     * stylesheet().keyframes(Keyframes.spin());
     * style().animation(spin(s(2)).iterationCount(infinite).timing(linear))
     * </pre>
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder spin(jweb.CSSValue duration) {
        return new AnimationBuilder("spin", duration);
    }



    // ==================== Flip Animations ====================





    // ==================== Shake/Wobble Animations ====================

    /**
     * Creates a horizontal shake animation.
     *
     * @param duration animation duration
     * @return AnimationBuilder for chaining
     */
    public static AnimationBuilder shake(jweb.CSSValue duration) {
        return new AnimationBuilder("shake", duration);
    }






    // ==================== Attention Seekers ====================




    // ==================== Utility Animations ====================

    /**
     * Creates a staggered animation delay for multiple elements.
     * Use with CSS variables or JavaScript to apply different delays.
     *
     * <p>Example:</p>
     * <pre>
     * // In Java/CSS:
     * style().animationDelay(staggerDelay(0, 100))  // Element 0: 0ms
     * style().animationDelay(staggerDelay(1, 100))  // Element 1: 100ms
     * style().animationDelay(staggerDelay(2, 100))  // Element 2: 200ms
     * </pre>
     *
     * @param index element index in sequence
     * @param delayMs milliseconds between each element
     * @return CSSValue for staggered delay
     *
     * @deprecated Use {@code ms(index * delayMs)} — that is all this does.
     */
    @Deprecated
    public static CSSValue staggerDelay(int index, int delayMs) {
        return ms(index * delayMs);
    }

    /**
     * Creates an animation sequence by combining multiple animation names.
     * Animations run one after another.
     *
     * <p>Example:</p>
     * <pre>
     * style().animationName(sequenceAnimations("fadeIn", "slideUp", "pulse"))
     * </pre>
     *
     * @param names animation names in sequence
     * @return CSSValue for animation sequence
     *
     * @deprecated Use {@link #composeAnimations(String...)} — byte-identical output.
     */
    @Deprecated
    public static CSSValue sequenceAnimations(String... names) {
        return () -> String.join(", ", names);
    }
}
