package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import com.osmig.Jweb.framework.security.Auth;
import com.osmig.Jweb.framework.security.Principal;
import com.osmig.Jweb.framework.server.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** Admin business logic - authentication and message retrieval. */
@Component
public class AdminApi {

    @Value("${jweb.admin.token:}")
    private String adminToken;

    @Value("${jweb.admin.email:}")
    private String adminEmail;

    /** Validates admin credentials and logs in if valid. Returns true on success. */
    public boolean login(Request request, String email, String token) {
        // Refuse to authenticate against an unconfigured (blank) token or email,
        // otherwise a blank submission would match a blank default.
        if (adminToken == null || adminToken.isBlank()) return false;
        if (adminEmail == null || adminEmail.isBlank()) return false;
        if (email == null || token == null) return false;

        // Constant-time comparison so the token cannot be recovered via timing.
        boolean tokenOk = constantTimeEquals(adminToken, token);
        boolean emailOk = adminEmail.equals(email);
        if (!tokenOk || !emailOk) return false;

        Auth.login(request, Principal.of("admin", email, "admin"));
        return true;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    /** Logs out the current admin session. */
    public void logout(Request request) {
        Auth.logout(request);
    }

    /** Returns true if the request has an authenticated session with the admin role. */
    public boolean isAuthenticated(Request request) {
        if (!Auth.isAuthenticated(request)) return false;
        Principal principal = Auth.getPrincipal(request);
        return principal != null && principal.hasRole("admin");
    }

    /** Retrieves all contact messages, newest first. */
    public List<Doc> getMessages() {
        return Mongo.find("contacts")
            .orderByDesc("_id")
            .toList();
    }
}
