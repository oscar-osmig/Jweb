package com.osmig.Jweb.framework.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Per-render registry of deferred Suspense blocks during a streaming render.
 * While active (opened by the controller for {@link Streamed} pages), every
 * {@code Suspense} renders an instantly-flushed placeholder and registers a
 * future that resolves to the block's final HTML.
 */
public final class StreamingContext {

    private static final ThreadLocal<StreamingContext> CURRENT = new ThreadLocal<>();

    /** A deferred block: placeholder id + future resolving to its HTML. */
    public record Pending(String placeholderId, CompletableFuture<String> html) {}

    private final List<Pending> pendings = new ArrayList<>();
    private int counter = 0;

    private StreamingContext() {}

    /** Opens a streaming context on this thread (returns it for collection). */
    public static StreamingContext open() {
        StreamingContext context = new StreamingContext();
        CURRENT.set(context);
        return context;
    }

    /** Closes the streaming context on this thread. */
    public static void close() {
        CURRENT.remove();
    }

    /** The active context, or null when not rendering a streamed page. */
    public static StreamingContext active() {
        return CURRENT.get();
    }

    /** Registers a deferred block; returns its placeholder element id. */
    public String register(CompletableFuture<String> htmlFuture) {
        String id = "jw-s-" + (++counter);
        pendings.add(new Pending(id, htmlFuture));
        return id;
    }

    /** The deferred blocks registered during the render. */
    public List<Pending> pendings() {
        return pendings;
    }
}
