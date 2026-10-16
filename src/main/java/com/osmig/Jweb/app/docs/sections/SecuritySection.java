package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SecuritySection {
    private SecuritySection() {}

    public static Element render() {
        return section(
            docTitle("Security"),
            para("Built-in security for auth, passwords, JWT, and sessions."),

            docSubtitle("Password Hashing"),
            codeBlock("""
                    // Hash password (BCrypt)
                    String hash = Password.hash("secret123");

                    // Verify password
                    boolean valid = Password.verify("secret123", hash);"""),

            docSubtitle("JWT Authentication"),
            codeBlock("""
                    class CurrentUser {
                        String getId() { return "42"; }
                        String getRole() { return "admin"; }
                    }
                    CurrentUser user = new CurrentUser();

                    // Configure the signing key once at startup
                    Jwt.init(System.getenv("JWT_SECRET"));

                    // Generate token
                    String token = Jwt.create()
                        .subject(user.getId())
                        .claim("role", user.getRole())
                        .expiresIn(Duration.ofHours(24))
                        .sign();

                    // Verify token
                    Jwt.Token parsed = Jwt.parse(token);
                    String userId = parsed.subject();"""),

            docSubtitle("Protected Routes"),
            codeBlock("""
                        Element profilePage(Principal user) { return div("Hi, " + user.getName()); }

                        app.use("/admin", Auth.requireAuth("/login"));
                        app.use("/api", Jwt.protect());

                        // In handler
                        app.get("/profile", req -> {
                            Principal user = Auth.requirePrincipal(req);
                            return profilePage(user);
                        });"""),

            docSubtitle("Rate Limiting"),
            codeBlock("""
                        app.use(RateLimit.perMinute(100).byIp().build());
                        app.use("/api", RateLimit.perMinute(30).byIp().build());"""),

            since("v3.0.3",
            docSubtitle("The Flows: Reset, Verify, Remember Me, Two-Factor"),
            para("The user model stays yours; Auth provides the mechanics, stateless or on a shared store. " +
                 "Set jweb.auth.secret (32+ random characters) in every environment."),
            codeBlock("""
import jweb.Auth;
import jweb.Mail;
import jweb.Principal;
import static jweb.El.*;

String passwordHash = "$2a$12$...";
Principal principal = Principal.of("42", "ada@example.com", "user");

// password reset: a signed token bound to the password hash — dead once the password changes
String token = Auth.token("reset", "42", Duration.ofHours(1), passwordHash);
Mail.to("ada@example.com").subject("Reset your password")
    .html(p(a(href("https://app.example.com/reset?token=" + token), "Choose a new password"))).later();
Optional<String> userId = Auth.verify("reset", token, passwordHash);

// remember me: the cookie on login, the middleware for returning visitors
Response.Redirect home = Response.redirect("/").cookie(Auth.rememberCookie(principal, Duration.ofDays(30)));
app.use(Auth.remember(id -> principal));

// log out everywhere: every session and remember-me cookie of the user
Auth.logoutEverywhere("42");

// two-factor
String secret = Auth.Totp.secret();
String uri = Auth.Totp.uri("MyApp", "ada@example.com", secret);   // show as a QR code
boolean ok = Auth.Totp.verify(secret, req.formParam("code"));""")),

            warn("Always use HTTPS in production and store secrets in environment variables.")
        );
    }
}
