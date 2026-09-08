package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.app.forms.ContactForm;
import jweb.Element;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * Contact page. The whole form is the {@link ContactForm} record: labels,
 * control types, the CSRF hidden field and the per-field error slots all come
 * from it. The submit is a progressive fragment swap — no JavaScript is written
 * here; without JS the form still posts natively to the same route.
 */
public class ContactPage implements Template {

    @Override
    public Element render() {
        return div(container(px(500))
                .padding(clamp(rem(2), vw(8), rem(4)), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT), "Get in Touch"),
            p(style().marginTop(SP_4).color(TEXT_LIGHT).lineHeight(1.7),
                "Have questions, feedback, or ideas? We'd love to hear from you."),
            div(style().marginTop(SP_8),
                form(ContactForm.class)
                    .id("contact-form")
                    .action("/contact/submit")                     // no-JS fallback
                    .swapForm("/contact/submit", "#form-status")   // progressive swap
                    .field("name", f -> f.placeholder("Your name").autocomplete("name"))
                    .field("email", f -> f.placeholder("you@example.com").autocomplete("email"))
                    .field("message", f -> f.placeholder("How can we help?"))
                    .submit("Send Message")),
            div(id("form-status"), style().marginTop(SP_4))
        );
    }
}
