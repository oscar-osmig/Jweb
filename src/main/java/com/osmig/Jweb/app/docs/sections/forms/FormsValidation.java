package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsValidation {
    private FormsValidation() {}

    public static Element render() {
        return section(
            h3Title("HTML5 Validation"),
            para("Use built-in validation attributes."),
            codeBlock("""
// Required field
input(type("text"), name("name"), required())

// Email validation
input(type("email"), name("email"), required())

// Min/max length
input(type("text"), name("username"),
    attrs().minlength(3).maxlength(20))

// Number range
input(type("number"), name("age"),
    attrs().min("18").max("100"))

// Pattern validation
input(type("text"), name("phone"),
    attrs().pattern("[0-9]{3}-[0-9]{3}-[0-9]{4}")
        .title("Format: 123-456-7890"))"""),

            h3Title("Custom Error Messages"),
            codeBlock("""
input(type("email"), name("email"), required(),
    attrs().title("Please enter a valid email address"))

// Custom validation message via JavaScript
input(type("text"), name("username"),
    attrs().pattern("[a-z0-9_]+"),
    data("error", "Username can only contain lowercase letters, numbers, and underscores"))"""),

            h3Title("Server-Side Validation"),
            codeBlock("""
app.post("/register", req -> {
    String email = req.formParam("email");
    String password = req.formParam("password");

    List<String> errors = new ArrayList<>();

    if (email == null || !email.contains("@")) {
        errors.add("Invalid email address");
    }
    if (password == null || password.length() < 8) {
        errors.add("Password must be at least 8 characters");
    }

    if (!errors.isEmpty()) {
        return registerForm(errors);
    }

    userService.register(email, password);
    return Response.redirect("/login");
});""")
        );
    }
}
