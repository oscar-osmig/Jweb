package com.osmig.Jweb.app.docs;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class DocSidebar implements Template {
    private final String active;

    public DocSidebar(String active) {
        this.active = active;
    }

    @Override
    public Element render() {
        // Layout props (width, padding, border, overflow) live in DocsPage's
        // .docs-sidebar rules so the phone breakpoint can reshape them.
        return aside(class_("docs-sidebar"), style()
                .backgroundColor(hex("#fafafa")),
            div(class_("docs-sidebar-inner"),
                navSection("Basics",
                    link("intro", "Introduction"),
                    link("setup", "Getting Started"),
                    link("elements", "Elements")),
                navSection("Core",
                    link("styling", "Styling"),
                    link("conditionals", "Conditionals"),
                    link("components", "Components"),
                    link("javascript", "JavaScript")),
                navSection("Features",
                    link("routing", "Routing"),
                    link("fragments", "Fragments"),
                    link("streaming", "Streaming SSR"),
                    link("state", "State"),
                    link("forms", "Forms"),
                    link("layouts", "Layouts")),
                navSection("Advanced",
                    link("api", "REST API"),
                    link("ai", "AI"),
                    link("performance", "Performance & SEO"),
                    link("security", "Security"),
                    link("ui", "UI Components"),
                    link("data", "Database"),
                    link("devtools", "DevTools")),
                navSection("More",
                    link("examples", "Examples")),
                aiDocsLink()
            )
        );
    }

    /**
     * Link to the plain-text documentation dump that AI assistants ground on.
     *
     * <p>It deliberately does NOT carry {@code .docs-nav-link}: DocsNavScript
     * delegates clicks on that class to
     * {@code e.preventDefault(); loadSection(t.dataset.section)}, which would
     * swallow the navigation and call {@code loadSection(undefined)}.
     *
     * <p>No {@code target="_blank"}: the endpoint replies with
     * Content-Disposition attachment, so the click starts a download without
     * navigating. Opening a tab as well would just leave an empty one behind.
     */
    private Element aiDocsLink() {
        return div(class_("docs-nav-section"),
            h2(class_("docs-nav-title"), style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)), text("For AI")),
            a(attrs().href("/docs/tell")
                .class_("docs-tell-link")
                .title("Downloads every guide and reference topic as one markdown "
                       + "file, for an AI assistant to use as a source")
                .style()
                    .display(block)
                    .padding(SP_2, SP_3).borderRadius(ROUNDED)
                    .border(px(1), dashed, BORDER)
                    .fontSize(TEXT_SM).color(TEXT_LIGHT)
                    .textDecoration(none).transition(all, s(0.15), ease)
                .done(),
                span(style().display(block), text("Download all docs (.md)")),
                span(style()
                        .display(block).marginTop(px(2))
                        .fontFamily("ui-monospace, SFMono-Regular, monospace")
                        .fontSize(px(11)).opacity(0.75),
                    text("/docs/tell"))));
    }

    private Element navSection(String title, Element... links) {
        return div(class_("docs-nav-section"),
            h2(class_("docs-nav-title"), style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)), text(title)),
            nav(class_("docs-nav-links"), fragment(links)));
    }

    private Element link(String id, String label) {
        boolean isActive = id.equals(active);
        return a(attrs().href("/docs?section=" + id)
            .data("section", id)
            .class_(isActive ? "docs-nav-link active" : "docs-nav-link")
            .style()
                .padding(SP_2, SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
                .color(isActive ? PRIMARY : TEXT_LIGHT).fontWeight(isActive ? 600 : 400)
                .backgroundColor(isActive ? hex("#eef2ff") : transparent)
                .textDecoration(none).transition(all, s(0.15), ease)
            .done(), text(label));
    }
}
