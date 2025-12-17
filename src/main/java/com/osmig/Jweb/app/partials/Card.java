package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Reusable card component.
 */
public class Card implements Template {

    private final String title;
    private final String description;

    public Card(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static Card create(String title, String description) {
        return new Card(title, description);
    }

    @Override
    public Element render() {
        return div(attrs().class_("card").style(
                style()
                    .backgroundColor(BG)
                    .borderRadius(RADIUS_MD)
                    .padding(SPACE_LG)
                    .marginBottom(SPACE_MD)
                    .boxShadow(px(0), px(2), px(8), SHADOW)
            ),
            h3(attrs().style(
                style()
                    .color(PRIMARY)
                    .marginBottom(SPACE_SM)
                    .fontSize(FONT_XL)
            ), text(title)),
            p(attrs().style(
                style()
                    .color(TEXT_LIGHT)
                    .lineHeight(1.6)
            ), text(description))
        );
    }
}
