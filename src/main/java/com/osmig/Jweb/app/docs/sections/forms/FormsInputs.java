package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsInputs {
    private FormsInputs() {}

    public static Element render() {
        return section(
            h3Title("Input Types"),
            para("All HTML5 input types are supported."),
            codeBlock("""
// Text inputs
input(attrs().type("text").name("name"))
input(attrs().type("email").name("email"))
input(attrs().type("password").name("password"))
input(attrs().type("tel").name("phone"))
input(attrs().type("url").name("website"))
input(attrs().type("search").name("query"))

// Number inputs
input(attrs().type("number").name("age").min("0").max("120"))
input(attrs().type("range").name("volume").min("0").max("100"))

// Date/time inputs
input(attrs().type("date").name("birthdate"))
input(attrs().type("time").name("appointment"))
input(attrs().type("datetime-local").name("meeting"))

// Other inputs
input(attrs().type("color").name("favorite"))
input(attrs().type("file").name("avatar").accept("image/*"))
input(attrs().type("hidden").name("userId").value("123"))"""),

            h3Title("Textarea"),
            codeBlock("""
textarea(attrs()
    .name("bio")
    .rows(4)
    .cols(50)
    .placeholder("Tell us about yourself...")
    .maxlength("500"))""")
        );
    }
}
