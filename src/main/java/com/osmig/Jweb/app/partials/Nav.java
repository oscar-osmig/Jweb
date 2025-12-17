package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Navigation bar partial.
 */
public class Nav implements Template {

    public Nav() {
    }

    public static Nav create() {
        return new Nav();
    }

    @Override
    public Element render() {
        return nav(class_("navbar"),
        a("/", "Home"),
        a("/about", "About"),
        a("/contact", "Contact")
    );
    }
}
