package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.server.Request;
import jakarta.servlet.http.HttpSession;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * CSRF protection utilities for JWeb.
 *
 * <p>Provides methods for generating and validating CSRF tokens, as well as
 * creating hidden form fields for token submission.</p>
 *
 * <h2>Usage in Routes/Handlers:</h2>
 * <pre>
 * // GET request - render form with CSRF token
 * app.get("/contact", req -> {
 *     CsrfToken token = Csrf.getOrCreateToken(req);
 *     return form(
 *         Csrf.tokenField(token),
 *         input().type("text").name("message"),
 *         button("Submit")
 *     );
 * });
 *
 * // POST request - validate CSRF token
 * app.post("/contact", req -> {
 *     Csrf.validate(req);  // Throws CsrfException if invalid
 *     // Process form...
 * });
 * </pre>
 *
 * <h2>Usage with Middleware:</h2>
 * <pre>
 * // In middleware configuration
 * app.use(Csrf.middleware());
 * </pre>
 */
public final class Csrf {

    private static final String SESSION_ATTRIBUTE = "jweb_csrf_token";

    private Csrf() {
        // Static utility class
    }

    /**
     * Gets the CSRF token from the session, creating one if it doesn't exist.
     *
     * @param request the HTTP request
     * @return the CSRF token
     */
    public static CsrfToken getOrCreateToken(Request request) {
        HttpSession session = request.raw().getSession(true);
        CsrfToken token = (CsrfToken) session.getAttribute(SESSION_ATTRIBUTE);

        if (token == null || token.isExpired()) {
            token = CsrfToken.generate();
            session.setAttribute(SESSION_ATTRIBUTE, token);
        }

        return token;
    }

    /**
     * Gets the CSRF token from the session without creating one.
     *
     * @param request the HTTP request
     * @return the CSRF token, or null if none exists
     */
    public static CsrfToken getToken(Request request) {
        HttpSession session = request.raw().getSession(false);
        if (session == null) {
            return null;
        }
        return (CsrfToken) session.getAttribute(SESSION_ATTRIBUTE);
    }

    /**
     * Validates the CSRF token in the request.
     * Checks both the form parameter and the header.
     *
     * @param request the HTTP request
     * @throws CsrfException if validation fails
     */
    public static void validate(Request request) throws CsrfException {
        CsrfToken sessionToken = getToken(request);
        if (sessionToken == null) {
            throw CsrfException.missing();
        }

        if (sessionToken.isExpired()) {
            throw CsrfException.expired();
        }

        // Check form parameter first, then header
        String submittedToken = request.formParam(CsrfToken.TOKEN_PARAM_NAME);
        if (submittedToken == null) {
            submittedToken = request.header(CsrfToken.TOKEN_HEADER_NAME);
        }

        if (submittedToken == null) {
            throw CsrfException.missing();
        }

        if (!sessionToken.matches(submittedToken)) {
            throw CsrfException.invalid();
        }
    }

    /**
     * Checks if CSRF validation would pass without throwing an exception.
     *
     * @param request the HTTP request
     * @return true if CSRF token is valid
     */
    public static boolean isValid(Request request) {
        try {
            validate(request);
            return true;
        } catch (CsrfException e) {
            return false;
        }
    }

    /**
     * Creates a hidden input field containing the CSRF token.
     *
     * @param token the CSRF token
     * @return a hidden input element
     */
    public static Element tokenField(CsrfToken token) {
        return input()
                .type("hidden")
                .name(CsrfToken.TOKEN_PARAM_NAME)
                .value(token.getValue());
    }

    /**
     * Creates a hidden input field with the CSRF token from the request.
     *
     * @param request the HTTP request
     * @return a hidden input element
     */
    public static Element tokenField(Request request) {
        return tokenField(getOrCreateToken(request));
    }

    /**
     * Creates a meta tag containing the CSRF token for JavaScript access.
     * Useful for AJAX requests.
     *
     * <p>JavaScript can read the token like this:</p>
     * <pre>
     * const token = document.querySelector('meta[name="csrf-token"]').content;
     * </pre>
     *
     * @param token the CSRF token
     * @return a meta element
     */
    public static Element tokenMeta(CsrfToken token) {
        return meta()
                .name("csrf-token")
                .attr("content", token.getValue());
    }

    /**
     * Creates a meta tag with the CSRF token from the request.
     *
     * @param request the HTTP request
     * @return a meta element
     */
    public static Element tokenMeta(Request request) {
        return tokenMeta(getOrCreateToken(request));
    }

    /**
     * Refreshes the CSRF token in the session.
     * Use this after successful authentication to prevent session fixation.
     *
     * @param request the HTTP request
     * @return the new CSRF token
     */
    public static CsrfToken refreshToken(Request request) {
        CsrfToken token = CsrfToken.generate();
        request.raw().getSession(true).setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    /**
     * Checks if a request method requires CSRF protection.
     * GET, HEAD, OPTIONS, and TRACE are considered safe methods.
     *
     * @param method the HTTP method
     * @return true if CSRF validation is required
     */
    public static boolean requiresProtection(String method) {
        if (method == null) return true;
        return switch (method.toUpperCase()) {
            case "GET", "HEAD", "OPTIONS", "TRACE" -> false;
            default -> true;
        };
    }

    /**
     * Checks if the request method requires CSRF protection.
     *
     * @param request the HTTP request
     * @return true if CSRF validation is required
     */
    public static boolean requiresProtection(Request request) {
        return requiresProtection(request.method());
    }
}
