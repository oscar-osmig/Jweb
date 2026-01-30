[← Back to README](./../README.md)

# Backend

## REST API

JWeb provides custom annotations that map to Spring's REST annotations for cleaner syntax:

| JWeb Annotation | Maps To | Purpose |
|-----------------|---------|---------|
| `@REST("/path")` | `@RestController` + `@RequestMapping` | Class-level REST controller with base path |
| `@GET` / `@GET("/{id}")` | `@GetMapping` | GET endpoint |
| `@POST` / `@POST("/path")` | `@PostMapping` | POST endpoint |
| `@UPDATE("/{id}")` | `@PutMapping` | PUT endpoint |
| `@PATCH("/{id}")` | `@PatchMapping` | PATCH endpoint |
| `@DEL("/{id}")` | `@DeleteMapping` | DELETE endpoint |

```java
@REST("/api/users")
public class UserApi {

    @GET
    public List<User> getAll() {
        return userService.findAll();
    }

    @GET("/{id}")
    public User getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @POST
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(201).body(userService.save(user));
    }

    @UPDATE("/{id}")
    public User update(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DEL("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
```

---

## Database Integration

### MongoDB

MongoDB auto-connects when `jweb.data.enabled: true` in `application.yaml` via `JWebConfiguration.mongoInitializer()`.

**MongoDB Classes:**

| Class | Purpose |
|-------|---------|
| `Mongo` | Connection management (`connect()`) + CRUD operations (`save()`, `find()`, `update()`, `delete()`, `count()`) |
| `Doc` | Document wrapper (`of(collection)`, `set(key, value)`, `get(key)`, `getString()`, `getInt()`, `getDate()`) |
| `Schema` | Collection schema definition and validation |
| `MongoQuery` | Fluent query builder (`where().eq/ne/gt/gte/lt/lte/in/nin()`, `sort()`, `limit()`, `skip()`, `orderByDesc()`, `toList()`, `first()`) |
| `MongoUpdate` | Fluent update builder (`where().set().inc().unset().execute()`) |
| `MongoDelete` | Fluent delete builder (`where().eq().execute()`) |

```java
import static com.osmig.Jweb.framework.db.mongo.Mongo.*;

// Manual connection (if not using auto-connect)
Mongo.connect("mongodb://localhost:27017", "mydb");

// Create
Doc user = Doc.of("users")
    .set("name", "John Doe")
    .set("email", "john@example.com")
    .set("createdAt", new Date());
Mongo.save(user);

// Query
List<Doc> users = Mongo.find("users")
    .where("age").gte(18)
    .where("status").eq("active")
    .sort("name", 1)
    .limit(10)
    .toList();

// Query with descending order
List<Doc> recent = Mongo.find("contacts")
    .orderByDesc("_id")
    .toList();

// Update
Mongo.update("users")
    .where("id", id)
    .set("name", "Jane Doe")
    .execute();

// Delete
Mongo.delete("users")
    .where("status").eq("inactive")
    .execute();
```

---

## Security

### JWT Authentication

```java
import com.osmig.Jweb.framework.security.Jwt;

Jwt.init("your-256-bit-secret-key-minimum-32-chars");

String token = Jwt.create()
    .subject("user123")
    .claim("role", "ADMIN")
    .expiresIn(Duration.ofHours(24))
    .sign();

if (Jwt.isValid(token)) {
    Jwt.Token parsed = Jwt.parse(token);
    String userId = parsed.subject();
    String role = parsed.claim("role");
}

app.use("/api/**", Jwt.protect());
```

### Session-based Authentication

JWeb provides a comprehensive session-based authentication system with `Auth` and `Principal`:

