package com.osmig.Jweb.framework.error;

import jweb.ValidationResult;

/**
 * @deprecated the real class is {@link jweb.ValidationException}; this name is kept so
 *             existing source compiles. The framework throws {@code jweb.ValidationException},
 *             so catch that one.
 */
@Deprecated
public class ValidationException extends jweb.ValidationException {
    public ValidationException(ValidationResult validationResult) { super(validationResult); }
    public ValidationException(String message, ValidationResult validationResult) { super(message, validationResult); }
}
