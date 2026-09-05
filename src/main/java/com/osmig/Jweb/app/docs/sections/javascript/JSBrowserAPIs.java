package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSBrowserAPIs {
    private JSBrowserAPIs() {}

    public static Element render() {
        return section(
            h3Title("Browser APIs"),
            para("Type-safe access to browser APIs."),

            h3Title("Storage"),
            codeBlock("""
import static jweb.js.JSStorage.*;

// localStorage
local().set("token", "abc123")
local().get("token")
local().remove("token")
local().clear()

// JSON storage
local().setJson("user", obj("name", "John", "id", 123))
local().getJson("user")
local().getJsonOr("user", obj())  // With fallback

// sessionStorage
session().set("temp", "value")
session().get("temp")

// Cross-tab communication
onStorageChange(callback("e")
    .if_(v("e").dot("key").eq("theme"),
        call("updateTheme", v("e").dot("newValue")))
)"""),

            h3Title("WebSocket"),
            codeBlock("""
import static jweb.js.JSWebSocket.*;

// Create connection
webSocket("/ws/chat")
    .onOpen(callback().log("Connected"))
    .onMessage(callback("e")
        .call("handleMessage", v("e").dot("data"))
    )
    .onClose(callback().log("Disconnected"))
    .onError(callback("e").log(v("e")))
    .autoReconnect(3000)  // Auto-reconnect every 3s
    .build("ws")

// Send messages
send(v("ws"), v("message"))
sendJson(v("ws"), obj("type", "chat", "text", v("msg")))

// Close
close(v("ws"))"""),

            h3Title("Clipboard"),
            codeBlock("""
import static jweb.js.JSClipboard.*;

// Write to clipboard
copyText("Copied text")

// Copy an element's text or value
copyElementText("code-snippet")
copyElementValue("share-url")

// Read (returns promise)
readText().then(callback("text").log(v("text")))"""),

            h3Title("Notifications"),
            codeBlock("""
import static jweb.js.JSNotification.*;

// Check permission
permission()
hasPermission()

// Request permission
requestPermission()

// Show notification
notification("New Message")
    .body("You have a new message")
    .icon("/icon.png")
    .build()"""),

            h3Title("Geolocation"),
            codeBlock("""
import static jweb.js.JSGeolocation.*;

// Get current position
getCurrentPosition()
    .onSuccess(callback("pos")
        .let("lat", v("pos").dot("coords").dot("latitude"))
        .let("lng", v("pos").dot("coords").dot("longitude"))
        .call("showOnMap", v("lat"), v("lng"))
    )
    .onError(callback("err").log(v("err")))
    .build()

// Watch position (continuous updates)
watchPosition()
    .onSuccess(callback("pos")
        .call("updatePosition", v("pos").dot("coords"))
    )
    .build("watchId")"""),

            h3Title("Web Share"),
            codeBlock("""
import static jweb.js.JSShare.*;

// Share content
share()
    .title("Check this out")
    .text("Interesting article")
    .url("/article/42")
    .build()

// Quick URL share
shareUrl("/article/42")

// Check if sharing is supported
canShare()   // boolean Val for use in conditions"""),

            h3Title("Fullscreen"),
            codeBlock("""
import static jweb.js.JSFullscreen.*;

// Enter fullscreen
requestFullscreen(v("videoElem"))

// Exit fullscreen
exitFullscreen()

// Check state
isFullscreen()"""),

            h3Title("Page Visibility"),
            codeBlock("""
import static jweb.js.JSVisibility.*;

// Check if visible
isVisible()
isHidden()

// Handle visibility change
onVisibilityChange(callback()
    .if_(isVisible())
        .then(call("resumeVideo"))
    .else_(call("pauseVideo"))
)

// Or use the dedicated helpers
onVisible(callback().call("resumeVideo"))
onHidden(callback().call("pauseVideo"))"""),

            h3Title("Observers"),
            codeBlock("""
import static jweb.js.JSObservers.*;

// IntersectionObserver (lazy loading, infinite scroll)
intersection()
    .onIntersect(callback("entries")
        .call("lazyLoadVisible", v("entries"))
    )
    .threshold(0.5)
    .observe("hero-img", "heroObserver")

// ResizeObserver
resize()
    .onResize(callback("entries")
        .call("handleResize", v("entries"))
    )
    .observe("container", "containerObserver")

// MutationObserver
mutation()
    .onMutate(callback("mutations")
        .call("handleDOMChanges", v("mutations"))
    )
    .childList()
    .subtree()
    .observe("content", "contentObserver")""")
        );
    }
}
