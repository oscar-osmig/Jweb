package com.osmig.Jweb.framework.validation;

import java.util.*;
import java.util.function.Function;

/**
 * Fluent form validator for validating key-value form data.
 *
 * <p>Modern lambda-based usage (recommended):</p>
 * <pre>
 * ValidationResult result = FormValidator.create()
 *     .field("email", email, f -> f.required().email())
 *     .field("password", password, f -> f.required().minLength(8))
 *     .field("age", age, f -> f.optional().numeric())
 *     .validate();
 * </pre>
 *
 * <p>Legacy chained usage (still supported):</p>
 * <pre>
 * ValidationResult result = FormValidator.create()
 *     .field("email", email).required().email()
 *     .field("password", password).required().minLength(8)
 *     .validate();
 * </pre>
 */
public class FormValidator {

    private final List<FieldValidator> fieldValidators;
    private final List<InternalField> legacyFields;
    private InternalField currentField;

    private FormValidator(List<FieldValidator> fieldValidators, List<InternalField> legacyFields) {
        this.fieldValidators = fieldValidators;
        this.legacyFields = legacyFields;
        this.currentField = null;
    }

    public static FormValidator create() {
        return new FormValidator(new ArrayList<>(), new ArrayList<>());
    }

    // ==================== Lambda-based API (Immutable) ====================

    /**
     * Adds a field with lambda-based validation configuration.
     *
     * @param name      the field name
     * @param value     the field value
     * @param configure lambda that configures the field validator
     * @return this validator for chaining
     */
    public FormValidator field(String name, String value, Function<FieldValidator, FieldValidator> configure) {
        flushCurrentField();
        FieldValidator fv = configure.apply(FieldValidator.of(name, value));
        List<FieldValidator> newValidators = new ArrayList<>(fieldValidators);
        newValidators.add(fv);
        return new FormValidator(newValidators, legacyFields);
    }

    // ==================== Legacy Chained API ====================

    /**
     * Starts validation for a new field (legacy pattern).
     *
     * @param name  the field name
     * @param value the field value
     * @return this validator for chaining
     */
    public FormValidator field(String name, String value) {
        flushCurrentField();
        currentField = new InternalField(name, value);
        return this;
    }

    private void flushCurrentField() {
        if (currentField != null) {
            legacyFields.add(currentField);
            currentField = null;
        }
    }

    public FormValidator required() {
        if (currentField != null) currentField.required = true;
        return this;
    }

    public FormValidator optional() {
        if (currentField != null) currentField.required = false;
        return this;
    }

    public FormValidator minLength(int min) {
        if (currentField != null) currentField.validators.add(Validators.minLength(min));
        return this;
    }

    public FormValidator maxLength(int max) {
        if (currentField != null) currentField.validators.add(Validators.maxLength(max));
        return this;
    }

    public FormValidator lengthBetween(int min, int max) {
        if (currentField != null) currentField.validators.add(Validators.lengthBetween(min, max));
        return this;
    }

    public FormValidator email() {
        if (currentField != null) currentField.validators.add(Validators.email());
        return this;
    }

    public FormValidator numeric() {
        if (currentField != null) currentField.validators.add(Validators.numeric());
        return this;
    }

    public FormValidator alphanumeric() {
        if (currentField != null) currentField.validators.add(Validators.alphanumeric());
        return this;
    }

    public FormValidator alpha() {
        if (currentField != null) currentField.validators.add(Validators.alpha());
        return this;
    }

    public FormValidator url() {
        if (currentField != null) currentField.validators.add(Validators.url());
        return this;
    }

    public FormValidator phone() {
        if (currentField != null) currentField.validators.add(Validators.phone());
        return this;
    }

    public FormValidator pattern(String regex, String message) {
        if (currentField != null) currentField.validators.add(Validators.pattern(regex, message));
        return this;
    }

    public FormValidator matches(String otherFieldName, String otherFieldValue) {
        if (currentField != null) {
            currentField.validators.add(Validator.of(
                s -> Objects.equals(s, otherFieldValue),
                field -> field + " must match " + otherFieldName
            ));
        }
        return this;
    }

    public FormValidator custom(Validator<String> validator) {
        if (currentField != null) currentField.validators.add(validator);
        return this;
    }

    public FormValidator check(java.util.function.Predicate<String> predicate, String message) {
        if (currentField != null) currentField.validators.add(Validator.of(predicate, message));
        return this;
    }

    // ==================== Validate ====================

    public ValidationResult validate() {
        flushCurrentField();
        ValidationResult result = new ValidationResult();

        for (FieldValidator fv : fieldValidators) {
            result.merge(fv.validate());
        }

        for (InternalField field : legacyFields) {
            result.merge(field.validate());
        }

        return result;
    }

    // ==================== Internal Field Class (Legacy Support) ====================

    private static class InternalField {
        final String name;
        final String value;
        boolean required = false;
        final List<Validator<String>> validators = new ArrayList<>();

        InternalField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        ValidationResult validate() {
            ValidationResult result = new ValidationResult();

            if (required) {
                if (value == null || value.isBlank()) {
                    result.addError(name, name + " is required");
                    return result;
                }
            } else {
                if (value == null || value.isBlank()) {
                    return result;
                }
            }

            for (Validator<String> validator : validators) {
                result.merge(validator.validate(value, name));
            }

            return result;
        }
    }
}
