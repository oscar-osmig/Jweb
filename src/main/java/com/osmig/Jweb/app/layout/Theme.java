package com.osmig.Jweb.app.layout;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;

/**
 * Design tokens and brand style fragments for the app.
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

    // Brand gradient (loops back to the first color for seamless animation)
    public static final CSSValue BRAND_GRADIENT = linearGradient("90deg",
        hex("#6366f1"), hex("#8b5cf6"), hex("#a855f7"),
        hex("#ec4899"), hex("#8b5cf6"), hex("#6366f1"));

    /**
     * The animated flowing brand gradient as a reusable style fragment.
     * Pairs with the {@code gradientShift} keyframes defined in Head.
     *
     * <pre>
     * button(attrs().style().padding(SP_3).apply(brandFlow()).done(), ...)
     * </pre>
     */
    public static Style<?> brandFlow() {
        return style()
            .background(BRAND_GRADIENT)
            .backgroundSize(percent(300), percent(100))
            .animation(anim("gradientShift"), s(3), linear, s(0), infinite);
    }

    /**
     * Animated gradient border overlay. Place as the first child of a
     * {@code position: relative} container; content goes in a sibling with
     * {@code z-index: 1}.
     */
    public static Element brandBorder(CSSValue radius) {
        return div(attrs().style()
            .position(absolute).inset(zero)
            .borderRadius(radius).padding(px(2))
            .apply(brandFlow())
            .borderMask()
            .zIndex(0)
        .done());
    }
}
