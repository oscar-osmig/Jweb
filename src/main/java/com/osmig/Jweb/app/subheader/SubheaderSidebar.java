package com.osmig.Jweb.app.subheader;

import jweb.Cls;
import jweb.Element;
import jweb.Id;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Right sidebar showing sub-headers (h3) from the current documentation section.
 * Sub-headers are dynamically populated via JavaScript scanning the content.
 */
public class SubheaderSidebar implements Template {

    /** The rail element itself — {@code #subheader-sidebar} — the {@link SubheaderScript} host. */
    public static final Id SUBHEADER_SIDEBAR_ID = id("subheader-sidebar");
    /** The rail's visibility class — {@code .subheader-sidebar} — shown by {@link com.osmig.Jweb.app.docs.DocsPage}'s media rule once headings exist. */
    public static final Cls SUBHEADER_SIDEBAR_CLS = cls("subheader-sidebar");
    /** The scrolling link list — {@code #subheader-nav} — the {@link SubheaderScript} nav target. */
    public static final Id SUBHEADER_NAV = id("subheader-nav");
    /** A generated heading link — {@code .subheader-link} — rendered by {@link SubheaderScript}'s scrollSpy, never by Java. */
    public static final Cls SUBHEADER_LINK = cls("subheader-link");
    /** Added to {@link #SUBHEADER_SIDEBAR_ID} once the scrollSpy finds headings — {@code .has-headers}. */
    public static final Cls HAS_HEADERS = cls("has-headers");

    @Override
    public Element render() {
        return aside(SUBHEADER_SIDEBAR_ID, SUBHEADER_SIDEBAR_CLS,
            style()
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
                .overflowY(hidden),
                // visibility is class-driven (.has-headers + min-width media
                // rule in DocsPage) — no inline display, so CSS stays in charge
            h2(style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_4).textTransform(uppercase)
                .letterSpacing(em(0.05)),
                "On This Page"),
            nav(SUBHEADER_NAV, stack(SP_1)
                .overflowY(auto)
                .maxHeight(vh(100).minus(px(50)))
                .paddingRight(SP_2)
                .paddingBottom(rem(10)))
        );
    }
}
