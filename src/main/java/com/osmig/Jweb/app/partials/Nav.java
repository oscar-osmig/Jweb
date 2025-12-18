package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Navigation bar partial with animated gradient background.
 */
public class Nav implements Template {

    public Nav() {}

    public static Nav create() {
        return new Nav();
    }

    @Override
    public Element render() {
        return nav(attrs().class_("navbar gradient-bg").style()
                .padding(SPACE_SM, SPACE_XL)
                .display(flex)
                .alignItems(center)
                .justifyContent(spaceBetween)
            .done(),
            // Logo / Brand
            div(attrs().style()
                    .display(flex)
                    .alignItems(center)
                    .gap(SPACE_SM)
                .done(),
                span(attrs().style()
                    .fontSize(FONT_XL)
                    .fontWeight(700)
                    .color(white)
                    .letterSpacing(px(-1))
                .done(), text("JWeb"))
            ),
            // Navigation Links
            div(attrs().style()
                    .display(flex)
                    .alignItems(center)
                    .gap(SPACE_XS)
                .done(),
                navLink("/", "Home"),
                navLink("/demo", "Demo"),
                navLink("/about", "About"),
                navLink("/contact", "Contact")
            )
        );
    }

    private Element navLink(String href, String label) {
        return a(attrs().href(href).style()
                .color(rgba(255, 255, 255, 0.9))
                .textDecoration(none)
                .fontSize(FONT_SM)
                .fontWeight(500)
                .padding(SPACE_XS, SPACE_MD)
                .borderRadius(RADIUS_SM)
                .transition("all", TRANSITION_FAST, ease)
            .done(), text(label));
    }
}
