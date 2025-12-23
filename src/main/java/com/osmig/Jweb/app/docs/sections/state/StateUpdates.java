package com.osmig.Jweb.app.docs.sections.state;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateUpdates {
    private StateUpdates() {}

    public static Element render() {
        return section(
            h3Title("Setting State"),
            para("Use set() to replace the value, update() to transform it."),
            codeBlock("""
State<String> name = useState("John");
State<Integer> count = useState(0);

// Direct set
name.set("Jane");
count.set(10);

// Update based on current value
count.update(c -> c + 1);
count.update(c -> c * 2);

// Toggle boolean
State<Boolean> visible = useState(false);
visible.update(v -> !v);"""),

            h3Title("Batch Updates"),
            para("Multiple updates in one event handler are batched."),
            codeBlock("""
State<Integer> x = useState(0);
State<Integer> y = useState(0);

button(attrs().onClick(e -> {
    x.set(100);  // Batched
    y.set(200);  // Batched
    // Single re-render after both updates
}), text("Move"))"""),

            h3Title("Conditional Updates"),
            codeBlock("""
State<Integer> count = useState(0);

// Only update if condition met
button(attrs().onClick(e -> {
    if (count.get() < 10) {
        count.update(c -> c + 1);
    }
}), text("Increment (max 10)"))

// Reset to initial
button(attrs().onClick(e -> count.set(0)), text("Reset"))""")
        );
    }
}
