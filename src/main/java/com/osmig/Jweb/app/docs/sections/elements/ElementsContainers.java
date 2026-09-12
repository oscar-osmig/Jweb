package com.osmig.Jweb.app.docs.sections.elements;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsContainers {
    private ElementsContainers() {}

    public static Element render() {
        return section(
            h3Title("Container Elements"),
            para("Structural elements for organizing content."),
            codeBlock("""
Element child1 = div("Child 1");
Element child2 = div("Child 2");
Element child3 = div("Child 3");
Element header = div("Header");
Element content = div("Content");
Element footer = div("Footer");
Element title = div("Title");
Element body = div("Body");
Element sidebar = div("Sidebar");
Element nav = div("Nav");
Element logo = div("Logo");
Element links = div("Links");
Element copyright = div("© 2026");
Element menuItems = div("Menu");

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
            a(href("/"), "Home"),
            a(href("/about"), "About")
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
