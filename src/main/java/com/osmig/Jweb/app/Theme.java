package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;

import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.Styles.*;

/**
 * Centralized design tokens and reusable styles for the app.
 * This keeps styling consistent and easy to maintain.
 */
public final class Theme {

    private Theme() {}

    // ==================== Colors ====================

    public static final CSSValue PRIMARY = hex("#667eea");
    public static final CSSValue PRIMARY_DARK = hex("#5a67d8");
    public static final CSSValue PRIMARY_LIGHT = hex("#818cf8");
    public static final CSSValue SECONDARY = hex("#764ba2");
    public static final CSSValue ACCENT = hex("#f093fb");

    // Status colors
    public static final CSSValue SUCCESS = hex("#10b981");
    public static final CSSValue SUCCESS_LIGHT = hex("#d1fae5");
    public static final CSSValue WARNING = hex("#f59e0b");
    public static final CSSValue WARNING_LIGHT = hex("#fef3c7");
    public static final CSSValue ERROR = hex("#ef4444");
    public static final CSSValue ERROR_LIGHT = hex("#fee2e2");
    public static final CSSValue INFO = hex("#3b82f6");
    public static final CSSValue INFO_LIGHT = hex("#dbeafe");

    public static final CSSValue TEXT = hex("#1f2937");
    public static final CSSValue TEXT_LIGHT = hex("#4b5563");
    public static final CSSValue TEXT_MUTED = hex("#9ca3af");

    public static final CSSValue BG = white;
    public static final CSSValue BG_LIGHT = hex("#f9fafb");
    public static final CSSValue BG_DARK = hex("#111827");

    public static final CSSValue BORDER = hex("#e5e7eb");
    public static final CSSValue BORDER_LIGHT = hex("#f3f4f6");
    public static final CSSValue SHADOW = rgba(0, 0, 0, 0.08);
    public static final CSSValue SHADOW_HOVER = rgba(0, 0, 0, 0.12);
    public static final CSSValue SHADOW_LG = rgba(0, 0, 0, 0.15);

    // ==================== Spacing ====================

    public static final CSSValue SPACE_XS = rem(0.25);
    public static final CSSValue SPACE_SM = rem(0.5);
    public static final CSSValue SPACE_MD = rem(1);
    public static final CSSValue SPACE_LG = rem(1.5);
    public static final CSSValue SPACE_XL = rem(2);
    public static final CSSValue SPACE_2XL = rem(3);

    // ==================== Typography ====================

    public static final String FONT_FAMILY = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

    public static final CSSValue FONT_SM = rem(0.875);
    public static final CSSValue FONT_BASE = rem(1);
    public static final CSSValue FONT_LG = rem(1.125);
    public static final CSSValue FONT_XL = rem(1.25);
    public static final CSSValue FONT_2XL = rem(1.5);
    public static final CSSValue FONT_3XL = rem(2);

    // ==================== Border Radius ====================

    public static final CSSValue RADIUS_SM = px(4);
    public static final CSSValue RADIUS_MD = px(8);
    public static final CSSValue RADIUS_LG = px(12);
    public static final CSSValue RADIUS_FULL = px(9999);

    // ==================== Transitions ====================

    public static final CSSValue TRANSITION_FAST = ms(150);
    public static final CSSValue TRANSITION_NORMAL = ms(200);
    public static final CSSValue TRANSITION_SLOW = ms(300);

    // ==================== Reusable Styles ====================

    /** Container with max-width and centered */
    public static Style container() {
        return style()
            .maxWidth(px(1200))
            .margin(zero, auto)
            .padding(SPACE_XL);
    }

    /** Flexbox row with gap */
    public static Style flexRow() {
        return style()
            .display(flex)
            .flexDirection(row)
            .gap(SPACE_MD);
    }

    /** Flexbox column with gap */
    public static Style flexCol() {
        return style()
            .display(flex)
            .flexDirection(column)
            .gap(SPACE_MD);
    }

    /** Center content with flexbox */
    public static Style flexCenter() {
        return style()
            .display(flex)
            .justifyContent(center)
            .alignItems(center);
    }

    /** Card shadow */
    public static Style cardShadow() {
        return style()
            .boxShadow(px(0), px(2), px(8), SHADOW);
    }

    /** Hover shadow */
    public static Style cardShadowHover() {
        return style()
            .boxShadow(px(0), px(4), px(12), SHADOW_HOVER);
    }

    /** Primary button style */
    public static Style btnPrimary() {
        return style()
            .backgroundColor(PRIMARY)
            .color(white)
            .border(none)
            .padding(SPACE_SM, SPACE_LG)
            .fontSize(FONT_SM)
            .fontWeight(600)
            .borderRadius(RADIUS_SM)
            .cursor(pointer)
            .transition("all", TRANSITION_FAST, ease);
    }

    /** Secondary button style */
    public static Style btnSecondary() {
        return style()
            .backgroundColor(transparent)
            .color(PRIMARY)
            .border(px(2), solid, PRIMARY)
            .padding(SPACE_SM, SPACE_LG)
            .fontSize(FONT_SM)
            .fontWeight(600)
            .borderRadius(RADIUS_SM)
            .cursor(pointer)
            .transition("all", TRANSITION_FAST, ease);
    }

    /** Badge/tag style */
    public static Style badge() {
        return style()
            .display(inlineBlock)
            .padding(SPACE_XS, SPACE_SM)
            .fontSize(FONT_SM)
            .fontWeight(500)
            .borderRadius(RADIUS_FULL)
            .backgroundColor(PRIMARY_LIGHT)
            .color(white);
    }

    /** Input field style */
    public static Style inputStyle() {
        return style()
            .width(percent(100))
            .padding(SPACE_SM, SPACE_MD)
            .fontSize(FONT_BASE)
            .border(px(1), solid, BORDER)
            .borderRadius(RADIUS_SM)
            .backgroundColor(white)
            .transition("border-color", TRANSITION_FAST, ease);
    }
}
