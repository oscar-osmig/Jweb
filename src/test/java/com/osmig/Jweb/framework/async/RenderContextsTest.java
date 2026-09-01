package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.security.CspNonce;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.osmig.Jweb.framework.elements.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Render-scoped ThreadLocals must travel into async render blocks and never
 * leak between renders on executor threads.
 */
class RenderContextsTest {

    @AfterEach
    void cleanup() {
        StreamingContext.close();
        CspNonce.clear();
    }

    @Test
    void carryRestoresARegisteredContextOnTheExecutingThreadAndClearsIt() throws Exception {
        ThreadLocal<String> context = new ThreadLocal<>();
        RenderContexts.register(new RenderContexts.Propagator() {
            @Override public Object capture() { return context.get(); }
            @Override public void restore(Object snapshot) {
                if (snapshot != null) context.set((String) snapshot);
            }
            @Override public void clear() { context.remove(); }
        });

        context.set("carried-value");
        // capture happens HERE, on the requesting thread — the returned
        // supplier restores the value wherever it later runs
        var carried = RenderContexts.carry(() -> context.get());
        AtomicReference<String> insideBlock = new AtomicReference<>();
        AtomicReference<String> afterBlock = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            insideBlock.set(carried.get());
            afterBlock.set(context.get());
        });
        thread.start();
        thread.join(5000);

        assertEquals("carried-value", insideBlock.get(), "captured value must reach the block");
        assertNull(afterBlock.get(), "the executing thread must be clean afterwards");
        context.remove();
    }

    @Test
    void cspNonceStillReachesStreamedSuspenseBlocks() {
        CspNonce.set("nonce-abc123");
        StreamingContext ctx = StreamingContext.open();

        Suspense.of((java.util.concurrent.Callable<String>) () -> "data")
            .loading(() -> span(text("...")))
            .render(data -> p(text("nonce=" + CspNonce.current())))
            .toHtml();

        String resolved = ctx.pendings().get(0).html().join();
        assertTrue(resolved.contains("nonce=nonce-abc123"),
            "the request's CSP nonce must be visible inside the streamed block: " + resolved);
    }

    @Test
    void handlersRegisteredInStreamedBlocksAreContextScoped() {
        // attrs().onClick(...) delegates to EventRegistry.register — inside a
        // streamed block that used to land in the global registry because the
        // state context didn't survive the thread hop. Now it must be scoped
        // to the page's context, exactly as on the request thread.
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        StreamingContext ctx = StreamingContext.open();
        AtomicReference<String> handlerId = new AtomicReference<>();
        try {
            Suspense.of((java.util.concurrent.Callable<String>) () -> "data")
                .loading(() -> span(text("...")))
                .render(data -> {
                    var handler = com.osmig.Jweb.framework.events.EventRegistry
                        .register("click", e -> {});
                    handlerId.set(handler.getId());
                    return p(text("ok"));
                })
                .toHtml();
            ctx.pendings().get(0).html().join();

            String id = handlerId.get();
            assertNotNull(id, "block must have registered its handler");
            assertNull(com.osmig.Jweb.framework.events.EventRegistry.get(id),
                "handler must NOT fall back to the global registry");
            assertNotNull(com.osmig.Jweb.framework.events.EventRegistry
                    .get(context.getSessionId(), id),
                "handler must be scoped to the page's context");
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }
    }

    @Test
    void loaderRunsUnderCarriedContextsInBlockingMode() {
        CspNonce.set("nonce-blocking");
        String html = Suspense.of((java.util.concurrent.Callable<String>) () -> "seen=" + CspNonce.current())
            .render(data -> p(text(data)))
            .toHtml();
        assertTrue(html.contains("seen=nonce-blocking"),
            "data loaders must also see request contexts: " + html);
    }
}
