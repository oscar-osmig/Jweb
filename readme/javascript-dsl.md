[← Back to README](./../README.md)

# JavaScript DSL

## Core JS Module

```java
import static com.osmig.Jweb.framework.js.JS.*;

// Variables and values
variable("count")           // count
str("hello")               // 'hello'
obj("name", "John", "age", 30)  // {name:'John',age:30}
array(1, 2, 3)             // [1,2,3]

// Functions
Func formatTime = func("formatTime", "seconds")
    .var_("hrs", floor(variable("seconds").div(3600)))
    .ret(variable("hrs").padStart(2, "0"));

// DOM access
getElem("submit")          // document.getElementById('submit')
$("submit")                // shorthand for getElem()
query(".card")             // document.querySelector('.card')
queryAll(".item")          // document.querySelectorAll('.item')

// Property access shortcuts
variable("response").call("json")         // response.json()
variable("e").path("target.result")       // e.target.result
variable("res").json()                    // res.json()

// Script builder
String js = script()
    .var_("count", 0)
    .var_("running", false)
    .add(formatTime)
    .build();
```

## Actions

```java
import static com.osmig.Jweb.framework.js.Actions.*;

showMessage("status").success("Saved!")
showMessage("status").error("Failed!")
resetForm("contact-form")
navigateTo("/dashboard")
reload()
show("modal")
hide("modal")
toggle("panel")
setText("title", "New Title")
addClass("card", "active")
removeClass("card", "active")
```

## Form Handlers

```java
// Form submission to API
onSubmit("login-form")
    .loading("Logging in...")
    .post("/api/login").withFormData()
    .ok(all(
        showMessage("status").success("Login successful!"),
        navigateTo("/dashboard")))
    .fail(showMessage("status").error("Invalid credentials"))

// With helpers (adds JS helper functions to page)
script()
    .withHelpers()
    .add(onSubmit("contact-form")
        .loading("Sending...")
        .post("/api/v1/contact").withFormData()
        .ok(all(
            showMessage("form-status").success("Message sent!"),
            resetForm("contact-form")))
        .fail(showMessage("form-status").error("Failed to send.")))
    .build();
```

## Click Handlers

```java
onClick("delete-btn")
    .confirm("Are you sure?")
    .post("/api/delete")
    .ok(reload())
    .fail(showMessage("error").error("Delete failed"))

onClick("toggle-btn").toggle("panel")
onClick("show-btn").show("modal")
onClick("close-btn").hide("modal")
```

## Async/Await

```java
asyncFunc("loadData")
    .does(
        await_(get("/api/users").ok(assignVar("users", "_data"))),
        await_(get("/api/posts").ok(assignVar("posts", "_data"))),
        call("renderDashboard")
    )

promiseAll(
    get("/api/users"),
    get("/api/posts"),
    get("/api/comments")
).ok(all(
    assignVar("users", "_data[0]"),
    assignVar("posts", "_data[1]"),
    assignVar("comments", "_data[2]")
))
```

## Fetch Builder

```java
get("/api/users")
    .ok(assignVar("users", "_data"))
    .fail(showMessage("error").error("Failed to load"))

post("/api/users").json("{\"name\":\"John\"}")
    .ok(showMessage("status").success("Created!"))

post("/api/contact").formData("contact-form")
    .ok(resetForm("contact-form"))

// Status code handling
get("/api/data")
    .onUnauthorized(navigateTo("/login"))
    .onForbidden(showMessage("error"))
    .onNotFound(showMessage("error"))
    .ok(processData())
```

## DOM Query Builder

```java
query("#myDiv").hide()
query("#panel").addClass("visible")
query("#title").setText("Hello World")

queryAll(".item").addClass("processed")
```

## Advanced JS Modules

JWeb includes 43 JavaScript DSL modules for comprehensive browser API coverage:

