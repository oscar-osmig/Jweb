package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/**
 * The layout mixins and the token system.
 */
public final class StylingMixins {
    private StylingMixins() {}

    public static Element render() {
        return section(
            h3Title("Layout mixins"),
            para("Every app writes display:flex; align-items:center a hundred times. "
                + "These are that, named. Each returns a Style, so it composes with "
                + "apply(), with tokens, and with the conditional rules above."),

            codeBlock("text", """
row()                  // flex, items centred
row(SP_4)              // …with a gap
stack()                // flex column
stack(SP_4)            // …with a gap
center()               // centred on both axes
cluster(SP_2)          // a row that wraps — chips, tags, a toolbar
container(px(1200))    // full width up to a maximum, then centred
card()                 // surface: background, hairline border, radius, padding
truncate()             // one line, ellipsis
truncate(3)            // three lines, ellipsis
srOnly()               // visually hidden, still read aloud
fullBleed()            // break out of the container to the viewport width
aspect(16, 9)          // a fixed ratio
cover() / contain()    // fill or fit a box — images and video
grid(3, SP_4)          // three equal columns
autoGrid(px(240))      // as many columns as fit — no breakpoints needed"""),

            codeBlock("""
// Before
div(style().display(flex).flexDirection(column).gap(SP_4)
           .maxWidth(px(500)).margin(zero, auto), …)

// After
div(stack(SP_4).maxWidth(px(500)).marginInline(auto), …)

// They stack up
nav(cluster(SP_2).justifyContent(spaceBetween).padding(SP_3, GUTTER), …)
div(row(SP_4).apply(card()).hover(style().borderColor(PRIMARY)), …)"""),

            h3Title("Design tokens"),
            para("A Theme is a set of CSS custom properties. Write it once, emit it "
                + "through a page's stylesheet, and read it back anywhere as a CSSValue "
                + "— the value lives in the document, so devtools shows it and a theme "
                + "swap is a value change rather than a recompile."),

            codeBlock("""
import jweb.css.Theme;

public static final Theme TOKENS = Theme.light()
    .color("primary", hex("#4f46e5"))
    .color("text",    hex("#1e293b"))
    .color("bg",      hex("#ffffff"))
    .space("4",       rem(1))
    .radius("md",     px(6))
    .text("lg",       rem(1.125))
    .dark(Theme.dark()
        .color("text", hex("#e2e8f0"))
        .color("bg",   hex("#0f172a")));

// emit it from the layout
public Stylesheet styles() { return stylesheet().add(TOKENS); }

// read it anywhere
style().color(Theme.color("text")).padding(Theme.space("4"))"""),

            para("The named groups are color, space, radius, font, text, shadow and "
                + "gradient — each prefixes its property (--color-primary, --space-4). "
                + "token(name, value) defines any other name verbatim, and "
                + "Theme.var(name, fallback) reads one with a default."),

            para("css() emits three blocks: the light values on :root, the dark values "
                + "under prefers-color-scheme (guarded so an explicit light choice still "
                + "wins), and the same dark values under [data-theme=dark] so a toggle "
                + "overrides the system setting in both directions."),

            codeBlock("css", """
:root{--color-text:#1e293b;--color-bg:#ffffff;…}
@media (prefers-color-scheme: dark){
  :root:not([data-theme=light]){--color-text:#e2e8f0;--color-bg:#0f172a;}
}
:root[data-theme=dark]{--color-text:#e2e8f0;--color-bg:#0f172a;}"""),

            para("An app names its tokens once and keeps its own constants — the demo "
                + "app's Theme is exactly this: TOKENS plus PRIMARY, SP_4, ROUNDED and "
                + "friends, each a var() reference."),

            codeBlock("""
public static final CSSValue PRIMARY = Theme.color("primary");
public static final CSSValue SP_4    = Theme.space("4");
public static final CSSValue ROUNDED = Theme.radius("md");""")
        );
    }
}
