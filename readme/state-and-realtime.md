[← Back to README](./../README.md)

# State & Realtime

This document covers reactive state, server-side events, hydration, WebSocket/SSE transport,
and the interactive UI utilities (transitions, portals, refs, toasts, suspense).

> ✅ **The reactive round-trip works end-to-end**: the client runtime is auto-injected into
> rendered pages (disable with `jweb.runtime.enabled: false`), render contexts survive until a
> TTL reaper collects them (5 min idle, refreshed by WebSocket activity), and `live(...)`
> regions re-render on the server and morph into the DOM. See
> [Known Issues](./known-issues.md) for the sharp edges.

## Reactive State

### Creating state

```java
import jweb.state.State;                                // the state value type
import static jweb.State.*;                             // useState & friends (alias of StateHooks)
import jweb.StateManager;

// The constructor is internal — create state through the hooks/manager:
State<Integer> count = useState(0);
State<String>  name  = useState();                      // null initial
State<Integer> other = StateManager.createState(0);     // equivalent
State<Integer> named = StateManager.createState("cart-count", 0);
```

> ⚠️ `State.of(...)` does **not** exist (older docs claimed it did).

### Using state

```java
class Log { static Logger framework() { return new Logger(); } static class Logger { void info(String f, Object... a) {} } }
State<Integer> count = useState(0);
State<List<Integer>> items = useState(new ArrayList<>());
int x = 1;

count.get();                 // read
count.set(5);                // write (no-op if value unchanged); marks dirty; notifies
count.update(n -> n + 1);    // transform — returning the same instance (a list you added to) still notifies
items.mutate(l -> l.add(x)); // mutate in place and notify
count.subscribe(v -> Log.framework().info("count is now {}", v));
count.getId();               // "state_<n>" — used by client bindings
count.toJson();              // {"id":"state_1","value":5}
```

### Hooks

```java
class Log { static Logger framework() { return new Logger(); } static class Logger { void info(String f, Object... a) {} } }
State<Integer> price = useState(10);
State<Integer> qty = useState(2);

// Computed state — re-evaluates whenever a dependency changes
State<Integer> total = useComputed(
    () -> price.get() * qty.get(), price, qty);

// Effect — runs immediately AND on every dependency change (no cleanup fn, no diffing)
useEffect(() -> Log.framework().info("qty changed"), qty);
```

### Contexts

`StateManager` scopes states per request in a `StateContext` (ThreadLocal + a registry keyed by
`ctx_<uuid>` — unguessable). `JWebController` creates a context per router request, renders,
serializes changed state into the hydration payload, then detaches the ThreadLocal — the
registry entry survives so WebSocket events can restore it. Contexts idle longer than
5 minutes are reaped by a background cleanup task (WebSocket activity refreshes the TTL).

### Binding state to elements

Four element arguments keep the DOM in step with state, with no client code written.
`bind`/`bindInput` live in `jweb.El`; `bindAttr`/`bindClass`/`live` in `jweb.State` — the two
wildcards coexist.

```java
import static jweb.El.*;
import static jweb.State.*;

record Person(String name) {}
record Todo(String text) {}
List<Todo> visible(List<Todo> list, String f) { return list; }

State<Integer> clicks = useState(0);
State<String> name = useState("");
State<Boolean> saving = useState(false);
State<Boolean> active = useState(true);
Runnable save = () -> saving.set(false);
State<List<Integer>> items = useState(new ArrayList<>());
State<Person> user = useState(new Person("Ada"));
State<List<Todo>> todos = useState(new ArrayList<>());
State<String> filter = useState("all");
State<String> first = useState("Ada");
State<String> last = useState("Lovelace");

// Text: bind renders the value AND patches it on every change
p("Clicks: ", span(bind(clicks)))               // <span data-state-bind="state_1">0</span>
span(bind(clicks), "Total: " + clicks.get())    // other content? then bind is just the attribute

// Input: value (or checked) rendered, and every keystroke sent back — two-way
input(type("text"), bindInput(name))

// Attribute and class, by truthiness (true / non-zero / non-empty set it; false / 0 / "" / null clear it)
button(bindAttr(saving, "disabled"), onClick(e -> save.run()), "Save")
li(bindClass(active, "on"), class_("tab"), "Home")   // put bindClass before class_(...)

// A region: re-rendered on the server from the state, morphed into the page
live(items, list -> ul(each(list, i -> li(i))))
live(user, u -> u == null ? a(href("/login"), "Sign in") : span("Hi " + u.name()))
live(todos, filter, (list, f) -> ul(each(visible(list, f), t -> li(t.text()))))
live(() -> p(first.get() + " " + last.get()), first, last)   // any number of states
```

