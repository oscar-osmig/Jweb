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

            docSubtitle("Bare Styles — the concise form"),
            para("When an element only needs styling, pass style() directly as an " +
                 "argument — no attrs() ceremony. It composes with Attr shortcuts like " +
                 "class_() and id()."),
            codeBlock("""
                    div(style().padding(SP_4).color(TEXT), text("hi"))

                    div(class_("card"), id("hero"),
                        style().margin(zero).borderRadius(px(12)),
                        p("content"))"""),

            docSubtitle("Composing Style Fragments"),
            para("Define a style once, reuse it everywhere with apply() — the " +
                 "composition primitive for design systems."),
            codeBlock("""
                    // In your Theme:
                    public static Style<?> brandFlow() {
                        return style().background(BRAND_GRADIENT)
                                      .backgroundSize(percent(300), percent(100))
                                      .animation(anim("gradientShift"), s(3), linear, s(0), infinite);
                    }

                    // Anywhere:
                    button(style().padding(SP_3).apply(brandFlow()).color(white), ...)

                    // Handy helpers that replace prop("...") strings:
                    style().content()                      // content: '' for ::before/::after
                    style().borderMask()                   // gradient-border mask trick
                    style().srOnly()                       // screen-reader-only pattern
                    style().transitionAll(s(0.2))          // also transitionColors/Background/...
                    style().backdropFilter(blur(px(10)))   // emits -webkit- prefix too"""),

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
