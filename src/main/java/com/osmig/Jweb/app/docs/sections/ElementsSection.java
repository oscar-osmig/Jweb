package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import com.osmig.Jweb.app.docs.sections.elements.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSection {
    private ElementsSection() {}

    public static Element render() {
        return section(
            docTitle("HTML DSL"),
            para("JWeb's HTML DSL provides Java methods for all HTML elements. " +
                 "Elements are composable, nestable, and fully type-checked at compile time."),

            docSubtitle("Import Statement"),
            codeBlock("""
import static jweb.El.*;"""),
            para("This single import gives you access to all HTML elements, attributes, and helpers."),

            docSubtitle("Basic Pattern"),
            para("Every HTML element has a corresponding Java method. Pass children as arguments to create nested structures."),
            codeBlock("""
// Simple element with text
h1("Hello World")

// Nested elements
div(
    h1("Title"),
    p("Content here")
)

// With attributes
div(id("main"), class_("container"),
    h1("Title"),
    p("Content")
)

// With inline styles (bare style() builder)
div(class_("card"),
    style()
        .padding(rem(1))
        .backgroundColor(white),
    p("Styled content")
)"""),

            ElementsBasics.render(),
            ElementsContainers.render(),
            ElementsLists.render(),
            ElementsTables.render(),
            ElementsMedia.render(),
            ElementsForms.render(),
            ElementsModern.render(),
            ElementsEnhanced.render(),
            ElementsSVG.render(),
            ElementsAttributes.render(),
            ElementsConditionals.render(),
            ElementsFragments.render()
        );
    }
}
