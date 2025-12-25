package com.osmig.Jweb.app.tryit;

import com.osmig.Jweb.framework.api.GET;
import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.db.mongo.Mongo;
import com.osmig.Jweb.framework.server.Response;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API for the Try It feature.
 * Uses JWeb framework's Mongo/Doc directly - no models, repositories, or services needed.
 */
@REST("/api/try-it")
public class TryItApi {

    private static final Logger LOG = Logger.getLogger(TryItApi.class.getName());
    private static final String COLLECTION = "access_requests";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_VALIDITY_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofPattern("MMM dd, yyyy 'at' HH:mm")
        .withZone(ZoneId.systemDefault());

    private final ProjectGenerator projectGenerator;
    private boolean mongoAvailable = false;
    private boolean initialized = false;

    public TryItApi(ProjectGenerator projectGenerator) {
        this.projectGenerator = projectGenerator;
    }

    private synchronized void ensureMongo() {
        if (initialized) return;
        initialized = true;
        try {
            Mongo.connect("mongodb://localhost:27017", "jweb");
            Mongo.getDatabase().listCollectionNames().first();
            mongoAvailable = true;
            LOG.info("MongoDB connected for TryIt API");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "MongoDB not available: " + e.getMessage());
        }
    }

    // ==================== Public Endpoints ====================

    @POST("/request")
    public ResponseEntity<String> submitRequest(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String message = body.get("message");

        if (email == null || email.isBlank()) {
            return Response.badRequest("Email is required");
        }
        if (!isValidEmail(email)) {
            return Response.badRequest("Please enter a valid email address");
        }
        if (message == null || message.isBlank()) {
            return Response.badRequest("Please tell us why you'd like to try JWeb");
        }

        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        // Check for existing request
        Doc existing = Mongo.find(COLLECTION).where("email", email).first();
        if (existing != null) {
            String status = existing.getString("status");
            if ("APPROVED".equals(status) && isTokenValid(existing)) {
                return Response.badRequest("You already have an active access token.");
            }
            if ("PENDING".equals(status)) {
                return Response.badRequest("Your request is still pending review.");
            }
        }

        // Create new request
        Doc request = Doc.of(COLLECTION)
            .set("email", email)
            .set("message", message)
            .set("status", "PENDING")
            .set("createdAt", Instant.now().toEpochMilli());

        Mongo.save(request);
        logEmail("the.jweb.team@gmail.com", "New JWeb Access Request from " + email,
            "Email: " + email + "\nMessage: " + message);

        return Response.json(Map.of(
            "success", true,
            "message", "Your request has been submitted! We'll review it and send you a download token via email."
        ));
    }

    @GET("/download")
    public ResponseEntity<?> download(@RequestParam String token, @RequestParam String email) {
        if (token == null || token.isBlank()) {
            return Response.badRequest("Token is required");
        }
        if (email == null || email.isBlank()) {
            return Response.badRequest("Email is required");
        }

        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        Doc request = Mongo.find(COLLECTION).where("token", token).first();
        if (request == null || !isTokenValid(request)) {
            return Response.unauthorized("Invalid or expired token");
        }
        if (!request.getString("email").equalsIgnoreCase(email)) {
            return Response.unauthorized("Email does not match the token");
        }

        try {
            byte[] zipBytes = projectGenerator.generate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("jweb-starter.zip")
                .build());
            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return Response.serverError("Failed to generate project: " + e.getMessage());
        }
    }

    @GET("/validate")
    public ResponseEntity<String> validateToken(
            @RequestParam String token,
            @RequestParam(required = false) String email) {
        ensureMongo();
        if (!mongoAvailable) {
            return Response.json(Map.of("valid", false, "error", "Database not available"));
        }

        Doc request = Mongo.find(COLLECTION).where("token", token).first();
        if (request == null || !isTokenValid(request)) {
            return Response.json(Map.of("valid", false, "error", "Invalid or expired token"));
        }
        if (email != null && !email.isBlank() && !request.getString("email").equalsIgnoreCase(email)) {
            return Response.json(Map.of("valid", false, "error", "Email does not match the token"));
        }

        return Response.json(Map.of("valid", true, "email", request.getString("email")));
    }

    // ==================== Admin Endpoints ====================

    @GET("/admin/requests")
    public ResponseEntity<String> getPendingRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return Response.unauthorized();
        }
        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        var requests = Mongo.find(COLLECTION)
            .where("status", "PENDING")
            .toList()
            .stream()
            .map(this::docToMap)
            .toList();

        return Response.json(requests);
    }

    @GET("/admin/requests/all")
    public ResponseEntity<String> getAllRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return Response.unauthorized();
        }
        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        var requests = Mongo.find(COLLECTION)
            .toList()
            .stream()
            .map(this::docToMap)
            .toList();

        return Response.json(requests);
    }

    @POST("/admin/approve/{id}")
    public ResponseEntity<String> approveRequest(
            @PathVariable String id,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return Response.unauthorized();
        }
        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        Doc request = Mongo.findById(COLLECTION, id);
        if (request == null) {
            return Response.badRequest("Request not found");
        }
        if ("APPROVED".equals(request.getString("status")) && isTokenValid(request)) {
            return Response.badRequest("Request already approved with active token");
        }

        String token = generateToken();
        long expiry = Instant.now().plus(TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS).toEpochMilli();

        Mongo.update(COLLECTION)
            .where("id", id)
            .set("token", token)
            .set("tokenExpiry", expiry)
            .set("status", "APPROVED")
            .set("approvedAt", Instant.now().toEpochMilli())
            .execute();

        String email = request.getString("email");
        logEmail(email, "Your JWeb Framework Access Has Been Approved!",
            "Your token: " + token + "\nValid until: " + DATE_FORMAT.format(Instant.ofEpochMilli(expiry)));

        return Response.json(Map.of(
            "success", true,
            "message", "Request approved. Token sent to " + email,
            "token", token
        ));
    }

    @POST("/admin/reject/{id}")
    public ResponseEntity<String> rejectRequest(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return Response.unauthorized();
        }
        ensureMongo();
        if (!mongoAvailable) {
            return Response.serverError("Database not available");
        }

        Doc request = Mongo.findById(COLLECTION, id);
        if (request == null) {
            return Response.badRequest("Request not found");
        }

        Mongo.update(COLLECTION)
            .where("id", id)
            .set("status", "REJECTED")
            .execute();

        String reason = body != null ? body.get("reason") : null;
        logEmail(request.getString("email"), "Update on Your JWeb Framework Access Request",
            "Your request has been rejected." + (reason != null ? " Reason: " + reason : ""));

        return Response.json(Map.of("success", true, "message", "Request rejected"));
    }

    // ==================== Helpers ====================

    private boolean isTokenValid(Doc doc) {
        String token = doc.getString("token");
        Long expiry = doc.getLong("tokenExpiry");
        return token != null && expiry != null && Instant.now().toEpochMilli() < expiry;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidAdmin(String key, String email) {
        String expectedKey = System.getenv().getOrDefault("JWEB_ADMIN_KEY", "jweb-admin-secret-key");
        String expectedEmail = System.getenv().getOrDefault("JWEB_ADMIN_EMAIL", "the.jweb.team@gmail.com");
        return expectedKey.equals(key) && expectedEmail.equalsIgnoreCase(email);
    }

    private Map<String, Object> docToMap(Doc doc) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", doc.getId() != null ? doc.getId() : "");
        map.put("email", doc.getString("email", ""));
        map.put("message", doc.getString("message", ""));
        map.put("status", doc.getString("status", ""));
        map.put("createdAt", doc.getLong("createdAt", 0L));
        map.put("approvedAt", doc.getLong("approvedAt", 0L));
        map.put("token", doc.getString("token") != null ? doc.getString("token") : "");
        map.put("tokenExpiry", doc.getLong("tokenExpiry", 0L));
        return map;
    }

    private void logEmail(String to, String subject, String body) {
        LOG.info("\n========== EMAIL ==========\nTo: " + to + "\nSubject: " + subject + "\n\n" + body + "\n===========================");
    }
}
