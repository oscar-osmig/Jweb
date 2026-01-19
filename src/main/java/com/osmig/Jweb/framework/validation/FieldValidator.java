package com.osmig.Jweb.framework.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Immutable field validator for building validation chains.
 * <pre>
 * FormValidator.create()
 *     .field("email", email, f -> f.required().email())
 *     .validate();
 * </pre>
 */
public final class FieldValidator {
    private final String name;
    private final String value;
    private final boolean required;
    private final List<Validator<String>> validators;

    private FieldValidator(String name, String value, boolean required, List<Validator<String>> validators) {
        this.name = name;
        this.value = value;
        this.required = required;
        this.validators = validators;
    }

    public static FieldValidator of(String name, String value) {
        return new FieldValidator(name, value, false, new ArrayList<>());
    }

    public FieldValidator required() { return new FieldValidator(name, value, true, validators); }
    public FieldValidator optional() { return new FieldValidator(name, value, false, validators); }
    public FieldValidator minLength(int min) { return add(Validators.minLength(min)); }
    public FieldValidator maxLength(int max) { return add(Validators.maxLength(max)); }
    public FieldValidator lengthBetween(int min, int max) { return add(Validators.lengthBetween(min, max)); }
    public FieldValidator email() { return add(Validators.email()); }
    public FieldValidator numeric() { return add(Validators.numeric()); }
    public FieldValidator alphanumeric() { return add(Validators.alphanumeric()); }
    public FieldValidator alpha() { return add(Validators.alpha()); }
    public FieldValidator url() { return add(Validators.url()); }
    public FieldValidator phone() { return add(Validators.phone()); }
    public FieldValidator pattern(String regex, String msg) { return add(Validators.pattern(regex, msg)); }
    public FieldValidator custom(Validator<String> v) { return add(v); }
    public FieldValidator check(Predicate<String> p, String msg) { return add(Validator.of(p, msg)); }

    private FieldValidator add(Validator<String> v) {
        List<Validator<String>> newList = new ArrayList<>(validators);
        newList.add(v);
        return new FieldValidator(name, value, required, newList);
    }

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (required && (value == null || value.isBlank())) {
            result.addError(name, name + " is required");
            return result;
        }
        if (!required && (value == null || value.isBlank())) return result;
        for (Validator<String> v : validators) result.merge(v.validate(value, name));
        return result;
    }
}
