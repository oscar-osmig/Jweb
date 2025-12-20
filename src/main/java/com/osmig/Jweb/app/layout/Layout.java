package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
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
            body(attrs().style().display(flex).flexDirection(column).minHeight(vh(100)).done(),
                new Nav().render(),
                main(attrs().style().flex(num(1)).done(), content),
                new Footer().render()
            )
        );
    }
}
