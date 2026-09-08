package com.osmig.Jweb.app.docs.sections.state;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateLists {
    private StateLists() {}

    public static Element render() {
        return section(
            h3Title("State with Lists"),
            para("Manage collections with reactive state. Mutating the list in place " +
                 "counts as a change — update() and mutate() both notify."),
            codeBlock("""
State<List<String>> items = useState(new ArrayList<>());

// Add item
items.mutate(list -> list.add("New item"));

// Remove item
items.mutate(list -> list.remove(index));

// Filter items (a new list)
items.update(list -> list.stream()
    .filter(item -> !item.isEmpty())
    .collect(Collectors.toList()));"""),

            h3Title("Todo List Example"),
            para("A list is structural, so it renders inside a live region: the region re-renders " +
                 "on the server whenever the list changes and is morphed into the page — " +
                 "checkboxes keep their focus, nothing else is touched."),
            codeBlock("""
import static jweb.State.*;

record Todo(String text, boolean done) {}

State<List<Todo>> todos = useState(new ArrayList<>());

// Add todo
void addTodo(String text) {
    todos.mutate(list -> list.add(new Todo(text, false)));
}

// Toggle todo
void toggleTodo(int index) {
    todos.mutate(list -> {
        Todo old = list.get(index);
        list.set(index, new Todo(old.text(), !old.done()));
    });
}

// Render — live(state, value -> element) re-renders this on every change
live(todos, list -> ul(each(IntStream.range(0, list.size()).boxed().toList(), i -> {
    Todo todo = list.get(i);
    return li(class_(todo.done() ? "todo done" : "todo"),
        input(type("checkbox"), attrs().checked(todo.done()), onChange(e -> toggleTodo(i))),
        span(todo.text())
    );
})))"""),
            docTip("Without live(...) a list renders once and never updates — state only patches " +
                   "what is bound to it. Text binds with bind(state); everything structural goes in live.")
        );
    }
}
