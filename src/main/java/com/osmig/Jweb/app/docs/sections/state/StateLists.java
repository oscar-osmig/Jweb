package com.osmig.Jweb.app.docs.sections.state;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateLists {
    private StateLists() {}

    public static Element render() {
        return section(
            h3Title("State with Lists"),
            para("Manage collections with reactive state."),
            codeBlock("""
State<List<String>> items = useState(new ArrayList<>());

// Add item
items.update(list -> {
    list.add("New item");
    return list;
});

// Remove item
items.update(list -> {
    list.remove(index);
    return list;
});

// Filter items
items.update(list -> list.stream()
    .filter(item -> !item.isEmpty())
    .collect(Collectors.toList()));"""),

            h3Title("Todo List Example"),
            codeBlock("""
record Todo(String text, boolean done) {}

State<List<Todo>> todos = useState(new ArrayList<>());

// Add todo
void addTodo(String text) {
    todos.update(list -> {
        list.add(new Todo(text, false));
        return list;
    });
}

// Toggle todo
void toggleTodo(int index) {
    todos.update(list -> {
        Todo old = list.get(index);
        list.set(index, new Todo(old.text(), !old.done()));
        return list;
    });
}

// Render
ul(each(todos.get(), (todo, i) ->
    li(
        input(attrs().type("checkbox")
            .checked(todo.done())
            .onChange(e -> toggleTodo(i))),
        span(todo.text())
    )
))""")
        );
    }
}