**Live regions** are the answer to lists, conditionals and anything structural: the body
receives the current value and returns any element; its root gets a `data-live="live_N"`
attribute. When an event changes one of the region's states, the server re-renders the body
and ships the HTML as a `domUpdate` patch; the runtime **morphs** it into the existing element
— unchanged nodes stay, so focus, scroll position and typed input survive. A region only
re-renders for the states it was given (none listed = every change). Handlers inside the body
are re-registered on each render, like the first one. `useComponent(id, supplier)` is the
deprecated pre-3.0 form (a `<div id>` wrapper that re-renders on any change).

The client contract, for reference:

| Attribute | Behavior on state change |
|-----------|--------------------------|
| `data-state-bind="state_1"` (also legacy `data-state`) | text or value updated |
| `data-state-input="true"` + `data-state-bind` | input sends `setState` on every change |
| `data-state-attr="disabled=state_1"` | attribute set while truthy, removed otherwise |
| `data-state-class="on=state_1"` | class added while truthy, removed otherwise |
| `data-live="live_1"` | element morphed with the server's re-render |
| `data-state-text="on:off"` | picks text by truthiness |
| `data-state-toggle="state_1"` | toggles the `toggle-on` class |

`bind(state)` / `bindInput(state)` (in `jweb.El`) are element arguments carrying
`data-state-bind`; `bind(state)` renders the value itself, so a live counter is one line:

```java
State<Integer> clicks = useState(0);

p("Clicks: ", span(bind(clicks)))
button(onClick(e -> clicks.update(n -> n + 1)), "Click me")
```

A `jweb:stateChange` CustomEvent fires on every patch. The JS DSL reads the client's copy
of a state and listens for those patches without touching the event by hand:

```java
import static jweb.Js.*;

State<Integer> clicks = useState(0);

syncState(clicks)                                     // a Val — JWeb.getState('state_1')
button(onClick(setText("total", syncState(clicks).plus(1))), "+1")
onStateChange(clicks, callback("now", "before").log(v("now")))   // an Action
```

Events: `jweb:stateChange` fires on every state patch, `jweb:liveUpdate` after a region morphs.
The whole protocol lives in one runtime function (`initLive`) and one server class
(`StateBinding` + `LiveRegion`).

## Server-Side Events (`events/`)

Attach Java lambdas to DOM events; the framework registers them and renders a JS call:

```java
State<Integer> count = useState(0);

button(onClick(e -> count.update(n -> n + 1)), "Increment")
// renders: <button onclick="JWeb.call('h_1_9f3c2a…', event)">Increment</button>
```

- `EventRegistry.register(type, Consumer<Event>)` stores the handler under an unguessable id
  (`h_<n>_<random>` — counter for uniqueness, random hex suffix).
- The `Event` interface exposes: `value()`, `targetId()`, `type()`, `key()`, `keyCode()`,
  modifier keys, `clientX/Y()`, `checked()`, `formData()`, `data(name)`/`dataset()`.
- `preventDefault()`/`stopPropagation()` set flags on the server-side `DomEvent`; form submits
  are always prevented client-side before sending.
- Registration is **context-scoped when a render context is active** (the normal case):
  handlers get unguessable IDs (`h_<n>_<random>`), live in the context's namespace, and are
  evicted when the context dies. Outside a render they register globally (static export).
