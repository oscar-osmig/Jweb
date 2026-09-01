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
            actionsBlock(),
            block("Fast query (400ms)", 400),
            block("Slow query (1200ms)", 1200),
            statefulBlock(),
            fragmentBlock()
        );
    }

    /**
     * Actions-DSL handler in the shell: no server round-trip, and no inline
     * onclick= — the JS registers with the render, ships in the page's
     * nonce-stamped definitions script, and the runtime delegates the click.
     */
    private static Element actionsBlock() {
        return div(style().marginTop(SP_6),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                text("Client-only action (Actions DSL, CSP-safe)")),
            button(attrs().id("toggle-details")
                    .onClick(jweb.Actions.toggle("actions-panel"))
                    .style().marginTop(SP_2).padding(SP_1, SP_3)
                        .borderRadius(ROUNDED).cursor(pointer).done(),
                text("Toggle details")),
            div(attrs().id("actions-panel")
                    .style().marginTop(SP_2).padding(SP_3).borderRadius(ROUNDED)
                        .backgroundColor(hex("#fefce8")).color(hex("#854d0e")).done(),
                text("Toggled entirely in the browser — under the page's nonce CSP."))
        );
    }

    /**
     * A swapped-in fragment whose button is itself an Actions-DSL handler:
     * the fragment rides its definitions script along, and the runtime
     * executes it on swap (innerHTML never runs scripts by itself).
     */
    private static Element fragmentBlock() {
        return div(style().marginTop(SP_6),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                text("Fragment swap carrying its own action")),
            button(attrs().id("load-fragment")
                    .swap("/demo/streaming/fragment", "#frag-slot")
                    .style().marginTop(SP_2).padding(SP_1, SP_3)
                        .borderRadius(ROUNDED).cursor(pointer).done(),
                text("Load fragment")),
            div(attrs().id("frag-slot").style().marginTop(SP_2).done())
        );
    }

    /** The fragment served at /demo/streaming/fragment. */
    public static Element fragment() {
        return div(
            style().padding(SP_3).borderRadius(ROUNDED)
                   .backgroundColor(hex("#fdf2f8")).color(hex("#9d174d")),
            p(text("This fragment was fetched and swapped in.")),
            button(attrs().id("frag-action")
                    .onClick(jweb.Actions.hide("frag-note"))
                    .style().marginTop(SP_2).padding(SP_1, SP_3)
                        .borderRadius(ROUNDED).cursor(pointer).done(),
                text("Hide the note")),
            p(attrs().id("frag-note"), text("Its button works because the "
                + "definitions script rode along and ran on swap."))
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
                        text("Click me")),
                    // Actions-DSL handler born on the block's render thread:
                    // its definition rides the chunk's script, late
                    button(attrs().id("late-action")
                            .onClick(jweb.Actions.toggle("late-note"))
                            .style().marginTop(SP_2).marginLeft(SP_2).padding(SP_1, SP_3)
                                .borderRadius(ROUNDED).cursor(pointer).done(),
                        text("Toggle note (client-only)")),
                    p(attrs().id("late-note"),
                        text("A streamed-in Actions-DSL handler toggled this."))
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
