package jweb;

/**
 * Enter/leave transitions for conditionally rendered content:
 *
 * <pre>{@code
 * import jweb.Transition;
 *
 * Transition.fade(open, () -> div("panel"))
 * Transition.when(open).enter("fade-in").leave("fade-out").render(() -> div("panel"))
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.transition.Transition} —
 * the same statics under the short import; the result is an {@link Element}.</p>
 */
public class Transition extends com.osmig.Jweb.framework.transition.Transition {

    protected Transition(boolean show) {
        super(show);
    }
}
