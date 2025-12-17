package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Footer partial with copyright.
 */
public class Footer implements Template {

    public Footer() {}

    public static Footer create() {
        return new Footer();
    }

    @Override
    public Element render() {
        return footer(attrs().class_("footer").style()
                .backgroundColor(BG_LIGHT)
                .padding(SPACE_MD, SPACE_XL)
                .marginTop(auto)
                .borderTop(px(1), solid, BORDER_LIGHT)
                .textAlign(center)
            .done(),
            p(attrs().style()
                .color(TEXT_MUTED)
                .fontSize(FONT_SM)
            .done(), text("\u00A9 2025 JWeb Framework"))
        );
    }
}
