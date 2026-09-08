package com.osmig.Jweb.framework.styles;

import jweb.CSSValue;
import jweb.Style;

/**
 * CSS Text Wrapping DSL for modern text layout control.
 *
 * <p>Provides modern text wrapping properties including text-wrap: balance
 * (for balanced headlines), text-wrap: pretty (for improved paragraph
 * wrapping), and white-space-collapse control.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static jweb.Css.*;
 *
 * // Balanced headlines
 * rule("h1, h2, h3")
 *     .apply(textWrapBalance())
 *
 * // Pretty paragraph wrapping
 * rule("p")
 *     .apply(textWrapPretty())
 *
 * // Prevent wrapping
 * rule(".nowrap")
 *     .apply(textWrapNowrap())
 *
 * // Word break control
 * rule(".break-all")
 *     .apply(wordBreakAll())
 *
 * // White space collapse
 * rule("pre")
 *     .apply(whiteSpaceCollapse("preserve"))
 * }</pre>
 *
 * @see CSS for creating style rules
 */
public class CSSTextWrap extends CSSAnchorPositioning {

    protected CSSTextWrap() {}

    // ==================== text-wrap ====================

    /**
     * Creates a text-wrap declaration.
     *
     * @param value "wrap", "nowrap", "balance", "pretty", "stable"
     * @return a {@code Style} holding {@code text-wrap: value}
     */
    public static Style<?> textWrap(String value) {
        return CSS.style().prop("text-wrap", value);
    }

    /**
     * Creates a text-wrap declaration from a {@code CSSValue}.
     *
     * @param value the text-wrap value
     * @return a {@code Style} holding {@code text-wrap: value}
     */
    public static Style<?> textWrap(CSSValue value) {
        return textWrap(value.css());
    }

    /** text-wrap: balance - Balances text across lines for headings. */
    public static Style<?> textWrapBalance() {
        return textWrap("balance");
    }

    /** text-wrap: pretty - Improves wrapping for better typography. */
    public static Style<?> textWrapPretty() {
        return textWrap("pretty");
    }

    /** text-wrap: stable - Keeps text stable during editing. */
    public static Style<?> textWrapStable() {
        return textWrap("stable");
    }

    /** text-wrap: nowrap - Prevents text wrapping. */
    public static Style<?> textWrapNowrap() {
        return textWrap("nowrap");
    }

    /** text-wrap: wrap - Default wrapping behavior. */
    public static Style<?> textWrapWrap() {
        return textWrap("wrap");
    }

    // ==================== white-space-collapse ====================

    /**
     * Creates a white-space-collapse declaration.
     *
     * @param value "collapse", "preserve", "preserve-breaks", "preserve-spaces", "break-spaces"
     * @return a {@code Style} holding {@code white-space-collapse: value}
     */
    public static Style<?> whiteSpaceCollapse(String value) {
        return CSS.style().prop("white-space-collapse", value);
    }

    /**
     * Creates a white-space-collapse declaration from a {@code CSSValue}.
     *
     * @param value the white-space-collapse value
     * @return a {@code Style} holding {@code white-space-collapse: value}
     */
    public static Style<?> whiteSpaceCollapse(CSSValue value) {
        return whiteSpaceCollapse(value.css());
    }

    /** Collapses whitespace (default). */
    public static Style<?> whiteSpaceCollapse() {
        return whiteSpaceCollapse("collapse");
    }

    /** Preserves all whitespace. */
    public static Style<?> whiteSpacePreserve() {
        return whiteSpaceCollapse("preserve");
    }

    /** Preserves line breaks but collapses spaces. */
    public static Style<?> whiteSpacePreserveBreaks() {
        return whiteSpaceCollapse("preserve-breaks");
    }

    /** Preserves spaces but collapses line breaks. */
    public static Style<?> whiteSpacePreserveSpaces() {
        return whiteSpaceCollapse("preserve-spaces");
    }

    /** Preserves whitespace and allows break opportunities. */
    public static Style<?> whiteSpaceBreakSpaces() {
        return whiteSpaceCollapse("break-spaces");
    }

    // ==================== word-break ====================

    /**
     * Creates a word-break declaration.
     *
     * @param value "normal", "break-all", "keep-all", "break-word", "auto-phrase"
     * @return a {@code Style} holding {@code word-break: value}
     */
    public static Style<?> wordBreak(String value) {
        return CSS.style().prop("word-break", value);
    }

