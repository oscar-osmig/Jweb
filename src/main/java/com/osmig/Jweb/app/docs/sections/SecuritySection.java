package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SecuritySection {
    private SecuritySection() {}

    public static Element render() {
        return section(
            docTitle("Security"),
            para("Built-in security for auth, passwords, JWT, and sessions."),

            docSubtitle("Password Hashing"),
            codeBlock("""
                    // Hash password
                    String hash = Passwords.hash("secret123");
                    
                    // Verify password
                    boolean valid = Passwords.verify("secret123", hash);"""),

            docSubtitle("JWT Authentication"),
            codeBlock("""
                    // Generate token
                    String token = JWT.create()
                        .subject(user.getId())
                        .claim("role", user.getRole())
                        .expiresIn(Duration.ofHours(24))
                        .sign(SECRET_KEY);

                    // Verify token
                    Claims claims = JWT.verify(token, SECRET_KEY);
                    String userId = claims.getSubject();"""),

            docSubtitle("Protected Routes"),
            codeBlock("""
                        app.use("/admin/**", Auth.required());
                        app.use("/api/**", Auth.jwt(SECRET_KEY));
                        
                        // In handler
                        app.get("/profile", req -> {
                            User user = req.user();  // Current user
                            return profilePage(user);
                        });"""),

            docSubtitle("Rate Limiting"),
            codeBlock("""
                        app.use(RateLimit.perMinute(100));
                        app.use("/api/**", RateLimit.perMinute(30));"""),

            warn("Always use HTTPS in production and store secrets in environment variables.")
        );
    }
}
