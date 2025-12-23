package com.osmig.Jweb.app.docs.sections.components;

import com.osmig.Jweb.framework.core.Element;
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
        return div(attrs().class_("panel"),
            div(attrs().class_("panel-header"), h3(title)),
            div(attrs().class_("panel-body"), fragment(children))
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
        return div(attrs().class_("dialog"),
            div(attrs().class_("dialog-header"), header),
            div(attrs().class_("dialog-body"), body),
            div(attrs().class_("dialog-footer"), footer)
        );
    }
}

// Usage
new Dialog(
    h2("Confirm Delete"),
    p("Are you sure you want to delete this item?"),
    fragment(
        button("Cancel"),
        button(attrs().class_("btn-danger"), text("Delete"))
    )
)""")
        );
    }
}