| Module | Purpose |
|--------|---------|
| `JSPromise` | Promise utilities - retry, timeout, cancellable, combinators |
| `JSIterator` | Iterators and generators - `function*`, `yield`, async iterators |
| `JSProxy` | Proxy and Reflect API - all 13 handler traps |
| `JSWorker` | Web Workers - dedicated, shared, message channels |
| `JSServiceWorker` | Service Workers - caching, push notifications, sync |
| `JSMedia` | Media APIs - audio/video control, MediaRecorder, Picture-in-Picture |
| `JSCanvas` | Canvas 2D API - drawing, gradients, transforms |
| `JSWebRTC` | WebRTC - peer connections, media streams, data channels |
| `JSCrypto` | Web Crypto - encryption, hashing, key management |
| `JSPerformance` | Performance API - timing, observers, metrics |
| `JSIndexedDB` | IndexedDB - client-side database, transactions, cursors |
| `JSHistory` | History API - pushState, navigation guards, query params |
| `JSDragDrop` | Drag and Drop - draggable, drop zones, DataTransfer |
| `JSPointer` | Pointer Events - unified mouse/touch/pen, multi-touch |
| `JSSpeech` | Web Speech - text-to-speech, speech recognition |
| `JSStorage` | localStorage/sessionStorage |
| `JSWebSocket` | WebSocket with auto-reconnect |
| `JSAnimation` | requestAnimationFrame, transitions |
| `JSWebAnimations` | Web Animations API |
| `JSGeolocation` | Geolocation API |
| `JSNotification` | Push notifications |
| `JSClipboard` | Clipboard API |
| `JSFullscreen` | Fullscreen API |
| `JSShare` | Web Share API |
| `JSVisibility` | Page Visibility API |
| `JSObservers` | Intersection/Mutation/Resize observers |
| `JSIntl` | Internationalization (number/date formatting) |

## IndexedDB

```java
import static com.osmig.Jweb.framework.js.JSIndexedDB.*;

// Open a database with schema
openDB("myApp", 1)
    .onUpgrade(callback("db")
        .unsafeRaw("db.createObjectStore('users',{keyPath:'id'})"))
    .onSuccess(callback("db").log("Database opened"))
    .build();

// Add a record
transaction(variable("db"), "users", "readwrite")
    .store("users")
    .add(obj("id", 1, "name", "Alice"))
    .build();

// Cursor iteration
cursor(variable("db"), "users")
    .direction("next")
    .onEach(callback("cursor").log(variable("cursor").dot("value")))
    .build();
```

## History API

```java
import static com.osmig.Jweb.framework.js.JSHistory.*;

// Push/replace state
pushState("/dashboard", obj("page", "dashboard"), "Dashboard")
replaceState("/login", obj("page", "login"))

// Listen for back/forward navigation
onPopState(callback("e").log(variable("e").dot("state")))

// Navigation guards (prevent accidental navigation)
navigationGuard("You have unsaved changes!")
conditionalGuard(variable("formDirty"))

// Query parameter manipulation
getQueryParam("page")
setQueryParam("page", str("2"))
removeQueryParam("filter")
```

## Drag and Drop

```java
import static com.osmig.Jweb.framework.js.JSDragDrop.*;

// Make an element draggable
draggable("card-1")
    .data("text/plain", "Card 1 data")
    .data("application/json", str("{\"id\":1}"))
    .effectAllowed("move")
    .onDragStart(callback("e").log("drag started"))
    .build();

// Create a drop zone
dropZone("target-area")
    .dropEffect("move")
    .onDrop(callback("e")
        .let_("data", getData(variable("e"), "text/plain"))
        .log("Dropped:", variable("data")))
    .build();
```

## Pointer Events

```java
import static com.osmig.Jweb.framework.js.JSPointer.*;

// Basic pointer event listening
onPointerDown("canvas", callback("e")
    .log(pointerId(variable("e")))
    .log(pressure(variable("e"))))

// Multi-touch tracking
multiPointerTracker("canvas")
    .onStart(callback("e").log("pointer down:", pointerId(variable("e"))))
    .onMove(callback("e").log("moving:", clientX(variable("e"))))
    .onEnd(callback("e").log("pointer up"))
    .build();
```

## Speech API

```java
import static com.osmig.Jweb.framework.js.JSSpeech.*;

// Text-to-speech
speak("Hello, welcome to JWeb!")

// With voice configuration
speakBuilder("Hello world")
    .lang("en-US")
    .rate(1.2)
    .pitch(1.0)
    .volume(0.8)
    .onEnd(callback().log("Done speaking"))
    .build();

// Speech recognition
recognizer()
    .lang("en-US")
    .continuous(true)
    .interimResults(true)
    .onResult(callback("e")
        .log("Heard:", transcript(variable("e"))))
    .build("sr");
```
