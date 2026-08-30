package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.CSSScrollSnap} — same API, shorter import:
 * {@code import static jweb.css.CSSScrollSnap.*;}
 *
 * @deprecated Use the {@code Style} methods instead — {@link jweb.Style#scrollSnapType}, {@link jweb.Style#scrollSnapAlign}, {@link jweb.Style#scrollSnapStop}, {@link jweb.Style#scrollPadding}, {@link jweb.Style#scrollMargin} — which take typed values and use the real CSS property names ({@code scroll-padding}/{@code scroll-margin}, not {@code scroll-snap-*}).
 */
@Deprecated
@SuppressWarnings("deprecation")
public class CSSScrollSnap extends com.osmig.Jweb.framework.styles.CSSScrollSnap {

    protected CSSScrollSnap() {}
}
