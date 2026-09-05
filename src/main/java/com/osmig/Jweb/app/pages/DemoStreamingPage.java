package com.osmig.Jweb.app.pages;

import jweb.Suspense;
import jweb.Element;
import jweb.state.State;

import static jweb.El.*;
import static jweb.Css.*;
import static jweb.Js.*;
import static jweb.State.useState;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Streaming SSR demo: slow data blocks — one of them reactive — stream in after the shell. */
public final class DemoStreamingPage {

    private DemoStreamingPage() {}

    public static Element content() {
        return div(style().maxWidth(px(700)).margin(zero, auto)
                .padding(clamp(rem(2), vw(6), rem(3)), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT), "Streaming SSR"),
            p(style().marginTop(SP_2).color(TEXT_LIGHT),
                "This shell arrived instantly. The blocks below streamed in "
                    + "when their data was ready — no JavaScript written."),
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
                "Client-only action (Actions DSL, CSP-safe)"),
            button(id("toggle-details"), onClick(toggle("actions-panel")), buttonStyle(),
                "Toggle details"),
            div(id("actions-panel"),
                style().marginTop(SP_2).padding(SP_3).borderRadius(ROUNDED)
                    .backgroundColor(hex("#fefce8")).color(hex("#854d0e")),
                "Toggled entirely in the browser — under the page's nonce CSP."),
            // Raw set("onclick", js): the serializer rewrites even this
            // hand-written form to delegation at render time
            button(id("raw-inline"),
                attrs().set("onclick", "document.getElementById('raw-note').textContent="
                    + "'The raw handler ran — rewritten to delegation at render time.'"),
                buttonStyle().marginLeft(SP_2),
                "Raw set(\"onclick\") handler"),
            p(id("raw-note"), "Not clicked yet.")
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
                "Fragment swap carrying its own action"),
            button(id("load-fragment"), swap("/demo/streaming/fragment", "#frag-slot"),
                buttonStyle(), "Load fragment"),
            div(id("frag-slot"), style().marginTop(SP_2))
        );
    }

    /** The fragment served at /demo/streaming/fragment. */
    public static Element fragment() {
        return div(
            style().padding(SP_3).borderRadius(ROUNDED)
                   .backgroundColor(hex("#fdf2f8")).color(hex("#9d174d")),
            p("This fragment was fetched and swapped in."),
            button(id("frag-action"), onClick(hide("frag-note")), buttonStyle(),
                "Hide the note"),
            p(id("frag-note"), "Its button works because the "
                + "definitions script rode along and ran on swap.")
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
                "Reactive state, streamed in (800ms)"),
            Suspense.of(() -> {
                Thread.sleep(800);
                return "ready";
            })
            .loading(() -> p(style().color(TEXT_LIGHT), "Loading..."))
            .render(data -> {
                State<Integer> clicks = useState(0);
                return div(
                    style().padding(SP_3).borderRadius(ROUNDED)
                           .backgroundColor(hex("#eef2ff")).color(hex("#3730a3")),
                    p("Clicks: ", span(bind(clicks), clicks.get())),
                    button(onClick(e -> clicks.update(n -> n + 1)), buttonStyle(), "Click me"),
                    // Actions-DSL handler born on the block's render thread:
                    // its definition rides the chunk's script, late
                    button(id("late-action"), onClick(toggle("late-note")),
                        buttonStyle().marginLeft(SP_2), "Toggle note (client-only)"),
                    p(id("late-note"), "A streamed-in Actions-DSL handler toggled this.")
                );
            })
        );
    }

    private static Element block(String label, long delayMs) {
        return div(style().marginTop(SP_6),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT), label),
            Suspense.of(() -> {
                Thread.sleep(delayMs);
                return "Data loaded after " + delayMs + "ms";
            })
            .loading(() -> p(style().color(TEXT_LIGHT), "Loading..."))
            .render(data -> p(
                style().padding(SP_3).borderRadius(ROUNDED)
                       .backgroundColor(hex("#f0fdf4")).color(hex("#15803d")),
                data))
        );
    }

    private static jweb.Style<?> buttonStyle() {
        return style().marginTop(SP_2).padding(SP_1, SP_3).borderRadius(ROUNDED).cursor(pointer);
    }
}
