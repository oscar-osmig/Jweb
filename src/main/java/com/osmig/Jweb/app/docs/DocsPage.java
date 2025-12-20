package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Documentation page with sidebar and content.
 */
public class DocsPage implements Template {
    private final String section;

    public DocsPage(String section) {
        this.section = section != null ? section : "intro";
    }

    @Override
    public Element render() {
        return div(attrs().style().display(flex).minHeight(vh(80)).done(),
            new DocSidebar(section).render(),
            main(attrs().style()
                .prop("flex", "1").padding(SP_8).maxWidth(px(800))
            .done(),
                DocContent.get(section)
            )
        );
    }
}
