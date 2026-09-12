# State Management

JWeb provides React-like reactive state management with `State<T>`.

## Basic Usage

```java
import jweb.state.State;
import static jweb.State.*;

public class Counter implements Template {
    private final State<Integer> count = useState(0);

    @Override
    public Element render() {
        return div(
            h1("Count: " + count.get()),
            button(onClick(e -> count.update(c -> c + 1)), "Increment")
        );
    }
}
```

## State Operations

### Get Value

```java
State<Integer> count = useState(0);
int value = count.get();  // Returns 0
```

### Set Value

```java
State<Integer> count = useState(0);
count.set(5);  // Sets to 5
```

### Update Based on Current Value

```java
State<Integer> count = useState(0);
count.update(c -> c + 1);  // Increment by 1
count.update(c -> c * 2);  // Double the value
```

## State Types

### Primitive States

```java
State<Integer> count = useState(0);
State<String> name = useState("John");
State<Boolean> isVisible = useState(false);
State<Double> price = useState(9.99);
```

### Object States

```java
record User(String name, String email) {}

State<User> currentUser = useState(new User("John", "john@example.com"));

// Update object
currentUser.set(new User("Jane", "jane@example.com"));

// Access properties
String email = currentUser.get().email();
```

### List States

```java
State<List<String>> items = useState(new ArrayList<>());

// Add item
items.update(list -> {
    list.add("New Item");
    return list;
});

// Remove item
items.update(list -> {
    list.remove("Old Item");
    return list;
});
```

### Map States

```java
State<Map<String, Object>> settings = useState(new HashMap<>());

settings.update(map -> {
    map.put("theme", "dark");
    return map;
});
```

## Subscribers

React to state changes:

```java
State<Integer> count = useState(0);

count.subscribe(newValue -> {
    System.out.println("Count changed to: " + newValue);
});

count.set(5);  // Prints: "Count changed to: 5"
```

### Unsubscribe

```java
State<Integer> count = useState(0);

Consumer<Integer> listener = value -> System.out.println(value);
count.subscribe(listener);

// Later...
count.unsubscribe(listener);
```

## State in Components

```java
public class TodoList implements Template {
    private final State<List<String>> todos = useState(new ArrayList<>());
    private final State<String> newTodo = useState("");

    @Override
    public Element render() {
        return div(
            h1("Todo List"),

            // Input for new todo
            form(onSubmit(e -> { e.preventDefault(); addTodo(); }),
                input(
                    type("text"),
                    value(newTodo.get()),
                    onInput(e -> newTodo.set(e.value()))
                ),
                button(type("submit"), "Add")
            ),

            // List of todos
            ul(
                each(todos.get(), todo ->
                    li(
                        span(todo),
                        button(onClick(e -> removeTodo(todo)), "Remove")
                    )
                )
            )
        );
    }

    public void addTodo() {
        if (!newTodo.get().isEmpty()) {
            todos.update(list -> {
                list.add(newTodo.get());
                return list;
            });
            newTodo.set("");
        }
    }

    public void removeTodo(String todo) {
        todos.update(list -> {
            list.remove(todo);
            return list;
        });
    }
}
```

## State Manager

The `StateManager` handles state lifecycle:

```java
// Create named state
State<Integer> count = StateManager.createState("counter", 0);

// Get all states for serialization
var allStates = StateManager.getContext().getStates();
```

## State Serialization

States can be serialized to JSON for hydration:

```java
record User(String name, String email) {}

State<User> user = useState(new User("John", "john@example.com"));

// Serialize to JSON
String json = user.toJson();
// {"id":"state_1","value":{"name":"John","email":"john@example.com"}}
```

## Dirty Tracking

Check if state has changed since last render:

```java
State<Integer> count = useState(0);

count.isDirty();  // false
count.set(5);
count.isDirty();  // true
```

## Best Practices

### 1. Initialize State at Field Level

```java
public class MyComponent implements Template {
    // Good: Initialize at field level
    private final State<Integer> count = useState(0);

    @Override
    public Element render() {
        return div(h1("Count: " + count.get()));
    }
}
```

### 2. Use Immutable Updates for Objects

```java
class User {
    private String name;
    private final String email;
    User(String name, String email) { this.name = name; this.email = email; }
    String getName() { return name; }
    void setName(String name) { this.name = name; }
    String getEmail() { return email; }
}

State<User> user = useState(new User("John", "john@example.com"));

// Instead of mutating...
user.get().setName("Jane");  // Bad: no change detection

// Create new object
user.set(new User("Jane", user.get().getEmail()));  // Good
```

### 3. Keep State Minimal

```java
List<String> items = List.of("a", "b");

// Store only what you need
State<String> userId = useState("123");  // Good: just the ID

// Avoid storing derived data
State<Boolean> hasItems = useState(items.size() > 0);  // Bad: derive from items
```

### 4. Group Related State

```java
record FormData(String firstName, String lastName, String email) {}

// Instead of multiple states...
State<String> firstName = useState("");
State<String> lastName = useState("");
State<String> email = useState("");

// Use an object
State<FormData> form = useState(new FormData("", "", ""));
```
