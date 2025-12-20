package com.osmig.Jweb.framework.validation;

import java.util.*;

/**
 * Fluent form validator for validating key-value form data.
 *
 * <p>Usage:</p>
 * <pre>
 * Map&lt;String, String&gt; formData = request.formData();
 *
 * ValidationResult result = FormValidator.create()
 *     .field("email", formData.get("email"))
 *         .required()
 *         .email()
 *     .field("password", formData.get("password"))
 *         .required()
 *         .minLength(8)
 *         .maxLength(100)
 *     .field("age", formData.get("age"))
 *         .optional()
 *         .numeric()
 *     .validate();
 *
 * if (result.hasErrors()) {
 *     // Show errors to user
 * }
 * </pre>
 */
public class FormValidator {

    private final List<FieldValidator> fieldValidators = new ArrayList<>();
    private FieldValidator currentField = null;

    private FormValidator() {
    }

    /**
     * Creates a new form validator.
     *
     * @return a new FormValidator instance
     */
    public static FormValidator create() {
        return new FormValidator();
    }

    /**
     * Starts validation for a new field.
     *
     * @param name  the field name
     * @param value the field value
     * @return this validator for chaining
     */
    public FormValidator field(String name, String value) {
        if (currentField != null) {
            fieldValidators.add(currentField);
        }
        currentField = new FieldValidator(name, value);
        return this;
    }

    /**
     * Marks the current field as required.
     *
     * @return this validator for chaining
     */
    public FormValidator required() {
        if (currentField != null) {
            currentField.required = true;
        }
        return this;
    }

    /**
     * Marks the current field as optional.
     * Optional fields only validate if they have a value.
     *
     * @return this validator for chaining
     */
    public FormValidator optional() {
        if (currentField != null) {
            currentField.required = false;
        }
        return this;
    }

    /**
     * Adds a minimum length constraint.
     *
     * @param min minimum length
     * @return this validator for chaining
     */
    public FormValidator minLength(int min) {
        if (currentField != null) {
            currentField.validators.add(Validators.minLength(min));
        }
        return this;
    }

    /**
     * Adds a maximum length constraint.
     *
     * @param max maximum length
     * @return this validator for chaining
     */
    public FormValidator maxLength(int max) {
        if (currentField != null) {
            currentField.validators.add(Validators.maxLength(max));
        }
        return this;
    }

    /**
     * Adds a length range constraint.
     *
     * @param min minimum length
     * @param max maximum length
     * @return this validator for chaining
     */
    public FormValidator lengthBetween(int min, int max) {
        if (currentField != null) {
            currentField.validators.add(Validators.lengthBetween(min, max));
        }
        return this;
    }

    /**
     * Adds an email format constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator email() {
        if (currentField != null) {
            currentField.validators.add(Validators.email());
        }
        return this;
    }

    /**
     * Adds a numeric-only constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator numeric() {
        if (currentField != null) {
            currentField.validators.add(Validators.numeric());
        }
        return this;
    }

    /**
     * Adds an alphanumeric constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator alphanumeric() {
        if (currentField != null) {
            currentField.validators.add(Validators.alphanumeric());
        }
        return this;
    }

    /**
     * Adds an alpha-only constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator alpha() {
        if (currentField != null) {
            currentField.validators.add(Validators.alpha());
        }
        return this;
    }

    /**
     * Adds a URL format constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator url() {
        if (currentField != null) {
            currentField.validators.add(Validators.url());
        }
        return this;
    }

    /**
     * Adds a phone number format constraint.
     *
     * @return this validator for chaining
     */
    public FormValidator phone() {
        if (currentField != null) {
            currentField.validators.add(Validators.phone());
        }
        return this;
    }

    /**
     * Adds a regex pattern constraint.
     *
     * @param regex   the regex pattern
     * @param message the error message if pattern doesn't match
     * @return this validator for chaining
     */
    public FormValidator pattern(String regex, String message) {
        if (currentField != null) {
            currentField.validators.add(Validators.pattern(regex, message));
        }
        return this;
    }

    /**
     * Adds a constraint that the field must match another field's value.
     *
     * @param otherFieldName  the name of the other field
     * @param otherFieldValue the value of the other field
     * @return this validator for chaining
     */
    public FormValidator matches(String otherFieldName, String otherFieldValue) {
        if (currentField != null) {
            currentField.validators.add(Validator.of(
                    s -> Objects.equals(s, otherFieldValue),
                    field -> field + " must match " + otherFieldName
            ));
        }
        return this;
    }

    /**
     * Adds a custom validator.
     *
     * @param validator the custom validator
     * @return this validator for chaining
     */
    public FormValidator custom(Validator<String> validator) {
        if (currentField != null) {
            currentField.validators.add(validator);
        }
        return this;
    }

    /**
     * Adds a custom validation check with a message.
     *
     * @param predicate the validation predicate
     * @param message   the error message if predicate fails
     * @return this validator for chaining
     */
    public FormValidator check(java.util.function.Predicate<String> predicate, String message) {
        if (currentField != null) {
            currentField.validators.add(Validator.of(predicate, message));
        }
        return this;
    }

    /**
     * Runs all validations and returns the result.
     *
     * @return the validation result
     */
    public ValidationResult validate() {
        if (currentField != null) {
            fieldValidators.add(currentField);
            currentField = null;
        }

        ValidationResult result = new ValidationResult();
        for (FieldValidator fv : fieldValidators) {
            result.merge(fv.validate());
        }
        return result;
    }

    /**
     * Internal class to hold field validation configuration.
     */
    private static class FieldValidator {
        final String name;
        final String value;
        boolean required = false;
        final List<Validator<String>> validators = new ArrayList<>();

        FieldValidator(String name, String value) {
            this.name = name;
            this.value = value;
        }

        ValidationResult validate() {
            ValidationResult result = new ValidationResult();

            // Check required first
            if (required) {
                if (value == null || value.isBlank()) {
                    result.addError(name, name + " is required");
                    return result; // Don't run other validators if required fails
                }
            } else {
                // Optional field with no value - skip other validators
                if (value == null || value.isBlank()) {
                    return result;
                }
            }

            // Run all validators
            for (Validator<String> validator : validators) {
                result.merge(validator.validate(value, name));
            }

            return result;
        }
    }
}
