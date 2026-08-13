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
    // PRIMARY is used both as text on white/#eef2ff and as a fill behind white text,
    // so it has to clear WCAG AA (4.5:1) in both directions. #6366f1 only reached 4.47:1.
    public static final CSSValue PRIMARY = hex("#4f46e5");
    public static final CSSValue PRIMARY_DARK = hex("#4338ca");
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

    // Fluid horizontal page gutter: 2rem on wide screens, tightening toward
    // 1rem on narrow phones — no breakpoint needed.
    public static final CSSValue GUTTER = clamp(SP_4, vw(5), SP_8);

    // Brand gradient (loops back to the first color for seamless animation).
    // Every stop carries white text somewhere, so each one clears 4.5:1 against white;
    // the lightest stop here is #db2777 at 4.60:1.
    public static final CSSValue BRAND_GRADIENT = linearGradient("90deg",
        hex("#4f46e5"), hex("#7c3aed"), hex("#9333ea"),
        hex("#db2777"), hex("#7c3aed"), hex("#4f46e5"));

    /**
     * The animated flowing brand gradient as a reusable style fragment.
     * Pairs with the {@code gradientShift} keyframes defined in Head.
     *
     * <pre>
     * button(style().padding(SP_3).apply(brandFlow()), ...)
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
        return div(style()
            .position(absolute).inset(zero)
            .borderRadius(radius).padding(px(2))
            .apply(brandFlow())
            .borderMask()
            .zIndex(0));
    }
}
