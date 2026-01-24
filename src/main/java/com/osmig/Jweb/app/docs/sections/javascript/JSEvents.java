package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
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
        .call("toggleTodo", variable("target").getData("id"))
    )

// Works for dynamically added items
// Single listener on parent, not one per child"""),

            h3Title("Debouncing"),
            para("Delay execution until user stops typing/scrolling."),
            codeBlock("""
// Search input - wait 300ms after typing stops
onChange("search-input")
    .then(debounce("searchTimer", 300).wrap(
        fetch("/api/search?q=" + variable("this").dot("value"))
            .get()
            .ok(setHtml("results"))
    ))

// Resize handler - wait for resize to stop
onResize(debounce("resizeTimer", 200).wrap(
    callback().call("recalculateLayout")
))"""),

            h3Title("Throttling"),
            para("Limit execution frequency."),
            codeBlock("""
// Scroll handler - run at most every 100ms
onScroll(throttle("scrollTimer", 100).wrap(
    callback().call("updateScrollPosition")
))

// Mouse move - limit to 60fps
onMouseMove("canvas", throttle("moveTimer", 16).wrap(
    callback("e").call("draw", variable("e").dot("clientX"), variable("e").dot("clientY"))
))"""),

            h3Title("Keyboard Events"),
            codeBlock("""
// Key combinations
onKeyCombo("ctrl+s", callback("e")
    .call("preventDefault", variable("e"))
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
onArrowKeys(callback("direction")
    .call("navigate", variable("direction"))
)"""),

            h3Title("Touch & Swipe"),
            codeBlock("""
// Swipe gestures
swipe(variable("carousel"))
    .threshold(100)  // Minimum distance in pixels
    .onLeft(callback().call("nextSlide"))
    .onRight(callback().call("prevSlide"))
    .onUp(callback().call("openFullscreen"))
    .onDown(callback().call("closeFullscreen"))
    .build()

// Touch events
onTouchStart("element", callback("e")
    .let_("touch", variable("e").dot("touches").at(0))
    .call("startDrag", variable("touch"))
)"""),

            h3Title("Server-Sent Events (SSE)"),
            para("Real-time server push."),
            codeBlock("""
// Connect to SSE endpoint
sse("/api/notifications")
    .onMessage(callback("e")
        .let_("data", jsonParse(variable("e").dot("data")))
        .call("showNotification", variable("data"))
    )
    .onError(callback()
        .log("SSE connection error")
    )
    .build()

// With auto-reconnect
sse("/api/events")
    .reconnectDelay(3000)
    .onOpen(callback().log("Connected"))
    .onMessage(handler)
    .build()"""),

            h3Title("Custom Events"),
            codeBlock("""
// Create and dispatch custom event
dispatchCustomEvent(
    variable("element"),
    "item-selected",
    obj("id", itemId, "name", itemName)
)

// Listen for custom event
on("item-selected", callback("e")
    .call("handleSelection", variable("e").dot("detail"))
)"""),

            h3Title("Event Utilities"),
            codeBlock("""
// Prevent default behavior
preventDefault(variable("event"))

// Stop propagation
stopPropagation(variable("event"))

// Once - remove after first call
once("button", "click", callback()
    .call("initializeOnce")
)

// Remove listener
removeListener(variable("element"), "click", variable("handler"))""")
        );
    }
}
