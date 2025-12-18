package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.app.layouts.MainLayout;
import com.osmig.Jweb.app.partials.Card;

/**
 * Home page template with hero section, features, and call-to-action.
 */
public class HomePage implements Template {

    public HomePage() {}

    public static HomePage create() {
        return new HomePage();
    }

    @Override
    public Element render() {
        return new MainLayout("Home - JWeb",
            div(
                // Hero section with animated entrance - centered in viewport
                div(attrs().class_("slide-up").style()
                        .display(flex)
                        .flexDirection(column)
                        .justifyContent(center)
                        .alignItems(center)
                        .textAlign(center)
                        .minHeight(vh(85))
                        .paddingTop(SPACE_XL)
                        .paddingBottom(SPACE_2XL)
                    .done(),
                    // Main heading with gradient
                    h1(attrs().class_("gradient-text").style()
                        .fontSize(rem(2.5))
                        .fontWeight(700)
                        .marginBottom(SPACE_MD)
                        .lineHeight(1.2)
                    .done(), text("Build Beautiful Web Apps")),
                    h1(attrs().style()
                        .fontSize(rem(2.5))
                        .fontWeight(700)
                        .color(TEXT)
                        .marginBottom(SPACE_LG)
                        .lineHeight(1.2)
                    .done(), text("in Pure Java")),
                    // Subtitle
                    p(attrs().style()
                        .fontSize(FONT_LG)
                        .color(TEXT_LIGHT)
                        .maxWidth(px(550))
                        .margin(zero, auto)
                        .marginBottom(SPACE_XL)
                        .lineHeight(1.7)
                    .done(), text("No JavaScript, no something else, just Java code. Type-safe HTML generation" +
                            " with a fluent CSS builder.")),
                    // CTA Buttons
                    div(attrs().style()
                            .display(flex)
                            .justifyContent(center)
                            .gap(SPACE_MD)
                            .flexWrap(wrap)
                        .done(),
                        a(attrs().href("/about").class_("btn").style(btnPrimary()), text("Learn More")),
                        a(attrs().href("/contact").style(btnSecondary()), text("Get in Touch"))
                    )
                ),

                // Features section
                div(attrs().style()
                        .paddingTop(SPACE_XL)
                        .paddingBottom(SPACE_XL)
                    .done(),
                    // Section header
                    div(attrs().style()
                            .textAlign(center)
                            .marginBottom(SPACE_XL)
                        .done(),
                        h2(attrs().style()
                            .fontSize(FONT_2XL)
                            .color(TEXT)
                            .marginBottom(SPACE_SM)
                                .marginTop(px(25))
                        .done(), text("Why Choose JWeb?")),
                        p(attrs().style()
                            .color(TEXT_LIGHT)
                            .maxWidth(px(500))
                            .margin(zero, auto)
                        .done(), text("Everything you need to build modern web applications"))
                    ),

                    // Feature cards grid with staggered animation
                    div(attrs().style()
                            .display(grid)
                            .gridTemplateColumns("repeat(auto-fit, minmax(280px, 1fr))")
                            .gap(SPACE_LG)
                        .done(),
                        Card.withAnimation(
                            "Type-Safe HTML",
                            "Generate valid HTML with compile-time safety. IDE autocomplete catches errors before they reach production.",
                            "\uD83D\uDEE1\uFE0F",
                            "scale-in delay-1"
                        ),
                        Card.withAnimation(
                            "Fluent CSS Builder",
                            "Style components with a type-safe CSS API. Media queries, animations, and modern CSS features built-in.",
                            "\uD83C\uDFA8",
                            "scale-in delay-2"
                        ),
                        Card.withAnimation(
                            "Component-Based",
                            "Build reusable UI components that compose together. Create design systems that scale.",
                            "\uD83E\uDDE9",
                            "scale-in delay-3"
                        )
                    )
                )
            )
        );
    }

    private Element techBadge(String label) {
        return span(attrs().style()
            .display(inlineBlock)
            .padding(SPACE_SM, SPACE_LG)
            .fontSize(FONT_SM)
            .fontWeight(500)
            .color(TEXT_LIGHT)
            .backgroundColor(white)
            .borderRadius(RADIUS_FULL)
            .boxShadow(px(0), px(2), px(4), SHADOW)
        .done(), text(label));
    }
}
