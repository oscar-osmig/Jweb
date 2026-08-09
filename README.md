# JWeb Framework

**Version 1.0.0** | **Last Updated: 2026-08-08**

A pure Java web framework that lets you build full-stack web applications entirely in Java. No HTML templates, no JSP, no Thymeleaf — just type-safe Java code with compile-time safety and full IDE support.

JWeb brings modern frontend concepts (component model, reactive state, virtual DOM) to server-side Java development, giving you the productivity of React/Vue with the power and safety of Java.

---

## Philosophy

JWeb is built on these core principles:

1. **Pure Java** — Your entire frontend is Java code. No context switching between languages.
2. **Type Safety** — Catch errors at compile time, not runtime. Your IDE becomes your best friend.
3. **Fluent DSL** — Expressive, readable APIs that feel natural to write.
4. **Component-Based** — Build UIs from composable, reusable pieces.
5. **Reactive** — State changes propagate to the UI (see [State & Realtime](./readme/state-and-realtime.md) for current wiring status).
6. **Minimal Dependencies** — Built on Spring Boot, no JavaScript toolchain required.
7. **Modular Architecture** — Clean separation with focused files. Facade entry points (`El.*` for HTML, `CSS.*` for CSS, `JS.*`/`Actions.*` for JavaScript) hide complexity while keeping internals maintainable.

---

## Features

- **Type-safe HTML** — 24 element modules; build HTML with Java methods, no string templates
- **Type-safe CSS** — 35 style modules; CSS properties as methods, units, colors, animations, `@media`/`@container`/`@supports`/`@layer`/`@scope`
- **Type-safe JavaScript** — 43 JS modules; generate client-side JS from Java, from form handlers to IndexedDB and WebRTC
- **Component-based** — Reusable components via the `Template` interface
- **Virtual DOM** — `VNode` tree rendering with XSS-safe text escaping by default
- **Routing & Middleware** — Fluent route definitions with `:param` path parameters and a request pipeline
- **REST API** — Annotation-based REST controllers (`@REST`, `@GET`, `@POST`, `@UPDATE`, `@PATCH`, `@DEL`) dispatched by Spring MVC
- **MongoDB Integration** — Fluent DSL (`Mongo`, `Doc`, `MongoQuery`) with Spring-driven auto-connection
- **Security** — JWT auth, session auth (`Auth`/`Principal`), CSRF protection, rate limiting, BCrypt hashing, OAuth2 providers
- **Validation** — Composable `Validator<T>` plus `FormValidator`/`FieldValidator` fluent APIs
- **OpenAPI** — Generates OpenAPI 3.0.3 spec + Swagger UI, Redoc, and Scalar doc pages
- **Realtime primitives** — SSE broadcaster, WebSocket handler, reactive `State<T>` (partially wired — see known issues)
- **Background work** — Virtual-thread `Jobs`, cron `Scheduler`, `Suspense` for async rendering, TTL `Cache`
- **Developer Experience** — Hot reload dev server, testing utilities (`JWebTest`, `MockRequest`, `TestClient`), CLI scaffolding, accessibility auditor

---

## Quick Start

```java
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.El.*;

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
        // Middleware (applies to router-handled routes; see architecture doc for caveats)
        app.use(Middlewares.logging());

        // Pages with a shared layout — call layout() BEFORE pages()
        app.layout(Layout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // Dynamic routes with request access (`:param` syntax)
        app.get("/profile/:id", req -> new ProfilePage(req.param("id")));

        // API-style routes on the JWeb router (note: paths starting with /api/v
        // are reserved for Spring MVC @REST controllers — see architecture doc)
        app.get("/data/users", req -> Response.json(userService.findAll()));
    }
}
```

### Form with Server Submission (JS DSL)

```java
import static com.osmig.Jweb.framework.js.Actions.*;

// withHelpers() is REQUIRED — the generated handlers depend on the $_ helper it defines
String js = script()
    .withHelpers()
    .add(onSubmit("contact-form")
        .loading("Sending...")
        .post("/api/v1/contact").withFormData()
        .ok(all(showMessage("form-status").success("Sent!"), resetForm("contact-form")))
        .fail(showMessage("form-status").error("Failed.")))
    .build();

// Place it in the page:
inlineScript(js)
```

---

## Documentation

| Topic | Description |
|-------|-------------|
| [Architecture](./readme/architecture.md) | Rendering pipeline, request flow, routing systems, middleware, Request/Response APIs |
| [HTML DSL](./readme/html-dsl.md) | Elements, attributes, modern HTML5, popover, forms, conditionals, fluent builders |
| [CSS DSL](./readme/css-dsl.md) | Inline styles, rules, units, colors, animations, at-rules, themes, utilities |
| [JavaScript DSL](./readme/javascript-dsl.md) | Actions, form handlers, fetch, events, and the full 43-module browser API surface |
| [State & Realtime](./readme/state-and-realtime.md) | Reactive state, events, hydration, WebSocket, SSE, transitions, portals, async rendering |
| [Backend](./readme/backend.md) | REST API, OpenAPI, MongoDB, security, validation, forms, uploads, jobs, testing |
| [Configuration](./readme/configuration.md) | Setup, config files, environment variables, dev tools, CLI, project structure |
| [Known Issues](./readme/known-issues.md) | Verified gaps, unwired features, and API pitfalls — read before extending the framework |

Additional internal reference docs ship with the framework source at
`src/main/java/com/osmig/Jweb/framework/docs/` (15 topic files) and are copied into the jar
under `framework-src/`.

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

Then open `http://localhost:8085` in your browser (port is `${PORT:8085}` in `application.yaml`;
note the Dockerfile currently `EXPOSE`s 8080 — set `PORT=8080` when running in Docker, or align the two).

The sample app serves:

- `/`, `/about`, `/contact` — public pages
- `/docs` — the interactive documentation site (17 sections)
- `/only-admin/log/in` — admin dashboard (token-based; configure `JWEB_ADMIN_TOKEN`/`JWEB_ADMIN_EMAIL`)
- `/api/docs`, `/api/redoc`, `/api/scalar`, `/api/openapi.json` — generated API documentation

---

## Requirements

- **Java 21+** (uses records, sealed interfaces, pattern matching, virtual threads)
- **Maven 3.6+**
- **Spring Boot 4.0.0** (parent POM)
- **MongoDB** (optional — set `jweb.data.enabled: false` to run without it)

---

## Project Status

The HTML/CSS/JS DSLs, routing, MongoDB integration, security, and validation layers are
functional and used by the bundled sample app. The reactive state/WebSocket round-trip and
client hydration are **partially wired** — the server emits everything needed, but the client
runtime is not auto-injected yet. [Known Issues](./readme/known-issues.md) tracks exactly
what works, what doesn't, and where the sharp edges are. AI/LLM integration (Spring AI) is
**planned but not shipped** — its dependencies are commented out in `pom.xml`.

---

## License

MIT

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Email: the.jweb.team@gmail.com

---

Built with Java by developers who love Java!
