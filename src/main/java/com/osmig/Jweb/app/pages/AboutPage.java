package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * About page explaining the project.
 */
public class AboutPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style().maxWidth(px(700)).margin(zero, auto).padding(rem(4), SP_8).done(),
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("About JWeb")),
            p(attrs().style().marginTop(SP_6).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb was born from a simple idea: what if building web apps in Java didn't require switching between languages? No more context-switching between Java, HTML, CSS, and JavaScript.")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("With JWeb, your entire frontend is Java. Elements are methods. Styles are builders. Routes are lambdas. Everything compiles, everything has autocomplete, and your IDE catches errors before runtime.")),
            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Why Pure Java?")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("Modern web development often means managing multiple toolchains: npm, webpack, transpilers, and more. JWeb eliminates that complexity. Just Maven and Java. Your frontend code lives alongside your backend, sharing types and logic.")),
            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Built for Developers")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb provides type-safe DSLs for HTML and CSS, a fluent routing API, middleware support, state management, and more. All with the tooling Java developers already know and love."))
        );
    }
}
