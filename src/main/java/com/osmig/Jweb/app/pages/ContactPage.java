package com.osmig.Jweb.app.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;
import com.osmig.Jweb.app.layouts.MainLayout;

/**
 * Contact page template.
 */
public class ContactPage implements Template {

    public ContactPage() {
    }

    public static ContactPage create() {
        return new ContactPage();
    }

    @Override
    public Element render() {
        return new MainLayout("Contact - JWeb",
        div(
            h1("Contact Us"),
            p("Get in touch with the JWeb team."),

            form(action("/contact"), method("post"),
                div(style_("margin-bottom: 1rem;"),
                    label(for_("name"), "Name:"),
                    br(),
                    input(type("text"), name("name"), id("name"), placeholder("Your name"), required())
                ),
                div(style_("margin-bottom: 1rem;"),
                    label(for_("email"), "Email:"),
                    br(),
                    input(type("email"), name("email"), id("email"), placeholder("your@email.com"), required())
                ),
                div(style_("margin-bottom: 1rem;"),
                    label(for_("message"), "Message:"),
                    br(),
                    textarea(name("message"), id("message"), attr("rows", "5"), attr("cols", "40"), placeholder("Your message..."), required())
                ),
                button(type("submit"), "Send Message")
            ),

            p(style_("margin-top: 2rem;"), a("/", "\u2190 Back Home"))
        )
    );
    }
}
