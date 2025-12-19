package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.app.layouts.MainLayout;

/**
 * Contact page template with styled form and contact info.
 */
public class ContactPage implements Template {

    public ContactPage() {}

    public static ContactPage create() {
        return new ContactPage();
    }

    @Override
    public Element render() {
        return new MainLayout("Contact - JWeb",
            div(attrs().class_("fade-in").style()
                    .display(flex)
                    .justifyContent(center)
                    .alignItems(flexStart)
                    .gap(SPACE_2XL)
                    .flexWrap(wrap)
                .done(),
                // Left side - Info
                div(attrs().style()
                        .flex(1, 1, px(300))
                        .maxWidth(px(380))
                    .done(),
                    // Header
                    span(attrs().style()
                        .display(inlineBlock)
                        .padding(SPACE_XS, SPACE_MD)
                        .fontSize(FONT_SM)
                        .fontWeight(500)
                        .borderRadius(RADIUS_FULL)
                        .backgroundColor(rgba(102, 126, 234, 0.1))
                        .color(PRIMARY)
                        .marginBottom(SPACE_MD)
                            .marginTop(px(325))
                    .done(), text("Get in Touch")),
                    h1(attrs().class_("gradient-text").style()
                        .fontSize(rem(2))
                        .fontWeight(700)
                        .marginBottom(SPACE_SM)
                    .done(), text("Contact Us")),
                    p(attrs().style()
                        .color(TEXT_LIGHT)
                        .fontSize(FONT_BASE)
                        .marginBottom(SPACE_XL)
                        .lineHeight(1.7)
                    .done(), text("Have a question, suggestion, or just want to say hello? We'd love to hear from you!")),
                    // Back link
                    a(attrs().href("/").style()
                        .display(inlineBlock)
                        .marginTop(SPACE_XL)
                        .color(PRIMARY)
                        .fontSize(FONT_SM)
                        .fontWeight(500)
                    .done(), text("\u2190 Back to Home"))
                ),
                // Right side - Form
                form(attrs()
                        .action("/contact")
                        .method("post")
                        .class_("scale-in")
                        .style()
                            .flex(1, 1, px(400))
                            .maxWidth(px(480))
                            .marginTop(px(60))
                            .marginLeft(px(70))
                            .backgroundColor(white)
                            .padding(SPACE_XL)
                            .borderRadius(RADIUS_LG)
                            .boxShadow(px(0), px(4), px(20), rgba(0, 0, 0, 0.08))
                            .border(px(1), solid, BORDER_LIGHT)
                        .done(),

                    // Form header
                    h2(attrs().style()
                        .fontSize(FONT_XL)
                        .color(TEXT)
                        .marginBottom(SPACE_LG)
                    .done(), text("Send us a message")),

                    // Name and Email in a row
                    div(attrs().style()
                            .display(flex)
                            .gap(SPACE_MD)
                            .marginBottom(SPACE_MD)
                        .done(),
                        formField("name", "text", "Name", "John Doe"),
                        formField("email", "email", "Email", "john@example.com")
                    ),

                    // Subject field
                    div(attrs().style().marginBottom(SPACE_MD).done(),
                        label(attrs()
                            .for_("subject")
                            .style(labelStyle()),
                            text("Subject")),
                        input(attrs()
                            .type("text")
                            .name("subject")
                            .id("subject")
                            .placeholder("What's this about?")
                            .required()
                            .style(inputStyle()))
                    ),

                    // Message textarea
                    div(attrs().style().marginBottom(SPACE_LG).done(),
                        label(attrs()
                            .for_("message")
                            .style(labelStyle()),
                            text("Message")),
                        textarea(attrs()
                            .name("message")
                            .id("message")
                            .set("rows", "4")
                            .placeholder("Tell us what you need help with...")
                            .required()
                            .style(inputStyle().resize(vertical).minHeight(px(100))))
                    ),

                    // Submit button - full width
                    button(attrs()
                        .type("submit")
                        .class_("btn")
                        .style()
                            .width(percent(100))
                            .backgroundColor(PRIMARY)
                            .color(white)
                            .border(px(0), solid, transparent)
                            .padding(SPACE_MD, SPACE_LG)
                            .fontSize(FONT_BASE)
                            .fontWeight(600)
                            .borderRadius(RADIUS_MD)
                            .cursor(pointer)
                            .transition("all", TRANSITION_FAST, ease)
                            .display(flex)
                            .alignItems(center)
                            .justifyContent(center)
                            .gap(SPACE_SM)
                        .done(),
                        text("Send Message"),
                        span(text(" \u2192"))
                    ),

                    // Privacy note
                    p(attrs().style()
                        .marginTop(SPACE_MD)
                        .fontSize(FONT_SM)
                        .color(TEXT_MUTED)
                        .textAlign(center)
                    .done(), text("We respect your privacy. Your data is never shared."))
                )
            )
        );
    }

    private Element contactInfo(String icon, String label, String value) {
        return div(attrs().style()
                .display(flex)
                .alignItems(center)
                .gap(SPACE_MD)
                .padding(SPACE_MD)
                .backgroundColor(BG_LIGHT)
                .borderRadius(RADIUS_MD)
            .done(),
            span(attrs().style()
                .fontSize(rem(1.25))
            .done(), text(icon)),
            div(
                span(attrs().style()
                    .display(block)
                    .fontSize(FONT_SM)
                    .color(TEXT_MUTED)
                .done(), text(label)),
                span(attrs().style()
                    .display(block)
                    .fontWeight(500)
                    .color(TEXT)
                .done(), text(value))
            )
        );
    }

    private Element formField(String name, String type, String labelText, String placeholder) {
        return div(attrs().style().flex(1, 1, zero).done(),
            label(attrs()
                .for_(name)
                .style(labelStyle()),
                text(labelText)),
            input(attrs()
                .type(type)
                .name(name)
                .id(name)
                .placeholder(placeholder)
                .required()
                .style(inputStyle()))
        );
    }

    private Style labelStyle() {
        return new Style()
            .display(block)
            .marginBottom(SPACE_XS)
            .fontWeight(500)
            .fontSize(FONT_SM)
            .color(TEXT);
    }

    private Style inputStyle() {
        return new Style()
            .width(percent(100))
            .padding(SPACE_SM, SPACE_MD)
            .fontSize(FONT_BASE)
            .border(px(1), solid, BORDER)
            .borderRadius(RADIUS_MD)
            .backgroundColor(white)
            .transition("all", TRANSITION_FAST, ease);
    }
}
