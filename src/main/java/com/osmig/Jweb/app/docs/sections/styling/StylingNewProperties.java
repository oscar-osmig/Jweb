package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/**
 * The properties and at-rules 3.0 added to the Style builder.
 */
public final class StylingNewProperties {
    private StylingNewProperties() {}

    public static Element render() {
        return section(
            h3Title("Newer properties"),
            para("Properties the DSL was missing — each one is a typed method now, "
                + "not a prop(\"name\", \"value\") string."),

            codeBlock("""
// An input that grows with what is typed into it
input(type("text"), style().fieldSizing(content))

// Scrollbars
style().scrollbarColor(hex("#94a3b8"), transparent)
style().scrollbarWidth(thin)
style().scrollbarGutter(stable)              // reserve the space
style().scrollbarGutter(stableBothEdges)     // …on both sides

// Trim the font's half-leading so a heading's box hugs its glyphs
style().textBoxTrim(trimBoth).textBoxEdge(capAlphabetic)
style().textBox(trimBoth, capAlphabetic)     // the shorthand

// Animate to and from auto / min-content / fit-content
stylesheet().rule(":root", style().interpolateSize(allowKeywords))

// Anchor positioning: confine anchor names to this subtree, so a repeated
// component resolves to its own anchor instead of the first in the document
style().anchorScope("all")

// One view-transition rule for every card, instead of a name per element
style().viewTransitionClass("card")

// One-value border sides (the three-value form is still there)
style().borderRight(none)"""),

            h3Title("@starting-style"),
            para("The values a transition animates from the first time an element is "
                + "rendered — what makes a popover or a toast fade in rather than "
                + "appear. It works on an inline style and on a stylesheet."),

            codeBlock("""
// On an inline style
div(style()
        .opacity(1).transition(all, ms(200), ease)
        .startingStyle(style().opacity(0)),
    "I fade in")

// On a stylesheet
stylesheet()
    .rule(".toast", style().opacity(1).transition(all, ms(200), ease))
    .startingStyle(".toast", style().opacity(0))"""),

            h3Title("Container style queries"),
            para("A container query can also match on a custom property, not just a "
                + "size — one variant flag on the container restyles everything in it."),

            codeBlock("""
import static jweb.css.ContainerQuery.*;

container("card").style("variant", "featured")
    .rule(".title", style().fontWeight(700))
    .rule(".badge", style().display(block))

// @container card style(--variant: featured) { … }"""),

            h3Title(":popover-open and :open"),
            codeBlock("""
// On the Selector builder — Selectors.cls/tag, since cls()/tag() are also El's
Selectors.cls("menu").popoverOpen()        // .menu:popover-open
Selectors.tag("details").open()            // details:open

// Or straight on an inline style
div(popover("auto"), style()
        .opacity(0)
        .popoverOpen(style().opacity(1))
        .startingStyle(style().opacity(0)),
    "menu")""")
        );
    }
}
