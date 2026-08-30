package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSActions {
    private JSActions() {}

    public static Element render() {
        return section(
            h3Title("Actions DSL"),
            para("High-level builders for common UI interactions."),

            h3Title("Form Submission"),
            codeBlock("""
// Basic form submit
onSubmit("contact-form")
    .post("/api/contact").withFormData()
    .ok(showMessage("status").text("Sent!"))
    .fail(showMessage("status").text("Error"))

// With loading state
onSubmit("login-form")
    .loading("Signing in...")          // Disable form, show text
    .post("/api/login").withFormData()
    .ok(navigateTo("/dashboard"))
    .fail(showMessage("error").text("Invalid credentials"))

// Reset the form after success
onSubmit("register-form")
    .loading("Creating account...")
    .post("/api/register").withFormData()
    .ok(all(
        resetForm("register-form"),
        showMessage("success").text("Account created!")
    ))"""),

            h3Title("Click Handlers"),
            codeBlock("""
// Simple click
onClick("save-btn")
    .post("/api/save")
    .ok(showMessage("status").text("Saved!"))

// With confirmation
onClick("delete-btn")
    .confirm("Are you sure you want to delete this?")
    .post("/api/delete/" + id)
    .ok(reload())

// Chain actions
onClick("logout-btn")
    .then(all(
        hide("user-menu"),
        navigateTo("/login")
    ))"""),

            h3Title("UI Manipulation"),
            codeBlock("""
// Show/hide elements
show("panel")
hide("loader")
toggle("dropdown")

// Set content
setText("label", "Updated text")
setHtml("container", "<strong>Updated</strong>")
setInnerHtml("container").fromVar("htmlVar")

// CSS classes
addClass("card", "highlighted")
removeClass("card", "disabled")
toggleClass("menu", "open")

// Multiple actions
all(
    hide("loading"),
    show("content"),
    addClass("card", "loaded")
)"""),

            h3Title("Navigation"),
            codeBlock("""
// Navigate to URL
navigateTo("/dashboard")

// Reload page
reload()

// Trigger a file download
download("/files/report.pdf")

// Smooth-scroll to element
query("#section-id").scrollIntoView()"""),

            h3Title("Fetch Requests"),
            codeBlock("""
// GET request (fetch defaults to GET)
fetch("/api/users")
    .ok(setInnerHtml("user-list").fromVar("_data.html"))
    .fail(showMessage("error").text("Failed to load"))

// POST with JSON body
fetch("/api/users")
    .post()
    .json("{\\"name\\":\\"John\\",\\"email\\":\\"john@example.com\\"}")
    .ok(call("addUserToList", "_data.user"))

// POST with form data
fetch("/api/upload")
    .post()
    .formData("upload-form")
    .ok(showMessage("status").text("Uploaded!"))

// With headers
fetch("/api/protected")
    .header("Authorization", "Bearer " + token)
    .ok(setTextFromResponse("data", "value"))"""),

            h3Title("Modals & Dialogs"),
            codeBlock("""
// Show modal
showModal("confirm-dialog")
hideModal("confirm-dialog")

// Confirmation dialog
confirmDialog("modal")
    .message("Delete this item?")
    .danger()
    .onConfirm(fetch("/api/delete/" + id).post().ok(reload()))
    .onCancel(log("Cancelled"))

// Alert modal (modal id + body element ref)
alertModal("modal", "modal-body")
    .success("Operation completed!")

alertModal("modal", "modal-body")
    .error("Something went wrong")"""),

            h3Title("Messages & Toasts"),
            codeBlock("""
// Show message in element
showMessage("status-div")
    .text("Success!")

// From response
showMessage("result")
    .fromResponse("message")

// Styled variants
showMessage("notification")
    .success("Saved!")

showMessage("notification")
    .error("Failed to save")"""),

            h3Title("Conditional Actions"),
            codeBlock("""
// Based on condition
when("isLoggedIn")
    .then(navigateTo("/dashboard"))
    .otherwise(navigateTo("/login"))

// Based on response
whenResponse("success")
    .then(showMessage("status").text("Done!"))
    .otherwise(showMessage("error").text("Failed"))

// Based on variable (builds a JS ternary expression)
String targetUrl = whenVar("status")
    .equals("approved").thenUrl("/success")
    .elseUrl("/pending");

assignVar("nextUrl", targetUrl)""")
        );
    }
}
