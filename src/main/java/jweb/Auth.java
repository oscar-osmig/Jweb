package jweb;

import com.osmig.Jweb.framework.util.Log;
import jweb.http.Cookie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Authentication: who is logged in ({@code login}, {@code logout},
 * {@code getPrincipal}, the {@code require*} guards and middleware, inherited),
 * and the flows an app needs around it — all stateless or on a store the
 * instances share, so they work on a cluster:
 *
 * <ul>
 *   <li><b>Signed tokens</b> for password reset and email verification:
 *       {@link #token(String, String, Duration, String)} /
 *       {@link #verify(String, String, String)}. HMAC-SHA256 with
 *       {@code jweb.auth.secret}, a purpose, a subject, an expiry, and a
 *       fingerprint that makes the token single-use (the password hash: once it
 *       changes, the reset link is dead).</li>
 *   <li><b>Remember me</b>: {@link #rememberCookie(Principal, Duration)} on
 *       login, {@link #remember(Function)} as middleware to log a returning
 *       visitor in from the cookie.</li>
 *   <li><b>Log out everywhere</b>: {@link #logoutEverywhere(String)} bumps the
 *       user's session version; every session and remember-me cookie that
 *       logged in before it is no longer valid. {@link Versions} is in memory,
 *       or the {@code jweb_auth_versions} table when {@link Sql} is connected.</li>
 *   <li><b>Two-factor</b>: {@link Totp} — a secret, the {@code otpauth://} URI
 *       for the authenticator app, and {@code verify(secret, code)}; and
 *       {@link #recoveryCodes(int)} to store hashed.</li>
 * </ul>
 *
 * <pre>
 * // password reset: the token is bound to the current hash, so it works once
 * String token = Auth.token("reset", user.id(), Duration.ofHours(1), user.passwordHash());
 * Mail.to(user.email()).subject("Reset your password").text(base + "/reset?token=" + token).later();
 * // …on the reset page
 * Optional&lt;String&gt; userId = Auth.verify("reset", req.query("token"), user.passwordHash());
 *
 * // remember me
 * Auth.login(req, principal);
 * return Response.redirect("/").cookie(Auth.rememberCookie(principal, Duration.ofDays(30)));
 * app.use(Auth.remember(id -&gt; users.find(id).map(User::principal).orElse(null)));
 *
 * // two-factor
 * String secret = Auth.Totp.secret();                      // store it on the user
 * String uri = Auth.Totp.uri("MyApp", user.email(), secret);   // show as a QR code
 * boolean ok = Auth.Totp.verify(secret, req.formParam("code"));
 * </pre>
 *
 * <p>The secret: set {@code jweb.auth.secret} (32+ random characters) in
 * every environment. Without it one is generated at startup and tokens and
 * cookies stop working on restart, and across instances.</p>
 */
@SuppressWarnings("deprecation")
public class Auth extends com.osmig.Jweb.framework.security.Auth {

    /** The remember-me cookie's name. */
    public static final String REMEMBER_COOKIE = "jweb_remember";

    private static final String REMEMBER_PURPOSE = "remember";
    private static volatile byte[] secret;
    private static volatile boolean generatedSecret;
    private static volatile boolean rememberSecure;

    protected Auth() {}

    // ==================== the secret ====================

    /** The HMAC secret for tokens and cookies; blank generates one for this run, with a warning. */
    public static void secret(String value) {
        if (value == null || value.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            secret = random;
            generatedSecret = true;
            Log.warn("jweb.auth.secret is not set: tokens and remember-me cookies will not survive a restart "
                + "or work across instances. Set it to 32+ random characters.");
            return;
        }
        if (value.length() < 16) {
            Log.warn("jweb.auth.secret is short ({} characters); use 32 or more", value.length());
        }
        secret = value.getBytes(StandardCharsets.UTF_8);
        generatedSecret = false;
    }

    /** Whether the secret was generated for this run rather than configured. */
    public static boolean secretIsGenerated() {
        return generatedSecret;
    }

    private static byte[] key() {
        byte[] k = secret;
        if (k == null) {
            synchronized (Auth.class) {
                if (secret == null) secret(null);
                k = secret;
            }
        }
        return k;
    }

    /** Whether remember-me cookies carry the {@code Secure} flag (set behind HTTPS). */
    public static void rememberSecure(boolean secure) {
        rememberSecure = secure;
    }

    // ==================== signed tokens ====================

    /** A signed token for the purpose and subject, valid for the duration. */
    public static String token(String purpose, String subject, Duration ttl) {
        return token(purpose, subject, ttl, "");
    }

    /**
     * A signed token bound to a fingerprint — the user's password hash for a
     * reset link, so the link dies the moment the password changes; the email
     * for a verification link, so it verifies that address and no other.
     */
    public static String token(String purpose, String subject, Duration ttl, String fingerprint) {
        if (purpose == null || purpose.isBlank()) throw new IllegalArgumentException("purpose is blank");
        if (subject == null) throw new IllegalArgumentException("subject is null");
        long expires = Instant.now().plus(ttl).getEpochSecond();
        String body = b64(purpose) + "." + b64(subject) + "." + expires;
        return body + "." + sign(body, fingerprint == null ? "" : fingerprint);
    }

    /** The subject, when the token is for this purpose, unexpired and untampered. */
    public static Optional<String> verify(String purpose, String token) {
        return verify(purpose, token, "");
    }

    /** As {@link #verify(String, String)}, and the fingerprint must be the one the token was made with. */
    public static Optional<String> verify(String purpose, String token, String fingerprint) {
        if (token == null || purpose == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 4) return Optional.empty();
        String body = parts[0] + "." + parts[1] + "." + parts[2];
        String expected = sign(body, fingerprint == null ? "" : fingerprint);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), parts[3].getBytes(StandardCharsets.US_ASCII))) {
            return Optional.empty();
        }
        try {
            if (!purpose.equals(unb64(parts[0]))) return Optional.empty();
            long expires = Long.parseLong(parts[2]);
            if (Instant.now().getEpochSecond() > expires) return Optional.empty();
            return Optional.of(unb64(parts[1]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * The token's subject, read <b>without verifying</b> — to look the user
     * up and then {@link #verify(String, String, String)} against the
     * fingerprint only that user knows (the password hash). Never trust it
     * before the verify.
     */
    public static Optional<String> subject(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 4) return Optional.empty();
        try {
            return Optional.of(unb64(parts[1]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** When the token expires, or empty when it is not a token at all. */
    public static Optional<Instant> expiry(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 4) return Optional.empty();
        try {
            return Optional.of(Instant.ofEpochSecond(Long.parseLong(parts[2])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String sign(String body, String fingerprint) {
        return hmac(body, fingerprint);
    }

    /**
     * HMAC-SHA256 of {@code body} and {@code context} under the app secret,
     * base64url without padding — the one signature the framework's own
     * signed artifacts share ({@link Storage} URLs sign with it too).
     */
    static String hmac(String body, String context) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key(), "HmacSHA256"));
            mac.update(body.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            mac.update(context.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal());
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String unb64(String s) {
        return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
    }

    // ==================== remember me ====================

    /**
     * The cookie that logs this user back in on a later visit: a signed token
     * bound to the user's session version, HttpOnly, SameSite=Lax, on the
     * whole site. Attach it to the login response.
     */
    public static Cookie rememberCookie(Principal principal, Duration ttl) {
        String id = principal.getId();
        if (id == null) throw new IllegalArgumentException("the principal has no id");
        String token = token(REMEMBER_PURPOSE, id, ttl, String.valueOf(version(id)));
        Cookie cookie = Cookie.of(REMEMBER_COOKIE, token).path("/").httpOnly().sameSiteLax().maxAge(ttl);
        if (rememberSecure) cookie.secure();
        return cookie;
    }

    /** The cookie that clears the remembered login. Attach it to the logout response. */
    public static Cookie forgetCookie() {
        return Cookie.delete(REMEMBER_COOKIE).path("/");
    }

    /** The user id the request's remember-me cookie stands for, when it is valid for the user's current version. */
    public static Optional<String> remembered(Request request) {
        String token = request.cookie(REMEMBER_COOKIE);
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 4) return Optional.empty();
        String id;
        try {
            id = unb64(parts[1]);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return verify(REMEMBER_PURPOSE, token, String.valueOf(version(id)));
    }

    /**
     * Middleware: a visitor with no session login but a valid remember-me
     * cookie is logged in with the principal the loader returns for the id
     * (null: the user is gone, the cookie is ignored).
     */
    public static Middleware remember(Function<String, Principal> userById) {
        return (request, chain) -> {
            if (!isAuthenticated(request)) {
                Optional<String> id = remembered(request);
                if (id.isPresent()) {
                    Principal principal = userById.apply(id.get());
                    if (principal != null) login(request, principal);
                }
            }
            return chain.next();
        };
    }

    // ==================== log out everywhere ====================

    private static volatile Versions versionStore = new MemoryVersions();

    static {
        com.osmig.Jweb.framework.security.Auth.versions(versionStore);
    }

    /** Installs the versions store the sessions are checked against. */
    public static void versions(Versions store) {
        versionStore = store;
        com.osmig.Jweb.framework.security.Auth.versions(store);
    }

    /** The user's current session version. */
    public static long version(String userId) {
        return versionStore.current(userId);
    }

    /**
     * Ends every session and remember-me cookie of the user, on every
     * instance: the next request each makes finds its version behind. The new
     * version.
     */
    public static long logoutEverywhere(String userId) {
        return versionStore.bump(userId);
    }

    /** As {@link #logoutEverywhere(String)} for the request's user, and this session with it. */
    public static void logoutEverywhere(Request request) {
        Principal principal = getPrincipal(request);
        if (principal != null && principal.getId() != null) logoutEverywhere(principal.getId());
        logout(request);
    }

    /** Session versions in this JVM. */
    public static final class MemoryVersions implements Versions {

        private final Map<String, Long> versions = new ConcurrentHashMap<>();

        @Override
        public long current(String userId) {
            return versions.getOrDefault(userId, 0L);
        }

        @Override
        public long bump(String userId) {
            return versions.merge(userId, 1L, Long::sum);
        }
    }

    /**
     * Session versions in the {@code jweb_auth_versions} table, shared by every
     * instance; reads are cached for ten seconds, which is how long a bump
     * takes to reach a session on another instance.
     */
    public static final class SqlVersions implements Versions {

        public static final String TABLE = "jweb_auth_versions";
        private static final long CACHE_MS = 10_000;

        private final Sql.Database db;
        private final Map<String, long[]> cache = new ConcurrentHashMap<>();   // [version, readAtMillis]

        public SqlVersions(Sql.Database db) {
            this.db = db;
            try {
                db.query("select 1 from " + TABLE + " where 1 = 0").rows();
            } catch (Sql.SqlException missing) {
                db.execute("create table " + TABLE + " (user_id varchar(100) primary key, version bigint not null)");
            }
        }

        @Override
        public long current(String userId) {
            long now = System.currentTimeMillis();
            long[] cached = cache.get(userId);
            if (cached != null && now - cached[1] < CACHE_MS) return cached[0];
            Long stored = db.query("select version from " + TABLE + " where user_id = ?", userId).scalar(Long.class);
            long version = stored == null ? 0L : stored;
            cache.put(userId, new long[]{version, now});
            return version;
        }

        @Override
        public long bump(String userId) {
            long next = db.transaction(() -> {
                Long stored = db.query("select version from " + TABLE + " where user_id = ?", userId).scalar(Long.class);
                if (stored == null) {
                    db.update("insert into " + TABLE + " (user_id, version) values (?, ?)", userId, 1L);
                    return 1L;
                }
                db.update("update " + TABLE + " set version = ? where user_id = ?", stored + 1, userId);
                return stored + 1;
            });
            cache.put(userId, new long[]{next, System.currentTimeMillis()});
            return next;
        }
    }

    // ==================== two-factor ====================

    /**
     * Time-based one-time passwords (RFC 6238, SHA-1, six digits, thirty
     * seconds) — what Google Authenticator, 1Password and the rest generate.
     */
    public static final class Totp {

        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        private static final int PERIOD = 30;
        private static final int DIGITS = 6;

        private Totp() {}

        /** A new secret, base32 — store it on the user, show it once. */
        public static String secret() {
            byte[] bytes = new byte[20];
            new SecureRandom().nextBytes(bytes);
            return base32(bytes);
        }

        /** The {@code otpauth://} URI an authenticator app scans (render it as a QR code). */
        public static String uri(String issuer, String account, String secret) {
            String label = encode(issuer) + ":" + encode(account);
            return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
        }

        /** The code for this instant. */
        public static String code(String secret) {
            return code(secret, Instant.now().getEpochSecond());
        }

        /** The code at an instant (epoch seconds) — for tests. */
        public static String code(String secret, long epochSeconds) {
            return hotp(unbase32(secret), epochSeconds / PERIOD);
        }

        /** Whether the code is this period's or an adjacent one's (clock skew). */
        public static boolean verify(String secret, String code) {
            return verify(secret, code, 1);
        }

        /** Accepts codes up to {@code window} periods either side of now. */
        public static boolean verify(String secret, String code, int window) {
            if (secret == null || code == null) return false;
            String given = code.replace(" ", "").trim();
            if (given.length() != DIGITS) return false;
            byte[] key;
            try {
                key = unbase32(secret);
            } catch (IllegalArgumentException e) {
                return false;
            }
            long counter = Instant.now().getEpochSecond() / PERIOD;
            boolean ok = false;
            for (long c = counter - window; c <= counter + window; c++) {
                // every candidate is compared, so timing does not say which matched
                ok |= MessageDigest.isEqual(hotp(key, c).getBytes(StandardCharsets.US_ASCII), given.getBytes(StandardCharsets.US_ASCII));
            }
            return ok;
        }

        static String hotp(byte[] key, long counter) {
            try {
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(new SecretKeySpec(key, "HmacSHA1"));
                byte[] msg = new byte[8];
                for (int i = 7; i >= 0; i--) {
                    msg[i] = (byte) (counter & 0xff);
                    counter >>>= 8;
                }
                byte[] hash = mac.doFinal(msg);
                int offset = hash[hash.length - 1] & 0x0f;
                int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
                int otp = binary % 1_000_000;
                return String.format("%06d", otp);
            } catch (java.security.GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }

        static String base32(byte[] bytes) {
            StringBuilder out = new StringBuilder((bytes.length * 8 + 4) / 5);
            int buffer = 0, bits = 0;
            for (byte b : bytes) {
                buffer = (buffer << 8) | (b & 0xff);
                bits += 8;
                while (bits >= 5) {
                    out.append(ALPHABET.charAt((buffer >> (bits - 5)) & 31));
                    bits -= 5;
                }
            }
            if (bits > 0) out.append(ALPHABET.charAt((buffer << (5 - bits)) & 31));
            return out.toString();
        }

        static byte[] unbase32(String s) {
            String clean = s.replace(" ", "").replace("=", "").toUpperCase(java.util.Locale.ROOT);
            byte[] out = new byte[clean.length() * 5 / 8];
            int buffer = 0, bits = 0, index = 0;
            for (char c : clean.toCharArray()) {
                int value = ALPHABET.indexOf(c);
                if (value < 0) throw new IllegalArgumentException("not base32: " + c);
                buffer = (buffer << 5) | value;
                bits += 5;
                if (bits >= 8) {
                    out[index++] = (byte) ((buffer >> (bits - 8)) & 0xff);
                    bits -= 8;
                }
            }
            return out;
        }

        private static String encode(String s) {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
        }
    }

    // ==================== recovery codes ====================

    /**
     * One-time codes for when the authenticator is gone — {@code xxxx-xxxx}
     * from an alphabet without look-alikes. Store {@code Password.hash(code)}
     * for each; on use, find the one that {@code Password.verify}s and delete it.
     */
    public static List<String> recoveryCodes(int count) {
        String alphabet = "abcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder(9);
            for (int j = 0; j < 8; j++) {
                if (j == 4) sb.append('-');
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            codes.add(sb.toString());
        }
        return codes;
    }
}
