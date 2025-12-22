package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSection {
    private ElementsSection() {}

    public static Element render() {
        return section(
            title("Elements"),
            text("All HTML elements are available as static methods."),

            subtitle("Basic Usage"),
            code("""
                // Simple elements
                h1("Hello World")
                p("Some text")
                div(h1("Title"), p("Content"))

                // With attributes
                div(id("main"), class_("container"), "Content")
                a(href("/about"), "About Us")
                img(src("/logo.png"), alt("Logo"))"""),

            subtitle("Attributes Builder"),
            code("""
                div(attrs()
                    .id("card")
                    .class_("card featured")
                    .data("id", "123"),
                    h2("Card Title")
                )

                input(attrs()
                    .type("email")
                    .name("email")
                    .placeholder("you@example.com")
                    .required())"""),

            subtitle("Available Elements"),
            code("""
                // Structure: html, head, body, div, span
                // Text: h1-h6, p, strong, em, code, pre
                // Lists: ul, ol, li
                // Tables: table, thead, tbody, tr, th, td
                // Forms: form, input, textarea, select, button
                // Media: img, video, audio, canvas, svg""")
        );
    }
}
