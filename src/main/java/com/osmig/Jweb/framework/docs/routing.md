# Routing

JWeb provides a fluent routing API for defining HTTP endpoints.

## Basic Routes

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/", req -> new HomePage())
           .get("/about", req -> new AboutPage())
           .post("/login", this::handleLogin);
    }
}
```

## HTTP Methods

```java
app.get("/users", handler)      // GET
   .post("/users", handler)     // POST
   .put("/users/:id", handler)  // PUT
   .patch("/users/:id", handler) // PATCH
   .delete("/users/:id", handler); // DELETE
```

## Path Parameters

Use `:paramName` syntax for dynamic segments:

```java
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

## Query Parameters

```java
app.get("/search", req -> {
    String query = req.query("q");           // ?q=hello
    int page = req.queryInt("page", 1);      // ?page=2 (default: 1)
    long limit = req.queryLong("limit", 10); // ?limit=50
    return new SearchPage(query, page, limit);
});
```

## Request Body

```java
// JSON body
app.post("/api/users", req -> {
    User user = req.bodyAs(User.class);
    return Response.json(userService.save(user));
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
app.get("/api/data", req -> {
    String auth = req.header("Authorization");
    String contentType = req.contentType();
    return Response.json(data);
});
```

## Response Types

### HTML Response (Templates)

```java
app.get("/", req -> new HomePage());  // Returns Element
app.get("/about", req -> div(h1("About")));  // Inline Element
```

### JSON Response

```java
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
