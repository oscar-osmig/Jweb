package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsBuilders {
    private FormsBuilders() {}

    public static Element render() {
        return section(
            h3Title("Form Input Builders"),
            para("JWeb provides type-safe input builders with validation and styling built-in."),
            codeBlock("""
import jweb.Input;

// Fluent input builders
Input.text("username")
Input.text("fullName").placeholder("John Doe")

// Email with validation
Input.email("email").required()

// Password with constraints
Input.password("password")
    .minLength(8)
    .required()

// Number inputs
Input.number("age").min(0).max(120)
Input.number("price").step("0.01")"""),

            h3Title("Labeled Fields"),
            para("The field() helper wraps an input with a label wired to its id."),
            codeBlock("""
import static jweb.El.*;

// Basic text field
field("Full Name", textInput("name"))

// Email field with placeholder
field("Email Address", emailInput("email", "user@example.com"))

// Password field
field("Password", passwordInput("password"))

// Number field
field("Quantity", numberInput("quantity"))"""),

            h3Title("Checkbox and Radio"),
            codeBlock("""
// Single checkbox (Input builder)
Input.checkbox("terms")
Input.checkbox("newsletter").checked()

// Radio group — same name, different values
div(
    label(for_("size-s"), "Small"),
    Input.radio("size", "s").id("size-s"),
    label(for_("size-m"), "Medium"),
    Input.radio("size", "m").id("size-m").checked(),
    label(for_("size-l"), "Large"),
    Input.radio("size", "l").id("size-l")
)"""),

            h3Title("Select Dropdown"),
            codeBlock("""
// Labeled select
field("Country", select(name("country"), id("country"),
    option(value("us"), "United States"),
    option(value("uk"), "United Kingdom"),
    option(value("ca"), "Canada")
))

// With groups
select(name("car"),
    optgroup(attr("label", "Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")
    ),
    optgroup(attr("label", "German Cars"),
        option(value("mercedes"), "Mercedes"),
        option(value("audi"), "Audi")
    )
)

// Multi-select
select(name("skills"), attrs().multiple(),
    each(skillsList, s -> option(value(s), s))
)"""),

            docTip("Input builders automatically generate IDs and wire up labels for accessibility.")
        );
    }
}
