package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsFragments {
    private ElementsFragments() {}

    public static Element render() {
        return section(
            h3Title("Fragments"),
            para("Group elements without adding a wrapper element to the DOM."),
            codeBlock("""
// fragment() groups elements without a wrapper
fragment(
    h1("Title"),
    p("First paragraph"),
    p("Second paragraph")
)

// Useful in conditionals
when(showExtra, () -> fragment(
    p("Extra info 1"),
    p("Extra info 2"),
    p("Extra info 3")
))

// Return multiple elements from a method
public Element renderUserInfo(User user) {
    return fragment(
        span(attrs().class_("name"), text(user.getName())),
        span(attrs().class_("email"), text(user.getEmail()))
    );
}"""),

            h3Title("Raw HTML"),
            para("Insert pre-rendered HTML when needed (use carefully)."),
            codeBlock("""
// raw() inserts HTML directly (be careful with user input!)
raw("<svg>...</svg>")

// Safe usage: only with trusted/sanitized content
String sanitizedMarkdown = markdownToHtml(trustedContent);
div(attrs().class_("markdown-body"), raw(sanitizedMarkdown))"""),

            warn("Never use raw() with user-provided content. It can lead to XSS vulnerabilities.")
        );
    }
}
