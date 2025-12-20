package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.error.JWebException;
import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.server.Request;
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
 */
public final class Auth {

    private static final String SESSION_PRINCIPAL_KEY = "jweb_principal";
    private static final String REQUEST_PRINCIPAL_KEY = "jweb_auth_principal";

    private Auth() {
        // Static utility class
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
        session.setAttribute(SESSION_PRINCIPAL_KEY, principal);
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
            if (principal != null) {
                // Cache in request for subsequent calls
                request.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            }
        }
        return principal;
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
     *
     * @param authChecker function that takes the request and returns a Principal (or null)
     * @return authentication middleware
     */
    public static Middleware customAuth(Function<Request, Principal> authChecker) {
        return (req, chain) -> {
            Principal principal = authChecker.apply(req);
            if (principal != null) {
                // Store principal in request for access in handlers
                req.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
            }
            return chain.next();
        };
    }

    /**
     * Creates middleware that extracts a Bearer token from Authorization header
     * and validates it using the provided function.
     *
     * @param tokenValidator function that validates token and returns Principal
     * @return authentication middleware
     */
    public static Middleware bearerAuth(Function<String, Principal> tokenValidator) {
        return (req, chain) -> {
            String authHeader = req.header("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Principal principal = tokenValidator.apply(token);
                if (principal != null) {
                    req.raw().setAttribute(REQUEST_PRINCIPAL_KEY, principal);
                }
            }
            return chain.next();
        };
    }
}
