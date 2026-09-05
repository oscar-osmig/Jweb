package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.DevServer;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;

/**
 * Main layout wrapper with nav and footer.
 */
public class Layout implements Template {
    private final String title;
    private final Element content;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        // A Template is an Element, so components drop straight in as children.
        return html(
            new Head(title),
            // The body shell (fixed height, flex column) lives in Head's global
            // stylesheet so the dvh fallback can apply; main is the one scroll
            // container, so any page taller than a phone screen just scrolls.
            body(
                new Nav(),
                main(style().flex(1).minHeight(zero).overflowY(auto), content),
                new Footer(),
                DevServer.script() // Hot reload - only active when jweb.dev.hot-reload=true
            )
        );
    }
}