- **Handler ids are capabilities.** A WebSocket message that names a context can only run
  that context's handlers — there is no fallback to the global registry — and a message for
  a context that no longer exists is logged and dropped (the client sees a
  `context_expired` error and should reload). Only messages that name no context reach the
  global registry. See [Known Issues](./known-issues.md#the-handler-capability-model).
- The client populates `formData` for submits and `dataset` for every event
  (`Event.data("userId")` reads `data-user-id`).

## Hydration (`hydration/`)

For every `Element` response, the controller injects three scripts before `</body>`
(prefetch, hydration data, and the JWeb client runtime):

1. the `Prefetch` hover-prefetch script (external, cached `/jweb/prefetch.js`),
2. `<script id="__JWEB_DATA__" type="application/json">{"contextId":"ctx_...","vnode":null,"state":[{"id":"state_1","value":0}],"handlers":[]}</script>` (inline, per request), and
3. the JWeb client runtime (external, cached `/jweb/runtime.js`).

`HydrationData.builder()` supports `vnode(...)`/`handlers(...)` too, but the controller
currently populates only `contextId` + `states`. `VNodeSerializer` (VNode → JSON:
`{"type":"element","tag":...,"attrs":...,"children":[...]}`) is wired into
`HydrationData.builder().vnode(...)`, but no caller populates a vnode yet.

The consumer of this payload is `JWebRuntime` (`js/JWebRuntime.java`): it defines the global
`JWeb` object (`JWeb.init`, `JWeb.call`, WebSocket connect with reconnect + 30s ping) and reads
`__JWEB_DATA__` on `DOMContentLoaded`. It is **auto-injected** into rendered pages; opt out
with `jweb.runtime.enabled: false` (then `JWebRuntime.getScriptTag()` lets you inject it
manually, e.g. for pages rendered outside the controller).

## WebSocket (`websocket/`)

- `JWebSocketConfig` registers `JWebSocketHandler` at **`/jweb`**. Origins default to
  **same-origin only**; allow others with `jweb.websocket.allowed-origins` (comma-separated,
  `*` for dev).
- Message protocol (JSON, `type` discriminator):
  - client → server: `event` (handler id, contextId, event payload, formData), `init`
    (contextId), `setState` (id, value), `ping`
  - server → client: `connected`, `stateUpdate` (`[{id,value}]`), `domUpdate`
    (`[{id,html}]`), `eventHandled`, `initState`, `pong`, `error`
- On an `event` message the handler restores the `StateContext` by contextId, executes the
  registered handler (in that context only), collects `getChangedStates()`, and pushes
  `stateUpdate` back, followed by one `domUpdate` carrying the re-rendered HTML of every
  `live` region that depends on a changed state (`[{id:"live_1",html:"..."}]`; the runtime
  morphs each into its `data-live` element).

## Session

`jweb.Session` is the visitor's session — typed, null-safe, with one-shot flash messages —
so nothing hand-rolls `getAttribute`/`instanceof`/`setAttribute` again:

```java
import jweb.Session;

// A per-visitor object, created on first use through its no-arg constructor
Visit visit = Session.of(Visit.class, req);
visit.entered = true;
Session.of(Visit.class, req, () -> new Visit(defaults));   // or with a factory

// Keyed values
Session session = Session.of(req);
session.put("theme", "dark");
String theme = session.get("theme", String.class);          // null when absent
String theme = session.get("theme", String.class, "light"); // with a default
Optional<Cart> cart = session.find(Cart.class);
session.remove("theme"); session.has(Cart.class);

// Flash — written now, read once on the next page
session.flash("notice", "Saved!");
String notice = session.flash("notice");                    // then it is gone

// Lifetime
session.exists(); session.id(); session.end();              // end() = logout's invalidate

// Inside a page, without a Request parameter (page routes, router handlers, streamed blocks)
public Element render() {
    Visit visit = session().of(Visit.class);
    ...
}
```

**Lifetime:** the session is the servlet container's — created on the first write (reads never
create one), carried by the `JSESSIONID` cookie (HttpOnly, SameSite=Lax; set
`SESSION_COOKIE_SECURE=true` behind HTTPS), expired after the idle timeout
(`server.servlet.session.timeout`, 30 minutes by default), ended by `session.end()` /
`Auth.logout`. Values live in server memory: keep them small; a typed value whose class
changed shape across a deploy simply starts fresh. `Template.session()` and `Session.current()`
read the request in flight, so they are not available inside WebSocket event handlers —
capture what a handler needs at render time.

