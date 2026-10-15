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

## PubSub: One Publish, Every Page

`jweb.PubSub` is the server-started half of the event loop: publish a message to a topic and
every page that subscribed re-renders what the message touched. A subscription made during a
render belongs to that page's context — the handler runs in it, the live regions reading the
states it changed are re-rendered, and the patches go down the page's socket. It ends with
the context. Subscribe once per page, in `render()`, not inside a `live` body.

```java
import jweb.PubSub;
import static jweb.El.*;
import static jweb.State.*;

record Post(String author, String text) {}
String room = "42";

State<List<Post>> posts = useState(new ArrayList<>());
PubSub.subscribe("room:" + room, Post.class, post -> posts.mutate(list -> list.add(post)));
Element wall = live(posts, list -> ul(each(list, p -> li(p.author() + ": " + p.text()))));

PubSub.publish("room:" + room, new Post("ada", "hello"));   // from anywhere
```

Outside a render a subscription is a plain listener until `close()`. Messages are JSON on
the wire and arrive parsed to the class asked for; `subscribe(topic, Consumer<String>)` gets
the JSON text. Delivery runs on one thread in publish order; a handler that throws is logged.

```java
import jweb.PubSub;

record Order(long id, String status) {}

PubSub.Subscription s = PubSub.subscribe("orders", Order.class, o -> System.out.println(o.id()));
s.close();
int n = PubSub.subscribers("orders");
```

`PubSub.Memory` (default) reaches this instance's pages. `jweb.RedisPubSub` reaches every
instance through Redis with no client library: set `jweb.pubsub.redis.url`
(`redis://[user[:password]@]host[:port]`, `rediss://` for TLS; `jweb.pubsub.redis.prefix`,
default `jweb:`). A `PubSub.Backend` bean replaces both. In a test, `RenderedPage.awaitPush()`
returns what a publish sent the page.

## Shared Cache and Locks

`Cache.create`/`Cache.named` live in this instance's memory. `Cache.shared(name, Type.class)`
is what every instance sees — a Redis when `jweb.cache.redis.url` (or `jweb.redis.url`) is
set, spoken by the framework itself; memory otherwise, same semantics.

```java
import jweb.Cache;

record Price(String sku, long cents) {}
static Price lookup(String sku) { return new Price(sku, 100); }
static void sendDigest() {}

Cache.Shared<Price> prices = Cache.shared("prices", Price.class, Duration.ofMinutes(10));
Price p = prices.getOrSet("sku-9", () -> lookup("sku-9"));
prices.set("sku-9", p);  prices.get("sku-9");  prices.delete("sku-9");  prices.clear();
Cache.shared("counts", Long.class).increment("signups", 1, Duration.ofDays(1));

boolean ranHere = Cache.once("digest", Duration.ofMinutes(5), () -> sendDigest());   // one instance per five minutes
try (Cache.Lock lock = Cache.lock("import", Duration.ofMinutes(1)).orElseThrow()) {   // held until closed or expired
    // ...
}
```

Values travel as JSON (a record, a bean, a String, a Number). A store that cannot be reached
is a miss, a no-op, or "not acquired" — logged, never a 500. `RateLimit` counts in the shared
store on its own once one is configured (`.shared(false)` keeps a limiter local).

```yaml
jweb:
  redis:
    url: ${REDIS_URL:}          # cache and PubSub together; or jweb.cache.redis.url / jweb.pubsub.redis.url apart
  cache:
    redis:
      prefix: "jweb:cache:"
      connections: 4
```
