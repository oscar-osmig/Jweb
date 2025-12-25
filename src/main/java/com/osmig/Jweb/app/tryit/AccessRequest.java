package com.osmig.Jweb.app.tryit;

import java.time.Instant;

/**
 * Model representing an access request to try JWeb.
 */
public class AccessRequest {
    private String id;
    private String email;
    private String message;
    private Status status;
    private String token;
    private Instant tokenExpiry;
    private Instant createdAt;
    private Instant approvedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public AccessRequest() {
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }

    public AccessRequest(String email, String message) {
        this();
        this.email = email;
        this.message = message;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(Instant tokenExpiry) { this.tokenExpiry = tokenExpiry; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public boolean isTokenValid() {
        return token != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry);
    }
}
