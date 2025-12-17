package com.osmig.Jweb.app.layouts;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.app.Theme.*;

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
                main(attrs().class_("container").style(
                    style()
                        .maxWidth(px(1200))
                        .margin(zero, auto)
                        .padding(SPACE_MD, SPACE_XL)
                        .flexGrow(1)
                ), content),
                new Footer()
            )
        );
    }
}
