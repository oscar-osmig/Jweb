package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
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
                // Hero section with animated entrance
                div(attrs().class_("slide-up").style(
                        style()
                            .textAlign(center)
                            .paddingTop(SPACE_XL)
                            .paddingBottom(SPACE_2XL)
                    ),
                    // Badge
                    span(attrs().style(
                        style()
                            .display(inlineBlock)
                            .padding(SPACE_XS, SPACE_MD)
                            .fontSize(FONT_SM)
                            .fontWeight(500)
                            .borderRadius(RADIUS_FULL)
                            .backgroundColor(rgba(102, 126, 234, 0.1))
                            .color(PRIMARY)
                            .marginBottom(SPACE_MD)
                    ), text("Java Web Framework")),
                    // Main heading with gradient
                    h1(attrs().class_("gradient-text").style(
                        style()
                            .fontSize(rem(2.5))
                            .fontWeight(700)
                            .marginBottom(SPACE_MD)
                            .lineHeight(1.2)
                    ), text("Build Beautiful Web Apps")),
                    h1(attrs().style(
                        style()
                            .fontSize(rem(2.5))
                            .fontWeight(700)
                            .color(TEXT)
                            .marginBottom(SPACE_LG)
                            .lineHeight(1.2)
                    ), text("in Pure Java")),
                    // Subtitle
                    p(attrs().style(
                        style()
                            .fontSize(FONT_LG)
                            .color(TEXT_LIGHT)
                            .maxWidth(px(550))
                            .margin(zero, auto)
                            .marginBottom(SPACE_XL)
                            .lineHeight(1.7)
                    ), text("No JavaScript, no something else, just Java code. Type-safe HTML generation with a fluent CSS builder.")),
                    // CTA Buttons
                    div(attrs().style(
                            style()
                                .display(flex)
                                .justifyContent(center)
                                .gap(SPACE_MD)
                                .flexWrap(wrap)
                        ),
                        a(attrs().href("/about").class_("btn").style(
                            btnPrimary()
                        ), text("Learn More")),
                        a(attrs().href("/contact").style(
                            btnSecondary()
                        ), text("Get in Touch"))
                    )
                ),

                // Features section
                div(attrs().style(
                        style()
                            .paddingTop(SPACE_XL)
                            .paddingBottom(SPACE_XL)
                    ),
                    // Section header
                    div(attrs().style(
                            style()
                                .textAlign(center)
                                .marginBottom(SPACE_XL)
                        ),
                        h2(attrs().style(
                            style()
                                .fontSize(FONT_2XL)
                                .color(TEXT)
                                .marginBottom(SPACE_SM)
                        ), text("Why Choose JWeb?")),
                        p(attrs().style(
                            style()
                                .color(TEXT_LIGHT)
                                .maxWidth(px(500))
                                .margin(zero, auto)
                        ), text("Everything you need to build modern web applications"))
                    ),

                    // Feature cards grid with staggered animation
                    div(attrs().style(
                            style()
                                .display(grid)
                                .gridTemplateColumns("repeat(auto-fit, minmax(280px, 1fr))")
                                .gap(SPACE_LG)
                        ),
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
                ),

                // Tech stack section
                div(attrs().style(
                        style()
                            .backgroundColor(BG_LIGHT)
                            .borderRadius(RADIUS_LG)
                            .padding(SPACE_XL)
                            .marginTop(SPACE_XL)
                            .textAlign(center)
                    ),
                    h3(attrs().style(
                        style()
                            .fontSize(FONT_XL)
                            .color(TEXT)
                            .marginBottom(SPACE_LG)
                    ), text("Built With Modern Tech")),
                    div(attrs().style(
                            style()
                                .display(flex)
                                .justifyContent(center)
                                .gap(SPACE_XL)
                                .flexWrap(wrap)
                        ),
                        techBadge("Java 21"),
                        techBadge("Spring Boot"),
                        techBadge("Virtual DOM"),
                        techBadge("CSS-in-Java")
                    )
                )
            )
        );
    }

    private Element techBadge(String label) {
        return span(attrs().style(
            style()
                .display(inlineBlock)
                .padding(SPACE_SM, SPACE_LG)
                .fontSize(FONT_SM)
                .fontWeight(500)
                .color(TEXT_LIGHT)
                .backgroundColor(white)
                .borderRadius(RADIUS_FULL)
                .boxShadow(px(0), px(2), px(4), SHADOW)
        ), text(label));
    }
}