## Server-Sent Events (`sse/`)

Working server-side primitives for one-way streaming:

```java
import jweb.SseEmitter;
import jweb.SseBroadcaster;
import jweb.SseEvent;

Object payload = Map.of("count", 1);
Object obj = Map.of("count", 1);
Object data = Map.of("count", 1);
SseEvent event = SseEvent.of("Breaking!");
boolean someCondition = true;

SseBroadcaster broadcaster = new SseBroadcaster();       // 15s heartbeat comments
SseBroadcaster quiet = new SseBroadcaster(0);            // heartbeat disabled

// Events
SseEvent.of("plain data");
SseEvent.of("eventName", "data");
SseEvent.json("update", payload);                        // JSON-serialized
SseEvent.create().id("42").name("tick").data(obj).retry(3000).build();

// Broadcast
broadcaster.broadcast("New notification!");
broadcaster.broadcast(SseEvent.json("update", data));
broadcaster.broadcast("news", SseEvent.of("Breaking!"));  // channel-scoped
broadcaster.broadcastIf(em -> someCondition, event);
broadcaster.getSubscriberCount(); broadcaster.shutdown();
```

> **Serving the stream:** JWeb router handlers can return the emitter directly — the
> controller passes SSE emitters (JWeb's `SseEmitter` or Spring's) through to Spring MVC for
> streaming. Spring `@RestController`s work too.

```java
SseBroadcaster broadcaster = new SseBroadcaster();

// JWeb router route
app.get("/events", req -> {
    SseEmitter emitter = SseEmitter.create(0);   // 0 = no timeout
    broadcaster.subscribe(emitter);
    return emitter;                              // or emitter.toResponse()
});
```

Client side, use `sse("/api/v1/events").onMessage(...).build()` from the JS DSL
(`import static jweb.Js.*;`).

## View Transitions (`transition/`)

```java
import jweb.Transition;

boolean isVisible = true;
Element content = div();

// Conditional show/hide with enter animation classes
Transition.when(isVisible)
    .enter("jweb-fade-enter", 300)
    .render(() -> content)

Transition.fade(isVisible, () -> content);       // presets: fade, slideDown, scale
style(Transition.css());                          // emits the .jweb-fade-*/-slide-*/-scale-* rules
```

When hidden, nothing renders; when shown, enter classes and `data-transition`/
`data-enter-class`/`data-enter-duration` (plus `data-leave-*`) attributes are added. The JWeb
runtime removes enter classes after the enter duration, and `JWeb.leave(elOrId, callback)`
applies the leave classes, waits the leave duration, then removes the element.

For CSS `transition:` properties on any element, use `attrs().transition()...done()` (see the
CSS DSL doc).

## Portals (`portal/`)

Render content into a named outlet elsewhere in the tree (modals, toasts, tooltips):

```java
import jweb.Portal;

// In the layout — outlets must render AFTER all Portal.to() calls (put them last in body)
body(
    div(id("app"), nav, main(content)),
    Portal.outlet("modals"),
    Portal.outlet("toasts")
)

// Anywhere during the same render:
Portal.to("modals", div(class_("modal"), h2("Confirm"), ...));
Portal.modal(content);     // shorthand for to("modals", ...)
Portal.tooltip(content);   // "tooltips"
Portal.toast(content);     // "toasts"
```

> Storage is a ThreadLocal that `JWebController` clears at the end of every request, so
> content can't leak between requests on pooled threads. Call `Portal.clear()` yourself only
> when rendering outside the controller (e.g. background jobs).

## Refs (`ref/`)

Type-safe element references whose methods are `Action`s, so they plug into any handler:

