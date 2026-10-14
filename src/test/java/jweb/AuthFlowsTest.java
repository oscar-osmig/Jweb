package jweb;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The auth flows: signed single-use tokens, the remember-me cookie and its
 * middleware, log-out-everywhere through session versions (memory and SQL),
 * TOTP against the RFC 6238 vectors, recovery codes.
 */
class AuthFlowsTest {

    @BeforeEach
    void secret() {
        Auth.secret("a-test-secret-that-is-long-enough-for-hmac");
        Auth.versions(new Auth.MemoryVersions());
        Auth.rememberSecure(false);
    }

    @AfterEach
    void reset() {
        Auth.versions(new Auth.MemoryVersions());
    }

    // ==================== tokens ====================

    @Test
    void aTokenRoundTripsForItsPurposeAndFingerprintOnly() {
        String token = Auth.token("reset", "user-42", Duration.ofHours(1), "$2a$hash");
        assertEquals(Optional.of("user-42"), Auth.subject(token), "the subject reads without the fingerprint");
        assertEquals(Optional.empty(), Auth.subject("nope"));
        assertEquals(Optional.of("user-42"), Auth.verify("reset", token, "$2a$hash"));
        assertTrue(Auth.expiry(token).orElseThrow().isAfter(java.time.Instant.now().plus(Duration.ofMinutes(59))));

        assertEquals(Optional.empty(), Auth.verify("verify", token, "$2a$hash"), "another purpose");
        assertEquals(Optional.empty(), Auth.verify("reset", token, "$2a$other"), "the password changed");
        assertEquals(Optional.empty(), Auth.verify("reset", token), "no fingerprint given");
        assertEquals(Optional.empty(), Auth.verify("reset", token.substring(0, token.length() - 2) + "xx", "$2a$hash"), "tampered");
        assertEquals(Optional.empty(), Auth.verify("reset", "not.a.token", "$2a$hash"));
        assertEquals(Optional.empty(), Auth.verify("reset", null, "$2a$hash"));

        String expired = Auth.token("reset", "user-42", Duration.ofSeconds(-5));
        assertEquals(Optional.empty(), Auth.verify("reset", expired));

        String plain = Auth.token("verify", "ada@example.com", Duration.ofDays(1));
        assertEquals(Optional.of("ada@example.com"), Auth.verify("verify", plain));

        Auth.secret("a-different-secret-on-another-deploy");
        assertEquals(Optional.empty(), Auth.verify("verify", plain), "signed with another secret");
        assertThrows(IllegalArgumentException.class, () -> Auth.token("", "x", Duration.ofHours(1)));
    }

    @Test
    void aBlankSecretIsGeneratedAndFlagged() {
        Auth.secret("");
        assertTrue(Auth.secretIsGenerated());
        String token = Auth.token("t", "s", Duration.ofMinutes(1));
        assertEquals(Optional.of("s"), Auth.verify("t", token));
        Auth.secret("configured-secret-value-long-enough");
        assertFalse(Auth.secretIsGenerated());
    }

    // ==================== remember me ====================

    @Test
    void theRememberCookieLogsAReturningVisitorInAndDiesOnLogoutEverywhere() throws Exception {
        Principal ada = Principal.of("42", "ada@example.com", "user");
        jweb.http.Cookie cookie = Auth.rememberCookie(ada, Duration.ofDays(30));
        assertEquals(Auth.REMEMBER_COOKIE, cookie.getName());
        String header = cookie.toHeaderValue();
        assertTrue(header.contains("HttpOnly"), header);
        assertTrue(header.contains("SameSite=Lax"), header);
        assertTrue(header.contains("Path=/"), header);
        assertFalse(header.contains("Secure"), header);
        Auth.rememberSecure(true);
        assertTrue(Auth.rememberCookie(ada, Duration.ofDays(1)).toHeaderValue().contains("Secure"));

        Request returning = MockRequest.get("/dashboard").cookie(Auth.REMEMBER_COOKIE, cookie.getValue()).build();
        assertEquals(Optional.of("42"), Auth.remembered(returning));
        assertNull(Auth.getPrincipal(returning), "not logged in yet");

        AtomicReference<String> loaded = new AtomicReference<>();
        Middleware remember = Auth.remember(id -> {
            loaded.set(id);
            return "42".equals(id) ? ada : null;
        });
        Object result = remember.handle(returning, () -> "next");
        assertEquals("next", result);
        assertEquals("42", loaded.get());
        Principal now = Auth.getPrincipal(returning);
        assertNotNull(now, "logged in from the cookie");
        assertEquals("ada@example.com", now.getName());

        // the cookie is bound to the user's version: log out everywhere kills it
        Auth.logoutEverywhere("42");
        Request later = MockRequest.get("/").cookie(Auth.REMEMBER_COOKIE, cookie.getValue()).build();
        assertEquals(Optional.empty(), Auth.remembered(later));
        loaded.set(null);
        remember.handle(later, () -> "next");
        assertNull(loaded.get(), "the loader was not even asked");
        assertNull(Auth.getPrincipal(later));

        // a new cookie after the bump works; a garbage cookie is ignored; the forget cookie clears
        jweb.http.Cookie fresh = Auth.rememberCookie(ada, Duration.ofDays(30));
        assertNotEquals(cookie.getValue(), fresh.getValue());
        assertEquals(Optional.of("42"), Auth.remembered(MockRequest.get("/").cookie(Auth.REMEMBER_COOKIE, fresh.getValue()).build()));
        assertEquals(Optional.empty(), Auth.remembered(MockRequest.get("/").cookie(Auth.REMEMBER_COOKIE, "garbage").build()));
        assertEquals(Optional.empty(), Auth.remembered(MockRequest.get("/").build()));
        assertTrue(Auth.forgetCookie().toHeaderValue().contains("Max-Age=0"), Auth.forgetCookie().toHeaderValue());

        // a user that no longer exists: cookie valid, loader says null, no login
        Request gone = MockRequest.get("/").cookie(Auth.REMEMBER_COOKIE, fresh.getValue()).build();
        Auth.remember(id -> null).handle(gone, () -> "next");
        assertNull(Auth.getPrincipal(gone));
    }

