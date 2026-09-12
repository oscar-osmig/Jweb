# Validation

JWeb provides a fluent validation API for validating form data and request inputs.

For a whole HTML form, prefer the record-based form system
(`jweb.Form`, `@Form.Required`, `Form.bind()` — see
[templates.md](./templates.md)), which validates and binds in one step. Use
`FormValidator` directly for one-off fields, JSON bodies, or rules that don't
map to a single record.

## Basic Usage

```java
import jweb.FormValidator;
import jweb.ValidationResult;
import jweb.Validator;
import jweb.Validators;

Object registerHandler(Request req) {
    ValidationResult result = FormValidator.create()
        .field("email", req.formParam("email"))
            .required()
            .email()
        .field("password", req.formParam("password"))
            .required()
            .minLength(8)
        .validate();

    if (result.hasErrors()) {
        // Handle validation errors
        return Response.json(400, result.getAllErrors());
    }

    return Response.redirect("/welcome");
}
```

## Built-in Validators

### Presence

```java
String value = "example";

FormValidator.create().field("x", value)
    .required();       // Field must have a value

Validators.notNull();  // Field must not be null — a Validator<String>, pair with .custom(...)

FormValidator.create().field("x", value)
    .optional();        // Skip validation if empty
```

### String Length

```java
String value = "example";

FormValidator.create().field("x", value)
    .minLength(8)              // Minimum 8 characters
    .maxLength(100)            // Maximum 100 characters
    .lengthBetween(8, 100);    // Between 8 and 100 characters
```

### Format

```java
String value = "example";

FormValidator.create().field("x", value)
    .email()         // Valid email format
    .url()           // Valid URL format
    .phone();        // Valid phone number
```

### Character Types

```java
String value = "example";

FormValidator.create().field("x", value)
    .numeric()       // Only digits
    .alpha()         // Only letters
    .alphanumeric(); // Letters and digits only
```

### Custom Patterns

```java
String value = "example";

FormValidator.create().field("x", value)
    .pattern("^[A-Z]{2}\\d{4}$", "Must be 2 letters followed by 4 digits");
```

### Numeric Ranges

```java
// FormValidator's field chain validates Strings and has no numeric-range
// methods; min/max/range/positive/negative are Validator<Number> factories
// on Validators instead — compose them with .and(...) (see "Composing
// Validators" below) or hand one to .custom(...) once a field's value is
// parsed to a number.
Validators.min(0);          // Minimum value
Validators.max(100);        // Maximum value
Validators.range(1, 10);    // Between 1 and 10
Validators.positive();      // Greater than 0
Validators.negative();      // Less than 0
```

## Validation Result

```java
// var here — the lambda-configured field(...) overload currently returns
// FormValidator's internal supertype, not the jweb.FormValidator create() gave you
var validator = FormValidator.create()
    .field("email", req.formParam("email"), f -> f.required().email());
ValidationResult result = validator.validate();

// Check if valid
if (result.isValid()) {
    // Proceed
}

// Check for errors
if (result.hasErrors()) {
    // Get errors for specific field
    List<String> emailErrors = result.getErrors("email");

    // Get all errors
    Map<String, List<String>> allErrors = result.getAllErrors();

    // Get first error for a field
    String firstError = result.getFirstError("email");
}
```

## Form Validation Example

record RegisterPage(Map<String, List<String>> errors) implements Template {
    public Element render() { return div("Register"); }
}

app.post("/register", req -> {
    ValidationResult result = FormValidator.create()
        .field("username", req.formParam("username"))
            .required()
            .minLength(3)
            .maxLength(20)
            .alphanumeric()
        .field("email", req.formParam("email"))
            .required()
            .email()
        .field("password", req.formParam("password"))
            .required()
            .minLength(8)
        .field("confirmPassword", req.formParam("confirmPassword"))
            .required()
            .check(val -> val.equals(req.formParam("password")),
                "Passwords must match")
        .field("age", req.formParam("age"))
            .optional()
            .numeric()
            .check(val -> Integer.parseInt(val) >= 18, "Must be 18 or older")
        .validate();

    if (result.hasErrors()) {
        return new RegisterPage(result.getAllErrors());
    }

    // Create user...
    return Response.redirect("/welcome");
});
```

## Custom Validators

### Inline Custom Validation

```java
boolean isUnique(String value) { return true; }

// custom(...) takes a Validator<String> directly; a predicate + message goes
// through check(...) instead
FormValidator.create().field("username", req.formParam("username"))
    .check(value -> isUnique(value), "Username already taken");
```

### Reusable Validators

```java
Validator<String> usernameValidator = Validator.of(
    value -> value.matches("^[a-z][a-z0-9_]+$"),
    "Username must start with a letter and contain only lowercase letters, numbers, and underscores"
);

FormValidator.create()
    .field("username", req.formParam("username"))
        .custom(usernameValidator)
```

### Composing Validators

```java
Validator<String> strongPassword = Validators.required()
    .and(Validators.minLength(8))
    .and(Validator.of(
        s -> s.matches(".*[A-Z].*"),
        "Must contain uppercase letter"
    ))
    .and(Validator.of(
        s -> s.matches(".*[0-9].*"),
        "Must contain a number"
    ));
```

## Failing fast with an exception

For an API route, throwing carries the messages to the error middleware, which
turns them into a 422 with the field errors in the body:

```java
import jweb.ValidationException;

app.post("/api/users", req -> {
    ValidationResult result = FormValidator.create()
        .field("email", req.formParam("email"), f -> f.required().email())
        .validate();

    if (result.hasErrors()) throw new ValidationException(result);

    // Process valid data...
    return Response.json(Map.of("status", "created"));
});
```

For an HTML form, prefer `Form.bind(...)` and re-render with
`form(X.class).errors(bound)` — the user sees the messages next to the fields
instead of an error page.

## JSON API Validation

```java
record User(String email, String name) {}
class UserService { User save(User u) { return u; } }
UserService userService = new UserService();
com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();

// Request has no typed bodyAs(...) yet, so parse the raw body yourself
app.post("/api/users", req -> {
    User user;
    try {
        user = json.readValue(req.body(), User.class);
    } catch (Exception e) {
        return Response.error(400, "Invalid JSON");
    }

    ValidationResult result = FormValidator.create()
        .field("email", user.email())
            .required()
            .email()
        .field("name", user.name())
            .required()
            .minLength(2)
        .validate();

    if (result.hasErrors()) {
        return Response.json(400, Map.of(
            "error", "Validation failed",
            "fields", result.getAllErrors()
        ));
    }

    return Response.json(userService.save(user));
});
```

## Displaying Errors in Templates

A record form renders its own messages — pass the binding result and every field
gets its message, `aria-invalid`, a summary, and the value the user typed:

```java
public record Register(@Form.Required @Form.Email String email) {}

Form.Bound<Register> bound = Form.bind(Register.class, req);
form(Register.class).action("/register").errors(bound).submit("Register")
```

Hand-built markup reads the `ValidationResult` itself:

```java
public class RegisterPage implements Template {
    private final ValidationResult errors;

    public RegisterPage(ValidationResult errors) {
        this.errors = errors != null ? errors : ValidationResult.valid();
    }

    @Override
    public Element render() {
        return form(method("post"), action("/register"),
            div(cls("field"),
                label(for_("email"), "Email"),
                input(type("email"), name("email"), id("email"),
                    attrs().aria("invalid", errors.hasErrors("email") ? "true" : "false")),
                when(errors.hasErrors("email"),
                    span(cls("error"), errors.getFirstError("email")))
            ),
            button(type("submit"), "Register")
        );
    }
}
```
