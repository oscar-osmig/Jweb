# Testing

JWeb provides testing utilities for testing routes, handlers, and templates.

## Mock Requests

Create mock HTTP requests for testing:

```java
// GET request
Request getReq = MockRequest.get("/users").build();

// With query parameters
Request queryReq = MockRequest.get("/users")
    .queryParam("page", "1")
    .queryParam("limit", "10")
    .build();

// With headers
Request headerReq = MockRequest.get("/api/data")
    .header("Authorization", "Bearer token123")
    .header("Accept", "application/json")
    .build();

// POST with JSON body
Request jsonReq = MockRequest.post("/api/users")
    .json("{\"name\": \"John\", \"email\": \"john@example.com\"}")
    .build();

// POST with form data — formData(...) takes the whole map at once
Request formReq = MockRequest.post("/login")
    .formData(Map.of("username", "john", "password", "secret"))
    .build();

// Other methods — PATCH has no dedicated factory, use request(method, path)
MockRequest.put("/users/1")
MockRequest.request("PATCH", "/users/1")
MockRequest.delete("/users/1")
```

## Testing Routes

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        app.get("/", req -> "Welcome");
        app.get("/api/users", req -> Response.json(List.of("users")));
    }
}

@Test
void testHomePage() {
    JWeb app = JWeb.create();
    new Routes().configure(app);

    JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/"));

    assertTrue(result.isSuccess());
    assertTrue(result.bodyContains("Welcome"));
}

@Test
void testApiEndpoint() {
    JWeb app = JWeb.create();
    new Routes().configure(app);

    JWebTest.TestResult result = JWebTest.test(app,
        MockRequest.get("/api/users")
            .header("Accept", "application/json")
    );

    assertEquals(200, result.getStatus());
    assertTrue(result.bodyContains("users"));
}
```

## Testing Handlers

Test individual handlers:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void testUserHandler() {
    RouteHandler handler = req -> {
        String id = req.param("id");
        return Response.json(Map.of("id", id, "name", "John"));
    };

    // testHandler(...) takes the MockRequest itself, not a built Request
    MockRequest req = MockRequest.get("/users/123");
    JWebTest.TestResult result = JWebTest.testHandler(handler, req);

    assertTrue(result.isSuccess());
    assertTrue(result.bodyContains("123"));
}
```

## TestResult Methods

```java
JWeb app = JWeb.create();
MockRequest request = MockRequest.get("/");
JWebTest.TestResult result = JWebTest.test(app, request);

// Status checks
result.isSuccess();      // 2xx status
result.getStatus();      // HTTP status code

// Body checks
result.bodyContains("text");
result.getBody();        // the raw body String

// TestResult does not capture response headers yet
```

## HTML Assertions

Assert HTML content:

```java
Element element = div(cls("container"), h1("Welcome"));
String html = element.toHtml();

// Content assertions
JWebTest.assertContains(html, "Welcome");
JWebTest.assertNotContains(html, "Error");

// Attribute assertions
JWebTest.assertHasClass(html, "container");
JWebTest.assertHasId(html, "main");

// Tag assertions
JWebTest.assertHasTag(html, "form");
JWebTest.assertHasTag(html, "button");
```

## Testing Templates

```java
import org.junit.jupiter.api.Test;

record HomePage() implements Template {
    public Element render() {
        return div(nav(), h1("Welcome"), cls("hero"));
    }
}
record Card(String title, String content) implements Template {
    public Element render() {
        return div(cls("card"), h2(title), p(content));
    }
}

@Test
void testHomePage() {
    HomePage page = new HomePage();
    String html = page.render().toHtml();

    JWebTest.assertContains(html, "Welcome");
    JWebTest.assertHasClass(html, "hero");
    JWebTest.assertHasTag(html, "nav");
}

@Test
void testCardComponent() {
    Card card = new Card("Title", "Content");
    String html = card.render().toHtml();

    JWebTest.assertContains(html, "Title");
    JWebTest.assertContains(html, "Content");
    JWebTest.assertHasClass(html, "card");
}
```

