package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSEvents {
    private JSEvents() {}

    public static Element render() {
        return section(
            h3Title("Event Handling"),
            para("Advanced event patterns for interactive UIs."),

            h3Title("Event Delegation"),
            para("Efficient handling for dynamic lists."),
            codeBlock("""
// Delegate clicks within a container
delegate("todo-list", "click", "li")
    .handler(callback("e", "target")
        .call("toggleTodo", v("target").dot("dataset").dot("id"))
    )

// Works for dynamically added items
// Single listener on parent, not one per child"""),

            h3Title("Debouncing"),
            para("Delay execution until user stops typing/scrolling."),
            codeBlock("""
// debounce(ms, handler) is a self-contained function expression -
// no timer variable to name, and it attaches like any listener
byId("search-input").addEventListener("input",
    debounce(300, callback("e").call("runSearch"))
)"""),

            h3Title("Throttling"),
            para("Limit execution frequency."),
            codeBlock("""
// Runs at most every 100ms
byId("feed").addEventListener("scroll",
    throttle(100, callback("e").call("updateScrollPosition"))
)"""),

            h3Title("Keyboard Events"),
            codeBlock("""
// Key combinations - onKeyCombo prevents the browser default for you
onKeyCombo("ctrl+s", callback("e").call("save"))

onKeyCombo("ctrl+shift+p", callback("e")
    .call("openCommandPalette")
)

// Single keys
onEscape(callback().call("closeModal"))
onEnter(callback().call("submit"))
onKey("Delete", callback().call("deleteSelected"))

// Arrow navigation
onKey("ArrowLeft", callback().call("prevItem"))
onKey("ArrowRight", callback().call("nextItem"))"""),

            h3Title("Touch & Swipe"),
            codeBlock("""
// Swipe gestures
swipe(v("carousel"))
    .threshold(100)  // Minimum distance in pixels
    .onLeft(callback().call("nextSlide"))
    .onRight(callback().call("prevSlide"))
    .onUp(callback().call("openFullscreen"))
    .onDown(callback().call("closeFullscreen"))
    .build()

// Touch events
onTouchStart(byId("canvas"), callback("e")
    .let("touch", firstTouch(v("e")))
    .call("startDrag", v("touch"))
)"""),

            h3Title("Server-Sent Events (SSE)"),
            para("Real-time server push."),
            codeBlock("""
// Connect to SSE endpoint
sse("/api/notifications")
    .onMessage(callback("e")
        .let("data", JSJson.parse(v("e").dot("data")))
        .call("showNotification", v("data"))
    )
    .onError(callback()
        .log("SSE connection error")
    )
    .build()

// Named server events - SseEvent.of("notification", data)
sse("/api/events")
    .onOpen(callback().log("Connected"))
    .on("notification", callback("e")
        .call("showNotification", v("e").dot("data")))
    .build()"""),

            h3Title("Custom Events"),
            codeBlock("""
// Create and dispatch custom event (target element first)
dispatchCustomEvent(
    byId("item-list"),
    "item-selected",
    obj("id", itemId, "name", itemName)
)

// Listen for custom event
onCustomEvent(byId("item-list"), "item-selected", callback("e")
    .call("handleSelection", eventDetail(v("e")))
)"""),

            h3Title("Event Utilities"),
            codeBlock("""
// Inside any callback, the event is named e - these are statements
// on it, so they go straight into does(...)
callback("e").does(preventDefault(), call("save"))
callback("e").does(stopPropagation())

// Or, on an event you hold as an expression
preventDefault(v("someEvent"))
stopPropagation(v("someEvent"))

// Once - remove after first call
once(byId("button"), "click", callback()
    .call("initializeOnce")
)

// Remove listener
byId("button").removeEventListener("click", v("handler"))""")
        );
    }
}
