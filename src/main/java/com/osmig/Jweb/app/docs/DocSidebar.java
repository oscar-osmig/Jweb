package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Documentation sidebar navigation.
 */
public class DocSidebar implements Template {
    private final String active;

    public DocSidebar(String active) {
        this.active = active;
    }

    @Override
    public Element render() {
        return aside(attrs().style()
                .width(px(240)).padding(SP_6)
                .prop("border-right", "1px solid #e2e8f0")
                .backgroundColor(hex("#fafafa"))
                .minHeight(vh(80))
            .done(),
            div(attrs().style()
                    .position(sticky).top(SP_6)
                .done(),
                h3(attrs().style()
                        .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                        .marginBottom(SP_4).textTransform(uppercase)
                        .prop("letter-spacing", "0.05em")
                    .done(), text("Documentation")),
                nav(attrs().style().display(flex).flexDirection(column).gap(SP_1).done(),
                    link("intro", "Introduction"),
                    link("setup", "Getting Started"),
                    link("routing", "Routing"),
                    link("templates", "Templates"),
                    link("styling", "Styling"),
                    link("state", "State Management"),
                    link("forms", "Forms"),
                    link("api", "API Reference")
                )
            )
        );
    }

    private Element link(String id, String label) {
        boolean isActive = id.equals(active);
        return a(attrs().href("/docs?section=" + id).style()
            .padding(SP_2, SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
            .color(isActive ? PRIMARY : TEXT_LIGHT).fontWeight(isActive ? 600 : 400)
            .backgroundColor(isActive ? hex("#eef2ff") : transparent)
            .textDecoration(none)
            .prop("transition", "all 0.15s ease")
        .done(), text(label));
    }
}
