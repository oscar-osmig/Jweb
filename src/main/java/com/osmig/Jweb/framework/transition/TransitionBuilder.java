package com.osmig.Jweb.framework.transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for CSS transitions on elements.
 *
 * <p>TransitionBuilder provides a type-safe way to define CSS transitions
 * inline on elements.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * div(attrs()
 *     .class_("box")
 *     .transition()
 *         .property("opacity", "transform")
 *         .duration("300ms")
 *         .easing("ease-in-out")
 *     .done(),
 *     content
 * )
 * }</pre>
 *
 * <h2>Multiple Properties</h2>
 * <pre>{@code
 * button(attrs()
 *     .transition()
 *         .property("background-color").duration("200ms")
 *         .property("transform").duration("150ms").easing("ease-out")
 *     .done(),
 *     "Hover me"
 * )
 * }</pre>
 *
 * <h2>Preset Transitions</h2>
 * <pre>{@code
 * div(attrs().transition().fade().done(), content)
 * div(attrs().transition().slide().done(), content)
 * div(attrs().transition().all("300ms").done(), content)
 * }</pre>
 */
public class TransitionBuilder {

    /**
     * Interface for the parent that accepts transition results.
     */
    public interface TransitionReceiver {
        String get(String name);
        TransitionReceiver set(String name, String value);
    }

    private final TransitionReceiver parent;
    private final List<String> transitions = new ArrayList<>();
    private String currentProperty;
    private String currentDuration = "300ms";
    private String currentEasing = "ease";
    private String currentDelay = "0s";

    public TransitionBuilder(TransitionReceiver parent) {
        this.parent = parent;
    }

    /**
     * Sets the property to transition.
     *
     * @param properties one or more CSS properties
     * @return this builder
     */
    public TransitionBuilder property(String... properties) {
        // Flush current if exists
        flushCurrent();

        for (String prop : properties) {
            currentProperty = prop;
            flushCurrent();
        }
        currentProperty = null;
        return this;
    }

    /**
     * Sets the transition duration.
     *
     * @param duration CSS duration (e.g., "300ms", "0.5s")
     * @return this builder
     */
    public TransitionBuilder duration(String duration) {
        this.currentDuration = duration;
        return this;
    }

    /**
     * Sets the transition duration in milliseconds.
     *
     * @param ms duration in milliseconds
     * @return this builder
     */
    public TransitionBuilder duration(int ms) {
        this.currentDuration = ms + "ms";
        return this;
    }

    /**
     * Sets the easing function.
     *
     * @param easing CSS easing (e.g., "ease", "linear", "ease-in-out")
     * @return this builder
     */
    public TransitionBuilder easing(String easing) {
        this.currentEasing = easing;
        return this;
    }

    /**
     * Sets the transition delay.
     *
     * @param delay CSS delay (e.g., "100ms", "0.2s")
     * @return this builder
     */
    public TransitionBuilder delay(String delay) {
        this.currentDelay = delay;
        return this;
    }

    /**
     * Sets the delay in milliseconds.
     *
     * @param ms delay in milliseconds
     * @return this builder
     */
    public TransitionBuilder delay(int ms) {
        this.currentDelay = ms + "ms";
        return this;
    }

    // ==================== Easing Presets ====================

    /** Linear easing. */
    public TransitionBuilder linear() {
        return easing("linear");
    }

    /** Ease easing (default). */
    public TransitionBuilder ease() {
        return easing("ease");
    }

    /** Ease-in easing. */
    public TransitionBuilder easeIn() {
        return easing("ease-in");
    }

    /** Ease-out easing. */
    public TransitionBuilder easeOut() {
        return easing("ease-out");
    }

    /** Ease-in-out easing. */
    public TransitionBuilder easeInOut() {
        return easing("ease-in-out");
    }

    /**
     * Cubic bezier easing.
     *
     * @param x1 first control point x
     * @param y1 first control point y
     * @param x2 second control point x
     * @param y2 second control point y
     * @return this builder
     */
    public TransitionBuilder cubicBezier(double x1, double y1, double x2, double y2) {
        return easing("cubic-bezier(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
    }

    // ==================== Preset Transitions ====================

    /**
     * Applies a fade transition (opacity).
     *
     * @return this builder
     */
    public TransitionBuilder fade() {
        return property("opacity").duration("300ms").easeInOut();
    }

    /**
     * Applies a slide transition (transform).
     *
     * @return this builder
     */
    public TransitionBuilder slide() {
        return property("transform").duration("300ms").easeOut();
    }

    /**
     * Applies a transform transition.
     *
     * @return this builder
     */
    public TransitionBuilder transform() {
        return property("transform");
    }

    /**
     * Transitions all properties.
     *
     * @param duration the duration
     * @return this builder
     */
    public TransitionBuilder all(String duration) {
        transitions.add("all " + duration + " " + currentEasing);
        return this;
    }

    /**
     * Transitions all properties with default duration.
     *
     * @return this builder
     */
    public TransitionBuilder all() {
        return all("300ms");
    }

    /**
     * No transition (useful for disabling).
     *
     * @return this builder
     */
    public TransitionBuilder none() {
        transitions.clear();
        transitions.add("none");
        return this;
    }

    // ==================== Complete ====================

    private void flushCurrent() {
        if (currentProperty != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(currentProperty);
            sb.append(" ").append(currentDuration);
            sb.append(" ").append(currentEasing);
            if (!"0s".equals(currentDelay) && !"0ms".equals(currentDelay)) {
                sb.append(" ").append(currentDelay);
            }
            transitions.add(sb.toString());
        }
        // Reset for next property
        currentDuration = "300ms";
        currentEasing = "ease";
        currentDelay = "0s";
    }

    /**
     * Completes the transition builder and returns to the parent.
     *
     * @param <T> the parent type
     * @return the parent with the transition style applied
     */
    @SuppressWarnings("unchecked")
    public <T extends TransitionReceiver> T done() {
        flushCurrent();

        if (!transitions.isEmpty()) {
            String existing = parent.get("style");
            String transition = "transition:" + String.join(",", transitions) + ";";

            if (existing != null && !existing.isBlank()) {
                if (!existing.endsWith(";")) {
                    existing += ";";
                }
                parent.set("style", existing + transition);
            } else {
                parent.set("style", transition);
            }
        }

        return (T) parent;
    }

    /**
     * Gets the transition CSS value without applying to parent.
     *
     * @return the CSS transition value
     */
    public String build() {
        flushCurrent();
        return String.join(",", transitions);
    }
}
