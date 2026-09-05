package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsBasics {
    private FormsBasics() {}

    public static Element render() {
        return section(
            h3Title("Basic Form"),
            para("Create forms with action and method attributes."),
            codeBlock("""
form(action("/submit"), method("POST"),
    div(
        label(for_("name"), "Name"),
        input(type("text"), id("name"), name("name"),
            placeholder("Enter your name"), required())
    ),
    div(
        label(for_("email"), "Email"),
        input(type("email"), id("email"), name("email"),
            placeholder("you@example.com"), required())
    ),
    button(type("submit"), "Submit")
)"""),

            h3Title("Form Structure"),
            para("Group form fields with fieldset and legend."),
            codeBlock("""
form(action("/register"), method("POST"),
    fieldset(
        legend("Personal Information"),
        div(
            label(for_("fname"), "First Name"),
            input(type("text"), id("fname"), name("firstName"))
        ),
        div(
            label(for_("lname"), "Last Name"),
            input(type("text"), id("lname"), name("lastName"))
        )
    ),
    fieldset(
        legend("Account"),
        div(
            label(for_("uname"), "Username"),
            input(type("text"), id("uname"), name("username"))
        )
    ),
    button(type("submit"), "Register")
)""")
        );
    }
}
