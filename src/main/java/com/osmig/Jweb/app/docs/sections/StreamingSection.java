package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StreamingSection {
    private StreamingSection() {}

    public static Element render() {
        return section(
            docTitle("Streaming SSR"),
            para("Wrap a page in Streamed.of(() -> ...) and the shell flushes to the " +
                 "browser immediately. Every Suspense block renders its loading " +
                 "placeholder instantly and streams its real HTML into the page the " +
                 "moment its data resolves — blocks load in parallel and arrive in " +
                 "completion order. No JavaScript is written."),

            docSubtitle("Usage"),
            codeBlock("""
                    app.get("/dashboard", req -> Streamed.of(() ->
                        new Layout("Dashboard", div(
                            header(),                              // paints immediately
                            Suspense.of(() -> reports.slowQuery()) // streams in when ready
                                .loading(() -> p("Crunching numbers..."))
                                .render(data -> reportTable(data))
                        )).render()));"""),

            docSubtitle("How It Works"),
            docList(
                "The shell (with placeholders) is written and flushed first — TTFB ~35ms",
                "Each Suspense block runs on a virtual thread, in parallel",
                "Completed blocks stream as <template> chunks plus a tiny swap script",
                "Total time = the slowest block, not the sum"),

            warn("The page must be built inside the supplier — the element DSL evaluates " +
                 "eagerly, so a pre-built tree would already have executed its Suspense " +
                 "blocks before streaming starts."),

            docTip("See it live: /demo/streaming renders a 400ms and a 1200ms block. " +
                   "Outside a Streamed page, Suspense behaves exactly as before " +
                   "(blocking or nonBlocking)."),

            docSubtitle("When To Use It"),
            para("Streaming shines when a page mixes fast content (navigation, headers, " +
                 "cached data) with slow content (reports, external APIs, heavy queries). " +
                 "The user sees and can interact with the page immediately instead of " +
                 "staring at a blank tab for the slowest query's duration.")
        );
    }
}
