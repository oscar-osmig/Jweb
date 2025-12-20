package com.osmig.Jweb.framework.security;

/**
 * Exception thrown when CSRF validation fails.
 *
 * <p>This exception indicates that a request failed CSRF validation,
 * typically because:</p>
 * <ul>
 *   <li>The CSRF token was missing from the request</li>
 *   <li>The CSRF token was invalid or did not match</li>
 *   <li>The CSRF token has expired</li>
 * </ul>
 */
public class CsrfException extends SecurityException {

    public CsrfException(String message) {
        super(message);
    }

    public CsrfException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception for a missing CSRF token.
     *
     * @return a new CsrfException
     */
    public static CsrfException missing() {
        return new CsrfException("CSRF token is missing from request");
    }

    /**
     * Creates an exception for an invalid CSRF token.
     *
     * @return a new CsrfException
     */
    public static CsrfException invalid() {
        return new CsrfException("CSRF token is invalid");
    }

    /**
     * Creates an exception for an expired CSRF token.
     *
     * @return a new CsrfException
     */
    public static CsrfException expired() {
        return new CsrfException("CSRF token has expired");
    }
}