<!-- nocompile: Ref has no jweb.* alias yet (framework.ref.Ref only), and a docs sample may not name a long framework package -->
```java
Ref inputRef = Ref.create();          // id "jweb-ref-<n>"; or Ref.of("existing-id")

form(
    input(ref(inputRef), type("text")),
    button(onClick(inputRef.focus()), "Focus the input")
)

inputRef.scrollIntoView(); inputRef.addClass("highlight");   // Actions
inputRef.set("value", "hello");                              // Action
inputRef.get("value");                                       // a JS expression (Val)
inputRef.selector();                                         // document.getElementById('...')
```

## Toasts (`ui/Toast`)

```java
import jweb.Toast;

// One-time setup in the layout (container + styles + script):
body(content, Toast.setup())                       // or setup(Position.TOP_RIGHT)

// Trigger from any handler — typed Actions:
button(onClick(Toast.success("Saved!")), "Save")
Toast.error("Failed"); Toast.warning("Careful"); Toast.info("FYI");
// (the *Js String variants are deprecated)

// Show on page load:
Toast.initial(Toast.Type.SUCCESS, "Welcome back!")

// Builder — the action button takes an Action too
Toast.builder().type(Toast.Type.INFO).message("Update available")
     .duration(8000).action("Reload", reload()).build()
```

(The methods are `toastScript()`/`init()`/`setup()` — there is no `Toast.script()`.)

## UI Components (`ui/UI`)

A large static library of prebuilt components: `primaryButton/secondaryButton/dangerButton`,
`badge`, `tag`, `alert` (+info/success/warning/error variants), `card`, `avatar`,
`progressBar`, `spinner`, `skeleton`, `emptyState`, `tooltip`, `breadcrumb`, `pagination`,
`kbd`, `codeBlock` — plus fluent builders:

```java
UI.Modal.create("confirm").title("Delete?").body(p("This cannot be undone."))
   .footer(UI.dangerButton("Delete", e -> ...)).build()
button(onClick(UI.Modal.open("confirm")), "Delete")   // open/close are Actions

UI.Tabs.create("settings").tab("general", "General", generalPanel)
   .tab("advanced", "Advanced", advancedPanel).style(UI.Tabs.TabStyle.PILLS).build()

UI.Dropdown.create("menu").trigger(button("Options"))
   .item("Edit", e -> ...).divider().item("Delete", e -> ...).build()

UI.Accordion.create("faq").item("Q1", a1).item("Q2", a2).allowMultiple().build()

UI.DataTable.<User>create()
   .column("Name", u -> text(u.name()))
   .column("Email", u -> text(u.email()))
   .data(users).striped().hoverable().build()
```

Include the matching scripts once per page: `UI.uiScripts()` (or the individual
`modalScript()`, `tabsScript()`, `dropdownScript()`, `accordionScript()`).

## Prefetch (`performance/`)

Auto-injected on every Element response. Hovering any `<a href>` (or `[data-prefetch-url]`)
prefetches the target; opt out per link with `data-no-prefetch="true"`.

Config (`jweb.yaml`): `jweb.performance.prefetch.enabled` (true), `.cache-ttl` (300000 ms),
`.hover-delay` (yaml sets 300 ms; code default is 100 ms).

The richer SPA navigation runtime (partial swaps, View Transitions, active-link tracking) is
separate: add `Navigation.script()` to your layout and use `Link` builders — see the
architecture doc.

## Async rendering (`async/Suspense`)

```java
import jweb.Suspense;
import static jweb.Suspense.*;

record User(String name) { String getName() { return name; } }
class UserService { List<User> getUsers() { return List.of(); } }
UserService userService = new UserService();
UserService slowApi = new UserService();
Element userList(List<User> users) { return ul(each(users, u -> li(u.getName()))); }
Supplier<Integer> loader = () -> 1;
var loadingElement = UI.spinner();
Function<Integer, Element> contentFn = n -> div(String.valueOf(n));
Function<Throwable, Element> errorFn = e -> UI.errorAlert(e.getMessage());

// Blocking (default): loader runs inline during render
Suspense.of(() -> userService.getUsers())
    .loading(() -> UI.spinner())                  // shown only in non-blocking timeout mode
    .error(e -> UI.errorAlert("Failed: " + e.getMessage()))
    .render(users -> ul(each(users, u -> li(u.getName()))));

// Non-blocking: give the loader a time budget; render loading state if exceeded
suspendFast(() -> slowApi.getUsers(), UI.spinner(), users -> userList(users));
suspendFast(() -> slowApi.getUsers(), UI.spinner(), 200, users -> userList(users));

// Convenience statics
suspend(loader, loadingElement, contentFn);
suspend(loader, loadingElement, errorFn, contentFn);
suspendSilent(loader, contentFn);                 // render nothing on failure
```

