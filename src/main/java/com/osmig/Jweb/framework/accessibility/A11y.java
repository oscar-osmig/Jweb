package com.osmig.Jweb.framework.accessibility;

import com.osmig.Jweb.framework.core.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accessibility (A11y) validation helpers for JWeb applications.
 *
 * <p>Provides utilities to check HTML output for common accessibility issues
 * based on WCAG 2.1 guidelines.</p>
 *
 * <h2>Validating Elements</h2>
 * <pre>
 * Element page = div(
 *     img(attrs().src("/logo.png")),  // Missing alt text!
 *     button("Submit")                 // OK
 * );
 *
 * A11y.ValidationResult result = A11y.validate(page);
 * if (!result.isValid()) {
 *     result.getIssues().forEach(issue ->
 *         System.out.println(issue.getSeverity() + ": " + issue.getMessage())
 *     );
 * }
 * </pre>
 *
 * <h2>Validating HTML Strings</h2>
 * <pre>
 * String html = "&lt;img src='logo.png'&gt;";
 * A11y.ValidationResult result = A11y.validateHtml(html);
 * </pre>
 *
 * <h2>Quick Checks</h2>
 * <pre>
 * // Check specific issues
 * A11y.hasImageAlt(html);           // All images have alt attributes
 * A11y.hasFormLabels(html);         // All form inputs have labels
 * A11y.hasButtonText(html);         // All buttons have accessible text
 * A11y.hasHeadingHierarchy(html);   // Proper heading structure (h1-h6)
 * A11y.hasLinkText(html);           // Links have descriptive text
 * </pre>
 *
 * <h2>Severity Levels</h2>
 * <ul>
 *   <li>ERROR - Critical accessibility violation (WCAG Level A)</li>
 *   <li>WARNING - Important issue (WCAG Level AA)</li>
 *   <li>INFO - Suggestion for improvement (WCAG Level AAA)</li>
 * </ul>
 */
public final class A11y {

    private A11y() {
        // Static utility class
    }

    // ========== Validation ==========

    /**
     * Validates an Element for accessibility issues.
     *
     * @param element the element to validate
     * @return validation result with any issues found
     */
    public static ValidationResult validate(Element element) {
        return validateHtml(element.toHtml());
    }

    /**
     * Validates an HTML string for accessibility issues.
     *
     * @param html the HTML to validate
     * @return validation result with any issues found
     */
    public static ValidationResult validateHtml(String html) {
        List<Issue> issues = new ArrayList<>();

        // Check for images without alt text
        checkImageAlt(html, issues);

        // Check for form labels
        checkFormLabels(html, issues);

        // Check for button accessible names
        checkButtonText(html, issues);

        // Check heading hierarchy
        checkHeadingHierarchy(html, issues);

        // Check link text
        checkLinkText(html, issues);

        // Check for language attribute
        checkLanguageAttr(html, issues);

        // Check for table headers
        checkTableHeaders(html, issues);

        // Check for ARIA misuse
        checkAriaUsage(html, issues);

        // Check color contrast (basic)
        checkColorContrast(html, issues);

        // Check focus management
        checkFocusManagement(html, issues);

        return new ValidationResult(issues);
    }

    // ========== Quick Checks ==========

