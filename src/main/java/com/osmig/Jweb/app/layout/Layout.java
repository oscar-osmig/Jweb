package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.dev.DevServer;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

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
        return html(
            new Head(title).render(),
            // The body shell (fixed height, flex column) lives in Head's global
            // stylesheet so the dvh fallback can apply; main is the one scroll
            // container, so any page taller than a phone screen just scrolls.
            body(
                new Nav().render(),
                main(style().flex(1).minHeight(num(0)).overflowY(auto), content),
                new Footer().render(),
                DevServer.script() // Hot reload - only active when jweb.dev.hot-reload=true
            )
        );
    }
}
