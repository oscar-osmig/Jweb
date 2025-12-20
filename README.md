# JWeb Framework

**Version 1.0.0**

A pure Java web framework with type-safe DSLs for HTML, CSS, and JavaScript. Write your entire frontend in Java with compile-time safety and IDE support.

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Running the Application](#running-the-application)
- [Requirements](#requirements)
- [License](#license)

## Features

- **Type-safe HTML** - Build HTML with Java methods, no string templates
- **Type-safe CSS** - CSS properties as methods with unit validation
- **Type-safe JavaScript** - Generate JS code from Java with full IDE support
- **Component-based** - Create reusable components with the `Template` interface
- **Routing System** - Fluent route definitions with path parameters
- **Middleware System** - Request processing pipeline with built-in middleware
- **HTTP Client** - Fluent `Fetch` API for consuming external APIs
- **Validation Framework** - Fluent API for input validation
- **Security** - CSRF protection, session-based auth, role-based access control
- **State Management** - React-like reactive state with `State<T>`
- **Real-time** - Server-Sent Events and WebSocket support
- **Background Jobs** - Async task execution with progress tracking
- **File Uploads** - Easy multipart file handling with validation
- **Internationalization** - i18n support with locale detection
- **Email** - Fluent email builder with attachments
- **Testing Utilities** - Mock requests and assertions for testing routes
- **Accessibility** - WCAG 2.1 compliance checking
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
        return div(class_("container"),
            h1("Hello, JWeb!"),
            p("Built with pure Java")
        );
    }
}
```

### Define Routes

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Middleware
        app.use(Middlewares.logging())
           .use(Middlewares.cors());

        // Pages
        app.get("/", req -> new HomePage())
           .get("/users/:id", req -> new UserPage(req.param("id")));

        // API endpoints
        app.get("/api/users", req -> Response.json(userService.findAll()))
           .post("/api/users", req -> {
               User user = req.bodyAs(User.class);
               return Response.json(userService.save(user));
           });
    }
}
```

### Consume APIs

```java
// Fetch data from external APIs
FetchResult result = Fetch.get("https://api.example.com/users")
    .bearer("token123")
    .send();

List<User> users = result.asList(User.class);
```

## Documentation

Detailed documentation for each feature:

| Feature | Description |
|---------|-------------|
| [Routing](src/main/java/com/osmig/Jweb/framework/docs/routing.md) | Route definitions, path parameters, handlers |
| [Elements](src/main/java/com/osmig/Jweb/framework/docs/elements.md) | HTML DSL, Tag builder, attributes |
| [CSS](src/main/java/com/osmig/Jweb/framework/docs/css.md) | CSS DSL, units, colors, media queries |
| [Middleware](src/main/java/com/osmig/Jweb/framework/docs/middleware.md) | Request pipeline, built-in middleware |
| [Validation](src/main/java/com/osmig/Jweb/framework/docs/validation.md) | Form validation, built-in validators |
| [Security](src/main/java/com/osmig/Jweb/framework/docs/security.md) | CSRF, authentication, authorization |
| [HTTP Client](src/main/java/com/osmig/Jweb/framework/docs/http.md) | Fetch API for consuming APIs |
| [State](src/main/java/com/osmig/Jweb/framework/docs/state.md) | Reactive state management |
| [SSE](src/main/java/com/osmig/Jweb/framework/docs/sse.md) | Server-Sent Events for real-time |
| [Background Jobs](src/main/java/com/osmig/Jweb/framework/docs/jobs.md) | Async tasks and scheduling |
| [File Upload](src/main/java/com/osmig/Jweb/framework/docs/file-upload.md) | Multipart file handling |
| [i18n](src/main/java/com/osmig/Jweb/framework/docs/i18n.md) | Internationalization |
| [Email](src/main/java/com/osmig/Jweb/framework/docs/email.md) | Email sending |
| [Testing](src/main/java/com/osmig/Jweb/framework/docs/testing.md) | Test utilities and mocks |
| [Database](src/main/java/com/osmig/Jweb/framework/docs/database-integration.md) | Database integration |

## Project Structure

```
src/main/java/com/osmig/Jweb/
├── app/                    # Your application code
│   ├── pages/              # Page components
│   ├── partials/           # Reusable UI components
│   ├── Routes.java         # Route definitions
│   └── App.java            # Application entry point
└── framework/              # JWeb framework
    ├── core/               # Element, Renderable interfaces
    ├── elements/           # HTML elements (Tag, Elements)
    ├── styles/             # CSS DSL
    ├── js/                 # JavaScript DSL
    ├── routing/            # Router and routes
    ├── middleware/         # Middleware system
    ├── server/             # Request, Response, Controller
    ├── http/               # Fetch HTTP client
    ├── validation/         # Validation framework
    ├── security/           # Auth, CSRF
    ├── state/              # State management
    ├── sse/                # Server-Sent Events
    ├── websocket/          # WebSocket support
    ├── async/              # Background jobs
    ├── upload/             # File uploads
    ├── i18n/               # Internationalization
    ├── email/              # Email sending
    ├── testing/            # Test utilities
    ├── accessibility/      # A11y validation
    ├── error/              # Error handling
    ├── util/               # JSON, logging utilities
    └── docs/               # Documentation
```

## Running the Application

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080` in your browser.

## Requirements

- Java 21+
- Maven 3.6+
- Spring Boot 4.0

## License

MIT
