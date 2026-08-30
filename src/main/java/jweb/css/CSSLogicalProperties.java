package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.CSSLogicalProperties} — same API, shorter import:
 * {@code import static jweb.css.CSSLogicalProperties.*;}
 *
 * @deprecated Use the {@code Style} methods instead — {@link jweb.Style#marginInline}, {@link jweb.Style#paddingBlock}, {@link jweb.Style#insetInline}, {@link jweb.Style#inlineSize} and friends — which take typed values instead of pre-joined {@code "prop:value"} strings.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class CSSLogicalProperties extends com.osmig.Jweb.framework.styles.CSSLogicalProperties {

    protected CSSLogicalProperties() {}
}
