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
