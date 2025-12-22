package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
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
        return aside(attrs().style()
                .width(px(220)).padding(SP_6)
                .borderRight(px(1), solid, hex("#e2e8f0"))
                .backgroundColor(hex("#fafafa")).minHeight(vh(80))
            .done(),
            div(attrs().style().position(sticky).top(SP_6).done(),
                navSection("Basics",
                    link("intro", "Introduction"),
                    link("setup", "Getting Started"),
                    link("elements", "Elements")),
                navSection("Core",
                    link("styling", "Styling"),
                    link("conditionals", "Conditionals"),
                    link("components", "Components")),
                navSection("Features",
                    link("routing", "Routing"),
                    link("state", "State"),
                    link("forms", "Forms"),
                    link("layouts", "Layouts")),
                navSection("More",
                    link("examples", "Examples"))
            )
        );
    }

    private Element navSection(String title, Element... links) {
        return div(attrs().style().marginBottom(SP_6).done(),
            h4(attrs().style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)).done(), text(title)),
            nav(attrs().style().display(flex).flexDirection(column).gap(SP_1).done(),
                fragment(links)));
    }

    private Element link(String id, String label) {
        boolean isActive = id.equals(active);
        return a(attrs().href("/docs?section=" + id).style()
            .padding(SP_2, SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
            .color(isActive ? PRIMARY : TEXT_LIGHT).fontWeight(isActive ? 600 : 400)
            .backgroundColor(isActive ? hex("#eef2ff") : transparent)
            .textDecoration(none).transition(all, s(0.15), ease)
        .done(), text(label));
    }
}
