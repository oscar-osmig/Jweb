package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.async.Suspense;
import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Streaming SSR demo: two slow data blocks stream in after the shell. */
public final class DemoStreamingPage {

    private DemoStreamingPage() {}

    public static Element content() {
        return div(attrs().style().maxWidth(px(700)).margin(zero, auto).padding(rem(3), SP_8).done(),
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Streaming SSR")),
            p(attrs().style().marginTop(SP_2).color(TEXT_LIGHT).done(),
                text("This shell arrived instantly. The two blocks below streamed in "
                    + "when their data was ready — no JavaScript written.")),
            block("Fast query (400ms)", 400),
            block("Slow query (1200ms)", 1200)
        );
    }

    private static Element block(String label, long delayMs) {
        return div(attrs().style().marginTop(SP_6).done(),
            h3(attrs().style().fontSize(TEXT_LG).fontWeight(600).color(TEXT).done(), text(label)),
            Suspense.of((java.util.concurrent.Callable<String>) () -> {
                Thread.sleep(delayMs);
                return "Data loaded after " + delayMs + "ms";
            })
            .loading(() -> p(attrs().style().color(TEXT_LIGHT).done(), text("Loading...")))
            .render(data -> p(attrs().style()
                    .padding(SP_3).borderRadius(ROUNDED)
                    .backgroundColor(hex("#f0fdf4")).color(hex("#16a34a"))
                .done(),
                text(data)))
        );
    }
}
