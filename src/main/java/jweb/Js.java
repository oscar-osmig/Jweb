package jweb;

/**
 * The client-side JavaScript DSL in one import — page-level handlers and
 * actions, expressions and statements, DOM access, events, the reactive
 * runtime, and async helpers:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * button(onClick(toggle("panel")), "Menu")                 // an Action on an element
 *
 * inlineScript(actions()
 *     .add(onSubmit("contact-form")
 *         .post("/api/contact").withFormData()
 *         .ok(showMessage("status").success("Sent!")))
 *     .build())
 *
 * Func check = func("check", "x")
 *     .if_(v("x").gt(10), call("big"))
 *     .else_(call("small"));
 * }</pre>
 *
 * <p>Combines the legacy {@code Actions}, {@code JS}, {@code Events},
 * {@code Runtime} and {@code Async} entry points. Where the page-level and
 * expression-level layers shared a name — {@code fetch(url)}, {@code call(fn)},
 * {@code sleep(ms)} — the page-level {@code Action} form wins here; the
 * expression forms remain reachable qualified
 * ({@code Async.fetch(...)}, {@code JS.call(...)}). {@link Actions} is the
 * same surface under its old name.</p>
 */
@SuppressWarnings("deprecation")
public class Js extends com.osmig.Jweb.framework.js.Actions {

    protected Js() {}
}