    /**
     * Checks if all images have alt attributes.
     *
     * @param html the HTML to check
     * @return true if all images have alt text
     */
    public static boolean hasImageAlt(String html) {
        Pattern imgPattern = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = imgPattern.matcher(html);
        while (matcher.find()) {
            String img = matcher.group();
            if (!img.contains("alt=") && !img.contains("role=\"presentation\"")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all form inputs have associated labels.
     *
     * @param html the HTML to check
     * @return true if all inputs have labels
     */
    public static boolean hasFormLabels(String html) {
        List<Issue> issues = new ArrayList<>();
        checkFormLabels(html, issues);
        return issues.isEmpty();
    }

    /**
     * Checks if all buttons have accessible text.
     *
     * @param html the HTML to check
     * @return true if all buttons have text
     */
    public static boolean hasButtonText(String html) {
        List<Issue> issues = new ArrayList<>();
        checkButtonText(html, issues);
        return issues.isEmpty();
    }

    /**
     * Checks if heading hierarchy is correct.
     *
     * @param html the HTML to check
     * @return true if headings are in proper order
     */
    public static boolean hasHeadingHierarchy(String html) {
        List<Issue> issues = new ArrayList<>();
        checkHeadingHierarchy(html, issues);
        return issues.isEmpty();
    }

    /**
     * Checks if all links have descriptive text.
     *
     * @param html the HTML to check
     * @return true if all links have text
     */
    public static boolean hasLinkText(String html) {
        List<Issue> issues = new ArrayList<>();
        checkLinkText(html, issues);
        return issues.isEmpty();
    }

    // ========== Internal Checks ==========

    private static void checkImageAlt(String html, List<Issue> issues) {
        Pattern imgPattern = Pattern.compile("<img([^>]*)>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = imgPattern.matcher(html);
        while (matcher.find()) {
            String attrs = matcher.group(1);
            if (!attrs.contains("alt=") && !attrs.contains("role=\"presentation\"") && !attrs.contains("role='presentation'")) {
                issues.add(new Issue(
                        Severity.ERROR,
                        "Image missing alt attribute",
                        "All images must have an alt attribute. Use alt=\"\" for decorative images or role=\"presentation\".",
                        "WCAG 1.1.1",
                        extractContext(html, matcher.start())
                ));
            } else if (attrs.contains("alt=\"\"") || attrs.contains("alt=''")) {
                // Empty alt is OK for decorative images, but warn if image seems meaningful
                if (attrs.contains("src=") && !attrs.contains("icon") && !attrs.contains("decoration")) {
                    issues.add(new Issue(
                            Severity.INFO,
                            "Image has empty alt text",
                            "Empty alt is correct for decorative images. Ensure this image is purely decorative.",
                            "WCAG 1.1.1",
                            extractContext(html, matcher.start())
                    ));
                }
            }
        }
    }

    private static void checkFormLabels(String html, List<Issue> issues) {
        // Find inputs that need labels
        Pattern inputPattern = Pattern.compile("<input([^>]*)>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = inputPattern.matcher(html);
        while (matcher.find()) {
            String attrs = matcher.group(1);
            // Skip hidden, submit, button, image, reset types
            if (attrs.contains("type=\"hidden\"") || attrs.contains("type=\"submit\"") ||
                attrs.contains("type=\"button\"") || attrs.contains("type=\"image\"") ||
                attrs.contains("type=\"reset\"")) {
                continue;
            }

            // Check for id and matching label, or aria-label, or aria-labelledby
            if (!attrs.contains("aria-label") && !attrs.contains("aria-labelledby")) {
                Pattern idPattern = Pattern.compile("id=[\"']([^\"']+)[\"']");
                Matcher idMatcher = idPattern.matcher(attrs);
                if (idMatcher.find()) {
                    String id = idMatcher.group(1);
                    if (!html.contains("for=\"" + id + "\"") && !html.contains("for='" + id + "'")) {
                        issues.add(new Issue(
                                Severity.ERROR,
                                "Form input missing label",
                                "Input with id=\"" + id + "\" has no associated <label> element. Add <label for=\"" + id + "\"> or aria-label.",
                                "WCAG 1.3.1, 3.3.2",
                                extractContext(html, matcher.start())
                        ));
                    }
                } else if (!attrs.contains("aria-label")) {
                    issues.add(new Issue(
                            Severity.ERROR,
                            "Form input missing label",
                            "Input has no id for label association and no aria-label.",
                            "WCAG 1.3.1, 3.3.2",
                            extractContext(html, matcher.start())
                    ));
                }
            }
        }
    }

    private static void checkButtonText(String html, List<Issue> issues) {
        // Check <button> elements
        Pattern buttonPattern = Pattern.compile("<button([^>]*)>(.*?)</button>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = buttonPattern.matcher(html);
        while (matcher.find()) {
            String attrs = matcher.group(1);
            String content = matcher.group(2).trim();

            // Remove HTML tags to get text content
            String textContent = content.replaceAll("<[^>]+>", "").trim();

            if (textContent.isEmpty() && !attrs.contains("aria-label") && !attrs.contains("aria-labelledby")) {
                issues.add(new Issue(
                        Severity.ERROR,
                        "Button has no accessible name",
                        "Buttons must have text content or aria-label.",
                        "WCAG 4.1.2",
                        extractContext(html, matcher.start())
                ));
            }
        }

        // Check input[type="button"], input[type="submit"]
        Pattern inputButtonPattern = Pattern.compile("<input([^>]*type=[\"'](button|submit)[\"'][^>]*)>", Pattern.CASE_INSENSITIVE);
        Matcher inputMatcher = inputButtonPattern.matcher(html);
        while (inputMatcher.find()) {
            String attrs = inputMatcher.group(1);
            if (!attrs.contains("value=") && !attrs.contains("aria-label")) {
                issues.add(new Issue(
                        Severity.ERROR,
                        "Input button has no accessible name",
                        "Input buttons must have a value attribute or aria-label.",
                        "WCAG 4.1.2",
                        extractContext(html, inputMatcher.start())
                ));
            }
        }
    }

    private static void checkHeadingHierarchy(String html, List<Issue> issues) {
        Pattern headingPattern = Pattern.compile("<h([1-6])[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = headingPattern.matcher(html);

        int lastLevel = 0;
        boolean hasH1 = false;

        while (matcher.find()) {
            int level = Integer.parseInt(matcher.group(1));

            if (level == 1) {
                if (hasH1) {
                    issues.add(new Issue(
                            Severity.WARNING,
                            "Multiple h1 elements",
                            "Page should typically have only one h1 element.",
                            "WCAG 1.3.1",
                            extractContext(html, matcher.start())
                    ));
                }
                hasH1 = true;
            }

            if (lastLevel > 0 && level > lastLevel + 1) {
                issues.add(new Issue(
                        Severity.WARNING,
                        "Skipped heading level",
                        "Heading level jumped from h" + lastLevel + " to h" + level + ". Heading levels should not skip.",
                        "WCAG 1.3.1",
                        extractContext(html, matcher.start())
                ));
            }

            lastLevel = level;
        }

        // Check if page has h1
        if (html.contains("<h") && !hasH1) {
            issues.add(new Issue(
                    Severity.WARNING,
                    "Missing h1 element",
                    "Page has headings but no h1. Consider adding an h1 as the main page heading.",
                    "WCAG 1.3.1",
                    null
            ));
        }
    }

    private static void checkLinkText(String html, List<Issue> issues) {
        Pattern linkPattern = Pattern.compile("<a([^>]*)>(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = linkPattern.matcher(html);

        Set<String> genericTexts = Set.of("click here", "here", "read more", "more", "link", "learn more");

        while (matcher.find()) {
            String attrs = matcher.group(1);
            String content = matcher.group(2).trim();
            String textContent = content.replaceAll("<[^>]+>", "").trim().toLowerCase();

            if (textContent.isEmpty() && !attrs.contains("aria-label") && !attrs.contains("aria-labelledby")) {
                // Check if link contains an image with alt
                if (!content.contains("alt=")) {
                    issues.add(new Issue(
                            Severity.ERROR,
                            "Link has no accessible name",
                            "Links must have text content, aria-label, or contain an image with alt text.",
                            "WCAG 2.4.4",
                            extractContext(html, matcher.start())
                    ));
                }
            } else if (genericTexts.contains(textContent)) {
                issues.add(new Issue(
                        Severity.WARNING,
                        "Link has generic text",
                        "Link text \"" + textContent + "\" is not descriptive. Use text that describes the link destination.",
                        "WCAG 2.4.4",
                        extractContext(html, matcher.start())
                ));
            }
        }
    }

    private static void checkLanguageAttr(String html, List<Issue> issues) {
        if (html.contains("<html") && !html.contains("lang=")) {
            issues.add(new Issue(
                    Severity.ERROR,
                    "Missing language attribute",
                    "The <html> element should have a lang attribute specifying the page language (e.g., lang=\"en\").",
                    "WCAG 3.1.1",
                    null
            ));
        }
    }

    private static void checkTableHeaders(String html, List<Issue> issues) {
        // Check if tables have th elements or scope attributes
        Pattern tablePattern = Pattern.compile("<table[^>]*>(.*?)</table>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = tablePattern.matcher(html);

        while (matcher.find()) {
            String tableContent = matcher.group(1);

            // Skip if table has role="presentation"
            String tableTag = html.substring(matcher.start(), html.indexOf('>', matcher.start()) + 1);
            if (tableTag.contains("role=\"presentation\"") || tableTag.contains("role='presentation'")) {
                continue;
            }

            if (!tableContent.contains("<th")) {
                issues.add(new Issue(
                        Severity.WARNING,
                        "Table missing headers",
                        "Data tables should have <th> elements to identify column/row headers.",
                        "WCAG 1.3.1",
                        extractContext(html, matcher.start())
                ));
            }
        }
    }

    private static void checkAriaUsage(String html, List<Issue> issues) {
        // Check for common ARIA misuses

        // aria-hidden on focusable elements
        Pattern ariaHiddenPattern = Pattern.compile("<(a|button|input|select|textarea)[^>]*aria-hidden=[\"']true[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = ariaHiddenPattern.matcher(html);
        while (matcher.find()) {
            issues.add(new Issue(
                    Severity.ERROR,
                    "aria-hidden on focusable element",
                    "Using aria-hidden=\"true\" on focusable elements can cause accessibility issues.",
                    "ARIA",
                    extractContext(html, matcher.start())
            ));
        }

        // role="button" without keyboard support
        Pattern roleButtonPattern = Pattern.compile("<(?!button)[^>]*role=[\"']button[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher buttonMatcher = roleButtonPattern.matcher(html);
        while (buttonMatcher.find()) {
            String element = buttonMatcher.group();
            if (!element.contains("tabindex")) {
                issues.add(new Issue(
                        Severity.WARNING,
                        "Custom button may lack keyboard support",
                        "Elements with role=\"button\" should have tabindex=\"0\" for keyboard accessibility.",
                        "WCAG 2.1.1",
                        extractContext(html, buttonMatcher.start())
                ));
            }
        }
    }

    private static void checkColorContrast(String html, List<Issue> issues) {
        // Basic check: warn if inline styles set color without background
        Pattern colorPattern = Pattern.compile("style=[\"'][^\"']*color:\\s*([^;\"']+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = colorPattern.matcher(html);
        while (matcher.find()) {
            String fullStyle = html.substring(matcher.start(), Math.min(html.length(), matcher.end() + 100));
            if (!fullStyle.contains("background") && !fullStyle.contains("bg-color")) {
                issues.add(new Issue(
                        Severity.INFO,
                        "Color set without background",
                        "Setting text color without a background color may cause contrast issues in some contexts. Consider specifying both.",
                        "WCAG 1.4.3",
                        extractContext(html, matcher.start())
                ));
            }
        }
    }

    private static void checkFocusManagement(String html, List<Issue> issues) {
        // Check for tabindex > 0
        Pattern tabindexPattern = Pattern.compile("tabindex=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = tabindexPattern.matcher(html);
        while (matcher.find()) {
            String value = matcher.group(1);
            try {
                int tabindex = Integer.parseInt(value);
                if (tabindex > 0) {
                    issues.add(new Issue(
                            Severity.WARNING,
                            "Positive tabindex value",
                            "tabindex=\"" + tabindex + "\" creates a non-standard tab order. Prefer tabindex=\"0\" or rely on DOM order.",
                            "WCAG 2.4.3",
                            extractContext(html, matcher.start())
                    ));
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric tabindex
            }
        }
    }

    private static String extractContext(String html, int position) {
        int start = Math.max(0, position - 20);
        int end = Math.min(html.length(), position + 60);
        String context = html.substring(start, end).replaceAll("\\s+", " ");
        if (start > 0) context = "..." + context;
        if (end < html.length()) context = context + "...";
        return context;
    }

    // ========== Issue & Result Classes ==========

    /**
     * Severity levels for accessibility issues.
     */
    public enum Severity {
        /** Critical violation - must fix (WCAG Level A) */
        ERROR,
        /** Important issue - should fix (WCAG Level AA) */
        WARNING,
        /** Suggestion for improvement (WCAG Level AAA) */
        INFO
    }

    /**
     * Represents a single accessibility issue.
     */
    public static class Issue {
        private final Severity severity;
        private final String message;
        private final String recommendation;
        private final String wcagCriteria;
        private final String context;

        public Issue(Severity severity, String message, String recommendation, String wcagCriteria, String context) {
            this.severity = severity;
            this.message = message;
            this.recommendation = recommendation;
            this.wcagCriteria = wcagCriteria;
            this.context = context;
        }

        public Severity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getRecommendation() { return recommendation; }
        public String getWcagCriteria() { return wcagCriteria; }
        public String getContext() { return context; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(severity).append("] ").append(message);
            if (wcagCriteria != null) {
                sb.append(" (").append(wcagCriteria).append(")");
            }
            sb.append("\n  ").append(recommendation);
            if (context != null) {
                sb.append("\n  Context: ").append(context);
            }
            return sb.toString();
        }
    }

    /**
     * Result of accessibility validation.
     */
    public static class ValidationResult {
        private final List<Issue> issues;

        public ValidationResult(List<Issue> issues) {
            this.issues = Collections.unmodifiableList(issues);
        }

        /**
         * Returns true if no issues were found.
         */
        public boolean isValid() {
            return issues.isEmpty();
        }

        /**
         * Returns true if no errors were found (warnings/info are OK).
         */
        public boolean hasNoErrors() {
            return issues.stream().noneMatch(i -> i.getSeverity() == Severity.ERROR);
        }

        /**
         * Gets all issues.
         */
        public List<Issue> getIssues() {
            return issues;
        }

        /**
         * Gets issues filtered by severity.
         */
        public List<Issue> getIssues(Severity severity) {
            return issues.stream()
                    .filter(i -> i.getSeverity() == severity)
                    .toList();
        }

        /**
         * Gets error count.
         */
        public long getErrorCount() {
            return issues.stream().filter(i -> i.getSeverity() == Severity.ERROR).count();
        }

        /**
         * Gets warning count.
         */
        public long getWarningCount() {
            return issues.stream().filter(i -> i.getSeverity() == Severity.WARNING).count();
        }

        @Override
        public String toString() {
            if (isValid()) {
                return "No accessibility issues found.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(issues.size()).append(" accessibility issue(s):\n");
            sb.append("  Errors: ").append(getErrorCount()).append("\n");
            sb.append("  Warnings: ").append(getWarningCount()).append("\n\n");
            issues.forEach(issue -> sb.append(issue).append("\n\n"));
            return sb.toString();
        }
    }
}
