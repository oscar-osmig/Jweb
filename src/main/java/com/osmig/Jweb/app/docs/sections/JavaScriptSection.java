package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.javascript.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JavaScriptSection {
    private JavaScriptSection() {}

    public static Element render() {
        return section(
            docTitle("JavaScript DSL"),
            para("JWeb provides type-safe JavaScript generation. Write JS in Java with full IDE support, " +
                 "compile-time checks, and no context switching."),

            docSubtitle("Import Statements"),
            codeBlock("""
// Core JS DSL
import static com.osmig.Jweb.framework.js.JS.*;

// High-level UI actions
import static com.osmig.Jweb.framework.js.Actions.*;

// Event handling patterns
import static com.osmig.Jweb.framework.js.Events.*;

// Async/await and fetch
import static com.osmig.Jweb.framework.js.Async.*;"""),

            docSubtitle("Two Approaches"),
            para("Use Actions DSL for common UI patterns, or JS DSL for custom logic."),

            h3Title("Actions DSL (High-Level)"),
            para("Declarative builders for common interactions."),
            codeBlock("""
// Form submission with loading state
onSubmit("login-form")
    .loading("Signing in...")
    .post("/api/login").withFormData()
    .ok(navigateTo("/dashboard"))
    .fail(showMessage("error").text("Invalid credentials"))

// Button click with confirmation
onClick("delete-btn")
    .confirm("Delete this item?")
    .post("/api/delete/" + id)
    .ok(reload())

// Input change handler
onChange("search-input")
    .then(fetch("/api/search?q=" + variable("this").dot("value"))
        .get()
        .ok(setHtml("results")))"""),

            h3Title("JS DSL (Low-Level)"),
            para("Full control over generated JavaScript."),
            codeBlock("""
// Variables and values
script()
    .var_("count", 0)
    .let_("name", str("John"))
    .const_("PI", 3.14159)

// Variable references
variable("count")      // count
str("hello")          // 'hello'
array(1, 2, 3)        // [1,2,3]
obj("a", 1, "b", 2)   // {a:1,b:2}

// Functions
func("greet", "name")
    .var_("msg", str("Hello, ").plus(variable("name")))
    .ret(variable("msg"))

// Callbacks (anonymous functions)
callback("item")
    .ret(variable("item").times(2))"""),

            JSCore.render(),
            JSActions.render(),
            JSEvents.render(),
            JSAsync.render(),
            JSBrowserAPIs.render(),
            JSAdvanced.render(),
            JSNewAPIs.render(),

            spacer()
        );
    }
}
