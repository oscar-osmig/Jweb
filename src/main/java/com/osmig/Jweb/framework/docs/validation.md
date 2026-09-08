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

ValidationResult result = FormValidator.create()
    .field("email", request.formParam("email"))
        .required()
        .email()
    .field("password", request.formParam("password"))
        .required()
        .minLength(8)
    .validate();

if (result.hasErrors()) {
    // Handle validation errors
    return Response.badRequest(result.getAllErrors());
}
```

## Built-in Validators

### Presence

```java
.required()      // Field must have a value
.notNull()       // Field must not be null
.optional()      // Skip validation if empty
```

### String Length

```java
.minLength(8)              // Minimum 8 characters
.maxLength(100)            // Maximum 100 characters
.lengthBetween(8, 100)     // Between 8 and 100 characters
```

### Format

```java
.email()         // Valid email format
.url()           // Valid URL format
.phone()         // Valid phone number
```

### Character Types

```java
.numeric()       // Only digits
.alpha()         // Only letters
.alphanumeric()  // Letters and digits only
```

### Custom Patterns

```java
.pattern("^[A-Z]{2}\\d{4}$", "Must be 2 letters followed by 4 digits")
```

### Numeric Ranges

```java
.min(0)          // Minimum value
.max(100)        // Maximum value
.range(1, 10)    // Between 1 and 10
.positive()      // Greater than 0
.negative()      // Less than 0
```

## Validation Result

```java
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

```java
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
            .custom(val -> val.equals(req.formParam("password")),
                "Passwords must match")
        .field("age", req.formParam("age"))
            .optional()
            .numeric()
            .min(18)
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
.custom(value -> isUnique(value), "Username already taken")
```

### Reusable Validators

```java
Validator<String> usernameValidator = Validator.of(
    value -> value.matches("^[a-z][a-z0-9_]+$"),
    "Username must start with a letter and contain only lowercase letters, numbers, and underscores"
);

FormValidator.create()
    .field("username", req.formParam("username"))
        .apply(usernameValidator)
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
import com.osmig.Jweb.framework.error.ValidationException;   // no short name yet

app.post("/api/users", req -> {
    ValidationResult result = FormValidator.create()
        .field("email", req.formParam("email"), f -> f.required().email())
        .validate();

    if (result.hasErrors()) throw new ValidationException(result);

    // Process valid data...
});
```

For an HTML form, prefer `Form.bind(...)` and re-render with
`form(X.class).errors(bound)` — the user sees the messages next to the fields
instead of an error page.

## JSON API Validation

```java
app.post("/api/users", req -> {
    User user = req.bodyAs(User.class);

    ValidationResult result = FormValidator.create()
        .field("email", user.getEmail())
            .required()
            .email()
        .field("name", user.getName())
            .required()
            .minLength(2)
        .validate();

    if (result.hasErrors()) {
        return Response.json(HttpStatus.BAD_REQUEST, Map.of(
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
