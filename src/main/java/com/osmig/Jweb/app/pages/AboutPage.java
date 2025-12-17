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

/**
 * About page template with timeline and features.
 */
public class AboutPage implements Template {

    public AboutPage() {}

    public static AboutPage create() {
        return new AboutPage();
    }

    @Override
    public Element render() {
        return new MainLayout("About - JWeb",
            div(attrs().class_("fade-in"),
                // Hero section
                div(attrs().style(
                        style()
                            .textAlign(center)
                            .paddingBottom(SPACE_2XL)
                            .borderBottom(px(1), solid, BORDER_LIGHT)
                            .marginBottom(SPACE_2XL)
                    ),
                    span(attrs().style(
                        style()
                            .display(inlineBlock)
                            .padding(SPACE_XS, SPACE_MD)
                            .fontSize(FONT_SM)
                            .fontWeight(500)
                            .borderRadius(RADIUS_FULL)
                            .backgroundColor(rgba(16, 185, 129, 0.1))
                            .color(SUCCESS)
                            .marginBottom(SPACE_MD)
                    ), text("Open Source")),
                    h1(attrs().class_("gradient-text").style(
                        style()
                            .fontSize(rem(2.25))
                            .fontWeight(700)
                            .marginBottom(SPACE_MD)
                    ), text("About JWeb")),
                    p(attrs().style(
                        style()
                            .fontSize(FONT_LG)
                            .color(TEXT_LIGHT)
                            .maxWidth(px(600))
                            .margin(zero, auto)
                            .lineHeight(1.8)
                    ), text("JWeb is a modern Java web framework that lets you build web applications entirely in Java. No more context-switching between languages."))
                ),

                // Two column layout
                div(attrs().style(
                        style()
                            .display(grid)
                            .gridTemplateColumns("1fr 1fr")
                            .gap(SPACE_2XL)
                    ),
                    // Left column - Why JWeb
                    div(
                        h2(attrs().style(
                            style()
                                .fontSize(FONT_XL)
                                .color(TEXT)
                                .marginBottom(SPACE_LG)
                                .display(flex)
                                .alignItems(center)
                                .gap(SPACE_SM)
                        ), text("\u2728 Why JWeb?")),

                        div(attrs().style(style().display(flex).flexDirection(column).gap(SPACE_MD)),
                            featureCard("\uD83D\uDEE1\uFE0F", "Type-Safe", "Compile-time validation catches errors before runtime"),
                            featureCard("\u26A1", "Fast", "Virtual DOM diffing for efficient updates"),
                            featureCard("\uD83C\uDFA8", "Stylish", "Fluent CSS builder with full IDE support"),
                            featureCard("\uD83E\uDDE9", "Modular", "Component-based architecture that scales")
                        )
                    ),

                    // Right column - How it works
                    div(
                        h2(attrs().style(
                            style()
                                .fontSize(FONT_XL)
                                .color(TEXT)
                                .marginBottom(SPACE_LG)
                                .display(flex)
                                .alignItems(center)
                                .gap(SPACE_SM)
                        ), text("\uD83D\uDEE0\uFE0F How It Works")),

                        div(attrs().style(
                                style()
                                    .backgroundColor(BG_LIGHT)
                                    .borderRadius(RADIUS_LG)
                                    .padding(SPACE_LG)
                            ),
                            step(1, "Write Components", "Create reusable UI components in pure Java"),
                            step(2, "Style with CSS", "Use the fluent CSS builder for type-safe styling"),
                            step(3, "Define Routes", "Map URLs to your page components"),
                            step(4, "Deploy", "Run as a Spring Boot app anywhere")
                        )
                    )
                ),

                // CTA section
                div(attrs().style(
                        style()
                            .marginTop(SPACE_2XL)
                            .padding(SPACE_XL)
                            .backgroundColor(BG_LIGHT)
                            .borderRadius(RADIUS_LG)
                            .textAlign(center)
                    ),
                    h3(attrs().style(
                        style()
                            .fontSize(FONT_XL)
                            .color(TEXT)
                            .marginBottom(SPACE_SM)
                    ), text("Ready to get started?")),
                    p(attrs().style(
                        style()
                            .color(TEXT_LIGHT)
                            .marginBottom(SPACE_LG)
                    ), text("Try JWeb today and build your first Java web app in minutes.")),
                    div(attrs().style(
                            style()
                                .display(flex)
                                .justifyContent(center)
                                .gap(SPACE_MD)
                        ),
                        a(attrs().href("/").class_("btn").style(btnPrimary()), text("\u2190 Back to Home")),
                        a(attrs().href("/contact").style(btnSecondary()), text("Contact Us"))
                    )
                )
            )
        );
    }

    private Element featureCard(String icon, String title, String desc) {
        return div(attrs().style(
                style()
                    .display(flex)
                    .alignItems(flexStart)
                    .gap(SPACE_MD)
                    .padding(SPACE_MD)
                    .backgroundColor(BG_LIGHT)
                    .borderRadius(RADIUS_MD)
            ),
            span(attrs().style(
                style()
                    .fontSize(rem(1.5))
                    .lineHeight(1.0)
            ), text(icon)),
            div(
                strong(attrs().style(
                    style()
                        .display(block)
                        .color(TEXT)
                        .marginBottom(SPACE_XS)
                ), text(title)),
                span(attrs().style(
                    style()
                        .fontSize(FONT_SM)
                        .color(TEXT_LIGHT)
                ), text(desc))
            )
        );
    }

    private Element step(int num, String title, String desc) {
        return div(attrs().style(
                style()
                    .display(flex)
                    .alignItems(flexStart)
                    .gap(SPACE_MD)
                    .paddingBottom(SPACE_MD)
                    .marginBottom(SPACE_MD)
                    .borderBottom(px(1), solid, BORDER_LIGHT)
            ),
            // Step number
            span(attrs().style(
                style()
                    .display(flex)
                    .alignItems(center)
                    .justifyContent(center)
                    .width(px(28))
                    .height(px(28))
                    .borderRadius(RADIUS_FULL)
                    .backgroundColor(PRIMARY)
                    .color(white)
                    .fontSize(FONT_SM)
                    .fontWeight(600)
                    .flexShrink(0)
            ), text(String.valueOf(num))),
            div(
                strong(attrs().style(
                    style()
                        .display(block)
                        .color(TEXT)
                        .marginBottom(SPACE_XS)
                ), text(title)),
                span(attrs().style(
                    style()
                        .fontSize(FONT_SM)
                        .color(TEXT_LIGHT)
                ), text(desc))
            )
        );
    }
}