```java
import com.osmig.Jweb.framework.security.Auth;
import com.osmig.Jweb.framework.security.Principal;

// Login - stores principal in session, regenerates CSRF token
Auth.login(request, Principal.of("admin", "admin@example.com", "admin"));

// Check authentication
if (Auth.isAuthenticated(request)) {
    Principal user = Auth.getPrincipal(request);
    String id = user.getId();
    String name = user.getName();
    boolean isAdmin = user.hasRole("admin");
}

// Require principal (throws JWebException if not authenticated)
Principal user = Auth.requirePrincipal(request);

// Role checking
Auth.hasRole(request, "admin");           // single role
Auth.hasAnyRole(request, "user", "admin"); // any of roles

// Logout - invalidates session
Auth.logout(request);
```

**Auth Middleware Factories:**

```java
// Require any authenticated user
app.use("/dashboard", Auth.requireAuth());

// Require auth with redirect to login page on failure
app.use("/dashboard", Auth.requireAuth("/login"));

// Require specific role
app.use("/admin", Auth.requireRole("admin"));

// Require any of the listed roles
app.use("/api", Auth.requireAnyRole("user", "admin"));

// Require all listed roles
app.use("/superadmin", Auth.requireAllRoles("admin", "superadmin"));

// Custom auth check
app.use("/custom", Auth.customAuth(request -> {
    // return true if authorized
    return Auth.isAuthenticated(request) && someOtherCheck(request);
}));

// Bearer token auth
app.use("/api", Auth.bearerAuth(token -> validateJwtToken(token)));
```

**Principal Builder Pattern:**

```java
// Simple factory methods
Principal admin = Principal.of("admin-id");
Principal user = Principal.of("user-id", "john@example.com");
Principal userWithRoles = Principal.of("user-id", "john@example.com", "user", "editor");

// Builder for complex principals with attributes
Principal principal = Principal.builder()
    .id("user-123")
    .name("john@example.com")
    .role("admin")
    .role("editor")
    .attribute("department", "engineering")
    .attribute("level", "senior")
    .build();
```

### Password Hashing

```java
import com.osmig.Jweb.framework.security.Password;

String hashed = Password.hash("userPassword123");
boolean isValid = Password.verify("userPassword123", hashed);
```

### Real-World Example: Admin Authentication

This is the actual pattern used in the JWeb sample application for admin authentication:

```java
// AdminApi.java - session-based admin auth with env config
@Component
public class AdminApi {

    @Value("${jweb.admin.token:}")
    private String adminToken;

    @Value("${jweb.admin.email:}")
    private String adminEmail;

    public boolean login(Request request, String email, String token) {
        if (adminToken == null || adminToken.isBlank()) return false;
        if (!adminToken.equals(token) || !adminEmail.equals(email)) return false;
        Auth.login(request, Principal.of("admin", email, "admin"));
        return true;
    }

    public boolean isAuthenticated(Request request) {
        return Auth.isAuthenticated(request);
    }

    public void logout(Request request) {
        Auth.logout(request);
    }

    public List<Doc> getMessages() {
        return Mongo.find("contacts").orderByDesc("_id").toList();
    }
}

// Routes.java - protected admin routes
app.get("/only-admin/log/in", ctx -> {
    if (adminApi.isAuthenticated(ctx)) {
        return Response.redirect("/only-admin/messages");
    }
    return Response.html(new Layout("Admin Login",
        new AdminLoginPage().render()
    ).render());
});

app.post("/only-admin/log/in", (RouteHandler) ctx -> {
    if (adminApi.login(ctx, ctx.formParam("email"), ctx.formParam("token"))) {
        return Response.redirect("/only-admin/messages");
    }
    return Response.html(new Layout("Admin Login",
        new AdminLoginPage("Invalid email or token").render()
    ).render());
});

app.get("/only-admin/messages", ctx -> {
    if (!adminApi.isAuthenticated(ctx)) {
        return Response.redirect("/only-admin/log/in");
    }
    return Response.html(new Layout("Messages - Admin",
        new AdminMessagesPage(adminApi.getMessages()).render()
    ).render());
});

app.get("/only-admin/logout", ctx -> {
    adminApi.logout(ctx);
    return Response.redirect("/");
});
```

