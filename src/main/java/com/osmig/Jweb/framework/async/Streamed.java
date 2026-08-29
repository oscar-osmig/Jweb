package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.core.Element;

import java.util.function.Supplier;

/**
 * Marks a page for streaming SSR: the shell is flushed immediately with
 * loading placeholders where {@code Suspense} blocks sit, and each block's
 * HTML streams in (and replaces its placeholder) the moment its data is
 * ready — no JavaScript written, works during page load.
 *
 * <p>The page is a {@code Supplier} so the tree is built inside the
 * streaming context (the element DSL evaluates eagerly — a pre-built tree
 * would already have executed its Suspense blocks):</p>
 *
 * <pre>
 * app.get("/dashboard", req -> Streamed.of(() -> new Layout("Dashboard",
 *     div(
 *         header(),                                       // flushed instantly
 *         Suspense.of(() -> reports.slowQuery())          // streams in later
 *             .loading(() -> spinner("Crunching numbers..."))
 *             .render(data -> reportTable(data))
 *     )).render()));
 * </pre>
 */
public record Streamed(Supplier<? extends jweb.Element> page) {

    /** Wraps a lazily-built page for streaming delivery. */
    public static Streamed of(Supplier<? extends jweb.Element> page) {
        return new Streamed(page);
    }
}
