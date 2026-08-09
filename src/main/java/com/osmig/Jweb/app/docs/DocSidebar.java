package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class DocSidebar implements Template {
    private final String active;

    public DocSidebar(String active) {
        this.active = active;
    }

    @Override
    public Element render() {
        return aside(class_("docs-sidebar"), style()
                .width(px(220))
                .padding(SP_6)
                .borderRight(px(1), solid, BORDER)
                .backgroundColor(hex("#fafafa"))
                .overflowY(auto)
                .flexShrink(0),
            div(
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
        return div(style().marginBottom(SP_6),
            h2(style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)), text(title)),
            nav(style().display(flex).flexDirection(column).gap(SP_1),
                fragment(links)));
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
