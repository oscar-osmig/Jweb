package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Navigation bar partial.
 */
public class Nav implements Template {

    public Nav() {}

    public static Nav create() {
        return new Nav();
    }

    @Override
    public Element render() {
        return nav(attrs().class_("navbar").style(
                style()
                    .background(linearGradient("135deg", PRIMARY, SECONDARY))
                    .padding(SPACE_SM, SPACE_XL)
                    .display(flex)
                    .alignItems(center)
                    .gap(SPACE_MD)
            ),
            navLink("/", "Home"),
            navLink("/about", "About"),
            navLink("/contact", "Contact")
        );
    }

    private Element navLink(String href, String label) {
        return a(attrs().href(href).style(
                style()
                    .color(white)
                    .textDecoration(none)
                    .fontSize(FONT_SM)
                    .fontWeight(500)
                    .padding(SPACE_XS, SPACE_SM)
                    .borderRadius(RADIUS_SM)
                    .transition("background", TRANSITION_FAST, ease)
            ), text(label));
    }
}
