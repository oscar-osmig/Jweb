package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingSupports {
    private StylingSupports() {}

    public static Element render() {
        return section(
            h3Title("@supports Feature Queries"),
            para("Test for CSS feature support before applying styles."),
            codeBlock("""
import static jweb.css.Supports.*;
import static jweb.Css.*;

// Check for single property support
supports("display", "grid")
    .rule(".container", style()
        .display(grid)
        .gridTemplateColumns(repeat(3, fr(1))))
    .build()

// Fallback for unsupported browsers
supports().not().property("display", "grid")
    .rule(".container", style().display(block))
    .build()"""),

            h3Title("Combining Conditions"),
            para("Use and/or to combine feature queries."),
            codeBlock("""
// AND condition - both must be supported
supports("display", "flex").and().property("gap", "1rem")
    .rule(".flex-gap", style()
        .display(flex)
        .gap(rem(1)))

// OR condition - either is supported
supports("backdrop-filter", "blur(10px)")
    .or().property("-webkit-backdrop-filter", "blur(10px)")
    .rule(".glass", style().backdropFilter("blur(10px)"))"""),

            h3Title("Selector Support"),
            para("Test for CSS selector support."),
            codeBlock("""
// Check if :has() is supported
supportsSelector(":has(> img)")
    .rule(".card:has(> img)", style()
        .paddingTop(px(0)))

// Check for :focus-visible
supportsSelector(":focus-visible")
    .rule("button:focus-visible", style()
        .outline(px(2), solid, hex("#0066cc")))"""),

            h3Title("Complete Example"),
            codeBlock("""
String css = styles(
    // Base styles
    rule(".container")
        .display(block)
        .padding(rem(1))
)
// Progressive enhancement with grid
+ supports("display", "grid")
    .rule(".container", style()
        .display(grid)
        .gridTemplateColumns(repeat(autoFit(), minmax(px(250), fr(1))))
        .gap(rem(1)))
    .build()
// Modern color functions
+ supports("color", "oklch(0.5 0.2 240)")
    .rule(".accent", style().prop("color", "oklch(0.7 0.15 200)"))
    .build();"""),

            docTip("Use @supports for progressive enhancement - base styles first, then enhancements.")
        );
    }
}
