package com.osmig.Jweb.framework.styles;

/**
 * CSS selector builders for modern and complex selectors.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.Selectors.*;
 *
 *   // :has() selector - style parent based on children
 *   has(".icon")              // :has(.icon)
 *   has("> img")              // :has(> img)
 *
 *   // :is() selector - matches any of the selectors
 *   is("h1", "h2", "h3")      // :is(h1, h2, h3)
 *
 *   // :where() selector - same as :is() but zero specificity
 *   where("article", "section") // :where(article, section)
 *
 *   // :not() selector - negation
 *   not(".disabled")          // :not(.disabled)
 *
 *   // Combine in CSS rules:
 *   ".card" + has(".featured")  // .card:has(.featured)
 */
public final class Selectors {

    private Selectors() {}

    // ==================== Modern Selectors ====================

    /**
     * Creates a :has() selector.
     * Selects elements that contain elements matching the argument.
     *
     * Example: has(".active") -> :has(.active)
     */
    public static String has(String selector) {
        return ":has(" + selector + ")";
    }

    /**
     * Creates a :has() selector with multiple arguments.
     *
     * Example: has(".icon", ".badge") -> :has(.icon, .badge)
     */
    public static String has(String... selectors) {
        return ":has(" + String.join(", ", selectors) + ")";
    }

    /**
     * Creates an :is() selector.
     * Matches any element that matches any of the selectors.
     * Takes the specificity of its most specific argument.
     *
     * Example: is("h1", "h2", "h3") -> :is(h1, h2, h3)
     */
    public static String is(String... selectors) {
        return ":is(" + String.join(", ", selectors) + ")";
    }

    /**
     * Creates a :where() selector.
     * Same as :is() but always has zero specificity.
     *
     * Example: where("article", "section") -> :where(article, section)
     */
    public static String where(String... selectors) {
        return ":where(" + String.join(", ", selectors) + ")";
    }

    /**
     * Creates a :not() selector.
     * Matches elements that don't match the argument.
     *
     * Example: not(".disabled") -> :not(.disabled)
     */
    public static String not(String selector) {
        return ":not(" + selector + ")";
    }

    /**
     * Creates a :not() selector with multiple arguments.
     *
     * Example: not(".disabled", ".hidden") -> :not(.disabled, .hidden)
     */
    public static String not(String... selectors) {
        return ":not(" + String.join(", ", selectors) + ")";
    }

    // ==================== Structural Selectors ====================

    /**
     * :nth-child() selector.
     *
     * Example: nthChild("2n+1") -> :nth-child(2n+1)
     */
    public static String nthChild(String expression) {
        return ":nth-child(" + expression + ")";
    }

    /**
     * :nth-child() with number.
     *
     * Example: nthChild(3) -> :nth-child(3)
     */
    public static String nthChild(int n) {
        return ":nth-child(" + n + ")";
    }

    /**
     * :nth-last-child() selector.
     */
    public static String nthLastChild(String expression) {
        return ":nth-last-child(" + expression + ")";
    }

    public static String nthLastChild(int n) {
        return ":nth-last-child(" + n + ")";
    }

    /**
     * :nth-of-type() selector.
     */
    public static String nthOfType(String expression) {
        return ":nth-of-type(" + expression + ")";
    }

    public static String nthOfType(int n) {
        return ":nth-of-type(" + n + ")";
    }

    /**
     * :nth-last-of-type() selector.
     */
    public static String nthLastOfType(String expression) {
        return ":nth-last-of-type(" + expression + ")";
    }

    public static String nthLastOfType(int n) {
        return ":nth-last-of-type(" + n + ")";
    }

    // ==================== Common Patterns ====================

    /** :first-child */
    public static final String firstChild = ":first-child";

    /** :last-child */
    public static final String lastChild = ":last-child";

    /** :first-of-type */
    public static final String firstOfType = ":first-of-type";

    /** :last-of-type */
    public static final String lastOfType = ":last-of-type";

    /** :only-child */
    public static final String onlyChild = ":only-child";

    /** :only-of-type */
    public static final String onlyOfType = ":only-of-type";

    /** :empty */
    public static final String empty = ":empty";

    /** :root */
    public static final String root = ":root";

    /** :target */
    public static final String target = ":target";

    // ==================== Form State Selectors ====================

    /** :checked */
    public static final String checked = ":checked";

    /** :disabled */
    public static final String disabled = ":disabled";

    /** :enabled */
    public static final String enabled = ":enabled";

    /** :required */
    public static final String required = ":required";

    /** :optional */
    public static final String optional = ":optional";

    /** :valid */
    public static final String valid = ":valid";

    /** :invalid */
    public static final String invalid = ":invalid";

    /** :in-range */
    public static final String inRange = ":in-range";

    /** :out-of-range */
    public static final String outOfRange = ":out-of-range";

    /** :read-only */
    public static final String readOnly = ":read-only";

    /** :read-write */
    public static final String readWrite = ":read-write";

    /** :placeholder-shown */
    public static final String placeholderShown = ":placeholder-shown";

    /** :focus-visible */
    public static final String focusVisible = ":focus-visible";