    // ==================== log out everywhere ====================

    @Test
    void logoutEverywhereEndsTheSessionsThatLoggedInBefore() throws Exception {
        Principal ada = Principal.of("42", "ada@example.com");
        Request first = MockRequest.post("/login").build();
        Auth.login(first, ada);
        Request sameSession = MockRequest.get("/").session(first.session()).build();
        assertNotNull(Auth.getPrincipal(sameSession));

        assertEquals(1L, Auth.logoutEverywhere("42"));
        Request afterBump = MockRequest.get("/").session(first.session()).build();
        assertNull(Auth.getPrincipal(afterBump), "the session's version is behind");
        assertFalse(Auth.isAuthenticated(afterBump));

        // logging in again records the new version
        Auth.login(afterBump, ada);
        assertNotNull(Auth.getPrincipal(MockRequest.get("/").session(first.session()).build()));

        // the request form: bumps and ends this session
        Request mine = MockRequest.post("/logout-everywhere").session(first.session()).build();
        Auth.logoutEverywhere(mine);
        assertEquals(2L, Auth.version("42"));
        assertNull(Auth.getPrincipal(MockRequest.get("/").build()));
    }

    @Test
    void sqlVersionsAreSharedThroughTheTable() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:authversions;DB_CLOSE_DELAY=-1");
        Sql.Database db = Sql.of(ds);
        try {
            Auth.SqlVersions a = new Auth.SqlVersions(db);
            Auth.SqlVersions b = new Auth.SqlVersions(db);   // "another instance"
            assertEquals(0L, a.current("u1"));
            assertEquals(1L, a.bump("u1"));
            assertEquals(2L, a.bump("u1"));
            assertEquals(2L, b.current("u1"), "read through the table");
            assertEquals(0L, b.current("u2"));
            assertEquals(2L, db.query("select version from " + Auth.SqlVersions.TABLE + " where user_id = ?", "u1").scalar(Long.class));

            Auth.versions(a);
            Principal u1 = Principal.of("u1");
            Request login = MockRequest.post("/login").build();
            Auth.login(login, u1);
            assertNotNull(Auth.getPrincipal(MockRequest.get("/").session(login.session()).build()));
            a.bump("u1");
            assertNull(Auth.getPrincipal(MockRequest.get("/").session(login.session()).build()));
        } finally {
            db.execute("drop table " + Auth.SqlVersions.TABLE);
        }
    }

    // ==================== TOTP ====================

    @Test
    void totpMatchesTheRfc6238Vectors() {
        // RFC 6238 appendix B: secret "12345678901234567890", SHA-1; the last six digits of the 8-digit codes
        String secret = Auth.Totp.base32("12345678901234567890".getBytes());
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", secret);
        assertEquals("287082", Auth.Totp.code(secret, 59));
        assertEquals("081804", Auth.Totp.code(secret, 1111111109));
        assertEquals("050471", Auth.Totp.code(secret, 1111111111));
        assertEquals("005924", Auth.Totp.code(secret, 1234567890));
        assertEquals("279037", Auth.Totp.code(secret, 2000000000));

        String now = Auth.Totp.code(secret);
        assertTrue(Auth.Totp.verify(secret, now));
        assertTrue(Auth.Totp.verify(secret, now.substring(0, 3) + " " + now.substring(3)), "spaces are fine");
        assertFalse(Auth.Totp.verify(secret, "000000") && Auth.Totp.verify(secret, "111111") && Auth.Totp.verify(secret, "222222"));
        assertFalse(Auth.Totp.verify(secret, "12345"));
        assertFalse(Auth.Totp.verify(secret, null));
        assertFalse(Auth.Totp.verify("not base32 !!", now));

        String fresh = Auth.Totp.secret();
        assertEquals(32, fresh.length(), "20 bytes in base32");
        assertEquals(20, Auth.Totp.unbase32(fresh).length);
        String uri = Auth.Totp.uri("My App", "ada@example.com", fresh);
        assertTrue(uri.startsWith("otpauth://totp/My%20App:ada%40example.com?secret=" + fresh + "&issuer=My%20App"), uri);
        assertTrue(uri.endsWith("&algorithm=SHA1&digits=6&period=30"), uri);
    }

    @Test
    void recoveryCodesAreDistinctAndReadable() {
        List<String> codes = Auth.recoveryCodes(10);
        assertEquals(10, codes.size());
        assertEquals(10, new HashSet<>(codes).size());
        for (String code : codes) {
            assertTrue(code.matches("[abcdefghjkmnpqrstuvwxyz23456789]{4}-[abcdefghjkmnpqrstuvwxyz23456789]{4}"), code);
        }
        String hash = Password.hash(codes.get(0));
        assertTrue(Password.verify(codes.get(0), hash));
        assertFalse(Password.verify(codes.get(1), hash));
    }
}
