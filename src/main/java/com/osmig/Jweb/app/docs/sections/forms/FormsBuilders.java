package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsBuilders {
    private FormsBuilders() {}

    public static Element render() {
        return section(
            h3Title("Form Input Builders"),
            para("JWeb provides type-safe input builders with validation and styling built-in."),
            codeBlock("""
import static com.osmig.Jweb.framework.elements.Elements.*;

// Text inputs with labels
textInput("username", "Username")
textInput("fullName", "Full Name").placeholder("John Doe")

// Email with validation
emailInput("email", "Email Address").required()

// Password with constraints
passwordInput("password", "Password")
    .minLength(8)
    .required()

// Number inputs
numberInput("age", "Age").min(0).max(120)
numberInput("price", "Price").step(0.01)"""),

            h3Title("Complete Field Builder"),
            para("The field() builder creates a full form field with label, input, and error container."),
            codeBlock("""
// Basic text field
field("name").label("Full Name").text().required()

// Email field with placeholder
field("email")
    .label("Email Address")
    .email()
    .placeholder("user@example.com")
    .required()

// Password field
field("password")
    .label("Password")
    .password()
    .minLength(8)
    .required()

// Number with range
field("quantity")
    .label("Quantity")
    .number()
    .min(1)
    .max(100)
    .value(1)"""),

            h3Title("Checkbox and Radio"),
            codeBlock("""
// Single checkbox
checkbox("terms", "I agree to the terms")
checkbox("newsletter", "Subscribe to newsletter").checked()

// Radio group
radioGroup("gender",
    radio("male", "Male"),
    radio("female", "Female"),
    radio("other", "Other")
)

// Inline radio
radioGroup("size",
    radio("s", "Small"),
    radio("m", "Medium"),
    radio("l", "Large")
).inline()"""),

            h3Title("Select Dropdown"),
            codeBlock("""
// Simple select
selectField("country", "Country",
    option("us", "United States"),
    option("uk", "United Kingdom"),
    option("ca", "Canada")
)

// With groups
selectField("car", "Choose a car",
    optgroup("Swedish Cars",
        option("volvo", "Volvo"),
        option("saab", "Saab")
    ),
    optgroup("German Cars",
        option("mercedes", "Mercedes"),
        option("audi", "Audi")
    )
)

// Multi-select
selectField("skills", "Skills").multiple()
    .options(skillsList)"""),

            docTip("Input builders automatically generate IDs and wire up labels for accessibility.")
        );
    }
}
