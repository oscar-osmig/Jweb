package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsBasics {
    private ElementsBasics() {}

    public static Element render() {
        return section(
            h3Title("Text Elements"),
            para("Create headings, paragraphs, and inline text with type-safe methods."),
            codeBlock("""
// Headings h1-h6
h1("Main Title")
h2("Section Title")
h3("Subsection")

// Paragraphs and text
p("A paragraph of text")
span("Inline text")
text("Raw text node")
strong("Bold text")
em("Italic text")"""),

            h3Title("Creating Text Content"),
            para("Use text() for raw text nodes inside elements."),
            codeBlock("""
// text() creates a TextElement
span(text("Hello"), text(" "), text("World"))

// String shorthand for simple cases
p("Simple paragraph")

// Mix text with other elements
p(text("Click "), a(attrs().href("/here"), text("here")), text(" to continue"))""")
        );
    }
}
