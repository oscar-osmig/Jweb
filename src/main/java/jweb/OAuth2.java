package jweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth2 client for social login (Google, GitHub, Discord, etc.).
 *
 * <p>Setup:</p>
 * <pre>
 * // Configure providers
 * OAuth2.Provider google = OAuth2.google()
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .redirectUri("http://localhost:8080/auth/google/callback")
 *     .build();
 *
 * OAuth2.Provider github = OAuth2.github()
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .redirectUri("http://localhost:8080/auth/github/callback")
 *     .build();
 * </pre>
 *
 * <p>Login flow — the state and the PKCE verifier live in the visitor's
 * session, so the callback can land on any instance:</p>
 * <pre>
 * // 1. Send the user to the provider
 * app.get("/auth/google", req -> Response.redirect(google.authorizationUrl(req)));
 *
 * // 2. The provider sends them back
 * app.get("/auth/google/callback", req -> {
 *     OAuth2.Login login = google.callback(req);      // state checked, code exchanged, profile fetched
 *     OAuth2.UserInfo user = login.user();
 *     Auth.login(req, Principal.of(user.id(), user.email(), "user"));
 *     return Response.redirect("/");
 * });
 * </pre>
 *
 * <p>{@link Provider#callback(Request)} throws {@link OAuth2Exception} when
 * the provider reported an error, the state does not match the session's, the
 * code is missing, or the exchange failed; the message says which.</p>
 */
public class OAuth2 {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private static final Map<String, Long> stateStore = new ConcurrentHashMap<>();
    private static final long STATE_EXPIRY_MS = 10 * 60 * 1000; // 10 minutes
    private static final String SESSION_KEY = "jweb.oauth2.";

    protected OAuth2() {}

    // ==================== Provider Builders ====================

    /**
     * Creates a Google OAuth2 provider builder.
     */
    public static ProviderBuilder google() {
        return new ProviderBuilder()
            .name("google")
            .authorizationUrl("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUrl("https://oauth2.googleapis.com/token")
            .userInfoUrl("https://www.googleapis.com/oauth2/v2/userinfo")
            .scopes("openid", "email", "profile");
    }

    /**
     * Creates a GitHub OAuth2 provider builder.
     */
    public static ProviderBuilder github() {
        return new ProviderBuilder()
            .name("github")
            .authorizationUrl("https://github.com/login/oauth/authorize")
            .tokenUrl("https://github.com/login/oauth/access_token")
            .userInfoUrl("https://api.github.com/user")
            .scopes("user:email");
    }

    /**
     * Creates a Discord OAuth2 provider builder.
     */
    public static ProviderBuilder discord() {
        return new ProviderBuilder()
            .name("discord")
            .authorizationUrl("https://discord.com/api/oauth2/authorize")
            .tokenUrl("https://discord.com/api/oauth2/token")
            .userInfoUrl("https://discord.com/api/users/@me")
            .scopes("identify", "email");
    }

    /**
     * Creates a Microsoft OAuth2 provider builder.
     */
    public static ProviderBuilder microsoft() {
        return new ProviderBuilder()
            .name("microsoft")
            .authorizationUrl("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
            .tokenUrl("https://login.microsoftonline.com/common/oauth2/v2.0/token")
            .userInfoUrl("https://graph.microsoft.com/v1.0/me")
            .scopes("openid", "email", "profile");
    }

    /**
     * Creates a custom OAuth2 provider builder.
     */
    public static ProviderBuilder custom(String name) {
        return new ProviderBuilder().name(name);
    }

    // ==================== State Management ====================

    /**
     * Generates a CSRF state token held in this instance's memory.
     *
     * @deprecated The session-backed flow — {@link Provider#authorizationUrl(Request)}
     * and {@link Provider#callback(Request)} — works across instances and adds PKCE.
     */
    @Deprecated
    public static String generateState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        stateStore.put(state, System.currentTimeMillis());
        cleanupExpiredStates();
        return state;
    }

    /**
     * Verifies and consumes an in-memory state token.
     *
     * @deprecated See {@link #generateState()}.
     */
    @Deprecated
    public static boolean verifyState(String state) {
        if (state == null) return false;
        Long timestamp = stateStore.remove(state);
        if (timestamp == null) return false;
        return System.currentTimeMillis() - timestamp < STATE_EXPIRY_MS;
    }

    private static void cleanupExpiredStates() {
        long now = System.currentTimeMillis();
        stateStore.entrySet().removeIf(e -> now - e.getValue() > STATE_EXPIRY_MS);
    }

    // ==================== Provider Builder ====================

    public static class ProviderBuilder {
        private String name;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationUrl;
        private String tokenUrl;
        private String userInfoUrl;
        private final List<String> scopes = new ArrayList<>();
        private boolean pkce = true;

        ProviderBuilder() {}

        public ProviderBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProviderBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ProviderBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public ProviderBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public ProviderBuilder authorizationUrl(String url) {
            this.authorizationUrl = url;
            return this;
        }

        public ProviderBuilder tokenUrl(String url) {
            this.tokenUrl = url;
            return this;
        }

        public ProviderBuilder userInfoUrl(String url) {
            this.userInfoUrl = url;
            return this;
        }

        public ProviderBuilder scopes(String... scopes) {
            this.scopes.addAll(Arrays.asList(scopes));
            return this;
        }

        public ProviderBuilder addScope(String scope) {
            this.scopes.add(scope);
            return this;
        }

        /** PKCE (S256) is on for the session-backed flow; a provider that rejects it turns it off here. */
        public ProviderBuilder pkce(boolean enabled) {
            this.pkce = enabled;
            return this;
        }

        public Provider build() {
            Objects.requireNonNull(clientId, "clientId is required");
            Objects.requireNonNull(clientSecret, "clientSecret is required");
            Objects.requireNonNull(redirectUri, "redirectUri is required");
            Objects.requireNonNull(authorizationUrl, "authorizationUrl is required");
            Objects.requireNonNull(tokenUrl, "tokenUrl is required");

            return new Provider(
                name, clientId, clientSecret, redirectUri,
                authorizationUrl, tokenUrl, userInfoUrl,
                String.join(" ", scopes), pkce
            );
        }
    }

    // ==================== Provider ====================

    public static class Provider {
        private final String name;
        private final String clientId;
        private final String clientSecret;
        private final String redirectUri;
        private final String authorizationUrl;
        private final String tokenUrl;
        private final String userInfoUrl;
        private final String scope;
        private final boolean pkce;

        Provider(String name, String clientId, String clientSecret, String redirectUri,
                 String authorizationUrl, String tokenUrl, String userInfoUrl, String scope) {
            this(name, clientId, clientSecret, redirectUri, authorizationUrl, tokenUrl, userInfoUrl, scope, true);
        }

        Provider(String name, String clientId, String clientSecret, String redirectUri,
                 String authorizationUrl, String tokenUrl, String userInfoUrl, String scope, boolean pkce) {
            this.name = name;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
            this.authorizationUrl = authorizationUrl;
            this.tokenUrl = tokenUrl;
            this.userInfoUrl = userInfoUrl;
            this.scope = scope;
            this.pkce = pkce;
        }

        // ==================== the session-backed flow ====================

        /**
         * The URL to send the visitor to. A fresh state and, with PKCE, a code
         * verifier are stored in the visitor's session (ten minutes), so the
         * callback is checked against what this visitor started, on any instance.
         */
        public String authorizationUrl(Request request) {
            String state = randomToken();
            String verifier = pkce ? randomToken() : "";
            long expires = System.currentTimeMillis() + STATE_EXPIRY_MS;
            Session.of(request).put(SESSION_KEY + name, state + " " + verifier + " " + expires);
            String url = authorizationUrl(state);
            if (pkce) {
                url += "&code_challenge=" + encode(s256(verifier)) + "&code_challenge_method=S256";
            }
            return url;
        }

        /**
         * Finishes the login the provider sent back: checks the state against
         * the session (single use), exchanges the code (with the PKCE verifier),
         * and fetches the profile.
         *
         * @throws OAuth2Exception the provider's error, a state mismatch or
         *                         expiry, a missing code, or a failed exchange
         */
        public Login callback(Request request) throws OAuth2Exception {
            String error = request.query("error");
            if (error != null && !error.isEmpty()) {
                String description = request.query("error_description");
                throw new OAuth2Exception(name + " refused the login: " + error
                    + (description == null ? "" : " — " + description));
            }
            Session session = Session.of(request);
            String stored = session.get(SESSION_KEY + name, String.class);
            session.remove(SESSION_KEY + name);
            if (stored == null) {
                throw new OAuth2Exception("No " + name + " login in progress for this session (expired, or started elsewhere)");
            }
            String[] parts = stored.split(" ", 3);
            String state = request.query("state");
            if (state == null || parts.length < 3 || !constantTimeEquals(parts[0], state)) {
                throw new OAuth2Exception("OAuth2 state does not match the session's — the login was not started here");
            }
            if (System.currentTimeMillis() > Long.parseLong(parts[2])) {
                throw new OAuth2Exception("The " + name + " login took longer than ten minutes; start again");
            }
            String code = request.query("code");
            if (code == null || code.isEmpty()) {
                throw new OAuth2Exception(name + " sent no authorization code");
            }
            String verifier = parts[1].isEmpty() ? null : parts[1];
            TokenResponse tokens = exchangeCode(code, verifier);
            UserInfo user = userInfoUrl == null ? null : getUserInfo(tokens.accessToken());
            return new Login(tokens, user);
        }

        /** Whether PKCE is on for this provider. */
        public boolean pkce() {
            return pkce;
        }

        // ==================== the pieces ====================

        /**
         * Generates the authorization URL with an in-memory state token.
         *
         * @deprecated Use {@link #authorizationUrl(Request)}: the state lives in the session.
         */
        @Deprecated
        public String authorizationUrl() {
            String state = generateState();
            return authorizationUrl(state);
        }

        /**
         * Generates the authorization URL with a custom state.
         */
        public String authorizationUrl(String state) {
            StringBuilder url = new StringBuilder(authorizationUrl);
            url.append("?response_type=code");
            url.append("&client_id=").append(encode(clientId));
            url.append("&redirect_uri=").append(encode(redirectUri));
            url.append("&state=").append(encode(state));
            if (scope != null && !scope.isEmpty()) {
                url.append("&scope=").append(encode(scope));
            }
            // Google specific
            if ("google".equals(name)) {
                url.append("&access_type=offline");
                url.append("&prompt=consent");
            }
            return url.toString();
        }

        /**
         * Exchanges an authorization code for tokens.
         */
        public TokenResponse exchangeCode(String code) throws OAuth2Exception {
            return exchangeCode(code, null);
        }

        /**
         * Exchanges an authorization code for tokens, with the PKCE verifier the
         * authorization request carried the challenge for.
         */
        public TokenResponse exchangeCode(String code, String codeVerifier) throws OAuth2Exception {
            String body = "grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirectUri)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + (codeVerifier == null ? "" : "&code_verifier=" + encode(codeVerifier));

            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new OAuth2Exception("Token exchange failed: " + response.body());
                }

                JsonNode json = mapper.readTree(response.body());
                return new TokenResponse(
                    json.path("access_token").asText(),
                    json.path("refresh_token").asText(null),
                    json.path("token_type").asText("Bearer"),
                    json.path("expires_in").asInt(3600),
                    json.path("scope").asText(null)
                );
            } catch (IOException | InterruptedException e) {
                throw new OAuth2Exception("Token exchange failed", e);
            }
        }

        /**
         * Fetches user info using the access token.
         */
        public UserInfo getUserInfo(String accessToken) throws OAuth2Exception {
            if (userInfoUrl == null) {
                throw new OAuth2Exception("UserInfo URL not configured for this provider");
            }

            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userInfoUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new OAuth2Exception("Failed to fetch user info: " + response.body());
                }

                JsonNode json = mapper.readTree(response.body());
                return parseUserInfo(json);
            } catch (IOException | InterruptedException e) {
                throw new OAuth2Exception("Failed to fetch user info", e);
            }
        }

        private UserInfo parseUserInfo(JsonNode json) {
            // Different providers use different field names
            String id = getField(json, "id", "sub");
            String email = getField(json, "email");
            String name = getField(json, "name", "login", "displayName");
            String picture = getField(json, "picture", "avatar_url", "avatar");

            Map<String, Object> raw = new HashMap<>();
            json.fields().forEachRemaining(e -> raw.put(e.getKey(), nodeToObject(e.getValue())));

            return new UserInfo(id, email, name, picture, this.name, raw);
        }

        private String getField(JsonNode json, String... fieldNames) {
            for (String name : fieldNames) {
                JsonNode node = json.path(name);
                if (!node.isMissingNode() && !node.isNull()) {
                    return node.asText();
                }
            }
            return null;
        }

        private Object nodeToObject(JsonNode node) {
            if (node.isNull()) return null;
            if (node.isBoolean()) return node.asBoolean();
            if (node.isInt()) return node.asInt();
            if (node.isLong()) return node.asLong();
            if (node.isDouble()) return node.asDouble();
            if (node.isTextual()) return node.asText();
            return node.toString();
        }

        public String getName() { return name; }
    }

    // ==================== Response Types ====================

    /** What a finished {@link Provider#callback(Request)} hands back. */
    public record Login(TokenResponse tokens, UserInfo user) {}

    /**
     * OAuth2 token response.
     */
    public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn,
        String scope
    ) {}

    /**
     * User info from OAuth2 provider.
     */
    public record UserInfo(
        String id,
        String email,
        String name,
        String picture,
        String provider,
        Map<String, Object> raw
    ) {
        /**
         * Gets a raw field from the provider response.
         */
        @SuppressWarnings("unchecked")
        public <T> T get(String field) {
            return (T) raw.get(field);
        }
    }

    /**
     * OAuth2 exception.
     */
    public static class OAuth2Exception extends Exception {
        public OAuth2Exception(String message) {
            super(message);
        }
        public OAuth2Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ==================== Helpers ====================

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** The PKCE S256 challenge for a verifier: base64url(sha256(verifier)). */
    static String s256(String verifier) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
            a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
