package com.osmig.Jweb.app.docs.sections.state;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateAdvanced {
    private StateAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Derived State"),
            para("Compute values from other state."),
            codeBlock("""
State<List<Todo>> todos = useState(new ArrayList<>());

// Derived values (recomputed on render)
int total = todos.get().size();
int completed = (int) todos.get().stream()
    .filter(Todo::done).count();
int remaining = total - completed;

div(
    p("Total: " + total),
    p("Completed: " + completed),
    p("Remaining: " + remaining)
)"""),

            h3Title("Shared State"),
            para("Share state between components via constructor."),
            codeBlock("""
// Parent owns the state
public class App implements Template {
    private final State<User> user = useState(null);

    public Element render() {
        return div(
            new Header(user),     // Pass state
            new Content(user),    // Same state
            new Footer(user)      // Same state
        );
    }
}

// Child receives and uses state
public class Header implements Template {
    private final State<User> user;

    public Header(State<User> user) {
        this.user = user;
    }

    public Element render() {
        return header(
            when(user.get() != null,
                () -> span("Welcome, " + user.get().name()))
        );
    }
}"""),

            docTip("State changes trigger automatic re-renders via WebSocket.")
        );
    }
}
