package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

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
                "© 2025 - Built with JWeb Framework")
        );
    }
}
