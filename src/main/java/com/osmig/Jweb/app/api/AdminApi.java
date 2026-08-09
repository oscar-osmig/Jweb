package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.security.Auth;
import com.osmig.Jweb.framework.security.Principal;
import com.osmig.Jweb.framework.server.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Admin business logic - authentication and message retrieval. */
@Component
public class AdminApi {

    private static final int MAX_ATTEMPTS = 5;
    private static final long ATTEMPT_WINDOW_MS = 15 * 60 * 1000;

    @Value("${jweb.admin.token:}")
    private String adminToken;

    @Value("${jweb.admin.email:}")
    private String adminEmail;

    // Per-IP failed-login tracking (windowStart, count)
    private final Map<String, long[]> failedAttempts = new ConcurrentHashMap<>();

    private final MessageStore messageStore;

    public AdminApi(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    /**
     * True when admin credentials are configured. When false, admin login is
     * disabled entirely (fails closed) — set JWEB_ADMIN_TOKEN/JWEB_ADMIN_EMAIL.
     */
    public boolean isConfigured() {
        return adminToken != null && !adminToken.isBlank()
            && adminEmail != null && !adminEmail.isBlank();
    }

    /** Validates admin credentials and logs in if valid. Returns true on success. */
    public boolean login(Request request, String email, String token) {
        if (!isConfigured()) return false;
        if (isRateLimited(request.ip())) return false;

        // Tolerate copy-paste whitespace and email case differences
        String cleanEmail = email == null ? null : email.strip().toLowerCase();
        String cleanToken = token == null ? null : token.strip();
        String expectedEmail = adminEmail == null ? null : adminEmail.strip().toLowerCase();

        if (!constantTimeEquals(adminToken.strip(), cleanToken)
                || !constantTimeEquals(expectedEmail, cleanEmail)) {
            recordFailure(request.ip());
            return false;
        }

        failedAttempts.remove(request.ip());
        Auth.login(request, Principal.of("admin", cleanEmail, "admin"));
        return true;
    }

    /** Timing-safe string comparison so the token can't be guessed byte-by-byte. */
    private static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            actual.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isRateLimited(String ip) {
        long[] entry = failedAttempts.get(ip);
        if (entry == null) return false;
        long now = System.currentTimeMillis();
        if (now - entry[0] > ATTEMPT_WINDOW_MS) {
            failedAttempts.remove(ip);
            return false;
        }
        return entry[1] >= MAX_ATTEMPTS;
    }

    private void recordFailure(String ip) {
        long now = System.currentTimeMillis();
        failedAttempts.compute(ip, (k, entry) -> {
            if (entry == null || now - entry[0] > ATTEMPT_WINDOW_MS) {
                return new long[]{now, 1};
            }
            entry[1]++;
            return entry;
        });
        // Bound the map so attackers rotating IPs can't grow it forever
        if (failedAttempts.size() > 10_000) {
            failedAttempts.entrySet().removeIf(e -> now - e.getValue()[0] > ATTEMPT_WINDOW_MS);
        }
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
        return messageStore.findAll();
    }
}
