package com.osmig.Jweb.framework.core;

import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Error boundary for gracefully handling rendering errors in components.
 *
 * <p>Error boundaries catch exceptions during rendering and display a fallback UI
 * instead of crashing the entire page. This is similar to React's Error Boundaries
 * but implemented server-side.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Wrap a risky component with a fallback
 * ErrorBoundary.of(() -> riskyComponent.render())
 *     .fallback(error -> div(class_("error"),
 *         h3("Something went wrong"),
 *         p(error.getMessage())
 *     ))
 *     .render()
 * }</pre>
 *
 * <h2>Static Helper</h2>
 * <pre>{@code
 * // Quick inline usage
 * ErrorBoundary.wrap(
 *     () -> dangerousComponent.render(),
 *     error -> p("Failed: " + error.getMessage())
 * )
 * }</pre>
 *
 * <h2>With Logging</h2>
 * <pre>{@code
 * ErrorBoundary.of(() -> component.render())
 *     .fallback(error -> errorMessage(error))
 *     .onError(error -> logger.error("Render failed", error))
 *     .render()
 * }</pre>
 *
 * <h2>Nested Boundaries</h2>
 * <pre>{@code
 * // Each section has its own boundary
 * div(
 *     ErrorBoundary.wrap(() -> header.render(), e -> fallbackHeader()),
 *     ErrorBoundary.wrap(() -> content.render(), e -> fallbackContent()),
 *     ErrorBoundary.wrap(() -> footer.render(), e -> fallbackFooter())
 * )
 * }</pre>
 *
 * @see Element
 * @see Renderable
 */
public class ErrorBoundary implements Element {

    private final Supplier<Element> content;
    private Function<Throwable, Element> fallback;
    private java.util.function.Consumer<Throwable> errorHandler;

    private ErrorBoundary(Supplier<Element> content) {
        this.content = content;
        this.fallback = error -> () -> new VFragment(List.of()); // Empty by default
    }

    /**
     * Creates a new ErrorBoundary wrapping the given content.
     *
     * @param content supplier that produces the element to render
     * @return a new ErrorBoundary builder
     */
    public static ErrorBoundary of(Supplier<Element> content) {
        return new ErrorBoundary(content);
    }

    /**
     * Creates an ErrorBoundary wrapping a Template.
     *
     * @param template the template to wrap
     * @return a new ErrorBoundary builder
     */
    public static ErrorBoundary of(com.osmig.Jweb.framework.template.Template template) {
        return new ErrorBoundary(template::render);
    }

    /**
     * Quick static helper to wrap content with a fallback.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * ErrorBoundary.wrap(
     *     () -> riskyComponent.render(),
     *     error -> p("Error: " + error.getMessage())
     * )
     * }</pre>
     *
     * @param content the content to render
     * @param fallback the fallback if an error occurs
     * @return the rendered element or fallback
     */
    public static Element wrap(Supplier<Element> content, Function<Throwable, Element> fallback) {
        return new ErrorBoundary(content).fallback(fallback);
    }

    /**
     * Quick static helper with a simple fallback element.
     *
     * @param content the content to render
     * @param fallback the static fallback element
     * @return the rendered element or fallback
     */
    public static Element wrap(Supplier<Element> content, Element fallback) {
        return new ErrorBoundary(content).fallback(e -> fallback);
    }

    /**
     * Sets the fallback to display when an error occurs.
     *
     * @param fallback function that receives the error and returns a fallback element
     * @return this builder for chaining
     */
    public ErrorBoundary fallback(Function<Throwable, Element> fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * Sets a static fallback element (ignores the error).
     *
     * @param fallback the static fallback element
     * @return this builder for chaining
     */
    public ErrorBoundary fallback(Element fallback) {
        this.fallback = e -> fallback;
        return this;
    }

    /**
     * Sets an error handler for logging or monitoring.
     * Called when an error occurs, before rendering the fallback.
     *
     * @param handler consumer that receives the error
     * @return this builder for chaining
     */
    public ErrorBoundary onError(java.util.function.Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Renders the content, catching any errors and displaying the fallback.
     *
     * @return the content element or fallback if an error occurred
     */
    public Element render() {
        try {
            return content.get();
        } catch (Throwable t) {
            if (errorHandler != null) {
                try {
                    errorHandler.accept(t);
                } catch (Throwable ignored) {
                    // Don't let error handler crash the fallback
                }
            }
            return fallback.apply(t);
        }
    }

    @Override
    public VNode toVNode() {
        return render().toVNode();
    }

    /**
     * Creates a boundary that catches errors and returns an empty fragment.
     * Useful when you want errors to silently fail without any UI.
     *
     * @param content the content to render
     * @return an element that renders content or nothing on error
     */
    public static Element silent(Supplier<Element> content) {
        return new ErrorBoundary(content);
    }

    /**
     * Creates a boundary with a simple text fallback.
     *
     * @param content the content to render
     * @param message the fallback message
     * @return an element that renders content or the message on error
     */
    public static Element withMessage(Supplier<Element> content, String message) {
        return new ErrorBoundary(content)
            .fallback(e -> () -> new com.osmig.Jweb.framework.vdom.VText(message));
    }
}
