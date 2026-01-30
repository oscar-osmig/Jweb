package com.osmig.Jweb.framework.styles;

/**
 * CSS Text Wrapping DSL for modern text layout control.
 *
 * <p>Provides modern text wrapping properties including text-wrap: balance
 * (for balanced headlines), text-wrap: pretty (for improved paragraph
 * wrapping), and white-space-collapse control.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSTextWrap.*;
 *
 * // Balanced headlines
 * rule("h1, h2, h3")
 *     .prop(textWrapBalance())
 *
 * // Pretty paragraph wrapping
 * rule("p")
 *     .prop(textWrapPretty())
 *
 * // Prevent wrapping
 * rule(".nowrap")
 *     .prop(textWrapNowrap())
 *
 * // Word break control
 * rule(".break-all")
 *     .prop(wordBreakAll())
 *
 * // White space collapse
 * rule("pre")
 *     .prop(whiteSpaceCollapse("preserve"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public final class CSSTextWrap {

    private CSSTextWrap() {}

    // ==================== text-wrap ====================

    /**
     * Creates a text-wrap property.
     *
     * @param value "wrap", "nowrap", "balance", "pretty", "stable"
     * @return the property string
     */
    public static String textWrap(String value) {
        return "text-wrap:" + value;
    }

    /** text-wrap: balance - Balances text across lines for headings. */
    public static String textWrapBalance() {
        return "text-wrap:balance";
    }

    /** text-wrap: pretty - Improves wrapping for better typography. */
    public static String textWrapPretty() {
        return "text-wrap:pretty";
    }

    /** text-wrap: stable - Keeps text stable during editing. */
    public static String textWrapStable() {
        return "text-wrap:stable";
    }

    /** text-wrap: nowrap - Prevents text wrapping. */
    public static String textWrapNowrap() {
        return "text-wrap:nowrap";
    }

    /** text-wrap: wrap - Default wrapping behavior. */
    public static String textWrapWrap() {
        return "text-wrap:wrap";
    }

    // ==================== white-space-collapse ====================

    /**
     * Creates a white-space-collapse property.
     *
     * @param value "collapse", "preserve", "preserve-breaks", "preserve-spaces", "break-spaces"
     * @return the property string
     */
    public static String whiteSpaceCollapse(String value) {
        return "white-space-collapse:" + value;
    }

    /** Collapses whitespace (default). */
    public static String whiteSpaceCollapse() {
        return "white-space-collapse:collapse";
    }

    /** Preserves all whitespace. */
    public static String whiteSpacePreserve() {
        return "white-space-collapse:preserve";
    }

    /** Preserves line breaks but collapses spaces. */
    public static String whiteSpacePreserveBreaks() {
        return "white-space-collapse:preserve-breaks";
    }

    /** Preserves spaces but collapses line breaks. */
    public static String whiteSpacePreserveSpaces() {
        return "white-space-collapse:preserve-spaces";
    }

    /** Preserves whitespace and allows break opportunities. */
    public static String whiteSpaceBreakSpaces() {
        return "white-space-collapse:break-spaces";
    }

    // ==================== word-break ====================

    /**
     * Creates a word-break property.
     *
     * @param value "normal", "break-all", "keep-all", "break-word", "auto-phrase"
     * @return the property string
     */
    public static String wordBreak(String value) {
        return "word-break:" + value;
    }

    /** Normal word breaking. */
    public static String wordBreakNormal() {
        return "word-break:normal";
    }

    /** Breaks anywhere between characters. */
    public static String wordBreakAll() {
        return "word-break:break-all";
    }

    /** Keeps words together (useful for CJK text). */
    public static String wordBreakKeepAll() {
        return "word-break:keep-all";
    }

    /** Auto-phrase: breaks at natural phrase boundaries (experimental). */
    public static String wordBreakAutoPhrase() {
        return "word-break:auto-phrase";
    }

    // ==================== overflow-wrap ====================

    /**
     * Creates an overflow-wrap property.
     *
     * @param value "normal", "break-word", "anywhere"
     * @return the property string
     */
    public static String overflowWrap(String value) {
        return "overflow-wrap:" + value;
    }

    /** Allows breaking within words to prevent overflow. */
    public static String overflowWrapBreakWord() {
        return "overflow-wrap:break-word";
    }

    /** Allows breaking anywhere to prevent overflow. */
    public static String overflowWrapAnywhere() {
        return "overflow-wrap:anywhere";
    }

    // ==================== hyphens ====================

    /**
     * Creates a hyphens property.
     *
     * @param value "none", "manual", "auto"
     * @return the property string
     */
    public static String hyphens(String value) {
        return "hyphens:" + value;
    }

    /** Enables automatic hyphenation. */
    public static String hyphensAuto() {
        return "hyphens:auto";
    }

    /** Disables hyphenation. */
    public static String hyphensNone() {
        return "hyphens:none";
    }

    /** Manual hyphenation only (at &shy; marks). */
    public static String hyphensManual() {
        return "hyphens:manual";
    }

    // ==================== line-clamp ====================

    /**
     * Creates a line-clamp effect (truncates text with ellipsis after N lines).
     * Uses the -webkit-line-clamp approach with display: -webkit-box.
     *
     * @param lines the maximum number of lines
     * @return the property string (multiple declarations)
     */
    public static String lineClamp(int lines) {
        return "display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:" + lines + ";overflow:hidden";
    }

    // ==================== text-overflow ====================

    /**
     * Creates a text-overflow property.
     *
     * @param value "clip", "ellipsis", or a custom string
     * @return the property string
     */
    public static String textOverflow(String value) {
        return "text-overflow:" + value;
    }

    /** Truncates text with ellipsis. */
    public static String textOverflowEllipsis() {
        return "text-overflow:ellipsis";
    }

    /** Clips overflowing text. */
    public static String textOverflowClip() {
        return "text-overflow:clip";
    }
}
