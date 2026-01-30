[← Back to README](./../README.md)

# Architecture

## Core Rendering System

JWeb uses a hierarchical rendering system with these core interfaces:

| Interface | Purpose |
|-----------|---------|
| `Element` | Base interface for anything that can be rendered to HTML |
| `Renderable` | Elements that produce output |
| `Template` | Reusable UI components with a `render()` method |
| `Page` | Full-page templates that define `head()` and `body()` |
| `Component` | Stateful components with lifecycle |
| `ErrorBoundary` | Error boundary for graceful error handling |

```
Element (interface)
    |-- Template (interface) - render() returns Element
            |-- Page - Full page with head/body
            |-- Layout - Wraps pages with common structure
            |-- (your components)
```

## Virtual DOM

JWeb uses a Virtual DOM (VNode) for efficient rendering:

| VNode Type | Purpose |
|------------|---------|
| `VElement` | Represents an HTML element with tag, attributes, children |
| `VText` | Represents escaped text content (XSS-safe by default) |
| `VRaw` | Represents raw/unescaped HTML (use with caution) |
| `VFragment` | Groups multiple nodes without a wrapper element |

The rendering pipeline: `Template.render()` -> `Element.toVNode()` -> `VNode.toHtml()` -> HTML string

## Application Entry Point

