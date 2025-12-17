package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.app.layouts.MainLayout;

/**
 * About page template.
 */
public class AboutPage implements Template {

    public AboutPage() {}

    public static AboutPage create() {
        return new AboutPage();
    }

    @Override
    public Element render() {
        return new MainLayout("About - JWeb",
            div(
                h1(attrs().style(
                    style()
                        .fontSize(FONT_3XL)
                        .color(TEXT)
                        .marginBottom(SPACE_MD)
                ), text("About JWeb")),

                p(attrs().style(
                    style()
                        .fontSize(FONT_LG)
                        .color(TEXT_LIGHT)
                        .marginBottom(SPACE_XL)
                        .lineHeight(1.8)
                ), text("JWeb is a Java web framework that lets you build web applications entirely in Java. " +
                                "No more context-switching between languages.")),

                h2(attrs().style(
                    style()
                        .fontSize(FONT_2XL)
                        .color(TEXT)
                        .marginBottom(SPACE_LG)
                ), text("Why JWeb?")),

                ul(attrs().style(
                        style()
                            .listStyle(none)
                            .padding(zero)
                    ),
                    featureItem("No JavaScript required - everything is Java"),
                    featureItem("Type-safe templates with compile-time validation"),
                    featureItem("Fluent CSS builder with IDE autocomplete"),
                    featureItem("Component-based architecture for reusable UI"),
                    featureItem("Spring Boot integration for production-ready apps")
                ),

                // Back link
                div(attrs().style(style().marginTop(SPACE_XL)),
                    a(attrs().href("/").style(
                        style()
                            .color(PRIMARY)
                            .fontSize(FONT_BASE)
                    ), text("\u2190 Back to Home"))
                )
            )
        );
    }

    private Element featureItem(String text) {
        return li(attrs().style(
                style()
                    .padding(SPACE_SM, zero)
                    .paddingLeft(SPACE_LG)
                    .position(relative)
                    .color(TEXT_LIGHT)
            ),
            span(attrs().style(
                style()
                    .position(absolute)
                    .left(zero)
                    .color(PRIMARY)
            ), text("\u2713")),
            text(text)
        );
    }
}
