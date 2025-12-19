package com.osmig.Jweb.framework.styles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for CSS media queries.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.MediaQuery.*;
 *
 *   // Simple breakpoint
 *   media().minWidth(px(768)).rules(
 *       rule(".container", style().maxWidth(px(720)))
 *   )
 *
 *   // Combined conditions
 *   media().screen().minWidth(px(1024)).maxWidth(px(1280)).rules(
 *       rule(".sidebar", style().display(none))
 *   )
 *
 *   // Print styles
 *   media().print().rules(
 *       rule("body", style().fontSize(pt(12)))
 *   )
 */
public class MediaQuery {

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Style<?>> rules = new LinkedHashMap<>();

    private MediaQuery() {}

    /**
     * Creates a new media query builder.
     *
     * <p>Example:</p>
     * <pre>
     * media().minWidth(px(768)).rule(".container", new Style&lt;&gt;().maxWidth(px(720)))
     * </pre>
     *
     * @return a new MediaQuery builder instance
     */
    public static MediaQuery media() {
        return new MediaQuery();
    }

    // ==================== Media Types ====================

    /**
     * Targets screen devices (computers, tablets, phones).
     *
     * <p>Example:</p>
     * <pre>
     * media().screen().minWidth(px(768))
     * // Output: @media screen and (min-width: 768px)
     * </pre>
     *
     * @return this builder for chaining
     */
    public MediaQuery screen() {
        conditions.add("screen");
        return this;
    }

    /**
     * Targets print preview/printed pages.
     *
     * <p>Example:</p>
     * <pre>
     * media().print().rule("body", new Style&lt;&gt;().fontSize(pt(12)))
     * // Output: @media print { body { font-size: 12pt; } }
     * </pre>
     *
     * @return this builder for chaining
     */
    public MediaQuery print() {
        conditions.add("print");
        return this;
    }

    /**
     * Targets all media types.
     *
     * @return this builder for chaining
     */
    public MediaQuery all() {
        conditions.add("all");
        return this;
    }

    // ==================== Width Conditions ====================

    /**
     * Sets minimum viewport width condition.
     *
     * <p>Example:</p>
     * <pre>
     * media().minWidth(px(768))
     * // Output: @media (min-width: 768px)
     * </pre>
     *
     * @param value the minimum width value (e.g., px(768), rem(48))
     * @return this builder for chaining
     */
    public MediaQuery minWidth(CSSValue value) {
        conditions.add("(min-width: " + value.css() + ")");
        return this;
    }

    /**
     * Sets maximum viewport width condition.
     *
     * <p>Example:</p>
     * <pre>
     * media().maxWidth(px(767))
     * // Output: @media (max-width: 767px)
     * </pre>
     *
     * @param value the maximum width value
     * @return this builder for chaining
     */
    public MediaQuery maxWidth(CSSValue value) {
        conditions.add("(max-width: " + value.css() + ")");
        return this;
    }

    /**
     * Sets exact viewport width condition.
     *
     * @param value the exact width value
     * @return this builder for chaining
     */
    public MediaQuery width(CSSValue value) {
        conditions.add("(width: " + value.css() + ")");
        return this;
    }

    // ==================== Height Conditions ====================

    /**
     * Sets minimum viewport height condition.
     *
     * @param value the minimum height value
     * @return this builder for chaining
     */
    public MediaQuery minHeight(CSSValue value) {
        conditions.add("(min-height: " + value.css() + ")");
        return this;
    }

    /**
     * Sets maximum viewport height condition.
     *
     * @param value the maximum height value
     * @return this builder for chaining
     */
    public MediaQuery maxHeight(CSSValue value) {
        conditions.add("(max-height: " + value.css() + ")");
        return this;
    }

    /**
     * Sets exact viewport height condition.
     *
     * @param value the exact height value
     * @return this builder for chaining
     */
    public MediaQuery height(CSSValue value) {
        conditions.add("(height: " + value.css() + ")");
        return this;
    }

    // ==================== Orientation ====================

    /**
     * Targets portrait orientation (height &gt; width).
     *
     * <p>Example:</p>
     * <pre>
     * media().portrait().rule(".sidebar", new Style&lt;&gt;().display(none))
     * </pre>
     *
     * @return this builder for chaining
     */
    public MediaQuery portrait() {
        conditions.add("(orientation: portrait)");
        return this;
    }

    /**
     * Targets landscape orientation (width &gt; height).
     *
     * @return this builder for chaining
     */
    public MediaQuery landscape() {
        conditions.add("(orientation: landscape)");
        return this;
    }

    // ==================== Display Features ====================

    /**
     * Targets user's preferred color scheme.
     *
     * @param scheme the color scheme ("light" or "dark")
     * @return this builder for chaining
     * @see #prefersDark()
     * @see #prefersLight()
     */
    public MediaQuery prefersColorScheme(String scheme) {
        conditions.add("(prefers-color-scheme: " + scheme + ")");
        return this;
    }

