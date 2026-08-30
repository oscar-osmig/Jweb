package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingNested {
    private StylingNested() {}

    public static Element render() {
        return section(
            h3Title("Nested CSS"),
            para("Write CSS with nesting syntax for cleaner, more organized stylesheets."),
            codeBlock("""
import jweb.css.CSSNested;
import static jweb.Css.*;

// Basic nesting (CSSNested stays qualified — its rule() clashes with Css.rule)
CSSNested.rule(".card")
    .style(style()
        .padding(rem(1))
        .borderRadius(px(8)))
    .nest(".title")
        .style(style()
            .fontSize(rem(1.25))
            .fontWeight(600))
    .parent()
    .nest(".content")
        .style(style().color(hex("#666")))
    .parent()
    .build()

// Generates native CSS nesting:
// .card {
//   padding: 1rem; border-radius: 8px;
//   .title { font-size: 1.25rem; font-weight: 600; }
//   .content { color: #666; }
// }"""),

            h3Title("Parent Selector (&)"),
            para("Reference the parent selector for states and modifiers."),
            codeBlock("""
CSSNested.rule(".button")
    .style(style()
        .padding(rem(0.75), rem(1.5))
        .backgroundColor(hex("#3b82f6")))
    .nest("&:hover")
        .style(style().backgroundColor(hex("#2563eb")))
    .parent()
    .nest("&:active")
        .style(style().transform(scale(0.98)))
    .parent()
    .nest("&.primary")
        .style(style().backgroundColor(hex("#6366f1")))
    .parent()
    .nest("&.disabled")
        .style(style().opacity(0.5).pointerEvents(none))
    .parent()
    .build()"""),

            h3Title("Deep Nesting"),
            para("Nest multiple levels for complex component styles."),
            codeBlock("""
CSSNested.rule(".nav")
    .style(style().display(flex))
    .nest(".menu")
        .style(style().display(flex).gap(rem(1)))
        .nest(".item")
            .style(style().padding(rem(0.5)))
            .nest("&:hover")
                .style(style().color(hex("#3b82f6")))
            .parent()
            .nest("&.active")
                .style(style().fontWeight(600))
            .parent()
        .parent()
    .parent()
    .nest(".logo")
        .style(style().fontSize(rem(1.5)))
    .parent()
    .build()"""),

            h3Title("Media Queries in Nesting"),
            para("Nest media queries inside rules for component-scoped responsive styles."),
            codeBlock("""
CSSNested.rule(".grid")
    .style(style()
        .display(grid)
        .gridTemplateColumns(fr(1))
        .gap(rem(1)))
    .media("(min-width: 768px)")
        .style(style().gridTemplateColumns(repeat(2, fr(1))))
    .parent()
    .media("(min-width: 1024px)")
        .style(style().gridTemplateColumns(repeat(3, fr(1))))
    .parent()
    .build()"""),

            h3Title("Complete Component Example"),
            codeBlock("""
String cardStyles = CSSNested.rule(".card")
    .style(style()
        .backgroundColor(white)
        .borderRadius(px(8))
        .boxShadow(px(0), px(2), px(4), rgba(0, 0, 0, 0.1)))
    .nest(".header")
        .style(style()
            .padding(rem(1))
            .borderBottom(px(1), solid, hex("#e5e7eb")))
        .nest(".title")
            .style(style()
                .fontSize(rem(1.125))
                .fontWeight(600))
        .parent()
    .parent()
    .nest(".body")
        .style(style().padding(rem(1)))
    .parent()
    .nest(".footer")
        .style(style()
            .padding(rem(1))
            .backgroundColor(hex("#f9fafb"))
            .borderRadius(px(0), px(0), px(8), px(8)))
    .parent()
    .nest("&:hover")
        .style(style().boxShadow(px(0), px(4), px(8), rgba(0, 0, 0, 0.15)))
    .parent()
    .build();"""),

            docTip("Nesting keeps related styles together. Limit depth to 3 levels for readability.")
        );
    }
}
