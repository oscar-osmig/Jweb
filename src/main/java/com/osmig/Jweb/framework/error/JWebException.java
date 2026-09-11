package com.osmig.Jweb.framework.error;

import org.springframework.http.HttpStatus;

/**
 * @deprecated the real class is {@link jweb.JWebException}; this name is kept so
 *             existing source compiles. The framework throws {@code jweb.JWebException},
 *             so catch that one.
 */
@Deprecated
public class JWebException extends jweb.JWebException {
    public JWebException(HttpStatus status, String message) { super(status, message); }
    public JWebException(HttpStatus status, String message, String errorCode) { super(status, message, errorCode); }
    public JWebException(HttpStatus status, String message, Throwable cause) { super(status, message, cause); }
    public JWebException(HttpStatus status, String message, String errorCode, Throwable cause) { super(status, message, errorCode, cause); }
}
