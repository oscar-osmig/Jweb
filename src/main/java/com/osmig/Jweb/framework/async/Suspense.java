package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VText;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Suspense for handling async data loading with declarative loading states.
 *
 * <p>Suspense provides a clean way to handle async operations during server-side
 * rendering, showing loading states while data is being fetched and error states
 * if something goes wrong.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * Suspense.of(() -> userService.getUsers())
 *     .loading(() -> spinner("Loading users..."))
 *     .error(e -> alert("Failed to load: " + e.getMessage()))
 *     .render(users ->
 *         ul(each(users, u -> li(u.getName())))
 *     )
 * }</pre>
 *
 * <h2>Quick Helper</h2>
 * <pre>{@code
 * suspend(
 *     () -> api.getUsers(),
 *     loading(spinner()),
 *     users -> userList(users)
 * )
 * }</pre>
 *
 * <h2>With Timeout</h2>
 * <pre>{@code
 * Suspense.of(() -> slowService.getData())
 *     .timeout(5, TimeUnit.SECONDS)
 *     .loading(() -> p("Loading..."))
 *     .error(e -> p("Timed out or failed"))
 *     .render(data -> displayData(data))
 * }</pre>
 *
 * <h2>With Async/CompletableFuture</h2>
 * <pre>{@code
 * Suspense.async(asyncService.fetchDataAsync())
 *     .loading(() -> spinner())
 *     .render(data -> showData(data))
 * }</pre>
 *
 * @param <T> the type of data being loaded
 * @see Element
 */
public class Suspense<T> implements Element {

    private final Callable<T> dataLoader;
    private Supplier<Element> loadingElement;
    private Function<Throwable, Element> errorElement;
    private Function<T, Element> contentRenderer;
    private long timeoutMs = 30000; // 30 second default

    private Suspense(Callable<T> dataLoader) {
        this.dataLoader = dataLoader;
        this.loadingElement = () -> () -> new VText("Loading...");
        this.errorElement = e -> () -> new VText("Error: " + e.getMessage());
    }

    /**
     * Creates a new Suspense with a data loader.
     *
     * @param loader the data loading function
     * @param <T> the data type
     * @return a new Suspense builder
     */
    public static <T> Suspense<T> of(Callable<T> loader) {
        return new Suspense<>(loader);
    }

    /**
     * Creates a Suspense from a Supplier (no checked exceptions).
     *
     * @param loader the data loading supplier
     * @param <T> the data type
     * @return a new Suspense builder
     */
    public static <T> Suspense<T> of(Supplier<T> loader) {
        return new Suspense<>(loader::get);
    }

    /**
     * Creates a Suspense from a CompletableFuture.
     *
     * @param future the async data source
     * @param <T> the data type
     * @return a new Suspense builder
     */
    public static <T> Suspense<T> async(CompletableFuture<T> future) {
        return new Suspense<>(() -> future.get(30, TimeUnit.SECONDS));
    }

    /**
     * Creates a Suspense from a CompletableFuture with custom timeout.
     *
     * @param future the async data source
     * @param timeout the timeout duration
     * @param unit the timeout unit
     * @param <T> the data type
     * @return a new Suspense builder
     */
    public static <T> Suspense<T> async(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        return new Suspense<>(() -> future.get(timeout, unit));
    }

    /**
     * Sets the loading state element.
     *
     * @param loading supplier for the loading element
     * @return this builder
     */
    public Suspense<T> loading(Supplier<Element> loading) {
        this.loadingElement = loading;
        return this;
    }

    /**
     * Sets the loading state element (eager).
     *
     * @param loading the loading element
     * @return this builder
     */
    public Suspense<T> loading(Element loading) {
        this.loadingElement = () -> loading;
        return this;
    }

    /**
     * Sets the error state element.
     *
     * @param error function that receives the error and returns an element
     * @return this builder
     */
    public Suspense<T> error(Function<Throwable, Element> error) {
        this.errorElement = error;
        return this;
    }

    /**
     * Sets a static error element (ignores the error details).
     *
     * @param error the error element
     * @return this builder
     */
    public Suspense<T> error(Element error) {
        this.errorElement = e -> error;
        return this;
    }

    /**
     * Sets the timeout for data loading.
     *
     * @param duration the timeout duration
     * @param unit the timeout unit
     * @return this builder
     */
    public Suspense<T> timeout(long duration, TimeUnit unit) {
        this.timeoutMs = unit.toMillis(duration);
        return this;
    }

    /**
     * Sets the content renderer and completes the Suspense.
     *
     * @param renderer function that receives the loaded data and returns an element
     * @return the rendered element
     */
    public Element render(Function<T, Element> renderer) {
        this.contentRenderer = renderer;
        return this;
    }

    @Override
    public VNode toVNode() {
        try {
            // For server-side rendering, we block and wait for data
            T data = dataLoader.call();

            if (contentRenderer != null) {
                return contentRenderer.apply(data).toVNode();
            }
            return new VFragment(List.of());
        } catch (Throwable t) {
            return errorElement.apply(t).toVNode();
        }
    }

    // ==================== Static Helpers for Elements.java ====================

    /**
     * Quick helper for suspense with loading and content.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * suspend(
     *     () -> api.getUsers(),
     *     spinner(),
     *     users -> userList(users)
     * )
     * }</pre>
     *
     * @param loader the data loader
     * @param loading the loading element
     * @param content the content renderer
     * @param <T> the data type
     * @return the rendered element
     */
    public static <T> Element suspend(
            Supplier<T> loader,
            Element loading,
            Function<T, Element> content) {
        return Suspense.of(loader)
            .loading(loading)
            .render(content);
    }

    /**
     * Quick helper with loading, error, and content.
     *
     * @param loader the data loader
     * @param loading the loading element
     * @param error the error handler
     * @param content the content renderer
     * @param <T> the data type
     * @return the rendered element
     */
    public static <T> Element suspend(
            Supplier<T> loader,
            Element loading,
            Function<Throwable, Element> error,
            Function<T, Element> content) {
        return Suspense.of(loader)
            .loading(loading)
            .error(error)
            .render(content);
    }

    /**
     * Suspense that shows nothing while loading.
     *
     * @param loader the data loader
     * @param content the content renderer
     * @param <T> the data type
     * @return the rendered element
     */
    public static <T> Element suspendSilent(
            Supplier<T> loader,
            Function<T, Element> content) {
        return Suspense.of(loader)
            .loading(() -> new VFragment(List.of()))
            .error(e -> () -> new VFragment(List.of()))
            .render(content);
    }
}
