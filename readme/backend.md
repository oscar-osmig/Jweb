[← Back to README](./../README.md)

# Backend

## Fragments — server-driven UI (zero JavaScript)

Any route can return a fragment (an Element without `html`/`body`); the framework serves
it clean (no script injection). Declarative swap attributes fetch and insert fragments —
wrapped in a View Transition when the browser supports it:

```java
// Server: a route returning a fragment
app.get("/products/list", req -> productList(req.queryInt("page", 1)));

// Client: no JS written — the JWeb runtime handles it
button(swap("/products/list?page=2", "#products"),
       swapPush("/products?page=2"),                  // optional history entry
    "Next page")

// Progressive forms: POST + swap the response fragment
form(swapForm("/comments", "#comment-list"),
    Input.text("message"), button("Post"))
```

`swapOuter(url, sel)` replaces the target element itself; `swapMorph(url, sel)` **morphs
the target in place** — unchanged nodes are kept, so focus, scroll position and in-progress
input survive the update (best for lists/forms that refresh under the user). Back/forward
re-swap via history state; a `jweb:swap` event fires after each swap, with the address bar
already showing the URL the swap navigated to. Going back *past* the first swap reloads:
that history entry is the document as the server first sent it.

`data-swap-cache="<ms>"` (or `navigate(url).cache(ms)` in the JS DSL) reuses a fetched
fragment for that long, out of the same store
[`prefetch(selector)`](./javascript-dsl.md#behaviors) warms on hover — so a link the
pointer has rested on swaps with no round trip.

## Streaming SSR

Wrap a page in `Streamed.of(() -> ...)` and the shell flushes immediately; every
`Suspense` block renders its loading placeholder instantly and **streams its real HTML
into the page the moment its data resolves** — blocks load in parallel, arrive in
completion order, and no JavaScript is written:

```java
app.get("/dashboard", req -> Streamed.of(() -> new Layout("Dashboard", div(
    header(),                                        // paints immediately
    Suspense.of(() -> reports.slowQuery())           // streams in when ready
        .loading(() -> spinner("Crunching numbers..."))
        .render(data -> reportTable(data))
)).render()));
```

Measured on the demo route (`/demo/streaming`): TTFB ~35ms, total = the slowest block.
The page must be built inside the supplier (the element DSL evaluates eagerly).
Outside a streamed page, `Suspense` behaves exactly as before.

## Typed Routes — compile-time checked URLs

Declare a route's path and parameter types once; registration and every link are then
type-checked (String, Integer, Long, Double, Boolean, UUID):

```java
static final TypedRoute.Path1<Long> USER = TypedRoute.path("/users/:id", Long.class);
static final TypedRoute.Path2<String, Integer> POST =
    TypedRoute.path("/blog/:slug/comments/:page", String.class, Integer.class);

app.get(USER, (req, id) -> userPage(id));             // id is already a Long
app.get(POST, (req, slug, page) -> comments(slug, page));

a(href(USER.url(42L)), "Profile")                        // "/users/42", URL-encoded
```

Bad parameter values (e.g. `/users/abc`) return 400, not 500.

Query parameters get the same treatment:

```java
static final QueryParam<Integer> PAGE = QueryParam.of("page", Integer.class).orElse(1);
static final QueryParam<Long>    USER = QueryParam.of("userId", Long.class).required();

app.get("/products", req -> productList(PAGE.from(req)));   // parsed, defaulted, no null checks
```

## SEO (`seo/Seo`)

```java
head(metaCharset(), metaViewport(),
    Seo.of("JWeb — Java Web Framework", "Build complete web apps entirely in Java")
        .url("https://jweb.dev/").image("https://jweb.dev/og.png").siteName("JWeb")
        .render())
```

Emits title, meta description, canonical link, Open Graph, and Twitter card tags —
all consistent from one declaration.

## Image Optimization

`GET /jweb/img?src=/static/photo.jpg&w=400` serves the image scaled to 400px wide
(aspect kept), immutably cached, restricted to classpath `static/`/`public/` resources.
Zero dependencies (ImageIO). Use directly in the DSL:

```java
img("/jweb/img?src=/static/hero.jpg&w=800")
```

## Page routes with parameters

`app.pages(...)` paths take `:param` segments and a `*` wildcard, like router routes. Exact
paths still resolve by lookup and win over patterns. The page reads its parameters in
`beforeRender(req)` or through the `pathParam(name)` / `params()` accessors:

```java
app.layout(Layout.class).pages(
    "/", HomePage.class,
    "/users/:id", UserPage.class,
    "/posts/:category/:slug", PostPage.class,
    "/docs/*", DocsPage.class);

public class UserPage implements Template {
    private User user;

    @Override public void beforeRender(Request req) { user = users.find(req.requireParamLong("id")); }
    @Override public Element render() { return div(h1(user.name()), p("id " + pathParam("id"))); }
    @Override public String pageTitle() { return user.name(); }      // plain String, null = default
    @Override public String description() { return user.bio(); }     // <meta name="description">
    @Override public boolean cacheable() { return false; }
}
```

(`title()` is not the hook's name on purpose: a `Template.title()` would shadow the
`title(...)` element factory inside every Template.)

## Guards

A guard runs in front of every route under a path pattern — page routes, router routes
**and `@REST` controllers** — after the middleware stack and before the handler. It returns
`null` to pass, or the response to send instead; guards run in registration order and the
first answer wins.

```java
app.guard("/admin/**", req -> req.principal() != null ? null : Response.redirect("/admin/login"));
app.guard("/admin/**", Auth.requireLogin("/admin/login"));   // the same as sugar; the login page passes
app.guard("/admin/**", Auth.requireRole("admin"));            // any Middleware: its response or 401/403 answers
app.guard("/api/v1/admin/**", Jwt.protect());
```

Patterns follow `app.use(path, ...)`: a plain prefix, `/prefix/**`, or a glob with `*` in one
segment. `req.principal()` is what `Auth.login` stored. A `JWebException` thrown by a guard or
middleware now answers with its own status (401/403/...) instead of a 500 page.

## Action routes — typed parameters

A mutation reachable from a link **or** a form: `app.action` registers GET and POST, and binds
the query/form parameters to a record before the handler runs. `app.get(path, Type.class, ...)`
and `app.post(...)` do the same for one method; `req.bind(Type.class)` does it by hand.

```java
import jweb.api.Range;

enum Setting { EBB, SLACK, FLOOD }
record Vane(@Range(min = 1, max = 3) int n, Setting set) {}
record Pref(Optional<Setting> sound, Optional<Boolean> motion) {}

app.action("/act/tide/vane", Vane.class, (req, vane) -> {      // GET /act/tide/vane?n=2&set=flood
    Visit v = Session.of(Visit.class, req);
    v.vanes.put(vane.n(), vane.set());
    return Response.redirect("/worlds/tide-archive").anchor(v.vanesAligned() ? "content" : "chamber");
});

app.action("/act/pref", Pref.class, (req, pref) -> {
    pref.sound().ifPresent(s -> Session.of(Visit.class, req).soundOn = s != Setting.EBB);
    return Response.redirectBack(req);                            // the Referer (same site), or "/"
});
```

Binding rules: `String`, the primitives and boxes, `UUID`, enums (by name, case-insensitive),
`Optional<T>` (optional), `List<T>` (every value of that name). Everything not Optional is
required — a primitive `boolean` reads `false` when absent (an unchecked checkbox). `@Range`,
`@Length`, `@Pattern` (all in `jweb.api`) validate; a record's compact constructor may throw
`IllegalArgumentException` too. Any failure is a **400 with a message naming the component**
(an HTML page for browsers, JSON for `Accept: application/json`) — never an exception page.

## REST API (`@REST` controllers)

A controller is `jweb.*` and `java.*` only. `@REST` is a meta-annotated `@RestController`,
so component scanning registers it; the parameter annotations are resolved by JWeb's own
argument resolver (Spring's `@RequestParam` & co. target parameters only and cannot be
meta-annotated, so they are re-implemented, not aliased):

| Annotation | Purpose |
|------------|---------|
| `@REST("/api/v1/x")` | Controller with base path (must start with `/api/v`) |
| `@GET` `@POST` `@UPDATE` `@PATCH` `@DEL` | One per method; `@GET("/{id}")` adds a path |
| `@Param` | Path placeholder `{id}` → int, long, String, UUID, enum... (name = the parameter's) |
| `@Query` | Query/form parameter; `value`, `defaultValue`, `required`; `Optional<T>`, `List<T>` |
| `@Body` | JSON body → record, POJO, `Map`, `List` (a `String` gets the raw text) |
| `@Header`, `@Cookie` | A header / cookie value, same options as `@Query` |
| `@Upload("file")` | Multipart file → `jweb.UploadedFile` (never null — `isEmpty()`), or `List<UploadedFile>` |
| `jweb.Request` | A bare `Request` parameter is injected (path params included) |
| `@Component`, `@Value` | `jweb.api` spellings of the Spring stereotypes for your stores and config values |

```java
import jweb.Request;
import jweb.api.*;

@REST("/api/v1/users")
public class UserApi {

    private final UserStore users;
    public UserApi(UserStore users) { this.users = users; }        // @Component injected

    @GET
    public List<User> getAll() { return users.findAll(); }

    @GET("/{id}")
    public User getById(@Param long id) { return users.find(id); }

    @GET("/search")
    public List<User> search(@Query String q, @Query(value = "limit", defaultValue = "10") int limit) { ... }

    @POST
    public Object create(@Body User user) {
        return Response.created("/api/v1/users/" + users.save(user), user);   // 201 + Location
    }

    @UPDATE("/{id}")
    public User update(@Param long id, @Body User user) { return users.update(id, user); }

    @DEL("/{id}")
    public Object delete(@Param long id) { users.delete(id); return Response.noContent(); }

    @POST("/{id}/avatar")
    public Map<String, Object> avatar(@Param long id, @Upload("file") UploadedFile file) { ... }

    @GET("/whoami")
    public Map<String, Object> whoami(Request req) { return Map.of("ip", req.ip()); }
}
```

Missing or malformed input answers **400 with a message naming the parameter**. Statuses come
from `Response` — `serverError()`, `status(int)`, `unauthorized()`, `forbidden()`,
`tooManyRequests()`, `noContent()`, `created(location[, body])`, `error(code, message)`,
`html(code, element)`, `json(code, body)`, `redirect(url).anchor(..)`, `redirectBack(req)` —
so no `HttpStatus` import is ever needed.

What that implies:

- **Put controllers under `/api/v*`** — `JWebController` explicitly bypasses those paths so
  Spring MVC can serve them. (A JWeb router route under `/api/v1/...` would silently never fire.)
- **Guards apply** (`app.guard("/api/v1/admin/**", ...)`); **middleware does not**
  (`app.use(...)` runs only for what `JWebController` dispatches).
- The Spring spellings (`@PathVariable`, `@RequestBody`, `HttpServletRequest`, ...) still work
  in a controller; they are just no longer needed.

---

## OpenAPI Documentation

```java
import jweb.OpenApi;

OpenApi.create()
    .title("JWeb Example API")
    .version("1.0.0")
    .description("Example REST API built with JWeb")
    .server("http://localhost:8085")
    .bearerAuth()
    .tag("users", "User management")
    .addApi(UserApi.class)
    .addApi(ContactApi.class)
    .mount(app, "/api");
```

`mount(app, "/api")` registers four routes:

| Route | Serves |
|-------|--------|
| `/api/openapi.json` | The OpenAPI 3.0.3 spec |
| `/api/docs` | Swagger UI |
| `/api/redoc` | Redoc |
| `/api/scalar` | Scalar API reference |

Enrich generated docs with annotations:

```java
@ApiDoc(
    summary = "Search users",
    params = { @ApiParam(name = "q", description = "Query string", required = true) },
    responses = {
        @ApiResponse(code = 200, description = "Matches found"),
        @ApiResponse(code = 400, description = "Bad query")
    })
@GET("/search")
public List<User> search(@RequestParam String q) { ... }
```

Notes: the spec is built eagerly at `mount(...)` time from `getDeclaredMethods()`; POJO schemas
degrade to `{"type":"object"}` (no property introspection); `scan(String pkg)` does a real
classpath scan for `@REST`-annotated classes (use it or `addApi(Class)`).

---

## Database Integration (MongoDB)

MongoDB auto-connects when `jweb.data.enabled: true` via `JWebConfiguration.mongoInitializer()`
(an `ApplicationRunner`, so connection happens after context startup). URI/database come from
`jweb.data.mongo.uri` / `jweb.data.mongo.database` (see [Configuration](./configuration.md)).

| Class | Purpose |
|-------|---------|
| `Mongo` | Static facade: connection + CRUD (`save`, `insert`, `find`, `findById`, `update`, `delete`, `count`, `exists`, `deleteById`) + POJO overloads |
| `Doc` | Dynamic document: `of(coll)`, `set(k,v)` (dotted keys nest), typed getters, `toBson()`, `as(Class)` |
| `MongoQuery` | Fluent query builder |
| `MongoUpdate` | Fluent update builder |
| `MongoDelete` | Fluent delete builder |
| `Schema` | Schema/index definitions — validated on `save`/`insert` once registered (see notes) |

### CRUD

```java
import jweb.Mongo;
import jweb.Doc;

// Create
Doc user = Doc.of("users")
    .set("name", "John Doe")
    .set("email", "john@example.com")
    .set("profile.city", "Oslo")             // dotted keys create nested documents
    .set("createdAt", LocalDateTime.now());  // LocalDateTime <-> Date handled automatically
Mongo.save(user);                            // insert (no id) or replace (has id)

// Read by id — use findById, NOT find().where("id", ...)
Doc found = Mongo.findById("users", id);
Optional<Doc> maybe = Mongo.findByIdOptional("users", id);

// Query
List<Doc> adults = Mongo.find("users")
    .where("age").gte(18)
    .where("status", "active")               // 2-arg where = equality
    .orderBy("name")                         // ascending; orderByDesc(...) / .desc()
    .limit(10)
    .toList();

List<Doc> recent = Mongo.find("contacts").orderByDesc("_id").toList();
Doc first = Mongo.find("users").where("email", email).first();

// Richer conditions
Mongo.find("users").where("role").in("admin", "editor")
Mongo.find("users").where("role").notIn("banned")        // notIn, not "nin"
Mongo.find("users").where("bio").regex("java", "i")
Mongo.find("users").where("deletedAt").isNull()
Mongo.find("posts").where("tags").containsAll("java", "web")
Mongo.find("users").or(
    q -> q.where("email", input),
    q -> q.where("username", input))
Mongo.find("users").select("name", "email").skip(20).limit(10).toList();
long n = Mongo.find("users").where("active", true).count();

// Update ("id" IS translated to _id here)
Mongo.update("users")
    .where("id", id)
    .set("name", "Jane Doe")
    .inc("loginCount", 1)
    .push("history", entry)
    .execute();
Mongo.update("users").where("id", id).set("verified", true).upsert().execute();
Doc updated = Mongo.update("users").where("id", id).set("x", 1).returnNew().executeAndGet();

// Delete ("id" IS translated to _id here)
Mongo.delete("users").where("status", "inactive").execute();
Mongo.delete("logs").where("age").gt(30).execute();      // comparisons: gt/gte/lt/lte/ne/in
Mongo.deleteById("users", id);

// POJOs
String id = Mongo.save("users", userPojo);
User u = Mongo.findById("users", id, User.class);
List<User> all = Mongo.find("users").toList(User.class);
```

### Doc getters

`getString`, `getInt`, `getLong`, `getDouble`, `getBoolean` (each with default-value
overloads), `getDateTime` (⚠️ not `getDate`), `getList`, `getDoc`, `get(dotted.path)`, `has`,
`keys`, `toMap`, `as(Class)`.

### Notes

- **`MongoQuery.where("id", ...)` translates `id` → `_id`** (with String→ObjectId
  conversion), matching the update/delete builders. `Mongo.findById(...)` also works.
- **`Schema` definitions are enforced**: `register()` wires the schema into `Mongo`, field
  constraints (`required`/`min`/`max`/`minLength`/`maxLength`/`pattern`/`enum_`/defaults) are
  validated on `Mongo.save`/`insert` (throwing `Schema.ValidationException`), and declared
  indexes plus `unique()` fields are created on connect (`Mongo.ensureIndexes`).
  `Schema.timestamps()` auto-sets `createdAt`/`updatedAt` on save/insert and update.

---

## Security

### Session-based Authentication (`Auth` + `Principal`)

```java
import jweb.Auth;
import jweb.Principal;

// Login — stores principal in session, regenerates the CSRF token
Auth.login(request, Principal.of("admin", "admin@example.com", "admin"));

// Checks
if (Auth.isAuthenticated(request)) {
    Principal user = Auth.getPrincipal(request);
    user.getId(); user.getName(); user.hasRole("admin");
}
Principal user = Auth.requirePrincipal(request);   // throws JWebException.unauthorized
Auth.hasRole(request, "admin");
Auth.hasAnyRole(request, "user", "admin");
Auth.logout(request);                              // invalidates the session
```

**Guards and middleware factories** — as a guard they also cover `@REST` controllers under
the prefix; as middleware they apply to JWeb-router routes and page routes:

```java
app.guard("/admin/**", Auth.requireLogin("/admin/login"));   // 302 → /admin/login; the login page passes
app.guard("/admin/**", Auth.requireRole("admin"));           // 401 then 403
app.guard("/api/v1/admin/**", Auth.requireAnyRole("user", "admin"));

app.use("/dashboard", Auth.requireAuth());            // 401 via JWebException
app.use("/dashboard", Auth.requireAuth("/login"));    // 302 → /login?redirect=<path>
app.use("/super", Auth.requireAllRoles("admin", "superadmin"));

Principal who = req.principal();                      // null when anonymous
```

> `Auth.customAuth(fn)` and `Auth.bearerAuth(fn)` take `Function<Request, Principal>` /
> `Function<String, Principal>` and **reject with 401** when the function returns null (or the
> Bearer header is missing). For optional authentication that attaches a principal without
> rejecting, use `Auth.attachPrincipal(fn)`.

**Principal construction:**

```java
Principal.of("admin-id");
Principal.of("user-id", "john@example.com");
Principal.of("user-id", "john@example.com", "user", "editor");

Principal.builder()
    .id("user-123").name("john@example.com")
    .role("admin").role("editor")
    .attribute("department", "engineering")
    .build();
```

### JWT Authentication

```java
import jweb.Jwt;

Jwt.init("your-256-bit-secret-key-minimum-32-chars");   // or Jwt.init() → env JWT_SECRET

String token = Jwt.create()
    .subject("user123")
    .claim("role", "ADMIN")
    .roles("user", "admin")
    .expiresIn(Duration.ofHours(24))
    .sign();

if (Jwt.isValid(token)) {
    Jwt.Token parsed = Jwt.parse(token);
    parsed.subject(); parsed.claim("role"); parsed.roles(); parsed.isExpired();
}

app.use("/api", Jwt.protect());          // 401 on missing/expired/invalid Bearer token
app.use("/api", Jwt.optional());         // attaches token when present, never rejects
Optional<Jwt.Token> t = Jwt.getToken(request);   // inside handlers, after protect()
```

### CSRF

```java
// The middleware lives in Middlewares (there is no Csrf.middleware()):
app.use(Middlewares.csrf());
app.use(Middlewares.csrf(Set.of("/webhooks")));   // excluded paths

// In forms / pages:
form(attrs().action("/save").method("POST"),
    Csrf.tokenField(request),                     // hidden _csrf input
    ...)
head(..., Csrf.tokenMeta(request))                // <meta name="csrf-token"> for AJAX

// Manual: Csrf.validate(request) throws CsrfException; token in form param "_csrf"
// or header "X-CSRF-TOKEN"; tokens expire after 30 min; comparison is constant-time.
```

### Password Hashing (BCrypt)

```java
import jweb.Password;

String hashed = Password.hash("userPassword123");
boolean ok = Password.verify("userPassword123", hashed);
Password.needsRehash(hashed);                     // strength upgraded since hashing?
Password.setStrength(12);                          // 4..31, default 12
String generated = Password.generate(16);          // random password
Password.isStrong(pw); Password.validate(pw);      // strength checks
```

### CORS

```java
app.use(Cors.allowAll());
app.use(Cors.origins("https://app.example.com", "https://admin.example.com"));
app.use(Cors.configure()
    .origins("https://app.example.com")
    .methods("GET", "POST")
    .credentials()                     // throws if combined with wildcard origin
    .maxAge(Duration.ofHours(1))
    .build());
```

### Rate Limiting

```java
app.use(RateLimit.perMinute(60).build());                       // by client IP
app.use(RateLimit.perHour(1000).byApiKey("X-Api-Key").build());
app.use(RateLimit.requests(10).per(Duration.ofSeconds(30))
    .byUser()                                                    // falls back to IP
    .forPath("/api/*")                                           // glob supported HERE
    .skipIf(req -> req.ip().equals("127.0.0.1"))
    .onLimit((req, info) -> Response.error(429, "Slow down"))
    .build());
// Emits X-RateLimit-Limit/Remaining/Reset + Retry-After; 429 JSON body on limit.
```

### OAuth2

```java
OAuth2.Provider google = OAuth2.google()
    .clientId(env("GOOGLE_CLIENT_ID"))
    .clientSecret(env("GOOGLE_CLIENT_SECRET"))
    .redirectUri("https://app.example.com/oauth/callback")
    .scopes("openid", "email", "profile")
    .build();

app.get("/oauth/login", req -> Response.redirect(google.authorizationUrl()));

app.get("/oauth/callback", req -> {
    if (!OAuth2.verifyState(req.query("state"))) return Response.forbidden("Bad state");
    var tokens = google.exchangeCode(req.query("code"));
    var info   = google.getUserInfo(tokens.accessToken());
    Auth.login(req, Principal.of(info.id(), info.email(), "user"));
    return Response.redirect("/dashboard");
});
```

Providers: `google()`, `github()`, `discord()`, `microsoft()`, `custom(name)`. State tokens are
in-memory, single-use, 10-minute expiry (not cluster-safe). No PKCE yet.

### Real-World Example: Admin Authentication (from the sample app)

```java
// AdminApi.java — session-based admin auth with env config (jweb.api.Component / Value)
@Component
public class AdminApi {

    @Value("${jweb.admin.token:}") private String adminToken;
    @Value("${jweb.admin.email:}") private String adminEmail;

    public boolean login(Request request, String email, String token) {
        if (adminToken == null || adminToken.isBlank()) return false;   // fail closed
        if (isRateLimited(request.ip())) return false;                  // 5 failures / 15 min
        // Timing-safe comparison so the token can't be guessed byte-by-byte
        if (!constantTimeEquals(adminToken, token)
                || !constantTimeEquals(adminEmail, email)) return false;
        Auth.login(request, Principal.of("admin", email, "admin"));
        return true;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return java.security.MessageDigest.isEqual(
            expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public boolean isAuthenticated(Request request) { return Auth.isAuthenticated(request); }
    public void logout(Request request) { Auth.logout(request); }
    public List<Doc> getMessages() { return Mongo.find("contacts").orderByDesc("_id").toList(); }
}

// Routes.java — one guard protects everything under /only-admin (the login page passes),
// the login form binds to a record, the messages view binds its query, logout leaves a flash
public record Login(Optional<String> email, Optional<String> token) {}
public record MessagesView(Optional<AdminMessagesPage.Order> order,
                           @Range(min = 1, max = 500) Optional<Integer> limit) {}

app.guard("/only-admin/**", Auth.requireLogin("/only-admin/log/in"));

app.get("/only-admin/log/in", ctx -> {
    if (adminApi.isAuthenticated(ctx)) return Response.redirect("/only-admin/messages");
    String notice = Session.of(ctx).flash("notice");              // "You have been signed out."
    return Response.html(new Layout("Admin Login",
        new AdminLoginPage(null, notice, Csrf.getOrCreateToken(ctx)).render()).render());
});

app.post("/only-admin/log/in", Login.class, (ctx, login) -> {
    if (Csrf.isValid(ctx) && adminApi.login(ctx, login.email().orElse(null), login.token().orElse(null))) {
        return Response.redirect("/only-admin/messages");
    }
    return Response.html(new Layout("Admin Login",
        new AdminLoginPage("Invalid email or token", Csrf.getOrCreateToken(ctx)).render()).render());
});

app.get("/only-admin/messages", MessagesView.class, (ctx, view) -> {   // ?order=oldest&limit=20
    var order = view.order().orElse(AdminMessagesPage.Order.NEWEST);
    var messages = order.apply(adminApi.getMessages(), view.limit().orElse(Integer.MAX_VALUE));
    return Response.html(new Layout("Messages - Admin",
        new AdminMessagesPage(messages, Csrf.getOrCreateToken(ctx), order).render()).render());
});

app.post("/only-admin/logout", (RouteHandler) ctx -> {
    if (Csrf.isValid(ctx)) {
        adminApi.logout(ctx);
        Session.of(ctx).flash("notice", "You have been signed out.");
    }
    return Response.redirect("/only-admin/log/in");
});
```

---

## Validation

| Class | Purpose |
|-------|---------|
| `Validator<T>` | Functional interface: `validate(value, field)`; compose with `and()`/`or()`; build ad hoc with `Validator.of(predicate, message)` |
| `Validators` | Static factories — strings: `required`, `notNull`, `minLength`, `maxLength`, `exactLength`, `lengthBetween`, `pattern`, `email`, `alphanumeric`, `alpha`, `numeric`, `url`, `phone`; numbers: `min`, `max`, `range`, `positive`, `negative`, `nonNegative`; collections: `notEmpty`, `minSize`, `maxSize`; objects: `equalTo`, `oneOf`; booleans: `isTrue`, `isFalse` |
| `ValidationResult` | Per-field error map: `isValid()`, `hasErrors([field])`, `getErrors(field)`, `getFirstError(field)`, `getAllErrors()`, `getAllMessages()`, `getFieldsWithErrors()`, `getErrorCount()`, `merge(other)` |
| `FormValidator` | Multi-field form validation (lambda + legacy chained styles) |
| `FieldValidator` | Single string field: `required`, `optional`, length/pattern/format checks, `custom(Validator<String>)`, `check(predicate, msg)` — **no numeric `min`/`max`** |
| `NumberValidators` | `required`, `min`, `max`, `range`, `intRange`, `positive`, `negative`, `nonNegative` |

```java
import static jweb.Validators.*;

// Composable validators
Validator<String> emailValidator = required().and(email()).and(maxLength(100));
ValidationResult result = emailValidator.validate(value, "email");
if (!result.isValid()) {
    List<String> errors = result.getErrors("email");    // NOT result.errors()
    String first = result.getFirstError("email");
}

// Form validation (lambda style)
ValidationResult form = FormValidator.create()
    .field("email", email, f -> f.required().email())
    .field("password", password, f -> f.required().minLength(8))
    .field("website", site, f -> f.optional().url())
    .validate();

// Numeric fields: validate the parsed number with Validators/NumberValidators
ValidationResult age = NumberValidators.range(18, 120).validate(parsedAge, "age");

// Failing fast with a 422
if (!form.isValid()) throw new ValidationException(form);
```

---

## Forms

One form system, `jweb.Form`, and a form is a record. The record declares the fields and
their rules; the same record renders the form, validates the submission, and comes back as
a typed value. See [HTML DSL → Forms](./html-dsl.md#forms-a-record-is-the-form) for the
rendering side.

```java
import jweb.Form;

public record Registration(
    @Form.Required @Form.Label("Full name") String name,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Password @Form.Length(min = 8) String password,
    Plan plan,                                     // an enum renders a <select>
    boolean newsletter) {}

// Render — the CSRF hidden field comes from the current request
form(Registration.class).action("/register").submit("Sign up")

// Bind a submission
app.post("/register", req -> {
    Form.Bound<Registration> submitted = Form.bind(Registration.class, req);
    if (!submitted.ok()) {
        return form(Registration.class).action("/register").errors(submitted).submit("Sign up");
    }
    users.create(submitted.value());
    return Response.redirect("/welcome");
});
```

`Bound` carries `value()`, `errors()` (a `ValidationResult` keyed by component name),
`ok()` and `submitted()` (the raw input, so the re-render shows what the user typed).
`Form.validate(Registration.class, values)` runs the same rules without building the
record; the messages come from `jweb.Validators`.

The 2.2.3 `Form.create()` fluent builder and `FormModel` (POJO ⇄ form, `@FormField`,
`bindFromParameterMap`) are both gone — see
[Migrating to 3.0](./../dsl-simplification-3.md).

---

## File Uploads

```java
import jweb.FileUpload;
import jweb.UploadedFile;

UploadedFile file = FileUpload.getFile(req, "avatar");        // never null (empty wrapper)
Optional<UploadedFile> f = FileUpload.getFileOptional(req, "avatar");
List<UploadedFile> files = FileUpload.getFiles(req, "photos");

var validation = FileUpload.validate(file)
    .required().maxSizeMB(5).imagesOnly();                    // or allowedExtensions("pdf")
if (!validation.isValid()) return Response.badRequest(validation.getFirstError());

Path saved = file.saveTo(Path.of("uploads"));                 // UUID filename
file.saveTo(dir, "custom-name.png");
file.isImage(); file.getExtension(); file.getSize();
```

Requires multipart requests (Spring `MultipartHttpServletRequest`). There is no
`FileUpload.single(...)` — use `getFile`.

An `UploadedFile` component in a form record renders the file input and switches the form
to `multipart/form-data` on its own, so there is no enctype to remember:

```java
public record Attachment(@Form.Required String title, UploadedFile document) {}

form(Attachment.class).action("/upload").submit("Upload")

Form.Bound<Attachment> bound = Form.bind(Attachment.class, req);
if (bound.ok()) bound.value().document().saveTo(Path.of("uploads"));
```

---

## Document → Markdown Conversion (markitdown)

`framework/markdown/Markitdown.java` wraps [Microsoft's markitdown](https://github.com/microsoft/markitdown)
CLI to convert PDF, Word, PowerPoint, Excel, HTML, CSV, images, and more into Markdown.

**Setup** (one-time, project-local — the venv lives in `.tools/`, which is gitignored):

```bash
brew install uv
uv venv .tools/markitdown --python 3.12
uv pip install --python .tools/markitdown/bin/python "markitdown[all]"
```

**Java API:**

```java
import jweb.Markitdown;

String md = Markitdown.convert(Path.of("report.pdf"));   // from disk
String md = Markitdown.convert(bytes, "docx");           // in-memory (temp file under the hood)
Markitdown.isAvailable();                                 // CLI installed?
```

Throws `Markitdown.MarkitdownException` on missing CLI, bad input, non-zero exit, or timeout.

**REST endpoints** (`app/api/MarkitdownApi.java`):

- `GET /api/v1/markitdown/status` → `{"available": true}`
- `POST /api/v1/markitdown/convert` (multipart, field `file`) → `{"filename": ..., "markdown": ...}`
  or `{"error": ...}`

**Config** (`application.yaml`):

```yaml
jweb:
  markitdown:
    command: ${MARKITDOWN_CMD:.tools/markitdown/bin/markitdown}
    timeout-seconds: 120
```

Notes: audio transcription needs `ffmpeg` (`brew install ffmpeg`); multipart upload limit is
25 MB (`spring.servlet.multipart`). Conversions run as a subprocess per request — CPU-bound
for large PDFs.

---

## Internationalization

```java
import jweb.I18n;
import jweb.Messages;

// Register bundles programmatically (no properties-file auto-loading yet)
Messages.setDefaultLocale(Locale.ENGLISH);
Messages.load("en", Map.of("greeting", "Hello, {0}!"));
Messages.load("no", Map.of("greeting", "Hei, {0}!"));

// Resolve locale: ?lang= param → session → cookie → Accept-Language → default
Locale locale = I18n.getLocale(request);
app.use(I18n.middleware());                       // sets the thread-local locale per request

String msg = I18n.t("greeting", user.name());     // uses current locale
String msg2 = Messages.get("no", "greeting", "Ola");
```

> The middleware intentionally leaves the locale set after the chain returns (the framework
> clears it at the end of the request), so lazily-rendered elements can call `I18n.t(...)`
> safely. `I18n.t(request, key, ...)` (explicit request) also works anywhere.

---

## Testing

```java
import jweb.JWebTest;
import jweb.MockRequest;
import jweb.Request;
import jweb.TestClient;

// Unit-test a handler with a mock request
Request req = MockRequest.get("/users/42").pathParam("id", "42").build();
JWebTest.TestResult result = JWebTest.testHandler(handler, MockRequest.get("/x"));
result.getStatus(); result.bodyContains("Hello"); result.bodyAs(User.class);

// Render assertions (they take the rendered HTML string)
JWebTest.assertContains(element.toHtml(), "Welcome");
JWebTest.assertHasClass(element.toHtml(), "card");
JWebTest.assertHasAttribute(element.toHtml(), "data-id", "42");

// Integration tests against a running server
TestClient client = TestClient.localhost(8085).withAuth(token);
client.get("/api/v1/users")
    .assertOk()
    .assertJson()
    .assertBodyContains("john@example.com");
client.post("/api/v1/users").json(newUser).send().assertStatus(201);
```

Note: `JWebTest.test(app, mockRequest)` exercises page routes (`app.pages(...)`) and Router
routes alike — the middleware stack and lifecycle hooks run, and method mismatches return 405,
matching production dispatch.

---

## AI Integration (`framework/ai`)

Chat completions, conversations, tools, and agent loops — **zero extra dependencies**.
JWeb speaks the OpenAI wire format directly over its own `Fetch` client, so it works with
OpenAI, Ollama, Groq, LM Studio, vLLM, or any OpenAI-compatible endpoint. (The Spring AI
starters remain optional in `pom.xml` if you prefer that stack, but nothing requires them.)

**Configure** (`application.yaml` — disabled by default):

```yaml
jweb:
  ai:
    enabled: true
    base-url: ${AI_BASE_URL:https://api.openai.com/v1}   # Ollama: http://localhost:11434/v1
    api-key: ${AI_API_KEY:}                              # blank for local providers
    model: ${AI_MODEL:gpt-4o-mini}                       # Ollama: llama3.2
```

**One-liners:**

```java
String answer = AI.ask("Summarize this: " + text);
```

**Conversations** (history kept between sends):

```java
Chat chat = AI.chat().system("You are a concise support agent");
String a = chat.send("What is JWeb?");
String b = chat.send("Show me an example");   // remembers the topic
```

**Agents with tools** — the model loops: reason → call your Java functions → see
results → repeat until it has an answer:

```java
Tool weather = Tool.of("get_weather", "Get the weather for a city")
    .param("city", "The city name")
    .handler(args -> weatherService.lookup((String) args.get("city")));

Tool search = Tool.of("search_docs", "Search the documentation")
    .param("query", "Search terms")
    .handler(args -> docsIndex.search((String) args.get("query")));

String result = AI.agent()
    .system("You are a support agent for this app")
    .tools(weather, search)
    .maxSteps(8)
    .onStep((step, info) -> Log.info("agent step {}: {}", step, info))
    .run("Should I pack an umbrella for Paris this weekend?");
```

Tool handlers receive the model's arguments as a `Map<String, Object>` and can return any
object (serialized as the tool result). Errors inside a tool are reported back to the model
as text, so the agent can recover. `maxSteps` bounds the loop (`AiException` if exceeded).

**Drop-in chat UI** — a widget plus a ready endpoint (`POST /jweb/ai/chat`, active when
`jweb.ai.enabled=true`, per-session history with a 30-minute TTL):

```java
body(
    ...,
    AiChatWidget.render("Ask JWeb")
)
```

Per-call overrides: `AI.chat().model("gpt-4o").temperature(0.2)` — same on agents.
Everything is testable without a key: see `AiModuleTest`, which scripts an
OpenAI-compatible mock with the JDK's built-in HttpServer.
