package com.osmig.Jweb.framework.validation;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.ValidationResult} — same class, shorter import.
 *             Validators return {@code jweb.ValidationResult}.
 */
@Deprecated
public class ValidationResult extends jweb.ValidationResult {

    public ValidationResult() {}

    public ValidationResult(Map<String, List<String>> errors) {
        super(errors);
    }
}
