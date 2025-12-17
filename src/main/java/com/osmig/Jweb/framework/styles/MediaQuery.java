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
    private final Map<String, Style> rules = new LinkedHashMap<>();

    private MediaQuery() {}

    public static MediaQuery media() {
        return new MediaQuery();
    }

    // ==================== Media Types ====================

    public MediaQuery screen() {
        conditions.add("screen");
        return this;
    }

    public MediaQuery print() {
        conditions.add("print");
        return this;
    }

    public MediaQuery all() {
        conditions.add("all");
        return this;
    }

    // ==================== Width Conditions ====================

    public MediaQuery minWidth(CSSValue value) {
        conditions.add("(min-width: " + value.css() + ")");
        return this;
    }

    public MediaQuery maxWidth(CSSValue value) {
        conditions.add("(max-width: " + value.css() + ")");
        return this;
    }

    public MediaQuery width(CSSValue value) {
        conditions.add("(width: " + value.css() + ")");
        return this;
    }

    // ==================== Height Conditions ====================

    public MediaQuery minHeight(CSSValue value) {
        conditions.add("(min-height: " + value.css() + ")");
        return this;
    }

    public MediaQuery maxHeight(CSSValue value) {
        conditions.add("(max-height: " + value.css() + ")");
        return this;
    }

    public MediaQuery height(CSSValue value) {
        conditions.add("(height: " + value.css() + ")");
        return this;
    }

    // ==================== Orientation ====================

    public MediaQuery portrait() {
        conditions.add("(orientation: portrait)");
        return this;
    }

    public MediaQuery landscape() {
        conditions.add("(orientation: landscape)");
        return this;
    }

    // ==================== Display Features ====================

    public MediaQuery prefersColorScheme(String scheme) {
        conditions.add("(prefers-color-scheme: " + scheme + ")");
        return this;
    }

    public MediaQuery prefersDark() {
        return prefersColorScheme("dark");
    }

    public MediaQuery prefersLight() {
        return prefersColorScheme("light");
    }

    public MediaQuery prefersReducedMotion() {
        conditions.add("(prefers-reduced-motion: reduce)");
        return this;
    }

    public MediaQuery prefersReducedTransparency() {
        conditions.add("(prefers-reduced-transparency: reduce)");
        return this;
    }

    public MediaQuery prefersContrast(String value) {
        conditions.add("(prefers-contrast: " + value + ")");
        return this;
    }

    // ==================== Resolution ====================

    public MediaQuery minResolution(CSSValue value) {
        conditions.add("(min-resolution: " + value.css() + ")");
        return this;
    }

    public MediaQuery maxResolution(CSSValue value) {
        conditions.add("(max-resolution: " + value.css() + ")");
        return this;
    }

    public MediaQuery retina() {
        conditions.add("(-webkit-min-device-pixel-ratio: 2)");
        return this;
    }

    // ==================== Display Mode ====================

    public MediaQuery displayMode(String mode) {
        conditions.add("(display-mode: " + mode + ")");
        return this;
    }

    public MediaQuery fullscreen() {
        return displayMode("fullscreen");
    }

    public MediaQuery standalone() {
        return displayMode("standalone");
    }

    // ==================== Hover & Pointer ====================

    public MediaQuery hover() {
        conditions.add("(hover: hover)");
        return this;
    }

    public MediaQuery noHover() {
        conditions.add("(hover: none)");
        return this;
    }

    public MediaQuery pointer(String value) {
        conditions.add("(pointer: " + value + ")");
        return this;
    }

    public MediaQuery finePointer() {
        return pointer("fine");
    }

    public MediaQuery coarsePointer() {
        return pointer("coarse");
    }

    // ==================== Logical Operators ====================

    public MediaQuery and() {
        // 'and' is implicit when chaining conditions
        return this;
    }

    public MediaQuery not() {
        if (!conditions.isEmpty()) {
            int lastIndex = conditions.size() - 1;
            conditions.set(lastIndex, "not " + conditions.get(lastIndex));
        }
        return this;
    }

    public MediaQuery only() {
        if (!conditions.isEmpty()) {
            conditions.set(0, "only " + conditions.get(0));
        }
        return this;
    }

    // ==================== Custom Condition ====================

    public MediaQuery condition(String condition) {
        conditions.add(condition);
        return this;
    }

    // ==================== Rules ====================

    public MediaQuery rule(String selector, Style style) {
        rules.put(selector, style);
        return this;
    }

    public MediaQuery rules(Rule... ruleArray) {
        for (Rule r : ruleArray) {
            rules.put(r.selector(), r.style());
        }
        return this;
    }

    // ==================== Build ====================

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@media ");

        // Join conditions with 'and'
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append(" and ");
            sb.append(conditions.get(i));
        }

        sb.append(" {\n");

        for (Map.Entry<String, Style> entry : rules.entrySet()) {
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

    // Record for holding selector-style pairs
    public record Rule(String selector, Style style) {}

    // ==================== Common Breakpoints ====================

    public static MediaQuery xs() { return media().maxWidth(CSSUnits.px(575)); }
    public static MediaQuery sm() { return media().minWidth(CSSUnits.px(576)); }
    public static MediaQuery md() { return media().minWidth(CSSUnits.px(768)); }
    public static MediaQuery lg() { return media().minWidth(CSSUnits.px(992)); }
    public static MediaQuery xl() { return media().minWidth(CSSUnits.px(1200)); }
    public static MediaQuery xxl() { return media().minWidth(CSSUnits.px(1400)); }

    // Mobile-first breakpoints (max-width)
    public static MediaQuery mobile() { return media().maxWidth(CSSUnits.px(767)); }
    public static MediaQuery tablet() { return media().minWidth(CSSUnits.px(768)).maxWidth(CSSUnits.px(1023)); }
    public static MediaQuery desktop() { return media().minWidth(CSSUnits.px(1024)); }
}
