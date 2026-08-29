package jweb;

/**
 * Typed input helpers whose names would clash with {@link El} element
 * factories, kept behind their own import on purpose:
 *
 * <pre>{@code
 * import static jweb.Input.*;
 * }</pre>
 *
 * <p>Short alias for the legacy
 * {@code com.osmig.Jweb.framework.elements.Input} entry point.</p>
 */
@SuppressWarnings("deprecation")
public class Input extends com.osmig.Jweb.framework.elements.Input {

    protected Input() {}
}
