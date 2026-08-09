[← Back to README](./../README.md)

# Configuration

## Configuration files — who owns what

JWeb splits configuration across **two YAML files**:

| File | Owner | Loaded by | Contains |
|------|-------|-----------|----------|
| `src/main/resources/application.yaml` | your app | Spring Boot | port, app name, logging, `jweb.admin.*`, `jweb.data.*` |
| `src/main/resources/jweb.yaml` | framework | `@JWebApplication`'s `@PropertySource` | compression, devtools/livereload, `jweb.dev.*`, `jweb.performance.*` |

`application.yaml` values override `jweb.yaml` (standard Spring property precedence).

### `application.yaml` (actual contents of the sample app)

```yaml
server:
  port: ${PORT:8085}

spring:
  application:
    name: MyApp

logging:
  level:
    org.mongodb.driver: WARN

jweb:
  admin:
    token: ${JWEB_ADMIN_TOKEN:}    # empty default → admin login disabled (fails closed)
    email: ${JWEB_ADMIN_EMAIL:}
  api:
    base: /api/v1                  # NOTE: declared but currently unused by any code
  data:
    enabled: true
    mongo:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:myapp}
```

### `jweb.yaml` (framework defaults)

```yaml
server:
  compression:
    enabled: true
    mime-types: text/html,text/css,text/javascript,application/javascript,application/json,...
    min-response-size: 1024

spring:
  main:
    lazy-initialization: true
  devtools:
    restart:
      enabled: true
      poll-interval: 100ms
      quiet-period: 50ms
      additional-paths: src/main/java/com/osmig/Jweb/app
    livereload:
      enabled: true
      port: 35729

jweb:
  dev:
    hot-reload: true
    watch-paths: src/main/java/com/osmig/Jweb/app
    debounce-ms: 10          # code floor is 10; code default is 50
    debug: false
  performance:
    minify-css: true
    minify-html: false
    prefetch:
      enabled: true
      cache-ttl: 300000
      hover-delay: 300       # code default is 100; yaml overrides to 300
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 8085 |
| `JWEB_ADMIN_TOKEN` | Admin login token (empty = admin disabled) | — |
| `JWEB_ADMIN_EMAIL` | Admin email address | — |
| `JWT_SECRET` | JWT signing secret (min 32 chars; used by `Jwt.init()`) | — |
| `MONGO_URI` | MongoDB URI (via `application.yaml` placeholder) | mongodb://localhost:27017 |
| `MONGO_DB` | MongoDB database (via placeholder) | myapp |
| `OPENAI_API_KEY` | Reserved for the planned AI module | — |

`.env` files are supported via the `spring-dotenv` dependency.

> Note: `Mongo.connect()` (the no-arg form) reads `MONGO_URI`/`MONGO_DB` directly with a
> different database default (`test`), but the framework never calls it — the active path is the
> Spring placeholder route through `JWebConfiguration`.

> Docker note: the Dockerfile `EXPOSE`s **8085**, matching the app default.

### Framework toggles (application.yaml)

| Property | Default | Effect |
|----------|---------|--------|
| `jweb.dev.debug` | `false` | Show exception details and stack traces on error pages (dev only) |
| `jweb.runtime.enabled` | `true` | Inject the JWeb client runtime (WebSocket events, state sync, DOM patching) into rendered pages |
| `jweb.websocket.allowed-origins` | *(blank = same-origin)* | Comma-separated origins allowed to open the `/jweb` WebSocket (`*` for dev) |
| `jweb.markitdown.command` | `.tools/markitdown/bin/markitdown` | Path to the markitdown CLI |
| `jweb.markitdown.timeout-seconds` | `120` | Max seconds per document conversion |

## MongoDB Auto-Connection

`JWebConfiguration` registers an `ApplicationRunner` that calls
`Mongo.connect(uri, database)` when `jweb.data.enabled: true`. This happens **after** context
refresh, and applies registered `Schema` indexes on connect. Set `jweb.data.enabled: false`
to run without MongoDB (the test suite runs with it disabled).

## Development Tools

### Hot Reload

`DevServer` (`@ConditionalOnProperty jweb.dev.hot-reload=true`) watches
`jweb.dev.watch-paths` with a `WatchService` and exposes `/__jweb_dev/events` (SSE),
`/__jweb_dev/reload`, and `/__jweb_dev/status` via `DevController`. The sample layout includes
`DevServer.script()` in `<body>` so the browser reloads on change.

```java
// In your layout:
body(..., DevServer.script())          // recommended (SSE-based)
// Alternatives via HotReload: fast(), liveReload(), combined(),
// builder().useSSE(true).showIndicator(true).indicatorPosition("bottom-right").build()
```

### Prefetch

Auto-injected on every Element response (`Prefetch.scriptTag()`); hover-prefetches links.
Configure via `jweb.performance.prefetch.*`. For full SPA-style navigation (partial swaps, View
Transitions), additionally include `Navigation.script()` — see the architecture doc.

### CLI Scaffolding

`JWebCli` provides project and file generation:

```bash
# New project
java -cp Jweb.jar com.osmig.Jweb.framework.cli.JWebCli new myapp --package=com.example

