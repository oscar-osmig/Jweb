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
                .width(px(200)).padding(SP_6)
                .prop("border-right", "1px solid #e2e8f0")
            .done(),
            nav(attrs().style().display(flex).flexDirection(column).gap(SP_2).done(),
                link("intro", "Introduction"),
                link("setup", "Getting Started"),
                link("routing", "Routing"),
                link("templates", "Templates"),
                link("styling", "Styling"),
                link("state", "State Management"),
                link("forms", "Forms"),
                link("api", "API Reference")
            )
        );
    }

    private Element link(String id, String label) {
        boolean isActive = id.equals(active);
        return a(attrs().href("/docs?section=" + id).style()
            .padding(SP_2, SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
            .color(isActive ? PRIMARY : TEXT_LIGHT).fontWeight(isActive ? 600 : 400)
            .backgroundColor(isActive ? hex("#f1f5f9") : hex("#ffffff"))
            .textDecoration(none)
        .done(), text(label));
    }
}
