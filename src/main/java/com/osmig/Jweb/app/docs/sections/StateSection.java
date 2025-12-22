package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateSection {
    private StateSection() {}

    public static Element render() {
        return section(
            title("State Management"),
            text("Reactive state with useState hook."),

            subtitle("Creating State"),
            code("""
                import static com.osmig.Jweb.framework.state.StateHooks.*;

                public class Counter implements Template {
                    private final State<Integer> count = useState(0);

                    public Element render() {
                        return div(
                            h1("Count: " + count.get()),
                            button(attrs().onClick(e -> count.set(count.get() + 1)),
                                "Increment")
                        );
                    }
                }"""),

            subtitle("Reading & Updating"),
            code("""
                State<String> name = useState("John");

                // Read
                String current = name.get();

                // Set directly
                name.set("Jane");

                // Update based on current value
                State<Integer> count = useState(0);
                count.update(c -> c + 1);"""),

            subtitle("State with Lists"),
            code("""
                State<List<Todo>> todos = useState(new ArrayList<>());

                // Add item
                todos.update(list -> {
                    list.add(new Todo("New task"));
                    return list;
                });

                // Remove item
                todos.update(list -> {
                    list.removeIf(t -> t.getId().equals(id));
                    return list;
                });""")
        );
    }
}
