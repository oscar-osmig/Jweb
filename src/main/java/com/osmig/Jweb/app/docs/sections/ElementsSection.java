package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.elements.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsSection {
    private ElementsSection() {}

    public static Element render() {
        return section(
            docTitle("Elements"),
            para("JWeb's HTML DSL provides Java methods for all HTML elements. " +
                 "Elements are composable, nestable, and fully checked at compile time."),

            docSubtitle("Overview"),
            para("Every HTML element has a corresponding Java method. " +
                 "Pass child elements as arguments to create nested structures."),
            codeBlock("""
// Basic pattern: element(children...)
div(h1("Title"), p("Content"))

// With attributes: element(attrs()..., children...)
div(attrs().id("main").class_("container"),
    h1("Title"),
    p("Content")
)"""),

            ElementsBasics.render(),
            ElementsContainers.render(),
            ElementsLists.render(),
            ElementsTables.render(),
            ElementsMedia.render(),
            ElementsFragments.render()
        );
    }
}
