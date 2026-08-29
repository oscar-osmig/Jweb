package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class PerformanceSection {
    private PerformanceSection() {}

    public static Element render() {
        return section(
            docTitle("Performance & SEO"),
            para("Most of this is on by default — pages ship one small cached runtime "
                 + "instead of a framework bundle, and the server does the rendering."),

            docSubtitle("SEO Metadata"),
            para("One builder emits the title, description, canonical link, Open Graph, "
                 + "and Twitter card tags — all consistent from a single declaration."),
            codeBlock("""
                    head(
                        metaCharset(), metaViewport(),
                        Seo.of("JWeb — Java Web Framework",
                               "Build complete web apps entirely in Java")
                            .url("https://jweb.dev/")
                            .image("https://jweb.dev/og.png")
                            .siteName("JWeb")
                            .render()
                    )"""),
            docTip("Per-page control is also available through the Template hooks: "
                   + "pageTitle(), metaDescription() and extraHead()."),

            docSubtitle("Image Optimization"),
            para("Images are resized on the fly and cached immutably — no build step, "
                 + "no dependencies (ImageIO)."),
            codeBlock("""
                    img("/jweb/img?src=/static/hero.jpg&w=800")"""),
            docList(
                "src is restricted to classpath static/ and public/ resources",
                "w resizes to that width, keeping the aspect ratio (max 3840)",
                "Results are cached in memory and served with a 1-year immutable header"),

            docSubtitle("Asset Caching"),
            para("The client runtime and prefetch script are served as external files with "
                 + "a content-hash version, so browsers cache them across every navigation "
                 + "and only the page HTML travels on each request."),
            codeBlock("""
                    <script src="/jweb/runtime.js?v=67f7c91e"></script>
                    Cache-Control: max-age=31536000, public, immutable"""),

            docSubtitle("Streaming & Prefetch"),
            docList(
                "Streamed.of(() -> page) flushes the shell immediately and streams "
                    + "Suspense blocks as their data resolves (see Streaming SSR)",
                "Link prefetching warms pages on hover, so navigation feels instant",
                "A background warmup pre-renders registered pages at startup, so the "
                    + "first visitor doesn't pay lazy-initialization cost",
                "gzip compression is enabled for text responses"),

            docSubtitle("Secure Baseline"),
            para("One call adds the production essentials — security headers, request IDs, "
                 + "and compression negotiation — to every route:"),
            codeBlock("""
                    app.use(Middlewares.recommended());"""),
            docTip("Headers are applied to Element, String and ResponseEntity responses "
                   + "alike, including page routes and 404s.")
        );
    }
}
