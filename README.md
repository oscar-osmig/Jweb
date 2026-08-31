# JWeb Framework

**Version 2.0.0** | **Last Updated: 2026-08-30**

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
7. **Modular Architecture** — Clean separation with focused files. Facade entry points (`jweb.El.*` for HTML, `jweb.Css.*` for CSS, `jweb.Js.*`/`jweb.Actions.*` for JavaScript) hide complexity while keeping internals maintainable.

---

## Features

- **Type-safe HTML** — 24 element modules; build HTML with Java methods, no string templates
- **Type-safe CSS** — 29 style modules; CSS properties as methods, units, colors, animations, `@media`/`@container`/`@supports`/`@layer`/`@scope`
- **Type-safe JavaScript** — 43 JS modules; generate client-side JS from Java, from form handlers to IndexedDB and WebRTC
- **Component-based** — Reusable components via the `Template` interface
- **Virtual DOM** — `VNode` tree rendering with XSS-safe text escaping by default
- **Routing & Middleware** — Fluent route definitions with `:param` path parameters and a request pipeline
- **REST API** — Annotation-based REST controllers (`@REST`, `@GET`, `@POST`, `@UPDATE`, `@PATCH`, `@DEL`) dispatched by Spring MVC
- **MongoDB Integration** — Fluent DSL (`Mongo`, `Doc`, `MongoQuery`) with Spring-driven auto-connection
- **Security** — JWT auth, session auth (`Auth`/`Principal`), CSRF protection, rate limiting, BCrypt hashing, OAuth2 providers
- **Validation** — Composable `Validator<T>` plus `FormValidator`/`FieldValidator` fluent APIs
- **OpenAPI** — Generates OpenAPI 3.0.3 spec + Swagger UI, Redoc, and Scalar doc pages
- **Realtime** — Reactive `State<T>` with a wired browser↔server loop (WebSocket events, DOM patching), SSE from any route, `useComponent` reactive regions
- **Fragments (server-driven UI)** — `attrs().swap(url, target)` / `swapForm(...)` / `swapMorph(...)` fetch-and-swap HTML fragments with View Transitions, DOM morphing (focus/input state preserved), and history — the HTMX pattern, built in, zero JS written
- **Streaming SSR** — `Streamed.of(() -> page)` flushes the shell instantly; `Suspense` blocks stream in as their data resolves, in parallel, no JS written
- **Typed routes** — `TypedRoute.path("/users/:id", Long.class)`: handler params parsed and URLs compile-time checked
- **Built-in AI** — `AI.ask/chat/agent` with tool-calling loops against any OpenAI-compatible API (OpenAI, Ollama, Groq...), plus a drop-in chat widget — zero dependencies
- **SEO & performance** — `Seo` builder (OG/Twitter/canonical), on-the-fly image optimization (`/jweb/img`), immutably-cached runtime assets, gzip, startup warmup
- **Background work** — Virtual-thread `Jobs`, cron `Scheduler`, `Suspense` for async rendering, TTL `Cache`
- **Developer Experience** — Hot reload dev server, testing utilities (`JWebTest`, `MockRequest`, `TestClient`), CLI scaffolding, accessibility auditor, `Middlewares.recommended()` secure baseline

---

## Install

