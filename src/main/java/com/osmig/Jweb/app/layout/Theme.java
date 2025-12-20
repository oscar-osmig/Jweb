package com.osmig.Jweb.app.layout;

import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

import com.osmig.Jweb.framework.styles.CSSValue;

/**
 * Design tokens for the app.
 */
public final class Theme {
    private Theme() {}

    // Colors
    public static final CSSValue PRIMARY = hex("#6366f1");
    public static final CSSValue PRIMARY_DARK = hex("#4f46e5");
    public static final CSSValue TEXT = hex("#1e293b");
    public static final CSSValue TEXT_LIGHT = hex("#64748b");
    public static final CSSValue BG = hex("#ffffff");
    public static final CSSValue BG_DARK = hex("#0f172a");
    public static final CSSValue BORDER = hex("#e2e8f0");

    // Spacing
    public static final CSSValue SP_1 = rem(0.25);
    public static final CSSValue SP_2 = rem(0.5);
    public static final CSSValue SP_3 = rem(0.75);
    public static final CSSValue SP_4 = rem(1);
    public static final CSSValue SP_6 = rem(1.5);
    public static final CSSValue SP_8 = rem(2);
    public static final CSSValue SP_12 = rem(3);

    // Font sizes
    public static final CSSValue TEXT_SM = rem(0.875);
    public static final CSSValue TEXT_BASE = rem(1);
    public static final CSSValue TEXT_LG = rem(1.125);
    public static final CSSValue TEXT_XL = rem(1.25);
    public static final CSSValue TEXT_2XL = rem(1.5);
    public static final CSSValue TEXT_3XL = rem(2);
    public static final CSSValue TEXT_4XL = rem(2.5);

    // Border radius
    public static final CSSValue ROUNDED = px(6);
    public static final CSSValue ROUNDED_LG = px(12);
}
