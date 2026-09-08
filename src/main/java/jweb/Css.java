package jweb;

import jweb.css.Keyframes;
import jweb.css.MediaQuery;
import jweb.css.Stylesheet;
import com.osmig.Jweb.framework.styles.ViewTransitions;

/**
 * The CSS DSL — properties, units, colors, grid, animations, and variables
 * in one import:
 *
 * <pre>{@code
 * import jweb.Style;
 * import static jweb.Css.*;
 *
 * Style card = style()
 *     .padding(rem(1.5))
 *     .background(hsl(220, 15, 97))
 *     .borderRadius(px(12));
 * }</pre>
 *
 * <p>Combines the legacy {@code CSS}, {@code CSSUnits}, {@code CSSColors},
 * {@code CSSGrid}, {@code CSSAnimations} and {@code CSSVariables} entry
 * points, plus the {@code media()}, {@code keyframes()} and
 * {@code stylesheet()} factories — one wildcard import replaces them all.</p>
 */
@SuppressWarnings("deprecation")
public class Css extends com.osmig.Jweb.framework.styles.CSS {

    protected Css() {}

    // ==================== Media Queries ====================

    /** Starts a media query: {@code media().maxWidth(px(768)).rule(...)} */
    public static MediaQuery media() { return MediaQuery.media(); }

    /** Max-width 575px (phones). */
    public static MediaQuery xs() { return MediaQuery.xs(); }
    /** Min-width 576px. */
    public static MediaQuery sm() { return MediaQuery.sm(); }
    /** Min-width 768px. */
    public static MediaQuery md() { return MediaQuery.md(); }
    /** Min-width 992px. */
    public static MediaQuery lg() { return MediaQuery.lg(); }
    /** Min-width 1200px. */
    public static MediaQuery xl() { return MediaQuery.xl(); }
    /** Min-width 1400px. */
    public static MediaQuery xxl() { return MediaQuery.xxl(); }
    /**
     * Max-width 767px.
     *
     * @return the media query
     * @deprecated Use {@link #xs()} — keep one breakpoint system; xs–xxl is the canonical one.
     */
    @Deprecated
    public static MediaQuery mobile() { return MediaQuery.mobile(); }

    /**
     * 768–1023px.
     *
     * @return the media query
     * @deprecated Use {@link #md()} — keep one breakpoint system; xs–xxl is the canonical one.
     */
    @Deprecated
    public static MediaQuery tablet() { return MediaQuery.tablet(); }

    /**
     * Min-width 1024px.
     *
     * @return the media query
     * @deprecated Use {@link #lg()} — keep one breakpoint system; xs–xxl is the canonical one.
     */
    @Deprecated
    public static MediaQuery desktop() { return MediaQuery.desktop(); }

    // ==================== Keyframes & Stylesheets ====================

    /** Starts a keyframes animation: {@code keyframes("spin").from(...).to(...)} */
    public static Keyframes keyframes(String name) { return Keyframes.keyframes(name); }

    /** Starts a stylesheet: {@code stylesheet().add(".card", style()...)} */
    public static Stylesheet stylesheet() { return Stylesheet.stylesheet(); }

    // ==================== Layout Mixins ====================
    //
    // Style fragments for the handful of arrangements every app writes over
    // and over. Each returns a Style, so it composes with .apply(...), with
    // tokens, and with the conditional rules on Style:
    //
    //   div(row(SP_4).apply(card()).hover(style().borderColor(PRIMARY)), …)

    /** A horizontal flex row with its items vertically centred. */
    public static Style<?> row() {
        return style().display(flex).alignItems(center);
    }

    /**
     * A horizontal flex row with a gap between its items.
     *
     * @param gap the gap
     * @return the style fragment
     */
    public static Style<?> row(CSSValue gap) {
        return style().display(flex).alignItems(center).gap(gap);
    }

    /** A vertical flex column. */
    public static Style<?> stack() {
        return style().display(flex).flexDirection(column);
    }

    /**
     * A vertical flex column with a gap between its items.
     *
     * @param gap the gap
     * @return the style fragment
     */
    public static Style<?> stack(CSSValue gap) {
        return style().display(flex).flexDirection(column).gap(gap);
    }

