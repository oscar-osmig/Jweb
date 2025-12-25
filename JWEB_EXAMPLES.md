# JWeb DSL Examples: Simple to Advanced

## Required Imports
```java
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.state.StateHooks.*;
```

---

## Level 1: Basic Elements![img.png](img.png)

```java
// Plain text
h1("Hello World")

// With single attribute
div(id("main"), "Content here")

// Multiple attributes via shortcuts
p(class_("intro"), id("welcome"), "Welcome to JWeb!")

// Nested elements
div(
    h1("Title"),
    p("Paragraph text")
)
```

---

## Level 2: Attributes Builder

```java
// Using attrs() for multiple attributes
div(attrs().id("card").class_("card featured"),
    h2("Card Title"),
    p("Card content")
)

// Form with attributes
input(attrs()
    .type("email")
    .name("email")
    .placeholder("you@example.com")
    .required())

// Links with target blank (auto-adds rel="noopener noreferrer")
a(attrs().href("/about").targetBlank(), "About Us")
```

---

## Level 3: Inline Styles

```java
// Style via attrs().style()
div(attrs()
    .class_("box")
    .style(s -> s.padding(px(20)).backgroundColor(white)),
    p("Styled content")
)

// Fluent style chaining with .done()
div(attrs().style()
        .display(flex)
        .justifyContent(center)
        .alignItems(center)
        .gap(rem(1))
    .done(),
    span("Centered content")
)

// Direct InlineStyle usage (no .done() needed)
div(attrs().style()
        .flexCol()
        .padding(rem(2))
        .backgroundColor(hex("#f5f5f5"))
        .borderRadius(px(8)),
    h2("Card"),
    p("Content")
)
```

---

## Level 4: Collections & Conditionals

```java
// List iteration with each()
List<String> items = List.of("Apple", "Banana", "Cherry");

ul(each(items, item -> li(item)))

// Conditional rendering with when()
boolean isLoggedIn = true;

div(
    when(isLoggedIn, () -> span("Welcome back!")),
    when(!isLoggedIn, () -> a("/login", "Sign In"))
)

// If-else rendering
ifElse(isLoggedIn,
    () -> button("Logout"),
    () -> button("Login")
)

// Combining iteration with conditionals
ul(each(users, user ->
    li(
        span(user.getName()),
        when(user.isAdmin(), () -> span(class_("badge"), "Admin"))
    )
))
```

---

## Level 5: Event Handlers

```java
// Click handler
button(attrs()
    .class_("btn")
    .onClick(e -> System.out.println("Clicked!")),
    "Click Me"
)

// Form submission
form(attrs()
    .action("/submit")
    .method("POST")
    .onSubmit(e -> {
        e.preventDefault();
        handleSubmit(e.formData());
    }),
    input(attrs().type("text").name("username")),
    button(type("submit"), "Submit")
)

// Input change handler
input(attrs()
    .type("text")
    .onInput(e -> searchTerm.set(e.value()))
    .onKeyDown(e -> {
        if ("Enter".equals(e.key())) doSearch();
    })
)
```

---

## Level 6: Reactive State

```java
public class CounterPage implements Page {
    private final State<Integer> count = useState(0);

    @Override
    public Element render() {
        return div(
            h1("Counter: " + count.get()),
            button(attrs().onClick(e -> count.set(count.get() + 1)), "+"),
            button(attrs().onClick(e -> count.set(count.get() - 1)), "-"),
            button(attrs().onClick(e -> count.set(0)), "Reset")
        );
    }
}
```

---

## Level 7: Computed State & Effects

```java
public class ShoppingCart implements Page {
    private final State<List<Item>> items = useState(new ArrayList<>());
    private final State<Double> total = useComputed(
        () -> items.get().stream()
            .mapToDouble(Item::getPrice)
            .sum(),
        items  // dependency
    );

    {
        // Side effect: log when total changes
        useEffect(() -> {
            System.out.println("Total updated: $" + total.get());
        }, total);
    }

    @Override
    public Element render() {
        return div(
            h2("Shopping Cart"),
            ul(each(items.get(), item ->
                li(item.getName() + " - $" + item.getPrice())
            )),
            p(strong("Total: $" + total.get()))
        );
    }
}
```

