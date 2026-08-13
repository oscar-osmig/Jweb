package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;

public class AboutPage implements Template {

    @Override
    public Element render() {
        return div(style().maxWidth(px(700)).margin(zero, auto).padding(rem(2), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).marginBottom(rem(1.5)),
                text("About JWeb")),

            p(style().color(TEXT_LIGHT).lineHeight(1.7).marginBottom(rem(1)),
                text("JWeb is a Java web framework that provides a fluent DSL for building web " +
                     "applications entirely in Java. No separate HTML, CSS, or JavaScript files needed.")),

            p(style().color(TEXT_LIGHT).lineHeight(1.7).marginBottom(rem(1)),
                text("Built for developers who want simplicity. Components, styles, routes, and logic " +
                     "all live together in Java with full IDE support for autocomplete and refactoring.")),

            p(style().color(TEXT_LIGHT).lineHeight(1.7),
                text("Powered by Spring Boot, JWeb integrates seamlessly with the Java ecosystem " +
                     "that developers already know and trust."))
        );
    }
}
