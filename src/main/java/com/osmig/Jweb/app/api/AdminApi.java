package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import com.osmig.Jweb.framework.security.Auth;
import com.osmig.Jweb.framework.security.Principal;
import com.osmig.Jweb.framework.server.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        if (adminToken == null || adminToken.isBlank()) return false;
        if (!adminToken.equals(token) || !adminEmail.equals(email)) return false;

        Auth.login(request, Principal.of("admin", email, "admin"));
        return true;
    }

    /** Logs out the current admin session. */
    public void logout(Request request) {
        Auth.logout(request);
    }

    /** Returns true if the request has an authenticated admin session. */
    public boolean isAuthenticated(Request request) {
        return Auth.isAuthenticated(request);
    }

    /** Retrieves all contact messages, newest first. */
    public List<Doc> getMessages() {
        return Mongo.find("contacts")
            .orderByDesc("_id")
            .toList();
    }
}
