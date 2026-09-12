# Routing

JWeb provides a fluent routing API for defining HTTP endpoints.

## Basic Routes

```java
record HomePage() implements Template {
    public Element render() { return div(h1("Home")); }
}
record AboutPage() implements Template {
    public Element render() { return div(h1("About")); }
}

@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/", req -> new HomePage())
           .get("/about", req -> new AboutPage())
           .post("/login", this::handleLogin);
    }

    private Object handleLogin(Request req) {
        return Response.redirect("/dashboard");
    }
}
```

## HTTP Methods

```java
RouteHandler handler = req -> "ok";

// PATCH has no app.patch(...) yet — GET/POST/PUT/DELETE only
app.get("/users", handler)      // GET
   .post("/users", handler)     // POST
   .put("/users/:id", handler)  // PUT
   .delete("/users/:id", handler); // DELETE
```

## Path Parameters

Use `:paramName` syntax for dynamic segments:

```java
record UserPage(String userId) implements Template {
    public Element render() { return div("User " + userId); }
}
record CommentPage(String postId, String commentId) implements Template {
    public Element render() { return div("Comment " + commentId + " on post " + postId); }
}

app.get("/users/:id", req -> {
    String userId = req.param("id");
    return new UserPage(userId);
});

app.get("/posts/:postId/comments/:commentId", req -> {
    String postId = req.param("postId");
    String commentId = req.param("commentId");
    return new CommentPage(postId, commentId);
});
```

## Typed Route Parameters

Type-safe parameter extraction with automatic conversion and validation:

```java
record UserPage(int userId) implements Template {
    public Element render() { return div("User " + userId); }
}
record ProductPage(long productId) implements Template {
    public Element render() { return div("Product " + productId); }
}
record PriceFilterPage(double price) implements Template {
    public Element render() { return div("Under $" + price); }
}
record PostsPage(boolean published) implements Template {
    public Element render() { return div(published ? "Published" : "Drafts"); }
}
record OrderPage(UUID orderId) implements Template {
    public Element render() { return div("Order " + orderId); }
}

app.get("/users/:id", req -> {
    // Integer parameters
    int userId = req.paramInt("id");                             // null (NPEs on unboxing) if missing/invalid
    Optional<Integer> userIdOpt = req.paramIntOpt("id");         // empty if missing/invalid
    int page = req.paramInt("id", 1);                            // default value if missing/invalid

    return new UserPage(userId);
});

app.get("/products/:id", req -> {
    // Long parameters (for large IDs)
    long productId = req.paramLong("id");
    Optional<Long> productIdOpt = req.paramLongOpt("id");

    return new ProductPage(productId);
});

app.get("/items/:price", req -> {
    // Double parameters — there is no paramDoubleOpt(...) yet
    double price = req.paramDouble("price");
    Optional<Double> priceOpt = Optional.ofNullable(req.paramDouble("price"));

    return new PriceFilterPage(price);
});

app.get("/posts/:published", req -> {
    // Boolean parameters — "true"/"1"/"yes"/"on" parse as true, anything else as false;
    // there is no paramBoolOpt(...) yet
    boolean published = req.paramBool("published");
    Optional<Boolean> publishedOpt = Optional.ofNullable(req.paramBool("published"));

    return new PostsPage(published);
});

app.get("/orders/:uuid", req -> {
    // UUID parameters
    UUID orderId = req.paramUUID("uuid");
    Optional<UUID> orderIdOpt = req.paramUUIDOpt("uuid");

    return new OrderPage(orderId);
});
```

### Required Parameters (with Validation)

```java
record UserPage(int userId) implements Template {
    public Element render() { return div("User " + userId); }
}

app.get("/users/:id", req -> {
    // requireParamInt/Long/UUID throw IllegalArgumentException if missing or invalid.
    // double and boolean have no require* variant yet — Optional::orElseThrow gets there.
    int id = req.requireParamInt("id");
    long bigId = req.requireParamLong("id");
    UUID uuid = req.requireParamUUID("uuid");
    double amount = Optional.ofNullable(req.paramDouble("amount"))
        .orElseThrow(() -> new IllegalArgumentException("amount is required"));
    boolean active = Optional.ofNullable(req.paramBool("active"))
        .orElseThrow(() -> new IllegalArgumentException("active is required"));

    return new UserPage(id);
});
```

## Query Parameters

```java
record SearchPage(String query, int page, long limit) implements Template {
    public Element render() { return div("Results for " + query); }
}

app.get("/search", req -> {
    String query = req.query("q");                  // ?q=hello
    int page = req.queryInt("page", 1);              // ?page=2 (default: 1)
    Long limitParam = req.queryLong("limit");        // ?limit=50 — no default-value overload yet
    long limit = limitParam != null ? limitParam : 10;
    return new SearchPage(query, page, limit);
});
```

## Request Body

```java
record User(String id, String name) {}
class UserService { User save(User u) { return u; } }
UserService userService = new UserService();
com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();

// JSON body — Request has no typed bodyAs(...) yet, so parse the raw body
// yourself (a @REST class with @Body does bind a type for you)
app.post("/api/users", req -> {
    try {
        User user = json.readValue(req.body(), User.class);
        return Response.json(userService.save(user));
    } catch (Exception e) {
        return Response.error(400, "Invalid JSON");
    }
});

// Raw body
app.post("/webhook", req -> {
    String payload = req.body();
    return Response.ok().build();
});

// Form data
app.post("/contact", req -> {
    String name = req.formParam("name");
    String email = req.formParam("email");
    return Response.redirect("/thank-you");
});
```

## Headers

```java
Map<String, String> data = Map.of("status", "ok");

app.get("/api/data", req -> {
    String auth = req.header("Authorization");
    String contentType = req.contentType();
    return Response.json(data);
});
```

## Response Types

### HTML Response (Templates)

```java
record HomePage() implements Template {
    public Element render() { return div(h1("Home")); }
}

app.get("/", req -> new HomePage());  // Returns Element
app.get("/about", req -> div(h1("About")));  // Inline Element
```

### JSON Response

```java
List<String> users = List.of("ada", "linus");

app.get("/api/users", req -> Response.json(users));

app.get("/api/status", req -> Response.json()
    .put("status", "ok")
    .put("version", "1.0.0")
    .build());
```

### Redirects

```java
app.post("/login", req -> Response.redirect("/dashboard"));
app.post("/form", req -> Response.seeOther("/success"));  // 303 after POST
```

### Error Responses

```java
record User(String id, String name) {}
class UserService { User find(String id) { return null; } }
UserService userService = new UserService();

app.get("/api/users/:id", req -> {
    User user = userService.find(req.param("id"));
    if (user == null) {
        return Response.notFound("User not found");
    }
    return Response.json(user);
});
```

## Handler Return Types

Handlers can return:

| Return Type | Result |
|-------------|--------|
| `Element` / `Template` | Rendered as HTML |
| `ResponseEntity` | Used directly |
| `String` | Plain text response |
| `Object` | Serialized as JSON |

## Route Organization

For larger applications, organize routes by feature:

```java
@Component
class UserRoutes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/users", req -> "users");
    }
}
@Component
class ApiRoutes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/api/status", req -> "ok");
    }
}

@Component
public class Routes implements JWebRoutes {
    private final UserRoutes userRoutes;
    private final ApiRoutes apiRoutes;

    public Routes(UserRoutes userRoutes, ApiRoutes apiRoutes) {
        this.userRoutes = userRoutes;
        this.apiRoutes = apiRoutes;
    }

    @Override
    public void configure(JWeb app) {
        userRoutes.configure(app);
        apiRoutes.configure(app);
    }
}
```
