package com.osmig.Jweb.app.layouts;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;
import com.osmig.Jweb.app.partials.Head;
import com.osmig.Jweb.app.partials.Nav;
import com.osmig.Jweb.app.partials.Footer;

/**
 * Main layout template with header, nav, and footer.
 */
public class MainLayout implements Template {

    private final String pageTitle;
    private final Element content;

    public MainLayout(String pageTitle, Element content) {
        this.pageTitle = pageTitle;
        this.content = content;
    }

    public static MainLayout create(String pageTitle, Element content) {
        return new MainLayout(pageTitle, content);
    }

    @Override
    public Element render() {
        return html(
        new Head(pageTitle),
        body(
            new Nav(),
            main(class_("container"), content),
            new Footer()
        )
    );
    }
}
