[← Back to README](./../README.md)

# Architecture

## Core Rendering System

JWeb renders HTML from a tree of `Element` objects via a Virtual DOM.

| Interface / Class | Package | Purpose |
|-----------|---------|---------|
| `Renderable` | `core/` | Base: `VNode toVNode()` |
| `Element` | `core/` | `Renderable` + `default String toHtml()`. Functional interface — lambdas like `() -> new VFragment(...)` are valid Elements |
| `Template` | `template/` | Reusable UI components: `Element render()` (plus lifecycle hook defaults — see caveat below) |
| `Component` | `core/` | Marker interface (`extends Element`, no members) |
| `Page` | `core/` | Marker interface (`extends Element`, no members) — it does **not** define `head()`/`body()` |
| `RawContent` | `core/` | Unescaped payloads: `RawContent.html(s)`, `.json(s)`, `.text(s)`, `.of(s, contentType)`; `isJson()`, `isHtml()` |
| `ErrorBoundary` | `core/` | Fluent error containment: `ErrorBoundary.of(supplier).fallback(fn).onError(consumer)`; statics `wrap`, `silent`, `withMessage` |

```
Element (interface)
    └── Template (interface) — render() returns Element
            ├── your pages/components
            └── layouts
```

## Virtual DOM

`vdom/` defines a **sealed** hierarchy:

| VNode Type | Purpose |
|------------|---------|
| `VElement` | HTML element with tag, attributes, children. Immutable; `withAttribute`/`withChild` copy-on-write |
| `VText` | Escaped text (`& < > " '` escaped — XSS-safe by default) |
| `VRaw` | Raw/unescaped HTML (use with caution) |
| `VFragment` | Groups nodes without a wrapper element |

Pipeline: `Template.render()` → `Element.toVNode()` → `VNode.toHtml()` → HTML string.

Mechanics worth knowing:

- Every DSL factory funnels through `Tag.create(name, Object... items)`. `Attr`, `Attributes`, and
  `InlineStyle` args become attributes; everything else becomes children (`VNode` as-is, `Element`
  via `toVNode()`, `String` as escaped `VText`, other objects via `toString()`). One level of
  `Iterable` is flattened.
- **Void elements** (`img`, `br`, `input`, `hr`, `meta`, `link`, …) throw
  `IllegalArgumentException` if given children.
- Escaping asymmetry: `VText` escapes single quotes; `VElement` attribute values escape `& < > "`
  but **not** `'` — always use double quotes in generated attribute contexts.

## Application Entry Point

