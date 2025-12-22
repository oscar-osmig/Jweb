package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsSection {
    private FormsSection() {}

    public static Element render() {
        return section(
            title("Forms"),
            text("Build forms and validate user input."),

            subtitle("Basic Form"),
            code("""
                form(attrs().action("/submit").method("POST"),
                    label(for_("email"), "Email"),
                    input(attrs()
                        .type("email")
                        .id("email")
                        .name("email")
                        .placeholder("you@example.com")
                        .required()),
                    button(type("submit"), "Submit")
                )"""),

            subtitle("Form Elements"),
            code("""
                input(attrs().type("text").name("username"))
                input(attrs().type("password").name("password"))
                input(attrs().type("checkbox").name("agree").checked())
                textarea(attrs().name("bio").placeholder("About you"))
                select(attrs().name("country"),
                    option("us", "United States"),
                    option("uk", "United Kingdom")
                )"""),

            subtitle("Event Handlers"),
            code("""
                form(attrs().onSubmit(e -> {
                    e.preventDefault();
                    handleSubmit();
                }))

                input(attrs()
                    .onInput(e -> searchTerm.set(e.value()))
                    .onChange(e -> validate(e.value())))"""),

            subtitle("Validation"),
            code("""
                ValidationResult result = FormValidator.create()
                    .field("email", email)
                        .required()
                        .email()
                    .field("password", password)
                        .required()
                        .minLength(8)
                    .validate();

                if (result.hasErrors()) {
                    return showErrors(result.getErrors());
                }""")
        );
    }
}
