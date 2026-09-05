package com.osmig.Jweb.app.docs;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static jweb.css.Selectors.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import com.osmig.Jweb.app.subheader.SubheaderSidebar;
import com.osmig.Jweb.app.subheader.SubheaderScript;

/**
 * Documentation page with sidebar and content.
 */
public class DocsPage implements Template {
    private final String section;
    private final String version;

    public DocsPage(String section) {
        this(section, null);
    }

    public DocsPage(String section, String version) {
        this.section = section != null ? section : "intro";
        this.version = DocVersions.normalize(version);
    }

    @Override
    public Element render() {
        // Layout styles live in docsStyles() as class rules (not inline) so
        // the phone media query below can restack them — inline styles would
        // always win over @media rules.
        return div(class_("docs-layout"),
            style(docsStyles()),
            new DocSidebar(section, version).render(),
            div(class_("docs-content"), style()
                    .flex(1).minWidth(zero).minHeight(num(0))
                    .padding(SP_8, clamp(SP_4, vw(5), SP_12))
                    .overflowY(auto),
                versionBanner(),
                DocContent.get(section, version)),
            new SubheaderSidebar().render(),
            inlineScript(DocsNavScript.build()),
            inlineScript(SubheaderScript.build()),
            inlineScript(CodeCopyScript.build())
        );
    }

    /** A slim reminder while reading docs for anything but the latest release. */
    private Element versionBanner() {
        if (DocVersions.isLatest(version)) return null;
        return div(style()
                .display(flex).alignItems(center).gap(SP_2)
                .padding(SP_2, SP_3).marginBottom(SP_6)
                .borderRadius(ROUNDED)
                .backgroundColor(hex("#fffbeb"))
                .border(px(1), solid, hex("#fde68a"))
                .fontSize(TEXT_SM).color(hex("#92400e")),
            span(text("Viewing documentation for " + version + ".")),
            a(attrs().href("/docs?section=" + section)
                .style().color(hex("#92400e")).fontWeight(600).done(),
                text("Switch to " + DocVersions.latest() + " (latest)")));
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
            .add(media().minWidth(px(1100))
                .rule(".subheader-sidebar.has-headers", style().display(block)))
            // Phone: stack vertically; the section sidebar becomes a
            // horizontally scrolling chip strip above the content.
            .add(media().maxWidth(px(767))
                .rule(".docs-layout", style().flexDirection(column))
                .rule(".docs-sidebar", style()
                    .width(auto).padding(SP_3, SP_4)
                    .prop("border-right", "none")
                    .borderBottom(px(1), solid, BORDER)
                    .overflowX(auto).overflowY(hidden))
                .rule(".docs-sidebar-inner", style()
                    .display(flex).alignItems(center).gap(SP_4))
                .rule(".docs-nav-section", style().marginBottom(zero))
                .rule(".docs-nav-title", style().display(none))
                .rule(".docs-nav-links", style().flexDirection(row))
                .rule(".docs-nav-link", style().whiteSpace(nowrap)))
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
            // Code-block copy button: hidden until the block is hovered or the
            // button keyboard-focused; CodeCopyScript toggles .copied on click.
            // Hover reveal must be class rules — inline styles can't do :hover.
            .rule(".code-copy-btn", style()
                .position(absolute).top(SP_2).right(SP_2)
                .padding(px(4), px(10))
                .fontSize(rem(0.75)).lineHeight(1.4)
                .color(hex("#cbd5e1"))
                .backgroundColor(rgba(255, 255, 255, 0.08))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(px(6))
                .cursor(pointer)
                .opacity(0)
                .transitionOpacity(s(0.15)))
            .rule(".doc-code:hover .code-copy-btn, .code-copy-btn:focus-visible", style()
                .opacity(1))
            .rule(".code-copy-btn:hover", style()
                .backgroundColor(rgba(255, 255, 255, 0.18)).color(hex("#f1f5f9")))
            .rule(".code-copy-btn.copied", style()
                .color(hex("#6ee7b7")).borderColor(rgba(110, 231, 183, 0.4)))
            // Touch screens have no hover — keep the button always visible.
            .add(media().noHover()
                .rule(".code-copy-btn", style().opacity(1)))
            // Version chip on the dependency block: a details/summary dropdown
            // styled like the copy button, sitting just left of it. Always
            // visible — it carries information (the version), not just an action.
            .rule(".ver-picker", style()
                .position(absolute).top(SP_2).right(rem(4.4)).margin(zero))
            .rule(".ver-picker summary", style()
                .padding(px(4), px(10))
                .fontSize(rem(0.75)).lineHeight(1.4)
                .color(hex("#cbd5e1"))
                .backgroundColor(rgba(255, 255, 255, 0.08))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(px(6))
                .cursor(pointer)
                .prop("list-style", "none"))
            .rule(".ver-picker summary::-webkit-details-marker", style().display(none))
            .rule(".ver-picker summary:hover", style()
                .backgroundColor(rgba(255, 255, 255, 0.18)).color(hex("#f1f5f9")))
            .rule(".ver-picker[open] summary", style().color(hex("#f1f5f9")))
            .rule(".ver-picker-menu", style()
                .position(absolute).right(zero).marginTop(px(6))
                .minWidth(px(150))
                .backgroundColor(hex("#1e293b"))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(px(8)).padding(px(4))
                .boxShadow("0 8px 24px rgba(0,0,0,0.35)")
                .zIndex(20))
            .rule(".ver-picker-item", style()
                .display(block).padding(px(6), px(10)).borderRadius(px(6))
                .fontSize(rem(0.75)).color(hex("#cbd5e1"))
                .textDecoration(none))
            .rule(".ver-picker-item:hover", style()
                .backgroundColor(rgba(255, 255, 255, 0.1)).color(hex("#f1f5f9")))
            .rule(".ver-picker-item.current", style()
                .color(hex("#6ee7b7")).fontWeight(600))
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
            // The /docs/tell link is outside the section nav, so DocsNavScript's
            // delegated mouseover styling never reaches it — it needs a real
            // :hover rule, which an inline style cannot express.
            .rule(".docs-tell-link:hover", style()
                .color(PRIMARY)
                .borderColor(PRIMARY)
                .backgroundColor(hex("#eef2ff")))
            .build();
    }
}
