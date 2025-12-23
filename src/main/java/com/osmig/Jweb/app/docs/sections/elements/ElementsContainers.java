package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsContainers {
    private ElementsContainers() {}

    public static Element render() {
        return section(
            h3Title("Container Elements"),
            para("Structural elements for organizing content."),
            codeBlock("""
// Basic containers
div(child1, child2, child3)
section(header, content, footer)
article(title, body)
aside(sidebar)

// Semantic structure
header(nav, logo)
main(content)
footer(links, copyright)
nav(menuItems)"""),

            h3Title("Nesting Elements"),
            para("Elements can be nested to any depth."),
            codeBlock("""
div(
    header(
        h1("Welcome"),
        nav(
            a(attrs().href("/"), text("Home")),
            a(attrs().href("/about"), text("About"))
        )
    ),
    main(
        article(
            h2("Article Title"),
            p("Article content here...")
        )
    ),
    footer(p("Copyright 2025"))
)""")
        );
    }
}
