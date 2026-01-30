[← Back to README](./../README.md)

# Configuration

## Additional Features

| Feature | Package | Classes | Usage |
|---------|---------|---------|-------|
| **Internationalization** | `i18n/` | `I18n`, `Messages` | `I18n.load("en")`, `messages.get("greeting")`, locale detection |
| **File Uploads** | `upload/` | `FileUpload`, `UploadedFile` | `FileUpload.single(req, "file")`, validation, save to disk |
| **Background Jobs** | `async/` | `Jobs`, `Scheduler`, `BackgroundTask`, `Suspense` | `Jobs.run(() -> processOrder(id))`, scheduled tasks |
| **Health Checks** | `health/` | `Health`, `HealthCheck`, `HealthStatus` | `Health.register("db", () -> HealthStatus.healthy())` |
| **Testing** | `testing/` | `JWebTest`, `TestClient`, `MockRequest`, `MockSession` | Unit and integration test utilities |
| **Accessibility** | `accessibility/` | `A11y` | `A11y.srOnly("text")`, ARIA helpers, WCAG 2.1 compliance |
| **Server-Sent Events** | `sse/` | `SseBroadcaster`, `SseEmitter`, `SseEvent` | Real-time server-to-client updates |
| **WebSockets** | `websocket/` | `JWebSocketConfig`, `JWebSocketHandler`, `WebSocketMessage` | Bidirectional real-time with auto-reconnect |
| **View Transitions** | `transition/` | `Transition`, `TransitionBuilder` | Page/view transitions with CSS animations |
| **Portals** | `portal/` | `Portal` | Render content outside normal DOM hierarchy |
| **Caching** | `cache/` | `Cache` | Server-side response and data caching |
| **Metrics** | `metrics/` | `Metrics` | Application performance monitoring |
| **Error Handling** | `error/` | `ErrorHandler`, `ErrorResponse`, `JWebException`, `ValidationException` | Structured error responses (400, 401, 403, 404, 500) |
| **HTTP Client** | `http/` | `Fetch`, `FetchResult` | Server-side HTTP requests |
| **Hydration** | `hydration/` | `HydrationData`, `VNodeSerializer` | Client-side state hydration |
| **Navigation** | `navigation/` | `Link`, `Navigation` | Navigation helpers and link building |
| **UI Components** | `ui/` | `UI`, `Toast` | Toast notifications, UI utilities |
| **Context** | `context/` | `Context`, `ContextKey` | Request context management |
| **CLI** | `cli/` | `JWebCli`, `Templates` | Project scaffolding and generation |

---

## Development Tools

### Hot Reload

```yaml
jweb:
  dev:
    hot-reload: true
    watch-paths: src/main/java,src/main/resources
    debounce-ms: 10
    debug: false
```

### Prefetch

```yaml
jweb:
  performance:
    prefetch:
      enabled: true
      cache-ttl: 300000
      hover-delay: 300
```

### Claude Code Agents

JWeb ships with 13 specialized Claude agents in `.claude/agents/` for AI-assisted development:

| Agent | Purpose |
|-------|---------|
| `css-dsl-guardian.md` | CSS DSL validation and review |
| `html-dsl-guardian.md` | HTML DSL validation and review |
| `js-dsl-guardian.md` | JavaScript DSL validation and review |
| `jweb-dsl-validator.md` | General DSL validation |
| `jweb-dsl-reviewer.md` | DSL code review |
| `jweb-dsl-bugfix.md` | DSL bug fixing |
| `jweb-html-dsl-reviewer.md` | HTML DSL review |
| `jweb-css-dsl-validator.md` | CSS DSL validation |
| `todo-manager.md` | Task management |
| `context-taker.md` | Context gathering |
| `remember-last-session.md` | Session context |
| `search-web.md` | Web search |
| `simplify-dsl.md` | DSL simplification |

---

## application.yaml

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
    token: ${JWEB_ADMIN_TOKEN:}
    email: ${JWEB_ADMIN_EMAIL:}
  api:
    base: /api/v1
  data:
    enabled: true
    mongo:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:myapp}
  dev:
    hot-reload: true
    debug: false
  performance:
    minify-css: true
    prefetch:
      enabled: true
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | 8085 |
| `JWEB_ADMIN_TOKEN` | Admin authentication token | - |
| `JWEB_ADMIN_EMAIL` | Admin email address | - |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | - |
| `MONGO_URI` | MongoDB connection URI | mongodb://localhost:27017 |
| `MONGO_DB` | MongoDB database name | myapp |
| `OPENAI_API_KEY` | OpenAI API key for AI features | - |

