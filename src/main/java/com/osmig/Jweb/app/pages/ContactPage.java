package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.framework.js.Actions.*;
import static com.osmig.Jweb.app.forms.FormComponents.*;

/** Contact page with server-side form submission to MongoDB. */
public class ContactPage implements Template {

    @Override
    public Element render() {
        return div(attrs().style().maxWidth(px(500)).margin(zero, auto).padding(rem(4), SP_8).done(),
            h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                text("Get in Touch")),
            p(attrs().style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7).done(),
                text("Have questions, feedback, or ideas? We'd love to hear from you.")),
            form(attrs().id("contact-form").style()
                    .marginTop(SP_8).display(flex).flexDirection(column).gap(SP_4).done(),
                field("Name", "name", "text", "Your name"),
                field("Email", "email", "email", "you@example.com"),
                textareaField("Message", "message", "How can we help?", 4),
                statusBox("form-status"),
                submitButton("Send Message")
            ),
            inlineScript(ContactScripts.formHandler())
        );
    }
}

/** JavaScript handlers for Contact page using server POST. */
class ContactScripts {
    private ContactScripts() {}

    static String formHandler() {
        return script()
            .withHelpers()
            .add(onSubmit("contact-form")
                .loading("Sending...")
                .post("/api/v1/contact").withFormData()
                .ok(all(
                    showMessage("form-status").success("Message sent successfully!"),
                    resetForm("contact-form")))
                .fail(showMessage("form-status").error("Failed to send. Please try again.")))
            .build();
    }
}
