package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.styling.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingSection {
    private StylingSection() {}

    public static Element render() {
        return section(
            docTitle("CSS DSL"),
            para("JWeb's CSS DSL provides type-safe Java methods for all CSS properties. " +
                 "Write styles with IDE autocomplete and compile-time validation."),

            docSubtitle("Import Statements"),
            codeBlock("""
// Core CSS DSL
import static com.osmig.Jweb.framework.styles.CSS.*;

// Units (px, rem, em, vh, vw, etc.)
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

// Colors (named colors, rgb, hex, etc.)
import static com.osmig.Jweb.framework.styles.CSSColors.*;"""),

            docSubtitle("Inline Styles"),
            para("Apply styles directly to elements using the style builder."),
            codeBlock("""
// Lambda syntax (recommended)
div(attrs()
    .class_("card")
    .style(s -> s
        .padding(rem(1.5))
        .backgroundColor(white)
        .borderRadius(px(8))
        .boxShadow(px(0), px(2), px(8), rgba(0, 0, 0, 0.1))
    ),
    content
)

// Reusable Style object
Style cardStyle = style()
    .padding(rem(1.5))
    .backgroundColor(white)
    .borderRadius(px(8));

div(attrs().style(cardStyle), content)"""),

            docSubtitle("CSS Rules"),
            para("Generate CSS rules for stylesheets."),
            codeBlock("""
// Single rule
String buttonCss = rule(".btn")
    .display(inlineBlock)
    .padding(px(10), px(20))
    .backgroundColor(blue)
    .color(white)
    .borderRadius(px(4))
    .render();

// Multiple rules
String css = styles(
    rule("*").boxSizing(borderBox),
    rule("body")
        .margin(zero)
        .fontFamily("system-ui, -apple-system, sans-serif"),
    rule(".container")
        .maxWidth(px(1200))
        .margin(px(0), auto)
        .padding(px(0), rem(1))
);"""),

            StylingUnits.render(),
            StylingColors.render(),
            StylingBoxModel.render(),
            StylingFlexbox.render(),
            StylingGrid.render(),
            StylingTypography.render(),
            StylingEffects.render(),
            StylingResponsive.render(),
            StylingAnimations.render(),
            StylingVariables.render(),
            StylingModernCSS.render()
        );
    }
}
