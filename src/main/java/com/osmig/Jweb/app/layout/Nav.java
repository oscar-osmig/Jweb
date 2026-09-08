package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Navigation bar.
 */
public class Nav implements Template {

    @Override
    public Element render() {
        return nav(cluster(SP_2)
                .justifyContent(spaceBetween)
                .position(sticky).top(zero).zIndex(1000)
                .apply(brandFlow())
                .padding(rem(0.75), GUTTER),
            a(href("/"), style()
                .color(white).fontSize(rem(1.25)).fontWeight(700)
                .textDecoration(none), "JWeb"),
            div(cluster(clamp(SP_3, vw(3), rem(1.5))),
                link("/docs", "Documentation"),
                link("/sandbox", "Sandbox"),
                link("/about", "About"),
                link("/contact", "Contact")
            )
        );
    }

    private Element link(String url, String label) {
        return a(href(url), style()
            .color(rgba(255, 255, 255, 0.9)).fontSize(TEXT_SM)
            .textDecoration(none).fontWeight(500), label);
    }
}
