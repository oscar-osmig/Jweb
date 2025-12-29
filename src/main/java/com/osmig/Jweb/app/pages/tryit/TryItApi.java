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
            if ("APPROVED".equals(status) && TryItDb.isTokenValid(existing)) {
                return Response.json(Map.of("warning", true, "message", "You already have an active access token. Check your email for the download link."));
            }
            if ("PENDING".equals(status)) {
                return Response.json(Map.of("warning", true, "message", "A request was already submitted for this email. If approved, you'll receive a download token via email."));
            }
            TryItDb.deleteByEmail(email);
        }
        TryItDb.saveRequest(email, message);
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
