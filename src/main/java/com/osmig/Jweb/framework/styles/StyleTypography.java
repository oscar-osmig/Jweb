package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS typography properties.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StyleTypography<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Color ====================

    default T color(CSSValue value) { return prop("color", value); }

    // ==================== Font ====================

    default T fontFamily(String value) { return prop("font-family", () -> value); }
    default T fontSize(CSSValue value) { return prop("font-size", value); }
    default T fontWeight(CSSValue value) { return prop("font-weight", value); }
    default T fontWeight(int value) { return prop("font-weight", () -> String.valueOf(value)); }
    default T fontStyle(CSSValue value) { return prop("font-style", value); }
    default T fontVariant(CSSValue value) { return prop("font-variant", value); }
    default T fontStretch(CSSValue value) { return prop("font-stretch", value); }

    // ==================== Line & Letter Spacing ====================

    default T lineHeight(CSSValue value) { return prop("line-height", value); }
    default T lineHeight(double value) { return prop("line-height", () -> String.valueOf(value)); }
    default T letterSpacing(CSSValue value) { return prop("letter-spacing", value); }
    default T wordSpacing(CSSValue value) { return prop("word-spacing", value); }

    // ==================== Text Alignment & Decoration ====================

    default T textAlign(CSSValue value) { return prop("text-align", value); }
    default T textDecoration(CSSValue value) { return prop("text-decoration", value); }
    default T textTransform(CSSValue value) { return prop("text-transform", value); }
    default T textIndent(CSSValue value) { return prop("text-indent", value); }
    default T textShadow(String value) { return prop("text-shadow", () -> value); }
    default T textOverflow(CSSValue value) { return prop("text-overflow", value); }

    // ==================== White Space & Wrapping ====================

    default T whiteSpace(CSSValue value) { return prop("white-space", value); }
    default T wordBreak(CSSValue value) { return prop("word-break", value); }
    default T overflowWrap(CSSValue value) { return prop("overflow-wrap", value); }
    default T textWrap(CSSValue value) { return prop("text-wrap", value); }
    default T hyphens(CSSValue value) { return prop("hyphens", value); }

    // ==================== Convenience Presets ====================

    default T textCenter() { return textAlign(() -> "center"); }
    default T bold() { return fontWeight(700); }

    default T truncate() {
        return prop("overflow", () -> "hidden")
            .prop("text-overflow", () -> "ellipsis")
            .whiteSpace(() -> "nowrap");
    }
}