---

## Level 8: Reusable Components

```java
// Simple component
public class Badge implements Component {
    private final String text;
    private final String color;

    public Badge(String text, String color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public Element render() {
        return span(attrs().style()
                .padding(px(4), px(8))
                .borderRadius(px(4))
                .backgroundColor(hex(color))
                .color(white)
                .fontSize(rem(0.75)),
            text
        );
    }
}

// Usage
div(
    new Badge("New", "#22c55e"),
    new Badge("Sale", "#ef4444")
)
```

---

## Level 9: Component with Props & Children

```java
public class Card implements Component {
    private final String title;
    private final Element[] children;

    public Card(String title, Element... children) {
        this.title = title;
        this.children = children;
    }

    @Override
    public Element render() {
        return div(attrs().style()
                .padding(rem(1.5))
                .backgroundColor(white)
                .borderRadius(px(8))
                .boxShadow(px(0), px(2), px(8), rgba(0, 0, 0, 0.1)),
            h3(attrs().style().marginBottom(rem(1)), title),
            fragment(children)
        );
    }

    // Factory method for fluent API
    public static Card card(String title, Element... children) {
        return new Card(title, children);
    }
}

// Usage
card("User Profile",
    p("Name: John Doe"),
    p("Email: john@example.com"),
    button("Edit Profile")
)
```

---

## Level 10: Advanced Styling with CSS DSL

```java
// Stylesheet rules
String styles = styles(
    rule("*").boxSizing(borderBox),

    rule("body")
        .margin(zero)
        .fontFamily("system-ui, sans-serif")
        .backgroundColor(hex("#f8fafc")),

    rule(".btn")
        .padding(px(12), px(24))
        .border(px(0), solid, transparent)
        .borderRadius(px(6))
        .cursor(pointer)
        .transition(propAll, s(0.2), ease),

    rule(cls("btn").hover())
        .transform(translateY(px(-2)))
        .boxShadow(px(0), px(4), px(12), rgba(0, 0, 0, 0.15)),

    rule(".btn-primary")
        .backgroundColor(hex("#3b82f6"))
        .color(white),

    rule(cls("btn-primary").hover())
        .backgroundColor(hex("#2563eb"))
);

// Responsive grid
rule(".grid")
    .display(grid)
    .gridTemplateColumns(repeat(autoFill(), minmax(px(250), fr(1))))
    .gap(rem(1.5))
```

---

## Level 11: Full Interactive Page

