package com.osmig.Jweb.app.docs;

import jweb.Action;
import jweb.Cls;
import jweb.Element;
import jweb.Js;
import jweb.Template;
import jweb.css.Selector;

import java.util.Optional;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import com.osmig.Jweb.app.subheader.SubheaderSidebar;
import com.osmig.Jweb.app.subheader.SubheaderScript;

/**
 * Documentation page with sidebar and content.
 */
public class DocsPage implements Template {
    /** The three-pane flex shell — {@code .docs-layout} — {@link CodeCopyScript}'s delegate root. */
    public static final Cls DOCS_LAYOUT = cls("docs-layout");
    /** The scrolling content pane — {@code .docs-content} — {@code SubheaderScript}'s scroll container. */
    public static final Cls DOCS_CONTENT = cls("docs-content");

    // The version picker chip lives in SetupSection (out of scope for this
    // pass), which still spells these four names as raw strings — the
    // handles below exist only so this file's own rules stop repeating them.
    private static final Cls VER_PICKER = cls("ver-picker");
    private static final Cls VER_PICKER_MENU = cls("ver-picker-menu");
    private static final Cls VER_PICKER_ITEM = cls("ver-picker-item");
    private static final Cls CURRENT = cls("current");

    // Dead CSS, kept behaviourally identical: no element currently carries
    // this class, so the rule below matches nothing, same as before.
    private static final Cls DOCS_TELL_LINK = cls("docs-tell-link");

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
    public String pageTitle() {
        return "Documentation - JWeb";
    }

    @Override
    public Element render() {
        // The layout rules live in styles() — the render collects them and
        // emits them in <head>.
        return div(DOCS_LAYOUT,
            new DocSidebar(section, version),
            div(DOCS_CONTENT, style()
                    .flex(1).minWidth(zero).minHeight(num(0))
                    .padding(SP_8, clamp(SP_4, vw(5), SP_12))
                    .overflowY(auto),
                versionBanner(),
                DocContent.get(section, version)),
            new SubheaderSidebar()
        );
    }

    @Override
    public Optional<Action> scripts() {
        return Optional.of(Js.all(DocsNavScript.build(), SubheaderScript.build(), CodeCopyScript.build()));
    }

    /** A slim reminder while reading docs for anything but the latest release. */
    private Element versionBanner() {
        if (DocVersions.isLatest(version)) return null;
        return div(row(SP_2)
                .padding(SP_2, SP_3).marginBottom(SP_6)
                .borderRadius(ROUNDED)
                .backgroundColor(hex("#fffbeb"))
                .border(px(1), solid, hex("#fde68a"))
                .fontSize(TEXT_SM).color(hex("#92400e")),
            span("Viewing documentation for " + version + "."),
            a(href("/docs?section=" + section),
                style().color(hex("#92400e")).fontWeight(600),
                "Switch to " + DocVersions.latest() + " (latest)"));
    }

