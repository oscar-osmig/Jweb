# Testing

JWeb provides testing utilities for testing routes, handlers, and templates.

## Mock Requests

Create mock HTTP requests for testing:

```java
import com.osmig.Jweb.framework.testing.MockRequest;

// GET request
Request req = MockRequest.get("/users").build();

// With query parameters
Request req = MockRequest.get("/users")
    .queryParam("page", "1")
    .queryParam("limit", "10")
    .build();

// With headers
Request req = MockRequest.get("/api/data")
    .header("Authorization", "Bearer token123")
    .header("Accept", "application/json")
    .build();

// POST with JSON body
Request req = MockRequest.post("/api/users")
    .json("{\"name\": \"John\", \"email\": \"john@example.com\"}")
    .build();

// POST with form data
Request req = MockRequest.post("/login")
    .formParam("username", "john")
    .formParam("password", "secret")
    .build();

// Other methods
MockRequest.put("/users/1")
MockRequest.patch("/users/1")
MockRequest.delete("/users/1")
```

## Testing Routes

```java
import com.osmig.Jweb.framework.testing.JWebTest;

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
@Test
void testUserHandler() {
    RouteHandler handler = req -> {
        String id = req.param("id");
        return Response.json(Map.of("id", id, "name", "John"));
    };

    Request req = MockRequest.get("/users/123").build();
    JWebTest.TestResult result = JWebTest.testHandler(handler, req);

    assertTrue(result.isSuccess());
    assertTrue(result.bodyContains("123"));
}
```

## TestResult Methods

```java
JWebTest.TestResult result = JWebTest.test(app, request);

// Status checks
result.isSuccess();      // 2xx status
result.getStatus();      // HTTP status code

// Body checks
result.bodyContains("text");

// Headers
result.getHeaders();
result.getHeader("Content-Type");
```

## HTML Assertions

Assert HTML content:

```java
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
import com.osmig.Jweb.framework.testing.MockSession;

@Test
void testAuthenticatedRequest() {
    MockSession session = new MockSession();
    session.setAttribute("user", new Principal("user123", "john@example.com"));

    Request req = MockRequest.get("/dashboard")
        .session(session)
        .build();

    JWebTest.TestResult result = JWebTest.test(app, req);
    assertTrue(result.isSuccess());
}
```

## Testing with Authentication

```java
@Test
void testProtectedRoute() {
    JWeb app = JWeb.create();
    app.use("/admin", Auth.requireRole("admin"));
    app.get("/admin", req -> new AdminPage());

    // Without auth - should fail
    JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/admin"));
    assertEquals(401, result.getStatus());

    // With auth
    MockSession session = new MockSession();
    Auth.login(session, Principal.of("1", "admin@example.com", "admin"));

    result = JWebTest.test(app, MockRequest.get("/admin").session(session));
    assertTrue(result.isSuccess());
}
```

## Testing Validation

```java
@Test
void testFormValidation() {
    // Test valid input
    Request validReq = MockRequest.post("/register")
        .formParam("email", "john@example.com")
        .formParam("password", "securepass123")
        .build();

    JWebTest.TestResult result = JWebTest.test(app, validReq);
    assertTrue(result.isSuccess());

    // Test invalid input
    Request invalidReq = MockRequest.post("/register")
        .formParam("email", "invalid")
        .formParam("password", "short")
        .build();

    result = JWebTest.test(app, invalidReq);
    assertEquals(400, result.getStatus());
    assertTrue(result.bodyContains("email"));
}
```

## Testing JSON APIs

```java
@Test
void testJsonApi() {
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
@SpringBootTest
class RoutesIntegrationTest {

    @Autowired
    private JWeb app;

    @Test
    void testFullUserFlow() {
        // Register
        JWebTest.TestResult result = JWebTest.test(app,
            MockRequest.post("/register")
                .formParam("email", "test@example.com")
                .formParam("password", "password123")
        );
        assertEquals(302, result.getStatus());  // Redirect

        // Login
        result = JWebTest.test(app,
            MockRequest.post("/login")
                .formParam("email", "test@example.com")
                .formParam("password", "password123")
        );
        assertEquals(302, result.getStatus());

        // Access dashboard
        MockSession session = new MockSession();
        Auth.login(session, Principal.of("1", "test@example.com", "user"));

        result = JWebTest.test(app,
            MockRequest.get("/dashboard").session(session)
        );
        assertTrue(result.isSuccess());
        assertTrue(result.bodyContains("Dashboard"));
    }
}
```
