package com.osmig.Jweb.app.tryit;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API for the Try It feature.
 */
@RestController
@RequestMapping("/api/try-it")
public class TryItApi {

    private final AccessRequestService requestService;
    private final ProjectGenerator projectGenerator;

    public TryItApi(AccessRequestService requestService, ProjectGenerator projectGenerator) {
        this.requestService = requestService;
        this.projectGenerator = projectGenerator;
    }

    /**
     * Submit an access request.
     */
    @PostMapping("/request")
    public ResponseEntity<?> submitRequest(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String message = body.get("message");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email is required"));
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Please enter a valid email address"));
        }

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Please tell us why you'd like to try JWeb"));
        }

        try {
            AccessRequest request = requestService.submitRequest(email, message);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your request has been submitted! We'll review it and send you a download token via email."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Download the project with a valid token and matching email.
     */
    @GetMapping("/download")
    public ResponseEntity<?> download(
            @RequestParam String token,
            @RequestParam String email) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Token is required"));
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email is required"));
        }

        var validRequest = requestService.validateToken(token);
        if (validRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired token"));
        }

        // Check email matches the token
        if (!validRequest.get().getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email does not match the token"));
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate project: " + e.getMessage()));
        }
    }

    /**
     * Validate a token and email combination.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam String token,
            @RequestParam(required = false) String email) {
        var validRequest = requestService.validateToken(token);

        if (validRequest.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "error", "Invalid or expired token"
            ));
        }

        // If email provided, check it matches
        if (email != null && !email.isBlank()) {
            if (!validRequest.get().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "error", "Email does not match the token"
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
            "valid", true,
            "email", validRequest.get().getEmail()
        ));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // ==================== Admin Endpoints ====================

    /**
     * Get all pending requests (admin only).
     */
    @GetMapping("/admin/requests")
    public ResponseEntity<?> getPendingRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized"));
        }

        List<AccessRequest> requests = requestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all requests (admin only).
     */
    @GetMapping("/admin/requests/all")
    public ResponseEntity<?> getAllRequests(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {
        if (!isValidAdmin(adminKey, adminEmail)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized"));
        }

        List<AccessRequest> requests = requestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a request (admin only).
     */
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<?> approveRequest(
            @PathVariable String id,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {

        if (!isValidAdmin(adminKey, adminEmail)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized"));
        }

        try {
            AccessRequest request = requestService.approveRequest(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request approved. Token sent to " + request.getEmail(),
                "token", request.getToken()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reject a request (admin only).
     */
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {

        if (!isValidAdmin(adminKey, adminEmail)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized"));
        }

        String reason = body != null ? body.get("reason") : null;

        try {
            requestService.rejectRequest(id, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request rejected"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isValidAdmin(String key, String email) {
        // Check admin key
        String expectedKey = System.getenv("JWEB_ADMIN_KEY");
        if (expectedKey == null) {
            expectedKey = "jweb-admin-secret-key"; // Default for development
        }

        // Check admin email
        String expectedEmail = System.getenv("JWEB_ADMIN_EMAIL");
        if (expectedEmail == null) {
            expectedEmail = "the.jweb.team@gmail.com"; // Default admin email
        }

        return expectedKey.equals(key) && expectedEmail.equalsIgnoreCase(email);
    }
}