    /**
     * Creates a word-break declaration from a {@code CSSValue}.
     *
     * @param value the word-break value
     * @return a {@code Style} holding {@code word-break: value}
     */
    public static Style<?> wordBreak(CSSValue value) {
        return wordBreak(value.css());
    }

    /** Normal word breaking. */
    public static Style<?> wordBreakNormal() {
        return wordBreak("normal");
    }

    /** Breaks anywhere between characters. */
    public static Style<?> wordBreakAll() {
        return wordBreak("break-all");
    }

    /** Keeps words together (useful for CJK text). */
    public static Style<?> wordBreakKeepAll() {
        return wordBreak("keep-all");
    }

    /** Auto-phrase: breaks at natural phrase boundaries (experimental). */
    public static Style<?> wordBreakAutoPhrase() {
        return wordBreak("auto-phrase");
    }

    // ==================== overflow-wrap ====================

    /**
     * Creates an overflow-wrap declaration.
     *
     * @param value "normal", "break-word", "anywhere"
     * @return a {@code Style} holding {@code overflow-wrap: value}
     */
    public static Style<?> overflowWrap(String value) {
        return CSS.style().prop("overflow-wrap", value);
    }

    /**
     * Creates an overflow-wrap declaration from a {@code CSSValue}.
     *
     * @param value the overflow-wrap value
     * @return a {@code Style} holding {@code overflow-wrap: value}
     */
    public static Style<?> overflowWrap(CSSValue value) {
        return overflowWrap(value.css());
    }

    /** Allows breaking within words to prevent overflow. */
    public static Style<?> overflowWrapBreakWord() {
        return overflowWrap("break-word");
    }

    /** Allows breaking anywhere to prevent overflow. */
    public static Style<?> overflowWrapAnywhere() {
        return overflowWrap("anywhere");
    }

    // ==================== hyphens ====================

    /**
     * Creates a hyphens declaration.
     *
     * @param value "none", "manual", "auto"
     * @return a {@code Style} holding {@code hyphens: value}
     */
    public static Style<?> hyphens(String value) {
        return CSS.style().prop("hyphens", value);
    }

    /**
     * Creates a hyphens declaration from a {@code CSSValue}.
     *
     * @param value the hyphens value
     * @return a {@code Style} holding {@code hyphens: value}
     */
    public static Style<?> hyphens(CSSValue value) {
        return hyphens(value.css());
    }

    /** Enables automatic hyphenation. */
    public static Style<?> hyphensAuto() {
        return hyphens("auto");
    }

    /** Disables hyphenation. */
    public static Style<?> hyphensNone() {
        return hyphens("none");
    }

    /** Manual hyphenation only (at &shy; marks). */
    public static Style<?> hyphensManual() {
        return hyphens("manual");
    }

    // ==================== line-clamp ====================

    /**
     * Creates a line-clamp effect (truncates text with ellipsis after N lines).
     * Uses the -webkit-line-clamp approach with display: -webkit-box.
     *
     * @param lines the maximum number of lines
     * @return a {@code Style} holding the multi-declaration line-clamp fallback
     */
    public static Style<?> lineClamp(int lines) {
        return CSS.style()
            .prop("display", "-webkit-box")
            .prop("-webkit-box-orient", "vertical")
            .prop("-webkit-line-clamp", String.valueOf(lines))
            .prop("overflow", "hidden");
    }

    // ==================== text-overflow ====================

    /**
     * Creates a text-overflow declaration.
     *
     * @param value "clip", "ellipsis", or a custom string
     * @return a {@code Style} holding {@code text-overflow: value}
     */
    public static Style<?> textOverflow(String value) {
        return CSS.style().prop("text-overflow", value);
    }

    /**
     * Creates a text-overflow declaration from a {@code CSSValue}.
     *
     * @param value the text-overflow value
     * @return a {@code Style} holding {@code text-overflow: value}
     */
    public static Style<?> textOverflow(CSSValue value) {
        return textOverflow(value.css());
    }

    /** Truncates text with ellipsis. */
    public static Style<?> textOverflowEllipsis() {
        return textOverflow("ellipsis");
    }

    /** Clips overflowing text. */
    public static Style<?> textOverflowClip() {
        return textOverflow("clip");
    }
}
