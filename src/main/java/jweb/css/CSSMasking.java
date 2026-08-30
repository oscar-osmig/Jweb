package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.CSSMasking} — same API, shorter import:
 * {@code import static jweb.css.CSSMasking.*;}
 *
 * @deprecated Use the {@code Style} methods instead — {@link jweb.Style#mask}, {@link jweb.Style#maskImage}, {@link jweb.Style#maskComposite}, {@link jweb.Style#clipPath} and friends — which take typed values instead of pre-joined {@code "prop:value"} strings.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class CSSMasking extends com.osmig.Jweb.framework.styles.CSSMasking {

    protected CSSMasking() {}
}
