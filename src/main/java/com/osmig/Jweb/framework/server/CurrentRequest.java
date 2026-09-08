package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.async.RenderContexts;
import jweb.Request;

/**
 * The request in flight on this thread — what {@code Template.session()},
 * {@code Template.params()} and {@code Session.current()} read, so a page
 * can reach its session without a {@code Request} parameter.
 *
 * <p>Set by the controller around every dispatch (page routes, router
 * routes, 404s) and by {@code JWebTest}; cleared with the other per-request
 * thread-locals. Registered with {@link RenderContexts} so streamed and
 * suspended blocks, which render on other threads, see the same request.
 * WebSocket event handlers run outside any HTTP request and see none.</p>
 */
public final class CurrentRequest {

    private static final ThreadLocal<Request> CURRENT = new ThreadLocal<>();

    static {
        RenderContexts.register(new RenderContexts.Propagator() {
            @Override public Object capture() { return CURRENT.get(); }
            @Override public void restore(Object snapshot) {
                if (snapshot != null) CURRENT.set((Request) snapshot);
            }
            @Override public void clear() { CURRENT.remove(); }
        });
    }

    private CurrentRequest() {}

    /** Marks {@code request} as the one in flight on this thread. */
    public static void set(Request request) {
        if (request == null) CURRENT.remove(); else CURRENT.set(request);
    }

    /** The request in flight, or null outside a dispatch. */
    public static Request get() {
        return CURRENT.get();
    }

    /**
     * The request in flight, or an {@link IllegalStateException} naming the
     * caller when there is none (a render outside any request).
     */
    public static Request require(String what) {
        Request request = CURRENT.get();
        if (request == null) {
            throw new IllegalStateException(what + " needs a request in flight — it is available while "
                + "a page route or router handler renders (and inside its streamed blocks), not from "
                + "a bare toHtml() or a WebSocket event handler");
        }
        return request;
    }

    /** Forgets the request on this thread. */
    public static void clear() {
        CURRENT.remove();
    }
}
