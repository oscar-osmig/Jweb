package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsCheckboxes {
    private FormsCheckboxes() {}

    public static Element render() {
        return section(
            h3Title("Checkboxes"),
            para("Single and multiple checkboxes."),
            codeBlock("""
// Single checkbox
div(
    input(type("checkbox"), id("agree"), name("agree"), required()),
    label(for_("agree"), "I agree to the terms")
)

// Checkbox group
fieldset(
    legend("Select interests"),
    div(
        input(type("checkbox"), id("tech"), name("interests"), value("tech")),
        label(for_("tech"), "Technology")
    ),
    div(
        input(type("checkbox"), id("sports"), name("interests"), value("sports")),
        label(for_("sports"), "Sports")
    ),
    div(
        input(type("checkbox"), id("music"), name("interests"), value("music")),
        label(for_("music"), "Music")
    )
)"""),

            h3Title("Radio Buttons"),
            codeBlock("""
fieldset(
    legend("Select plan"),
    div(
        input(type("radio"), id("free"), name("plan"), value("free")),
        label(for_("free"), "Free")
    ),
    div(
        input(type("radio"), id("pro"), name("plan"), value("pro"), checked()),
        label(for_("pro"), "Pro - $9/month")
    ),
    div(
        input(type("radio"), id("enterprise"), name("plan"), value("enterprise")),
        label(for_("enterprise"), "Enterprise - $99/month")
    )
)""")
        );
    }
}
