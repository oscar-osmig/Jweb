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

            warn("Always use HTTPS in production and store secrets in environment variables.")
        );
    }
}