    /**
     * The docs layout's own stylesheet. These have to be class rules, not
     * inline styles: the phone media query below restacks the panes, and an
     * inline style would always win over an {@code @media} rule.
     */
    @Override
    public jweb.css.Stylesheet styles() {
        return stylesheet()
            // Desktop-first three-pane layout
            .rule(DOCS_LAYOUT, style()
                .display(flex).height(percent(100)).minHeight(num(0)))
            .rule(DocSidebar.DOCS_SIDEBAR, style()
                .width(px(220)).padding(SP_6)
                .borderRight(px(1), solid, BORDER)
                .overflowY(auto).flexShrink(0))
            .rule(DocSidebar.NAV_SECTION, style().marginBottom(SP_6))
            .rule(DocSidebar.NAV_LINKS, stack(SP_1))
            // Nav and rail links are styled by class, never inline: the active
            // marking moves between links on every client-side navigation, and
            // an inline color would outrank the .active rule forever after.
            .rule(DocSidebar.NAV_LINK, style()
                .padding(SP_2, SP_3).borderRadius(ROUNDED).fontSize(TEXT_SM)
                .color(TEXT_LIGHT).fontWeight(400)
                .backgroundColor(transparent)
                .textDecoration(none).transition(all, s(0.15), ease))
            .rule(DocSidebar.NAV_LINK.hover(), style()
                .color(PRIMARY).backgroundColor(hex("#eef2ff")))
            .rule(DocSidebar.NAV_LINK.cls(DocSidebar.ACTIVE.name()), style()
                .color(PRIMARY).fontWeight(600).backgroundColor(hex("#eef2ff")))
            // The rail's links are generated by scrollSpy; data-level carries
            // the heading depth, so the h3 indent is a rule and not JS.
            // overflow-wrap keeps a heading whose single longest token is wider
            // than the rail (a path, a URL) from stranding the list number.
            .rule(SubheaderSidebar.SUBHEADER_LINK, style()
                .display(block).overflowWrap(breakWord)
                .padding(rem(0.5), rem(0.75)).borderRadius(px(6))
                .fontSize(rem(0.875)).color(TEXT_LIGHT)
                .textDecoration(none).transition(all, s(0.15), ease))
            .rule(SubheaderSidebar.SUBHEADER_LINK.attr("data-level", "3"), style()
                .paddingLeft(rem(1.5)).fontSize(rem(0.8)))
            .rule(SubheaderSidebar.SUBHEADER_LINK.hover(), style()
                .color(PRIMARY).backgroundColor(hex("#eef2ff")))
            .rule(SubheaderSidebar.SUBHEADER_LINK.cls(DocSidebar.ACTIVE.name()), style()
                .color(PRIMARY).fontWeight(600).backgroundColor(hex("#eef2ff")))
            // The "On This Page" rail is opt-in: hidden until its script finds
            // headers (adds .has-headers) AND the screen is wide enough.
            .rule(SubheaderSidebar.SUBHEADER_SIDEBAR_CLS, style().display(none))
            .add(media().minWidth(px(1100))
                .rule(SubheaderSidebar.SUBHEADER_SIDEBAR_CLS.cls(SubheaderSidebar.HAS_HEADERS.name()), style().display(block)))
            // Phone: stack vertically; the section sidebar becomes a
            // horizontally scrolling chip strip above the content.
            .add(media().maxWidth(px(767))
                .rule(DOCS_LAYOUT, style().flexDirection(column))
                .rule(DocSidebar.DOCS_SIDEBAR, style()
                    .width(auto).padding(SP_3, SP_4)
                    .borderRight(none)
                    .borderBottom(px(1), solid, BORDER)
                    .overflowX(auto).overflowY(hidden))
                .rule(DocSidebar.DOCS_SIDEBAR_INNER, row(SP_4))
                .rule(DocSidebar.NAV_SECTION, style().marginBottom(zero))
                .rule(DocSidebar.NAV_TITLE, style().display(none))
                .rule(DocSidebar.NAV_LINKS, style().flexDirection(row))
                .rule(DocSidebar.NAV_LINK, style().whiteSpace(nowrap)))
            .rule(DOCS_CONTENT.pseudoEl("-webkit-scrollbar"), style().width(px(6)))
            .rule(DOCS_CONTENT.pseudoEl("-webkit-scrollbar-track"), style().background(transparent))
            .rule(DOCS_CONTENT.pseudoEl("-webkit-scrollbar-thumb"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(DOCS_CONTENT.pseudoEl("-webkit-scrollbar-thumb").hover(), style().background(rgba(0, 0, 0, 0.2)))
            .rule(DocSidebar.DOCS_SIDEBAR.pseudoEl("-webkit-scrollbar"), style().width(px(4)).height(px(4)))
            .rule(DocSidebar.DOCS_SIDEBAR.pseudoEl("-webkit-scrollbar-track"), style().background(transparent))
            .rule(DocSidebar.DOCS_SIDEBAR.pseudoEl("-webkit-scrollbar-thumb"), style().background(rgba(0, 0, 0, 0.05)).borderRadius(px(2)))
            .rule(DocSidebar.DOCS_SIDEBAR.pseudoEl("-webkit-scrollbar-thumb").hover(), style().background(rgba(0, 0, 0, 0.1)))
            .rule(SubheaderSidebar.SUBHEADER_NAV.pseudoEl("-webkit-scrollbar"), style().width(px(6)))
            .rule(SubheaderSidebar.SUBHEADER_NAV.pseudoEl("-webkit-scrollbar-track"), style().background(transparent))
            .rule(SubheaderSidebar.SUBHEADER_NAV.pseudoEl("-webkit-scrollbar-thumb"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(SubheaderSidebar.SUBHEADER_NAV.pseudoEl("-webkit-scrollbar-thumb").hover(), style().background(rgba(0, 0, 0, 0.2)))
            .rule(DOCS_CONTENT.descendant(Selector.type("h2")).or(DOCS_CONTENT.descendant(Selector.type("h3"))), style().scrollMarginTop(rem(1.5)))
            // Code-block copy button: hidden until the block is hovered or the
            // button keyboard-focused; CodeCopyScript toggles .copied on click.
            // Hover reveal must be class rules — inline styles can't do :hover.
            .rule(DocComponents.COPY_BTN, chip()
                .position(absolute).top(SP_2).right(SP_2)
                .opacity(0)
                .transitionOpacity(s(0.15)))
            .rule(DocComponents.DOC_CODE.hover().descendant(DocComponents.COPY_BTN).or(DocComponents.COPY_BTN.focusVisible()), style()
                .opacity(1))
            .rule(DocComponents.COPY_BTN.hover(), chipHover())
            .rule(DocComponents.COPY_BTN.cls(DocComponents.COPIED.name()), style()
                .color(hex("#6ee7b7")).borderColor(rgba(110, 231, 183, 0.4)))
            // Touch screens have no hover — keep the button always visible.
            .add(media().noHover()
                .rule(DocComponents.COPY_BTN, style().opacity(1)))
            // Version chip on the dependency block: a details/summary dropdown
            // styled like the copy button, sitting just left of it. Always
            // visible — it carries information (the version), not just an action.
            .rule(VER_PICKER, style()
                .position(absolute).top(SP_2).right(rem(4.4)).margin(zero))
            .rule(VER_PICKER.descendant(Selector.type("summary")), chip()
                .listStyle(none))
            .rule(VER_PICKER.descendant(Selector.type("summary")).pseudoEl("-webkit-details-marker"), style().display(none))
            .rule(VER_PICKER.descendant(Selector.type("summary")).hover(), chipHover())
            .rule(VER_PICKER.attr("open").descendant(Selector.type("summary")), style().color(hex("#f1f5f9")))
            .rule(VER_PICKER_MENU, style()
                .position(absolute).right(zero).marginTop(px(6))
                .minWidth(px(150))
                .backgroundColor(hex("#1e293b"))
                .border(px(1), solid, rgba(255, 255, 255, 0.15))
                .borderRadius(px(8)).padding(px(4))
                .boxShadow(shadow(0, px(8), px(24), rgba(0, 0, 0, 0.35)))
                .zIndex(20))
            .rule(VER_PICKER_ITEM, style()
                .display(block).padding(px(6), px(10)).borderRadius(px(6))
                .fontSize(rem(0.75)).color(hex("#cbd5e1"))
                .textDecoration(none))
            .rule(VER_PICKER_ITEM.hover(), style()
                .backgroundColor(rgba(255, 255, 255, 0.1)).color(hex("#f1f5f9")))
            .rule(VER_PICKER_ITEM.cls(CURRENT.name()), style()
                .color(hex("#6ee7b7")).fontWeight(600))
            .rule(DocSidebar.NAV_LINK.cls(DocSidebar.ACTIVE.name()).or(SubheaderSidebar.SUBHEADER_LINK.cls(DocSidebar.ACTIVE.name())), style()
                .position(relative)
                .overflow(visible)
                .zIndex(1))
            .rule(DocSidebar.NAV_LINK.cls(DocSidebar.ACTIVE.name()).before().or(SubheaderSidebar.SUBHEADER_LINK.cls(DocSidebar.ACTIVE.name()).before()), style()
                .content()
                .position(absolute).inset(zero)
                .borderRadius(ROUNDED)
                .padding(px(2))
                .apply(brandFlow())
                .borderMask()
                .zIndex(-1)
                .pointerEvents(none))
            // The /docs/tell link keeps its inline styling, so it needs a real
            // :hover rule of its own — an inline style cannot express one.
            .rule(DOCS_TELL_LINK.hover(), style()
                .color(PRIMARY)
                .borderColor(PRIMARY)
                .backgroundColor(hex("#eef2ff")));
    }

    /**
     * The pill sitting on a dark code block — the copy button and the version
     * picker are the same object with different contents.
     */
    private static jweb.Style<?> chip() {
        return style()
            .padding(px(4), px(10))
            .fontSize(rem(0.75)).lineHeight(1.4)
            .color(hex("#cbd5e1"))
            .backgroundColor(rgba(255, 255, 255, 0.08))
            .border(px(1), solid, rgba(255, 255, 255, 0.15))
            .borderRadius(px(6))
            .cursor(pointer);
    }

    /** The chip's hover state. */
    private static jweb.Style<?> chipHover() {
        return style()
            .backgroundColor(rgba(255, 255, 255, 0.18))
            .color(hex("#f1f5f9"));
    }
}
