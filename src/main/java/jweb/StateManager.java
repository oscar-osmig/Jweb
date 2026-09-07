package jweb;

/**
 * The state registry behind {@code useState}: explicit ids, global change
 * listeners and per-session state contexts:
 *
 * <pre>{@code
 * import jweb.StateManager;
 *
 * State<Integer> count = StateManager.createState("count", 0);
 * StateManager.withContext(() -> render());
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.state.StateManager} —
 * the same statics under the short import. For everyday state prefer
 * {@code import static jweb.State.*} ({@code useState}, {@code useComputed}).</p>
 */
public class StateManager extends com.osmig.Jweb.framework.state.StateManager {

    protected StateManager() {}
}
