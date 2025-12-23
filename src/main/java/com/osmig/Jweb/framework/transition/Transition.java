package com.osmig.Jweb.framework.transition;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Transition for declarative CSS transitions and animations.
 *
 * <p>Transition provides a way to add CSS transitions and animations to elements
 * in a declarative manner, similar to Vue's Transition component.</p>
 *
 * <h2>Basic Show/Hide Transition</h2>
 * <pre>{@code
 * Transition.when(isVisible)
 *     .enter("fade-in")
 *     .leave("fade-out")
 *     .render(() -> modal())
 * }</pre>
 *
 * <h2>With Duration</h2>
 * <pre>{@code
 * Transition.when(isOpen)
 *     .enter("slide-in", 300)
 *     .leave("slide-out", 200)
 *     .render(() -> dropdown())
 * }</pre>
 *
 * <h2>CSS Classes Mode</h2>
 * <pre>{@code
 * // Adds classes for CSS to handle
 * Transition.when(isActive)
 *     .enterClass("entering")
 *     .enterActiveClass("enter-active")
 *     .leaveClass("leaving")
 *     .leaveActiveClass("leave-active")
 *     .render(() -> content)
 * }</pre>
 *
 * <h2>Using with CSS</h2>
 * <pre>{@code
 * // In your CSS
 * .fade-enter { opacity: 0; }
 * .fade-enter-active { transition: opacity 0.3s; }
 * .fade-leave-active { transition: opacity 0.3s; opacity: 0; }
 *
 * // In Java
 * Transition.fade(isVisible, () -> content)
 * }</pre>
 *
 * <h2>Inline Styles</h2>
 * <pre>{@code
 * div(attrs()
 *     .transition()
 *         .property("opacity", "transform")
 *         .duration("300ms")
 *         .easing("ease-in-out")
 *     .done(),
 *     content
 * )
 * }</pre>
 *
 * @see Element
 */
public final class Transition implements Element {

    private final boolean show;
    private Supplier<Element> content;
    private String enterClass;
    private String enterActiveClass;
    private String leaveClass;
    private String leaveActiveClass;
    private int enterDuration = 300;
    private int leaveDuration = 300;

    private Transition(boolean show) {
        this.show = show;
    }

    /**
     * Creates a transition based on a visibility condition.
     *
     * @param show whether the element should be shown
     * @return a new Transition builder
     */
    public static Transition when(boolean show) {
        return new Transition(show);
    }

    /**
     * Sets the enter animation class.
     *
     * @param className the CSS class to apply on enter
     * @return this builder
     */
    public Transition enter(String className) {
        this.enterClass = className;
        return this;
    }

    /**
     * Sets the enter animation class with duration.
     *
     * @param className the CSS class
     * @param durationMs duration in milliseconds
     * @return this builder
     */
    public Transition enter(String className, int durationMs) {
        this.enterClass = className;
        this.enterDuration = durationMs;
        return this;
    }

    /**
     * Sets the leave animation class.
     *
     * @param className the CSS class to apply on leave
     * @return this builder
     */
    public Transition leave(String className) {
        this.leaveClass = className;
        return this;
    }

    /**
     * Sets the leave animation class with duration.
     *
     * @param className the CSS class
     * @param durationMs duration in milliseconds
     * @return this builder
     */
    public Transition leave(String className, int durationMs) {
        this.leaveClass = className;
        this.leaveDuration = durationMs;
        return this;
    }

    /**
     * Sets the enter class (initial state).
     *
     * @param className the class for entering state
     * @return this builder
     */
    public Transition enterClass(String className) {
        this.enterClass = className;
        return this;
    }

    /**
     * Sets the enter-active class (during transition).
     *
     * @param className the class during enter transition
     * @return this builder
     */
    public Transition enterActiveClass(String className) {
        this.enterActiveClass = className;
        return this;
    }

    /**
     * Sets the leave class (initial state of leaving).
     *
     * @param className the class for leaving state
     * @return this builder
     */
    public Transition leaveClass(String className) {
        this.leaveClass = className;
        return this;
    }

