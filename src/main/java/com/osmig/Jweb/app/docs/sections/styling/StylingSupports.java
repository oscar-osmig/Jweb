package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingSupports {
    private StylingSupports() {}

    public static Element render() {
        return section(
            h3Title("@supports Feature Queries"),
            para("Test for CSS feature support before applying styles."),
            codeBlock("""
import static com.osmig.Jweb.framework.styles.Supports.*;

// Check for single property support
supports(property("display", "grid"),
    rule(".container")
        .display(grid)
        .gridTemplateColumns(repeat(3, fr(1)))
)

// Fallback for unsupported browsers
supportsNot(property("display", "grid"),
    rule(".container")
        .display(block)
)"""),

            h3Title("Combining Conditions"),
            para("Use and/or to combine feature queries."),
            codeBlock("""
// AND condition - both must be supported
supports(
    and(
        property("display", "flex"),
        property("gap", "1rem")
    ),
    rule(".flex-gap")
        .display(flex)
        .gap(rem(1))
)

// OR condition - either is supported
supports(
    or(
        property("backdrop-filter", "blur(10px)"),
        property("-webkit-backdrop-filter", "blur(10px)")
    ),
    rule(".glass")
        .backdropFilter("blur(10px)")
)"""),

            h3Title("Selector Support"),
            para("Test for CSS selector support."),
            codeBlock("""
// Check if :has() is supported
supports(selector(":has(> img)"),
    rule(".card:has(> img)")
        .paddingTop(px(0))
)

// Check for :focus-visible
supports(selector(":focus-visible"),
    rule("button:focus-visible")
        .outline(px(2), solid, hex("#0066cc"))
)"""),

            h3Title("Complete Example"),
            codeBlock("""
String css = styles(
    // Base styles
    rule(".container")
        .display(block)
        .padding(rem(1)),

    // Progressive enhancement with grid
    supports(property("display", "grid"),
        rule(".container")
            .display(grid)
            .gridTemplateColumns(repeat(auto_fit, minmax(px(250), fr(1))))
            .gap(rem(1))
    ),

    // Modern color functions
    supports(property("color", "oklch(0.5 0.2 240)"),
        rule(".accent")
            .color("oklch(0.7 0.15 200)")
    )
).render();"""),

            docTip("Use @supports for progressive enhancement - base styles first, then enhancements.")
        );
    }
}
