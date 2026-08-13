package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/**
 * Page footer with glassmorphism effect.
 */
public class Footer implements Template {

    @Override
    public Element render() {
        return footer(style()
                .position(sticky).bottom(zero).zIndex(1000)
                .backgroundColor(rgba(255, 255, 255, 0.3))
                .backdropFilter(blur(px(10)))
                .borderTop(px(1), solid, rgba(255, 255, 255, 0.3))
                .padding(SP_4, GUTTER)
                .textAlign(center),
            p(style().color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("© 2025 - Built with JWeb Framework"))
        );
    }
}