    /**
     * Sets the leave-active class (during leave transition).
     *
     * @param className the class during leave transition
     * @return this builder
     */
    public Transition leaveActiveClass(String className) {
        this.leaveActiveClass = className;
        return this;
    }

    /**
     * Sets the content to transition.
     *
     * @param content the element to show/hide
     * @return the final element
     */
    public Element render(Supplier<Element> content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the content to transition (eager).
     *
     * @param content the element to show/hide
     * @return the final element
     */
    public Element render(Element content) {
        this.content = () -> content;
        return this;
    }

    @Override
    public VNode toVNode() {
        if (!show) {
            return new VFragment(List.of());
        }

        Element el = content.get();
        VNode node = el.toVNode();

        // If we have transition classes, wrap or modify the element
        if (enterClass != null || enterActiveClass != null) {
            if (node instanceof VElement ve) {
                Map<String, String> newAttrs = new LinkedHashMap<>(ve.getAttributes());

                // Build class string
                List<String> classes = new ArrayList<>();
                String existing = newAttrs.get("class");
                if (existing != null && !existing.isBlank()) {
                    classes.add(existing);
                }
                if (enterClass != null) {
                    classes.add(enterClass);
                }
                if (enterActiveClass != null) {
                    classes.add(enterActiveClass);
                }

                if (!classes.isEmpty()) {
                    newAttrs.put("class", String.join(" ", classes));
                }

                // Add data attributes for client-side handling
                newAttrs.put("data-transition", "true");
                if (enterDuration > 0) {
                    newAttrs.put("data-enter-duration", String.valueOf(enterDuration));
                }

                return VElement.of(ve.getTag(), newAttrs, ve.getChildren());
            }
        }

        return node;
    }

    // ==================== Preset Transitions ====================

    /**
     * Creates a fade transition.
     *
     * @param show whether to show
     * @param content the content supplier
     * @return a fade transition element
     */
    public static Element fade(boolean show, Supplier<Element> content) {
        return Transition.when(show)
            .enter("jweb-fade-enter")
            .enterActiveClass("jweb-fade-enter-active")
            .leave("jweb-fade-leave")
            .leaveActiveClass("jweb-fade-leave-active")
            .render(content);
    }

    /**
     * Creates a slide-down transition.
     *
     * @param show whether to show
     * @param content the content supplier
     * @return a slide transition element
     */
    public static Element slideDown(boolean show, Supplier<Element> content) {
        return Transition.when(show)
            .enter("jweb-slide-enter")
            .enterActiveClass("jweb-slide-enter-active")
            .leave("jweb-slide-leave")
            .leaveActiveClass("jweb-slide-leave-active")
            .render(content);
    }

    /**
     * Creates a scale transition.
     *
     * @param show whether to show
     * @param content the content supplier
     * @return a scale transition element
     */
    public static Element scale(boolean show, Supplier<Element> content) {
        return Transition.when(show)
            .enter("jweb-scale-enter")
            .enterActiveClass("jweb-scale-enter-active")
            .leave("jweb-scale-leave")
            .leaveActiveClass("jweb-scale-leave-active")
            .render(content);
    }

    // ==================== CSS Generator ====================

    /**
     * Generates the CSS for built-in transition classes.
     * Include this in your page's style tag.
     *
     * @return CSS string for transitions
     */
    public static String css() {
        return """
            /* Fade */
            .jweb-fade-enter { opacity: 0; }
            .jweb-fade-enter-active { transition: opacity 0.3s ease; }
            .jweb-fade-leave-active { transition: opacity 0.3s ease; opacity: 0; }

            /* Slide */
            .jweb-slide-enter { transform: translateY(-10px); opacity: 0; }
            .jweb-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
            .jweb-slide-leave-active { transition: transform 0.3s ease, opacity 0.3s ease; transform: translateY(-10px); opacity: 0; }

            /* Scale */
            .jweb-scale-enter { transform: scale(0.9); opacity: 0; }
            .jweb-scale-enter-active { transition: transform 0.2s ease, opacity 0.2s ease; }
            .jweb-scale-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; transform: scale(0.9); opacity: 0; }
            """;
    }
}