# Generators: page (p), component (c), layout (l), form (f), crud, api
java -cp ... JWebCli generate page Pricing
java -cp ... JWebCli generate crud Product name:string price:double
```

### Claude Code Agents

The repo ships specialized agents in `.claude/agents/` for AI-assisted development (DSL
guardians/validators/reviewers for HTML/CSS/JS, todo management, context helpers). Note
`.claude/` is gitignored — agents are local to each machine.

## Project Structure

### Application code (`src/main/java/com/osmig/Jweb/app/`, ~108 files)

```
app/
├── api/                       # REST controllers (Spring MVC dispatch)
│   ├── AdminApi.java             # Session-based admin auth (env-configured token) + messages
│   ├── ContactApi.java           # @REST("/api/v1/contact") — saves submissions to MongoDB
│   └── ExampleApi.java           # @REST("/api/v1/example") — demo CRUD (stub data)
├── docs/                      # ~90 files — the /docs documentation site
│   ├── DocsPage.java             # 3-column shell (sidebar | content | on-this-page rail)
│   ├── DocContent.java           # section-id → section dispatch (17 sections)
│   ├── DocSidebar.java           # nav groups (Basics/Core/Features/Advanced/More)
│   ├── DocExamples.java          # ~111 code-sample constants (1585 lines)
│   ├── DocComponents/DocStyles/DocsNavScript
│   └── sections/                 # 17 top-level sections + per-topic subpackages
├── forms/
│   └── FormComponents.java       # field/textareaField/statusBox/submitButton helpers
├── layout/
│   ├── Layout.java               # html > Head + body[Nav, main(content), Footer, DevServer.script()]
│   ├── Head.java                 # meta/title + global Stylesheet + keyframes
│   ├── Nav.java                  # sticky animated-gradient navbar
│   ├── Footer.java               # glassmorphism footer
│   └── Theme.java                # design tokens (colors, spacing, text sizes, radii)
├── pages/
│   ├── HomePage.java  AboutPage.java  ContactPage.java
│   └── admin/
│       ├── AdminLoginPage.java       # posts to /only-admin/log/in
│       └── AdminMessagesPage.java    # contact-submission grid
├── subheader/                 # scroll-synced "On This Page" rail
├── App.java                   # @JWebApplication entry point
└── Routes.java                # the single JWebRoutes implementation
```

### Framework code

237 Java files across 45 packages — see the package tree in
[Architecture](./architecture.md#framework-structure-237-framework-files-across-45-packages--108-app-files).

### Root-level documents

| File | Purpose | Status |
|------|---------|--------|
| `README.md` | User-facing overview (this doc set) | current |
| `readme/*.md` | The 8 detailed docs | current |
| `STANDARD.md` | Coding standards for JWeb apps (file size, DSL-only, separation) | current |
| `framework/MODERN_ELEMENTS.md` | Modern HTML5 elements guide | ⚠️ contains non-compiling `attrs().onclick(...)` examples |
| `dsl-todos.md` | DSL improvement tracker | ⚠️ several "remaining" items actually shipped (anchor positioning, scroll snap, popover, IndexedDB, …) |
| `PLAN.md` | Original design doc (2025-12) | historical — describes aspirational WebSocket state sync |
| `JWEB_EXAMPLES.md` | 20-level DSL tutorial | ⚠️ stale imports (`Elements.*` vs `El.*`) |
| `prompt.md` | AI-agent governance prompt | stale; contradicts STANDARD.md on file-size limits |
| `src/main/java/com/osmig/Jweb/framework/docs/*.md` | 15 internal reference docs, shipped in the jar | mostly current; no doc for db/mongo or openapi |

## Dependencies (from `pom.xml`)

**Coordinates:** `com.osmig:Jweb:1.0.0` · **Parent:** `spring-boot-starter-parent:4.0.0` ·
**Java:** 21

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-web | (managed 4.0.0) | HTTP handling |
| spring-boot-starter-websocket | (managed) | WebSocket support |
| spring-boot-starter-test | (managed, test) | Testing |
| spring-boot-devtools | (managed, runtime, optional) | Hot reload |
| spring-dotenv | 4.0.0 | `.env` file support |
| jackson-databind | (managed) | JSON processing |
| mongodb-driver-sync / bson / mongodb-driver-core | 5.2.0 | MongoDB |
| spring-security-crypto | (managed, optional) | BCrypt hashing |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 (optional) | JWT support |

**Commented out (not active):** spring-boot-starter-data-jpa, Spring AI OpenAI/Ollama
(2.0.0-M1), Testcontainers.

**Build plugins:**

- `maven-resources-plugin` — copies `framework/**/*.java|*.md` into the jar under
  `framework-src/` (used by the CLI scaffolder) and `README.md` under `readme/`.
- `spring-boot-maven-plugin` — mainClass `com.osmig.Jweb.app.App`, with
  `--add-opens java.base/sun.misc` and `java.base/java.nio` JVM args.

## Feature Reference (smaller packages)

| Feature | Package | Key API |
|---------|---------|---------|
| Internationalization | `i18n/` | `Messages.load(lang, map)`, `I18n.t(key, args)`, `I18n.middleware()` — see [Backend](./backend.md#internationalization) |
| File Uploads | `upload/` | `FileUpload.getFile(req, name)`, `validate(...)`, `UploadedFile.saveTo(dir)` |
| Background Jobs | `async/` | `Jobs.run/submit/track`, `Scheduler.cron(...)`, `Suspense` — see [State & Realtime](./state-and-realtime.md) |
| Health Checks | `health/` | `Health.register(name, check)`, `Health.setupEndpoints(app)` → `/health[/live|/ready]` |
| Metrics | `metrics/` | `Metrics.counter/gauge/timer`, `Metrics.middleware()`, `/metrics` (JSON or Prometheus) |
| Testing | `testing/` | `JWebTest`, `MockRequest`, `MockSession`, `TestClient` |
| Accessibility | `accessibility/` | `A11y.validate(element)` — WCAG 2.1 **auditor** (checks alt text, labels, heading order; it is not an ARIA helper library) |
| SSE | `sse/` | `SseBroadcaster`, `SseEmitter`, `SseEvent` |
| WebSockets | `websocket/` | auto-registered at `/jweb` |
| View Transitions | `transition/` | `Transition.when(show).enter(...)`, `attrs().transition()` |
| Portals | `portal/` | `Portal.to(name, el)` / `Portal.outlet(name)` |
| Caching | `cache/` | `Cache.create(ttl)`, `getOrSet`, `Cache.named(...)` |
| Error Handling | `error/` | `JWebException`, `ErrorHandler.errorHandling()` middleware |
| HTTP Client | `http/` | `Fetch.get(url).bearer(t).send()` → `FetchResult` |
| Hydration | `hydration/` | `HydrationData`, `VNodeSerializer` (see State & Realtime) |
| Navigation | `navigation/` | `Link.to(...)`, `Navigation.script()` |
| UI Components | `ui/` | `UI.*` builders, `Toast.setup()` |
| Context | `context/` | `Context.key/provide/use/find` |
| CLI | `cli/` | `JWebCli` (`Templates` is package-private) |

## Testing status

The suite (30 tests, no MongoDB required) covers Route matching and 405 semantics
(`RouterTest`), middleware ordering/glob scoping/queued headers (`MiddlewareStackTest`), the
reactive-state loop — context lifetime, scoped handlers, `useComponent` (`StateLoopTest`) —
Mongo `Schema` validation (`SchemaValidationTest`), and the DSL fixes (`DslFixesTest`), plus
the Spring context smoke test (which runs with `jweb.data.enabled=false`). Run with
`./mvnw test`. Next candidates: `AdminApi.login`, `ContactApi.submit`, and an integration
test through `JWebController` for page routes.
