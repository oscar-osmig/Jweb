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
// Build a debounced handler body (runs 300ms after the last event)
Val debounced = debounce("searchTimer", 300).wrap(
    callback().call("runSearch")
);

// Attach it inside an input listener
byId("search-input").addEventListener("input",
    callback("e").raw(debounced.js())
)"""),

            h3Title("Throttling"),
            para("Limit execution frequency."),
            codeBlock("""
// Throttled handler body - runs at most every 100ms
Val throttled = throttle("scrollLast", 100).wrap(
    callback().call("updateScrollPosition")
);

byId("feed").addEventListener("scroll",
    callback("e").raw(throttled.js())
)"""),

            h3Title("Keyboard Events"),
            codeBlock("""
// Key combinations
onKeyCombo("ctrl+s", callback("e")
    .raw("e.preventDefault()")
    .call("save")
)

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

// With open handler
sse("/api/events")
    .onOpen(callback().log("Connected"))
    .onMessage(handler)
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
// Prevent default behavior
preventDefault(v("event"))

// Stop propagation
stopPropagation(v("event"))

// Once - remove after first call
once(byId("button"), "click", callback()
    .call("initializeOnce")
)

// Remove listener
byId("button").removeEventListener("click", v("handler"))""")
        );
    }
}
