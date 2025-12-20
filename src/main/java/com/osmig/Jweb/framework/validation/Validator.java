package com.osmig.Jweb.framework.validation;

import java.util.function.Predicate;

/**
 * Functional interface for single-value validators.
 *
 * <p>Validators check a value and return either a success or failure result.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * Validator&lt;String&gt; notEmpty = Validator.of(
 *     s -> s != null && !s.isBlank(),
 *     "Value is required"
 * );
 *
 * ValidationResult result = notEmpty.validate("test");
 * </pre>
 *
 * @param <T> the type of value to validate
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates a value.
     *
     * @param value the value to validate
     * @param field the field name (for error messages)
     * @return validation result
     */
    ValidationResult validate(T value, String field);

    /**
     * Validates a value with a default field name.
     *
     * @param value the value to validate
     * @return validation result
     */
    default ValidationResult validate(T value) {
        return validate(value, "value");
    }

    /**
     * Combines this validator with another (both must pass).
     *
     * @param other the other validator
     * @return a combined validator
     */
    default Validator<T> and(Validator<T> other) {
        return (value, field) -> {
            ValidationResult result = this.validate(value, field);
            return result.merge(other.validate(value, field));
        };
    }

    /**
     * Creates a validator that passes if either this or the other passes.
     *
     * @param other the other validator
     * @return a combined validator
     */
    default Validator<T> or(Validator<T> other) {
        return (value, field) -> {
            ValidationResult result = this.validate(value, field);
            if (result.isValid()) {
                return result;
            }
            return other.validate(value, field);
        };
    }

    /**
     * Creates a validator from a predicate and error message.
     *
     * @param predicate the validation predicate
     * @param message   the error message if validation fails
     * @param <T>       the type to validate
     * @return a new validator
     */
    static <T> Validator<T> of(Predicate<T> predicate, String message) {
        return (value, field) -> {
            if (predicate.test(value)) {
                return ValidationResult.valid();
            }
            return ValidationResult.invalid(field, message);
        };
    }

    /**
     * Creates a validator from a predicate with a dynamic message.
     *
     * @param predicate      the validation predicate
     * @param messageFactory function that creates the error message
     * @param <T>            the type to validate
     * @return a new validator
     */
    static <T> Validator<T> of(Predicate<T> predicate, java.util.function.Function<String, String> messageFactory) {
        return (value, field) -> {
            if (predicate.test(value)) {
                return ValidationResult.valid();
            }
            return ValidationResult.invalid(field, messageFactory.apply(field));
        };
    }

    /**
     * Creates a validator that always passes.
     *
     * @param <T> the type to validate
     * @return a validator that always returns valid
     */
    static <T> Validator<T> alwaysValid() {
        return (value, field) -> ValidationResult.valid();
    }

    /**
     * Creates a validator that always fails.
     *
     * @param message the error message
     * @param <T>     the type to validate
     * @return a validator that always returns invalid
     */
    static <T> Validator<T> alwaysInvalid(String message) {
        return (value, field) -> ValidationResult.invalid(field, message);
    }
}
