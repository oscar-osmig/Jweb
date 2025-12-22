package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

public final class SecuritySection {
    private SecuritySection() {}

    public static Element render() {
        return section(
            title("Security"),
            text("JWeb provides built-in security utilities for authentication, " +
                 "password hashing, JWT tokens, and session management."),

            subtitle("Password Hashing"),
            text("Use BCrypt for secure password hashing:"),
            code(SECURITY_PASSWORD),

            subtitle("JWT Authentication"),
            text("Generate and validate JWT tokens for stateless authentication:"),
            code(SECURITY_JWT),

            subtitle("Session Management"),
            text("Built-in session handling with secure defaults:"),
            code(SECURITY_SESSION),

            subtitle("Protected Routes"),
            text("Protect routes with authentication middleware:"),
            code(SECURITY_PROTECTED),

            subtitle("CSRF Protection"),
            text("Enable CSRF tokens for form submissions:"),
            code(SECURITY_CSRF)
        );
    }
}
