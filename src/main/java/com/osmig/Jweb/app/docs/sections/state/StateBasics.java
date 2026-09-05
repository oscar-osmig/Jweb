package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateBasics {
    private StateBasics() {}

    public static Element render() {
        return section(
            h3Title("Creating State"),
            para("Use useState to create reactive state in components."),
            codeBlock("""
import static jweb.State.*;

public class Counter implements Template {
    private final State<Integer> count = useState(0);

    public Element render() {
        return div(
            h1("Count: " + count.get()),
            button(onClick(e -> count.set(count.get() + 1)), "Increment"),
            button(onClick(e -> count.set(count.get() - 1)), "Decrement")
        );
    }
}"""),

            h3Title("Reading State"),
            para("Use get() to read the current value."),
            codeBlock("""
State<String> name = useState("John");
State<Integer> age = useState(25);
State<Boolean> active = useState(true);

// Read values
String currentName = name.get();
int currentAge = age.get();
boolean isActive = active.get();

// Use in templates
p("Name: " + name.get())
p("Age: " + age.get())
when(active.get(), () -> span("Active"))""")
        );
    }
}
