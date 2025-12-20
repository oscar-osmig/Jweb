package com.osmig.Jweb.framework.server;

import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Cookie utilities for HTTP cookie management.
 *
 * <p>Provides a fluent builder for creating cookies with proper security settings.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>
 * // Simple cookie
 * Cookie.of("theme", "dark").addTo(response);
 *
 * // Session cookie (expires when browser closes)
 * Cookie.of("sessionId", "abc123")
 *     .httpOnly()
 *     .secure()
 *     .addTo(response);
 *
 * // Persistent cookie
 * Cookie.of("rememberMe", "true")
 *     .maxAge(Duration.ofDays(30))
 *     .httpOnly()
 *     .addTo(response);
 * </pre>
 *
 * <h2>Security Settings</h2>
 * <pre>
 * Cookie.of("auth", token)
 *     .httpOnly()        // Not accessible via JavaScript
 *     .secure()          // Only sent over HTTPS
 *     .sameSite("Strict") // CSRF protection
 *     .path("/")
 *     .addTo(response);
 * </pre>
 *
 * <h2>Deleting Cookies</h2>
 * <pre>
 * Cookie.delete("theme").addTo(response);
 * </pre>
 *
 * <h2>Reading Cookies</h2>
 * <pre>
 * // Via Request
 * String theme = request.cookie("theme");
 * </pre>
 */
public final class Cookie {

    private static final DateTimeFormatter COOKIE_DATE_FORMAT = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            .withZone(ZoneId.of("GMT"));

    private final String name;
    private final String value;
    private String path = "/";
    private String domain;
    private Duration maxAge;
    private Instant expires;
    private boolean secure = false;
    private boolean httpOnly = false;
    private String sameSite;

    private Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a new cookie builder.
     *
     * @param name  the cookie name
     * @param value the cookie value
     * @return a new cookie builder
     */
    public static Cookie of(String name, String value) {
        return new Cookie(name, value);
    }

    /**
     * Creates a cookie that will delete an existing cookie.
     *
     * @param name the cookie name to delete
     * @return a delete cookie builder
     */
    public static Cookie delete(String name) {
        return new Cookie(name, "").maxAge(Duration.ZERO);
    }

    /**
     * Sets the path for the cookie.
     *
     * @param path the cookie path (default: "/")
     * @return this builder
     */
    public Cookie path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the domain for the cookie.
     *
     * @param domain the cookie domain
     * @return this builder
     */
    public Cookie domain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Sets the max age for the cookie.
     *
     * @param duration the max age duration
     * @return this builder
     */
    public Cookie maxAge(Duration duration) {
        this.maxAge = duration;
        return this;
    }

    /**
     * Sets the max age in seconds.
     *
     * @param seconds the max age in seconds
     * @return this builder
     */
    public Cookie maxAge(long seconds) {
        this.maxAge = Duration.ofSeconds(seconds);
        return this;
    }

    /**
     * Sets the expiration date for the cookie.
     *
     * @param expires the expiration instant
     * @return this builder
     */
    public Cookie expires(Instant expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Marks the cookie as secure (HTTPS only).
     *
     * @return this builder
     */
    public Cookie secure() {
        this.secure = true;
        return this;
    }

    /**
     * Sets the secure flag.
     *
     * @param secure true to make cookie HTTPS only
     * @return this builder
     */
    public Cookie secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Marks the cookie as HTTP only (not accessible via JavaScript).
     *
     * @return this builder
     */
    public Cookie httpOnly() {
        this.httpOnly = true;
        return this;
    }

    /**
     * Sets the HTTP only flag.
     *
     * @param httpOnly true to make cookie HTTP only
     * @return this builder
     */
    public Cookie httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * Sets the SameSite attribute.
     *
     * @param sameSite "Strict", "Lax", or "None"
     * @return this builder
     */
    public Cookie sameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    /**
     * Sets SameSite to Strict.
     *
     * @return this builder
     */
    public Cookie sameSiteStrict() {
        this.sameSite = "Strict";
        return this;
    }

    /**
     * Sets SameSite to Lax.
     *
     * @return this builder
     */
    public Cookie sameSiteLax() {
        this.sameSite = "Lax";
        return this;
    }

    /**
     * Sets SameSite to None (requires Secure flag).
     *
     * @return this builder
     */
    public Cookie sameSiteNone() {
        this.sameSite = "None";
        this.secure = true; // SameSite=None requires Secure
        return this;
    }

    /**
     * Builds the Set-Cookie header value.
     *
     * @return the cookie header value
     */
    public String toHeaderValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(encode(name)).append("=").append(encode(value));

        if (path != null) {
            sb.append("; Path=").append(path);
        }

        if (domain != null) {
            sb.append("; Domain=").append(domain);
        }

        if (maxAge != null) {
            sb.append("; Max-Age=").append(maxAge.getSeconds());
        }

        if (expires != null) {
            sb.append("; Expires=").append(COOKIE_DATE_FORMAT.format(expires));
        }

        if (secure) {
            sb.append("; Secure");
        }

        if (httpOnly) {
            sb.append("; HttpOnly");
        }

        if (sameSite != null) {
            sb.append("; SameSite=").append(sameSite);
        }

        return sb.toString();
    }

    /**
     * Adds this cookie to an HTTP response.
     *
     * @param response the servlet response
     */
    public void addTo(HttpServletResponse response) {
        response.addHeader("Set-Cookie", toHeaderValue());
    }

    /**
     * Gets the cookie name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the cookie value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    // ========== Encoding ==========

    private static String encode(String value) {
        if (value == null) {
            return "";
        }
        // Simple URL encoding for cookie values
        StringBuilder result = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (isValidCookieChar(c)) {
                result.append(c);
            } else {
                result.append('%');
                result.append(String.format("%02X", (int) c));
            }
        }
        return result.toString();
    }

    private static boolean isValidCookieChar(char c) {
        // RFC 6265 allowed characters
        return (c >= 0x21 && c <= 0x7E) && c != '"' && c != ',' && c != ';' && c != '\\';
    }

    @Override
    public String toString() {
        return toHeaderValue();
    }
}
