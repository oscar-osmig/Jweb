package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
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
    input(attrs().type("checkbox").id("agree").name("agree").required()),
    label(attrs().for_("agree"), text("I agree to the terms"))
)

// Checkbox group
fieldset(
    legend("Select interests"),
    div(
        input(attrs().type("checkbox").id("tech").name("interests").value("tech")),
        label(attrs().for_("tech"), text("Technology"))
    ),
    div(
        input(attrs().type("checkbox").id("sports").name("interests").value("sports")),
        label(attrs().for_("sports"), text("Sports"))
    ),
    div(
        input(attrs().type("checkbox").id("music").name("interests").value("music")),
        label(attrs().for_("music"), text("Music"))
    )
)"""),

            h3Title("Radio Buttons"),
            codeBlock("""
fieldset(
    legend("Select plan"),
    div(
        input(attrs().type("radio").id("free").name("plan").value("free")),
        label(attrs().for_("free"), text("Free"))
    ),
    div(
        input(attrs().type("radio").id("pro").name("plan").value("pro").checked()),
        label(attrs().for_("pro"), text("Pro - $9/month"))
    ),
    div(
        input(attrs().type("radio").id("enterprise").name("plan").value("enterprise")),
        label(attrs().for_("enterprise"), text("Enterprise - $99/month"))
    )
)""")
        );
    }
}
