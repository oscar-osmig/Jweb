package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/**
 * Stylesheets that belong to a page, and pseudo-classes on an inline style.
 */
public final class StylingPageStyles {
    private StylingPageStyles() {}

    public static Element render() {
        return section(
            h3Title("Page-owned stylesheets"),
            para("A component owns the class rules it needs. Override styles() and the "
                + "render collects it — the page's, its layout's, and every template "
                + "rendered inside them — dedupes by content, and emits one <style> in "
                + "the document head. Ten instances of a card contribute one copy."),

            codeBlock("""
public class Card implements Template {

    @Override
    public Element render() {
        return div(class_("card"), h3(title), p(body));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(".card", style().padding(rem(1)).borderRadius(px(12)))
            .rule(".card:hover", style().boxShadow(zero, px(4), px(12), rgba(0, 0, 0, 0.1)))
            .add(md().rule(".card", style().padding(rem(1.5))));
    }
}"""),

            para("A layout's stylesheet is the natural home for the reset and the design "
                + "tokens, so no page has to remember to include them."),

            codeBlock("""
public class Layout implements Template {

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .add(Theme.TOKENS)
            .rule("*, *::before, *::after", style().boxSizing(borderBox).margin(zero))
            .rule("body", style().color(TEXT).backgroundColor(BG));
    }
}"""),

            h3Title("Pseudo-classes and media on an inline style"),
            para("An inline style can carry conditional rules — the things a style "
                + "attribute cannot express. They get a generated class named after a "
                + "hash of the rules themselves, the rules go into the page's collected "
                + "stylesheet, and the plain declarations still ride the style attribute."),

            codeBlock("""
a(href("/docs"), style()
        .color(PRIMARY).padding(SP_2, SP_4).borderRadius(ROUNDED)
        .hover(style().backgroundColor(hex("#eef2ff")))
        .focusVisible(style().outline(px(2), solid, PRIMARY))
        .at(md(), style().padding(SP_3, SP_6))
        .dark(style().color(hex("#a5b4fc"))),
    "Documentation")

// renders:  <a href="/docs" class="j-3f9a1c" style="color: var(--color-primary); …">
// and adds: .j-3f9a1c:hover{background-color:#eef2ff}
//           .j-3f9a1c:focus-visible{outline:2px solid var(--color-primary)}
//           @media (min-width: 768px){.j-3f9a1c{padding:0.75rem 1.5rem}}
//           @media (prefers-color-scheme: dark){.j-3f9a1c{color:#a5b4fc}}"""),

            para("Because the class name is a content hash, fifty buttons with the same "
                + "hover rule share one class and one rule block, and the name is the "
                + "same in every render — a swap fragment naming a class the page "
                + "already carries is a no-op."),

            h3Title("The conditional vocabulary"),
            codeBlock("""
style()
    .hover(s)              // :hover
    .focus(s)              // :focus
    .focusVisible(s)       // :focus-visible — keyboard focus only
    .focusWithin(s)        // :focus-within
    .active(s)             // :active
    .visited(s)            // :visited
    .disabled(s)           // :disabled
    .checked(s)            // :checked
    .placeholder(s)        // ::placeholder
    .popoverOpen(s)        // :popover-open
    .before("→ ", s)       // ::before, with its content
    .after("", s)          // ::after
    .on(":nth-child(2n)", s)              // anything else, colons included
    .at(md(), s)                          // inside a media query
    .at(container("card").minWidth(px(400)), s)   // inside a container query
    .dark(s)               // @media (prefers-color-scheme: dark)
    .reducedMotion(s)      // @media (prefers-reduced-motion: reduce)
    .startingStyle(s)      // @starting-style — the values a transition starts from"""),

            para("They nest: a hover inside an at() is wrapped by that query too. And "
                + "apply() carries them, so a mixin can bring its own hover state."),

            codeBlock("""
static Style<?> chip() {
    return row(SP_2).padding(SP_1, SP_3).borderRadius(ROUNDED)
                    .hover(style().backgroundColor(hex("#eef2ff")));
}

span(chip().color(TEXT), "Java 21")      // the hover comes along"""),

            h3Title("Fragments and streaming"),
            para("A swap fragment, a streamed Suspense block and a WebSocket DOM patch "
                + "each carry the CSS their own render introduced — a <style> inserted "
                + "through innerHTML applies, unlike a script. Nothing is sent twice: "
                + "each render path drains only what it has not delivered yet.")
        );
    }
}
