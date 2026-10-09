package com.osmig.Jweb.app.docs;

import jweb.Cls;
import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class DocSidebar implements Template {
    /** The left rail — {@code .docs-sidebar} — phone breakpoint lives in {@link DocsPage}. */
    public static final Cls DOCS_SIDEBAR = cls("docs-sidebar");
    /** The chip-strip wrapper used on phone — {@code .docs-sidebar-inner}. */
    public static final Cls DOCS_SIDEBAR_INNER = cls("docs-sidebar-inner");
    /** One link group under a heading — {@code .docs-nav-section}. */
    public static final Cls NAV_SECTION = cls("docs-nav-section");
    /** A group's heading — {@code .docs-nav-title} — hidden on phone. */
    public static final Cls NAV_TITLE = cls("docs-nav-title");
    /** The link list inside a group — {@code .docs-nav-links}. */
    public static final Cls NAV_LINKS = cls("docs-nav-links");
    /** A section link — {@code .docs-nav-link}. */
    public static final Cls NAV_LINK = cls("docs-nav-link");
    /** The current-section/current-heading marker, shared with {@code SubheaderSidebar}'s rail links. */
    public static final Cls ACTIVE = cls("active");

    private final String active;
    private final String version;

    public DocSidebar(String active) {
        this(active, DocVersions.latest());
    }

    public DocSidebar(String active, String version) {
        this.active = active;
        this.version = DocVersions.normalize(version);
    }

    @Override
    public Element render() {
        // Layout props (width, padding, border, overflow) live in DocsPage's
        // .docs-sidebar rules so the phone breakpoint can reshape them.
        return aside(DOCS_SIDEBAR, style()
                .backgroundColor(hex("#fafafa")),
            div(DOCS_SIDEBAR_INNER,
                navSection("Basics",
                    link("intro", "Introduction"),
                    link("setup", "Getting Started"),
                    link("elements", "Elements")),
                navSection("Core",
                    link("styling", "Styling"),
                    link("conditionals", "Conditionals"),
                    link("components", "Components"),
                    link("javascript", "JavaScript")),
                navSection("Features",
                    link("routing", "Routing"),
                    link("fragments", "Fragments"),
                    link("streaming", "Streaming SSR"),
                    link("state", "State"),
                    link("forms", "Forms"),
                    link("layouts", "Layouts")),
                navSection("Advanced",
                    link("api", "REST API"),
                    link("ai", "AI"),
                    link("three", "3D Scenes"),
                    link("performance", "Performance & SEO"),
                    link("security", "Security"),
                    link("ui", "UI Components"),
                    link("data", "Database"),
                    link("devtools", "DevTools")),
                navSection("More",
                    link("examples", "Examples")),
                aiDocsLink()
            )
        );
    }

    /**
     * Link to the plain-text documentation dump that AI assistants ground on.
     *
     * <p>It deliberately carries neither {@code .docs-nav-link} nor any
     * {@code data-swap-*}: this is a real navigation to a download, not a
     * section swap, and the runtime's swap delegation would swallow it.
     *
     * <p>No {@code target="_blank"}: the endpoint replies with
     * Content-Disposition attachment, so the click starts a download without
     * navigating. Opening a tab as well would just leave an empty one behind.
     */
    private Element aiDocsLink() {
        return div(NAV_SECTION,
            h2(NAV_TITLE, style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)), "For AI"),
            // The hover state rides the inline style: this link is outside the
            // section nav, so DocsNavScript's delegated mouseover styling never
            // reaches it, and it needs a real :hover rule.
            a(attrs().href("/docs/tell")
                .title("Downloads every guide and reference topic as one markdown "
                       + "file, for an AI assistant to use as a source"),
                style()
                    .display(block)
                    .padding(SP_2, SP_3).borderRadius(ROUNDED)
                    .border(px(1), dashed, BORDER)
                    .fontSize(TEXT_SM).color(TEXT_LIGHT)
                    .textDecoration(none).transition(all, s(0.15), ease)
                    .hover(style()
                        .color(PRIMARY)
                        .borderColor(PRIMARY)
                        .backgroundColor(hex("#eef2ff"))),
                span(style().display(block), "Download all docs (.md)"),
                span(style()
                        .display(block).marginTop(px(2))
                        .fontFamily(uiMonospace, font("SFMono-Regular"), monospace)
                        .fontSize(px(11)).opacity(0.75),
                    "/docs/tell")));
    }

    private Element navSection(String title, Element... links) {
        return div(NAV_SECTION,
            h2(NAV_TITLE, style()
                .fontSize(TEXT_SM).fontWeight(600).color(TEXT)
                .marginBottom(SP_2).textTransform(uppercase)
                .letterSpacing(em(0.05)), title),
            nav(NAV_LINKS, fragment(links)));
    }

    /**
     * A section link. The {@code data-swap-*} attributes are the whole of the
     * client-side navigation: the runtime fetches the fragment, fills
     * {@code .docs-content}, pushes the history entry (version param included)
     * and restores it on back. {@link DocsNavScript} only adds hover
     * prefetching and the active-link marking on top.
     *
     * <p>Presentation is class rules in {@code DocsPage.docsStyles()} — an
     * inline style would outrank {@code .docs-nav-link.active}, so the marking
     * could never take effect after a swap.</p>
     */
    private Element link(String id, String label) {
        // Sections that don't exist in the viewed version stay out of the nav
        if (!DocVersions.sectionAvailable(id, version)) return null;
        String href = DocVersions.href(id, version);
        return a(attrs().href(href)
            .data("section", id)
            .swap(DocsNavScript.contentHref(id, version), DocsPage.DOCS_CONTENT.build())
            .swapPush(href)
            .data("swap-cache", String.valueOf(DocsNavScript.TTL))
            .cls(NAV_LINK.name()).classIf(id.equals(active), ACTIVE.name()),
            label);
    }
}
