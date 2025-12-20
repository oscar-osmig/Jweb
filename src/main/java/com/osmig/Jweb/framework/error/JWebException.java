package com.osmig.Jweb.framework.error;

import org.springframework.http.HttpStatus;

/**
 * Base exception for JWeb framework errors.
 *
 * <p>JWeb exceptions carry HTTP status codes and can be rendered
 * as error pages or JSON responses.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * throw new JWebException(HttpStatus.BAD_REQUEST, "Invalid input");
 * throw JWebException.notFound("User not found");
 * throw JWebException.badRequest("Missing required field: email");
 * </pre>
 */
public class JWebException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public JWebException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.errorCode = null;
    }

    public JWebException(HttpStatus status, String message, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public JWebException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = null;
    }

    public JWebException(HttpStatus status, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Gets the HTTP status code for this error.
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Gets the error code if set.
     *
     * @return the error code, or null
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the HTTP status code as an integer.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return status.value();
    }

    // ========== Factory Methods ==========

    /**
     * Creates a 400 Bad Request exception.
     */
    public static JWebException badRequest(String message) {
        return new JWebException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Creates a 400 Bad Request exception with error code.
     */
    public static JWebException badRequest(String message, String errorCode) {
        return new JWebException(HttpStatus.BAD_REQUEST, message, errorCode);
    }

    /**
     * Creates a 401 Unauthorized exception.
     */
    public static JWebException unauthorized(String message) {
        return new JWebException(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Creates a 403 Forbidden exception.
     */
    public static JWebException forbidden(String message) {
        return new JWebException(HttpStatus.FORBIDDEN, message);
    }

    /**
     * Creates a 404 Not Found exception.
     */
    public static JWebException notFound(String message) {
        return new JWebException(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Creates a 404 Not Found exception for a specific resource.
     */
    public static JWebException notFound(String resource, Object id) {
        return new JWebException(HttpStatus.NOT_FOUND, resource + " not found: " + id);
    }

    /**
     * Creates a 409 Conflict exception.
     */
    public static JWebException conflict(String message) {
        return new JWebException(HttpStatus.CONFLICT, message);
    }

    /**
     * Creates a 422 Unprocessable Content exception.
     */
    public static JWebException unprocessable(String message) {
        return new JWebException(HttpStatus.valueOf(422), message);
    }

    /**
     * Creates a 429 Too Many Requests exception.
     */
    public static JWebException tooManyRequests(String message) {
        return new JWebException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    /**
     * Creates a 500 Internal Server Error exception.
     */
    public static JWebException serverError(String message) {
        return new JWebException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Creates a 500 Internal Server Error exception with cause.
     */
    public static JWebException serverError(String message, Throwable cause) {
        return new JWebException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     * Creates a 503 Service Unavailable exception.
     */
    public static JWebException serviceUnavailable(String message) {
        return new JWebException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
