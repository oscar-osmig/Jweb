package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Footer partial.
 */
public class Footer implements Template {

    public Footer() {}

    public static Footer create() {
        return new Footer();
    }

    @Override
    public Element render() {
        return footer(attrs().class_("footer").style(
                style()
                    .backgroundColor(BG_LIGHT)
                    .padding(SPACE_SM, SPACE_XL)
                    .textAlign(center)
                    .marginTop(auto)
                    .borderTop(px(1), solid, BORDER)
            ),
            p(attrs().style(
                style()
                    .color(TEXT_MUTED)
                    .fontSize(FONT_SM)
            ), text("© 2025 JWeb Framework"))
        );
    }
}
