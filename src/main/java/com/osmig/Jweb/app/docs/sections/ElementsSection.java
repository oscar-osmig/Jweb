package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSection {
    private ElementsSection() {}

    public static Element render() {
        return section(
            title("Elements"),
            text("All HTML elements are available as static methods. Pass children as arguments " +
                 "and attributes using the attrs() builder."),

            subtitle("Basic Elements"),
            code("""
h1("Hello World")           // <h1>Hello World</h1>
p("Some text")              // <p>Some text</p>
div(h1("Title"), p("Text")) // Nested elements
span(text("inline"))        // Inline element"""),

            subtitle("With Attributes"),
            code("""
div(attrs().id("main").class_("container"), content)
a(attrs().href("/about"), text("About Us"))
img(attrs().src("/logo.png").alt("Logo"))
input(attrs().type("email").placeholder("you@example.com"))"""),

            subtitle("Attributes Builder"),
            code("""
attrs()
    .id("card")
    .class_("card featured")
    .data("id", "123")
    .aria("label", "Card")
    .style()
        .padding(rem(1))
        .backgroundColor(white)
    .done()"""),

            subtitle("Element Categories"),
            list(
                "Structure: html, head, body, div, span, section, article",
                "Text: h1-h6, p, strong, em, code, pre, blockquote",
                "Lists: ul, ol, li, dl, dt, dd",
                "Tables: table, thead, tbody, tr, th, td",
                "Forms: form, input, textarea, select, option, button, label",
                "Media: img, video, audio, canvas, svg, iframe"
            ),

            subtitle("Special Elements"),
            code("""
text("plain text")              // Text node
raw("<b>raw html</b>")          // Unescaped HTML
fragment(el1, el2)              // Group without wrapper
each(list, item -> li(item))    // Iterate a list
when(cond, () -> span("show"))  // Conditional render""")
        );
    }
}