> `.timeout(n, unit)` bounds blocking loads (default 30s) — on expiry the error element
> renders. `nonBlocking(...)`/`suspendFast(...)` instead show the loading element when data
> isn't ready in time.

## Background Jobs (`async/Jobs`, `async/Scheduler`)

```java
import java.util.concurrent.CompletableFuture;

class Log { static Logger framework() { return new Logger(); } static class Logger { void info(String f, Object... a) {} void error(String f, Object... a) {} } }
record User(String email) {}
record Report(String body) {}
User user = new User("a@b.com");
void sendEmail(User u) {}
Report generateReport() { return new Report(""); }
Report doImport() { return new Report(""); }
void sendReminder() {}
void cleanupTempFiles() {}
void generateDailyReport() {}
void cleanupFiles() {}
void sendDigest() {}
void sync() {}

// Fire-and-forget on virtual threads
Jobs.run(() -> sendEmail(user));
CompletableFuture<Report> f = Jobs.submit(() -> generateReport());

// Tracked tasks with progress
BackgroundTask<Report> task = Jobs.trackWithProgress("Import", progress -> {
    progress.update(50, "halfway");
    return doImport();
});
Jobs.getTask(task.getId()).ifPresent(t ->
    Log.framework().info("{} {}%", t.getStatus(), t.getProgress()));
Jobs.cleanupCompletedTasks();    // call periodically — the task map is not self-cleaning

// Simple scheduling
Jobs.delay(Duration.ofSeconds(30), () -> sendReminder());
Jobs.schedule("cleanup", Duration.ofMinutes(5), () -> cleanupTempFiles());
```

<!-- nocompile: Scheduler has no jweb.* alias yet (framework.async.Scheduler only), and a docs sample may not name a long framework package -->
```java
// Cron scheduling (5-field: min hour dom month dow, 0=Sunday; supports * , - /)
Scheduler.cron("daily-report", "0 9 * * *", () -> generateDailyReport());
Scheduler.job("cleanup").cron("0 3 * * *").timezone("America/New_York")
         .initialDelay(Duration.ofMinutes(1))
         .onError(e -> Log.framework().error("job failed", e))
         .run(() -> cleanupFiles());
Scheduler.daily("digest", LocalTime.of(9, 0), () -> sendDigest());
Scheduler.everyMinutes(15, "sync", () -> sync());
Scheduler.pause("sync"); Scheduler.resume("sync"); Scheduler.cancel("sync");
Scheduler.getAllJobs();          // List<JobInfo(name, schedule, paused, lastRun, runCount, active)>
```

## Cache (`cache/Cache`)

```java
import jweb.Cache;                                        // Js.Cache (a JS runtime module) collides otherwise

record User(String name) {}
record Report(String body) {}
static User user = new User("Ada");
static User load(String id) { return user; }

var users = Cache.<String, User>create(Duration.ofMinutes(10));
var global = Cache.<Object>global();                      // shared singleton, 5-min default TTL
var reports = Cache.<String, Report>named("reports", Duration.ofHours(1));

users.set("42", user);
users.set("42", user, Duration.ofMinutes(1));             // per-entry TTL
User u = users.getOrSet("42", () -> load("42"));          // atomic per key (single compute)
users.has("42"); users.delete("42"); users.touch("42");   // extend TTL
users.ttl("42"); users.stats();                           // CacheStats(total, active, expired, maxSize)
```

Background cleanup runs every minute. Size-capped caches evict the nearest-expiry entry (not
LRU).
