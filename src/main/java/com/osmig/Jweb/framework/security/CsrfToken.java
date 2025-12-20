package com.osmig.Jweb.framework.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF token for protection against Cross-Site Request Forgery attacks.
 *
 * <p>A CSRF token is a unique, unpredictable value that is generated for each
 * user session. The token must be included in state-changing requests (POST, PUT,
 * DELETE) and validated on the server to ensure the request originated from a
 * legitimate page.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * // Generate token
 * CsrfToken token = CsrfToken.generate();
 *
 * // Store in session
 * request.raw().getSession().setAttribute("csrfToken", token);
 *
 * // Include in forms
 * form(
 *     Csrf.tokenField(token),
 *     // ... other fields
 * )
 *
 * // Validate on POST
 * String submittedToken = request.formParam("_csrf");
 * if (!token.matches(submittedToken)) {
 *     throw new CsrfException("Invalid CSRF token");
 * }
 * </pre>
 */
public class CsrfToken {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32; // 256 bits
    public static final String TOKEN_PARAM_NAME = "_csrf";
    public static final String TOKEN_HEADER_NAME = "X-CSRF-TOKEN";

    private final String value;
    private final long createdAt;
    private final long expiresAt;

    /**
     * Creates a new CSRF token with the given value.
     *
     * @param value     the token value
     * @param expiresAt expiration timestamp (millis)
     */
    private CsrfToken(String value, long expiresAt) {
        this.value = value;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = expiresAt;
    }

    /**
     * Generates a new cryptographically secure CSRF token.
     *
     * @return a new CSRF token
     */
    public static CsrfToken generate() {
        return generate(30 * 60 * 1000L); // 30 minutes default expiry
    }

    /**
     * Generates a new CSRF token with custom expiry.
     *
     * @param expiryMillis how long the token is valid (milliseconds)
     * @return a new CSRF token
     */
    public static CsrfToken generate(long expiryMillis) {
        byte[] bytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new CsrfToken(value, System.currentTimeMillis() + expiryMillis);
    }

    /**
     * Creates a token from an existing value (e.g., from session).
     *
     * @param value     the token value
     * @param expiresAt expiration timestamp
     * @return the token
     */
    public static CsrfToken of(String value, long expiresAt) {
        return new CsrfToken(value, expiresAt);
    }

    /**
     * Gets the token value for embedding in forms/headers.
     *
     * @return the token value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets when the token was created.
     *
     * @return creation timestamp in milliseconds
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets when the token expires.
     *
     * @return expiration timestamp in milliseconds
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks if the token has expired.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    /**
     * Validates a submitted token against this token.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param submittedToken the token submitted by the client
     * @return true if valid and not expired
     */
    public boolean matches(String submittedToken) {
        if (submittedToken == null || isExpired()) {
            return false;
        }
        return constantTimeEquals(value, submittedToken);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    @Override
    public String toString() {
        return "CsrfToken[" + value.substring(0, 8) + "..., expired=" + isExpired() + "]";
    }
}
