package jweb;

/**
 * The client-side JavaScript DSL — script building, DOM access, events, the
 * reactive runtime, and async helpers in one import:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * script()
 *     .onClick("#save", handler -> handler
 *         .fetch("/api/v1/save").post()
 *         .then(res -> res.showToast("Saved!")))
 * }</pre>
 *
 * <p>Combines the legacy {@code JS}, {@code Events}, {@code Runtime} and
 * {@code Async} entry points — one wildcard import replaces them all.
 * The event-action DSL lives in {@link Actions} (it shares method names like
 * {@code query()} and {@code script()} with different meanings, so the two
 * cannot be merged).</p>
 */
@SuppressWarnings("deprecation")
public class Js extends com.osmig.Jweb.framework.js.JS {

    protected Js() {}
}
