package com.osmig.Jweb.framework.validation;

import java.util.*;

/**
 * Result of a validation operation.
 *
 * <p>Holds validation errors grouped by field name. A validation result
 * is considered valid if it contains no errors.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * ValidationResult result = validator.validate(user);
 * if (result.isValid()) {
 *     // proceed with valid data
 * } else {
 *     // handle errors
 *     result.getErrors("email").forEach(System.out::println);
 * }
 * </pre>
 */
public class ValidationResult {

    private final Map<String, List<String>> errors = new LinkedHashMap<>();

    /**
     * Creates an empty (valid) result.
     */
    public ValidationResult() {
    }

    /**
     * Creates a result from a map of errors.
     *
     * @param errors map of field names to error messages
     */
    public ValidationResult(Map<String, List<String>> errors) {
        if (errors != null) {
            errors.forEach((field, messages) -> {
                if (messages != null && !messages.isEmpty()) {
                    this.errors.put(field, new ArrayList<>(messages));
                }
            });
        }
    }

    /**
     * Adds an error for a specific field.
     *
     * @param field   the field name
     * @param message the error message
     * @return this result for chaining
     */
    public ValidationResult addError(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        return this;
    }

    /**
     * Adds multiple errors for a specific field.
     *
     * @param field    the field name
     * @param messages the error messages
     * @return this result for chaining
     */
    public ValidationResult addErrors(String field, List<String> messages) {
        if (messages != null && !messages.isEmpty()) {
            errors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(messages);
        }
        return this;
    }

    /**
     * Merges another validation result into this one.
     *
     * @param other the other result
     * @return this result for chaining
     */
    public ValidationResult merge(ValidationResult other) {
        if (other != null) {
            other.errors.forEach(this::addErrors);
        }
        return this;
    }

    /**
     * Checks if the validation passed (no errors).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Checks if the validation failed (has errors).
     *
     * @return true if invalid
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if a specific field has errors.
     *
     * @param field the field name
     * @return true if the field has errors
     */
    public boolean hasErrors(String field) {
        return errors.containsKey(field) && !errors.get(field).isEmpty();
    }

    /**
     * Gets all error messages for a specific field.
     *
     * @param field the field name
     * @return list of error messages (empty if none)
     */
    public List<String> getErrors(String field) {
        return errors.getOrDefault(field, Collections.emptyList());
    }

    /**
     * Gets the first error message for a specific field.
     *
     * @param field the field name
     * @return the first error message, or null if none
     */
    public String getFirstError(String field) {
        List<String> fieldErrors = errors.get(field);
        return (fieldErrors != null && !fieldErrors.isEmpty()) ? fieldErrors.get(0) : null;
    }

    /**
     * Gets all fields that have errors.
     *
     * @return set of field names
     */
    public Set<String> getFieldsWithErrors() {
        return Collections.unmodifiableSet(errors.keySet());
    }

    /**
     * Gets all errors as a map.
     *
     * @return unmodifiable map of field names to error messages
     */
    public Map<String, List<String>> getAllErrors() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        errors.forEach((field, messages) -> copy.put(field, Collections.unmodifiableList(messages)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Gets all error messages flattened into a single list.
     *
     * @return list of all error messages
     */
    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<>();
        errors.values().forEach(messages::addAll);
        return messages;
    }

    /**
     * Gets total count of errors.
     *
     * @return number of errors
     */
    public int getErrorCount() {
        return errors.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Creates a successful (valid) result.
     *
     * @return a valid result with no errors
     */
    public static ValidationResult valid() {
        return new ValidationResult();
    }

    /**
     * Creates a failed result with a single error.
     *
     * @param field   the field name
     * @param message the error message
     * @return an invalid result
     */
    public static ValidationResult invalid(String field, String message) {
        return new ValidationResult().addError(field, message);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "ValidationResult[valid]";
        }
        return "ValidationResult[errors=" + errors + "]";
    }
}
