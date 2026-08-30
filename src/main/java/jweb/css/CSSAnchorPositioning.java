package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.CSSAnchorPositioning} — same API, shorter import:
 * {@code import static jweb.css.CSSAnchorPositioning.*;}
 *
 * @deprecated Use the {@code Style} methods instead — {@link jweb.Style#anchorName}, {@link jweb.Style#positionAnchor}, {@link jweb.Style#positionArea}, {@link jweb.Style#positionVisibility}, {@link jweb.Style#positionTryFallbacks} — which take typed values instead of pre-joined {@code "prop:value"} strings.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class CSSAnchorPositioning extends com.osmig.Jweb.framework.styles.CSSAnchorPositioning {

    protected CSSAnchorPositioning() {}
}
