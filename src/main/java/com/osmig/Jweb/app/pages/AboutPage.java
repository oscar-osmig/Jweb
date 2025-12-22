package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class AboutPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style().maxWidth(px(700)).margin(zero, auto).padding(rem(4), SP_8).done(),
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("About JWeb")),

            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("The Origin")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb was born from a simple thought: what if building web apps could be " +
                     "easier with Java? What if I didn't need to write HTML, CSS, and JavaScript " +
                     "separately from my Java code?")),

            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Why JWeb?")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb is a Java web framework that provides a fluent DSL for building web " +
                     "applications entirely in Java. No need to write separate HTML files, CSS " +
                     "stylesheets, or JavaScript scripts.")),

            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Built for Simplicity")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb is built for developers who want simplicity. Components, styles, routes, " +
                     "and logic all live together in Java. Your IDE gives you autocomplete, " +
                     "refactoring, and compile-time checks across your entire application.")),

            h2(attrs().style().fontSize(TEXT_2XL).fontWeight(600).color(TEXT).marginTop(SP_8).done(),
                text("Powered by Spring Boot")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.8).done(),
                text("JWeb integrates seamlessly with the Java ecosystem. Built on Spring Boot, " +
                     "it leverages a great ecosystem that Java developers already know and trust."))
        );
    }
}
