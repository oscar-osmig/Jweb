package com.osmig.Jweb.app.docs.sections.forms;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FormsEvents {
    private FormsEvents() {}

    public static Element render() {
        return section(
            h3Title("Event Handlers"),
            para("Handle form events with state."),
            codeBlock("""
State<String> searchTerm = useState("");
State<String> email = useState("");

// Input event (fires on every keystroke)
input(
    type("text"),
    value(searchTerm.get()),
    onInput(e -> searchTerm.set(e.value())))

// Change event (fires on blur)
input(
    type("email"),
    value(email.get()),
    onChange(e -> {
        email.set(e.value());
        validateEmail(e.value());
    }))

// Focus and blur
input(
    type("text"),
    onFocus(e -> showHint()),
    onBlur(e -> hideHint()))"""),

            h3Title("Form Submit"),
            codeBlock("""
State<String> name = useState("");
State<String> email = useState("");
State<Boolean> loading = useState(false);

form(onSubmit(e -> {
    e.preventDefault();
    loading.set(true);

    // Submit data
    submitForm(name.get(), email.get())
        .thenRun(() -> loading.set(false));
}),
    input(type("text"), onInput(e -> name.set(e.value()))),
    input(type("email"), onInput(e -> email.set(e.value()))),
    button(type("submit"), attrs().disabled(loading.get()),
        loading.get() ? "Submitting..." : "Submit")
)""")
        );
    }
}
