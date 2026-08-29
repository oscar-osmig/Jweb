package jweb;

/**
 * The declarative event-action DSL — attach behaviors to elements without
 * writing raw JavaScript:
 *
 * <pre>{@code
 * import static jweb.Actions.*;
 *
 * button(text("Toggle"), onClick(toggleClass("#panel", "open")))
 * }</pre>
 *
 * <p>Short alias for the legacy
 * {@code com.osmig.Jweb.framework.js.Actions} entry point. Kept separate
 * from {@link Js} because both define {@code query()}, {@code queryAll()}
 * and {@code script()} with different return types.</p>
 */
@SuppressWarnings("deprecation")
public class Actions extends com.osmig.Jweb.framework.js.Actions {

    protected Actions() {}
}
