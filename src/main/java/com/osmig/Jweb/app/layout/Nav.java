package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Navigation bar.
 */
public class Nav implements Template {

    @Override
    public Element render() {
        return nav(style()
                .position(sticky).top(zero).zIndex(1000)
                .apply(brandFlow())
                .padding(rem(0.75), GUTTER)
                .display(flex).flexWrap(wrap).rowGap(SP_2)
                .alignItems(center).justifyContent(spaceBetween),
            a(href("/"), style()
                .color(white).fontSize(rem(1.25)).fontWeight(700)
                .textDecoration(none), text("JWeb")),
            div(style()
                    .display(flex).flexWrap(wrap).alignItems(center)
                    .gap(clamp(SP_3, vw(3), rem(1.5))),
                link("/docs", "Documentation"),
                link("/sandbox", "Sandbox"),
                link("/about", "About"),
                link("/contact", "Contact")
            )
        );
    }

    private Element link(String href, String label) {
        return a(href(href), style()
            .color(rgba(255, 255, 255, 0.9)).fontSize(TEXT_SM)
            .textDecoration(none).fontWeight(500), text(label));
    }
}