```java
public class TodoApp implements Page {
    private final State<List<Todo>> todos = useState(new ArrayList<>());
    private final State<String> newTodo = useState("");
    private final State<String> filter = useState("all");

    @Override
    public Element render() {
        List<Todo> filtered = todos.get().stream()
            .filter(t -> switch(filter.get()) {
                case "active" -> !t.done();
                case "completed" -> t.done();
                default -> true;
            })
            .toList();

        return div(attrs().style()
                .maxWidth(px(600))
                .margin(zero, auto)
                .padding(rem(2)),

            h1(attrs().style().textCenter(), "Todo App"),

            // Input form
            form(attrs()
                .onSubmit(e -> {
                    e.preventDefault();
                    if (!newTodo.get().isBlank()) {
                        todos.update(list -> {
                            list.add(new Todo(newTodo.get()));
                            return list;
                        });
                        newTodo.set("");
                    }
                }),
                div(attrs().style().display(flex).gap(rem(0.5)),
                    input(attrs()
                        .type("text")
                        .value(newTodo.get())
                        .placeholder("What needs to be done?")
                        .onInput(e -> newTodo.set(e.value()))
                        .style(s -> s.flex(1, 1, auto).padding(px(12)))),
                    button(type("submit"), "Add")
                )
            ),

            // Filter buttons
            div(attrs().style().display(flex).gap(rem(0.5)).marginY(rem(1)),
                filterBtn("all", "All"),
                filterBtn("active", "Active"),
                filterBtn("completed", "Completed")
            ),

            // Todo list
            ul(attrs().style().listStyle(none).padding(zero),
                each(filtered, todo ->
                    li(attrs().style()
                            .display(flex)
                            .alignItems(center)
                            .padding(rem(0.75))
                            .borderBottom(px(1), solid, hex("#e5e7eb")),
                        input(attrs()
                            .type("checkbox")
                            .checked(todo.done())
                            .onChange(e -> toggleTodo(todo))),
                        span(attrs().style()
                                .flex(1, 1, auto)
                                .marginLeft(rem(0.75))
                                .textDecoration(todo.done() ? lineThrough : none),
                            todo.text()),
                        button(attrs()
                            .onClick(e -> removeTodo(todo))
                            .style(s -> s.color(red).cursor(pointer)),
                            "x")
                    )
                )
            ),

            // Footer
            p(attrs().style().color(gray).fontSize(rem(0.875)),
                filtered.size() + " items")
        );
    }

    private Element filterBtn(String value, String label) {
        boolean active = filter.get().equals(value);
        return button(attrs()
            .onClick(e -> filter.set(value))
            .style(s -> s
                .padding(px(8), px(16))
                .border(px(1), solid, active ? hex("#3b82f6") : hex("#d1d5db"))
                .backgroundColor(active ? hex("#3b82f6") : white)
                .color(active ? white : hex("#374151"))
                .borderRadius(px(4))
                .cursor(pointer)),
            label
        );
    }

    private void toggleTodo(Todo todo) {
        todos.update(list -> {
            list.stream()
                .filter(t -> t.equals(todo))
                .forEach(t -> t.setDone(!t.done()));
            return list;
        });
    }

    private void removeTodo(Todo todo) {
        todos.update(list -> {
            list.remove(todo);
            return list;
        });
    }
}
```

---

## Level 12: Templates with Layout

```java
public class MainLayout implements Template {
    @Override
    public Element render(Element... content) {
        return html(
            head(
                title("My App"),
                meta("viewport", "width=device-width, initial-scale=1"),
                css("/styles.css")
            ),
            body(attrs().style()
                    .minHeight(vh(100))
                    .display(flex)
                    .flexDirection(column),

                // Header
                header(attrs().style()
                        .padding(rem(1))
                        .backgroundColor(hex("#1e293b"))
                        .color(white),
                    nav(attrs().style().flexBetween().maxWidth(px(1200)).margin(zero, auto),
                        a(attrs().href("/").style().color(white).textDecoration(none),
                            strong("MyApp")),
                        div(attrs().style().display(flex).gap(rem(1)),
                            a("/features", "Features"),
                            a("/pricing", "Pricing"),
                            a("/about", "About")
                        )
                    )
                ),

                // Main content
                main(attrs().style().flex(1, 1, auto).padding(rem(2)),
                    div(attrs().style().maxWidth(px(1200)).margin(zero, auto),
                        fragment(content)
                    )
                ),

                // Footer
                footer(attrs().style()
                        .padding(rem(2))
                        .backgroundColor(hex("#f1f5f9"))
                        .textAlign(center),
                    p("© 2024 MyApp. All rights reserved.")
                )
            )
        );
    }
}

// Usage in a Page
public class HomePage implements Page {
    @Override
    public Element render() {
        return new MainLayout().render(
            section(
                h1("Welcome to MyApp"),
                p("Build amazing web applications with Java.")
            )
        );
    }
}
```

---

## Summary Table

| Level | Features |
|-------|----------|
| 1 | Basic elements, text, nesting |
| 2 | `attrs()` builder, form attributes |
| 3 | Inline styles with `style()` |
| 4 | `each()`, `when()`, `ifElse()` |
| 5 | Event handlers (`onClick`, `onSubmit`) |
| 6 | Reactive `useState()` |
| 7 | `useComputed()`, `useEffect()` |
| 8 | Reusable `Component` classes |
| 9 | Components with props & children |
| 10 | CSS DSL: `rule()`, selectors, transitions |
| 11 | Full interactive pages |
| 12 | Templates with layouts |
