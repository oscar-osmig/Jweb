package com.osmig.Jweb.framework.error;

import com.osmig.Jweb.framework.validation.ValidationResult;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when validation fails.
 *
 * <p>Carries the ValidationResult with all validation errors.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * ValidationResult result = FormValidator.create()
 *     .field("email", email).required().email()
 *     .validate();
 *
 * if (result.hasErrors()) {
 *     throw new ValidationException(result);
 * }
 * </pre>
 */
public class ValidationException extends JWebException {

    private final ValidationResult validationResult;

    public ValidationException(ValidationResult validationResult) {
        super(HttpStatus.valueOf(422), "Validation failed", "VALIDATION_ERROR");
        this.validationResult = validationResult;
    }

    public ValidationException(String message, ValidationResult validationResult) {
        super(HttpStatus.valueOf(422), message, "VALIDATION_ERROR");
        this.validationResult = validationResult;
    }

    /**
     * Gets the validation result with all errors.
     *
     * @return the validation result
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Creates a validation exception for a single field error.
     *
     * @param field   the field name
     * @param message the error message
     * @return a new ValidationException
     */
    public static ValidationException of(String field, String message) {
        return new ValidationException(ValidationResult.invalid(field, message));
    }
}
