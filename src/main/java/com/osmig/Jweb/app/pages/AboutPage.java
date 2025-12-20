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
                text("JWeb is a Java web framework that lets you build complete web applications using only Java. Components, layouts, routing, styling, state management, and server logic all live in one language, one codebase.")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("Everything is a Java method. Components are classes. Styles are fluent builders. Routes are lambdas. Your IDE provides full autocomplete, refactoring, and compile-time verification across your entire application.")),
            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Why JWeb?")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("Traditional web development requires multiple languages and toolchains. JWeb takes a different approach: everything is Java. No npm, no webpack, no transpilers. Just Maven and your IDE. Your entire stack shares types, logic, and tooling.")),
            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Built for Java Developers")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb provides fluent APIs for building components, defining routes, managing state, handling forms, and more. Built on Spring Boot, it integrates seamlessly with the Java ecosystem you already know."))
        );
    }
}
