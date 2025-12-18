package com.osmig.Jweb.app.partials.demo;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.app.Theme.*;
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

/**
 * Reusable card wrapper for demo components.
 */
public final class DemoCard {

    private DemoCard() {}

    public static Element wrap(String title, String icon, Element... content) {
        return div(attrs().class_("card").style()
                .backgroundColor(white)
                .borderRadius(RADIUS_LG)
                .padding(SPACE_LG)
                .boxShadow(px(0), px(4), px(12), SHADOW)
            .done(),
            // Header
            div(attrs().style().display(flex).alignItems(center).gap(SPACE_SM)
                    .marginBottom(SPACE_MD).done(),
                span(attrs().style().fontSize(FONT_XL).done(), text(icon)),
                h3(attrs().style().fontSize(FONT_LG).color(TEXT).fontWeight(600).done(),
                    text(title))
            ),
            // Content
            div(content)
        );
    }
}
