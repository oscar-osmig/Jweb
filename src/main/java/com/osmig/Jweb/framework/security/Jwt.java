package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.server.Request;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * JWT utilities for token-based authentication.
 *
 * <p>Setup:</p>
 * <pre>
 * // Initialize with a secret (min 256 bits / 32 chars for HS256)
 * Jwt.init("your-256-bit-secret-key-here-min-32-chars");
 *
 * // Or use environment variable
 * Jwt.init(); // Uses JWT_SECRET env var
 * </pre>
 *
 * <p>Creating tokens:</p>
 * <pre>
 * // Simple token
 * String token = Jwt.create()
 *     .subject("user123")
 *     .expiresIn(Duration.ofHours(1))
 *     .sign();
 *
 * // Token with claims
 * String token = Jwt.create()
 *     .subject("user123")
 *     .claim("role", "ADMIN")
 *     .claim("email", "user@example.com")
 *     .expiresIn(Duration.ofDays(7))
 *     .sign();
 * </pre>
 *
 * <p>Validating tokens:</p>
 * <pre>
 * // Parse and validate
 * Jwt.Token token = Jwt.parse(tokenString);
 * String userId = token.subject();
 * String role = token.claim("role");
 *
 * // Check validity
 * if (Jwt.isValid(tokenString)) { ... }
 * </pre>
 *
 * <p>Middleware:</p>
 * <pre>
 * // Require valid JWT on all requests
 * app.use(Jwt.protect());
 *
 * // Protect specific paths
 * app.use(Jwt.protect().path("/api/**"));
 *
 * // Optional auth (parse but don't require)
 * app.use(Jwt.optional());
 * </pre>
 */
public final class Jwt {

    private static SecretKey secretKey;
    private static final String REQUEST_TOKEN_ATTR = "jwt_token";
    private static final String REQUEST_CLAIMS_ATTR = "jwt_claims";

    private Jwt() {}

    // ==================== Initialization ====================

