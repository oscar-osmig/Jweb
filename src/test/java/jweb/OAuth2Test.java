package jweb;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The session-backed OAuth2 flow: the state and the PKCE verifier are kept in
 * the visitor's session, the callback checks them, exchanges the code (with
 * the verifier) and fetches the profile — against a local stand-in provider.
 */
class OAuth2Test {

    private HttpServer provider;
    private final AtomicReference<Map<String, String>> tokenRequest = new AtomicReference<>();
    private OAuth2.Provider github;

    @BeforeEach
    void startProvider() throws IOException {
        provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        provider.createContext("/token", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            tokenRequest.set(form(body));
            String json = "{\"access_token\":\"at-123\",\"token_type\":\"bearer\",\"scope\":\"user:email\"}";
            reply(exchange, 200, json);
        });
        provider.createContext("/user", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (!"Bearer at-123".equals(auth)) {
                reply(exchange, 401, "{\"message\":\"bad token\"}");
                return;
            }
            reply(exchange, 200, "{\"id\":42,\"login\":\"ada\",\"email\":\"ada@example.com\",\"avatar_url\":\"https://x/a.png\"}");
        });
        provider.start();
        String base = "http://127.0.0.1:" + provider.getAddress().getPort();
        github = OAuth2.custom("github")
            .clientId("cid")
            .clientSecret("csecret")
            .redirectUri("http://app/auth/github/callback")
            .authorizationUrl(base + "/authorize")
            .tokenUrl(base + "/token")
            .userInfoUrl(base + "/user")
            .scopes("user:email")
            .build();
    }

    @AfterEach
    void stopProvider() {
        provider.stop(0);
    }

    @Test
    void authorizationUrlCarriesStateAndAPkceChallengeKeptInTheSession() throws Exception {
        Request start = MockRequest.get("/auth/github").build();
        String url = github.authorizationUrl(start);
        Map<String, String> q = query(url);

        assertEquals("code", q.get("response_type"));
        assertEquals("cid", q.get("client_id"));
        assertEquals("http://app/auth/github/callback", q.get("redirect_uri"));
        assertEquals("user:email", q.get("scope"));
        assertEquals("S256", q.get("code_challenge_method"));
        assertNotNull(q.get("state"));
        assertNotNull(q.get("code_challenge"));

        String stored = Session.of(start).get("jweb.oauth2.github", String.class);
        assertNotNull(stored, "the state and verifier live in the session");
        String[] parts = stored.split(" ");
        assertEquals(q.get("state"), parts[0]);
        assertEquals(q.get("code_challenge"), OAuth2.s256(parts[1]), "the challenge is S256 of the stored verifier");

        // the callback: same session, the provider's state and a code
        Request back = MockRequest.get("/auth/github/callback")
            .session(start.session())
            .queryParam("state", q.get("state"))
            .queryParam("code", "the-code")
            .build();
        OAuth2.Login login = github.callback(back);

        assertEquals("at-123", login.tokens().accessToken());
        assertEquals("42", login.user().id());
        assertEquals("ada@example.com", login.user().email());
        assertEquals("ada", login.user().name());
        assertEquals("https://x/a.png", login.user().picture());
        assertEquals("github", login.user().provider());

        Map<String, String> exchanged = tokenRequest.get();
        assertEquals("the-code", exchanged.get("code"));
        assertEquals("authorization_code", exchanged.get("grant_type"));
        assertEquals(parts[1], exchanged.get("code_verifier"), "the verifier went with the code");
        assertEquals("csecret", exchanged.get("client_secret"));

        assertNull(Session.of(back).get("jweb.oauth2.github", String.class), "single use");
        OAuth2.OAuth2Exception again = assertThrows(OAuth2.OAuth2Exception.class, () -> github.callback(back));
        assertTrue(again.getMessage().contains("No github login in progress"), again.getMessage());
    }

    @Test
    void aWrongStateOrAMissingCodeOrAProviderErrorIsRefused() throws Exception {
        Request start = MockRequest.get("/auth/github").build();
        String url = github.authorizationUrl(start);
        String state = query(url).get("state");

        Request wrongState = MockRequest.get("/cb").session(start.session())
            .queryParam("state", "forged").queryParam("code", "c").build();
        OAuth2.OAuth2Exception mismatch = assertThrows(OAuth2.OAuth2Exception.class, () -> github.callback(wrongState));
        assertTrue(mismatch.getMessage().contains("does not match"), mismatch.getMessage());

        github.authorizationUrl(start);   // start again; the previous attempt was consumed
        String state2 = Session.of(start).get("jweb.oauth2.github", String.class).split(" ")[0];
        Request noCode = MockRequest.get("/cb").session(start.session()).queryParam("state", state2).build();
        OAuth2.OAuth2Exception missing = assertThrows(OAuth2.OAuth2Exception.class, () -> github.callback(noCode));
        assertTrue(missing.getMessage().contains("no authorization code"), missing.getMessage());

        Request denied = MockRequest.get("/cb").session(start.session())
            .queryParam("error", "access_denied").queryParam("error_description", "user said no").build();
        OAuth2.OAuth2Exception refused = assertThrows(OAuth2.OAuth2Exception.class, () -> github.callback(denied));
        assertTrue(refused.getMessage().contains("access_denied"), refused.getMessage());
        assertTrue(refused.getMessage().contains("user said no"), refused.getMessage());

        Request fresh = MockRequest.get("/cb").queryParam("state", state).queryParam("code", "c").build();
        OAuth2.OAuth2Exception elsewhere = assertThrows(OAuth2.OAuth2Exception.class, () -> github.callback(fresh));
        assertTrue(elsewhere.getMessage().contains("No github login in progress"), elsewhere.getMessage());
    }

    @Test
    void pkceCanBeTurnedOffPerProvider() throws Exception {
        OAuth2.Provider plain = OAuth2.custom("legacy")
            .clientId("cid").clientSecret("s").redirectUri("http://app/cb")
            .authorizationUrl("http://127.0.0.1:" + provider.getAddress().getPort() + "/authorize")
            .tokenUrl("http://127.0.0.1:" + provider.getAddress().getPort() + "/token")
            .pkce(false)
            .build();
        Request start = MockRequest.get("/auth").build();
        String url = plain.authorizationUrl(start);
        assertNull(query(url).get("code_challenge"));
        String state = query(url).get("state");
        Request back = MockRequest.get("/cb").session(start.session())
            .queryParam("state", state).queryParam("code", "c").build();
        OAuth2.Login login = plain.callback(back);
        assertEquals("at-123", login.tokens().accessToken());
        assertNull(login.user(), "no userinfo URL configured");
        assertNull(tokenRequest.get().get("code_verifier"));
    }

    // ==================== helpers ====================

    private static Map<String, String> query(String url) {
        return form(url.substring(url.indexOf('?') + 1));
    }

    private static Map<String, String> form(String encoded) {
        Map<String, String> out = new HashMap<>();
        for (String pair : encoded.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            out.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return out;
    }

    private static void reply(com.sun.net.httpserver.HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
