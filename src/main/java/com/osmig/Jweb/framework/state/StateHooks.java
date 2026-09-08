package com.osmig.Jweb.framework.state;

import jweb.Attributes;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The state hooks — {@code useState}, {@code live} and friends. Import them
 * statically through the short name:
 *
 * <pre>{@code
 * import static jweb.State.*;
 *
 * State<Integer> count = useState(0);
 * State<List<String>> items = useState(new ArrayList<>());
 *
 * div(
 *     p("Count: ", span(bind(count))),                       // bind: El
 *     live(items, list -> ul(each(list, i -> li(i)))),        // live: State
 *     button(bindAttr(busy, "disabled"), onClick(e -> ...), "Add")
 * )
 * }</pre>
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
    public static <T> jweb.state.State<T> useState(T initialValue) {
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
    public static <T> jweb.state.State<T> useState() {
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
    public static <T> jweb.state.State<T> useComputed(Supplier<T> computation, State<?>... dependencies) {
        jweb.state.State<T> computed = StateManager.createState(computation.get());

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

    // ==================== Live regions ====================

    /**
     * A region of the page rendered from a state's value, re-rendered on the
     * server whenever that state changes and morphed into the DOM — lists,
     * conditionals and attributes stay in sync without any client code:
     *
     * <pre>{@code
     * State<List<String>> items = useState(new ArrayList<>());
     *
     * live(items, list -> ul(each(list, i -> li(i))))
     * live(user, u -> u == null ? a(href("/login"), "Sign in") : span("Hi " + u.name()))
     * }</pre>
     *
     * <p>The body receives the current value and returns any element; the
     * root element carries a {@code data-live} attribute the runtime targets.
     * Unchanged nodes are kept, so focus, scroll position and typed input
     * survive the update. Event handlers inside the body are re-registered
     * on each render, exactly like the initial one.</p>
     *
     * @param state the state the region depends on
     * @param body  renders the region from the state's value
     * @param <A>   the state's value type
     * @return the region, to place in the page like any element
     */
    public static <A> jweb.Element live(State<A> state,
                                        Function<? super A, ? extends jweb.Element> body) {
        return LiveRegion.of(() -> body.apply(state.get()), state);
    }

    /**
     * A live region over two states — re-rendered when either changes.
     *
     * <pre>{@code
     * live(todos, filter, (list, f) -> ul(each(visible(list, f), t -> li(t.text()))))
     * }</pre>
     */
    public static <A, B> jweb.Element live(State<A> a, State<B> b,
                                           BiFunction<? super A, ? super B, ? extends jweb.Element> body) {
        return LiveRegion.of(() -> body.apply(a.get(), b.get()), a, b);
    }

    /**
     * A live region over any number of states: the body reads them itself
     * with {@code get()}, and re-renders when any listed state changes (or,
     * with none listed, on every change in the page).
     *
     * <pre>{@code
     * live(() -> p(first.get() + " " + last.get()), first, last)
     * }</pre>
     */
    public static jweb.Element live(Supplier<? extends jweb.Element> body, State<?>... dependencies) {
        return LiveRegion.of(body, dependencies);
    }

    // ==================== Attribute and class bindings ====================

    /**
     * Binds an attribute's presence to a state's truthiness — see
     * {@link StateBinding#bindAttr}. {@code bind}/{@code bindInput} for the
     * text and value live in {@code jweb.El}.
     */
    public static Attributes bindAttr(State<?> state, String attribute) {
        return StateBinding.bindAttr(state, attribute);
    }

    /** Binds a class's presence to a state's truthiness — see {@link StateBinding#bindClass}. */
    public static Attributes bindClass(State<?> state, String className) {
        return StateBinding.bindClass(state, className);
    }

    // ==================== Legacy ====================

    /**
     * The pre-3.0 reactive region: a {@code <div id=...>} wrapper whose body
     * re-renders on every state change.
     *
     * @deprecated Use {@link #live(State, Function)} — no wrapper, no id to
     *             invent, and it re-renders only when its own state changes.
     */
    @Deprecated
    public static com.osmig.Jweb.framework.core.Element useComponent(
            String componentId, Supplier<? extends jweb.Element> body) {
        LiveRegion region = LiveRegion.withId(componentId, body);
        return region::toVNode;
    }
}
