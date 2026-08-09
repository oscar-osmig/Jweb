[← Back to README](./../README.md)

# JavaScript DSL

43 modules, ~23,300 lines, generating client-side JavaScript from type-safe Java. Two layers:

- **`Actions`** — the high-level, user-facing layer: form/click handlers, fetch chains, DOM
  actions, message boxes. This is what pages normally use.
- **`JS`** — the low-level expression/statement layer: values, functions, control flow, DOM
  access. Use it when `Actions` doesn't cover the shape you need.

## ⚠️ Two `script()` builders — pick deliberately

`JS.script()` and `Actions.script()` are **different builders with the same name**:

| | `JS.script()` | `Actions.script()` |
|---|---|---|
| Output | bare statements | wrapped in an IIFE `(function(){...})()` |
| `withHelpers()` | ❌ not available | ✅ available |
| Accepts | `var_/let_/const_`, `Func`, `AsyncFunc`, raw | handlers (`onSubmit`/`onClick`/...), `state()`, `onLoad()`, raw |

**Helpers are auto-injected.** Generated handlers reference `$_('id')` (plus `esc()` and
`fmtDate()`); `build()` detects their use and prepends the definitions automatically, so
`withHelpers()` is now optional (calling it explicitly is still fine and never duplicates).

Also: `JS.*` and `Actions.*` share many static names (`script`, `query`, `queryAll`, `call`,
`fetch`, `sleep`, `promiseAll`, `pushState`, ...). **Wildcard-importing both into one file causes
ambiguous-reference compile errors.** Convention: import `Actions.*` in page code; qualify
`JS.` explicitly when you need the low-level layer.

## The standard form-submission pattern (used by the sample ContactPage)

```java
import static com.osmig.Jweb.framework.js.Actions.*;

String js = script()
    .withHelpers()
    .add(onSubmit("contact-form")
        .loading("Sending...")                       // disables submit button, swaps text
        .post("/api/v1/contact").withFormData()      // serialize form fields to JSON body
        .ok(all(
            showMessage("form-status").success("Message sent!"),
            resetForm("contact-form")))
        .fail(showMessage("form-status").error("Failed to send.")))
    .build();

// Put it on the page:
inlineScript(js)
```

### The magic-locals contract

Inside `ok(...)` / `fail(...)` actions, the generated code exposes these locals:

| Local | Meaning |
|-------|---------|
| `_data` | parsed JSON response body |
| `_res` | the `fetch` Response object |
| `_fd` | the `FormData` (form handlers) |
| `_form` / `_btn` | the form element / its submit button |

This is why `assignVar("users", "_data")` and `showMessage(...).fromResponse("field")` work.
Typed accessors exist: `responseField("name")` → `_data.name`, `formField("email")` →
`_fd.get('email')`.

### FormHandler chain reference

`onSubmit(formId)` → `.loading(text)` `.before(action)` `.post(url)`/`.get(url)`
`.withFormData()` `.withJson("k", "v", ...)` `.header(n,v)` `.headerFrom(n, varName)`
`.ok(action)` `.fail(action)` `.always(action)`.
With GET + `withFormData()`, fields become URL query params instead of a body.
`onSubmitExternal(formId)` targets cross-origin endpoints.

## Click / Change Handlers

```java
onClick("delete-btn")
    .confirm("Are you sure?")
    .delete("/api/items/42")
    .ok(reload())
    .fail(showMessage("error").error("Delete failed"))

onClick("toggle-btn").toggle("panel")     // no-network shortcuts: toggle/show/hide
onChange("country-select").get("/api/regions").ok(...)
```

## Actions catalog

```java
// Messages (renders into an element; colors are hardcoded green/red/amber)
showMessage("status").success("Saved!")
showMessage("status").error("Failed!")
showMessage("status").warning("Careful…")
showMessage("status").fromResponse("message")

// Navigation & forms
navigateTo("/dashboard"); reload(); resetForm("contact-form"); pushState("/path")

// DOM
show("modal"); hide("modal"); toggle("panel")
setText("title", "New Title"); setHtml("box", "<b>hi</b>")
addClass("card", "active"); removeClass("card", "active"); toggleClass("card", "open")
focus("email"); copyToClipboard("text"); download("/file.pdf")

// Composition & control flow
all(actionA, actionB, actionC)               // run in sequence
when(condition).then(a).otherwise(b)
whenOk(a)  /  whenResponse(field, value).then(a)
tryCatch().attempt(a).onError(b)

// Response status handling
responseError("status-box").on401(...).on403(...).on404(...).on500(...).otherwise(...)

// Modals & templates
showModal("dialog-id"); hideModal("dialog-id"); alertModal("Heads up!")
renderList("list", "items", template("item")
    .field("name").dateField("createdAt").build())

// Async
asyncBlock(await_(get("/api/a").ok(...)), await_(get("/api/b").ok(...)))
sleep(500)
```

