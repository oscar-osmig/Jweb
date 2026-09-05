[← Back to README](./../README.md)

# JavaScript DSL

43 modules, ~23,000 lines, generating client-side JavaScript from type-safe Java. One
import, two layers:

```java
import static jweb.Js.*;
```

- **Actions** — the page-level layer: form/click handlers, fetch chains, DOM actions,
  message boxes. Everything here is an `Action`, and an `Action` plugs into any element
  handler. This is what pages normally use.
- **Expressions and statements** — values (`v`, `str`, `obj`), functions, control flow,
  DOM access. Use it when an Action doesn't cover the shape you need.

`jweb.Actions` is the same surface under its old name (importing both is harmless). Every
browser-API module (`JSCanvas`, `JSClipboard`, `JSCrypto`, `JSStorage`, ...) keeps its own
import and lives at `jweb.js.*` — e.g. `import static jweb.js.JSClipboard.*;`.

Where the two layers shared a name, the page-level `Action` form owns it: `fetch("/url")`
is the `.ok(...)/.fail(...)` builder, `call("fn")` and `sleep(ms)` are Actions. The
expression twins are `fetch(str("/url"))` / `fetch(v("apiUrl"))`, `JS.call("fn", args...)`
and `delay(ms)`.

## Two script builders

`script()` and `actions()` are different builders:

| | `script()` | `actions()` |
|---|---|---|
| Output | bare statements | wrapped in an IIFE `(function(){...})()` |
| Accepts | `var_/let/const_`, `Func`, `AsyncFunc`, raw | handlers (`onSubmit`/`onClick`/...), `state()`, `onLoad()`, raw |

`actions()` is itself an `Action`, so a whole script can be returned from a Template's
`scripts()` hook. The DOM query builders are `dom(selector)` / `domAll(selector)`.

**Helpers are auto-injected.** Generated handlers reference `$_('id')` (plus `esc()` and
`fmtDate()`); `build()` detects their use and prepends the definitions automatically.

**Actions plug into element handlers directly** — no raw JS strings:

```java
button(onClick(reload()), "Retry")
button(onClick(toggle("panel")), "Menu")
button(onClick(Toast.success("Saved!")), "Save")      // typed Toast actions
button(onClick(inputRef.focus()), "Focus")            // Ref methods are Actions
```

## Handlers take a `Func`

Every `onXxx(...)` in the browser-API modules takes a `Func`. Some also accept a raw
JavaScript `String`, but that form hides which parameter name the generated function
uses, so prefer the explicit escape hatch when you need raw JS:

```java
callback("e").unsafeRaw("console.log(e.detail)")   // function(e){console.log(e.detail)}
```

That way the parameter name is written where the code that uses it is written.

## The standard form-submission pattern

(The sample ContactPage now uses the zero-JS `swapForm(...)` fragment pattern instead — see
[Backend § Fragments](./backend.md) — but this remains the canonical `Actions` flow when you
want scripted control.)

```java
import static jweb.Actions.*;

String js = actions()
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

This is why `assign("users", response("users"))` and `showMessage(...).fromResponse("field")`
work.
Typed accessors exist: `response("name")` → `_data.name`, `formField("email")` →
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
onChange("country-select").then(get("/api/regions").ok(...))   // ChangeHandler wraps an action via then(...)

// Double-click — same two flavours as onClick everywhere it appears
// (attrs()/element handler attrs, the El/Elements facade, and Button):
// a Consumer<Event> server handler, or an Action, both delegated
// CSP-safe via data-jweb-ondblclick/data-jweb-actdblclick.
button(onDblClick(e -> zoomIn()), "Zoom")
button(onDblClick(toggle("fullscreen-panel")), "Zoom")
```

## Actions catalog

