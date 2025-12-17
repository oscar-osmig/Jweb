package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Footer partial.
 */
public class Footer implements Template {

    public Footer() {
    }

    public static Footer create() {
        return new Footer();
    }

    @Override
    public Element render() {
        return footer(class_("footer"),
        p("© 2025 JWeb Framework")
    );
    }
}
