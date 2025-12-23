package com.osmig.Jweb.app.docs.sections.forms;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsValidation {
    private FormsValidation() {}

    public static Element render() {
        return section(
            h3Title("HTML5 Validation"),
            para("Use built-in validation attributes."),
            codeBlock("""
// Required field
input(attrs().type("text").name("name").required())

// Email validation
input(attrs().type("email").name("email").required())

// Min/max length
input(attrs().type("text").name("username")
    .minlength("3").maxlength("20"))

// Number range
input(attrs().type("number").name("age")
    .min("18").max("100"))

// Pattern validation
input(attrs().type("text").name("phone")
    .pattern("[0-9]{3}-[0-9]{3}-[0-9]{4}")
    .title("Format: 123-456-7890"))"""),

            h3Title("Custom Error Messages"),
            codeBlock("""
input(attrs()
    .type("email")
    .name("email")
    .required()
    .title("Please enter a valid email address"))

// Custom validation message via JavaScript
input(attrs()
    .type("text")
    .name("username")
    .pattern("[a-z0-9_]+")
    .data("error", "Username can only contain lowercase letters, numbers, and underscores"))"""),

            h3Title("Server-Side Validation"),
            codeBlock("""
app.post("/register", req -> {
    String email = req.body("email");
    String password = req.body("password");

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
