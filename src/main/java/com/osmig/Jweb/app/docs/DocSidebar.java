package com.osmig.Jweb.app.docs;

import jweb.Cls;
import jweb.Element;
import jweb.Id;
import jweb.Template;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
    /** The search box above the nav — {@code .docs-search}. */
    public static final Cls SEARCH_BOX = cls("docs-search");
    /** The search field — {@code #docs-search-input}; {@link DocsSearchScript} listens on it. */
    public static final Id SEARCH_INPUT = id("docs-search-input");
    /** Where the hits land — {@code #docs-search-results}; {@link DocsSearch#results} fills it. */
    public static final Id SEARCH_RESULTS = id("docs-search-results");

    /**
     * Every section, in nav order, with its label. The nav renders from this
     * and {@link DocsSearch} indexes from it, so a new section is listed once.
     */
    public static final Map<String, String> SECTIONS;
    static {
        Map<String, String> s = new LinkedHashMap<>();
        s.put("intro", "Introduction");
        s.put("setup", "Getting Started");
        s.put("elements", "Elements");
        s.put("styling", "Styling");
        s.put("conditionals", "Conditionals");
        s.put("components", "Components");
        s.put("javascript", "JavaScript");
        s.put("routing", "Routing");
        s.put("fragments", "Fragments");
        s.put("streaming", "Streaming SSR");
        s.put("state", "State");
        s.put("forms", "Forms");
        s.put("layouts", "Layouts");
        s.put("api", "REST API");
        s.put("ai", "AI");
        s.put("three", "3D Scenes");
        s.put("performance", "Performance & SEO");
        s.put("security", "Security");
        s.put("ui", "UI Components");
        s.put("data", "Database");
        s.put("devtools", "DevTools");
        s.put("examples", "Examples");
        SECTIONS = Collections.unmodifiableMap(s);
    }

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
                searchBox(),
                navSection("Basics",
                    link("intro"),
                    link("setup"),
                    link("elements")),
                navSection("Core",
                    link("styling"),
                    link("conditionals"),
                    link("components"),
                    link("javascript")),
                navSection("Features",
                    link("routing"),
                    link("fragments"),
                    link("streaming"),
                    link("state"),
                    link("forms"),
                    link("layouts")),
                navSection("Advanced",
                    link("api"),
                    link("ai"),
                    link("three"),
                    link("performance"),
                    link("security"),
                    link("ui"),
                    link("data"),
                    link("devtools")),
                navSection("More",
                    link("examples"),
                    tutorialLink()),
                aiDocsLink()
            )
        );
    }

    /**
     * The step-by-step tutorial is markdown in the repository (its code blocks
     * are compiled by the suite), so this is a real navigation, not a section
     * swap: no {@code data-swap-*}, and {@code .docs-nav-link} only for the look.
     */
    private Element tutorialLink() {
        return a(NAV_LINK, href("https://github.com/oscar-osmig/Jweb/blob/main/readme/tutorial.md"),
            attrs().title("Your first app in fifteen minutes, on GitHub"),
            "Tutorial ↗");
    }

    /**
     * The search field and the container its hits land in. The field has no
     * form around it on purpose: typing is the submit ({@link DocsSearchScript}
     * fetches {@code /docs/search} as you type), and Enter should not reload.
     * Presentation is class rules in {@code DocsPage.styles()}.
     */
    private Element searchBox() {
        return div(SEARCH_BOX,
            input(SEARCH_INPUT, type("search"),
                attrs().placeholder("Search the docs…")
                    .autocomplete("off")
                    .set("aria-label", "Search the documentation")
                    .set("aria-controls", SEARCH_RESULTS.name())),
            div(SEARCH_RESULTS, attrs().set("aria-live", "polite")));
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
    private Element link(String id) {
        String label = SECTIONS.get(id);
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
