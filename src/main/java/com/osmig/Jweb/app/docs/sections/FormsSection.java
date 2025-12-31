package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.forms.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsSection {
    private FormsSection() {}

    public static Element render() {
        return section(
            docTitle("Forms"),
            para("JWeb's Form DSL provides Java methods for form elements with HTML5 validation. " +
                 "Handle form events with state or submit to server handlers."),

            docSubtitle("Overview"),
            para("Create forms with the form() element and various input elements. " +
                 "Use attrs() to set attributes like name, type, and validation rules."),
            codeBlock("""
form(attrs().action("/submit").method("POST"),
    input(attrs().type("text").name("name").required()),
    input(attrs().type("email").name("email").required()),
    button(attrs().type("submit"), text("Submit"))
)"""),

            FormsBasics.render(),
            FormsInputs.render(),
            FormsBuilders.render(),
            FormsSelects.render(),
            FormsCheckboxes.render(),
            FormsValidation.render(),
            FormsEvents.render(),
            FormsUpload.render()
        );
    }
}
