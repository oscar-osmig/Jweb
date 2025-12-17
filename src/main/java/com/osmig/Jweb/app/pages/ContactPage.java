package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.app.layouts.MainLayout;

/**
 * Contact page template with styled form.
 */
public class ContactPage implements Template {

    public ContactPage() {}

    public static ContactPage create() {
        return new ContactPage();
    }

    @Override
    public Element render() {
        return new MainLayout("Contact - JWeb",
            // Center the form horizontally and vertically
            div(attrs().style(
                    style()
                        .display(flex)
                        .justifyContent(center)
                        .alignItems(flexStart)
                        .gap(SPACE_XL)
                ),
                // Left side - Info
                div(attrs().style(
                        style()
                            .flex(1, 1, px(300))
                            .maxWidth(px(350))
                    ),
                    h1(attrs().style(
                        style()
                            .fontSize(FONT_2XL)
                            .color(TEXT)
                            .marginBottom(SPACE_SM)
                    ), text("Contact Us")),
                    p(attrs().style(
                        style()
                            .color(TEXT_LIGHT)
                            .fontSize(FONT_SM)
                            .marginBottom(SPACE_MD)
                    ), text("Have a question or feedback? We'd love to hear from you.")),
                    a(attrs().href("/").style(
                        style()
                            .color(PRIMARY)
                            .fontSize(FONT_SM)
                    ), text("\u2190 Back to Home"))
                ),

                // Right side - Form
                form(attrs()
                        .action("/contact")
                        .method("post")
                        .style(style()
                            .flex(1, 1, px(400))
                            .maxWidth(px(450))
                            .backgroundColor(BG_LIGHT)
                            .padding(SPACE_LG)
                            .borderRadius(RADIUS_MD)
                        ),

                    // Name and Email in a row
                    div(attrs().style(
                            style()
                                .display(flex)
                                .gap(SPACE_MD)
                                .marginBottom(SPACE_MD)
                        ),
                        formField("name", "text", "Name", "Your name"),
                        formField("email", "email", "Email", "you@email.com")
                    ),

                    // Message textarea
                    div(attrs().style(style().marginBottom(SPACE_MD)),
                        label(attrs()
                            .for_("message")
                            .style(labelStyle()),
                            text("Message")),
                        textarea(attrs()
                            .name("message")
                            .id("message")
                            .attr("rows", "3")
                            .placeholder("Your message...")
                            .required()
                            .style(inputStyle().resize(vertical)))
                    ),

                    // Submit button
                    button(attrs()
                        .type("submit")
                        .style(style()
                            .backgroundColor(PRIMARY)
                            .color(white)
                            .border(none)
                            .padding(SPACE_SM, SPACE_LG)
                            .fontSize(FONT_SM)
                            .fontWeight(600)
                            .borderRadius(RADIUS_SM)
                            .cursor(pointer)
                            .transition("background", TRANSITION_FAST, ease)
                        ), text("Send Message"))
                )
            )
        );
    }

    private Element formField(String name, String type, String labelText, String placeholder) {
        return div(attrs().style(style().flex(1, 1, zero)),
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
        return style()
            .display(block)
            .marginBottom(SPACE_XS)
            .fontWeight(500)
            .fontSize(FONT_SM)
            .color(TEXT);
    }

    private Style inputStyle() {
        return style()
            .width(percent(100))
            .padding(SPACE_SM)
            .fontSize(FONT_SM)
            .border(px(1), solid, BORDER)
            .borderRadius(RADIUS_SM)
            .backgroundColor(white)
            .transition("border-color", TRANSITION_FAST, ease);
    }
}
