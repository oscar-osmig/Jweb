package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * Contact page.
 */
public class ContactPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style().maxWidth(px(500)).margin(zero, auto).padding(rem(4), SP_8).done(),
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Get in Touch")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Have questions, feedback, or ideas? We'd love to hear from you.")),
            form(attrs().style().marginTop(SP_8).display(flex).flexDirection(column).gap(SP_4).done(),
                field("Name", "name", "text", "Your name"),
                field("Email", "email", "email", "you@example.com"),
                textareaField("Message", "message", "How can we help?"),
                button(attrs().type("submit").style()
                    .padding(SP_3, SP_6).backgroundColor(PRIMARY).color(white)
                    .prop("border", "none").borderRadius(ROUNDED).fontWeight(600)
                    .cursor(pointer).fontSize(TEXT_BASE)
                .done(), text("Send Message"))
            )
        );
    }

    private Element field(String label, String name, String type, String placeholder) {
        return div(
            label(attrs().for_(name).style().fontSize(TEXT_SM).fontWeight(500).color(TEXT).done(),
                text(label)),
            input(attrs().type(type).name(name).id(name).placeholder(placeholder).style()
                .marginTop(SP_1).width(percent(100)).padding(SP_3)
                .prop("border", "1px solid #e2e8f0").borderRadius(ROUNDED)
                .fontSize(TEXT_BASE)
            .done())
        );
    }

    private Element textareaField(String label, String name, String placeholder) {
        return div(
            label(attrs().for_(name).style().fontSize(TEXT_SM).fontWeight(500).color(TEXT).done(),
                text(label)),
            textarea(attrs().name(name).id(name).placeholder(placeholder)
                .set("rows", "4").style()
                .marginTop(SP_1).width(percent(100)).padding(SP_3)
                .prop("border", "1px solid #e2e8f0").borderRadius(ROUNDED)
                .fontSize(TEXT_BASE).prop("resize", "vertical")
            .done())
        );
    }
}
