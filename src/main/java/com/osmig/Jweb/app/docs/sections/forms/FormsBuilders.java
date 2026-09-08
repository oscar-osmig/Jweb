package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsBuilders {
    private FormsBuilders() {}

    public static Element render() {
        return section(
            before("v3.0.0",
                h3Title("Form Input Builders"),
                para("JWeb provides type-safe input builders with validation and styling built-in."),
                codeBlock("""
import jweb.Input;

// Fluent input builders
Input.text("username")
Input.email("email").required()
Input.password("password").minLength(8).required()

// Labeled fields
field("Full Name", textInput("name"))
field("Email Address", emailInput("email", "user@example.com"))""")),

            since("v3.0.0",
                h3Title("A Form Is a Record"),
                para("form(SomeRecord.class) renders the whole form: one labelled field per "
                     + "record component, the control type read from the Java type, the CSRF "
                     + "hidden field taken from the current request, and a submit button. "
                     + "The same record validates the submission on the server, so the rules "
                     + "are written once."),
                codeBlock("""
public record Contact(
    @Form.Required String name,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Multiline(rows = 4) String message) {}

// Render
form(Contact.class)
    .action("/contact")
    .submit("Send message")"""),

                h3Title("Component Type to Control"),
                para("The Java type picks the control; the hints refine it."),
                codeBlock("""
String                       -> <input type="text">
@Form.Email String           -> <input type="email">   + email validation
@Form.Password String        -> <input type="password">
@Form.Multiline String       -> <textarea>
int, long, Integer, Long     -> <input type="number">
double, float, BigDecimal    -> <input type="number" step="any">
boolean, Boolean             -> <input type="checkbox">
an enum                      -> <select> over its constants
LocalDate / LocalTime        -> <input type="date"> / type="time">
LocalDateTime                -> <input type="datetime-local">
UploadedFile                 -> <input type="file">, form becomes multipart"""),

                h3Title("Overriding a Field"),
                para("field(name, f -> ...) changes how a field is presented — label, "
                     + "placeholder, help text, control type, rows. The rules (required, "
                     + "email, length) stay on the record, so the browser and the server "
                     + "can never disagree about them."),
                codeBlock("""
form(Contact.class)
    .action("/contact")
    .field("email", f -> f.label("Your email")
                          .placeholder("you@example.com")
                          .autocomplete("email"))
    .field("message", f -> f.rows(6).help("Markdown is fine."))
    .submit("Send message")"""),

                h3Title("Binding and Errors"),
                para("Form.bind reads the submitted values, validates them against the same "
                     + "record, and hands back the value or the messages. Passing the result "
                     + "to errors(...) re-renders the form with the user's input, per-field "
                     + "messages, aria-invalid on the controls, and a summary."),
                codeBlock("""
app.post("/contact", req -> {
    Form.Bound<Contact> submitted = Form.bind(Contact.class, req);
    if (!submitted.ok()) {
        return form(Contact.class)
            .action("/contact")
            .errors(submitted)          // messages + the values as typed
            .submit("Send message");
    }
    Contact contact = submitted.value();
    messages.save(contact);
    return p("Thanks!");
});"""),

                h3Title("Progressive Submission"),
                para("swapForm posts over fetch and swaps the returned fragment into a "
                     + "target; the plain action() keeps working with JavaScript off."),
                codeBlock("""
form(Contact.class)
    .id("contact-form")
    .action("/contact/submit")                    // no-JS fallback
    .swapForm("/contact/submit", "#form-status")  // progressive swap
    .submit("Send Message")"""),

                h3Title("Styling"),
                para("The form emits stable class names — jweb-form, jweb-field, jweb-label, "
                     + "jweb-control, jweb-help, jweb-error, jweb-errors, jweb-submit. "
                     + "Form.styles() is a ready-made stylesheet for them; drop it in the head "
                     + "once and override what you like."),
                codeBlock("""
head(
    style(Form.styles()),
    style(stylesheet()
        .rule(".jweb-submit", style().backgroundColor(BRAND))
        .build())
)""")),

            h3Title("Selects Without a Record"),
            para("Outside a record form, a select is elements all the way down."),
            codeBlock("""
select(name("car"), id("car"),
    optgroup(attrs().label("Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")
    ),
    optgroup(attrs().label("German Cars"),
        option(value("mercedes"), "Mercedes"),
        option(value("audi"), "Audi")
    )
)

// Multi-select
select(name("skills"), attrs().multiple(),
    each(skillsList, s -> option(value(s), s))
)"""),

            docTip("The record is the single source of truth: it renders the form, states "
                   + "the rules, and is what you get back from Form.bind.")
        );
    }
}
