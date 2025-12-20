# Validation

JWeb provides a fluent validation API for validating form data and request inputs.

## Basic Usage

```java
import com.osmig.Jweb.framework.validation.*;

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

## Validation Exception

Throw validation errors as exceptions:

```java
app.post("/api/users", req -> {
    ValidationResult result = FormValidator.create()
        .field("email", req.formParam("email"))
            .required()
            .email()
        .validate();

    if (result.hasErrors()) {
        throw new ValidationException(result);
    }

    // Process valid data...
});
```

The `ErrorHandler` middleware will convert this to a proper error response.

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

```java
public class RegisterPage implements Template {
    private final Map<String, List<String>> errors;

    public RegisterPage(Map<String, List<String>> errors) {
        this.errors = errors != null ? errors : Map.of();
    }

    @Override
    public Element render() {
        return form(method("post"), action("/register"),
            div(class_("field"),
                label(for_("email"), "Email"),
                input(type("email"), name("email"), id("email")),
                when(errors.containsKey("email"), () ->
                    span(class_("error"), errors.get("email").get(0))
                )
            ),
            button(type("submit"), "Register")
        );
    }
}
```
