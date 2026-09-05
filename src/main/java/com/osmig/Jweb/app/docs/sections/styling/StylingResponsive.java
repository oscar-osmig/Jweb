package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingResponsive {
    private StylingResponsive() {}

    public static Element render() {
        return section(
            h3Title("Media Queries"),
            para("Create responsive designs that adapt to screen sizes."),

            codeBlock("""
// Basic breakpoint
media().minWidth(px(768))
    .rule(".container", style().maxWidth(px(720)))

// Combined conditions
media().screen().minWidth(px(1024)).maxWidth(px(1280))
    .rule(".sidebar", style().display(none))

// Common breakpoints (presets)
xs()        // max-width: 575px
sm()        // min-width: 576px
md()        // min-width: 768px
lg()        // min-width: 992px
xl()        // min-width: 1200px
xxl()       // min-width: 1400px

// One breakpoint set only. The mobile()/tablet()/desktop() presets are
// deprecated: they overlapped xs()-xxl() with different pixel values, so
// which set a page used changed where it broke.

// Anything else: state the query
media().maxWidth(px(900))
media().minWidth(px(600)).maxWidth(px(1200))"""),

            h3Title("Responsive Example"),
            codeBlock("""
import static jweb.Css.*;   // brings in stylesheet(), media(), style()

String css = stylesheet()
    // Base styles (mobile-first)
    .rule(".grid", style()
        .display(grid)
        .gridTemplateColumns(fr(1))
        .gap(rem(1)))

    // Tablet and up
    .add(media().minWidth(px(768))
        .rule(".grid", style().gridTemplateColumns(fr(1), fr(1))))

    // Desktop
    .add(media().minWidth(px(1024))
        .rule(".grid", style().gridTemplateColumns(fr(1), fr(1), fr(1))))
    .build();"""),

            h3Title("Dark Mode"),
            para("Support dark color scheme."),
            codeBlock("""
// Detect system preference
media().prefersDark()
    .rule(":root", style()
        .var("bg-color", hex("#1a1a1a"))
        .var("text-color", hex("#f5f5f5")))
    .rule("body", style()
        .backgroundColor(var("bg-color"))
        .color(var("text-color")))

// lightDark() function
.backgroundColor(lightDark(white, hex("#1a1a1a")))
.color(lightDark(black, white))"""),

            h3Title("Accessibility Queries"),
            codeBlock("""
// Reduced motion (accessibility)
media().prefersReducedMotion()
    .rule("*", style()
        .animationDuration(ms(0))
        .transitionDuration(ms(0)))

// High contrast
media().prefersContrast("more")
    .rule("*", style().borderWidth(px(2)))"""),

            h3Title("Orientation & Features"),
            codeBlock("""
// Portrait/landscape
media().portrait().rule("body", style().fontSize(rem(0.95)))
media().landscape().rule("body", style().fontSize(rem(1)))

// Hover capability
media().hover()
    .rule(".card:hover", style().transform(translateY(px(-4))))

media().noHover()
    .rule(".card", style().boxShadow("none"))  // touch devices

// Retina displays
media().retina()
    .rule(".logo", style().backgroundImage(url("/logo@2x.png")))

// Print styles
media().print()
    .rule(".no-print", style().display(none))
    .rule("body", style().fontSize(px(12)))"""),

            h3Title("Container Queries"),
            para("Style based on container size, not viewport."),
            codeBlock("""
// Define container
rule(".card-container")
    .containerType(inlineSize)
    .containerName("card")

// Query the container
container("card").minWidth(px(400))
    .rule(".card", style()
        .display(grid)
        .gridTemplateColumns(fr(1), fr(2)))

// Inline container query in style
style()
    .containerType(inlineSize)
    // Children can use @container queries""")
        );
    }
}
