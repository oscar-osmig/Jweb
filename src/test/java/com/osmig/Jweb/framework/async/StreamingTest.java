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
    void actionsInsideAStreamedBlockRideTheirChunk() {
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        StreamingContext ctx = StreamingContext.open();
        try {
            Suspense.of((java.util.concurrent.Callable<String>) () -> "data")
                .loading(() -> span(text("...")))
                .render(data -> p(
                    jweb.El.attrs().onClick(com.osmig.Jweb.framework.js.Actions.show("panel")),
                    text(data)))
                .toHtml();
            String resolved = ctx.pendings().get(0).html().join();

            // The block's element carries the delegation attribute, not inline JS
            assertTrue(resolved.contains("data-jweb-actclick=\"a"), resolved);
            assertFalse(resolved.contains("onclick="), resolved);

            // Its definition is pending for the chunk (registered via the
            // propagated context on the loader thread), and the chunk's
            // nonce'd script carries it into the page
            String late = com.osmig.Jweb.framework.js.ClientActions.drainJs(context);
            assertNotNull(late, "the block's action must be pending for its chunk");
            assertTrue(late.contains("panel"), late);
            assertTrue(late.contains("__JWEB_ACTIONS__"), late);

            String chunk = JWebController.streamChunk("jw-s-1", resolved, null, late);
            assertTrue(chunk.contains("__JWEB_ACTIONS__"), chunk);

            // Drained means delivered: the next chunk carries nothing again
            assertNull(com.osmig.Jweb.framework.js.ClientActions.drainJs(context));
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }
    }

    @Test
    void actionHandlersDelegateInsteadOfInliningUnderCsp() {
        // The client side of the Actions-DSL contract: delegation listeners
        // for data-jweb-act, definitions looked up in the global map, and a
        // nonce-stamped executor for definitions that arrive outside the
        // document parse (fragments, domUpdate payloads)
        String runtime = com.osmig.Jweb.framework.js.JWebRuntime.getScript();
        assertTrue(runtime.contains("data-jweb-act"), "action delegation selector missing");
        assertTrue(runtime.contains("runAction:function"), "action dispatcher missing");
        assertTrue(runtime.contains("__JWEB_ACTIONS__"), "definitions map lookup missing");
        assertTrue(runtime.contains("execScript:function"), "nonce-stamped executor missing");
        assertTrue(runtime.contains("pageNonce:function"), "page nonce recovery missing");
        assertTrue(runtime.contains("runFragmentScripts"), "fragment definitions execution missing");
        assertTrue(runtime.contains("actionsJs"), "domUpdate definitions delivery missing");
        assertTrue(runtime.contains("data-jweb-actmouseenter"), "non-propagating events need emulation");
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
