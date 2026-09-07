package jweb;

/**
 * Document-to-Markdown conversion through the {@code markitdown} CLI:
 *
 * <pre>{@code
 * import jweb.Markitdown;
 *
 * if (Markitdown.isAvailable()) {
 *     String md = Markitdown.convert(file.bytes(), file.extension());
 * }
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.markdown.Markitdown} —
 * the same statics under the short import.</p>
 */
public class Markitdown extends com.osmig.Jweb.framework.markdown.Markitdown {

    protected Markitdown() {}
}
