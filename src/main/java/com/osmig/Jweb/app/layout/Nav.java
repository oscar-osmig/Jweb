package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;

/**
 * Navigation bar.
 */
public class Nav implements Template {

    @Override
    public Element render() {
        return nav(attrs().style()
                .prop("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .padding(rem(0.75), rem(2))
                .display(flex).alignItems(center).justifyContent(spaceBetween)
            .done(),
            a(attrs().href("/").style()
                .color(white).fontSize(rem(1.25)).fontWeight(700)
                .textDecoration(none)
            .done(), text("JWeb")),
            div(attrs().style().display(flex).gap(rem(1.5)).done(),
                link("/docs", "Documentation"),
                link("/about", "About"),
                link("/contact", "Contact")
            )
        );
    }

    private Element link(String href, String label) {
        return a(attrs().href(href).style()
            .color(rgba(255, 255, 255, 0.9)).fontSize(rem(0.9))
            .textDecoration(none).fontWeight(500)
        .done(), text(label));
    }
}
