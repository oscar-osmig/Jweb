# Middleware

Middleware provides a way to process requests before they reach your route handlers.

## Basic Usage

```java
@Component
public class Routes implements JWebRoutes {
    @Override
    public void configure(JWeb app) {
        // Add middleware
        app.use(Middlewares.logging())
           .use(Middlewares.cors())
           .use(Middlewares.csrf());

        // Routes
        app.get("/", req -> new HomePage());
    }
}
```

## Built-in Middleware

### Logging

Logs all incoming requests:

```java
app.use(Middlewares.logging());
// Output: GET /users 200 45ms
```

### CORS

Enables Cross-Origin Resource Sharing:

```java
app.use(Middlewares.cors());

// With custom origins
app.use(Middlewares.cors("https://example.com", "https://api.example.com"));
```

### CSRF Protection

Protects against Cross-Site Request Forgery:

```java
app.use(Middlewares.csrf());
```

### Rate Limiting

Limits requests per time window:

```java
// 100 requests per minute
app.use(Middlewares.rateLimit(100, 60000));
```

### Security Headers

Adds common security headers:

```java
app.use(Middlewares.securityHeaders());
// Adds: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, etc.
```

### Timing

Adds response timing header:

```java
app.use(Middlewares.timing());
// Adds: X-Response-Time: 45ms
```

### Request ID

Adds unique request ID:

```java
app.use(Middlewares.requestId());
// Adds: X-Request-ID: uuid
```

### Cache Control

Sets cache headers:

```java
// Cache for 1 hour
app.use("/api", Middlewares.cacheControl(3600));

// Private cache with revalidation
app.use("/user", Middlewares.cacheControl(3600, true, true));

// No cache
app.use("/admin", Middlewares.noCache());

// Static assets (1 year, immutable)
app.use("/assets", Middlewares.staticCache());
```

### ETag

Enables ETag caching:

```java
app.use(Middlewares.etag());
```

### Compression Headers

Indicates compression support:

```java
app.use(Middlewares.compressionHeaders());
```

## Path-Specific Middleware

Apply middleware only to specific paths:

```java
// Only /admin routes
app.use("/admin", Auth.requireRole("admin"));

// Only /api routes
app.use("/api", Middlewares.cors());
```

## Method-Specific Middleware

Apply middleware for specific HTTP methods:

```java
// Only POST, PUT, DELETE
app.useForMethods(List.of("POST", "PUT", "DELETE"), Middlewares.csrf());
```

## Conditional Middleware

```java
app.useIf(
    req -> req.path().startsWith("/api"),
    Middlewares.cors()
);
```

## Custom Middleware

Create your own middleware:

```java
Middleware timing = (req, chain) -> {
    long start = System.currentTimeMillis();
    Object result = chain.next();
    long duration = System.currentTimeMillis() - start;
    System.out.println("Request took " + duration + "ms");
    return result;
};

app.use(timing);
```

### Before/After Helpers

```java
// Run code before handler
app.use(Middleware.before(req -> {
    System.out.println("Before: " + req.path());
}));

// Run code after handler
app.use(Middleware.after((req, response) -> {
    System.out.println("After: " + req.path());
}));
```

### Error Handling Middleware

```java
Middleware errorHandler = (req, chain) -> {
    try {
        return chain.next();
    } catch (Exception e) {
        return Response.serverError(e.getMessage());
    }
};

app.use(errorHandler);
```

## Middleware Order

Middleware executes in the order it's added:

```java
app.use(Middlewares.logging())      // 1. Logs request
   .use(Middlewares.cors())         // 2. Adds CORS headers
   .use(Auth.requireAuth())         // 3. Checks authentication
   .use(Middlewares.csrf());        // 4. Validates CSRF token
```

## Composing Middleware

Combine multiple middleware:

```java
Middleware security = Middleware.compose(
    Middlewares.securityHeaders(),
    Middlewares.csrf(),
    Auth.requireAuth()
);

app.use("/secure", security);
```

## Middleware Chain

The `chain.next()` call passes control to the next middleware or handler:

```java
Middleware example = (req, chain) -> {
    // Before handler
    System.out.println("Before");

    Object result = chain.next();  // Call next middleware/handler

    // After handler
    System.out.println("After");

    return result;
};
```

## Short-Circuit Responses

Return early without calling `chain.next()`:

```java
Middleware authCheck = (req, chain) -> {
    if (!isAuthenticated(req)) {
        return Response.unauthorized();  // Short-circuit
    }
    return chain.next();  // Continue to handler
};
```
