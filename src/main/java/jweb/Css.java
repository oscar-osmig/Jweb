package jweb;

import com.osmig.Jweb.framework.styles.Keyframes;
import com.osmig.Jweb.framework.styles.MediaQuery;
import com.osmig.Jweb.framework.styles.Stylesheet;
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

    // ==================== View Transitions ====================

    /**
     * Opts this document into cross-document View Transitions:
     * {@code stylesheet().add(viewTransitions())} emits
     * {@code @view-transition{navigation:auto}}.
     */
    public static ViewTransitions viewTransitions() { return ViewTransitions.viewTransitions(); }
}
