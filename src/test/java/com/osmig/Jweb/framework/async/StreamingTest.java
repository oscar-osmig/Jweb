package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.server.JWebController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static com.osmig.Jweb.framework.elements.El.*;
import static org.junit.jupiter.api.Assertions.*;

class StreamingTest {

    @AfterEach
    void cleanup() {
        StreamingContext.close();
    }

    @Test
    void suspenseDefersInsideStreamingContext() {
        StreamingContext ctx = StreamingContext.open();

        String html = Suspense.of((java.util.concurrent.Callable<String>) () -> "slow data")
            .loading(() -> span(text("Loading...")))
            .render(data -> p(text(data)))
            .toHtml();

        // The rendered HTML is the placeholder, not the content
        assertTrue(html.contains("id=\"jw-s-1\""), html);
        assertTrue(html.contains("Loading..."));
        assertFalse(html.contains("slow data"));

        // The future resolves to the real content HTML
        assertEquals(1, ctx.pendings().size());
        String resolved = ctx.pendings().get(0).html().join();
        assertTrue(resolved.contains("slow data"));
        assertTrue(resolved.contains("<p>"));
    }

    @Test
    void suspenseErrorsRenderErrorElementInStream() {
        StreamingContext ctx = StreamingContext.open();

        Suspense.of((java.util.concurrent.Callable<String>) () -> {
                throw new IllegalStateException("db down");
            })
            .loading(() -> span(text("...")))
            .error(t -> p(text("Failed: " + t.getMessage())))
            .render(data -> p(text(data)))
            .toHtml();

        String resolved = ctx.pendings().get(0).html().join();
        assertTrue(resolved.contains("Failed: db down"), resolved);
    }

    @Test
    void outsideStreamingContextSuspenseBlocksAsBefore() {
        String html = Suspense.of((java.util.concurrent.Callable<String>) () -> "direct data")
            .render(data -> p(text(data)))
            .toHtml();
        assertTrue(html.contains("direct data"));
        assertFalse(html.contains("jw-s-"));
    }

    @Test
    void streamChunkCarriesLateStatesWhenGiven() {
        String chunk = JWebController.streamChunk("jw-s-9", "<p>x</p>",
            "[{\"id\":\"state_9\",\"value\":\"a<b\"}]");
        assertTrue(chunk.contains("JWeb.lateStates("), chunk);
        assertTrue(chunk.contains("window.JWeb&&JWeb.lateStates"),
            "must not throw when the runtime is disabled: " + chunk);
        assertTrue(chunk.contains("a\\u003Cb"),
            "'<' must be escaped so state values can't break out of the script: " + chunk);
        assertTrue(chunk.contains("replaceWith"), "swap must still happen: " + chunk);

        assertFalse(JWebController.streamChunk("jw-s-9", "<p>x</p>").contains("lateStates"),
            "no late-state call when there is nothing to carry");
    }

    @Test
    void lateStatesJsonSendsEachStateExactlyOnce() {
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        try {
            var counter = jweb.State.useState(7);
            var sent = new java.util.HashSet<String>();

            String json = JWebController.lateStatesJson(context, sent);
            assertNotNull(json);
            assertTrue(json.contains("\"" + counter.getId() + "\""), json);
            assertTrue(json.contains("7"), json);

            assertNull(JWebController.lateStatesJson(context, sent),
                "already-sent states must never repeat");
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }
    }

    @Test
    void useStateInsideAStreamedBlockRidesItsChunk() {
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        StreamingContext ctx = StreamingContext.open();
        try {
            // the shell's hydration snapshot happens before any block resolves
            var sent = new java.util.HashSet<String>();
            var lateId = new java.util.concurrent.atomic.AtomicReference<String>();

            Suspense.of((java.util.concurrent.Callable<String>) () -> "data")
                .loading(() -> span(text("...")))
                .render(data -> {
                    var clicks = jweb.State.useState(41);
                    lateId.set(clicks.getId());
                    return p(text("n=" + clicks.get()));
                })
                .toHtml();
            ctx.pendings().get(0).html().join();

            String json = JWebController.lateStatesJson(context, sent);
            assertNotNull(json, "the block's state must be pending for its chunk");
            assertTrue(json.contains("\"" + lateId.get() + "\""), json);
            assertTrue(json.contains("41"), json);
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }
    }

    @Test
    void runtimeStagesLateStatesUntilInit() {
        String runtime = com.osmig.Jweb.framework.js.JWebRuntime.getScript();
        assertTrue(runtime.contains("lateStates:function"), "merge entry point missing");
        assertTrue(runtime.contains("lateQueue"), "pre-init staging missing");
    }

    @Test
    void serverEventHandlersDelegateInsteadOfInliningUnderCsp() {
        // attrs().onClick renders a data attribute — inline on<type>= handlers
        // can never execute under Middlewares.recommended()'s nonce CSP
        String html = div(jweb.El.attrs().onClick(e -> { }), text("x")).toHtml();
        assertTrue(html.contains("data-jweb-onclick=\"h_"), html);
        assertFalse(html.contains("onclick=\"JWeb.call"), "inline handler attribute must be gone: " + html);

        String runtime = com.osmig.Jweb.framework.js.JWebRuntime.getScript();
        assertTrue(runtime.contains("initServerEvents"), "delegation wiring missing");
        assertTrue(runtime.contains("data-jweb-on"), "delegation selector missing");
        assertTrue(runtime.contains("mouseenter"), "non-propagating events need emulation");
    }

    @Test
    void streamChunkSwapsPlaceholder() {
        String chunk = JWebController.streamChunk("jw-s-7", "<p>late content</p>");
        assertTrue(chunk.contains("<template id=\"jw-s-7-c\"><p>late content</p></template>"));
        assertTrue(chunk.contains("getElementById('jw-s-7')"));
        assertTrue(chunk.contains("replaceWith"));
    }

    @Test
    void contextRegistersInOrder() {
        StreamingContext ctx = StreamingContext.open();
        String a = ctx.register(CompletableFuture.completedFuture("<a>"));
        String b = ctx.register(CompletableFuture.completedFuture("<b>"));
        assertEquals("jw-s-1", a);
        assertEquals("jw-s-2", b);
        assertEquals(2, ctx.pendings().size());
    }
}
