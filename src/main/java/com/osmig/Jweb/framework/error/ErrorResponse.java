package com.osmig.Jweb.framework.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response format for API responses.
 *
 * <p>Provides a consistent JSON structure for error responses:</p>
 * <pre>
 * {
 *   "error": true,
 *   "status": 400,
 *   "message": "Validation failed",
 *   "code": "VALIDATION_ERROR",
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "path": "/api/users",
 *   "errors": {
 *     "email": ["Invalid email format"],
 *     "password": ["Must be at least 8 characters"]
 *   }
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean error = true;
    private final int status;
    private final String message;
    private final String code;
    private final String timestamp;
    private final String path;
    private final Map<String, List<String>> errors;

    private ErrorResponse(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.code = builder.code;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now().toString();
        this.path = builder.path;
        this.errors = builder.errors;
    }

    public boolean isError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    // ========== Builder ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status = 500;
        private String message;
        private String code;
        private String timestamp;
        private String path;
        private Map<String, List<String>> errors;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder errors(Map<String, List<String>> errors) {
            this.errors = errors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }

    // ========== Factory Methods ==========

    /**
     * Creates an error response from a JWebException.
     */
    public static ErrorResponse from(JWebException e, String path) {
        Builder builder = builder()
                .status(e.getStatusCode())
                .message(e.getMessage())
                .path(path);

        if (e.getErrorCode() != null) {
            builder.code(e.getErrorCode());
        }

        if (e instanceof ValidationException ve) {
            builder.errors(ve.getValidationResult().getAllErrors());
        }

        return builder.build();
    }

    /**
     * Creates a generic error response.
     */
    public static ErrorResponse of(int status, String message, String path) {
        return builder()
                .status(status)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Creates a validation error response.
     */
    public static ErrorResponse validation(Map<String, List<String>> errors, String path) {
        return builder()
                .status(422)
                .message("Validation failed")
                .code("VALIDATION_ERROR")
                .path(path)
                .errors(errors)
                .build();
    }
}
