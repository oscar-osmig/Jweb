[← Back to README](./../README.md)

# Configuration

## Configuration files — who owns what

JWeb splits configuration across **two YAML files**:

| File | Owner | Loaded by | Contains |
|------|-------|-----------|----------|
| `src/main/resources/application.yaml` | your app | Spring Boot | port, app name, logging, `jweb.admin.*`, `jweb.data.*` |
| `src/main/resources/jweb.yaml` | framework | `@JWebApplication`'s `@PropertySource` | compression, devtools/livereload, `jweb.dev.*`, `jweb.performance.*` |

`application.yaml` values override `jweb.yaml` (standard Spring property precedence).

### `application.yaml` (the sample app's config)

```yaml
server:
  port: ${PORT:8085}
  servlet:
    session:
      cookie:
        http-only: true
        same-site: lax
        secure: ${SESSION_COOKIE_SECURE:false}   # set true in production (HTTPS)

spring:
  application:
    name: MyApp
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 25MB

logging:
  level:
    org.mongodb.driver: WARN

jweb:
  admin:
    token: ${JWEB_ADMIN_TOKEN:}    # empty default → admin login disabled (fails closed)
    email: ${JWEB_ADMIN_EMAIL:}
  ai:
    enabled: false                 # built-in AI module; any OpenAI-compatible API
    base-url: ${AI_BASE_URL:https://api.openai.com/v1}
    api-key: ${AI_API_KEY:}
    model: ${AI_MODEL:gpt-4o-mini}
  markitdown:
    command: ${MARKITDOWN_CMD:.tools/markitdown/bin/markitdown}
    timeout-seconds: 120
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
| `AI_BASE_URL` | OpenAI-compatible API base URL for the built-in AI module | https://api.openai.com/v1 |
| `AI_API_KEY` | API key for the AI module (Ollama needs none) | — |
| `AI_MODEL` | Model name for the AI module | gpt-4o-mini |
| `MARKITDOWN_CMD` | Path to the markitdown CLI | .tools/markitdown/bin/markitdown |
| `SESSION_COOKIE_SECURE` | Mark the session cookie `Secure` (set true behind HTTPS) | false |

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
| `jweb.data.enabled` | `false` (code) — sample app sets `true` | Auto-connect MongoDB on startup |
| `jweb.ai.enabled` | `false` | Enable the built-in AI module (`AI.ask/chat/agent`) |
| `jweb.ai.base-url` / `api-key` / `model` | OpenAI defaults | Which OpenAI-compatible endpoint and model to use |
| `jweb.ai.temperature` / `timeout-seconds` | `0.7` / `60` | Sampling and per-request timeout for AI calls |

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

### AI-assisted development

`.claude/` is gitignored and local to each machine; it currently holds the `impeccable`
design-audit skill (see [Design Tooling](./design-tooling.md)) rather than any checked-in
agents.

## Project Structure

### Application code (`src/main/java/com/osmig/Jweb/app/`, ~122 files)

```
app/
├── api/                       # REST controllers (Spring MVC dispatch)
│   ├── AdminApi.java             # Session-based admin auth (env-configured token) + messages
│   ├── ContactApi.java           # @REST("/api/v1/contact") — saves submissions to MongoDB
│   ├── ExampleApi.java           # @REST("/api/v1/example") — demo CRUD (stub data)
│   ├── MarkitdownApi.java        # @REST("/api/v1/markitdown") — document conversion
│   └── MessageStore.java         # shared contact-message persistence
├── docs/                      # ~90 files — the /docs documentation site
│   ├── DocsPage.java             # 3-column shell (sidebar | content | on-this-page rail)
│   ├── DocContent.java           # section-id → section dispatch (21 sections)
│   ├── DocSidebar.java           # nav groups (Basics/Core/Features/Advanced/More)
│   ├── DocExamples.java          # ~101 code-sample constants (~1580 lines)
│   ├── DocComponents/DocStyles/DocsNavScript
│   └── sections/                 # 21 top-level sections + per-topic subpackages
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
├── sandbox/                   # /sandbox live DSL playground (sandboxed rendering)
├── subheader/                 # scroll-synced "On This Page" rail
├── App.java                   # @JWebApplication entry point
└── Routes.java                # the single JWebRoutes implementation
```

### Framework code

~247 Java files across ~47 packages — see the package tree in
[Architecture](./architecture.md#framework-structure).

### Root-level documents

| File | Purpose | Status |
|------|---------|--------|
| `README.md` | User-facing overview (this doc set) | current |
| `readme/*.md` | The 12 detailed docs | current |
| `STANDARD.md` | Coding standards for JWeb apps (file size, DSL-only, separation) | current |
| `framework/MODERN_ELEMENTS.md` | Modern HTML5 elements guide | ⚠️ contains non-compiling `attrs().onclick(...)` examples |
| `dsl-todos.md` | DSL improvement tracker | ⚠️ several "remaining" items actually shipped (anchor positioning, scroll snap, popover, IndexedDB, …) |
| `PLAN.md` | Original design doc (2025-12) | historical — describes aspirational WebSocket state sync |
| `JWEB_EXAMPLES.md` | 20-level DSL tutorial | ⚠️ stale imports (`Elements.*` vs `El.*`) |
| `prompt.md` | AI-agent governance prompt | stale; contradicts STANDARD.md on file-size limits |
| `src/main/java/com/osmig/Jweb/framework/docs/*.md` | 15 internal reference docs, shipped in the jar | mostly current; no doc for db/mongo or openapi |

## Dependencies (from `pom.xml`)

**Coordinates:** `com.osmig:Jweb:1.2.0` · **Parent:** `spring-boot-starter-parent:4.0.8` ·
**Java:** 21

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-web | (managed 4.0.8) | HTTP handling |
| spring-boot-starter-websocket | (managed) | WebSocket support |
| spring-boot-starter-test | (managed, test) | Testing |
| spring-boot-devtools | (managed, runtime, optional) | Hot reload |
| spring-dotenv | 4.0.0 | `.env` file support |
| jackson-databind | (managed) | JSON processing |
| mongodb-driver-sync / bson / mongodb-driver-core | 5.10.0 | MongoDB |
| spring-security-crypto | (managed, optional) | BCrypt hashing |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.7 (optional) | JWT support |

**Commented out (not active):** spring-boot-starter-data-jpa, Spring AI OpenAI/Ollama
(2.0.0-M1), Testcontainers.

**Build plugins:**

- `maven-resources-plugin` — copies `framework/**/*.java|*.md` into the jar under
  `framework-src/` (used by the CLI scaffolder) and `README.md` under `readme/`.
- `spring-boot-maven-plugin` — mainClass `com.osmig.Jweb.app.App`, with
  `--add-opens java.base/sun.misc` and `java.base/java.nio` JVM args. The default build
  is the publishable **library** (sample app excluded, repackage skipped); build the
  runnable showcase with `-Pdemo`, which produces `Jweb-<version>-exec.jar` (the
  `exec` classifier keeps the plain library jar as the main artifact).

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

The suite (107 tests across 14 classes, no MongoDB required) covers Route matching and 405
semantics (`RouterTest`), middleware ordering/glob scoping/queued headers
(`MiddlewareStackTest`), the reactive-state loop — context lifetime, scoped handlers,
`useComponent` (`StateLoopTest`) — Mongo `Schema` validation (`SchemaValidationTest`), the
DSL fixes (`DslFixesTest`), page routes end-to-end through `JWebController`
(`JWebTestPageRouteTest`), the AI module (`AiModuleTest`), streaming SSR (`StreamingTest`),
typed routes (`TypedRouteTest`), CSP nonces (`CspNonceTest`), sandbox hardening
(`SandboxSecurityTest`), and the v1.1+ import surface (`JwebShortImportsTest`,
`LegacyImportsCompatTest`), plus the Spring context smoke test (which runs with
`jweb.data.enabled=false`). Run with `./mvnw test`. Next candidates: `AdminApi.login` and
`ContactApi.submit`.
