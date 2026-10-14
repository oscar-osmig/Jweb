package com.osmig.Jweb.framework.security;

import jweb.Principal;
import jweb.JWebException;
import jweb.Middleware;
import jweb.Request;
import jakarta.servlet.http.HttpSession;

import java.util.function.Function;

/**
 * Authentication utilities for JWeb.
 *
 * <p>Provides methods for managing user authentication state via HTTP sessions.</p>
 *
 * <h2>Login/Logout</h2>
 * <pre>
 * // Login - store principal in session
 * Principal user = Principal.of("123", "john@example.com", "user");
 * Auth.login(request, user);
 *
 * // Logout - remove principal from session
 * Auth.logout(request);
 * </pre>
 *
 * <h2>Checking Authentication</h2>
 * <pre>
 * // Check if user is logged in
 * if (Auth.isAuthenticated(request)) {
 *     Principal user = Auth.getPrincipal(request);
 *     // ...
 * }
 *
 * // Get principal or throw exception
 * Principal user = Auth.requirePrincipal(request);
 * </pre>
 *
 * <h2>Authorization Middleware</h2>
 * <pre>
 * // Require authentication
 * app.use("/dashboard", Auth.requireAuth());
 *
 * // Require specific roles
 * app.use("/admin", Auth.requireRole("admin"));
 * app.use("/api", Auth.requireAnyRole("user", "admin"));
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.Auth} — shorter import, same behavior. Existing code keeps working.
 */
@Deprecated
public class Auth {

    private static final String SESSION_PRINCIPAL_KEY = "jweb_principal";
    private static final String SESSION_VERSION_KEY = "jweb_principal_version";
    private static final String REQUEST_PRINCIPAL_KEY = "jweb_auth_principal";

    /**
     * A per-user session version: every login records the user's current
     * version in the session, and a session whose recorded version is behind
     * the current one is no longer logged in. Bumping it is "log out
     * everywhere" — {@code jweb.Auth.logoutEverywhere(userId)}.
     */
    public interface Versions {
        /** The user's current version (0 until first bumped). */
        long current(String userId);

        /** Advances the version; every existing session and remember-me cookie of the user is now stale. */
        long bump(String userId);
    }

    private static volatile Versions versions;

    protected Auth() {
        // Static utility class
    }

    /** Installs the versions store; {@code null} turns the check off. */
    public static void versions(Versions store) {
        versions = store;
    }

    public static Versions versions() {
        return versions;
    }

    // ========== Principal Management ==========

    /**
     * Logs in a user by storing their principal in the session.
     *
     * @param request   the HTTP request
     * @param principal the authenticated principal
     */
    public static void login(Request request, Principal principal) {
        HttpSession session = request.raw().getSession(true);
        // Rotate the session ID at the privilege boundary so a session ID
        // planted before login (session fixation) never becomes authenticated.
        if (!session.isNew()) {
            request.raw().changeSessionId();
        }
        session.setAttribute(SESSION_PRINCIPAL_KEY, principal);
        Versions v = versions;
        if (v != null && principal.getId() != null) {
            session.setAttribute(SESSION_VERSION_KEY, v.current(principal.getId()));
        }
        // Also store in request for immediate access
        request.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
        // Regenerate CSRF token on login
        Csrf.refreshToken(request);
    }

