package com.osmig.Jweb.app.pages;

import jweb.Suspense;
import jweb.Element;
import jweb.state.State;

import static jweb.El.*;
import static jweb.Css.*;
import static jweb.State.useState;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Streaming SSR demo: slow data blocks — one of them reactive — stream in after the shell. */
public final class DemoStreamingPage {

    private DemoStreamingPage() {}

    public static Element content() {
        return div(style().maxWidth(px(700)).margin(zero, auto)
                .padding(clamp(rem(2), vw(6), rem(3)), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT),
                text("Streaming SSR")),
            p(style().marginTop(SP_2).color(TEXT_LIGHT),
                text("This shell arrived instantly. The blocks below streamed in "
                    + "when their data was ready — no JavaScript written.")),
            block("Fast query (400ms)", 400),
            block("Slow query (1200ms)", 1200),
            statefulBlock()
        );
    }

    /**
     * Reactive state born inside a streamed block: the counter's state and
     * click handler are created on the block's render thread after the shell
     * (and its hydration data) already flushed — the chunk carries the state,
     * and the handler is scoped to the page's context.
     */
    private static Element statefulBlock() {
        return div(style().marginTop(SP_6),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                text("Reactive state, streamed in (800ms)")),
            Suspense.of((java.util.concurrent.Callable<String>) () -> {
                Thread.sleep(800);
                return "ready";
            })
            .loading(() -> p(style().color(TEXT_LIGHT), text("Loading...")))
            .render(data -> {
                State<Integer> clicks = useState(0);
                return div(
                    style().padding(SP_3).borderRadius(ROUNDED)
                           .backgroundColor(hex("#eef2ff")).color(hex("#3730a3")),
                    p(text("Clicks: "),
                        span(attrs().data("state", clicks.getId()),
                            text(String.valueOf(clicks.get())))),
                    button(attrs().onClick(e -> clicks.set(clicks.get() + 1))
                            .style().marginTop(SP_2).padding(SP_1, SP_3)
                                .borderRadius(ROUNDED).cursor(pointer).done(),
                        text("Click me"))
                );
            })
        );
    }

    private static Element block(String label, long delayMs) {
        return div(style().marginTop(SP_6),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT), text(label)),
            Suspense.of((java.util.concurrent.Callable<String>) () -> {
                Thread.sleep(delayMs);
                return "Data loaded after " + delayMs + "ms";
            })
            .loading(() -> p(style().color(TEXT_LIGHT), text("Loading...")))
            .render(data -> p(
                style().padding(SP_3).borderRadius(ROUNDED)
                       .backgroundColor(hex("#f0fdf4")).color(hex("#15803d")),
                text(data)))
        );
    }
}
