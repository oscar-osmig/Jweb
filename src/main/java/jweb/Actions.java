package jweb;

/**
 * The event-action DSL — attach behaviors to elements without writing raw
 * JavaScript:
 *
 * <pre>{@code
 * import static jweb.Actions.*;
 *
 * button(onClick(toggleClass("panel", "open")), "Toggle")
 * }</pre>
 *
 * <p>Since 3.0 this is the same surface as {@link Js}: the two layers were
 * merged so one import covers both, and {@code import static jweb.Js.*} is the
 * recommended spelling. Importing both is harmless — every name resolves to
 * the same declaration.</p>
 */
@SuppressWarnings("deprecation")
public class Actions extends com.osmig.Jweb.framework.js.Actions {

    protected Actions() {}
}
