package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingResponsive {
    private StylingResponsive() {}

    public static Element render() {
        return section(
            h3Title("Media Queries"),
            para("Create responsive designs that adapt to screen sizes."),

            codeBlock("""
// Basic breakpoint
media().minWidth(px(768)).rules(
    rule(".container").maxWidth(px(720))
)

// Combined conditions
media().screen().minWidth(px(1024)).maxWidth(px(1280)).rules(
    rule(".sidebar").display(none)
)

// Common breakpoints (presets)
xs()        // max-width: 575px
sm()        // min-width: 576px
md()        // min-width: 768px
lg()        // min-width: 992px
xl()        // min-width: 1200px
xxl()       // min-width: 1400px

// Device presets
mobile()    // max-width: 767px
tablet()    // 768px - 1023px
desktop()   // min-width: 1024px"""),

            h3Title("Responsive Example"),
            codeBlock("""
String css = styles(
    // Base styles (mobile-first)
    rule(".grid")
        .display(grid)
        .gridTemplateColumns(fr(1))
        .gap(rem(1)),

    // Tablet and up
    media().minWidth(px(768)).rules(
        rule(".grid").gridTemplateColumns(fr(1), fr(1))
    ),

    // Desktop
    media().minWidth(px(1024)).rules(
        rule(".grid").gridTemplateColumns(fr(1), fr(1), fr(1))
    )
);"""),

            h3Title("Dark Mode"),
            para("Support dark color scheme."),
            codeBlock("""
// Detect system preference
media().prefersDark().rules(
    rule(":root")
        .set("--bg-color", "#1a1a1a")
        .set("--text-color", "#f5f5f5"),
    rule("body")
        .backgroundColor(var("bg-color"))
        .color(var("text-color"))
)

// lightDark() function
.backgroundColor(lightDark(white, hex("#1a1a1a")))
.color(lightDark(black, white))"""),

            h3Title("Accessibility Queries"),
            codeBlock("""
// Reduced motion (accessibility)
media().prefersReducedMotion().rules(
    rule("*")
        .animationDuration(ms(0))
        .transitionDuration(ms(0))
)

// High contrast
media().prefersContrast("more").rules(
    rule("*").borderWidth(px(2))
)"""),

            h3Title("Orientation & Features"),
            codeBlock("""
// Portrait/landscape
media().portrait().rules(...)
media().landscape().rules(...)

// Hover capability
media().hover().rules(
    rule(".card:hover").transform(translateY(px(-4)))
)

media().noHover().rules(
    // Styles for touch devices without hover
)

// Retina displays
media().retina().rules(
    rule(".logo").backgroundImage(url("/logo@2x.png"))
)

// Print styles
media().print().rules(
    rule(".no-print").display(none),
    rule("body").fontSize(pt(12))
)"""),

            h3Title("Container Queries"),
            para("Style based on container size, not viewport."),
            codeBlock("""
// Define container
rule(".card-container")
    .containerType(inlineSize)
    .containerName("card")

// Query the container
container("card").minWidth(px(400)).rules(
    rule(".card")
        .display(grid)
        .gridTemplateColumns(fr(1), fr(2))
)

// Inline container query in style
style()
    .containerType(inlineSize)
    // Children can use @container queries""")
        );
    }
}