## Fetch Builder (`Actions`)

```java
get("/api/users")
    .ok(assignVar("users", "_data"))
    .fail(showMessage("error").error("Failed to load"))

post("/api/users").json("{\"name\":\"John\"}").ok(...)
post("/api/contact").formData("contact-form").ok(resetForm("contact-form"))
put("/api/items/3").jsonExpr("payload").bearer("token").ok(...)

// Status-code handling
get("/api/data")
    .onUnauthorized(navigateTo("/login"))
    .onForbidden(showMessage("error").error("No access"))
    .onNotFound(showMessage("error").error("Missing"))
    .onStatus(429, showMessage("error").error("Slow down"))
    .ok(setText("result", "loaded"))
```

Full chain: `get/post/put/patch/delete/fetch(method, url)`, `.urlFromVar`, `.appendVar`,
`.json/.jsonExpr/.formData/.urlEncoded/.body`, `.header/.headerExpr/.bearer/.credentials/.mode`,
`.onStatus(...)` + named variants, `.ok/.fail/.then`.

> `Actions.promiseAll(actions...)` emits a true parallel `Promise.all([...])` (stripping any
> leading `await` from each action so calls don't serialize). To consume results, use
> `Actions.promiseAllThen(actions...)` which returns a chainable `Async.PromiseBuilder`
> (`.then(...)`/`.catch_(...)`), or `Async.promiseAll(vals...)`.

## DOM Query Builder (`Actions`)

```java
query("#myDiv").hide()
query("#panel").addClass("visible")
query("#title").setText("Hello World")
queryAll(".item").addClass("processed")
queryAll(".temp").remove()
```

## Core JS Module (`JS`)

```java
// Values
variable("count"); str("hello"); null_(); this_()
obj("name", "John", "age", 30)               // {name:'John',age:30}
array(1, 2, 3)
object().prop("a", 1).spread("rest").build()

// Functions
Func formatTime = func("formatTime", "seconds")
    .var_("hrs", floor(variable("seconds").div(3600)))
    .ret(variable("hrs").padStart(2, "0"));
Func cb = callback("e").log("clicked", variable("e").path("target.id"));

// Control flow on Func bodies
func("check", "x")
    .if_(variable("x").gt(10)).then_(...).elif_(...).else_(...).end()
    .forOf("item", variable("list")).body(...).endFor()
    .try_().body(...).catch_("err").endTry()
    .switch_(variable("mode")).case_("a", ...).default_(...).endSwitch();

// DOM
getElem("submit")   // or $("submit")        → El (getElementById)
JS.query(".card")                            → El (querySelector)
JS.queryAll(".item")                         → Val (querySelectorAll)

// Val chains (~180 methods): arithmetic, comparison, string/array/number ops
variable("res").json()
variable("e").path("target.result")
variable("items").filter(...).map(...).join(", ")

// El chains: setText/setValue/addClass/toggleClass/setStyle/hide/show/closest/
//            addEventListener/scrollIntoView/getBoundingClientRect/...

// Script assembly (low-level; no IIFE, no helpers)
String js = JS.script()
    .var_("count", 0)
    .add(formatTime)
    .build();
```

## Async/Await (`Async`)

```java
asyncFunc("loadData")
    .does(
        await_(get("/api/users").ok(assignVar("users", "_data"))),
        await_(get("/api/posts").ok(assignVar("posts", "_data"))),
        call("renderDashboard")
    )

Async.promiseAll(valA, valB).then(callback("results").log(...)).catch_(...)
Async.delay(500); Async.onceEvent(el, "transitionend")
```

`JSPromise` adds: `deferred()` (Promise.withResolvers), `cancellable`, `timeout`, `retry`,
`memoizePromise`, `debouncePromise`, `throttlePromise`.

## Events (`Events`)

```java
import static com.osmig.Jweb.framework.js.Events.*;

// Delegation — attach to parent, filter by selector
delegate("container", "click", ".card").handler(callback("e")
    .log("Clicked:", variable("e").path("target.textContent")))

// Debounce / throttle (note: they wrap handlers, taking a state-variable name)
debounce("searchTimer", 300).wrap(callback("e").log("search"))
throttle("lastScroll", 100).wrap(callback("e").log("scrolled"))

// Keyboard
onKeyCombo("ctrl+s", callback("e").log("save"))     // binds keydown
onKey("Escape", callback().log("esc"))              // there is no onKeyDown() — use onKey/onKeyCombo
onEnter(callback().log("submitted"))

// Touch / swipe — builder, not onSwipe()
swipe(variable("el")).threshold(50)
    .onLeft(callback().log("swiped left"))
    .onRight(callback().log("swiped right"))
    .build()

// Custom events
dispatchCustomEvent("cart:updated", obj("count", 3))
onCustomEvent("cart:updated", callback("e").log(eventDetail()))

// Client-side SSE
sse("/events")
    .onMessage(callback("e").log(variable("e").dot("data")))
    .onError(callback().log("SSE error"))
    .build()
```

## Full module inventory (43)

| Module | Purpose |
|--------|---------|
| `Actions` | High-level UI DSL — handlers, fetch, DOM actions, templates (main entry) |
| `JS` | Core expressions/statements — Val/El/Func/Script |
| `Async` | Fetch builder, async funcs, await, promise combinators |
| `Events` | Delegation, debounce/throttle, key combos, touch/swipe, custom events, SSE client |
| `Runtime` | IIFE, run-once guard, TTL cache, memoize patterns |
| `JWebRuntime` | The reactive-state client runtime (⚠️ currently not auto-injected — see Known Issues) |
| `JSAbort` | AbortController/AbortSignal (timeout/any/onAbort) |
| `JSAnimation` | requestAnimationFrame loops, CSS transition/animation helpers, lerp/easing |
| `JSCanvas` | Canvas 2D — paths, gradients, transforms, ImageData, OffscreenCanvas |
| `JSClipboard` | copyText/copyElementText/readText/write + fallback |
| `JSConsole` | console.log/table/group/time/count/trace/assert |
| `JSCrypto` | Web Crypto — digest/encrypt/sign/generateKey/deriveKey/wrapKey |
| `JSDate` | Date manipulation — add/startOf/endOf/diff/UTC/format |
| `JSDragDrop` | draggable()/dropZone() builders, DataTransfer accessors |
| `JSFile` | FileReader, Blob, object URLs, download helpers, validation |
| `JSFormData` | FormData construction/manipulation, toUrlEncoded/toObject |
| `JSFullscreen` | request/exit/toggle fullscreen, state, fullscreenButton |
| `JSGeolocation` | getCurrentPosition/watchPosition builders, distanceBetween |
| `JSHistory` | pushState/replaceState, popstate, navigation guards, query params |
| `JSIndexedDB` | openDB/createStore/transaction/cursorQuery builders, IDBKeyRange |
| `JSIntl` | number/currency/date/relative-time/list/plural formatting |
| `JSIterator` | Generators, yield, async iterators, range/zip/chain |
| `JSJson` | stringify/parse/safeParse/deepClone/isValidJson |
| `JSMath` | Math.* wrappers, clamp, randomInt/randomRange |
| `JSMedia` | audio/video control, MediaRecorder, Picture-in-Picture, Web Audio |
| `JSNotification` | Notification permission + builder (icon/badge/tag/actions) |
| `JSObservers` | Intersection/Mutation/Resize observers |
| `JSOperators` | Optional chaining, nullish coalescing, spread, in/delete/void |
| `JSPerformance` | marks/measures, Navigation/Resource Timing, PerformanceObserver |
| `JSPointer` | Pointer events, pressure/tilt, capture, multiPointerTracker |
| `JSPromise` | deferred/cancellable/timeout/retry/memoize/debounce/throttle promises |
| `JSProxy` | Proxy (13 traps) + Reflect + property descriptors |
| `JSRegex` | Regex literals, named groups, lookarounds, EMAIL/URL/UUID presets |
| `JSServiceWorker` | SW registration + sw.js lifecycle, Cache API, push, sync |
| `JSShare` | Web Share API, shareFiles, canShare |
| `JSSpeech` | speechSynthesis (speak) + SpeechRecognition (recognizer) |
| `JSStorage` | localStorage/sessionStorage (+JSON), cookies, storage events, quota |
| `JSUrl` | URL/URLSearchParams parsing and building |
| `JSVisibility` | Page Visibility, onVisible/onHidden, visibility-aware intervals |
| `JSWebAnimations` | element.animate() + keyframe builder, playback control |
| `JSWebRTC` | RTCPeerConnection, getUserMedia, data channels, stats |
| `JSWorker` | Web Workers (dedicated/inline/shared), MessageChannel, BroadcastChannel |
| `JSWebSocket` | WebSocket builder with autoReconnect, sendJson |

## Selected module guides

### IndexedDB

```java
import static com.osmig.Jweb.framework.js.JSIndexedDB.*;
import static com.osmig.Jweb.framework.js.JS.*;

openDB("myApp", 1)
    .onUpgrade(callback("db")
        .unsafeRaw("db.createObjectStore('users',{keyPath:'id'})"))
    .onSuccess(callback("db").log("Database opened"))
    .build();

transaction(variable("db"), "users", "readwrite")
    .store("users")
    .add(obj("id", 1, "name", "Alice"))
    .build();

// Cursor iteration — the method is cursorQuery, direction via ascending()/descending()
cursorQuery(variable("db"), "users")
    .descending()
    .onEach(callback("cursor").log(variable("cursor").dot("value")))
    .build();
```

### History API

```java
import static com.osmig.Jweb.framework.js.JSHistory.*;

pushState("/dashboard", obj("page", "dashboard"), "Dashboard")
replaceState("/login", obj("page", "login"))
onPopState(callback("e").log(variable("e").dot("state")))

// Navigation guards
navigationGuard("You have unsaved changes!")
navigationGuardWhen(variable("formDirty"), "You have unsaved changes!")  // conditional form

// Query params
getQueryParam("page"); setQueryParam("page", "2"); removeQueryParam("filter")
```

### Drag and Drop

```java
import static com.osmig.Jweb.framework.js.JSDragDrop.*;

draggable("card-1")
    .data("text/plain", "Card 1 data")
    .effectAllowed("move")
    .onDragStart(callback("e").log("drag started"))
    .build();

dropZone("target-area")
    .dropEffect("move")
    .onDrop(callback("e")
        .let_("data", getData(variable("e"), "text/plain"))
        .log("Dropped:", variable("data")))
    .build();
```

### Pointer Events

```java
import static com.osmig.Jweb.framework.js.JSPointer.*;

onPointerDown("canvas", callback("e")
    .log(pointerId(variable("e")))
    .log(pressure(variable("e"))))

multiPointerTracker("canvas")
    .onStart(callback("e").log("down:", pointerId(variable("e"))))
    .onMove(callback("e").log("move:", clientX(variable("e"))))
    .onEnd(callback("e").log("up"))
    .build();
```

### Speech

```java
import static com.osmig.Jweb.framework.js.JSSpeech.*;

speak("Hello, welcome to JWeb!")

speakBuilder("Hello world")
    .lang("en-US").rate(1.2).pitch(1.0).volume(0.8)
    .onEnd(callback().log("Done speaking"))
    .build();

// Recognition — note build(varName) requires a variable name
recognizer()
    .lang("en-US").continuous(true).interimResults(true)
    .onResult(callback("e").log("Heard:", transcript(variable("e"))))
    .build("rec");
startRecognition(variable("rec"));
```

### Storage & Cookies

```java
import static com.osmig.Jweb.framework.js.JSStorage.*;

local().setJson("user", obj("name", "Jo"))
local().getJsonOr("user", obj())
session().set("draft", variable("text"))
cookie("theme").days(30).sameLax().set("dark")
onStorageKeyChange("user", callback("e").log(eventNewValue()))
```

### WebSocket (client)

```java
import static com.osmig.Jweb.framework.js.JSWebSocket.*;

webSocket(wsUrl("/live"))
    .onOpen(callback().log("connected"))
    .onMessage(callback("e").log(variable("e").dot("data")))
    .autoReconnect()
    .build();
```

## How generated JS reaches the page

There is no bundler and no automatic script injection for DSL output. Three mechanisms:

1. **Inline script tags** — the DSL produces a `String`; place it yourself:
   `inlineScript(script().withHelpers().add(...).build())` (unescaped `VRaw` under the hood).
2. **Inline event attributes** — `attrs().onClick(action)` writes `action.inline()` into
   `onclick` etc.
3. **Auto-injected framework scripts** — the controller injects only the `Prefetch` script and
   the `__JWEB_DATA__` hydration JSON before `</body>`. The reactive-state client runtime
   (`JWebRuntime`) is **not** auto-injected — see
   [State & Realtime](./state-and-realtime.md) and [Known Issues](./known-issues.md).
