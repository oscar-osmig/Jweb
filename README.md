# JWeb Framework

**Version 1.0.0** | **Last Updated: 2026-01-29 23:30 UTC**

A pure Java web framework that lets you build full-stack web applications entirely in Java. No HTML templates, no JSP, no Thymeleaf - just type-safe Java code with compile-time safety and full IDE support.

JWeb brings modern frontend concepts (component model, reactive state, virtual DOM) to server-side Java development, giving you the productivity of React/Vue with the power and safety of Java.

---

## Philosophy

JWeb is built on these core principles:

1. **Pure Java** - Your entire frontend is Java code. No context switching between languages.
2. **Type Safety** - Catch errors at compile time, not runtime. Your IDE becomes your best friend.
3. **Fluent DSL** - Expressive, readable APIs that feel natural to write.
4. **Component-Based** - Build UIs from composable, reusable pieces.
5. **Reactive** - State changes automatically propagate to the UI.
6. **Minimal Dependencies** - Built on Spring Boot, no additional JavaScript toolchain required.
7. **Modular Architecture** - Clean separation with focused files. Single entry points (`El.*` for HTML, `CSS.*` for CSS, `JS.*` for JavaScript) hide complexity while keeping internals maintainable.

---

## Features

- **Type-safe HTML** - 24 modules for building HTML with Java methods, no string templates
- **Type-safe CSS** - 35 modules for CSS properties as methods with unit validation
- **Type-safe JavaScript** - 43 modules for generating JS code from Java with full IDE support
- **Component-based** - Reusable components with `Template` interface and lifecycle hooks
- **Reactive State** - React-like state management with `State<T>`
- **Virtual DOM** - Efficient rendering with VNode diffing
- **Routing & Middleware** - Fluent route definitions with typed path parameters and request pipeline
- **REST API** - Annotation-based REST controllers (`@REST`, `@GET`, `@POST`, etc.)
- **MongoDB Integration** - Fluent DSL for MongoDB operations with auto-connection
- **Security** - JWT auth, session auth, CSRF protection, rate limiting, password hashing
- **Validation** - Fluent API for input validation with `FormValidator`
- **OpenAPI** - Automatic API documentation generation
- **AI Integration** - Spring AI with fluent DSL for OpenAI and local LLMs
- **Developer Experience** - Hot reload, testing utilities, accessibility helpers, 13 Claude agents

---

## Quick Start

```java
import static com.osmig.Jweb.framework.elements.El.*;
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

        // Pages with layout
        app.layout(MainLayout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // API endpoints
        app.get("/api/users", req -> Response.json(userService.findAll()))
           .post("/api/users", req -> {
               User user = req.bodyAs(User.class);
               return Response.json(userService.save(user));
           });
    }
}
```

### Form with Server Submission (JS DSL)

```java
script()
    .withHelpers()
    .add(onSubmit("contact-form")
        .loading("Sending...")
        .post("/api/v1/contact").withFormData()
        .ok(all(showMessage("form-status").success("Sent!"), resetForm("contact-form")))
        .fail(showMessage("form-status").error("Failed.")))
    .build();
```

---

## Documentation

| Topic | Description |
|-------|-------------|
| [HTML DSL](./readme/html-dsl.md) | Type-safe HTML elements, attributes, modern HTML5, popover, forms, conditionals |
| [CSS DSL](./readme/css-dsl.md) | Inline styles, CSS rules, units, colors, animations, media queries, variables |
| [JavaScript DSL](./readme/javascript-dsl.md) | Actions, async/await, fetch, DOM queries, advanced browser APIs |
| [Architecture](./readme/architecture.md) | Components, templates, routing, middleware, request/response, state |
| [Backend](./readme/backend.md) | REST API, MongoDB, security, validation, OpenAPI, AI integration |
| [Configuration](./readme/configuration.md) | Setup, project structure, dev tools, dependencies |

---

## Running the Application

```bash
# Development (with hot reload)
./mvnw spring-boot:run

# Compile only
./mvnw compile

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ClassName

# Production build
./mvnw clean package
java -jar target/Jweb-1.0.0.jar

# With specific profile
java -jar target/Jweb-1.0.0.jar --spring.profiles.active=prod
```

Then open `http://localhost:8085` in your browser.

---

## Requirements

- **Java 21+** (uses modern Java features like records, pattern matching)
- **Maven 3.6+**
- **Spring Boot 4.x**

---

## License

MIT

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Email: the.jweb.team@gmail.com

---

Built with Java by developers who love Java!