    /**
     * Targets users who prefer dark color scheme.
     *
     * <p>Example:</p>
     * <pre>
     * media().prefersDark()
     *     .rule("body", new Style&lt;&gt;().backgroundColor(hex("#1a1a1a")).color(hex("#fff")))
     * </pre>
     *
     * @return this builder for chaining
     */
    public MediaQuery prefersDark() {
        return prefersColorScheme("dark");
    }

    /**
     * Targets users who prefer light color scheme.
     *
     * @return this builder for chaining
     */
    public MediaQuery prefersLight() {
        return prefersColorScheme("light");
    }

    /**
     * Targets users who prefer reduced motion (accessibility).
     *
     * <p>Example:</p>
     * <pre>
     * media().prefersReducedMotion()
     *     .rule("*", new Style&lt;&gt;().animationDuration(ms(0)).transitionDuration(ms(0)))
     * </pre>
     *
     * @return this builder for chaining
     */
    public MediaQuery prefersReducedMotion() {
        conditions.add("(prefers-reduced-motion: reduce)");
        return this;
    }

    /**
     * Targets users who prefer reduced transparency.
     *
     * @return this builder for chaining
     */
    public MediaQuery prefersReducedTransparency() {
        conditions.add("(prefers-reduced-transparency: reduce)");
        return this;
    }

    /**
     * Targets user's preferred contrast level.
     *
     * @param value the contrast preference ("more", "less", "custom")
     * @return this builder for chaining
     */
    public MediaQuery prefersContrast(String value) {
        conditions.add("(prefers-contrast: " + value + ")");
        return this;
    }

    // ==================== Resolution ====================

    /**
     * Sets minimum screen resolution condition.
     *
     * @param value the minimum resolution (e.g., dpi, dppx)
     * @return this builder for chaining
     */
    public MediaQuery minResolution(CSSValue value) {
        conditions.add("(min-resolution: " + value.css() + ")");
        return this;
    }

    /**
     * Sets maximum screen resolution condition.
     *
     * @param value the maximum resolution
     * @return this builder for chaining
     */
    public MediaQuery maxResolution(CSSValue value) {
        conditions.add("(max-resolution: " + value.css() + ")");
        return this;
    }

    /**
     * Targets high-DPI (Retina) displays (2x pixel density).
     *
     * @return this builder for chaining
     */
    public MediaQuery retina() {
        conditions.add("(-webkit-min-device-pixel-ratio: 2)");
        return this;
    }

    // ==================== Display Mode ====================

    /**
     * Targets specific display mode (for PWAs).
     *
     * @param mode the display mode ("fullscreen", "standalone", "minimal-ui", "browser")
     * @return this builder for chaining
     */
    public MediaQuery displayMode(String mode) {
        conditions.add("(display-mode: " + mode + ")");
        return this;
    }

    /**
     * Targets fullscreen display mode.
     *
     * @return this builder for chaining
     */
    public MediaQuery fullscreen() {
        return displayMode("fullscreen");
    }

    /**
     * Targets standalone display mode (PWA without browser UI).
     *
     * @return this builder for chaining
     */
    public MediaQuery standalone() {
        return displayMode("standalone");
    }

    // ==================== Hover & Pointer ====================

    /**
     * Targets devices with hover capability (mouse, trackpad).
     *
     * @return this builder for chaining
     */
    public MediaQuery hover() {
        conditions.add("(hover: hover)");
        return this;
    }

    /**
     * Targets devices without hover capability (touch screens).
     *
     * @return this builder for chaining
     */
    public MediaQuery noHover() {
        conditions.add("(hover: none)");
        return this;
    }

    /**
     * Targets devices by pointer accuracy.
     *
     * @param value the pointer type ("fine", "coarse", "none")
     * @return this builder for chaining
     */
    public MediaQuery pointer(String value) {
        conditions.add("(pointer: " + value + ")");
        return this;
    }

    /**
     * Targets devices with fine pointer (mouse, stylus).
     *
     * @return this builder for chaining
     */
    public MediaQuery finePointer() {
        return pointer("fine");
    }

    /**
     * Targets devices with coarse pointer (touch, motion sensors).
     *
     * @return this builder for chaining
     */
    public MediaQuery coarsePointer() {
        return pointer("coarse");
    }

    // ==================== Logical Operators ====================

    /**
     * Explicit AND operator (usually implicit when chaining).
     *
     * @return this builder for chaining
     */
    public MediaQuery and() {
        // 'and' is implicit when chaining conditions
        return this;
    }

    /**
     * Negates the last condition.
     *
     * @return this builder for chaining
     */
    public MediaQuery not() {
        if (!conditions.isEmpty()) {
            int lastIndex = conditions.size() - 1;
            conditions.set(lastIndex, "not " + conditions.get(lastIndex));
        }
        return this;
    }

    /**
     * Adds "only" prefix to prevent older browsers from applying styles.
     *
     * @return this builder for chaining
     */
    public MediaQuery only() {
        if (!conditions.isEmpty()) {
            conditions.set(0, "only " + conditions.get(0));
        }
        return this;
    }

