package com.osmig.Jweb.app.docs.sections.components;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class CompChildren {
    private CompChildren() {}

    public static Element render() {
        return section(
            h3Title("Components with Children"),
            para("Accept child elements via constructor."),
            codeBlock("""
public class Panel implements Template {
    private final String title;
    private final Element[] children;

    public Panel(String title, Element... children) {
        this.title = title;
        this.children = children;
    }

    public Element render() {
        return div(class_("panel"),
            div(class_("panel-header"), h3(title)),
            div(class_("panel-body"), fragment(children))
        );
    }
}

// Usage
new Panel("User Info",
    p("Name: John Doe"),
    p("Email: john@example.com"),
    button("Edit Profile")
)"""),

            h3Title("Slot Pattern"),
            para("Named slots for complex layouts."),
            codeBlock("""
public class Dialog implements Template {
    private final Element header;
    private final Element body;
    private final Element footer;

    public Dialog(Element header, Element body, Element footer) {
        this.header = header;
        this.body = body;
        this.footer = footer;
    }

    public Element render() {
        return div(class_("dialog"),
            div(class_("dialog-header"), header),
            div(class_("dialog-body"), body),
            div(class_("dialog-footer"), footer)
        );
    }
}

// Usage
new Dialog(
    h2("Confirm Delete"),
    p("Are you sure you want to delete this item?"),
    fragment(
        button("Cancel"),
        button(class_("btn-danger"), "Delete")
    )
)""")
        );
    }
}
