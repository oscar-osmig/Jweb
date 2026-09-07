package jweb;

/**
 * Toast notifications — a container element for the page plus {@link Action}s
 * that show one:
 *
 * <pre>{@code
 * import jweb.Toast;
 *
 * body(Toast.setup(), ...)
 * button(onClick(Toast.success("Saved!")), "Save")
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.ui.Toast} — the same
 * statics under the short import; every action returns {@code jweb.Action}.</p>
 */
public class Toast extends com.osmig.Jweb.framework.ui.Toast {

    protected Toast() {}
}
