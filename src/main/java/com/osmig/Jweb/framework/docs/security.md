# Security

JWeb provides built-in security features for authentication, authorization, and CSRF protection.

## CSRF Protection

### Enabling CSRF

```java
app.use(Middlewares.csrf());
```

### Form Protection

Add the CSRF token to forms:

```java
form(method("post"), action("/submit"),
    Csrf.tokenField(request),  // Hidden CSRF token field
    input(type("text"), name("message")),
    button("Submit")
)
```

### Validating CSRF Token

```java
app.post("/submit", req -> {
    Csrf.validate(req);  // Throws CsrfException if invalid
    // Process form...
    return Response.redirect("/success");
});
```

### AJAX Requests

For JavaScript requests, include the token in headers:

```java
// Get token for JS
String token = Csrf.getToken(request);
```

```javascript
fetch('/api/data', {
    method: 'POST',
    headers: {
        'X-CSRF-Token': token
    },
    body: JSON.stringify(data)
});
```

## Authentication

### Session-Based Auth

```java
// Login
app.post("/login", req -> {
    String email = req.formParam("email");
    String password = req.formParam("password");

    User user = userService.authenticate(email, password);
    if (user == null) {
        return Response.unauthorized("Invalid credentials");
    }

    // Create principal and login
    Principal principal = Principal.of(user.getId(), user.getEmail(), user.getRoles());
    Auth.login(req, principal);

    return Response.redirect("/dashboard");
});
```

### Logout

```java
app.post("/logout", req -> {
    Auth.logout(req);
    return Response.redirect("/");
});
```

### Check Authentication

```java
app.get("/profile", req -> {
    if (!Auth.isAuthenticated(req)) {
        return Response.redirect("/login");
    }

    Principal user = Auth.getPrincipal(req);
    return new ProfilePage(user);
});
```

## Authorization

### Require Authentication Middleware

```java
// Protect all /dashboard routes
app.use("/dashboard", Auth.requireAuth());
```

### Role-Based Access

```java
// Require specific role
app.use("/admin", Auth.requireRole("admin"));

// Require any of several roles
app.use("/manage", Auth.requireAnyRole("admin", "manager"));
```

### Check Roles in Handlers

```java
app.get("/admin", req -> {
    Principal user = Auth.getPrincipal(req);

    if (!user.hasRole("admin")) {
        return Response.forbidden("Admin access required");
    }

    return new AdminPage();
});
```

## Principal

The `Principal` represents an authenticated user:

```java
// Create principal
Principal user = Principal.of("user123", "john@example.com", "user", "admin");

// Access properties
String id = user.getId();
String email = user.getEmail();
Set<String> roles = user.getRoles();

// Check roles
boolean isAdmin = user.hasRole("admin");
boolean canManage = user.hasAnyRole("admin", "manager");
```

## Bearer Token Authentication

For API authentication:

```java
app.use("/api", Auth.bearerAuth(token -> {
    // Validate token and return Principal
    User user = tokenService.validateToken(token);
    if (user == null) {
        return null;  // Returns 401
    }
    return Principal.of(user.getId(), user.getEmail(), user.getRoles());
}));
```

### Making Authenticated API Requests

```java
// Using Fetch utility
FetchResult result = Fetch.get("https://api.example.com/data")
    .bearer("your-jwt-token")
    .send();
```

## Security Headers Middleware

Add common security headers:

```java
app.use(Middlewares.securityHeaders());
```

This adds:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`

## Password Hashing

For password storage, use Spring Security's `PasswordEncoder` or BCrypt:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// Hash password
String hashed = encoder.encode(rawPassword);

// Verify password
boolean matches = encoder.matches(rawPassword, hashedPassword);
```

## Complete Auth Example

```java
@Component
public class Routes implements JWebRoutes {

    private final UserService userService;

    @Override
    public void configure(JWeb app) {
        // Public routes
        app.get("/", req -> new HomePage())
           .get("/login", req -> new LoginPage())
           .post("/login", this::handleLogin);

        // Protected routes
        app.use("/dashboard", Auth.requireAuth());
        app.get("/dashboard", req -> new DashboardPage(Auth.getPrincipal(req)));

        // Admin routes
        app.use("/admin", Auth.requireRole("admin"));
        app.get("/admin", req -> new AdminPage());

        // API with bearer auth
        app.use("/api", Auth.bearerAuth(this::validateToken));
        app.get("/api/me", req -> Response.json(Auth.getPrincipal(req)));
    }

    private Object handleLogin(Request req) {
        String email = req.formParam("email");
        String password = req.formParam("password");

        User user = userService.authenticate(email, password);
        if (user == null) {
            return new LoginPage("Invalid credentials");
        }

        Auth.login(req, Principal.of(user.getId(), email, user.getRoles()));
        return Response.redirect("/dashboard");
    }

    private Principal validateToken(String token) {
        return tokenService.validate(token);
    }
}
```
