package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsBasics {
    private ElementsBasics() {}

    public static Element render() {
        return section(
            h3Title("Text Elements"),
            para("Create headings, paragraphs, and inline text."),
            codeBlock("""
// Headings h1-h6
h1("Main Title")
h2("Section Title")
h3("Subsection")
h4("Sub-subsection")
h5("Minor heading")
h6("Smallest heading")

// Paragraphs and text
p("A paragraph of text")
span("Inline text")
text("Raw text node")

// Text formatting
strong("Bold/important text")
em("Emphasized/italic text")
small("Fine print")
code("Inline code")
pre("Preformatted text")"""),

            h3Title("Links"),
            para("Create anchor links with href shorthand or full attributes."),
            codeBlock("""
// Simple link (href shorthand)
a("/home", "Home")
a("/about", "About Us")

// Link with attributes
a(attrs().href("/contact").class_("nav-link"), "Contact")

// External link (opens in new tab securely)
a(attrs().href("https://example.com").targetBlank(), "External Site")
// Adds: target="_blank" rel="noopener noreferrer"

// Link with multiple children
a(attrs().href("/profile"),
    img("/avatar.png", "Avatar"),
    span("View Profile")
)"""),

            h3Title("Semantic Structure"),
            para("Use semantic elements for accessible, well-structured pages."),
            codeBlock("""
// Page structure
body(
    header(
        h1("Site Title"),
        nav(
            a("/", "Home"),
            a("/about", "About"),
            a("/contact", "Contact")
        )
    ),
    main(
        article(
            h2("Article Title"),
            p("Article content...")
        ),
        aside(
            h3("Related Links"),
            ul(li(a("/related", "Related Item")))
        )
    ),
    footer(
        p("© 2026 My Company"),
        address(a("mailto:email@example.com", "Contact Us"))
    )
)

// Sectioning
section(
    h2("Features"),
    p("Our product features...")
)

// Search section (HTML5.2)
search(
    form(attrs().action("/search"),
        input(attrs().type("search").name("q")),
        button("Search")
    )
)"""),

            h3Title("Document Elements"),
            para("Build complete HTML documents with head and body."),
            codeBlock("""
html(
    head(
        title("My Page"),
        meta(attrs().charset("UTF-8")),
        meta(attrs().name("viewport").content("width=device-width, initial-scale=1")),
        css("/styles/main.css"),
        icon("/favicon.ico")
    ),
    body(
        h1("Welcome"),
        p("Page content")
    )
)

// Helper methods
css("/styles.css")       // Stylesheet link
icon("/favicon.ico")     // Favicon
script("/app.js")        // External script
inlineScript("console.log('Hi');")  // Inline JS""")
        );
    }
}
