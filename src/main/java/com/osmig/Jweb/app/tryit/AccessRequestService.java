package com.osmig.Jweb.app.tryit;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing access requests.
 */
@Service
public class AccessRequestService {

    private static final Duration TOKEN_VALIDITY = Duration.ofDays(7); // 7 days
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccessRequestRepository repository;
    private final EmailService emailService;

    public AccessRequestService(AccessRequestRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    /**
     * Submit a new access request.
     */
    public AccessRequest submitRequest(String email, String message) {
        // Check if request already exists
        Optional<AccessRequest> existing = repository.findByEmail(email);
        if (existing.isPresent()) {
            AccessRequest req = existing.get();
            if (req.getStatus() == AccessRequest.Status.APPROVED && req.isTokenValid()) {
                throw new IllegalStateException("You already have an active access token.");
            }
            if (req.getStatus() == AccessRequest.Status.PENDING) {
                throw new IllegalStateException("Your request is still pending review.");
            }
        }

        // Create new request
        AccessRequest request = new AccessRequest(email, message);
        repository.save(request);

        // Notify team
        emailService.notifyTeamOfNewRequest(request);

        return request;
    }

    /**
     * Approve a request and generate a download token.
     */
    public AccessRequest approveRequest(String requestId) {
        AccessRequest request = repository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() == AccessRequest.Status.APPROVED && request.isTokenValid()) {
            throw new IllegalStateException("Request already approved with active token");
        }

        // Generate unique token
        String token = generateToken();
        Instant expiry = Instant.now().plus(TOKEN_VALIDITY);

        // Update request
        repository.setToken(requestId, token, expiry);
        request.setToken(token);
        request.setTokenExpiry(expiry);
        request.setStatus(AccessRequest.Status.APPROVED);
        request.setApprovedAt(Instant.now());

        // Send token to user
        emailService.sendApprovalEmail(request);

        return request;
    }

    /**
     * Reject a request.
     */
    public void rejectRequest(String requestId, String reason) {
        AccessRequest request = repository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        repository.updateStatus(requestId, AccessRequest.Status.REJECTED);

        // Notify user
        emailService.sendRejectionEmail(request, reason);
    }

    /**
     * Validate a download token.
     */
    public Optional<AccessRequest> validateToken(String token) {
        return repository.findByToken(token)
            .filter(AccessRequest::isTokenValid);
    }

    /**
     * Get all pending requests.
     */
    public List<AccessRequest> getPendingRequests() {
        return repository.findByStatus(AccessRequest.Status.PENDING);
    }

    /**
     * Get all requests.
     */
    public List<AccessRequest> getAllRequests() {
        return repository.findAll();
    }

    /**
     * Get a request by ID.
     */
    public Optional<AccessRequest> getRequest(String id) {
        return repository.findById(id);
    }

    /**
     * Generate a secure random token.
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
