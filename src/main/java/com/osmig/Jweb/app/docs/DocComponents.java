package com.osmig.Jweb.app.docs;

import jweb.Element;
import java.util.List;
import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.docs.DocStyles.*;

public final class DocComponents {
    private DocComponents() {}

    public static Element section(Element... children) {
        return div(style().maxWidth(px(900)), fragment(children));
    }

    public static Element docTitle(String t) { return h1(attrs().style(title()), t); }

    public static Element docSubtitle(String t) {
        String id = toSlug(t);
        return h2(attrs().id(id).style(subtitle()), t);
    }

    public static Element h3Title(String t) {
        String id = toSlug(t);
        return h3(attrs().id(id).style(subtitle().fontSize(rem(1.1)).marginTop(rem(1.5))), t);
    }

    private static String toSlug(String text) {
        return text.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    public static Element para(String t) { return p(attrs().style(paragraph()), t); }

    /** A sample in the JWeb DSL — plain Java, and compiled as such by {@code DocSamplesCompileTest}. */
    public static Element codeBlock(String c) { return codeBlock("java", c); }

    /**
     * A sample in {@code lang}. The language rides out as {@code data-lang} on the
     * {@code <pre>} (a syntax hint for the page), and it is what
     * {@code jweb.DocSamplesCompileTest} reads to decide what to do with the block:
     *
     * <ul>
     *   <li>{@code "java"} — compiled as written.</li>
     *   <li>{@code "style"} — a chain of {@link jweb.Style} methods with the receiver
     *       elided; each chain is compiled as {@code style()} followed by the chain.</li>
     *   <li>anything else ({@code bash}, {@code yaml}, {@code xml}, {@code html},
     *       {@code json}, {@code css}, {@code text}) — not Java, and not compiled.</li>
     * </ul>
     */
    public static Element codeBlock(String lang, String c) {
        // Wrapper anchors the copy button; its hover reveal and the click
        // handler live in DocsPage.docsStyles() and CodeCopyScript.
        return div(attrs().cls("doc-code").style(style().position(relative)),
            pre(attrs().data("lang", lang).style(DocStyles.codeBlock()), code(c)),
            button(attrs().cls("code-copy-btn").type("button").aria("label", "Copy code to clipboard"),
                "Copy"));
    }

    public static Element inlineCode(String c) { return span(attrs().style(DocStyles.inlineCode()), c); }

    public static Element docList(String... items) {
        // Nulls are skipped so sinceText(...) items can drop out per version
        List<String> present = java.util.Arrays.stream(items)
            .filter(java.util.Objects::nonNull).toList();
        return ul(attrs().style(list()), each(present, i -> li(attrs().style(listItem()), i)));
    }

    public static Element docTip(String t) { return div(attrs().style(tip()), t); }

    public static Element warn(String t) { return div(attrs().style(warning()), t); }

    public static Element spacer() { return div(style().height(rem(2))); }

    // ==================== Version-dependent content ====================
    // For prose that differs between releases inside a shared section. The
    // version in view comes from the render context DocContent sets
    // (DocVersions.current()); on the latest docs everything since() shows
    // and everything before() is gone.

    /** Content that exists from {@code floor} on — hidden when older docs render. */
    public static Element since(String floor, Element... content) {
        return DocVersions.atLeast(DocVersions.current(), floor) ? fragment(content) : null;
    }

    /** Content that was only true before {@code floor} — the old way of things. */
    public static Element before(String floor, Element... content) {
        return DocVersions.atLeast(DocVersions.current(), floor) ? null : fragment(content);
    }

    /** A text item that exists from {@code floor} on; docList skips the null. */
    public static String sinceText(String floor, String text) {
        return DocVersions.atLeast(DocVersions.current(), floor) ? text : null;
    }
}
