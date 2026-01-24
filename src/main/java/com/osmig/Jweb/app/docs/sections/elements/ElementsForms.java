package com.osmig.Jweb.app.docs.sections.elements;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ElementsForms {
    private ElementsForms() {}

    public static Element render() {
        return section(
            h3Title("Form Elements"),
            para("Build forms with type-safe input elements and attributes."),
            codeBlock("""
// Basic form
form(attrs().action("/submit").method("POST"),
    label(attrs().for_("email"), "Email:"),
    input(attrs()
        .type("email")
        .name("email")
        .id("email")
        .placeholder("you@example.com")
        .required()
    ),
    button(attrs().type("submit"), "Submit")
)"""),

            h3Title("Input Types"),
            para("All HTML5 input types are supported."),
            codeBlock("""
// Text inputs
input(attrs().type("text").name("username"))
input(attrs().type("password").name("pwd"))
input(attrs().type("email").name("email"))
input(attrs().type("tel").name("phone"))
input(attrs().type("url").name("website"))
input(attrs().type("search").name("q"))

// Number inputs
input(attrs().type("number").name("qty").min(1).max(100))
input(attrs().type("range").name("volume").min(0).max(100))

// Date/time inputs
input(attrs().type("date").name("birthday"))
input(attrs().type("time").name("appointment"))
input(attrs().type("datetime-local").name("meeting"))
input(attrs().type("month").name("expiry"))
input(attrs().type("week").name("week"))

// Selection inputs
input(attrs().type("checkbox").name("agree").value("yes"))
input(attrs().type("radio").name("color").value("red"))

// File input
input(attrs().type("file").name("avatar").accept("image/*"))

// Hidden input
input(attrs().type("hidden").name("csrf").value(token))

// Color picker
input(attrs().type("color").name("theme").value("#6366f1"))"""),

            h3Title("Textarea"),
            para("Multi-line text input."),
            codeBlock("""
// Basic textarea
textarea(attrs().name("message").rows(5).cols(40))

// With default content
textarea(attrs().name("bio").placeholder("Tell us about yourself"),
    "Default text here"
)

// With validation
textarea(attrs()
    .name("comment")
    .required()
    .minlength(10)
    .maxlength(500)
)"""),

            h3Title("Select & Options"),
            para("Dropdown selects with options."),
            codeBlock("""
// Basic select
select(attrs().name("country"),
    option("", "Select a country"),
    option("us", "United States"),
    option("uk", "United Kingdom"),
    option("ca", "Canada")
)

// With selected option
select(attrs().name("size"),
    option("sm", "Small"),
    option(attrs().value("md").selected(), "Medium"),
    option("lg", "Large")
)

// Multiple select
select(attrs().name("colors").multiple(),
    option("red", "Red"),
    option("green", "Green"),
    option("blue", "Blue")
)

// Optgroup
select(attrs().name("car"),
    optgroup(attrs().label("Swedish Cars"),
        option("volvo", "Volvo"),
        option("saab", "Saab")
    ),
    optgroup(attrs().label("German Cars"),
        option("mercedes", "Mercedes"),
        option("audi", "Audi")
    )
)"""),

            h3Title("Buttons"),
            para("Different button types for various actions."),
            codeBlock("""
// Submit button (default)
button("Submit")
button(attrs().type("submit"), "Save")

// Reset button
button(attrs().type("reset"), "Clear Form")

// Regular button (for JS actions)
button(attrs().type("button").onclick("handleClick()"), "Click Me")

// Disabled button
button(attrs().disabled(), "Cannot Click")

// Button with icon
button(attrs().class_("btn-icon"),
    svg(...),  // Icon SVG
    span("Download")
)"""),

            h3Title("Form Validation Attributes"),
            para("HTML5 validation with type-safe attributes."),
            codeBlock("""
input(attrs()
    .type("text")
    .name("username")
    .required()                    // Must be filled
    .minlength(3)                  // Minimum 3 characters
    .maxlength(20)                 // Maximum 20 characters
    .pattern("[a-zA-Z0-9]+")       // Regex pattern
    .autocomplete("username")      // Browser autocomplete hint
)

input(attrs()
    .type("email")
    .name("email")
    .required()
    .placeholder("you@example.com")
)

input(attrs()
    .type("number")
    .name("age")
    .min(18)                       // Minimum value
    .max(120)                      // Maximum value
    .step(1)                       // Step increment
)"""),

            h3Title("Complete Form Example"),
            para("A complete registration form with validation."),
            codeBlock("""
form(attrs().action("/register").method("POST").class_("form"),
    // Username field
    div(class_("field"),
        label(attrs().for_("username"), "Username"),
        input(attrs()
            .type("text")
            .id("username")
            .name("username")
            .required()
            .minlength(3)
            .maxlength(20)
            .autocomplete("username")
        )
    ),

    // Email field
    div(class_("field"),
        label(attrs().for_("email"), "Email"),
        input(attrs()
            .type("email")
            .id("email")
            .name("email")
            .required()
            .autocomplete("email")
        )
    ),

    // Password field
    div(class_("field"),
        label(attrs().for_("password"), "Password"),
        input(attrs()
            .type("password")
            .id("password")
            .name("password")
            .required()
            .minlength(8)
            .autocomplete("new-password")
        )
    ),

    // Terms checkbox
    div(class_("field-checkbox"),
        input(attrs()
            .type("checkbox")
            .id("terms")
            .name("terms")
            .required()
        ),
        label(attrs().for_("terms"), "I agree to the terms")
    ),

    // Submit
    button(attrs().type("submit").class_("btn-primary"), "Register")
)"""),

            docTip("Use the for_ attribute on labels to associate them with inputs by id for accessibility.")
        );
    }
}
