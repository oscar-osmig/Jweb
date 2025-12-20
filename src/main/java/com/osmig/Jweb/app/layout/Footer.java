package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.auto;

/**
 * Page footer.
 */
public class Footer implements Template {

    @Override
    public Element render() {
        return footer(attrs().style()
                .backgroundColor(BORDER).padding(SP_4, SP_8)
                .textAlign(center).marginTop(auto)
            .done(),
            p(attrs().style().color(TEXT_LIGHT).fontSize(TEXT_SM).done(),
                text("© 2025 JWeb Framework"))
        );
    }
}
