package jweb.css;

/**
 * Short alias for {@link com.osmig.Jweb.framework.styles.Utility} — same API, shorter import:
 * {@code import static jweb.css.Utility.*;}
 *
 * @deprecated A Tailwind clone is a framework opinion, not a CSS capability — use
 *             {@code style()} / {@code rule()} directly. Many of its variant classes
 *             ({@code dark:}, responsive, {@code text-gray-*}) also generate no CSS
 *             at all; see {@link com.osmig.Jweb.framework.styles.Utility}.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class Utility extends com.osmig.Jweb.framework.styles.Utility {

    protected Utility() {}
}
