package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.api.GET;
import com.osmig.Jweb.framework.api.POST;
import com.osmig.Jweb.framework.api.REST;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.server.Response;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/** Public API endpoints for TryIt feature. */
@REST("/api/try-it")
public class TryItApi {
    private static final Logger LOG = Logger.getLogger(TryItApi.class.getName());
    private final ProjectGenerator projectGenerator;

    public TryItApi(ProjectGenerator projectGenerator) { this.projectGenerator = projectGenerator; }

    @POST("/request")
    public ResponseEntity<String> submitRequest(@RequestBody Map<String, String> body) {
        String email = body.get("email"), message = body.get("message");
        if (email == null || email.isBlank()) return Response.badRequest("Email is required");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return Response.badRequest("Please enter a valid email address");
        if (message == null || message.isBlank()) return Response.badRequest("Please tell us why you'd like to try JWeb");
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");

        Doc existing = TryItDb.findByEmail(email);
        if (existing != null) {
            String status = existing.getString("status");
            if ("APPROVED".equals(status) && TryItDb.isTokenValid(existing)) return Response.badRequest("You already have an active access token.");
            if ("PENDING".equals(status)) return Response.badRequest("Your request is still pending review.");
        }
        TryItDb.saveRequest(email, message);
        LOG.info("\n========== EMAIL ==========\nTo: the.jweb.team@gmail.com\nSubject: New JWeb Access Request from " + email + "\n\nEmail: " + email + "\nMessage: " + message + "\n===========================");
        return Response.json(Map.of("success", true, "message", "Your request has been submitted! We'll review it and send you a download token via email."));
    }

    @GET("/download")
    public ResponseEntity<?> download(@RequestParam String token, @RequestParam String email) {
        if (token == null || token.isBlank()) return Response.badRequest("Token is required");
        if (email == null || email.isBlank()) return Response.badRequest("Email is required");
        if (!TryItDb.ensureConnected()) return Response.serverError("Database not available");

        Doc request = TryItDb.findByToken(token);
        if (request == null || !TryItDb.isTokenValid(request)) return Response.unauthorized("Invalid or expired token");
        if (!request.getString("email").equalsIgnoreCase(email)) return Response.unauthorized("Email does not match the token");

        try {
            byte[] zipBytes = projectGenerator.generate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("jweb-starter.zip").build());
            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (IOException e) { return Response.serverError("Failed to generate project: " + e.getMessage()); }
    }

    @GET("/validate")
    public ResponseEntity<String> validateToken(@RequestParam String token, @RequestParam(required = false) String email) {
        if (!TryItDb.ensureConnected()) return Response.json(Map.of("valid", false, "error", "Database not available"));
        Doc request = TryItDb.findByToken(token);
        if (request == null || !TryItDb.isTokenValid(request)) return Response.json(Map.of("valid", false, "error", "Invalid or expired token"));
        if (email != null && !email.isBlank() && !request.getString("email").equalsIgnoreCase(email)) return Response.json(Map.of("valid", false, "error", "Email does not match the token"));
        return Response.json(Map.of("valid", true, "email", request.getString("email")));
    }
}
