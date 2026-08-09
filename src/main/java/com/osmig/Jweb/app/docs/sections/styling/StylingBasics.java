package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingBasics {
    private StylingBasics() {}

    public static Element render() {
        return section(
            h3Title("Inline Styles"),
            para("Use the fluent style() builder for inline CSS."),
            codeBlock("""
// Basic inline style
div(style()
    .padding(rem(2))
    .margin(rem(1))
    .backgroundColor(hex("#f5f5f5")), content)

// Chain multiple properties
div(style()
    .width(percent(100))
    .maxWidth(px(1200))
    .margin(px(0), auto)
    .padding(rem(1), rem(2)))"""),

            h3Title("Style Object"),
            para("Create reusable Style objects for consistent styling."),
            codeBlock("""
// Define a reusable style
Style cardStyle = style()
    .padding(rem(1.5))
    .backgroundColor(white)
    .borderRadius(px(8))
    .boxShadow(px(0), px(2), px(4), rgba(0, 0, 0, 0.1));

// Apply to elements — pass the style directly
div(cardStyle, content)

// Compose into other styles
div(style().apply(cardStyle).marginTop(rem(2)), content)

// Extend styles
Style highlightedCard = cardStyle
    .border(px(2), solid, hex("#6366f1"));""")
        );
    }
}
