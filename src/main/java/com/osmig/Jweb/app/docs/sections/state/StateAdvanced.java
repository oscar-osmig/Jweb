package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateAdvanced {
    private StateAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Derived State"),
            para("Compute values from other state."),
            codeBlock("""
record Todo(String text, boolean done) {}

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
record User(String name) {}

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
}

// Content and Footer follow the same shape as Header
class Content extends Header { Content(State<User> user) { super(user); } }
class Footer extends Header { Footer(State<User> user) { super(user); } }"""),

            docTip("State changes travel over the WebSocket: bound text, attributes and classes are " +
                   "patched, and live(...) regions are re-rendered on the server and morphed in.")
        );
    }
}
