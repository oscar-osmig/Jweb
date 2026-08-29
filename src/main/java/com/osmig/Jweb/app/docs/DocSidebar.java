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
                    link("examples", "Examples"))
            )
        );
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
