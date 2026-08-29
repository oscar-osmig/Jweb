package com.osmig.Jweb.framework.state;

/**
 * Static hook methods for state management.
 *
 * <p>Import statically for clean syntax:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.state.StateHooks.*;
 *
 * public class MyPage implements Template {
 *     private final State&lt;Integer&gt; count = useState(0);
 *     private final State&lt;String&gt; name = useState("Guest");
 *     private final State&lt;Boolean&gt; visible = useState(true);
 * }
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.State} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class StateHooks {

    protected StateHooks() {
        // Static utility class
    }

    /**
     * Creates a new reactive state with the given initial value.
     *
     * <p>This is the primary way to create state in JWeb components.</p>
     *
     * <p>Examples:</p>
     * <pre>
     * // Primitive types
     * State&lt;Integer&gt; count = useState(0);
     * State&lt;String&gt; name = useState("John");
     * State&lt;Boolean&gt; active = useState(false);
     * State&lt;Double&gt; price = useState(9.99);
     *
     * // Collections
     * State&lt;List&lt;String&gt;&gt; items = useState(new ArrayList&lt;&gt;());
     *
     * // Custom objects
     * State&lt;User&gt; user = useState(new User("admin"));
     * </pre>
     *
     * @param initialValue the initial state value
     * @param <T> the type of the state
     * @return a new State instance
     */
    public static <T> State<T> useState(T initialValue) {
        return StateManager.createState(initialValue);
    }

    /**
     * Creates a new reactive state with a null initial value.
     *
     * <p>Example:</p>
     * <pre>
     * State&lt;User&gt; currentUser = useState();  // Initially null
     * </pre>
     *
     * @param <T> the type of the state
     * @return a new State instance with null value
     */
    public static <T> State<T> useState() {
        return StateManager.createState(null);
    }

    /**
     * Creates a derived state that computes its value from other states.
     *
     * <p>Example:</p>
     * <pre>
     * State&lt;Integer&gt; width = useState(10);
     * State&lt;Integer&gt; height = useState(20);
     * State&lt;Integer&gt; area = useComputed(() -&gt; width.get() * height.get());
     * </pre>
     *
     * @param computation the computation function
     * @param dependencies states that this computed value depends on
     * @param <T> the type of the computed value
     * @return a new computed State instance
     */
    @SafeVarargs
    public static <T> State<T> useComputed(java.util.function.Supplier<T> computation, State<?>... dependencies) {
        State<T> computed = StateManager.createState(computation.get());

        // Subscribe to all dependencies
        for (State<?> dep : dependencies) {
            dep.subscribe(value -> computed.set(computation.get()));
        }

        return computed;
    }

    /**
     * Runs a side effect when dependencies change.
     *
     * <p>Example:</p>
     * <pre>
     * State&lt;String&gt; searchTerm = useState("");
     * useEffect(() -&gt; {
     *     System.out.println("Search term changed: " + searchTerm.get());
     * }, searchTerm);
     * </pre>
     *
     * @param effect the effect to run
     * @param dependencies states that trigger the effect
     */
    @SafeVarargs
    public static void useEffect(Runnable effect, State<?>... dependencies) {
        for (State<?> dep : dependencies) {
            dep.subscribe(value -> effect.run());
        }
        // Run effect immediately
        effect.run();
    }

    /**
     * Declares a reactive region of the page. The body is re-rendered on the
     * server whenever state changes during an event, and the resulting HTML is
     * patched into the DOM (matched by the given element ID).
     *
     * <p>Example:</p>
     * <pre>
     * State&lt;Integer&gt; count = useState(0);
     * ...
     * useComponent("counter", () -&gt; div(
     *     text("Count: " + count.get()),
     *     button(attrs().onClick(e -&gt; count.set(count.get() + 1)), text("+"))
     * ))
     * </pre>
     *
     * @param componentId the DOM id for the wrapper element (must be unique per page)
     * @param body supplies the region's content; called on every (re-)render
     * @return the wrapper element to place in the page
     */
    public static com.osmig.Jweb.framework.core.Element useComponent(
            String componentId, java.util.function.Supplier<? extends jweb.Element> body) {
        StateManager.StateContext context = StateManager.getContext();
        if (context != null) {
            context.registerComponent(componentId, () -> renderComponent(componentId, body));
        }
        return () -> wrapperVNode(componentId, body);
    }

    private static com.osmig.Jweb.framework.vdom.VElement wrapperVNode(
            String componentId, java.util.function.Supplier<? extends jweb.Element> body) {
        return com.osmig.Jweb.framework.vdom.VElement.of(
                "div",
                java.util.Map.of("id", componentId),
                java.util.List.of(body.get().toVNode()));
    }

    private static String renderComponent(
            String componentId, java.util.function.Supplier<? extends jweb.Element> body) {
        return wrapperVNode(componentId, body).toHtml();
    }
}
