package com.osmig.Jweb.framework.three;

import java.util.ArrayList;
import java.util.List;

/**
 * The per-thread collection point for {@link ThreePatch}es built during a
 * server event handler. The WebSocket handler {@link #open()}s it before
 * dispatching the handler, {@link #drain()}s afterwards, and sends whatever
 * accumulated on the answering socket — patches themselves never know about
 * sessions. Outside an open window, patches have nowhere to go and are
 * refused (so a stray {@code Three.patch} during a page render can't leak
 * across pooled request threads).
 */
public final class ThreePatchQueue {

    private static final ThreadLocal<List<ThreePatch>> QUEUE = new ThreadLocal<>();

    private ThreePatchQueue() {}

    /** Opens the collection window for this thread (WebSocket handler only). */
    public static void open() {
        QUEUE.set(new ArrayList<>());
    }

    /** Queues a patch. False when no window is open — the patch is undeliverable. */
    static boolean offer(ThreePatch patch) {
        List<ThreePatch> q = QUEUE.get();
        if (q == null) return false;
        q.add(patch);
        return true;
    }

    /** Returns and clears everything queued in this window. */
    public static List<ThreePatch> drain() {
        List<ThreePatch> q = QUEUE.get();
        QUEUE.remove();
        return q == null ? List.of() : q;
    }

    /** Closes the window, dropping anything still queued. */
    public static void close() {
        QUEUE.remove();
    }
}
