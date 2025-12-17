package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Reusable card component with icon support and animation.
 */
public class Card implements Template {

    private final String title;
    private final String description;
    private final String icon;
    private final String animationClass;

    public Card(String title, String description) {
        this(title, description, null, null);
    }

    public Card(String title, String description, String icon) {
        this(title, description, icon, null);
    }

    public Card(String title, String description, String icon, String animationClass) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.animationClass = animationClass;
    }

    public static Card create(String title, String description) {
        return new Card(title, description);
    }

    public static Card create(String title, String description, String icon) {
        return new Card(title, description, icon);
    }

    public static Card withAnimation(String title, String description, String icon, String animationClass) {
        return new Card(title, description, icon, animationClass);
    }

    @Override
    public Element render() {
        String classes = "card" + (animationClass != null ? " " + animationClass : "");

        return div(attrs().class_(classes).style(
                style()
                    .backgroundColor(BG)
                    .borderRadius(RADIUS_LG)
                    .padding(SPACE_LG)
                    .border(px(1), solid, BORDER_LIGHT)
                    .boxShadow(px(0), px(4), px(6), px(-1), SHADOW)
                    .position(relative)
                    .overflow(hidden)
            ),
            // Gradient accent at top
            div(attrs().style(
                style()
                    .position(absolute)
                    .top(zero)
                    .left(zero)
                    .right(zero)
                    .height(px(3))
                    .background(linearGradient("90deg", PRIMARY, SECONDARY))
            )),
            // Icon if provided
            icon != null ? div(attrs().class_("feature-icon").style(
                style()
                    .fontSize(rem(2))
                    .marginBottom(SPACE_MD)
                    .display(inlineBlock)
            ), text(icon)) : null,
            // Title
            h3(attrs().style(
                style()
                    .color(TEXT)
                    .marginBottom(SPACE_SM)
                    .fontSize(FONT_XL)
                    .fontWeight(600)
            ), text(title)),
            // Description
            p(attrs().style(
                style()
                    .color(TEXT_LIGHT)
                    .lineHeight(1.7)
                    .fontSize(FONT_SM)
            ), text(description))
        );
    }
}
