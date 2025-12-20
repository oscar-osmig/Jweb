package com.osmig.Jweb.framework.validation;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Built-in validators for common validation scenarios.
 *
 * <p>Usage:</p>
 * <pre>
 * // Single validators
 * Validators.required().validate(value, "name");
 * Validators.email().validate(email, "email");
 * Validators.minLength(3).validate(username, "username");
 *
 * // Chained validators
 * Validator&lt;String&gt; usernameValidator = Validators.required()
 *     .and(Validators.minLength(3))
 *     .and(Validators.maxLength(20))
 *     .and(Validators.alphanumeric());
 *
 * ValidationResult result = usernameValidator.validate(username, "username");
 * </pre>
 */
public final class Validators {

    // Common regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w.-]+)(:\\d+)?(/.*)?$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    private Validators() {
        // Static utility class
    }

    // ========== String Validators ==========

    /**
     * Validates that a string is not null or blank.
     *
     * @return a required string validator
     */
    public static Validator<String> required() {
        return Validator.of(
                s -> s != null && !s.isBlank(),
                field -> field + " is required"
        );
    }

    /**
     * Validates that a string is not null (but can be empty).
     *
     * @return a not-null validator
     */
    public static Validator<String> notNull() {
        return Validator.of(
                s -> s != null,
                field -> field + " cannot be null"
        );
    }

    /**
     * Validates that a string has at least the specified length.
     *
     * @param min minimum length
     * @return a minimum length validator
     */
    public static Validator<String> minLength(int min) {
        return Validator.of(
                s -> s != null && s.length() >= min,
                field -> field + " must be at least " + min + " characters"
        );
    }

    /**
     * Validates that a string has at most the specified length.
     *
     * @param max maximum length
     * @return a maximum length validator
     */
    public static Validator<String> maxLength(int max) {
        return Validator.of(
                s -> s == null || s.length() <= max,
                field -> field + " must be at most " + max + " characters"
        );
    }

    /**
     * Validates that a string has exactly the specified length.
     *
     * @param length exact length required
     * @return an exact length validator
     */
    public static Validator<String> exactLength(int length) {
        return Validator.of(
                s -> s != null && s.length() == length,
                field -> field + " must be exactly " + length + " characters"
        );
    }

    /**
     * Validates that a string has a length within the specified range.
     *
     * @param min minimum length
     * @param max maximum length
     * @return a length range validator
     */
    public static Validator<String> lengthBetween(int min, int max) {
        return Validator.of(
                s -> s != null && s.length() >= min && s.length() <= max,
                field -> field + " must be between " + min + " and " + max + " characters"
        );
    }

    /**
     * Validates that a string matches the specified pattern.
     *
     * @param pattern the regex pattern
     * @param message the error message
     * @return a pattern validator
     */
    public static Validator<String> pattern(Pattern pattern, String message) {
        return Validator.of(
                s -> s != null && pattern.matcher(s).matches(),
                message
        );
    }

    /**
     * Validates that a string matches the specified regex.
     *
     * @param regex   the regex string
     * @param message the error message
     * @return a pattern validator
     */
    public static Validator<String> pattern(String regex, String message) {
        return pattern(Pattern.compile(regex), message);
    }

    /**
     * Validates that a string is a valid email address.
     *
     * @return an email validator
     */
    public static Validator<String> email() {
        return Validator.of(
                s -> s != null && EMAIL_PATTERN.matcher(s).matches(),
                field -> field + " must be a valid email address"
        );
    }

    /**
     * Validates that a string contains only alphanumeric characters.
     *
     * @return an alphanumeric validator
     */
    public static Validator<String> alphanumeric() {
        return Validator.of(
                s -> s != null && ALPHANUMERIC_PATTERN.matcher(s).matches(),
                field -> field + " must contain only letters and numbers"
        );
    }

    /**
     * Validates that a string contains only letters.
     *
     * @return an alpha validator
     */
    public static Validator<String> alpha() {
        return Validator.of(
                s -> s != null && ALPHA_PATTERN.matcher(s).matches(),
                field -> field + " must contain only letters"
        );
    }

    /**
     * Validates that a string contains only digits.
     *
     * @return a numeric validator
     */
    public static Validator<String> numeric() {
        return Validator.of(
                s -> s != null && NUMERIC_PATTERN.matcher(s).matches(),
                field -> field + " must contain only numbers"
        );
    }

    /**
     * Validates that a string is a valid URL.
     *
     * @return a URL validator
     */
    public static Validator<String> url() {
        return Validator.of(
                s -> s != null && URL_PATTERN.matcher(s).matches(),
                field -> field + " must be a valid URL"
        );
    }

    /**
     * Validates that a string is a valid phone number.
     *
     * @return a phone number validator
     */
    public static Validator<String> phone() {
        return Validator.of(
                s -> s != null && PHONE_PATTERN.matcher(s.replaceAll("[\\s()-]", "")).matches(),
                field -> field + " must be a valid phone number"
        );
    }

    // ========== Number Validators ==========

    /**
     * Validates that a number is not null.
     *
     * @param <T> the number type
     * @return a required number validator
     */
    public static <T extends Number> Validator<T> requiredNumber() {
        return Validator.of(
                n -> n != null,
                field -> field + " is required"
        );
    }

    /**
     * Validates that a number is at least the specified minimum.
     *
     * @param min minimum value
     * @return a minimum value validator
     */
    public static Validator<Number> min(double min) {
        return Validator.of(
                n -> n != null && n.doubleValue() >= min,
                field -> field + " must be at least " + min
        );
    }

    /**
     * Validates that a number is at most the specified maximum.
     *
     * @param max maximum value
     * @return a maximum value validator
     */
    public static Validator<Number> max(double max) {
        return Validator.of(
                n -> n != null && n.doubleValue() <= max,
                field -> field + " must be at most " + max
        );
    }

    /**
     * Validates that a number is within the specified range (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return a range validator
     */
    public static Validator<Number> range(double min, double max) {
        return Validator.of(
                n -> n != null && n.doubleValue() >= min && n.doubleValue() <= max,
                field -> field + " must be between " + min + " and " + max
        );
    }

    /**
     * Validates that a number is positive (greater than zero).
     *
     * @return a positive number validator
     */
    public static Validator<Number> positive() {
        return Validator.of(
                n -> n != null && n.doubleValue() > 0,
                field -> field + " must be positive"
        );
    }

    /**
     * Validates that a number is negative (less than zero).
     *
     * @return a negative number validator
     */
    public static Validator<Number> negative() {
        return Validator.of(
                n -> n != null && n.doubleValue() < 0,
                field -> field + " must be negative"
        );
    }

    /**
     * Validates that a number is not negative (zero or positive).
     *
     * @return a non-negative number validator
     */
    public static Validator<Number> nonNegative() {
        return Validator.of(
                n -> n != null && n.doubleValue() >= 0,
                field -> field + " must not be negative"
        );
    }

    // ========== Collection Validators ==========

    /**
     * Validates that a collection is not null or empty.
     *
     * @param <T> the collection type
     * @return a not-empty collection validator
     */
    public static <T extends Collection<?>> Validator<T> notEmpty() {
        return Validator.of(
                c -> c != null && !c.isEmpty(),
                field -> field + " must not be empty"
        );
    }

    /**
     * Validates that a collection has at least the specified size.
     *
     * @param min minimum size
     * @param <T> the collection type
     * @return a minimum size validator
     */
    public static <T extends Collection<?>> Validator<T> minSize(int min) {
        return Validator.of(
                c -> c != null && c.size() >= min,
                field -> field + " must have at least " + min + " items"
        );
    }

    /**
     * Validates that a collection has at most the specified size.
     *
     * @param max maximum size
     * @param <T> the collection type
     * @return a maximum size validator
     */
    public static <T extends Collection<?>> Validator<T> maxSize(int max) {
        return Validator.of(
                c -> c == null || c.size() <= max,
                field -> field + " must have at most " + max + " items"
        );
    }

    // ========== Object Validators ==========

    /**
     * Validates that an object is not null.
     *
     * @param <T> the object type
     * @return a not-null validator
     */
    public static <T> Validator<T> requiredObject() {
        return Validator.of(
                o -> o != null,
                field -> field + " is required"
        );
    }

    /**
     * Validates that an object equals the expected value.
     *
     * @param expected the expected value
     * @param <T>      the object type
     * @return an equals validator
     */
    public static <T> Validator<T> equalTo(T expected) {
        return Validator.of(
                o -> expected.equals(o),
                field -> field + " must equal " + expected
        );
    }

    /**
     * Validates that an object is one of the allowed values.
     *
     * @param allowedValues the allowed values
     * @param <T>           the object type
     * @return an in-list validator
     */
    @SafeVarargs
    public static <T> Validator<T> oneOf(T... allowedValues) {
        return Validator.of(
                o -> {
                    for (T allowed : allowedValues) {
                        if (allowed.equals(o)) return true;
                    }
                    return false;
                },
                field -> field + " must be one of the allowed values"
        );
    }

    // ========== Boolean Validators ==========

    /**
     * Validates that a boolean is true.
     *
     * @return a true validator
     */
    public static Validator<Boolean> isTrue() {
        return Validator.of(
                b -> Boolean.TRUE.equals(b),
                field -> field + " must be true"
        );
    }

    /**
     * Validates that a boolean is false.
     *
     * @return a false validator
     */
    public static Validator<Boolean> isFalse() {
        return Validator.of(
                b -> Boolean.FALSE.equals(b),
                field -> field + " must be false"
        );
    }
}