---

## Project Structure

### Application Code (108 Java files)

```
app/
|-- api/                       # 3 files - REST API controllers
|   |-- AdminApi.java             # Admin authentication (session-based, env config) + message retrieval
|   |-- ContactApi.java           # Contact form API (@REST("/api/v1/contact"), saves to MongoDB)
|   |-- ExampleApi.java           # Example API endpoints
|-- docs/                      # 90 files - Documentation pages
|   |-- DocComponents.java        # Reusable doc UI components
|   |-- DocContent.java           # Content retrieval for client-side navigation
|   |-- DocExamples.java          # Example code blocks
|   |-- DocSidebar.java           # Docs navigation sidebar
|   |-- DocsNavScript.java        # Docs navigation JavaScript
|   |-- DocsPage.java             # Docs page shell
|   |-- DocStyles.java            # Docs CSS styles
|   |-- sections/                 # 83 documentation section files
|       |-- IntroSection.java, SetupSection.java
|       |-- ElementsSection.java (+ 12 element subsections)
|       |-- StylingSection.java (+ 15 styling subsections)
|       |-- JavaScriptSection.java (+ 9 JS subsections)
|       |-- ComponentsSection.java (+ 5 component subsections)
|       |-- RoutingSection.java (+ 5 routing subsections)
|       |-- FormsSection.java (+ 8 form subsections)
|       |-- ConditionalsSection.java (+ 4 conditional subsections)
|       |-- LayoutsSection.java, StateSection.java
|       |-- ApiSection.java, SecuritySection.java, DataSection.java
|       |-- UIComponentsSection.java, DevToolsSection.java, ExamplesSection.java
|-- forms/                     # 1 file
|   |-- FormComponents.java      # Reusable form components (field, textareaField, statusBox, submitButton)
|-- layout/                    # 5 files - Layout components
|   |-- Footer.java               # Site footer
|   |-- Head.java                 # HTML head (meta, CSS, fonts)
|   |-- Layout.java               # Main layout wrapper (title + content)
|   |-- Nav.java                  # Navigation bar
|   |-- Theme.java                # Design tokens (colors, spacing, fonts, rounded corners)
|-- pages/                     # 5 files - Page components
|   |-- AboutPage.java            # About page
|   |-- ContactPage.java          # Contact form with JS DSL submission + ContactScripts
|   |-- HomePage.java             # Home page
|   |-- admin/                    # 2 files - Admin dashboard
|       |-- AdminLoginPage.java       # Login form with gradient border card effect
|       |-- AdminMessagesPage.java    # Messages grid with gradient border cards
|-- subheader/                 # 2 files - Subheader components
|   |-- SubheaderSidebar.java     # Right sidebar with sub-header navigation
|   |-- SubheaderScript.java      # Scroll-synced navigation JavaScript
|-- App.java                   # Application entry point (@JWebApplication)
|-- Routes.java                # Route definitions (pages, admin auth, API docs, OpenAPI mount)
```

### Framework Code (237 Java files across 44 packages)