```java
// Messages (renders into an element; sets jweb-msg-<type> classes, themeable via
// --jweb-msg-<type>-bg/-fg CSS variables with green/red/amber defaults)
showMessage("status").success("Saved!")
showMessage("status").error("Failed!")
showMessage("status").warning("Careful…")
showMessage("status").fromResponse("message")

// Navigation & forms
navigateTo("/dashboard"); reload(); resetForm("contact-form")
pushState("admin", "true")                   // history.pushState({admin:true},'',location.href)
                                             // (for the real History API use jweb.js.JSHistory)

// DOM
show("modal"); hide("modal"); toggle("panel")
setText("title", "New Title"); setHtml("box", "<b>hi</b>")
addClass("card", "active"); removeClass("card", "active"); toggleClass("card", "open")
focus("email"); copyToClipboard("token-field"); download("/file.pdf")

// Composition & control flow
all(actionA, actionB, actionC)               // run in sequence
when(condition).then(a).otherwise(b)
whenOk().then(a)  /  whenResponse(field).then(a)     // _res.ok / truthy _data.<field>
tryCatch().try_(a).catch_(b)                 // optional .finally_(c); '_err' in the catch

// Response status handling
responseError("status-box").on401(...).on403(...).on404(...).on500(...).otherwise(...)

// Modals & templates
showModal("dialog-id"); hideModal("dialog-id")
alertModal("modal-overlay", "modal-body").success("Approved!")
renderList("list").from("items")             // items = JS array variable
    .using(template("item")
        .div().text(field("name")).end()
        .build())

// Async
asyncBlock(await(get("/api/a").ok(...)), await(get("/api/b").ok(...)))
sleep(500)
```

## Fetch Builder (`Actions`)

```java
get("/api/users")
    .ok(assign("users", response("users")))
    .fail(showMessage("error").error("Failed to load"))

post("/api/users").json("{\"name\":\"John\"}").ok(...)
post("/api/contact").formData("contact-form").ok(resetForm("contact-form"))
put("/api/items/3").json(v("payload")).bearer("token").ok(...)

// Status-code handling
get("/api/data")
    .onUnauthorized(navigateTo("/login"))
    .onForbidden(showMessage("error").error("No access"))
    .onNotFound(showMessage("error").error("Missing"))
    .onStatus(429, showMessage("error").error("Slow down"))
    .ok(setText("result", "loaded"))
```

Full chain: `get/post/put/patch/delete/fetch(method, url)`, `.urlFromVar`, `.appendVar`,
`.json/.formData/.urlEncoded/.body`, `.header/.bearer/.credentials/.mode`
(each takes a literal `String` or a `Val` expression — the old `*Expr` twins are deprecated),
`.onStatus(...)` + named variants, `.ok/.fail/.then`.

