package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingVariables {
    private StylingVariables() {}

    public static Element render() {
        return section(
            h3Title("CSS Variables"),
            para("Use CSS custom properties for theming and design tokens."),

            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSVariables.*;

// Define variables in :root
rule(":root")
    .var("primary-color", hex("#6366f1"))
    .var("secondary-color", hex("#8b5cf6"))
    .var("text-color", hex("#1f2937"))
    .var("bg-color", white)
    .var("spacing-sm", rem(0.5))
    .var("spacing-md", rem(1))
    .var("spacing-lg", rem(2))
    .var("radius", px(8))"""),

            h3Title("Using Variables"),
            codeBlock("""
// Reference variable
.color(var("primary-color"))
.padding(var("spacing-md"))
.borderRadius(var("radius"))

// With fallback value
.color(var("accent-color", blue))

// Chained fallback
.color(varChain("custom-color", "primary-color", blue))"""),

            h3Title("Component Example"),
            codeBlock("""
// Button using variables
rule(".btn")
    .padding(var("spacing-sm"), var("spacing-md"))
    .backgroundColor(var("primary-color"))
    .color(white)
    .borderRadius(var("radius"))
    .transition(propBackground, s(0.2))

rule(".btn:hover")
    .backgroundColor(var("secondary-color"))

// Card using variables
rule(".card")
    .padding(var("spacing-lg"))
    .backgroundColor(var("bg-color"))
    .color(var("text-color"))
    .borderRadius(var("radius"))"""),

            h3Title("Dark Mode with Variables"),
            codeBlock("""
// Light theme (default)
rule(":root")
    .var("bg-color", white)
    .var("text-color", hex("#1f2937"))
    .var("border-color", hex("#e5e7eb"))

// Dark theme
media().prefersDark().rules(
    rule(":root")
        .var("bg-color", hex("#1f2937"))
        .var("text-color", hex("#f9fafb"))
        .var("border-color", hex("#374151"))
)

// Components automatically adapt
rule("body")
    .backgroundColor(var("bg-color"))
    .color(var("text-color"))

rule(".card")
    .border(px(1), solid, var("border-color"))"""),

            h3Title("Design System Builder"),
            codeBlock("""
// Generate design tokens
String tokens = designSystem()
    .spacing(rem(0.25), rem(0.5), rem(1), rem(1.5), rem(2), rem(3))
    .colors(
        "primary", hex("#6366f1"),
        "secondary", hex("#8b5cf6"),
        "success", hex("#10b981"),
        "warning", hex("#f59e0b"),
        "error", hex("#ef4444")
    )
    .fontSize(
        rem(0.75),   // xs
        rem(0.875),  // sm
        rem(1),      // base
        rem(1.125),  // lg
        rem(1.25),   // xl
        rem(1.5)     // 2xl
    )
    .build();

// Generates:
// --spacing-1: 0.25rem;
// --spacing-2: 0.5rem;
// --color-primary: #6366f1;
// --font-size-1: 0.75rem;
// etc."""),

            h3Title("Theme Builder"),
            codeBlock("""
// Build light and dark themes together
String themes = theme()
    .light(
        "background", white,
        "surface", hex("#f9fafb"),
        "text", hex("#1f2937"),
        "text-muted", hex("#6b7280")
    )
    .dark(
        "background", hex("#111827"),
        "surface", hex("#1f2937"),
        "text", hex("#f9fafb"),
        "text-muted", hex("#9ca3af")
    )
    .buildBoth();

// Generates :root styles and @media (prefers-color-scheme: dark)"""),

            docTip("CSS variables cascade and can be overridden at any level. Define global variables in :root and override in component classes as needed.")
        );
    }
}