    // ==================== Custom Condition ====================

    /**
     * Adds a custom media condition string.
     *
     * <p>Example:</p>
     * <pre>
     * media().condition("(aspect-ratio: 16/9)")
     * </pre>
     *
     * @param condition the raw CSS media condition
     * @return this builder for chaining
     */
    public MediaQuery condition(String condition) {
        conditions.add(condition);
        return this;
    }

    // ==================== Rules ====================

    /**
     * Adds a CSS rule to this media query.
     *
     * <p>Example:</p>
     * <pre>
     * media().minWidth(px(768))
     *     .rule(".container", new Style&lt;&gt;().maxWidth(px(720)))
     *     .rule(".sidebar", new Style&lt;&gt;().display(block))
     * </pre>
     *
     * @param selector the CSS selector (e.g., ".container", "h1")
     * @param style the Style object containing CSS properties
     * @return this builder for chaining
     */
    public MediaQuery rule(String selector, Style<?> style) {
        rules.put(selector, style);
        return this;
    }

    /**
     * Adds multiple CSS rules to this media query.
     *
     * @param ruleArray array of Rule records
     * @return this builder for chaining
     */
    public MediaQuery rules(Rule... ruleArray) {
        for (Rule r : ruleArray) {
            rules.put(r.selector(), r.style());
        }
        return this;
    }

    // ==================== Build ====================

    /**
     * Builds the complete CSS media query string.
     *
     * @return the formatted CSS @media rule
     */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@media ");

        // Join conditions with 'and'
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append(" and ");
            sb.append(conditions.get(i));
        }

        sb.append(" {\n");

        for (Map.Entry<String, Style<?>> entry : rules.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" {\n");
            for (Map.Entry<String, String> prop : entry.getValue().toMap().entrySet()) {
                sb.append("    ").append(prop.getKey()).append(": ").append(prop.getValue()).append(";\n");
            }
            sb.append("  }\n");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    /**
     * Record for holding a CSS selector and its associated styles.
     *
     * @param selector the CSS selector
     * @param style the Style object with CSS properties
     */
    public record Rule(String selector, Style<?> style) {}

    // ==================== Common Breakpoints ====================

    /**
     * Extra small screens (max-width: 575px).
     * Use for mobile-first "down" queries.
     *
     * @return MediaQuery for xs breakpoint
     */
    public static MediaQuery xs() { return media().maxWidth(CSSUnits.px(575)); }

    /**
     * Small screens and up (min-width: 576px).
     * Typically targets larger phones in landscape.
     *
     * @return MediaQuery for sm breakpoint
     */
    public static MediaQuery sm() { return media().minWidth(CSSUnits.px(576)); }

    /**
     * Medium screens and up (min-width: 768px).
     * Typically targets tablets.
     *
     * <p>Example:</p>
     * <pre>
     * md().rule(".container", new Style&lt;&gt;().padding(SPACE_MD, SPACE_LG))
     * </pre>
     *
     * @return MediaQuery for md breakpoint
     */
    public static MediaQuery md() { return media().minWidth(CSSUnits.px(768)); }

    /**
     * Large screens and up (min-width: 992px).
     * Typically targets desktops.
     *
     * <p>Example:</p>
     * <pre>
     * lg().rule(".container", new Style&lt;&gt;().maxWidth(px(960)))
     * </pre>
     *
     * @return MediaQuery for lg breakpoint
     */
    public static MediaQuery lg() { return media().minWidth(CSSUnits.px(992)); }

    /**
     * Extra large screens and up (min-width: 1200px).
     * Typically targets large desktops.
     *
     * @return MediaQuery for xl breakpoint
     */
    public static MediaQuery xl() { return media().minWidth(CSSUnits.px(1200)); }

    /**
     * Extra extra large screens and up (min-width: 1400px).
     * Typically targets very large displays.
     *
     * @return MediaQuery for xxl breakpoint
     */
    public static MediaQuery xxl() { return media().minWidth(CSSUnits.px(1400)); }

    /**
     * Mobile devices only (max-width: 767px).
     * Use for mobile-specific styles.
     *
     * <p>Example:</p>
     * <pre>
     * mobile()
     *     .rule(".navbar", new Style&lt;&gt;().flexDirection(column))
     *     .rule("h1", new Style&lt;&gt;().fontSize(FONT_2XL))
     * </pre>
     *
     * @return MediaQuery for mobile screens
     */
    public static MediaQuery mobile() { return media().maxWidth(CSSUnits.px(767)); }

    /**
     * Tablet devices only (768px - 1023px).
     * Use for tablet-specific styles.
     *
     * @return MediaQuery for tablet screens
     */
    public static MediaQuery tablet() { return media().minWidth(CSSUnits.px(768)).maxWidth(CSSUnits.px(1023)); }

    /**
     * Desktop devices and up (min-width: 1024px).
     * Use for desktop-specific styles.
     *
     * @return MediaQuery for desktop screens
     */
    public static MediaQuery desktop() { return media().minWidth(CSSUnits.px(1024)); }
}