```
framework/
|-- core/               # 6 files: Element, Renderable, Component, Page, RawContent, ErrorBoundary
|-- elements/           # 24 files: HTML elements DSL (see HTML DSL section)
|-- styles/             # 35 files: CSS DSL (see CSS DSL section)
|-- js/                 # 43 files: JavaScript DSL (see JavaScript DSL section)
|-- template/           # 1 file: Template interface with lifecycle hooks
|-- vdom/               # 5 files: VNode, VElement, VText, VRaw, VFragment
|-- routing/            # 6 files: Router, Route, RouteHandler, PageRoute, PageRegistry, @Page
|-- middleware/         # 4 files: Middleware, MiddlewareChain, MiddlewareStack, Middlewares
|-- server/             # 6 files: Request, Response, Cookie, JWebController, JWebEventController, ErrorPage
|-- api/                # 6 files: @REST, @GET, @POST, @DEL, @PATCH, @UPDATE
|-- security/           # 10 files: Auth, Principal, Jwt, Password, Cors, Csrf, CsrfToken, CsrfException, RateLimit, OAuth2
|-- db/mongo/           # 6 files: Mongo, Doc, Schema, MongoQuery, MongoUpdate, MongoDelete
|-- openapi/            # 5 files: OpenApi, ApiDoc, ApiParam, ApiBody, ApiResponse
|-- validation/         # 7 files: Validator, Validators, ValidationResult, FormValidator, FieldValidator, NumberValidators
|-- state/              # 6 files: State, StateBinding, StateHooks, StateManager, ComponentRegistry, RenderableComponent
|-- attributes/         # 3 files: Attr, Attrs, Attributes
|-- events/             # 4 files: Event, DomEvent, EventHandler, EventRegistry
|-- error/              # 4 files: ErrorHandler, ErrorResponse, JWebException, ValidationException
|-- async/              # 4 files: Jobs, Scheduler, BackgroundTask, Suspense
|-- testing/            # 4 files: JWebTest, MockRequest, MockSession, TestClient
|-- websocket/          # 3 files: JWebSocketConfig, JWebSocketHandler, WebSocketMessage
|-- sse/                # 3 files: SseBroadcaster, SseEmitter, SseEvent
|-- dev/                # 3 files: HotReload, DevController, DevServer
|-- health/             # 3 files: Health, HealthCheck, HealthStatus
|-- util/               # 3 files: Json, Log, YamlPropertySourceFactory
|-- i18n/               # 2 files: I18n, Messages
|-- upload/             # 2 files: FileUpload, UploadedFile
|-- navigation/         # 2 files: Link, Navigation
|-- transition/         # 2 files: Transition, TransitionBuilder
|-- ui/                 # 2 files: UI, Toast
|-- http/               # 2 files: Fetch, FetchResult
|-- hydration/          # 2 files: HydrationData, VNodeSerializer
|-- context/            # 2 files: Context, ContextKey
|-- forms/              # 2 files: Form, FormModel
|-- cli/                # 2 files: JWebCli, Templates
|-- accessibility/      # 1 file: A11y (WCAG 2.1, ARIA)
|-- config/             # 1 file: JWebConfiguration (MongoDB init, Router, static resources)
|-- cache/              # 1 file: Cache
|-- layout/             # 1 file: Layout
|-- metrics/            # 1 file: Metrics
|-- performance/        # 1 file: Prefetch
|-- portal/             # 1 file: Portal
|-- ref/                # 1 file: Ref
|-- docs/               # 15 .md files (css, elements, http, i18n, javascript, jobs, middleware, routing, security, sse, state, templates, testing, validation, file-upload)
|-- JWeb.java           # Main framework class
|-- JWebApplication.java # @JWebApplication annotation
|-- JWebAutoConfiguration.java # Auto-configuration
|-- JWebRoutes.java     # Routes interface
|-- package-info.java   # Package documentation
```

### Documentation Files

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Comprehensive project context for Claude Code AI |
| `README.md` | User-facing documentation (this file) |
| `STANDARD.md` | Development standards (file length, DSL usage, separation of concerns) |
| `dsl-todos.md` | DSL improvement tracker (completed + remaining tasks) |
| `PLAN.md` | Project planning |
| `HELP.md` | Help documentation |
| `JWEB_EXAMPLES.md` | Example code |
| `prompt.md` | Prompt templates |
| `Dockerfile` | Docker configuration |
| `framework/MODERN_ELEMENTS.md` | Modern HTML5 elements guide |
| `framework/docs/*.md` | 15 detailed feature documentation files |

---

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.0 | Framework base |
| Spring Boot Web | - | HTTP handling |
| Spring Boot WebSocket | - | WebSocket support |
| Spring Boot DevTools | - | Hot reload |
| Spring Boot Test | - | Testing |
| spring-dotenv | 4.0.0 | .env file support |
| MongoDB Driver Sync | 5.2.0 | Database |
| MongoDB BSON | 5.2.0 | BSON types |
| MongoDB Driver Core | 5.2.0 | Driver core |
| Spring Security Crypto | - | BCrypt password hashing |
| JJWT | 0.12.6 | JWT support |
| Jackson Databind | - | JSON processing |

### Optional Dependencies (commented in pom.xml)
- Spring AI OpenAI 2.0.0-M1 - AI/LLM integration
- Spring AI Ollama 2.0.0-M1 - Local LLM support
- Testcontainers Ollama 1.20.4 - Docker-based Ollama
