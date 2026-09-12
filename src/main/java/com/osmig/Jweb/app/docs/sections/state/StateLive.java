package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/** Binding state to the page: bind, bindInput, bindAttr, bindClass and live regions. */
public final class StateLive {
    private StateLive() {}

    public static Element render() {
        return section(
            h3Title("Binding State to Elements"),
            para("Four element arguments keep the page in step with state, with no client code. " +
                 "bind and bindInput come from jweb.El; bindAttr, bindClass and live from jweb.State."),
            codeBlock("""
import static jweb.El.*;
import static jweb.State.*;

State<Integer> clicks = useState(0);
State<Boolean> saving = useState(false);
State<String> name = useState("");

// Text: bind renders the value, and patches it on every change
p("Clicks: ", span(bind(clicks)))

// Input: renders the value, sends every change back — two-way
input(type("text"), bindInput(name))

// Attribute and class, by truthiness
button(bindAttr(saving, "disabled"), onClick(e -> saving.set(true)), "Save")
li(bindClass(saving, "busy"), class_("row"), "Saving...")"""),

            h3Title("Live Regions"),
            para("Anything structural — a list, a conditional, several attributes at once — " +
                 "goes in a live region. The body receives the state's value and returns any " +
                 "element; when that state changes, the server renders the body again and the " +
                 "runtime morphs the result into the page, keeping focus, scroll and typed input."),
            codeBlock("""
record User(String name) {}

State<List<String>> items = useState(new ArrayList<>());
State<User> user = useState();
State<String> filter = useState("all");
State<String> first = useState("");
State<String> last = useState("");

// One state
live(items, list -> ul(each(list, item -> li(item))))

// A conditional whose root element changes
live(user, u -> u == null
    ? a(href("/login"), "Sign in")
    : span("Signed in as " + u.name()))

// Two states
live(items, filter, (list, f) -> ul(each(
    list.stream().filter(i -> f.equals("all") || i.equals(f)).toList(),
    item -> li(item))))

// Any number: the body reads them itself
live(() -> p(first.get() + " " + last.get()), first, last)

// Something to change it
button(onClick(e -> items.mutate(l -> l.add("Item " + (l.size() + 1)))), "Add")"""),
            docList(
                "A region re-renders only when one of its own states changes; with none listed, on every change.",
                "The root element carries data-live=\"live_N\"; a text-only body is wrapped in a span.",
                "Event handlers inside the body are registered again on each render, like the first one.",
                "Regions work inside streamed Suspense blocks — the state and the region belong to the page's context."
            ),
            docTip("Rule of thumb: text → bind, an input → bindInput, one attribute → bindAttr, " +
                   "one class → bindClass, everything else → live.")
        );
    }
}
