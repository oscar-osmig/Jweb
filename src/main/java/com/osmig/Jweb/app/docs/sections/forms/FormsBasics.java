package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsBasics {
    private FormsBasics() {}

    public static Element render() {
        return section(
            h3Title("Basic Form"),
            para("Create forms with action and method attributes."),
            codeBlock("""
form(attrs().action("/submit").method("POST"),
    div(
        label(attrs().for_("name"), text("Name")),
        input(attrs().type("text").id("name").name("name")
            .placeholder("Enter your name").required())
    ),
    div(
        label(attrs().for_("email"), text("Email")),
        input(attrs().type("email").id("email").name("email")
            .placeholder("you@example.com").required())
    ),
    button(attrs().type("submit"), text("Submit"))
)"""),

            h3Title("Form Structure"),
            para("Group form fields with fieldset and legend."),
            codeBlock("""
form(attrs().action("/register").method("POST"),
    fieldset(
        legend("Personal Information"),
        div(
            label(attrs().for_("fname"), text("First Name")),
            input(attrs().type("text").id("fname").name("firstName"))
        ),
        div(
            label(attrs().for_("lname"), text("Last Name")),
            input(attrs().type("text").id("lname").name("lastName"))
        )
    ),
    fieldset(
        legend("Account"),
        div(
            label(attrs().for_("uname"), text("Username")),
            input(attrs().type("text").id("uname").name("username"))
        )
    ),
    button(attrs().type("submit"), text("Register"))
)""")
        );
    }
}
