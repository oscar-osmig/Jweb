# JWeb Framework

**Version 1.0.0**

A pure Java web framework with type-safe DSLs for HTML, CSS, and JavaScript. Write your entire frontend in Java with compile-time safety and IDE support.

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Routing with Middleware](#routing-with-middleware)
- [DSL Syntax](#dsl-syntax)
  - [HTML DSL](#html-dsl)
  - [CSS DSL](#css-dsl)
  - [JavaScript DSL](#javascript-dsl)
- [Middleware](#middleware)
- [Validation](#validation)
- [CSRF Protection](#csrf-protection)
- [Authentication](#authentication)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [JSON Utilities](#json-utilities)
- [Response Utilities](#response-utilities)
- [Cookie Management](#cookie-management)
- [Logging](#logging)
- [Caching](#caching)
- [Accessibility Validation](#accessibility-validation)
- [Components](#components)
- [Project Structure](#project-structure)
- [Running the Application](#running-the-application)
- [Database Integration](#database-integration)
- [Requirements](#requirements)
- [License](#license)

## Features

- **Type-safe HTML** - Build HTML with Java methods, no string templates
- **Type-safe CSS** - CSS properties as methods with unit validation
- **Type-safe JavaScript** - Generate JS code from Java with full IDE support
- **Component-based** - Create reusable components with the `Template` interface
- **Middleware System** - Request processing pipeline with built-in middleware
- **Validation Framework** - Fluent API for input validation
- **CSRF Protection** - Built-in protection against cross-site request forgery
- **Authentication** - Session-based auth with role-based access control
- **Error Handling** - Structured exceptions with HTML/JSON responses
- **Testing Utilities** - Mock requests and assertions for testing routes
- **Response Utilities** - Fluent API for HTML, JSON, and error responses
- **Cookie Management** - Secure cookie builder with SameSite support
- **Logging** - SLF4J-based logging utilities
- **Caching Middleware** - Cache-Control, ETag, and static asset caching
- **Accessibility Validation** - WCAG 2.1 compliance checking
- **No build tools** - Just Maven and Java, no webpack/npm required

## Quick Start

```java
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

public class HelloWorld implements Template {
    @Override
    public Element render() {
        return div()
            .id("app")
            .class_("container")
            .children(
                h1().text("Hello, JWeb!"),
                p().text("Built with pure Java")
            );
    }
}
```

## Routing with Middleware

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Add middleware
        app.use(Middlewares.logging())
           .use(Middlewares.cors())
           .use(Middlewares.csrf())
           .use("/admin", Auth.requireRole("admin"));

        // Define routes
        app.get("/", HomePage::new)
           .get("/about", AboutPage::new)
           .get("/users/:id", req -> userPage(req.param("id")))
           .post("/login", this::handleLogin);
    }
}
```

## DSL Syntax

### HTML DSL

Create HTML elements with a fluent builder pattern:

```java
// Basic elements
div().id("main").class_("container").text("Hello")

// Nested children
div()
    .id("card")
    .children(
        h1().text("Title"),
        p().text("Content"),
        button()
            .onclick("handleClick()")
            .text("Click me")
    )

// Attributes
input()
    .type("text")
    .name("username")
    .placeholder("Enter name")
    .required()

// Conditional rendering
div()
    .when(isAdmin, () -> button().text("Delete"))
    .ifElse(isLoggedIn,
        () -> span().text("Welcome!"),
        () -> a().href("/login").text("Sign In"))

// Loop over items
ul().each(users, user -> li().text(user.getName()))
```

### CSS DSL

Write CSS with type-safe properties and units:

```java
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

// Single rule
String css = rule(".btn")
    .padding(px(10), px(20))
    .backgroundColor(blue)
    .color(white)
    .borderRadius(px(4))
    .build();

// Multiple rules
String css = styles(
    rule("*")
        .boxSizing(borderBox),
    rule("body")
        .margin(zero)
        .fontFamily("system-ui"),
    rule(".container")
        .maxWidth(px(1200))
        .margin(zero, auto),
    rule("a")
        .color(blue)
        .textDecoration(none),
    rule("a:hover")
        .textDecoration(underline)
);
```

**Available units:**
- `px(10)` - pixels
- `rem(1.5)` - root em
- `em(1)` - em
- `percent(100)` - percentage
- `vh(100)`, `vw(100)` - viewport units
- `ms(200)`, `s(1)` - time units
- `zero` - 0

**Available colors:**
- Named: `red`, `blue`, `green`, `white`, `black`, `gray`, `transparent`
- Hex: `hex("#ff5733")`
- RGB: `rgb(255, 87, 51)`
- RGBA: `rgba(255, 87, 51, 0.5)`

### JavaScript DSL

Generate JavaScript from Java with full type safety:

```java
import static com.osmig.Jweb.framework.js.JS.*;

// Define functions
Func increment = func("increment")
    .inc("count")
    .set(el("display").text(), variable("count"));

Func reset = func("reset")
    .set("count", 0)
    .call("updateDisplay");

// Build script
String js = script()
    .var_("count", 0)
    .add(increment)
    .add(reset)
    .build();
```

## Middleware

Built-in middleware for common tasks:

```java
app.use(Middlewares.logging())           // Request logging
   .use(Middlewares.cors())              // CORS headers
   .use(Middlewares.csrf())              // CSRF protection
   .use(Middlewares.rateLimit(100, 60000)) // Rate limiting
   .use(Middlewares.securityHeaders())   // Security headers
   .use(Middlewares.timing())            // Response timing
   .use(Middlewares.requestId());        // Request ID tracking
```

Custom middleware:

```java
Middleware timing = (req, chain) -> {
    long start = System.currentTimeMillis();
    Object result = chain.next();
    System.out.println("Request took " + (System.currentTimeMillis() - start) + "ms");
    return result;
};

app.use(timing);
```

## Validation

Fluent validation for form data:

```java
ValidationResult result = FormValidator.create()
    .field("email", request.formParam("email"))
        .required()
        .email()
    .field("password", request.formParam("password"))
        .required()
        .minLength(8)
        .maxLength(100)
    .field("age", request.formParam("age"))
        .optional()
        .numeric()
    .validate();

if (result.hasErrors()) {
    result.getErrors("email").forEach(System.out::println);
    throw new ValidationException(result);
}
```

Built-in validators:
- `required()`, `notNull()`, `optional()`
- `minLength(n)`, `maxLength(n)`, `lengthBetween(min, max)`
- `email()`, `url()`, `phone()`
- `numeric()`, `alpha()`, `alphanumeric()`
- `pattern(regex, message)`
- `min(n)`, `max(n)`, `range(min, max)`, `positive()`, `negative()`

## CSRF Protection

Automatic CSRF protection for forms:

```java
// In your form
form()
    .method("post")
    .action("/submit")
    .children(
        Csrf.tokenField(request),  // Hidden CSRF token field
        input().type("text").name("message"),
        button("Submit")
    )

// In your POST handler
app.post("/submit", req -> {
    Csrf.validate(req);  // Throws CsrfException if invalid
    // Process form...
});
```

## Authentication

Session-based authentication with role support:

```java
// Login
Principal user = Principal.of("user123", "john@example.com", "user", "admin");
Auth.login(request, user);

// Check authentication
if (Auth.isAuthenticated(request)) {
    Principal user = Auth.getPrincipal(request);
    if (user.hasRole("admin")) {
        // Admin access
    }
}

// Logout
Auth.logout(request);

// Middleware for protected routes
app.use("/dashboard", Auth.requireAuth())
   .use("/admin", Auth.requireRole("admin"))
   .use("/api", Auth.requireAnyRole("user", "admin"));
```

Bearer token authentication:

```java
app.use(Auth.bearerAuth(token -> {
    // Validate token and return Principal
    return tokenService.validate(token);
}));
```

## Error Handling

Structured exceptions with automatic responses:

```java
// Throw errors in handlers
throw JWebException.notFound("User not found");
throw JWebException.badRequest("Invalid input");
throw JWebException.unauthorized("Login required");
throw JWebException.forbidden("Access denied");

// Use error handling middleware
app.use(ErrorHandler.errorHandling());

// Custom error pages
app.use(ErrorHandler.errorHandling(ex ->
    div(
        h1(ex.getStatus() + " Error"),
        p(ex.getMessage())
    )
));
```

## Testing

Test utilities for routes and handlers:

```java
// Create mock requests
Request req = MockRequest.get("/users")
    .queryParam("page", "1")
    .header("Accept", "application/json")
    .build();

// Test routes
JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/"));
assert result.isSuccess();
assert result.bodyContains("Hello");

// Test handlers directly
JWebTest.TestResult result = JWebTest.testHandler(handler, MockRequest.post("/api/users").json("{\"name\":\"John\"}"));
assert result.getStatus() == 201;

// HTML assertions
String html = element.toHtml();
JWebTest.assertContains(html, "Welcome");
JWebTest.assertHasClass(html, "container");
JWebTest.assertHasId(html, "main");
JWebTest.assertHasTag(html, "form");
```

## JSON Utilities

Built-in JSON support via Jackson:

```java
import com.osmig.Jweb.framework.util.Json;

// Serialize
String json = Json.stringify(myObject);
String pretty = Json.stringifyPretty(myObject);

// Parse
User user = Json.parse(jsonString, User.class);
Map<String, Object> map = Json.parseMap(jsonString);
List<Object> list = Json.parseList(jsonString);

// Build JSON
String json = Json.object()
    .put("name", "John")
    .put("age", 30)
    .toString();

// Safe parsing
Optional<User> user = Json.tryParse(jsonString, User.class);
```

## Response Utilities

Fluent API for building HTTP responses:

```java
import com.osmig.Jweb.framework.server.Response;

// HTML responses
return Response.html(div(h1("Hello")));
return Response.html("<h1>Hello</h1>");

// JSON responses
return Response.json(user);
return Response.json(Map.of("status", "ok"));
return Response.json()
    .put("name", "John")
    .put("age", 30)
    .build();

// Redirects
return Response.redirect("/dashboard");
return Response.redirect("/home", true);  // permanent
return Response.seeOther("/success");     // after POST

// Error responses
return Response.notFound();
return Response.badRequest("Invalid email");
return Response.unauthorized();
return Response.forbidden();
return Response.error(500, "Server error");

// Custom responses
return Response.ok()
    .header("X-Custom", "value")
    .contentType(MediaType.APPLICATION_PDF)
    .body(pdfBytes);
```

## Cookie Management

Secure cookie handling with fluent builder:

```java
import com.osmig.Jweb.framework.server.Cookie;

// Simple cookie
Cookie.of("theme", "dark").addTo(response);

// Secure session cookie
Cookie.of("sessionId", "abc123")
    .httpOnly()
    .secure()
    .sameSiteStrict()
    .addTo(response);

// Persistent cookie
Cookie.of("rememberMe", "true")
    .maxAge(Duration.ofDays(30))
    .httpOnly()
    .addTo(response);

// Delete cookie
Cookie.delete("theme").addTo(response);

// Reading cookies
String theme = request.cookie("theme");
Map<String, String> all = request.cookies();
```

## Logging

SLF4J-based logging for the framework:

```java
import com.osmig.Jweb.framework.util.Log;

// Simple logging
Log.info("Server started");
Log.debug("Processing request: {}", path);
Log.warn("Deprecated API used");
Log.error("Failed to process", exception);

// Class-specific loggers
private static final Logger log = Log.forClass(MyClass.class);
log.info("Message");

// Level checks
if (Log.isDebugEnabled()) {
    Log.debug("Expensive debug info: {}", computeDebugInfo());
}
```

## Caching

Built-in middleware for cache control:

```java
// Cache for 1 hour
app.use("/api", Middlewares.cacheControl(3600));

// Private cache with revalidation
app.use("/user", Middlewares.cacheControl(3600, true, true));

// Disable caching
app.use("/admin", Middlewares.noCache());

// Static asset caching (1 year, immutable)
app.use("/assets", Middlewares.staticCache());

// ETag support
app.use(Middlewares.etag());

// Compression headers
app.use(Middlewares.compressionHeaders());
```

## Accessibility Validation

Check your HTML for WCAG 2.1 compliance:

```java
import com.osmig.Jweb.framework.accessibility.A11y;

// Validate an Element
A11y.ValidationResult result = A11y.validate(page);
if (!result.isValid()) {
    result.getIssues().forEach(issue ->
        System.out.println(issue.getSeverity() + ": " + issue.getMessage())
    );
}

// Quick checks
boolean ok = A11y.hasImageAlt(html);
boolean ok = A11y.hasFormLabels(html);
boolean ok = A11y.hasButtonText(html);
boolean ok = A11y.hasHeadingHierarchy(html);
boolean ok = A11y.hasLinkText(html);

// Filter by severity
List<A11y.Issue> errors = result.getIssues(A11y.Severity.ERROR);
long errorCount = result.getErrorCount();
```

Checks include:
- Missing image alt text
- Form inputs without labels
- Buttons without accessible names
- Heading hierarchy issues
- Generic link text ("click here")
- Missing language attribute
- Table headers
- ARIA misuse
- Focus management issues

## Components

Create reusable components by implementing `Template`:

```java
public class Card implements Template {
    private final String title;
    private final String content;

    public Card(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return div()
            .class_("card")
            .children(
                h2().text(title),
                p().text(content)
            );
    }
}

// Usage
new Card("Welcome", "Hello world!").render()
```

## Project Structure

```
src/main/java/com/osmig/Jweb/
├── app/
│   ├── pages/          # Page components
│   ├── partials/       # Reusable UI components
│   │   └── demo/       # Demo components
│   ├── Routes.java     # Route definitions
│   └── Theme.java      # Design tokens
└── framework/
    ├── core/           # Core interfaces (Element, Renderable)
    ├── elements/       # HTML element builders (Tag, Elements)
    ├── styles/         # CSS DSL (Style, CSS, CSSUnits, CSSColors)
    ├── js/             # JavaScript DSL (JS)
    ├── vdom/           # Virtual DOM implementation
    ├── server/         # HTTP server (JWebController)
    ├── routing/        # Router and route handling
    ├── middleware/     # Middleware system
    ├── validation/     # Validation framework
    ├── security/       # CSRF and authentication
    ├── error/          # Error handling
    ├── testing/        # Test utilities
    ├── util/           # JSON and other utilities
    └── websocket/      # WebSocket support
```

## Running the Application

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080` in your browser.

## Database Integration

JWeb works seamlessly with Spring Data JPA, JdbcTemplate, or any Spring-compatible database library.

See [docs/database-integration.md](docs/database-integration.md) for a complete guide including:
- Spring Data JPA setup
- JdbcTemplate examples
- Integration with JWeb routes
- Connection pooling configuration
- Complete CRUD example

Quick example:

```java
@Component
public class TaskApp {
    private final JWeb app;
    private final TaskRepository taskRepo;

    public TaskApp(JWeb app, TaskRepository taskRepo) {
        this.app = app;
        this.taskRepo = taskRepo;

        app.get("/tasks", req -> {
            var tasks = taskRepo.findAll();
            return ul(tasks.stream()
                .map(t -> li(t.getTitle()))
                .toArray(Element[]::new));
        });
    }
}
```

## Requirements

- Java 21+
- Maven 3.6+
- Spring Boot 4.0

## License

MIT
