package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.Stylesheet.*;
import static com.osmig.Jweb.framework.styles.Selectors.*;
import static com.osmig.Jweb.framework.styles.MediaQuery.media;
import static com.osmig.Jweb.app.layout.Theme.*;
import com.osmig.Jweb.app.subheader.SubheaderSidebar;
import com.osmig.Jweb.app.subheader.SubheaderScript;

/**
 * Documentation page with sidebar and content.
 */
public class DocsPage implements Template {
    private final String section;

    public DocsPage(String section) {
        this.section = section != null ? section : "intro";
    }

    @Override
    public Element render() {
        // Layout styles live in docsStyles() as class rules (not inline) so
        // the phone media query below can restack them — inline styles would
        // always win over @media rules.
        return div(class_("docs-layout"),
            style(docsStyles()),
            new DocSidebar(section).render(),
            div(class_("docs-content"), style()
                    .flex(1).minWidth(zero).minHeight(num(0))
                    .padding(SP_8, clamp(SP_4, vw(5), SP_12))
                    .overflowY(auto),
                DocContent.get(section)),
            new SubheaderSidebar().render(),
            inlineScript(DocsNavScript.build()),
            inlineScript(SubheaderScript.build())
        );
    }

    private String docsStyles() {
        return stylesheet()
            // Desktop-first three-pane layout
            .rule(".docs-layout", style()
                .display(flex).height(percent(100)).minHeight(num(0)))
            .rule(".docs-sidebar", style()
                .width(px(220)).padding(SP_6)
                .borderRight(px(1), solid, BORDER)
                .overflowY(auto).flexShrink(0))
            .rule(".docs-nav-section", style().marginBottom(SP_6))
            .rule(".docs-nav-links", style()
                .display(flex).flexDirection(column).gap(SP_1))
            // The "On This Page" rail is opt-in: hidden until its script finds
            // headers (adds .has-headers) AND the screen is wide enough.
            .rule(".subheader-sidebar", style().display(none))
            .mediaQuery(media().minWidth(px(1100)),
                new Rule(".subheader-sidebar.has-headers", style().display(block)))
            // Phone: stack vertically; the section sidebar becomes a
            // horizontally scrolling chip strip above the content.
            .mediaQuery(media().maxWidth(px(767)),
                new Rule(".docs-layout", style().flexDirection(column)),
                new Rule(".docs-sidebar", style()
                    .width(auto).padding(SP_3, SP_4)
                    .prop("border-right", "none")
                    .borderBottom(px(1), solid, BORDER)
                    .overflowX(auto).overflowY(hidden)),
                new Rule(".docs-sidebar-inner", style()
                    .display(flex).alignItems(center).gap(SP_4)),
                new Rule(".docs-nav-section", style().marginBottom(zero)),
                new Rule(".docs-nav-title", style().display(none)),
                new Rule(".docs-nav-links", style().flexDirection(row)),
                new Rule(".docs-nav-link", style().whiteSpace(nowrap)))
            .rule(scrollbar(".docs-content"), style().width(px(6)))
            .rule(scrollbarTrack(".docs-content"), style().background(transparent))
            .rule(scrollbarThumb(".docs-content"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(scrollbarThumbHover(".docs-content"), style().background(rgba(0, 0, 0, 0.2)))
            .rule(scrollbar(".docs-sidebar"), style().width(px(4)).height(px(4)))
            .rule(scrollbarTrack(".docs-sidebar"), style().background(transparent))
            .rule(scrollbarThumb(".docs-sidebar"), style().background(rgba(0, 0, 0, 0.05)).borderRadius(px(2)))
            .rule(scrollbarThumbHover(".docs-sidebar"), style().background(rgba(0, 0, 0, 0.1)))
            .rule(scrollbar("#subheader-nav"), style().width(px(6)))
            .rule(scrollbarTrack("#subheader-nav"), style().background(transparent))
            .rule(scrollbarThumb("#subheader-nav"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(scrollbarThumbHover("#subheader-nav"), style().background(rgba(0, 0, 0, 0.2)))
            .rule(".docs-content h2, .docs-content h3", style().scrollMarginTop(rem(1.5)))
            .rule(".docs-nav-link.active, .subheader-link.active", style()
                .position(relative)
                .overflow(visible)
                .zIndex(1))
            .rule(".docs-nav-link.active::before, .subheader-link.active::before", style()
                .content()
                .position(absolute).inset(zero)
                .borderRadius(ROUNDED)
                .padding(px(2))
                .apply(brandFlow())
                .borderMask()
                .zIndex(-1)
                .pointerEvents(none))
            .build();
    }
}
