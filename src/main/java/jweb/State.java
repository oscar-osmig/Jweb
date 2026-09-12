package jweb;

/**
 * Server-driven reactive state — {@code useState}, {@code live},
 * {@code bindAttr}, {@code bindClass} and friends. The hooks live here; the
 * value they hand out is {@link jweb.state.State jweb.state.State&lt;T&gt;},
 * which needs its own import because this facade owns the simple name
 * {@code State} in the {@code jweb} package:
 *
 * <pre>{@code
 * import jweb.state.State;         // the value type — explicit, even next to import jweb.*
 * import static jweb.State.*;      // the hooks
 * import static jweb.El.*;
 *
 * State<Integer> clicks = useState(0);
 * State<List<String>> items = useState(new ArrayList<>());
 *
 * p("Clicks: ", span(bind(clicks)))                    // bind renders the value
 * live(items, list -> ul(each(list, i -> li(i))))      // re-rendered on change
 * button(bindAttr(busy, "disabled"), onClick(e -> ...), "Add")
 * }</pre>
 *
 * <p>Short alias for the legacy
 * {@code com.osmig.Jweb.framework.state.StateHooks} entry point.</p>
 *
 * @see jweb.state.State
 */
@SuppressWarnings("deprecation")
public class State extends com.osmig.Jweb.framework.state.StateHooks {

    protected State() {}
}
