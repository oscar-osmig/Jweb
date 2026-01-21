package com.osmig.Jweb.framework.styles;

/**
 * Mixin interface for CSS box model properties: margin, padding, border, dimensions.
 *
 * @param <T> the concrete style type for fluent chaining
 */
public interface StyleBoxModel<T extends Style<T>> {

    T prop(String name, CSSValue value);

    // ==================== Margin ====================

    default T margin(CSSValue all) { return prop("margin", all); }

    default T margin(CSSValue vertical, CSSValue horizontal) {
        return prop("margin", () -> vertical.css() + " " + horizontal.css());
    }

    default T margin(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("margin", () -> top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    default T marginTop(CSSValue value) { return prop("margin-top", value); }
    default T marginRight(CSSValue value) { return prop("margin-right", value); }
    default T marginBottom(CSSValue value) { return prop("margin-bottom", value); }
    default T marginLeft(CSSValue value) { return prop("margin-left", value); }
    default T marginX(CSSValue value) { return marginLeft(value).marginRight(value); }
    default T marginY(CSSValue value) { return marginTop(value).marginBottom(value); }
    default T marginInline(CSSValue value) { return prop("margin-inline", value); }
    default T marginBlock(CSSValue value) { return prop("margin-block", value); }

    // ==================== Padding ====================

    default T padding(CSSValue all) { return prop("padding", all); }

    default T padding(CSSValue vertical, CSSValue horizontal) {
        return prop("padding", () -> vertical.css() + " " + horizontal.css());
    }

    default T padding(CSSValue top, CSSValue right, CSSValue bottom, CSSValue left) {
        return prop("padding", () -> top.css() + " " + right.css() + " " + bottom.css() + " " + left.css());
    }

    default T paddingTop(CSSValue value) { return prop("padding-top", value); }
    default T paddingRight(CSSValue value) { return prop("padding-right", value); }
    default T paddingBottom(CSSValue value) { return prop("padding-bottom", value); }
    default T paddingLeft(CSSValue value) { return prop("padding-left", value); }
    default T paddingX(CSSValue value) { return paddingLeft(value).paddingRight(value); }
    default T paddingY(CSSValue value) { return paddingTop(value).paddingBottom(value); }
    default T paddingInline(CSSValue value) { return prop("padding-inline", value); }
    default T paddingBlock(CSSValue value) { return prop("padding-block", value); }

    // ==================== Dimensions ====================

    default T width(CSSValue value) { return prop("width", value); }
    default T height(CSSValue value) { return prop("height", value); }
    default T minWidth(CSSValue value) { return prop("min-width", value); }
    default T maxWidth(CSSValue value) { return prop("max-width", value); }
    default T minHeight(CSSValue value) { return prop("min-height", value); }
    default T maxHeight(CSSValue value) { return prop("max-height", value); }

    // ==================== Border ====================

    default T border(CSSValue width, CSSValue style, CSSValue color) {
        return prop("border", () -> width.css() + " " + style.css() + " " + color.css());
    }

    default T border(CSSValue value) { return prop("border", value); }
    default T borderTop(CSSValue value) { return prop("border-top", value); }
    default T borderRight(CSSValue value) { return prop("border-right", value); }
    default T borderBottom(CSSValue value) { return prop("border-bottom", value); }
    default T borderLeft(CSSValue value) { return prop("border-left", value); }
    default T borderWidth(CSSValue value) { return prop("border-width", value); }
    default T borderStyle(CSSValue value) { return prop("border-style", value); }
    default T borderColor(CSSValue value) { return prop("border-color", value); }
    default T borderRadius(CSSValue value) { return prop("border-radius", value); }

    // ==================== Box Sizing ====================

    default T boxSizing(CSSValue value) { return prop("box-sizing", value); }
}