### Security Classes Reference

| Class | Purpose |
|-------|---------|
| `Auth` | Session-based authentication (login, logout, isAuthenticated, getPrincipal, middleware factories) |
| `Principal` | User principal with id, name, roles (Set), attributes (Map), builder pattern |
| `Jwt` | JWT creation, validation, parsing, middleware |
| `Password` | BCrypt hashing and verification |
| `Cors` | CORS configuration middleware |
| `Csrf` | CSRF protection with token management |
| `CsrfToken` | CSRF token storage and validation |
| `CsrfException` | CSRF validation failure exception |
| `RateLimit` | Rate limiting middleware (requests per time window) |
| `OAuth2` | OAuth2 integration |

---

## Validation

**Validation Classes:**

| Class | Purpose |
|-------|---------|
| `Validator<T>` | Core validator interface with `and()` composition and `validate()` method |
| `Validators` | Built-in validators: `required()`, `email()`, `minLength()`, `maxLength()`, `pattern()`, `min()`, `max()` |
| `ValidationResult` | Result container with `isValid()`, `errors()`, `firstError()` |
| `FormValidator` | Lambda-based form validation: `create().field(name, value, validator).validate()` |
| `FieldValidator` | Field-level validator chain: `required().email().minLength(5).maxLength(100)` |
| `NumberValidators` | Numeric validation rules (min, max, range, positive, negative) |

```java
import static com.osmig.Jweb.framework.validation.Validators.*;

// Chain validators
Validator<String> emailValidator = required()
    .and(email())
    .and(maxLength(100));

ValidationResult result = emailValidator.validate(value, "email");
if (!result.isValid()) {
    List<String> errors = result.errors();
}

// Form validation (lambda-based)
ValidationResult result = FormValidator.create()
    .field("email", email, f -> f.required().email())
    .field("password", password, f -> f.required().minLength(8))
    .field("age", age, f -> f.required().min(18).max(120))
    .validate();
```

---

## OpenAPI Documentation

**OpenAPI Classes:**

| Class | Purpose |
|-------|---------|
| `OpenApi` | Spec builder: `create().title().version().description().addApi().mount(app, path)` |
| `ApiDoc` | API documentation annotation for methods |
| `ApiParam` | Parameter documentation annotation |
| `ApiBody` | Request body documentation annotation |
| `ApiResponse` | Response documentation annotation |

```java
import com.osmig.Jweb.framework.openapi.OpenApi;

OpenApi.create()
    .title("JWeb Example API")
    .version("1.0.0")
    .description("Example REST API built with JWeb")
    .addApi(UserApi.class)
    .addApi(ContactApi.class)
    .mount(app, "/api");
```

This auto-generates a Swagger UI-style documentation page at the specified path (`/api`), scanning the annotated API classes for endpoint definitions.

---

## AI Integration

JWeb includes a fluent DSL for AI/LLM interactions via Spring AI OpenAI (optional dependency).

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
```

```java
import static com.osmig.Jweb.framework.ai.AI.*;

String answer = AI.ask("What is the capital of France?");

String response = AI.chat("Explain quantum computing")
    .model("gpt-4o")
    .temperature(0.7)
    .maxTokens(500)
    .send();

// Multi-turn conversations
Conversation conv = AI.conversation()
    .system("You are a friendly assistant");
String r1 = conv.say("My name is John");
String r2 = conv.say("What is my name?");  // AI remembers

// Task-specific helpers
String summary = AI.summarize(longArticle).maxLength(100).bullets().send();
String spanish = AI.translate("Hello!").to("Spanish").formal().send();
String code = AI.code("Sort a list").language("Java").send();
```

### Ollama (Local LLMs)

```java
Ollama.connect("http://localhost:11434");  // Connect to existing Ollama
Ollama.start("llama3.2");                  // Or start via Docker
String response = AI.chat("Hello!").send();
```
