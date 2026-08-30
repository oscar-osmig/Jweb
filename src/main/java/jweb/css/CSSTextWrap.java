package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.CSSTextWrap} — same API, shorter import:
 * {@code import static jweb.css.CSSTextWrap.*;}
 *
 * @deprecated Use the {@code Style} methods instead — {@link jweb.Style#textWrap}, {@link jweb.Style#whiteSpaceCollapse}, {@link jweb.Style#wordBreak}, {@link jweb.Style#overflowWrap}, {@link jweb.Style#hyphens}, {@link jweb.Style#lineClamp(int)} — which take typed values instead of pre-joined {@code "prop:value"} strings.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class CSSTextWrap extends com.osmig.Jweb.framework.styles.CSSTextWrap {

    protected CSSTextWrap() {}
}