> `Actions.promiseAll(actions...)` emits a true parallel `Promise.all([...])` (stripping any
> leading `await` from each action so calls don't serialize). To consume results, use
> `Actions.promiseAllThen(actions...)` which returns a chainable `Async.PromiseBuilder`
> (`.then(...)`/`.catch_(...)`), or `JSPromise.all(vals...)`.

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
v("count"); str("hello"); null_(); this_()      // v() is short for variable()
obj("name", "John", "age", 30)               // {name:'John',age:30}
array(1, 2, 3)
object().prop("a", 1).spread("rest").build()

// Functions
Func formatTime = func("formatTime", "seconds")
    .var_("hrs", floor(v("seconds").div(3600)))
    .return_(v("hrs").padStart(2, "0"));
Func cb = callback("e").log("clicked", v("e").path("target.id"));

// Control flow on Func bodies — every block takes its body inline, nothing closes it
func("check", "x")
    .if_(v("x").gt(10), call("big"))                 // if / else if / else
    .elif(v("x").gt(5), call("mid"))
    .else_(call("small"))
    .forOf("item", v("list"), stmt1, stmt2)          // same for while_/for_/forIn/doWhile
    .while_(v("go"), stmt)
    .try_().body(...).catch_("err", stmt)            // catch_ with a body closes the try
    .switch_(v("mode")).case_("a", stmt, "break").default_(stmt);   // default_ closes it

// A trailing underscore marks a Java keyword and nothing else: if_/else_/for_/while_/
// try_/catch_/switch_/case_/default_/return_/const_/var_/null_/this_ keep it;
// then, elif, let, await, in, delete do not. An Action is a statement anywhere a
// statement goes: .if_(cond, toggle("panel")) works.

// DOM
byId("submit")   // or $("submit")           → El (getElementById)
Js.query(".card")                            → El (querySelector)
JS.queryAll(".item")                         → Val (querySelectorAll)

// Viewport & media queries
matchMedia("(min-width: 768px)")             → Val (window.matchMedia(query))
reducedMotion()                              → Val boolean (prefers-reduced-motion: reduce)
viewportWidth(); viewportHeight()            → Val (window.innerWidth/innerHeight)

// Val chains (~180 methods): arithmetic, comparison, string/array/number ops
v("res").json()
v("e").path("target.result")
v("items").filter(...).map(...).join(", ")

// El chains: setText/setValue/addClass/toggleClass/setStyle/hide/show/closest/
//            addEventListener/scrollIntoView/getBoundingClientRect/...

// Script assembly (low-level; no IIFE, no helpers)
String js = Js.script()
    .var_("count", 0)
    .add(formatTime)
    .build();
```

## Async/Await (`Async`)

```java
asyncFunc("loadData")
    .does(
        await(get("/api/users").ok(assign("users", response("users")))),
        await(get("/api/posts").ok(assign("posts", response("posts")))),
        call("renderDashboard")
    )

JSPromise.all(valA, valB).then(callback("results").log(...)).catch_(...)   // Promise helpers live in JSPromise
Async.delay(500); Async.onceEvent(el, "transitionend")
```

`JSPromise` adds: `deferred()` (Promise.withResolvers), `cancellable`, `timeout`, `retry`,
`memoizePromise`, `debouncePromise`, `throttlePromise`.

## Events (`Events`)

```java
import static jweb.Js.*;

// Delegation — attach to parent, filter by selector
delegate("container", "click", ".card").handler(callback("e")
    .log("Clicked:", v("e").path("target.textContent")))

// Debounce / throttle (note: they wrap handlers, taking a state-variable name)
debounce(300, callback("e").log("search"))   // self-contained closure; no timer variable
throttle(100, callback("e").log("scroll"))
throttle("lastScroll", 100).wrap(callback("e").log("scrolled"))

// Keyboard
onKeyCombo("ctrl+s", callback("e").log("save"))     // binds keydown
onKey("Escape", callback().log("esc"))              // there is no onKeyDown() — use onKey/onKeyCombo
onEnter(callback().log("submitted"))

// Touch / swipe — builder, not onSwipe()
swipe(v("el")).threshold(50)
    .onLeft(callback().log("swiped left"))
    .onRight(callback().log("swiped right"))
    .build()

// Custom events (element first; eventDetail takes the event)
dispatchCustomEvent(v("document"), "cart:updated", obj("count", 3))
onCustomEvent(v("document"), "cart:updated",
    callback("e").log(eventDetail(v("e"))))

// Client-side SSE
sse("/events")
    .onMessage(callback("e").log(v("e").dot("data")))
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
| `JWebRuntime` | The reactive-state client runtime (auto-injected into rendered pages; opt out with `jweb.runtime.enabled: false`) |
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
| `JSMedia` | audio/video control, MediaRecorder, Picture-in-Picture, Web Audio synthesis |
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
import static jweb.js.JSIndexedDB.*;
import static jweb.Js.*;

openDB("myApp", 1)
    .onUpgrade(callback("db")
        .unsafeRaw("db.createObjectStore('users',{keyPath:'id'})"))
    .onSuccess(callback("db").log("Database opened"))
    .build();

transaction(v("db"), "users", "readwrite")
    .store("users")
    .add(obj("id", 1, "name", "Alice"))
    .build();

// Cursor iteration — the method is cursorQuery, direction via ascending()/descending()
cursorQuery(v("db"), "users")
    .descending()
    .onEach(callback("cursor").log(v("cursor").dot("value")))
    .build();
```

### History API

```java
import static jweb.js.JSHistory.*;

pushState(obj("page", str("dashboard")), "/dashboard")   // platform order: (state, url)
replaceState("/login", obj("page", "login"))
onPopState(callback("e").log(v("e").dot("state")))

// Navigation guards
navigationGuard("You have unsaved changes!")
navigationGuardWhen(v("formDirty"), "You have unsaved changes!")  // conditional form

// Query params
getQueryParam("page"); setQueryParam("page", "2"); removeQueryParam("filter")
```

### Drag and Drop

```java
import static jweb.js.JSDragDrop.*;

draggable("card-1")
    .data("text/plain", "Card 1 data")
    .effectAllowed("move")
    .onDragStart(callback("e").log("drag started"))
    .build();

dropZone("target-area")
    .dropEffect("move")
    .onDrop(callback("e")
        .let("data", getData(v("e"), "text/plain"))
        .log("Dropped:", v("data")))
    .build();
```

### Pointer Events

```java
import static jweb.js.JSPointer.*;

onPointerDown("canvas", callback("e")
    .log(pointerId(v("e")))
    .log(pressure(v("e"))))

multiPointerTracker("canvas")
    .onStart(callback("e").log("down:", pointerId(v("e"))))
    .onMove(callback("e").log("move:", clientX(v("e"))))
    .onEnd(callback("e").log("up"))
    .build();
```

### Speech

```java
import static jweb.js.JSSpeech.*;

speak("Hello, welcome to JWeb!")

speakBuilder("Hello world")
    .lang("en-US").rate(1.2).pitch(1.0).volume(0.8)
    .onEnd(callback().log("Done speaking"))
    .build();

// Recognition — note build(varName) requires a variable name
recognizer()
    .lang("en-US").continuous(true).interimResults(true)
    .onResult(callback("e").log("Heard:", transcript(v("e"))))
    .build("rec");
startRecognition(v("rec"));
```

### Storage & Cookies

```java
import static jweb.js.JSStorage.*;

local().setJson("user", obj("name", "Jo"))
local().getJsonOr("user", obj())
session().set("draft", v("text"))
cookie("theme").days(30).sameLax().set("dark")
onStorageKeyChange("user", callback("e").log(eventNewValue(v("e"))))
```

### Web Audio synthesis

```java
import static jweb.js.JSMedia.*;

Val ctx = audioContext();
Val gain = createGain(ctx);
Val osc = createOscillator(ctx);
connect(osc, gain); connect(gain, destination(ctx));

// Scheduling — these operate on an AudioParam (osc.frequency, gain.gain),
// reached with .dot("frequency")/.dot("gain"); unlike setFrequency/setGainValue
// (which stomp .value), these queue clickless, precisely-timed changes.
setValueAtTime(gain.dot("gain"), 0, 0);
linearRampToValueAtTime(gain.dot("gain"), 1, 0.05);
exponentialRampToValueAtTime(gain.dot("gain"), 0.0001, 0.4);

resume(ctx);        // short form of resumeAudioContext(ctx) — run from a
                    // click/keydown handler; AudioContext starts suspended
audioState(ctx);    // short form of audioContextState(ctx) → ctx.state

// Buffered playback
Val buffer = createBuffer(ctx, 1, 44100, 44100);
Val source = createBufferSource(ctx);
```

### WebSocket (client)

```java
import static jweb.js.JSWebSocket.*;

webSocket(wsUrl("/live"))
    .onOpen(callback().log("connected"))
    .onMessage(callback("e").log(v("e").dot("data")))
    .autoReconnect()
    .build();
```

## How generated JS reaches the page

There is no bundler and no automatic script injection for DSL output. Three mechanisms:

1. **Inline script tags** — the DSL produces a `String`; place it yourself:
   `inlineScript(actions().add(...).build())` (unescaped `VRaw` under the hood; the
   serializer stamps the request's CSP nonce on the tag).
2. **Event handlers** — `onClick(action)` as an element argument (or `attrs().onClick(...)`).
   Inside a page render this emits a `data-jweb-act<type>` attribute and ships the JS
   in a nonce-stamped definitions script the runtime delegates to (inline `on<type>=`
   can never run under the recommended nonce CSP); the serializer applies the same
   rewrite to raw `set("on<type>", js)` strings at render time. Outside a page render
   both fall back to classic inline attributes (`attrs().inlineHandlers()` forces that).
   A Template's `onMount()`/`onUnmount()` hooks return Actions and are wrapped in the
   right listener for you.
3. **Auto-injected framework scripts** — the controller injects three scripts before
   `</body>`: the `Prefetch` script (external, cached `/jweb/prefetch.js`), the
   `__JWEB_DATA__` hydration JSON (inline, per request), and the reactive-state client
   runtime (external, cached `/jweb/runtime.js`; opt out with `jweb.runtime.enabled: false`) —
   see [State & Realtime](./state-and-realtime.md).
