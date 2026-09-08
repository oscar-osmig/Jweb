package com.osmig.Jweb.app.layout;

import static jweb.El.*;
import static jweb.Css.*;

import jweb.Element;
import jweb.CSSValue;
import jweb.Style;

/**
 * Design tokens and brand style fragments for the app.
 *
 * <p>The tokens are CSS custom properties — {@link #TOKENS} is emitted once by
 * {@link Layout}'s stylesheet, and every constant below reads one back as
 * {@code var(--…)}. Pages keep naming {@code PRIMARY} and {@code SP_4}; what
 * changed is that the value now lives in the document, so devtools can show it
 * and a future theme swap is a value change rather than a recompile.</p>
 */
public final class Theme {
    private Theme() {}

    /** The token definitions. Emitted by {@link Layout#styles()}. */
    public static final jweb.css.Theme TOKENS = jweb.css.Theme.light()
        // PRIMARY is used both as text on white/#eef2ff and as a fill behind
        // white text, so it has to clear WCAG AA (4.5:1) in both directions.
        // #6366f1 only reached 4.47:1.
        .color("primary", hex("#4f46e5"))
        .color("primary-dark", hex("#4338ca"))
        .color("text", hex("#1e293b"))
        .color("text-light", hex("#64748b"))
        .color("bg", hex("#ffffff"))
        .color("bg-dark", hex("#0f172a"))
        .color("surface", hex("#ffffff"))
        .color("border", hex("#e2e8f0"))
        .space("1", rem(0.25))
        .space("2", rem(0.5))
        .space("3", rem(0.75))
        .space("4", rem(1))
        .space("6", rem(1.5))
        .space("8", rem(2))
        .space("12", rem(3))
        // Fluid horizontal page gutter: 2rem on wide screens, tightening
        // toward 1rem on narrow phones — no breakpoint needed.
        .space("gutter", clamp(rem(1), vw(5), rem(2)))
        .text("sm", rem(0.875))
        .text("base", rem(1))
        .text("lg", rem(1.125))
        .text("xl", rem(1.25))
        .text("2xl", rem(1.5))
        .text("3xl", rem(2))
        .text("4xl", rem(2.5))
        .radius("md", px(6))
        .radius("lg", px(12))
        // Brand gradient (loops back to the first color for seamless
        // animation). Every stop carries white text somewhere, so each one
        // clears 4.5:1 against white; the lightest stop is #db2777 at 4.60:1.
        .gradient("brand", linearGradient("90deg",
            hex("#4f46e5"), hex("#7c3aed"), hex("#9333ea"),
            hex("#db2777"), hex("#7c3aed"), hex("#4f46e5")));

    // Colors
    public static final CSSValue PRIMARY = jweb.css.Theme.color("primary");
    public static final CSSValue PRIMARY_DARK = jweb.css.Theme.color("primary-dark");
    public static final CSSValue TEXT = jweb.css.Theme.color("text");
    public static final CSSValue TEXT_LIGHT = jweb.css.Theme.color("text-light");
    public static final CSSValue BG = jweb.css.Theme.color("bg");
    public static final CSSValue BG_DARK = jweb.css.Theme.color("bg-dark");
    public static final CSSValue BORDER = jweb.css.Theme.color("border");

    // Spacing
    public static final CSSValue SP_1 = jweb.css.Theme.space("1");
    public static final CSSValue SP_2 = jweb.css.Theme.space("2");
    public static final CSSValue SP_3 = jweb.css.Theme.space("3");
    public static final CSSValue SP_4 = jweb.css.Theme.space("4");
    public static final CSSValue SP_6 = jweb.css.Theme.space("6");
    public static final CSSValue SP_8 = jweb.css.Theme.space("8");
    public static final CSSValue SP_12 = jweb.css.Theme.space("12");

    // Font sizes
    public static final CSSValue TEXT_SM = jweb.css.Theme.text("sm");
    public static final CSSValue TEXT_BASE = jweb.css.Theme.text("base");
    public static final CSSValue TEXT_LG = jweb.css.Theme.text("lg");
    public static final CSSValue TEXT_XL = jweb.css.Theme.text("xl");
    public static final CSSValue TEXT_2XL = jweb.css.Theme.text("2xl");
    public static final CSSValue TEXT_3XL = jweb.css.Theme.text("3xl");
    public static final CSSValue TEXT_4XL = jweb.css.Theme.text("4xl");

    // Border radius
    public static final CSSValue ROUNDED = jweb.css.Theme.radius("md");
    public static final CSSValue ROUNDED_LG = jweb.css.Theme.radius("lg");

    /** Fluid horizontal page gutter. */
    public static final CSSValue GUTTER = jweb.css.Theme.space("gutter");

    /** The animated flowing brand gradient. */
    public static final CSSValue BRAND_GRADIENT = jweb.css.Theme.gradient("brand");

    /**
     * The animated flowing brand gradient as a reusable style fragment.
     * Pairs with the {@code gradientShift} keyframes in {@link Layout}'s
     * stylesheet.
     *
     * <pre>
     * button(style().padding(SP_3).apply(brandFlow()), ...)
     * </pre>
     *
     * @return the style fragment
     */
    public static Style<?> brandFlow() {
        return style()
            .background(BRAND_GRADIENT)
            .backgroundSize(percent(300), percent(100))
            .animation("gradientShift", s(3), linear, s(0), infinite);
    }

    /**
     * Animated gradient border overlay. Place as the first child of a
     * {@code position: relative} container; content goes in a sibling with
     * {@code z-index: 1}.
     *
     * @param radius the corner radius to match the container
     * @return the overlay element
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
