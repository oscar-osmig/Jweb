package com.osmig.Jweb.framework.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

/**
 * Password hashing utilities using BCrypt.
 *
 * <p>Usage:</p>
 * <pre>
 * // Hash a password (for storage)
 * String hash = Password.hash("myPassword123");
 *
 * // Verify a password (for login)
 * if (Password.verify("myPassword123", hash)) {
 *     // Password matches
 * }
 *
 * // Check if password needs rehashing (after strength change)
 * if (Password.needsRehash(hash)) {
 *     String newHash = Password.hash(plainPassword);
 * }
 * </pre>
 *
 * <p>Strength configuration:</p>
 * <pre>
 * // Default strength is 12 (good balance of security and speed)
 * Password.setStrength(14);  // Higher = more secure but slower
 * </pre>
 */
public final class Password {

    private static int strength = 12;  // BCrypt work factor (log rounds)
    private static PasswordEncoder encoder = new BCryptPasswordEncoder(strength);

    private Password() {}

    // ==================== Main Operations ====================

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainPassword the plain text password
     * @return the BCrypt hash (includes salt and algorithm info)
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    /**
     * Verifies a plain text password against a hash.
     *
     * @param plainPassword the plain text password to check
     * @param hash the stored BCrypt hash
     * @return true if the password matches the hash
     */
    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null) {
            return false;
        }
        try {
            return encoder.matches(plainPassword, hash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Alias for verify() - checks if password matches hash.
     */
    public static boolean matches(String plainPassword, String hash) {
        return verify(plainPassword, hash);
    }

    // ==================== Rehashing ====================

    /**
     * Checks if a hash should be rehashed (e.g., after strength change).
     * BCrypt hashes include the work factor, so we can detect if it's outdated.
     *
     * @param hash the existing hash
     * @return true if the hash uses a different strength than current
     */
    public static boolean needsRehash(String hash) {
        if (hash == null || hash.length() < 7) return true;

        try {
            // BCrypt hash format: $2a$XX$... where XX is the work factor
            String[] parts = hash.split("\\$");
            if (parts.length < 3) return true;

            int hashStrength = Integer.parseInt(parts[2]);
            return hashStrength != strength;
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== Configuration ====================

    /**
     * Sets the BCrypt strength (work factor).
     * Higher values are more secure but slower.
     * Recommended: 10-14 for web apps, 14+ for high-security.
     *
     * @param newStrength work factor (4-31, default 12)
     */
    public static void setStrength(int newStrength) {
        if (newStrength < 4 || newStrength > 31) {
            throw new IllegalArgumentException("Strength must be between 4 and 31");
        }
        strength = newStrength;
        encoder = new BCryptPasswordEncoder(strength);
    }

    /**
     * Gets the current BCrypt strength.
     */
    public static int getStrength() {
        return strength;
    }

    // ==================== Password Generation ====================

    /**
     * Generates a random password.
     *
     * @param length password length
     * @return random password string
     */
    public static String generate(int length) {
        return generate(length, true, true, true);
    }

    /**
     * Generates a random password with options.
     *
     * @param length password length
     * @param includeUppercase include A-Z
     * @param includeNumbers include 0-9
     * @param includeSpecial include special characters
     * @return random password string
     */
    public static String generate(int length, boolean includeUppercase, boolean includeNumbers, boolean includeSpecial) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be at least 1");
        }

        StringBuilder chars = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
        if (includeUppercase) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (includeNumbers) chars.append("0123456789");
        if (includeSpecial) chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    // ==================== Validation ====================

    /**
     * Checks if a password meets minimum requirements.
     *
     * @param password the password to check
     * @param minLength minimum length required
     * @return true if password meets requirements
     */
    public static boolean isStrong(String password, int minLength) {
        if (password == null || password.length() < minLength) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Checks if password is strong (minimum 8 chars, upper, lower, digit).
     */
    public static boolean isStrong(String password) {
        return isStrong(password, 8);
    }

    /**
     * Validates password and returns validation result.
     */
    public static ValidationResult validate(String password) {
        return validate(password, 8);
    }

    /**
     * Validates password with custom minimum length.
     */
    public static ValidationResult validate(String password, int minLength) {
        if (password == null) {
            return new ValidationResult(false, "Password is required");
        }
        if (password.length() < minLength) {
            return new ValidationResult(false, "Password must be at least " + minLength + " characters");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (!hasUpper) return new ValidationResult(false, "Password must contain an uppercase letter");
        if (!hasLower) return new ValidationResult(false, "Password must contain a lowercase letter");
        if (!hasDigit) return new ValidationResult(false, "Password must contain a number");

        return new ValidationResult(true, "Password is valid");
    }

    /**
     * Password validation result.
     */
    public record ValidationResult(boolean valid, String message) {
        public boolean isValid() { return valid; }
    }
}