    /** :focus-within */
    public static final String focusWithin = ":focus-within";

    // ==================== Pseudo-elements ====================

    /** ::before */
    public static final String before = "::before";

    /** ::after */
    public static final String after = "::after";

    /** ::first-line */
    public static final String firstLine = "::first-line";

    /** ::first-letter */
    public static final String firstLetter = "::first-letter";

    /** ::selection */
    public static final String selection = "::selection";

    /** ::placeholder */
    public static final String placeholder = "::placeholder";

    /** ::marker */
    public static final String marker = "::marker";

    /** ::backdrop */
    public static final String backdrop = "::backdrop";

    // ==================== Scrollbar Pseudo-elements ====================

    /** ::-webkit-scrollbar - the entire scrollbar */
    public static final String scrollbar = "::-webkit-scrollbar";

    /** ::-webkit-scrollbar-track - the track (progress bar) */
    public static final String scrollbarTrack = "::-webkit-scrollbar-track";

    /** ::-webkit-scrollbar-thumb - the draggable handle */
    public static final String scrollbarThumb = "::-webkit-scrollbar-thumb";

    /** ::-webkit-scrollbar-button - up/down buttons */
    public static final String scrollbarButton = "::-webkit-scrollbar-button";

    /** ::-webkit-scrollbar-corner - corner where scrollbars meet */
    public static final String scrollbarCorner = "::-webkit-scrollbar-corner";

    /**
     * Creates a scrollbar selector for a specific element.
     * Example: scrollbar(".content") -> .content::-webkit-scrollbar
     */
    public static String scrollbar(String element) {
        return element + "::-webkit-scrollbar";
    }

    /**
     * Creates a scrollbar track selector for a specific element.
     * Example: scrollbarTrack(".content") -> .content::-webkit-scrollbar-track
     */
    public static String scrollbarTrack(String element) {
        return element + "::-webkit-scrollbar-track";
    }

    /**
     * Creates a scrollbar thumb selector for a specific element.
     * Example: scrollbarThumb(".content") -> .content::-webkit-scrollbar-thumb
     */
    public static String scrollbarThumb(String element) {
        return element + "::-webkit-scrollbar-thumb";
    }

    /**
     * Creates a scrollbar thumb hover selector for a specific element.
     * Example: scrollbarThumbHover(".content") -> .content::-webkit-scrollbar-thumb:hover
     */
    public static String scrollbarThumbHover(String element) {
        return element + "::-webkit-scrollbar-thumb:hover";
    }

    // ==================== Attribute Selectors ====================

    /**
     * Attribute exists selector.
     *
     * Example: attr("disabled") -> [disabled]
     */
    public static String attr(String name) {
        return "[" + name + "]";
    }

    /**
     * Attribute equals selector.
     *
     * Example: attrEquals("type", "button") -> [type="button"]
     */
    public static String attrEquals(String name, String value) {
        return "[" + name + "=\"" + value + "\"]";
    }

    /**
     * Attribute contains selector.
     *
     * Example: attrContains("class", "btn") -> [class*="btn"]
     */
    public static String attrContains(String name, String value) {
        return "[" + name + "*=\"" + value + "\"]";
    }

    /**
     * Attribute starts with selector.
     *
     * Example: attrStartsWith("href", "https") -> [href^="https"]
     */
    public static String attrStartsWith(String name, String value) {
        return "[" + name + "^=\"" + value + "\"]";
    }

    /**
     * Attribute ends with selector.
     *
     * Example: attrEndsWith("href", ".pdf") -> [href$=".pdf"]
     */
    public static String attrEndsWith(String name, String value) {
        return "[" + name + "$=\"" + value + "\"]";
    }

    /**
     * Attribute contains word selector (space-separated).
     *
     * Example: attrContainsWord("class", "active") -> [class~="active"]
     */
    public static String attrContainsWord(String name, String value) {
        return "[" + name + "~=\"" + value + "\"]";
    }

    /**
     * Attribute starts with prefix selector (hyphen-separated).
     *
     * Example: attrStartsWithPrefix("lang", "en") -> [lang|="en"]
     */
    public static String attrStartsWithPrefix(String name, String value) {
        return "[" + name + "|=\"" + value + "\"]";
    }

    // ==================== Combinator Helpers ====================

    /**
     * Descendant combinator (space).
     *
     * Example: descendant(".parent", ".child") -> .parent .child
     */
    public static String descendant(String parent, String child) {
        return parent + " " + child;
    }

    /**
     * Direct child combinator (>).
     *
     * Example: child(".parent", ".child") -> .parent > .child
     */
    public static String child(String parent, String child) {
        return parent + " > " + child;
    }

    /**
     * Adjacent sibling combinator (+).
     *
     * Example: adjacent("h1", "p") -> h1 + p
     */
    public static String adjacent(String first, String second) {
        return first + " + " + second;
    }

    /**
     * General sibling combinator (~).
     *
     * Example: sibling("h1", "p") -> h1 ~ p
     */
    public static String sibling(String first, String second) {
        return first + " ~ " + second;
    }
}
