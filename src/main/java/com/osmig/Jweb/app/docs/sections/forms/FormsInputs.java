package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsInputs {
    private FormsInputs() {}

    public static Element render() {
        return section(
            h3Title("Input Types"),
            para("All HTML5 input types are supported."),
            codeBlock("""
// Text inputs
input(type("text"), name("name"))
input(type("email"), name("email"))
input(type("password"), name("password"))
input(type("tel"), name("phone"))
input(type("url"), name("website"))
input(type("search"), name("query"))

// Number inputs
input(type("number"), name("age"), attrs().min("0").max("120"))
input(type("range"), name("volume"), attrs().min("0").max("100"))

// Date/time inputs
input(type("date"), name("birthdate"))
input(type("time"), name("appointment"))
input(type("datetime-local"), name("meeting"))

// Other inputs
input(type("color"), name("favorite"))
input(type("file"), name("avatar"), attrs().accept("image/*"))
input(type("hidden"), name("userId"), value("123"))"""),

            h3Title("Textarea"),
            codeBlock("""
textarea(name("bio"), placeholder("Tell us about yourself..."),
    attrs().rows(4).cols(50).maxlength(500))""")
        );
    }
}