    /**
     * Initializes JWT with the JWT_SECRET environment variable.
     */
    public static void init() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable must be set with at least 32 characters"
            );
        }
        init(secret);
    }

    /**
     * Initializes JWT with a secret key string.
     * Must be at least 32 characters (256 bits) for HS256.
     */
    public static void init(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Secret must be at least 32 characters for HS256");
        }
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Initializes JWT with a SecretKey.
     */
    public static void init(SecretKey key) {
        secretKey = key;
    }

    /**
     * Generates a new random secret key (for testing/development).
     */
    public static String generateSecret() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // ==================== Token Creation ====================

    /**
     * Starts building a new JWT token.
     */
    public static TokenBuilder create() {
        return new TokenBuilder();
    }

    /**
     * Creates a simple token with subject and expiration.
     */
    public static String token(String subject, Duration expiresIn) {
        return create().subject(subject).expiresIn(expiresIn).sign();
    }

    // ==================== Token Validation ====================

    /**
     * Parses and validates a JWT token.
     * @throws JwtException if token is invalid or expired
     */
    public static Token parse(String token) {
        ensureInitialized();
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return new Token(claims);
    }

    /**
     * Checks if a token is valid (not expired, properly signed).
     */
    public static boolean isValid(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the token from Authorization header (Bearer token).
     */
    public static Optional<String> extractToken(Request request) {
        String auth = request.header("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return Optional.of(auth.substring(7));
        }
        return Optional.empty();
    }

    /**
     * Gets parsed token from request (set by middleware).
     */
    public static Optional<Token> getToken(Request request) {
        Object token = request.raw().getAttribute(REQUEST_TOKEN_ATTR);
        return token instanceof Token t ? Optional.of(t) : Optional.empty();
    }

    // ==================== Middleware ====================

    /**
     * Creates middleware that requires a valid JWT token.
     * Returns 401 if missing or invalid.
     */
    public static Middleware protect() {
        return protect(null);
    }

    /**
     * Creates middleware that requires a valid JWT token.
     * Uses custom error handler for invalid tokens.
     */
    public static Middleware protect(Consumer<Request> onError) {
        return (req, chain) -> {
            Optional<String> tokenOpt = extractToken(req);

            if (tokenOpt.isEmpty()) {
                if (onError != null) onError.accept(req);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing Authorization header"));
            }

            try {
                Token token = parse(tokenOpt.get());
                req.raw().setAttribute(REQUEST_TOKEN_ATTR, token);
                req.raw().setAttribute(REQUEST_CLAIMS_ATTR, token.claims);
                return chain.next();
            } catch (ExpiredJwtException e) {
                if (onError != null) onError.accept(req);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token expired"));
            } catch (JwtException e) {
                if (onError != null) onError.accept(req);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
            }
        };
    }

    /**
     * Creates middleware that parses JWT if present but doesn't require it.
     * Token will be available via Jwt.getToken(request) if valid.
     */
    public static Middleware optional() {
        return (req, chain) -> {
            extractToken(req).ifPresent(tokenStr -> {
                try {
                    Token token = parse(tokenStr);
                    req.raw().setAttribute(REQUEST_TOKEN_ATTR, token);
                    req.raw().setAttribute(REQUEST_CLAIMS_ATTR, token.claims);
                } catch (JwtException ignored) {
                    // Invalid token - just continue without it
                }
            });
            return chain.next();
        };
    }

    // ==================== Helpers ====================

    private static void ensureInitialized() {
        if (secretKey == null) {
            throw new IllegalStateException("JWT not initialized. Call Jwt.init(secret) first.");
        }
    }

    // ==================== Token Builder ====================

    public static class TokenBuilder {
        private String subject;
        private String issuer;
        private String audience;
        private Instant expiration;
        private Instant notBefore;
        private final Map<String, Object> claims = new HashMap<>();

        TokenBuilder() {}

        /**
         * Sets the subject (usually user ID).
         */
        public TokenBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets the issuer.
         */
        public TokenBuilder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Sets the audience.
         */
        public TokenBuilder audience(String audience) {
            this.audience = audience;
            return this;
        }

        /**
         * Sets expiration as Duration from now.
         */
        public TokenBuilder expiresIn(Duration duration) {
            this.expiration = Instant.now().plus(duration);
            return this;
        }

        /**
         * Sets expiration as absolute time.
         */
        public TokenBuilder expiresAt(Instant instant) {
            this.expiration = instant;
            return this;
        }

        /**
         * Token not valid before this time.
         */
        public TokenBuilder notBefore(Instant instant) {
            this.notBefore = instant;
            return this;
        }

        /**
         * Adds a custom claim.
         */
        public TokenBuilder claim(String name, Object value) {
            claims.put(name, value);
            return this;
        }

        /**
         * Adds multiple claims.
         */
        public TokenBuilder claims(Map<String, Object> claims) {
            this.claims.putAll(claims);
            return this;
        }

        /**
         * Adds user roles as a claim.
         */
        public TokenBuilder roles(String... roles) {
            claims.put("roles", Arrays.asList(roles));
            return this;
        }

        /**
         * Adds user roles as a claim.
         */
        public TokenBuilder roles(List<String> roles) {
            claims.put("roles", roles);
            return this;
        }

        /**
         * Signs and returns the JWT string.
         */
        public String sign() {
            ensureInitialized();

            var builder = Jwts.builder()
                .claims(claims);

            if (subject != null) builder.subject(subject);
            if (issuer != null) builder.issuer(issuer);
            if (audience != null) builder.audience().add(audience);
            if (expiration != null) builder.expiration(Date.from(expiration));
            if (notBefore != null) builder.notBefore(Date.from(notBefore));

            builder.issuedAt(new Date());
            builder.id(UUID.randomUUID().toString());

            return builder.signWith(secretKey).compact();
        }
    }

    // ==================== Token (parsed) ====================

    public static class Token {
        private final Claims claims;

        Token(Claims claims) {
            this.claims = claims;
        }

        /**
         * Gets the subject (usually user ID).
         */
        public String subject() {
            return claims.getSubject();
        }

        /**
         * Gets the issuer.
         */
        public String issuer() {
            return claims.getIssuer();
        }

        /**
         * Gets the token ID (jti).
         */
        public String id() {
            return claims.getId();
        }

        /**
         * Gets when the token was issued.
         */
        public Instant issuedAt() {
            Date iat = claims.getIssuedAt();
            return iat != null ? iat.toInstant() : null;
        }

        /**
         * Gets when the token expires.
         */
        public Instant expiration() {
            Date exp = claims.getExpiration();
            return exp != null ? exp.toInstant() : null;
        }

        /**
         * Checks if the token is expired.
         */
        public boolean isExpired() {
            Instant exp = expiration();
            return exp != null && Instant.now().isAfter(exp);
        }

        /**
         * Gets a custom claim value.
         */
        @SuppressWarnings("unchecked")
        public <T> T claim(String name) {
            return (T) claims.get(name);
        }

        /**
         * Gets a claim with type.
         */
        public <T> T claim(String name, Class<T> type) {
            return claims.get(name, type);
        }

        /**
         * Gets roles claim as list.
         */
        @SuppressWarnings("unchecked")
        public List<String> roles() {
            Object roles = claims.get("roles");
            if (roles instanceof List<?> list) {
                return (List<String>) list;
            }
            return Collections.emptyList();
        }

        /**
         * Checks if token has a specific role.
         */
        public boolean hasRole(String role) {
            return roles().contains(role);
        }

        /**
         * Checks if token has any of the specified roles.
         */
        public boolean hasAnyRole(String... roles) {
            List<String> tokenRoles = roles();
            for (String role : roles) {
                if (tokenRoles.contains(role)) return true;
            }
            return false;
        }

        /**
         * Gets all claims as a map.
         */
        public Map<String, Object> allClaims() {
            return new HashMap<>(claims);
        }
    }
}
