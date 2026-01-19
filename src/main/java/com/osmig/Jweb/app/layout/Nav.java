package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
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
                .position(sticky).top(zero).zIndex(1000)
                .background(linearGradient("90deg",
                    hex("#6366f1"),
                    hex("#8b5cf6"),
                    hex("#a855f7"),
                    hex("#ec4899"),
                    hex("#8b5cf6"),
                    hex("#6366f1")))
                .backgroundSize(() -> "300% 100%")
                .prop("animation", "gradientShift 8s ease infinite")
                .padding(rem(0.75), rem(2))
                .display(flex).alignItems(center).justifyContent(spaceBetween)
            .done(),
            a(attrs().href("/").style()
                .color(white).fontSize(rem(1.25)).fontWeight(700)
                .textDecoration(none)
            .done(), text("JWeb")),
            div(attrs().style().display(flex).gap(rem(1.5)).alignItems(center).done(),
                link("/docs", "Documentation"),
                link("/about", "About"),
                link("/contact", "Contact"),
                tryItButton()
            )
        );
    }

    private Element link(String href, String label) {
        return a(attrs().href(href).style()
            .color(rgba(255, 255, 255, 0.9)).fontSize(rem(0.9))
            .textDecoration(none).fontWeight(500)
        .done(), text(label));
    }

    private Element tryItButton() {
        return a(attrs().href("/try-it").style()
            .backgroundColor(white)
            .color(hex("#6366f1"))
            .padding(rem(0.5), rem(1))
            .borderRadius(px(6))
            .fontSize(rem(0.9))
            .fontWeight(600)
            .textDecoration(none)
            .transition(() -> "all 0.2s")
            .boxShadow("0 2px 4px rgba(0,0,0,0.1)")
        .done(), text("Try It"));
    }
}
