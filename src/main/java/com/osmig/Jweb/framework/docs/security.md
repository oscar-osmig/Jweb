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
    Csrf.tokenField(req),  // Hidden CSRF token field
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
String token = Csrf.getToken(req).getValue();
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
record User(String id, String email, String[] roles) {}
class UserService {
    User authenticate(String email, String password) { return new User("1", email, new String[]{"user"}); }
}
UserService userService = new UserService();

// Login
app.post("/login", req -> {
    String email = req.formParam("email");
    String password = req.formParam("password");

    User user = userService.authenticate(email, password);
    if (user == null) {
        return Response.unauthorized("Invalid credentials");
    }

    // Create principal and login
    Principal principal = Principal.of(user.id(), user.email(), user.roles());
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
record ProfilePage(Principal user) implements Template {
    public Element render() { return div("Profile: " + user.getName()); }
}

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
record AdminPage() implements Template {
    public Element render() { return div("Admin"); }
}

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
String email = user.getName();  // the second Principal.of argument (often an email)
Set<String> roles = user.getRoles();

// Check roles
boolean isAdmin = user.hasRole("admin");
boolean canManage = user.hasAnyRole("admin", "manager");
```

## Bearer Token Authentication

For API authentication:

```java
record User(String id, String email, String[] roles) {}
class TokenService {
    User validateToken(String token) { return new User("1", "user@example.com", new String[]{"user"}); }
}
TokenService tokenService = new TokenService();

app.use("/api", Auth.bearerAuth(token -> {
    // Validate token and return Principal
    User user = tokenService.validateToken(token);
    if (user == null) {
        return null;  // Returns 401
    }
    return Principal.of(user.id(), user.email(), user.roles());
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
String rawPassword = "correct horse battery staple";

// Hash password
String hashed = Password.hash(rawPassword);

// Verify password
boolean matches = Password.verify(rawPassword, hashed);
```

## The Flows

Signed tokens, remember me, log out everywhere and two-factor — stateless or on a shared
store, so they work on a cluster. Set `jweb.auth.secret` (32+ random characters).

### Password reset and email verification

```java
import jweb.Auth;

String passwordHash = "$2a$12$...";
String email = "ada@example.com";

// bound to the password hash: once the password changes the link is dead
String reset = Auth.token("reset", "42", Duration.ofHours(1), passwordHash);
Optional<String> id = Auth.subject(reset);                                // "42", unverified: load the user by it
Optional<String> userId = Auth.verify("reset", reset, passwordHash);     // Optional.of("42"), verified against that user's hash

// bound to the email: verifies that address and no other
String verify = Auth.token("verify", "42", Duration.ofDays(1), email);
Optional<String> verified = Auth.verify("verify", verify, email);
Optional<Instant> expires = Auth.expiry(verify);
```

### Remember me

```java
import jweb.Auth;
import jweb.Principal;

Principal principal = Principal.of("42", "ada@example.com", "user");
Function<String, Principal> userById = id -> principal;

// on login
Auth.login(req, principal);
Response.Redirect home = Response.redirect("/").cookie(Auth.rememberCookie(principal, Duration.ofDays(30)));

// once, for returning visitors
app.use(Auth.remember(userById));

// on logout
Response.redirect("/").cookie(Auth.forgetCookie());
```

### Log out everywhere

```java
import jweb.Auth;

Auth.logoutEverywhere("42");     // every session and remember-me cookie of the user, on every instance
Auth.logoutEverywhere(req);      // the request's user, and this session with it
long version = Auth.version("42");
```

Versions are in memory, or in `jweb_auth_versions` when `jweb.Sql` is connected (reads cached
ten seconds); a `Auth.Versions` bean replaces both.

### Two-factor (TOTP)

```java
import jweb.Auth;
import jweb.Password;

String secret = Auth.Totp.secret();                               // store on the user
String uri = Auth.Totp.uri("MyApp", "ada@example.com", secret);   // render as a QR code
boolean ok = Auth.Totp.verify(secret, req.formParam("code"));     // one period of skew either side
List<String> recovery = Auth.recoveryCodes(8);                    // show once; store Password.hash(code) each
String stored = Password.hash(recovery.get(0));
```

## Complete Auth Example

```java
record User(String id, String email, String[] roles) {}

class UserService {
    User authenticate(String email, String password) { return new User("1", email, new String[]{"user"}); }
}

class TokenService {
    Principal validate(String token) { return Principal.of("1", "user@example.com", "user"); }
}

record HomePage() implements Template {
    public Element render() { return div("Home"); }
}

record LoginPage(String error) implements Template {
    LoginPage() { this(null); }
    public Element render() { return div("Login", error == null ? "" : error); }
}

record DashboardPage(Principal user) implements Template {
    public Element render() { return div("Dashboard: " + user.getName()); }
}

record AdminPage() implements Template {
    public Element render() { return div("Admin"); }
}

@Component
public class Routes implements JWebRoutes {

    private final UserService userService = new UserService();
    private final TokenService tokenService = new TokenService();

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

        Auth.login(req, Principal.of(user.id(), email, user.roles()));
        return Response.redirect("/dashboard");
    }

    private Principal validateToken(String token) {
        return tokenService.validate(token);
    }
}
```