    /**
     * Logs out the current user by invalidating the session.
     *
     * @param request the HTTP request
     */
    public static void logout(Request request) {
        HttpSession session = request.raw().getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Gets the currently authenticated principal from the session.
     *
     * @param request the HTTP request
     * @return the principal, or null if not authenticated
     */
    public static Principal getPrincipal(Request request) {
        // Check request attribute first (for same-request access after login)
        Principal principal = (Principal) request.raw().getAttribute(REQUEST_PRINCIPAL_KEY);
        if (principal != null) {
            return principal;
        }

        // Check session
        HttpSession session = request.raw().getSession(false);
        if (session != null) {
            principal = (Principal) session.getAttribute(SESSION_PRINCIPAL_KEY);
            if (principal != null && isStale(session, principal)) {
                // Logged out everywhere since this session logged in
                session.removeAttribute(SESSION_PRINCIPAL_KEY);
                session.removeAttribute(SESSION_VERSION_KEY);
                return null;
            }
            if (principal != null) {
                // Cache in request for subsequent calls
                request.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            }
        }
        return principal;
    }

    private static boolean isStale(HttpSession session, Principal principal) {
        Versions v = versions;
        if (v == null || principal.getId() == null) return false;
        Object recorded = session.getAttribute(SESSION_VERSION_KEY);
        long at = recorded instanceof Number n ? n.longValue() : 0L;
        return at < v.current(principal.getId());
    }

    /**
     * Gets the currently authenticated principal, throwing if not authenticated.
     *
     * @param request the HTTP request
     * @return the principal
     * @throws JWebException if not authenticated (401 Unauthorized)
     */
    public static Principal requirePrincipal(Request request) {
        Principal principal = getPrincipal(request);
        if (principal == null) {
            throw JWebException.unauthorized("Authentication required");
        }
        return principal;
    }

    /**
     * Checks if the current request has an authenticated user.
     *
     * @param request the HTTP request
     * @return true if authenticated
     */
    public static boolean isAuthenticated(Request request) {
        return getPrincipal(request) != null;
    }

    /**
     * Checks if the current user has a specific role.
     *
     * @param request the HTTP request
     * @param role    the role to check
     * @return true if the user has the role
     */
    public static boolean hasRole(Request request, String role) {
        Principal principal = getPrincipal(request);
        return principal != null && principal.hasRole(role);
    }

    /**
     * Checks if the current user has any of the specified roles.
     *
     * @param request the HTTP request
     * @param roles   the roles to check
     * @return true if the user has any of the roles
     */
    public static boolean hasAnyRole(Request request, String... roles) {
        Principal principal = getPrincipal(request);
        return principal != null && principal.hasAnyRole(roles);
    }

    // ========== Guard Factories ==========

    /**
     * A guard that sends anonymous visitors to the login page and lets the
     * login page itself through — for {@code app.guard(prefix, ...)}:
     *
     * <pre>
     * app.guard("/admin/**", Auth.requireLogin("/admin/login"));
     * </pre>
     *
     * <p>{@code requireRole(...)}, {@code requireAnyRole(...)} and the other
     * middleware factories work as guards too: {@code app.guard("/admin/**",
     * Auth.requireRole("admin"))}.</p>
     *
     * @param loginUrl where to redirect when there is no principal
     * @return the guard
     */
    public static jweb.Guard requireLogin(String loginUrl) {
        String loginPath = loginUrl.contains("?") ? loginUrl.substring(0, loginUrl.indexOf('?')) : loginUrl;
        return request -> {
            if (isAuthenticated(request)) return null;
            if (request.path().equals(loginPath)) return null;
            return jweb.Response.redirect(loginUrl);
        };
    }

    // ========== Middleware Factories ==========

    /**
     * Creates middleware that requires authentication.
     * Returns 401 Unauthorized if no user is logged in.
     *
     * @return authentication middleware
     */
    public static Middleware requireAuth() {
        return (req, chain) -> {
            if (!isAuthenticated(req)) {
                throw JWebException.unauthorized("Authentication required");
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware that requires authentication with a custom redirect.
     * Redirects to the specified URL if not authenticated.
     *
     * @param loginUrl the URL to redirect to
     * @return authentication middleware
     */
    public static Middleware requireAuth(String loginUrl) {
        return (req, chain) -> {
            if (!isAuthenticated(req)) {
                return org.springframework.http.ResponseEntity
                        .status(302)
                        .header("Location", loginUrl + "?redirect=" + req.path())
                        .build();
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware that requires a specific role.
     *
     * @param role the required role
     * @return authorization middleware
     */
    public static Middleware requireRole(String role) {
        return (req, chain) -> {
            Principal principal = getPrincipal(req);
            if (principal == null) {
                throw JWebException.unauthorized("Authentication required");
            }
            if (!principal.hasRole(role)) {
                throw JWebException.forbidden("Insufficient permissions");
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware that requires any of the specified roles.
     *
     * @param roles the roles (user must have at least one)
     * @return authorization middleware
     */
    public static Middleware requireAnyRole(String... roles) {
        return (req, chain) -> {
            Principal principal = getPrincipal(req);
            if (principal == null) {
                throw JWebException.unauthorized("Authentication required");
            }
            if (!principal.hasAnyRole(roles)) {
                throw JWebException.forbidden("Insufficient permissions");
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware that requires all of the specified roles.
     *
     * @param roles the roles (user must have all)
     * @return authorization middleware
     */
    public static Middleware requireAllRoles(String... roles) {
        return (req, chain) -> {
            Principal principal = getPrincipal(req);
            if (principal == null) {
                throw JWebException.unauthorized("Authentication required");
            }
            if (!principal.hasAllRoles(roles)) {
                throw JWebException.forbidden("Insufficient permissions");
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware with a custom authentication check.
     * Rejects the request with 401 when the checker returns null.
     *
     * <p>To attach a principal without rejecting unauthenticated requests,
     * use {@link #attachPrincipal(Function)}.</p>
     *
     * @param authChecker function that takes the request and returns a Principal (or null to reject)
     * @return authentication middleware
     */
    public static Middleware customAuth(Function<Request, Principal> authChecker) {
        return (req, chain) -> {
            Principal principal = authChecker.apply(req);
            if (principal == null) {
                throw JWebException.unauthorized("Authentication required");
            }
            req.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            return chain.next();
        };
    }

    /**
     * Creates middleware that extracts a Bearer token from Authorization header
     * and validates it using the provided function. Rejects the request with
     * 401 when the header is missing or the validator returns null.
     *
     * @param tokenValidator function that validates token and returns Principal (or null to reject)
     * @return authentication middleware
     */
    public static Middleware bearerAuth(Function<String, Principal> tokenValidator) {
        return (req, chain) -> {
            String authHeader = req.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw JWebException.unauthorized("Bearer token required");
            }
            Principal principal = tokenValidator.apply(authHeader.substring(7));
            if (principal == null) {
                throw JWebException.unauthorized("Invalid token");
            }
            req.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            return chain.next();
        };
    }

    /**
     * Creates middleware that attaches a principal when the checker resolves
     * one, but never rejects the request. Useful for optional authentication
     * (e.g. personalized-but-public pages).
     *
     * @param authChecker function that takes the request and returns a Principal (or null)
     * @return principal-attaching middleware
     */
    public static Middleware attachPrincipal(Function<Request, Principal> authChecker) {
        return (req, chain) -> {
            Principal principal = authChecker.apply(req);
            if (principal != null) {
                req.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            }
            return chain.next();
        };
    }
}
