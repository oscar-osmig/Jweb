package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingNested {
    private StylingNested() {}

    public static Element render() {
        return section(
            h3Title("Nested CSS"),
            para("Write CSS with nesting syntax for cleaner, more organized stylesheets."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSS.*;

// Basic nesting
rule(".card")
    .padding(rem(1))
    .borderRadius(px(8))
    .nested(
        rule(".title")
            .fontSize(rem(1.25))
            .fontWeight(600),
        rule(".content")
            .color(hex("#666"))
    )

// Generates:
// .card { padding: 1rem; border-radius: 8px; }
// .card .title { font-size: 1.25rem; font-weight: 600; }
// .card .content { color: #666; }"""),

            h3Title("Parent Selector (&)"),
            para("Reference the parent selector for states and modifiers."),
            codeBlock("""
rule(".button")
    .padding(rem(0.75), rem(1.5))
    .backgroundColor(hex("#3b82f6"))
    .nested(
        rule("&:hover")
            .backgroundColor(hex("#2563eb")),
        rule("&:active")
            .transform(scale(0.98)),
        rule("&.primary")
            .backgroundColor(hex("#6366f1")),
        rule("&.disabled")
            .opacity(0.5)
            .pointerEvents(none)
    )"""),

            h3Title("Deep Nesting"),
            para("Nest multiple levels for complex component styles."),
            codeBlock("""
rule(".nav")
    .display(flex)
    .nested(
        rule(".menu")
            .display(flex)
            .gap(rem(1))
            .nested(
                rule(".item")
                    .padding(rem(0.5))
                    .nested(
                        rule("&:hover")
                            .color(hex("#3b82f6")),
                        rule("&.active")
                            .fontWeight(600)
                    )
            ),
        rule(".logo")
            .fontSize(rem(1.5))
    )"""),

            h3Title("Media Queries in Nesting"),
            para("Nest media queries inside rules for component-scoped responsive styles."),
            codeBlock("""
rule(".grid")
    .display(grid)
    .gridTemplateColumns(fr(1))
    .gap(rem(1))
    .nested(
        media("min-width: 768px")
            .gridTemplateColumns(repeat(2, fr(1))),
        media("min-width: 1024px")
            .gridTemplateColumns(repeat(3, fr(1)))
    )"""),

            h3Title("Complete Component Example"),
            codeBlock("""
String cardStyles = rule(".card")
    .backgroundColor(white)
    .borderRadius(px(8))
    .boxShadow(px(0), px(2), px(4), rgba(0, 0, 0, 0.1))
    .nested(
        rule(".header")
            .padding(rem(1))
            .borderBottom(px(1), solid, hex("#e5e7eb"))
            .nested(
                rule(".title")
                    .fontSize(rem(1.125))
                    .fontWeight(600)
            ),
        rule(".body")
            .padding(rem(1)),
        rule(".footer")
            .padding(rem(1))
            .backgroundColor(hex("#f9fafb"))
            .borderRadius(px(0), px(0), px(8), px(8)),
        rule("&:hover")
            .boxShadow(px(0), px(4), px(8), rgba(0, 0, 0, 0.15))
    ).render();"""),

            docTip("Nesting keeps related styles together. Limit depth to 3 levels for readability.")
        );
    }
}
