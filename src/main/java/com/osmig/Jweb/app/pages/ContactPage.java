package com.osmig.Jweb.app.pages;

import jweb.Element;
import com.osmig.Jweb.framework.security.Csrf;
import com.osmig.Jweb.framework.security.CsrfToken;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import com.osmig.Jweb.app.forms.FormComponents;
import static com.osmig.Jweb.app.forms.FormComponents.*;

/**
 * Contact page. The form is a progressive fragment swap: no JavaScript is
 * written here — the JWeb runtime POSTs the form and swaps the returned
 * status fragment into #form-status. Without JS the form still submits
 * natively to the same route.
 */
public class ContactPage implements Template {

    private final CsrfToken csrfToken;

    public ContactPage(CsrfToken csrfToken) {
        this.csrfToken = csrfToken;
    }

    @Override
    public Element render() {
        return div(style().maxWidth(px(500)).margin(zero, auto)
                .padding(clamp(rem(2), vw(8), rem(4)), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT),
                text("Get in Touch")),
            p(style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7),
                text("Have questions, feedback, or ideas? We'd love to hear from you.")),
            form(attrs().id("contact-form")
                    .action("/contact/submit").method("post")          // no-JS fallback
                    .swapForm("/contact/submit", "#form-status")       // progressive swap
                    .style().marginTop(SP_8).display(flex).flexDirection(column).gap(SP_4).done(),
                Csrf.tokenField(csrfToken),
                field("Name", "name", "text", "Your name"),
                field("Email", "email", "email", "you@example.com"),
                textareaField("Message", "message", "How can we help?", 4),
                div(attrs().id("form-status")),
                FormComponents.submitButton("Send Message")
            )
        );
    }
}
