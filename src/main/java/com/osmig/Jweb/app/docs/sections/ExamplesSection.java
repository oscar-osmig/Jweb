package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ExamplesSection {
    private ExamplesSection() {}

    public static Element render() {
        return section(
            title("Examples"),
            text("Complete examples showing JWeb in action."),

            subtitle("Todo App"),
            code("""
                public class TodoApp implements Template {
                    private final State<List<String>> todos = useState(new ArrayList<>());
                    private final State<String> input = useState("");

                    public Element render() {
                        return div(attrs().style().maxWidth(px(400)).margin(zero, auto).done(),
                            h1("Todo List"),
                            form(attrs().onSubmit(e -> {
                                    e.preventDefault();
                                    if (!input.get().isBlank()) {
                                        todos.update(list -> {
                                            list.add(input.get());
                                            return list;
                                        });
                                        input.set("");
                                    }
                                }),
                                input(attrs()
                                    .type("text")
                                    .value(input.get())
                                    .onInput(e -> input.set(e.value()))),
                                button(type("submit"), "Add")
                            ),
                            ul(each(todos.get(), todo -> li(todo)))
                        );
                    }
                }"""),

            subtitle("User Card"),
            code("""
                public class UserCard implements Template {
                    private final User user;

                    public UserCard(User user) { this.user = user; }

                    public Element render() {
                        return div(attrs().style()
                                .padding(rem(1.5))
                                .backgroundColor(white)
                                .borderRadius(px(8))
                                .boxShadow(px(0), px(2), px(8), rgba(0,0,0,0.1)).done(),
                            h3(user.getName()),
                            p(user.getEmail()),
                            when(user.isAdmin(), span(attrs().style()
                                .backgroundColor(hex("#22c55e"))
                                .color(white)
                                .padding(px(2), px(8))
                                .borderRadius(px(4)).done(), "Admin"))
                        );
                    }
                }""")
        );
    }
}
