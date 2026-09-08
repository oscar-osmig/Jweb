package jweb;

/**
 * Server-driven reactive state — {@code useState}, {@code live},
 * {@code bindAttr}, {@code bindClass} and friends:
 *
 * <pre>{@code
 * import static jweb.State.*;
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
 */
@SuppressWarnings("deprecation")
public class State extends com.osmig.Jweb.framework.state.StateHooks {

    protected State() {}
}
