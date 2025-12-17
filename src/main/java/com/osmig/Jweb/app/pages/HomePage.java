package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.app.layouts.MainLayout;
import com.osmig.Jweb.app.partials.Card;

/**
 * Home page template.
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
                // Hero section
                div(attrs().style(
                        style()
                            .textAlign(center)
                            .marginBottom(SPACE_2XL)
                    ),
                    h1(attrs().style(
                        style()
                            .fontSize(FONT_3XL)
                            .color(TEXT)
                            .marginBottom(SPACE_MD)
                    ), text("Welcome to JWeb")),
                    p(attrs().style(
                        style()
                            .fontSize(FONT_LG)
                            .color(TEXT_LIGHT)
                            .maxWidth(px(600))
                            .margin(zero, auto)
                    ), text("Build beautiful web apps in pure Java. No JavaScript, no templates, just code."))
                ),

                // Features section
                h2(attrs().style(
                    style()
                        .fontSize(FONT_2XL)
                        .color(TEXT)
                        .marginBottom(SPACE_LG)
                ), text("Features")),

                // Cards grid
                div(attrs().style(
                        style()
                            .display(grid)
                            .gridTemplateColumns("repeat(auto-fit, minmax(280px, 1fr))")
                            .gap(SPACE_LG)
                    ),
                    new Card(
                        "Type-Safe HTML",
                        "Generate valid HTML with compile-time safety. No more typos in tag names."
                    ),
                    new Card(
                        "Fluent CSS",
                        "Style components with a type-safe CSS builder. IDE autocomplete included."
                    ),
                    new Card(
                        "Component-Based",
                        "Build reusable UI components that compose together beautifully."
                    )
                )
            )
        );
    }
}
