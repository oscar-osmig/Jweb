package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingVariables {
    private StylingVariables() {}

    public static Element render() {
        return section(
            h3Title("CSS Variables"),
            para("Use CSS custom properties for theming and design tokens."),

            codeBlock("""
import static jweb.Css.*;

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
            codeBlock("style", """
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
            para("Theme.dark() does this for you — the block below is what it emits. "
                 + "Write it by hand only when the tokens are not a design system."),
            codeBlock("""
// Light theme (default)
rule(":root")
    .var("bg-color", white)
    .var("text-color", hex("#1f2937"))
    .var("border-color", hex("#e5e7eb"))

// Dark theme
media().prefersDark().rule(":root", style()
    .var("bg-color", hex("#1f2937"))
    .var("text-color", hex("#f9fafb"))
    .var("border-color", hex("#374151")))

// Components automatically adapt
rule("body")
    .backgroundColor(var("bg-color"))
    .color(var("text-color"))

rule(".card")
    .border(px(1), solid, var("border-color"))"""),

            h3Title("One token builder"),
            para("designSystem(), theme() and the scoped()/component() string helpers "
                 + "are gone in 3.0 — they were four naming schemes for the same job. "
                 + "jweb.css.Theme is the one that stayed; see Design tokens above."),
            codeBlock("""
// Before (3 competing builders) — designSystem(), theme() and the scoped()/component() helpers are gone
// String tokens = designSystem().spacing(rem(0.5), rem(1)).colors("primary", blue).build();
// String themes = theme().light("bg", white).dark("bg", black).buildBoth();

// After
Theme TOKENS = Theme.light()
    .space("2", rem(0.5)).space("4", rem(1))
    .color("primary", blue).color("bg", white)
    .dark(Theme.dark().color("bg", black));

stylesheet().add(TOKENS);                       // emit
style().color(Theme.color("primary"));          // read"""),

            docTip("CSS variables cascade and can be overridden at any level. Define global variables in :root and override in component classes as needed.")
        );
    }
}