## Mock Sessions

Test session-based features:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void testAuthenticatedRequest() {
    JWeb app = JWeb.create();
    app.get("/dashboard", req -> "Dashboard");

    MockSession session = new MockSession();
    session.setAttribute("user", Principal.of("user123", "john@example.com"));

    MockRequest req = MockRequest.get("/dashboard").session(session);

    JWebTest.TestResult result = JWebTest.test(app, req);
    assertTrue(result.isSuccess());
}
```

## Testing with Authentication

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

record AdminPage() implements Template {
    public Element render() { return div(h1("Admin")); }
}

@Test
void testProtectedRoute() {
    JWeb app = JWeb.create();
    app.use("/admin", Auth.requireRole("admin"));
    app.get("/admin", req -> new AdminPage());

    // Without auth - should fail
    JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/admin"));
    assertEquals(401, result.getStatus());

    // With auth — Auth.login(...) takes a built Request, so log in on a request
    // that shares the same session, then reuse that session for the real call
    MockSession session = new MockSession();
    Auth.login(MockRequest.get("/admin").session(session).build(),
        Principal.of("1", "admin@example.com", "admin"));

    result = JWebTest.test(app, MockRequest.get("/admin").session(session));
    assertTrue(result.isSuccess());
}
```

## Testing Validation

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void testFormValidation() {
    JWeb app = JWeb.create();
    app.post("/register", req -> Response.redirect("/welcome"));

    // Test valid input
    MockRequest validReq = MockRequest.post("/register")
        .formData(Map.of("email", "john@example.com", "password", "securepass123"));

    JWebTest.TestResult result = JWebTest.test(app, validReq);
    assertTrue(result.isSuccess());

    // Test invalid input
    MockRequest invalidReq = MockRequest.post("/register")
        .formData(Map.of("email", "invalid", "password", "short"));

    result = JWebTest.test(app, invalidReq);
    assertEquals(400, result.getStatus());
    assertTrue(result.bodyContains("email"));
}
```

## Testing JSON APIs

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Test
void testJsonApi() {
    JWeb app = JWeb.create();
    app.post("/api/users", req -> Response.json(201, Map.of("name", "John")));
    app.get("/api/users/:id", req -> "1".equals(req.param("id"))
        ? Response.json(Map.of("id", "1"))
        : Response.notFound("User not found"));

    // Create
    JWebTest.TestResult result = JWebTest.test(app,
        MockRequest.post("/api/users")
            .json("{\"name\": \"John\", \"email\": \"john@example.com\"}")
    );

    assertEquals(201, result.getStatus());
    assertTrue(result.bodyContains("John"));

    // Read
    result = JWebTest.test(app, MockRequest.get("/api/users/1"));
    assertEquals(200, result.getStatus());

    // Not found
    result = JWebTest.test(app, MockRequest.get("/api/users/999"));
    assertEquals(404, result.getStatus());
}
```

## Integration Test Example

```java
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoutesIntegrationTest {

    @Autowired
    private JWeb app;

    @Test
    void testFullUserFlow() {
        // Register
        JWebTest.TestResult result = JWebTest.test(app,
            MockRequest.post("/register")
                .formData(Map.of("email", "test@example.com", "password", "password123"))
        );
        assertEquals(302, result.getStatus());  // Redirect

        // Login
        result = JWebTest.test(app,
            MockRequest.post("/login")
                .formData(Map.of("email", "test@example.com", "password", "password123"))
        );
        assertEquals(302, result.getStatus());

        // Access dashboard — Auth.login(...) takes a built Request, so log in on
        // a request that shares the session, then reuse that session below
        MockSession session = new MockSession();
        Auth.login(MockRequest.get("/dashboard").session(session).build(),
            Principal.of("1", "test@example.com", "user"));

        result = JWebTest.test(app,
            MockRequest.get("/dashboard").session(session)
        );
        assertTrue(result.isSuccess());
        assertTrue(result.bodyContains("Dashboard"));
    }
}
```
