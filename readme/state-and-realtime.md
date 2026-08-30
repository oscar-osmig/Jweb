[← Back to README](./../README.md)

# State & Realtime

This document covers reactive state, server-side events, hydration, WebSocket/SSE transport,
and the interactive UI utilities (transitions, portals, refs, toasts, suspense).

> ✅ **As of 2026-08-09 the reactive round-trip works end-to-end**: the client runtime is
> auto-injected into rendered pages (disable with `jweb.runtime.enabled: false`), render
> contexts survive until a TTL reaper collects them (5 min idle, refreshed by WebSocket
> activity), and `useComponent(...)` regions re-render on the server and patch into the DOM.
> See [Known Issues](./known-issues.md) for the full fix list.

## Reactive State

### Creating state

```java
import jweb.state.State;                                // the state value type
import static jweb.State.*;                             // useState & friends (alias of StateHooks)
import com.osmig.Jweb.framework.state.StateManager;

// The constructor is internal — create state through the hooks/manager:
State<Integer> count = useState(0);
State<String>  name  = useState();                      // null initial
State<Integer> other = StateManager.createState(0);     // equivalent
State<Integer> named = StateManager.createState("cart-count", 0);
```

> ⚠️ `State.of(...)` does **not** exist (older docs claimed it did).

### Using state

```java
count.get();                 // read
count.set(5);                // write (no-op if value unchanged); marks dirty; notifies
count.update(n -> n + 1);    // transform
count.subscribe(v -> Log.framework().info("count is now {}", v));
count.getId();               // "state_<n>" — used by client bindings
count.toJson();              // {"id":"state_1","value":5}
```

### Hooks

```java
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

### Binding state to elements (client contract)

The client runtime patches elements by attribute:

| Attribute | Behavior on state change |
|-----------|--------------------------|
| `data-state="state_1"` / `data-state-bind="state_1"` | text/value updated |
| `data-state-text="on:off"` | picks text by truthiness |
| `data-state-toggle="state_1"` | toggles the `toggle-on` class |

`StateBinding.bind(state)` / `StateBinding.bindInput(state)` return `Attributes` carrying
`data-state-bind`. A `jweb:stateChange` CustomEvent fires on every patch.

## Server-Side Events (`events/`)

Attach Java lambdas to DOM events; the framework registers them and renders a JS call:

```java
button(attrs().onClick(e -> count.update(n -> n + 1)), "Increment")
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
  evicted when the context dies. Outside a render they fall back to the global registry.
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
  registered handler, collects `getChangedStates()`, and pushes `stateUpdate` (+`domUpdate` for
  registered `RenderableComponent`s) back.

## Server-Sent Events (`sse/`)

Working server-side primitives for one-way streaming:

```java
import jweb.SseEmitter;
import com.osmig.Jweb.framework.sse.*;      // SseBroadcaster, SseEvent

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
import com.osmig.Jweb.framework.transition.Transition;

// Conditional show/hide with enter animation classes
Transition.when(isVisible)
    .enter("jweb-fade-enter", 300)
    .render(() -> modal())

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
import com.osmig.Jweb.framework.portal.Portal;

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

Type-safe element references whose methods generate JS snippets:

```java
Ref inputRef = Ref.create();          // id "jweb-ref-<n>"; or Ref.of("existing-id")

form(
    input(attrs().ref(inputRef).type("text")),
    button(attrs().set("onclick", inputRef.focus()), "Focus the input")
)

inputRef.scrollIntoView(); inputRef.addClass("highlight");
inputRef.set("value", "hello"); inputRef.selector();     // document.getElementById('...')
```

## Toasts (`ui/Toast`)

```java
import com.osmig.Jweb.framework.ui.Toast;

// One-time setup in the layout (container + styles + script):
body(content, Toast.setup())                       // or setup(Position.TOP_RIGHT)

// Trigger from actions/attributes (returns JS strings):
button(attrs().set("onclick", Toast.successJs("Saved!")), "Save")
Toast.errorJs("Failed"); Toast.warningJs("Careful"); Toast.infoJs("FYI");

// Show on page load:
Toast.initial(Toast.Type.SUCCESS, "Welcome back!")

// Builder
Toast.builder().type(Toast.Type.INFO).message("Update available")
     .duration(8000).action("Reload", "location.reload()").build()
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
UI.Modal.openJs("confirm");   // JS string

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
Cache<String, User> users = Cache.create(Duration.ofMinutes(10));
Cache<String, Object> global = Cache.global();            // shared singleton, 5-min default TTL
Cache<String, Report> reports = Cache.named("reports", Duration.ofHours(1));

users.set("42", user);
users.set("42", user, Duration.ofMinutes(1));             // per-entry TTL
User u = users.getOrSet("42", () -> load("42"));          // atomic per key (single compute)
users.has("42"); users.delete("42"); users.touch("42");   // extend TTL
users.ttl("42"); users.stats();                           // CacheStats(total, active, expired, maxSize)
```

Background cleanup runs every minute. Size-capped caches evict the nearest-expiry entry (not
LRU).
