package com.osmig.Jweb.framework.validation;

/**
 * Built-in validators for numeric values.
 * <pre>
 * NumberValidators.min(0).validate(count, "count");
 * NumberValidators.range(1, 100).validate(percentage, "percentage");
 * </pre>
 */
public final class NumberValidators {
    private NumberValidators() {}

    public static <T extends Number> Validator<T> required() {
        return Validator.of(n -> n != null, f -> f + " is required");
    }

    public static Validator<Number> min(double min) {
        return Validator.of(n -> n != null && n.doubleValue() >= min, f -> f + " must be at least " + min);
    }

    public static Validator<Number> max(double max) {
        return Validator.of(n -> n != null && n.doubleValue() <= max, f -> f + " must be at most " + max);
    }

    public static Validator<Number> range(double min, double max) {
        return Validator.of(n -> n != null && n.doubleValue() >= min && n.doubleValue() <= max,
            f -> f + " must be between " + min + " and " + max);
    }

    public static Validator<Number> positive() {
        return Validator.of(n -> n != null && n.doubleValue() > 0, f -> f + " must be positive");
    }

    public static Validator<Number> negative() {
        return Validator.of(n -> n != null && n.doubleValue() < 0, f -> f + " must be negative");
    }

    public static Validator<Number> nonNegative() {
        return Validator.of(n -> n != null && n.doubleValue() >= 0, f -> f + " must not be negative");
    }

    public static Validator<Integer> intRange(int min, int max) {
        return Validator.of(n -> n != null && n >= min && n <= max, f -> f + " must be between " + min + " and " + max);
    }
}
