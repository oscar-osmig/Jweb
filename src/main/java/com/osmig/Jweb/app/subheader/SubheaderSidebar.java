package com.osmig.Jweb.app.subheader;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Right sidebar showing sub-headers (h3) from the current documentation section.
 * Sub-headers are dynamically populated via JavaScript scanning the content.
 */
public class SubheaderSidebar implements Template {

    @Override
    public Element render() {
        return aside(attrs().id("subheader-sidebar").class_("subheader-sidebar").style()
                // 260, not 220: at 220 a link had 139px of text width, so the
                // longest heading ("5. pages/HomePage.java", 159px) could not fit
                // its filename token on the first line and the list number was
                // left stranded alone above it. 260 also clears the widest token
                // in the whole doc set — "when().then().otherwise()" at 153px,
                // which is an h3 and so indented 12px further than an h2.
                .width(px(260))
                .padding(SP_6)
                .borderLeft(px(1), solid, BORDER)
                .backgroundColor(hex("#fafafa"))
                .flexShrink(0)
                .position(sticky)
                .top(px(0))
                .maxHeight(vh(100))
                .overflowY(hidden)
                // visibility is class-driven (.has-headers + min-width media
                // rule in DocsPage) — no inline display, so CSS stays in charge
            .done(),
            h2(style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_4).textTransform(uppercase)
                .letterSpacing(em(0.05)),
                text("On This Page")),
            nav(attrs().id("subheader-nav"), style()
                .display(flex).flexDirection(column).gap(SP_1)
                .overflowY(auto)
                .maxHeight(calc("100vh - 50px"))
                .paddingRight(SP_2)
                .paddingBottom(rem(10)))
        );
    }
}
