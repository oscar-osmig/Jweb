package jweb.state;

/**
 * A reactive state value, as returned by {@code useState(...)}:
 *
 * <pre>{@code
 * import jweb.state.State;
 * import static jweb.State.*;
 *
 * State<Integer> count = useState(0);
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.state.State} — the
 * framework always hands out this subtype, so either name works in
 * declarations.</p>
 */
public class State<T> extends com.osmig.Jweb.framework.state.State<T> {

    /** Internal — obtain instances via {@code useState(...)}, not this ctor. */
    public State(String id, T initialValue) {
        super(id, initialValue);
    }
}
