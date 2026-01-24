package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSBrowserAPIs {
    private JSBrowserAPIs() {}

    public static Element render() {
        return section(
            h3Title("Browser APIs"),
            para("Type-safe access to browser APIs."),

            h3Title("Storage"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSStorage.*;

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
    .if_(variable("e").dot("key").eq("theme"))
    .then(call("updateTheme", variable("e").dot("newValue")))
)"""),

            h3Title("WebSocket"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSWebSocket.*;

// Create connection
webSocket("/ws/chat")
    .onOpen(callback().log("Connected"))
    .onMessage(callback("e")
        .call("handleMessage", variable("e").dot("data"))
    )
    .onClose(callback().log("Disconnected"))
    .onError(callback("e").log(variable("e")))
    .autoReconnect(3000)  // Auto-reconnect every 3s
    .build("ws")

// Send messages
send(variable("ws"), variable("message"))
sendJson(variable("ws"), obj("type", "chat", "text", variable("msg")))

// Close
close(variable("ws"))"""),

            h3Title("Clipboard"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSClipboard.*;

// Write to clipboard
clipboardWrite(str("Copied text"))

// Copy from element
clipboardWriteFromElement(variable("inputElem"))

// Read (returns promise)
clipboardRead().then(callback("text").log(variable("text")))"""),

            h3Title("Notifications"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSNotification.*;

// Check permission
notificationPermission()

// Request permission
requestNotificationPermission()

// Show notification
showNotification("New Message", obj(
    "body", "You have a new message",
    "icon", "/icon.png"
))"""),

            h3Title("Geolocation"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSGeolocation.*;

// Get current position
getCurrentPosition()
    .onSuccess(callback("pos")
        .let_("lat", variable("pos").dot("coords").dot("latitude"))
        .let_("lng", variable("pos").dot("coords").dot("longitude"))
        .call("showOnMap", variable("lat"), variable("lng"))
    )
    .onError(callback("err").log(variable("err")))
    .build()

// Watch position (continuous updates)
watchPosition(callback("pos")
    .call("updatePosition", variable("pos").dot("coords"))
)"""),

            h3Title("Web Share"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSShare.*;

// Share content
share(obj(
    "title", "Check this out",
    "text", "Interesting article",
    "url", window.location.href
))

// Check if sharing is supported
if_(canShare())
    .then(showShareButton())"""),

            h3Title("Fullscreen"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSFullscreen.*;

// Enter fullscreen
requestFullscreen(variable("videoElem"))

// Exit fullscreen
exitFullscreen()

// Check state
isFullscreen()"""),

            h3Title("Page Visibility"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSVisibility.*;

// Check if visible
isPageVisible()

// Handle visibility change
onVisibilityChange(callback()
    .if_(isPageVisible())
        .then(call("resumeVideo"))
    .else_(call("pauseVideo"))
)"""),

            h3Title("Observers"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSObservers.*;

// IntersectionObserver (lazy loading, infinite scroll)
intersectionObserver(callback("entries")
    .forEach(callback("entry")
        .if_(variable("entry").dot("isIntersecting"))
        .then(call("lazyLoad", variable("entry").dot("target")))
    )
).observe(queryAll("img[data-src]"))

// ResizeObserver
resizeObserver(callback("entries")
    .forEach(callback("entry")
        .call("handleResize", variable("entry").dot("contentRect"))
    )
).observe(variable("container"))

// MutationObserver
mutationObserver(callback("mutations")
    .call("handleDOMChanges", variable("mutations"))
).observe(variable("element"), obj(
    "childList", true,
    "subtree", true
))""")
        );
    }
}
