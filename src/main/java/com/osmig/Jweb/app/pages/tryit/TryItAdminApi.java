package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.api.GET;
import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.server.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;

/** Admin API endpoints for managing access requests. */
@REST("/api/try-it/admin")
public class TryItAdminApi {
    private static final Logger LOG = Logger.getLogger(TryItAdminApi.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm").withZone(ZoneId.systemDefault());

    @Value("${jweb.admin.token}")
    private String adminToken;

    @Value("${jweb.admin.email}")
    private String adminEmail;

    @GET("/requests")
    public ResponseEntity<String> getPendingRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        return Response.json(TryItDb.findByStatus("PENDING").stream().map(TryItDb::docToMap).toList());
    }

    @GET("/requests/all")
    public ResponseEntity<String> getAllRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        return Response.json(TryItDb.findAll().stream().map(TryItDb::docToMap).toList());
    }

    @POST("/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable String id,
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        Doc request = TryItDb.findById(id);
        if (request == null) return Response.badRequest("Request not found");
        if ("APPROVED".equals(request.getString("status")) && TryItDb.isTokenValid(request))
            return Response.badRequest("Request already approved with active token");

        String token = TryItDb.approveRequest(id);
        String reqEmail = request.getString("email");
        long expiry = Instant.now().plusSeconds(7 * 24 * 60 * 60).toEpochMilli();
        logEmail(reqEmail, "Your JWeb Framework Access Has Been Approved!", "Your token: " + token + "\nValid until: " + DATE_FMT.format(Instant.ofEpochMilli(expiry)));
        return Response.json(Map.of("success", true, "message", "Request approved. Token sent to " + reqEmail, "token", token));
    }

    @POST("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable String id, @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestHeader(value = "X-Admin-Email", required = false) String email) {
        if (!isValidAdmin(key, email)) return Response.unauthorized();
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");
        Doc request = TryItDb.findById(id);
        if (request == null) return Response.badRequest("Request not found");
        TryItDb.rejectRequest(id);
        String reason = body != null ? body.get("reason") : null;
        logEmail(request.getString("email"), "Update on Your JWeb Framework Access Request", "Your request has been rejected." + (reason != null ? " Reason: " + reason : ""));
        return Response.json(Map.of("success", true, "message", "Request rejected"));
    }

    private boolean isValidAdmin(String key, String email) {
        return adminToken.equals(key) && adminEmail.equalsIgnoreCase(email);
    }

    private void logEmail(String to, String subject, String body) {
        LOG.info("\n========== EMAIL ==========\nTo: " + to + "\nSubject: " + subject + "\n\n" + body + "\n===========================");
    }
}
