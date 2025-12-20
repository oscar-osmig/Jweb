/**
 * Validation framework for JWeb.
 *
 * <p>This package provides a fluent, type-safe validation API for validating
 * user input, form data, and request parameters.</p>
 *
 * <h2>Core Classes</h2>
 * <ul>
 *   <li>{@link com.osmig.Jweb.framework.validation.ValidationResult} - Holds validation errors</li>
 *   <li>{@link com.osmig.Jweb.framework.validation.Validator} - Functional validator interface</li>
 *   <li>{@link com.osmig.Jweb.framework.validation.Validators} - Built-in validator factories</li>
 *   <li>{@link com.osmig.Jweb.framework.validation.FormValidator} - Fluent form validation builder</li>
 * </ul>
 *
 * <h2>Example: Form Validation</h2>
 * <pre>{@code
 * ValidationResult result = FormValidator.create()
 *     .field("username", formData.get("username"))
 *         .required()
 *         .minLength(3)
 *         .maxLength(20)
 *         .alphanumeric()
 *     .field("email", formData.get("email"))
 *         .required()
 *         .email()
 *     .field("password", formData.get("password"))
 *         .required()
 *         .minLength(8)
 *     .field("confirmPassword", formData.get("confirmPassword"))
 *         .required()
 *         .matches("password", formData.get("password"))
 *     .validate();
 *
 * if (result.hasErrors()) {
 *     // Display errors
 *     result.getAllErrors().forEach((field, errors) -> {
 *         System.out.println(field + ": " + errors);
 *     });
 * }
 * }</pre>
 *
 * <h2>Example: Custom Validators</h2>
 * <pre>{@code
 * // Create a reusable validator
 * Validator<String> strongPassword = Validators.required()
 *     .and(Validators.minLength(8))
 *     .and(Validators.pattern(".*[A-Z].*", "Must contain uppercase letter"))
 *     .and(Validators.pattern(".*[a-z].*", "Must contain lowercase letter"))
 *     .and(Validators.pattern(".*[0-9].*", "Must contain digit"));
 *
 * ValidationResult result = strongPassword.validate(password, "password");
 * }</pre>
 *
 * <h2>Example: Object Validation</h2>
 * <pre>{@code
 * // Validate a user object
 * User user = new User("john", "john@example.com", 25);
 *
 * ValidationResult result = new ValidationResult()
 *     .merge(Validators.required().validate(user.getName(), "name"))
 *     .merge(Validators.email().validate(user.getEmail(), "email"))
 *     .merge(Validators.range(18, 120).validate(user.getAge(), "age"));
 * }</pre>
 */
package com.osmig.Jweb.framework.validation;