JWeb is published via [JitPack](https://jitpack.io/#oscar-osmig/Jweb). Add the repository
and the dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.oscar-osmig</groupId>
    <artifactId>Jweb</artifactId>
    <version>v2.0.0</version>
</dependency>
```

Gradle:

```groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies { implementation 'com.github.oscar-osmig:Jweb:v2.0.0' }
```

Then annotate your application class — the framework's beans arrive through Spring Boot
auto-configuration, so you only component-scan your own package:

```java
@JWebApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

Requires **Java 21+**. Use `main-SNAPSHOT` as the version to track the latest commit.

### Upgrading from 1.x

2.0.0 is **source-compatible but not binary-compatible**. 139 element overloads were removed
and the attribute surface moved to an interface, so recompile against the new jar rather than
dropping it in beside the old one.

Two call sites still compile but now mean something different — worth checking by hand:

- `textarea("hello")` renders the text `hello`; it used to set `name="hello"`. Use
  `textarea(name("bio"))`.
- `JSHistory.pushState(state, url)` takes its arguments in platform order. The
  `(String url, Val state)` and 3-argument forms are deleted, so those calls fail to compile
  rather than changing meaning.

A third change — 29 deleted CSS animation presets — is invisible, because none of them
animated anything. Everything else either still compiles with a deprecation warning or fails
loudly. Full details: **[dsl-simplification.md](dsl-simplification.md)**.

---

## Quick Start

```java
import jweb.Element;
import jweb.Template;
import static jweb.El.*;

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

### Form with Server Submission — zero JavaScript written

```java
// The form POSTs and swaps the returned fragment into #form-status.
// Without JS it still submits natively to the same route.
form(attrs().action("/contact/submit").method("post")
        .swapForm("/contact/submit", "#form-status"),
    field("Name", "name", "text", "Your name"),
    div(attrs().id("form-status")),
    submitButton("Send Message"))

// The route returns a fragment:
app.post("/contact/submit", ctx -> {
    messageStore.save(ctx.formParam("name"), ...);
    return ContactStatus.success("Message sent!");
});
```

For richer client behavior the JS DSL is still there (`actions().add(onSubmit(...)...)`) —
see [JavaScript DSL](./readme/javascript-dsl.md).

---

## Imports — one rule

Everything you import lives in the `jweb` package:

```java
// Static DSLs
import static jweb.El.*;      // HTML: elements, attributes, typed inputs, conditionals
import static jweb.Css.*;     // CSS: style(), units, colors, grid, media(), keyframes()
import static jweb.Js.*;      // low-level JS + events + reactive runtime + async
import static jweb.Actions.*; // declarative event actions (kept separate from Js)
import static jweb.State.*;   // server-driven state hooks
import static jweb.UI.*;      // prebuilt components
import static jweb.Layout.*;  // layout primitives
import static jweb.Mongo.*;   // MongoDB access (also jweb.Schema, jweb.Doc)

// Specialty modules keep their class names under jweb.css.* / jweb.js.*
import static jweb.css.Selectors.*;   // and MediaQuery, Keyframes, Stylesheet, ...
import static jweb.js.JSClipboard.*;  // and JSCanvas, JSCrypto, JSStorage, ...

// App infrastructure
import jweb.JWebApplication;  // the @JWebApplication annotation
import jweb.api.*;            // @REST, @GET, @POST, @UPDATE, @PATCH, @DEL
import jweb.Middlewares;      // recommended(), logging(), rateLimit(), ...
import jweb.Response;         // Response.html/json/redirect
import jweb.Csrf;             // CSRF tokens (and jweb.CsrfToken, jweb.Auth)
import jweb.Suspense;         // async blocks (and jweb.Streamed, jweb.Jobs)
import jweb.SseEmitter;       // server-sent events
import jweb.TypedRoute;       // compile-time-checked routes
import jweb.state.State;      // the State<T> type useState() returns

// Types
import jweb.Element;          // what components return
import jweb.Template;         // what pages implement
import jweb.Style;            // what style helpers return
import jweb.CSSValue;         // what color/unit helpers return
import jweb.JWeb;             // the app builder
import jweb.JWebRoutes;       // where you configure routes
```

**Migrating from the long imports:** the old
`com.osmig.Jweb.framework.*` entry points (`El`, `Elements`, `CSS`, `CSSUnits`,
`CSSColors`, `JS`, `Events`, `Actions`, `StateHooks`, `UI`, `Layout`, `Style`, ...)
still compile — they are `@Deprecated` aliases of the same code, so existing apps
keep working unchanged. New code should use the `jweb.*` forms; in most files
`jweb.El` + `jweb.Css` replace what used to take four or five imports.

---

## Documentation

| Topic | Description |
|-------|-------------|
| [Why JWeb?](./readme/why-jweb.md) | Feature comparison with React/Next, HTMX, Astro & co — and the honest trade-offs |
| [Architecture](./readme/architecture.md) | Rendering pipeline, request flow, routing systems, middleware, Request/Response APIs |
| [HTML DSL](./readme/html-dsl.md) | Elements, attributes, modern HTML5, popover, forms, conditionals, fluent builders |
| [CSS DSL](./readme/css-dsl.md) | Inline styles, rules, units, colors, animations, at-rules, themes, utilities |
| [JavaScript DSL](./readme/javascript-dsl.md) | Actions, form handlers, fetch, events, and the full 43-module browser API surface |
| [State & Realtime](./readme/state-and-realtime.md) | Reactive state, events, hydration, WebSocket, SSE, transitions, portals, async rendering |
| [Backend](./readme/backend.md) | REST API, OpenAPI, MongoDB, security, validation, forms, uploads, jobs, testing |
| [Configuration](./readme/configuration.md) | Setup, config files, environment variables, dev tools, CLI, project structure |
| [Known Issues](./readme/known-issues.md) | Verified gaps, unwired features, and API pitfalls — read before extending the framework |
| [Migrating to 2.0](./dsl-simplification.md) | What changed in the DSL pass, the six rules it follows now, and the breaking changes to check by hand |
| [Design Tooling](./readme/design-tooling.md) | impeccable setup, and how to run its detector against JWeb's rendered HTML |

Additional internal reference docs ship with the framework source at
`src/main/java/com/osmig/Jweb/framework/docs/` (15 topic files) and are copied into the jar
under `framework-src/`.

### For AI assistants: `/docs/tell`

`GET /docs/tell` returns this entire documentation set — the guides above plus all 15
reference topics — as one plain-text markdown document, version-stamped and ordered so the
DSL rules and the 2.x breaking changes come first. Point an assistant at it before asking it
to write JWeb code:

```
curl https://jweb.build/docs/tell            # everything, ~300KB
curl https://jweb.build/docs/tell?topic=css-dsl   # one topic, with the header
```

An unknown `topic` returns 404 listing the valid ids. Each document is fenced by
`BEGIN <id>` / `END <id>` comments so the response can be split back into files.

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

# Library build (default — sample app excluded, no executable jar)
./mvnw clean package

# Runnable showcase build (includes the sample app, produces an exec jar)
./mvnw clean package -Pdemo
java -jar target/Jweb-2.0.0-exec.jar

# With specific profile
java -jar target/Jweb-2.0.0-exec.jar --spring.profiles.active=prod
```

Then open `http://localhost:8085` in your browser (port is `${PORT:8085}` in `application.yaml`;
the Dockerfile `EXPOSE`s the same 8085).

The sample app serves:

- `/`, `/about`, `/contact` — public pages
- `/docs` — the interactive documentation site (21 sections)
- `/sandbox` — live DSL playground; `/demo/streaming` — streaming SSR demo
- `/only-admin/log/in` — admin dashboard (token-based; configure `JWEB_ADMIN_TOKEN`/`JWEB_ADMIN_EMAIL`)
- `/api/docs`, `/api/redoc`, `/api/scalar`, `/api/openapi.json` — generated API documentation

---

## Requirements

- **Java 21+** (uses records, sealed interfaces, pattern matching, virtual threads)
- **Maven 3.6+**
- **Spring Boot 4.0.8** (parent POM)
- **MongoDB** (optional — set `jweb.data.enabled: false` to run without it)

---

## Project Status

The HTML/CSS/JS DSLs, routing, MongoDB integration, security, and validation layers are
functional and used by the bundled sample app. The reactive state loop is fully wired: the
client runtime (`/jweb/runtime.js`) is auto-injected into every rendered page (disable with
`jweb.runtime.enabled: false`), and state changes patch the DOM over the WebSocket/event
channel. AI integration ships built-in (`AI.ask/chat/agent` against any OpenAI-compatible
API, configured via `jweb.ai.*`) — only the *optional* Spring AI starters remain commented
out in `pom.xml`. [Known Issues](./readme/known-issues.md) tracks exactly what works, what
doesn't, and where the sharp edges are.

---

## License

MIT

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Email: the.jweb.team@gmail.com

---

Built with Java by developers who love Java!