```java
@JWebApplication  // combines @SpringBootApplication + @ComponentScan + jweb.yaml loading
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

## Auto-Configuration Chain

1. `@JWebApplication` triggers Spring Boot + component scanning of `com.osmig.Jweb`
2. `JWebAutoConfiguration` discovers all `JWebRoutes` beans and calls `configure(app)` on each
3. `JWebConfiguration` initializes MongoDB (if `jweb.data.enabled: true`), creates Router bean, configures static resources
4. `JWebController` handles all HTTP requests via `@RequestMapping("/**")`

## Request Flow

```
HTTP Request
  -> JWebController (@Controller, handles /**)
    -> PageRegistry.findByPath() [O(1) HashMap lookup for page routes]
    -> OR Router.match() [API routes with path parameter extraction]
    -> MiddlewareStack.execute() [ordered middleware chain]
    -> Page/RouteHandler
    -> Template.render() -> Element.toVNode() -> VNode.toHtml()
    -> Hydration injection (prefetch + state scripts before </body>)
  -> ResponseEntity<String> (HTML/JSON/redirect)
```

## Framework Structure (237 Java files across 44 packages)

```
framework/
├── accessibility/      # A11y helpers (1 file: A11y)
├── api/                # REST annotations (6 files: @REST, @GET, @POST, @DEL, @PATCH, @UPDATE)
├── async/              # Background jobs (4 files: Jobs, Scheduler, BackgroundTask, Suspense)
├── attributes/         # HTML attribute builders (3 files: Attr, Attrs, Attributes)
├── cache/              # Caching utilities (1 file: Cache)
├── cli/                # CLI tools (2 files: JWebCli, Templates)
├── config/             # Configuration (1 file: JWebConfiguration)
├── context/            # Request context (2 files: Context, ContextKey)
├── core/               # Core interfaces (6 files: Element, Renderable, Component, Page, RawContent, ErrorBoundary)
├── db/mongo/           # MongoDB integration (6 files: Mongo, Doc, Schema, MongoQuery, MongoUpdate, MongoDelete)
├── dev/                # Development tools (3 files: HotReload, DevController, DevServer)
├── docs/               # Framework documentation (15 .md files)
├── elements/           # HTML elements DSL (24 modules)
├── error/              # Error handling (4 files: ErrorHandler, ErrorResponse, JWebException, ValidationException)
├── events/             # Event handling (4 files: Event, DomEvent, EventHandler, EventRegistry)
├── forms/              # Form utilities (2 files: Form, FormModel)
├── health/             # Health checks (3 files: Health, HealthCheck, HealthStatus)
├── http/               # HTTP utilities (2 files: Fetch, FetchResult)
├── hydration/          # Client-side hydration (2 files: HydrationData, VNodeSerializer)
├── i18n/               # Internationalization (2 files: I18n, Messages)
├── js/                 # JavaScript DSL (43 modules)
├── layout/             # Layout management (1 file: Layout)
├── metrics/            # Application metrics (1 file: Metrics)
├── middleware/         # Middleware system (4 files: Middleware, MiddlewareChain, MiddlewareStack, Middlewares)
├── navigation/         # Navigation utilities (2 files: Link, Navigation)
├── openapi/            # OpenAPI documentation (5 files: OpenApi, ApiDoc, ApiParam, ApiBody, ApiResponse)
├── performance/        # Performance utilities (1 file: Prefetch)
├── portal/             # Portal rendering (1 file: Portal)
├── ref/                # Reference utilities (1 file: Ref)
├── routing/            # Router and routes (6 files: Router, Route, RouteHandler, PageRoute, PageRegistry, @Page)
├── security/           # Security (10 files: Jwt, Password, Cors, Csrf, CsrfToken, CsrfException, Auth, OAuth2, RateLimit, Principal)
├── server/             # Request/Response (6 files: Request, Response, Cookie, JWebController, JWebEventController, ErrorPage)
├── sse/                # Server-Sent Events (3 files: SseBroadcaster, SseEmitter, SseEvent)
├── state/              # Reactive state (6 files: State, StateBinding, StateHooks, StateManager, ComponentRegistry, RenderableComponent)
├── styles/             # CSS DSL (35 modules)
├── template/           # Template interface (1 file: Template)
├── testing/            # Test utilities (4 files: JWebTest, MockRequest, MockSession, TestClient)
├── transition/         # View transitions (2 files: Transition, TransitionBuilder)
├── ui/                 # UI components (2 files: UI, Toast)
├── upload/             # File uploads (2 files: FileUpload, UploadedFile)
├── util/               # Utilities (3 files: Json, Log, YamlPropertySourceFactory)
├── validation/         # Validation (7 files: Validator, Validators, ValidationResult, FormValidator, FieldValidator, NumberValidators)
├── vdom/               # Virtual DOM (5 files: VNode, VElement, VText, VRaw, VFragment)
├── websocket/          # WebSocket support (3 files: JWebSocketConfig, JWebSocketHandler, WebSocketMessage)
├── JWeb.java                  # Main framework class (create, use, get/post/put/delete, layout, pages)
├── JWebApplication.java       # @JWebApplication annotation (combines @SpringBootApplication + @ComponentScan + jweb.yaml)
├── JWebAutoConfiguration.java # Auto-configuration (discovers JWebRoutes beans, creates JWeb bean)
├── JWebRoutes.java            # Routes interface (configure(JWeb app))
└── package-info.java          # Package documentation
```

---

## Components and Templates

### Template Interface

The `Template` interface is the core abstraction for reusable UI components. It extends `Element` and provides extensive lifecycle hooks:

| Method | Return Type | Purpose |
|--------|-------------|---------|
| `render()` | `Element` | **Required.** Returns the component's Element tree |
| `beforeRender(Request)` | `void` | Called before render() - data loading, validation, setup |
| `afterRender(Request)` | `void` | Called after render() - cleanup, post-processing |
| `onMount()` | `String` | Returns JavaScript to run after DOM ready (DOMContentLoaded) |
| `onUnmount()` | `String` | Returns JavaScript for cleanup (beforeunload/SPA navigation) |
| `pageTitle()` | `Optional<String>` | Document title for SEO |
| `metaDescription()` | `Optional<String>` | Meta description tag for SEO |
| `extraHead()` | `Optional<Element>` | Additional elements for the `<head>` section |
| `scripts()` | `Optional<String>` | Inline scripts added at end of `<body>` |
| `cacheable()` | `boolean` | Whether template output can be cached (default: `true`) |
| `cacheDuration()` | `int` | Cache duration in seconds (default: `0`) |

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
        return div(class_("card"),
            h3(class_("card-title"), title),
            p(class_("card-content"), content)
        );
    }
}

// Usage
div(class_("container"),
    new Card("Welcome", "Hello, World!"),
    new Card("Features", "Build apps in pure Java")
)
```

### Lifecycle Hooks

```java
public class UserProfilePage implements Template {
    private User user;
    private List<Post> posts;

    @Override
    public void beforeRender(Request request) {
        int userId = request.requireParamInt("id");
        this.user = userService.findById(userId);
        this.posts = userService.getPostsForUser(userId);
    }

    @Override
    public String onMount() {
        return "initProfileCharts(); setupInfiniteScroll();";
    }

    @Override
    public String onUnmount() {
        return "clearInterval(refreshTimer);";
    }

    @Override
    public Element render() {
        return div(class_("profile"),
            h1(user.getName()),
            div(class_("posts"), each(posts, PostCard::new))
        );
    }
}
```

### Layouts

```java
public class MainLayout implements Template {
    private final Element content;

    public MainLayout(Element content) {
        this.content = content;
    }

    @Override
    public Element render() {
        return div(class_("layout"),
            new Navbar(),
            main(class_("main-content"), content),
            new Footer()
        );
    }
}

app.layout(MainLayout.class)
   .pages("/", HomePage.class, "/about", AboutPage.class);
```

---

## Event Handling

JWeb provides server-side event handling through the `events/` package and client-side event handling through the JS DSL:

### Server-Side Events

| Class | Purpose |
|-------|---------|
| `Event` | Base event interface |
| `DomEvent` | DOM event representation |
| `EventHandler` | Event handler interface |
| `EventRegistry` | Event registration and dispatch |

### Client-Side Event DSL (via `Events.java`)

```java
import static com.osmig.Jweb.framework.js.Events.*;

// Event delegation (attach to parent, filter by selector)
delegate("container", "click", ".card", callback("e")
    .log("Clicked card:", variable("e").path("target.textContent")));

// Debounced events (fire only after pause)
debounce("search-input", "input", 300, callback("e")
    .log("Search:", variable("e").path("target.value")));

// Throttled events (fire at most once per interval)
throttle("scroll-container", "scroll", 100, callback("e")
    .log("Scrolled"));

// Keyboard events
onKeyDown("editor", callback("e")
    .log("Key:", variable("e").dot("key")));

// Touch/swipe events
onSwipe("carousel", "left", callback().log("Swiped left"));
onSwipe("carousel", "right", callback().log("Swiped right"));
```

---

## Reactive State

```java
import com.osmig.Jweb.framework.state.State;

State<Integer> count = State.of(0);
State<String> name = State.of("");

count.set(5);
count.update(n -> n + 1);
int currentCount = count.get();
```

**State Management Classes:**

| Class | Purpose |
|-------|---------|
| `State<T>` | Reactive state container (`of()`, `get()`, `set()`, `update()`) |
| `StateBinding` | Binds state to UI elements |
| `StateHooks` | React-like hooks pattern |
| `StateManager` | Global state manager with per-request isolation |
| `ComponentRegistry` | Component lifecycle tracking |
| `RenderableComponent` | Stateful renderable component |

---

## Routing

### Page Routes

```java
app.get("/", () -> new HomePage());
app.get("/about", () -> new AboutPage());
app.get("/profile/:id", req -> {
    User user = getCurrentUser(req);
    return new ProfilePage(user);
});

// Admin routes with authentication
app.get("/only-admin/messages", ctx -> {
    if (!adminApi.isAuthenticated(ctx)) {
        return Response.redirect("/only-admin/log/in");
    }
    return Response.html(new Layout("Messages",
        new AdminMessagesPage(adminApi.getMessages()).render()
    ).render());
});
```

### API Routes

```java
app.get("/api/users", req -> Response.json(userService.findAll()));
app.post("/api/users", req -> {
    User user = req.bodyAs(User.class);
    return Response.json(userService.save(user)).status(201);
});
```

### Route Parameters

```java
int id = req.paramInt("id");
long bigId = req.paramLong("id");
UUID uuid = req.paramUUID("id");
int page = req.paramInt("page", 1);  // with default
String username = req.requireParam("username");  // throws if missing
```

---

## Middleware

```java
app.use(Middlewares.logging())
   .use(Middlewares.cors())
   .use(Middlewares.csrf())
   .use(Middlewares.rateLimit(100, 60000))
   .use(Middlewares.securityHeaders());

// Path-specific middleware
app.use("/api/**", authMiddleware);
app.use("/admin/**", adminOnlyMiddleware);
```

---

## Request and Response

### Request API

The `Request` class wraps the HTTP request with a comprehensive fluent API:

**Path Parameters:**
```java
String id = req.param("id");                    // String path param
int userId = req.paramInt("id");                // int path param
long bigId = req.paramLong("id");               // long path param
double score = req.paramDouble("score");        // double path param
boolean active = req.paramBool("active");       // boolean path param
UUID uuid = req.paramUUID("id");                // UUID path param
Optional<String> opt = req.paramOpt("name");    // optional path param

// Require params (throws if missing)
String name = req.requireParam("name");
int page = req.requireParamInt("page");
long count = req.requireParamLong("count");
UUID id = req.requireParamUUID("id");
```

**Query Parameters:**
```java
String search = req.query("q");                 // query string param
int page = req.queryInt("page");                // int query param
long offset = req.queryLong("offset");          // long query param
boolean verbose = req.queryBool("verbose");     // boolean query param
Map<String, String> all = req.queryParams();    // all query params
```

**Headers, Body, and Form Data:**
```java
String contentType = req.header("Content-Type");
Map<String, String> headers = req.headers();
String body = req.body();
<T> T obj = req.bodyAs(MyClass.class);          // JSON deserialization
String name = req.formParam("name");            // form field
Map<String, String> form = req.formParams();    // all form fields
```

**Cookies and Session:**
```java
String token = req.cookie("auth_token");
Map<String, String> cookies = req.cookies();
Object sessionVal = req.sessionAttr("user");
HttpSession session = req.session();
```

**Client Information:**
```java
String ip = req.ip();
String ua = req.userAgent();
boolean ajax = req.isAjax();
boolean wantsJson = req.acceptsJson();
boolean wantsHtml = req.acceptsHtml();
```

### Response API

The `Response` class provides static factory methods for building HTTP responses:

```java
// HTML responses
Response.html(element)                          // 200 with rendered Element
Response.html(htmlString)                       // 200 with HTML string
Response.html(HttpStatus.OK, element)           // custom status with Element

// JSON responses
Response.json(object)                           // 200 with JSON body
Response.json(HttpStatus.CREATED, object)       // custom status with JSON
Response.json()                                 // JSON builder
    .put("status", "ok")
    .put("count", 42)
    .build();                                   // returns ResponseEntity

// Text response
Response.text("plain text response")

// Redirects
Response.redirect("/dashboard")                 // 302 redirect
Response.redirect("/new-url", true)             // 301 permanent redirect
Response.seeOther("/other")                     // 303 redirect

// Success responses
Response.ok()                                   // 200 empty
Response.created("/api/users/123")              // 201 with Location header
Response.noContent()                            // 204

// Error responses
Response.badRequest("Invalid input")            // 400
Response.unauthorized("Login required")         // 401
Response.forbidden("Access denied")             // 403
Response.notFound("Page not found")             // 404
Response.serverError("Internal error")          // 500
Response.error(HttpStatus.I_AM_A_TEAPOT, "Teapot")  // custom error
```