```java
@JWebApplication  // = @SpringBootApplication + @ComponentScan("com.osmig.Jweb")
                  //   + @PropertySource("classpath:jweb.yaml")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

## Auto-Configuration Chain

1. `@JWebApplication` triggers Spring Boot + component scanning of `com.osmig.Jweb`.
2. `JWebAutoConfiguration` creates the `JWeb` bean and calls `configure(app)` on every
   `JWebRoutes` bean. **Order across multiple `JWebRoutes` beans is unspecified** — if
   registration order matters (middleware!), use a single `Routes` class.
3. `JWebConfiguration` connects MongoDB (when `jweb.data.enabled: true`) via an
   `ApplicationRunner`, exposes `Router` and `MiddlewareStack` beans, and registers static
   resource handlers (`classpath:/static/`, `classpath:/public/`).
4. `JWebController` handles HTTP requests via `@RequestMapping("/**")`.

## The Three Routing Systems

JWeb has **three separate dispatch mechanisms**. Understanding which one serves a request is the
single most important thing about the architecture:

| System | Registered via | Dispatched by | Path params | Middleware applies? |
|--------|---------------|---------------|-------------|---------------------|
| **Page routes** | `app.layout(...).pages("/", HomePage.class, ...)` or `app.scanPages(pkg)` | `PageRegistry` — exact-string HashMap lookup | ❌ none | ✅ yes (GET/HEAD only; other methods → 405) |
| **Router routes** | `app.get/post/put/delete/patch(path, handler)` | `Router` — linear scan, first match wins; HEAD served by GET | ✅ `:name` syntax | ✅ yes |
| **@REST controllers** | `@REST("/api/v1/...")` classes | **Spring MVC** (not JWeb at all) | ✅ `{name}` Spring syntax | ❌ JWeb middleware does not apply — use Spring mechanisms |

Key consequences:

- `JWebController` **hard-excludes** paths starting with `/api/v` (plus `/h2-console`, exact
  `/jweb`, and WebSocket upgrades) and returns before touching JWeb routing. `@REST` controllers
  must therefore live under `/api/v1/...` (or any `/api/v*` prefix). Conversely,
  `app.get("/api/v1/anything", ...)` on the JWeb router **silently never fires**.
- Page routes are matched by exact string only: no parameters and no trailing-slash tolerance.
  They answer GET/HEAD; other methods get a 405. Use Router routes for anything dynamic.
- Page routes, Router routes, and 404s all run through the middleware stack, so auth, CSRF,
  headers, logging, and metrics see every JWeb-dispatched request. A method mismatch on a
  Router path returns **405 with an `Allow` header** instead of 404.

## Request Flow (verified against `JWebController`)

```
HTTP Request
  → JWebController (@Controller, handles /**)
    → bypass?  path startsWith /api/v | /h2-console, path == /jweb, Upgrade: websocket
         → return (handled by Spring MVC / WebSocket instead)
    → PageRegistry.findByPath(path)          [O(1) exact match]
         → found: instantiate page (cached no-arg ctor) → render → wrap in layout
                  → inject prefetch + hydration scripts → 200 text/html
                  (middleware NOT executed on this path)
    → Router.match(method, path)             [O(n) linear scan]
         → no match: 404 ErrorPage           (middleware NOT executed)
         → match:  StateManager.createContext()
                   → MiddlewareStack.execute(request, handler)
                   → processResult(...)
                   → finally: context.clearContext()
```

`processResult` dispatch (in order):

1. `null` → empty 200
2. `ResponseEntity` → returned as-is
3. `RawContent` → `application/json` if `isJson()` else `text/html`
4. `Element` → `toHtml()` + hydration injection, 200 `text/html`
5. `String` → 200 **`text/html`** (a JSON string still gets text/html — return `RawContent.json(...)` or `Response.json(...)` instead)
6. anything else → `result.toString()` labeled `application/json` — **POJOs are not Jackson-serialized**; use `Response.json(obj)`

Errors from handlers render `ErrorPage.render(500, ...)` **including the full stack trace** —
there is currently no dev/prod switch. See [Known Issues](./known-issues.md).

## Components and Templates

### Template Interface

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

// Usage — Templates are Elements, so compose freely:
div(class_("container"),
    new Card("Welcome", "Hello, World!"),
    new Card("Features", "Build apps in pure Java")
)
```

`Template` declares default lifecycle hooks — `beforeRender(Request)`, `afterRender(Request)`,
`onMount()`, `onUnmount()`, `pageTitle()`, `metaDescription()`, `extraHead()`, `scripts()`,
`cacheable()`, `cacheDuration()`.

> ✅ The controller invokes all of these for page routes and for Templates returned from
> Router handlers: `beforeRender(request)` runs before `render()`, `pageTitle()` overrides the
> layout title and `<title>`, `metaDescription()`/`extraHead()` inject into the head,
> `scripts()`/`onMount()`/`onUnmount()` inject before `</body>`, and
> `cacheable()`/`cacheDuration()` drive the Cache-Control header.

### Layouts

A layout is a `Template` whose constructor accepts the page content. The controller looks for a
`(String title, Element content)` constructor first, then `(Element content)`:

```java
public class Layout implements Template {
    private final String title;
    private final Element content;

    public Layout(String title, Element content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Element render() {
        return html(
            new Head(title),
            body(new Nav(), main(content), new Footer())
        );
    }
}

// ORDER MATTERS: layout() must come before pages() —
// PageRegistry captures the default layout at registration time.
app.layout(Layout.class)
   .pages("/", HomePage.class, "/about", AboutPage.class);
```

## JWeb API (route registration)

```java
JWeb app = JWeb.create();                          // done for you by auto-config

app.use(Middleware mw)                             // global middleware
app.useIf(boolean condition, Middleware mw)        // conditional registration
app.use(String pathPrefix, Middleware mw)          // prefix match — startsWith, NOT globs
app.useForMethods(List.of("POST","PUT"), mw)       // method-scoped

app.get(String path, Supplier<Element> page)       // simple page, no request access
app.get(String path, RouteHandler handler)         // handler: Object handle(Request)
app.post(String path, RouteHandler handler)
app.post(String path, Function<Request,Object> fn) // overload — lambdas may need a
                                                   // (RouteHandler) cast to disambiguate
app.put(String path, RouteHandler handler)
app.delete(String path, RouteHandler handler)
app.route(String path, Supplier<? extends Page>)   // alias for get
app.addRoute(Route route)

app.layout(Class<? extends Template> layout)
app.pages(Object... pathsAndPages)                 // alternating "/path", PageClass.class
```

Not available (as of now): `patch()`, `head()`, `options()`, route groups, glob middleware
paths. A method mismatch on an existing path yields **404**, not 405.

### Route path syntax (Router routes)

Path parameters use **colon syntax** — `{name}` braces are *not* supported here (they belong to
`@REST`/Spring routes):

```java
app.get("/users/:id", req -> { String id = req.param("id"); ... });
app.get("/files/*", handler);   // '*' segment matches anything
```

## Middleware

```java
// Functional interface: Object handle(Request request, MiddlewareChain chain)
app.use(Middlewares.logging());
app.use(Middlewares.csrf());                 // CSRF middleware lives in Middlewares, not Csrf
app.use(Middlewares.rateLimit(100, 60_000));
app.use(Middlewares.securityHeaders());

// Path-scoped: plain prefix or glob — "/api", "/api/**", "/files/*.png" all work
app.use("/api", Jwt.protect());
app.use("/admin", Auth.requireRole("admin"));

// Composition helpers
Middleware.before(req -> ...);
Middleware.after((req, result) -> ...);
Middleware.catchErrors((req, ex) -> fallbackResult);
Middleware.when(req -> req.isAjax(), mw);
```

Built-ins in `Middlewares`: `logging()`, `cors(...)` (3 overloads), `csrf(...)`,
`rateLimit(max, windowMs)`, `securityHeaders(...)`, `timing()`, `requestId()`,
`cacheControl(...)`, `noCache()`, `staticCache(...)`, `etag()`, `compressionHeaders()`.

> Header middlewares queue their headers on the `Request` (`req.responseHeader(...)`) and the
> controller applies them to the final response — so they work for `Element`, `String`, and
> `ResponseEntity` results alike, on page routes, Router routes, and 404s.

More middleware factories elsewhere: `Auth.requireAuth()/requireRole(...)/...` (security),
`Jwt.protect()/optional()`, `Cors.allowAll()/origins(...)/configure()`,
`RateLimit.perMinute(n)...build()`, `ErrorHandler.errorHandling()`, `Metrics.middleware()`,
`I18n.middleware()`.

## Request API (`server/Request.java`)

**Path parameters** (Router routes only):

```java
String id = req.param("id");                    // null if absent
String v  = req.param("id", "default");
Integer i = req.paramInt("id");                 // null on absent/bad input
int p     = req.paramInt("page", 1);            // safe with default
Long l    = req.paramLong("id");   long l2 = req.paramLong("id", 0L);
Double d  = req.paramDouble("score");
Boolean b = req.paramBool("active");            // true for "true|1|yes|on"
UUID u    = req.paramUUID("id");
Optional<String>  o1 = req.paramOpt("name");    // also paramIntOpt/paramLongOpt/paramUUIDOpt

// require* throw IllegalArgumentException when missing:
String name = req.requireParam("name");
int page    = req.requireParamInt("page");
long count  = req.requireParamLong("count");
UUID rid    = req.requireParamUUID("id");
```

**Query parameters:**

```java
String q  = req.query("q");
int page  = req.queryInt("page", 1);            // safe overload
Integer n = req.queryInt("page");               // null when missing or invalid
Long off  = req.queryLong("offset");            // same
Boolean v = req.queryBool("verbose");
Map<String, String[]> all = req.queryParams();  // note: String[] values
```

**Headers, body, form:**

```java
String ct = req.header("Content-Type");
Map<String, String> headers = req.headers();    // first value per name
String body = req.body();                       // raw body, read verbatim (cached)

// There is NO req.bodyAs(Class) — deserialize with the Json utility:
User user = Json.parse(req.body(), User.class);

String name = req.formParam("name");
Map<String, String[]> form = req.formParams();
```

**Cookies, session, attributes:**

```java
String token = req.cookie("auth_token");
Map<String, String> cookies = req.cookies();
HttpSession session = req.session();            // creates; session(false) does not
T val = req.sessionAttr("user");  req.sessionAttr("user", value);
T attr = req.attr("key");         req.attr("key", value);   // request-scoped
```

**Request/client info:**

```java
req.method(); req.path(); req.url(); req.queryString(); req.contentType();
req.ip();            // honors X-Forwarded-For
req.userAgent();
req.isAjax(); req.acceptsJson(); req.acceptsHtml();
req.raw();           // escape hatch: HttpServletRequest
```

## Response API (`server/Response.java`)

All factories return Spring `ResponseEntity` values:

```java
// HTML
Response.html(element)                          // 200, rendered Element
Response.html(htmlString)
Response.html(HttpStatus.OK, element)

// JSON
Response.json(object)                           // 200, serialized via Json.stringify
Response.json(HttpStatus.CREATED, object)       // ← use this for custom status
Response.json().put("status", "ok").put("count", 42).build()
// ⚠️ Response.json(obj).status(201) does NOT exist — pass the status as the first argument.

// Text / redirects
Response.text("plain text")
Response.redirect("/dashboard")                 // 302
Response.redirect("/new-url", true)             // 301
Response.seeOther("/other")                     // 303

// Success
Response.ok()                                   // ResponseBuilder: .header().contentType().body()/build()
Response.created("/api/users/123")              // 201 + Location
Response.noContent()                            // 204

// Errors (JSON bodies: {error:true, status, message})
Response.badRequest("Invalid input")            // 400
Response.unauthorized("Login required")         // 401
Response.forbidden("Access denied")             // 403
Response.notFound("Not found")                  // 404
Response.serverError("Internal error")          // 500
Response.error(HttpStatus.I_AM_A_TEAPOT, "..")  // any status
Response.error(418, "..")
```

## Error Handling

- `JWebException` — status-carrying runtime exception with factories: `badRequest`, `unauthorized`,
  `forbidden`, `notFound`, `notFound(resource, id)`, `conflict`, `unprocessable`,
  `tooManyRequests`, `serverError`, `serviceUnavailable` (+ optional error codes/causes).
- `ValidationException` — 422 wrapper around a `ValidationResult`.
- `ErrorResponse` — JSON error payload builder (`ErrorResponse.from(ex, path)`).
- `ErrorHandler` — converts exceptions to JSON/HTML responses; **must be registered manually**:
  `app.use(ErrorHandler.errorHandling())`. Without it, any exception becomes a generic 500 page
  (`ErrorPage`) and `JWebException` status codes are ignored.

## Health & Metrics

```java
Health.register("db", () -> HealthStatus.up("connected").withDetail("latencyMs", 4));
Health.registerLiveness("app", () -> HealthStatus.up());
Health.setupEndpoints(app);      // GET /health, /health/live, /health/ready (200/503 JSON)

Metrics.counter("orders.placed").increment();
Metrics.gauge("queue.depth", queue::size);
Metrics.timer("report.render").record(() -> renderReport());
app.use(Metrics.middleware());   // http.requests.total/success/error + duration
Metrics.setupEndpoint(app);      // GET /metrics — JSON, or Prometheus with Accept: text/plain
```

Metrics middleware sees page-route and 404 traffic too (everything runs through the stack).

## Server-side HTTP Client (`http/Fetch`)

```java
FetchResult res = Fetch.get("https://api.example.com/users")
    .bearer(token)
    .timeout(Duration.ofSeconds(10))
    .send();                       // throws Fetch.FetchException on network errors

if (res.isOk()) {
    User u = res.as(User.class);
    Map<String,Object> m = res.asMap();
}
Fetch.post(url).json(payload).send();
Fetch.post(url).form(Map.of("k", "v")).send();
CompletableFuture<FetchResult> f = Fetch.get(url).sendAsync();
```

> `FetchResult.asList(User.class)` resolves the element type at runtime and returns real
> `User` instances. `res.as(new TypeReference<List<User>>() {})` also works.

## Context (`context/`)

Thread-local dependency passing without prop drilling:

```java
static final ContextKey<User> CURRENT_USER = Context.key("currentUser");

Context.provide(CURRENT_USER, user, () -> page.render());   // scoped provide
User u = Context.use(CURRENT_USER);                          // throws if absent
Optional<User> maybe = Context.find(CURRENT_USER);
User or = Context.useOrDefault(CURRENT_USER, guest);
```

> `Context` is cleared by `JWebController` at the end of every request. The scoped
> `provide(..., runnable)` forms are still the cleanest pattern, but bare writes no longer
> leak across pooled servlet threads.

## Navigation (`navigation/`)

`Link` — fluent SPA-style anchor builder rendering `data-*` attributes:

```java
Link.to("/about", "About")                          // simple (returns Element)
Link.to("/docs").text("Docs").prefetch()            // builder form (1-arg returns Link)
    .swap("#content").activeClass("active")
Link.navLink("/about", "About", req.path())         // auto 'active' class
Link.to("/ext").newTab()                            // rel="noopener noreferrer"
```

`Navigation.script()` injects the client runtime that powers those attributes: hover prefetch,
click interception, partial swaps (`innerHTML|outerHTML|beforeend|afterbegin`), View Transitions
when available, and `window.JWebNav.{navigate,prefetch,clearCache}`. It must be added to the
layout explicitly — only the simpler `Prefetch` script is auto-injected by the controller.

## Framework Structure (237 framework files across 45 packages + ~108 app files)

```
framework/
├── accessibility/      # A11y — WCAG 2.1 HTML auditor (validate/validateHtml)
├── api/                # @REST, @GET, @POST, @UPDATE, @PATCH, @DEL (Spring meta-annotations)
├── async/              # Jobs, Scheduler, BackgroundTask, Suspense
├── attributes/         # Attr (record), Attributes (fluent builder), Attrs (@Deprecated)
├── cache/              # Cache<K,V> — TTL cache with named/global instances
├── cli/                # JWebCli (scaffolding CLI), Templates (package-private)
├── config/             # JWebConfiguration — Mongo init, Router/MiddlewareStack beans, static resources
├── context/            # Context, ContextKey — thread-local DI
├── core/               # Element, Renderable, Component, Page, RawContent, ErrorBoundary
├── db/mongo/           # Mongo, Doc, Schema, MongoQuery, MongoUpdate, MongoDelete
├── dev/                # HotReload, DevController (/__jweb_dev), DevServer (file watcher)
├── docs/               # 15 internal .md reference docs (shipped in jar)
├── elements/           # HTML DSL — 24 modules (El facade, Elements, Tag, per-category modules)
├── error/              # ErrorHandler, ErrorResponse, JWebException, ValidationException
├── events/             # Event, DomEvent, EventHandler, EventRegistry (server-side events)
├── forms/              # Form (fluent form builder), FormModel (POJO → form + binding)
├── health/             # Health, HealthCheck, HealthStatus
├── http/               # Fetch, FetchResult — server-side HTTP client
├── hydration/          # HydrationData (__JWEB_DATA__), VNodeSerializer
├── i18n/               # I18n (locale resolution + middleware), Messages (in-memory bundles)
├── js/                 # JavaScript DSL — 43 modules (JS, Actions, Async, Events, JS*)
├── layout/             # Layout — ~45 static layout helpers (container/grid/stack/card/...)
├── metrics/            # Metrics — counters/gauges/timers + /metrics endpoint
├── middleware/         # Middleware, MiddlewareChain, MiddlewareStack, Middlewares
├── navigation/         # Link (SPA anchors), Navigation (client nav runtime)
├── openapi/            # OpenApi generator + @ApiDoc/@ApiParam/@ApiBody/@ApiResponse
├── performance/        # Prefetch — hover-prefetch script (auto-injected)
├── portal/             # Portal — render content into named outlets
├── ref/                # Ref — element references producing JS snippets
├── routing/            # Router, Route (:param regex), RouteHandler, PageRoute, PageRegistry, @Page
├── security/           # Auth, Principal, Jwt, Password, Cors, Csrf, CsrfToken, RateLimit, OAuth2
├── server/             # Request, Response, Cookie, JWebController, JWebEventController, ErrorPage
├── sse/                # SseBroadcaster, SseEmitter, SseEvent
├── state/              # State<T>, StateHooks, StateBinding, StateManager, ComponentRegistry
├── styles/             # CSS DSL — 35 modules (CSS facade, Style, units, colors, at-rules, Theme…)
├── template/           # Template interface
├── testing/            # JWebTest, MockRequest, MockSession, TestClient
├── transition/         # Transition (show/hide animation), TransitionBuilder (CSS transitions)
├── ui/                 # UI (buttons/cards/modals/tabs/tables…), Toast
├── upload/             # FileUpload, UploadedFile
├── util/               # Json, Log, YamlPropertySourceFactory
├── validation/         # Validator, Validators, ValidationResult, FormValidator, FieldValidator, NumberValidators
├── vdom/               # VNode (sealed), VElement, VText, VRaw, VFragment
├── websocket/          # JWebSocketConfig (/jweb), JWebSocketHandler, WebSocketMessage
├── JWeb.java           # Route/middleware registration facade
├── JWebApplication.java
├── JWebAutoConfiguration.java
└── JWebRoutes.java     # void configure(JWeb app)
```

For state, events, hydration, WebSocket, and SSE details, see
[State & Realtime](./state-and-realtime.md). For everything that is knowingly unwired or buggy,
see [Known Issues](./known-issues.md).