    /** Centres its children on both axes. */
    public static Style<?> center() {
        return style().display(flex).alignItems(center).justifyContent(center);
    }

    /**
     * A row that wraps — chips, tags, a toolbar that survives a narrow screen.
     *
     * @param gap the gap between items and between wrapped lines
     * @return the style fragment
     */
    public static Style<?> cluster(CSSValue gap) {
        return style().display(flex).flexWrap(wrap).alignItems(center).gap(gap);
    }

    /**
     * A centred content column: full width up to {@code maxWidth}, then
     * centred with the page gutter on either side.
     *
     * @param maxWidth the maximum content width
     * @return the style fragment
     */
    public static Style<?> container(CSSValue maxWidth) {
        return style().width(percent(100)).maxWidth(maxWidth).marginInline(auto);
    }

    /**
     * A surface: background, hairline border, radius and padding, all read
     * from the theme's tokens when one is defined and falling back to sane
     * neutrals when it is not.
     *
     * @return the style fragment
     */
    public static Style<?> card() {
        return style()
            .background(jweb.css.Theme.var("color-surface", hex("#ffffff")))
            .border(px(1), solid, jweb.css.Theme.var("color-border", hex("#e2e8f0")))
            .borderRadius(jweb.css.Theme.var("radius-lg", px(12)))
            .padding(jweb.css.Theme.var("space-4", rem(1)));
    }

    /** One line, cut off with an ellipsis. */
    public static Style<?> truncate() {
        return style().overflow(hidden).textOverflow("ellipsis").whiteSpace(nowrap);
    }

    /**
     * Clamped to n lines, cut off with an ellipsis.
     *
     * @param lines how many lines to keep
     * @return the style fragment
     */
    public static Style<?> truncate(int lines) {
        return style().lineClamp(lines);
    }

    /** Visually hidden, still read by screen readers. */
    public static Style<?> srOnly() {
        return style().srOnly();
    }

    /**
     * Breaks out of a centred container to the full viewport width, without
     * causing a horizontal scrollbar.
     *
     * @return the style fragment
     */
    public static Style<?> fullBleed() {
        return style().width(vw(100)).marginInline(calc("50% - 50vw"));
    }

    /**
     * A fixed aspect ratio.
     *
     * @param width the ratio's width part
     * @param height the ratio's height part
     * @return the style fragment
     */
    public static Style<?> aspect(int width, int height) {
        return style().aspectRatio(width, height);
    }

    /** Fills its box, cropping the overflow — for images and video. */
    public static Style<?> cover() {
        return style().width(percent(100)).height(percent(100)).objectFit(cover);
    }

    /** Fits inside its box whole, letterboxed — for images and video. */
    public static Style<?> contain() {
        return style().width(percent(100)).height(percent(100)).objectFit(contain);
    }

    /**
     * A grid of equal columns.
     *
     * @param columns how many columns
     * @param gap the gap between cells
     * @return the style fragment
     */
    public static Style<?> grid(int columns, CSSValue gap) {
        return style().display(grid)
            .gridTemplateColumns("repeat(" + columns + ", minmax(0, 1fr))")
            .gap(gap);
    }

    /**
     * A responsive grid with no breakpoints: as many columns as fit at
     * {@code min} wide or more.
     *
     * @param min the narrowest a column may be
     * @return the style fragment
     */
    public static Style<?> autoGrid(CSSValue min) {
        return style().display(grid)
            .gridTemplateColumns("repeat(auto-fit, minmax(" + min.css() + ", 1fr))");
    }

    /**
     * A responsive grid with a gap — see {@link #autoGrid(CSSValue)}.
     *
     * @param min the narrowest a column may be
     * @param gap the gap between cells
     * @return the style fragment
     */
    public static Style<?> autoGrid(CSSValue min, CSSValue gap) {
        return style().display(grid)
            .gridTemplateColumns("repeat(auto-fit, minmax(" + min.css() + ", 1fr))")
            .gap(gap);
    }

    // ==================== View Transitions ====================

    /**
     * Opts this document into cross-document View Transitions:
     * {@code stylesheet().add(viewTransitions())} emits
     * {@code @view-transition{navigation:auto}}.
     */
    public static ViewTransitions viewTransitions() { return ViewTransitions.viewTransitions(); }
}
