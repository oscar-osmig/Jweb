package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSNewAPIs {
    private JSNewAPIs() {}

    public static Element render() {
        return section(
            h3Title("New Browser APIs"),
            para("Additional browser APIs for offline storage, navigation, drag-and-drop, pointer input, and speech."),

            h3Title("IndexedDB"),
            para("Client-side database for structured offline storage."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSIndexedDB.*;

// Open a database (creates if not exists)
openDB("myApp", 1)
    .onUpgrade(createStore(variable("db"), "users")
        .keyPath("id").autoIncrement()
        .index("email", "email")
        .uniqueIndex("username", "username")
        .build())
    .onSuccess(callback("db").log(str("DB opened")))
    .build()

// Transaction: add and read data
transaction(variable("db"), "users", "readwrite")
    .put(obj("id", num(1), "name", str("John")))
    .getAll()
    .onComplete(callback("results").log(variable("results")))
    .build()

// Cursor iteration with key range
cursorQuery(variable("db"), "users")
    .index("email")
    .bound("a", "m")
    .onEach(callback("cursor")
        .log(variable("cursor").dot("value")))
    .build()

// Delete a database
deleteDB("myApp")"""),

            h3Title("History API"),
            para("Browser navigation control for SPA routing patterns."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSHistory.*;

// Navigate without reload
pushState("/dashboard", obj("page", str("dashboard")))
pushState("/users/42")  // URL-only

// Replace current entry (no back button)
replaceState("/settings")

// Go back/forward
back()
forward()
go(-2)  // Go back 2 steps

// Listen for back/forward navigation
onPopState(callback("e")
    .log(variable("e").dot("state")))

// Navigation guard (prompt before leaving)
navigationGuard("You have unsaved changes. Leave?")

// Query parameter helpers
getQueryParam("page")          // Read ?page=...
setQueryParam("sort", "name")  // Update URL param
removeQueryParam("filter")     // Remove param
queryParamsObject()             // All params as object"""),

            h3Title("Drag and Drop"),
            para("Native HTML5 drag-and-drop with builder pattern."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSDragDrop.*;

// Make an element draggable
draggable("card-1")
    .data("text/plain", "Card 1 data")
    .effectAllowed("move")
    .onDragStart(callback("e").log(str("dragging")))
    .onDragEnd(callback("e").log(str("done")))
    .build()

// Create a drop zone
dropZone("drop-area")
    .dropEffect("move")
    .onDrop(callback("e")
        .log(getData(variable("e"), "text/plain")))
    .onDragEnter(callback("e")
        .log(str("entered drop zone")))
    .build()

// DataTransfer helpers
getData(variable("e"), "text/plain")
getFiles(variable("e"))
getTypes(variable("e"))"""),

            h3Title("Pointer Events"),
            para("Unified mouse, touch, and pen input handling."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSPointer.*;

// Listen for pointer events
onPointerDown("canvas", callback("e")
    .log(pointerId(variable("e"))))
onPointerMove("canvas", callback("e")
    .log(pressure(variable("e"))))
onPointerUp("canvas", callback("e")
    .log(str("released")))

// Capture pointer (receive events outside element)
setPointerCapture("slider", variable("e").dot("pointerId"))
releasePointerCapture("slider", variable("e").dot("pointerId"))

// Access pointer properties
pointerId(variable("e"))      // Unique pointer ID
pointerType(variable("e"))    // "mouse", "touch", "pen"
pressure(variable("e"))       // 0.0 to 1.0
tiltX(variable("e"))          // Pen tilt angle
isPrimary(variable("e"))      // Is primary pointer?

// Multi-pointer tracking (pinch, multi-touch)
multiPointer("canvas")
    .onUpdate(callback("pointers")
        .log(variable("pointers").dot("size")))
    .build()"""),

            h3Title("Web Speech"),
            para("Text-to-speech and speech recognition APIs."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSSpeech.*;

// Text-to-Speech (simple)
speak("Hello, welcome to JWeb!")

// Text-to-Speech (with options)
speakBuilder("Hello, welcome to JWeb!")
    .lang("en-US")
    .rate(1.0).pitch(1.0).volume(0.8)
    .onEnd(callback("e").log(str("Done speaking")))
    .build()

// Control speech
pauseSpeech()
resumeSpeech()
cancelSpeech()

// Speech Recognition (speech-to-text)
recognizer()
    .lang("en-US")
    .continuous(true)
    .interimResults(true)
    .onResult(callback("e")
        .log(transcript(variable("e"))))
    .onError(callback("e")
        .log(variable("e").dot("error")))
    .build("recognizer")

// Control recognition
startRecognition(variable("recognizer"))
stopRecognition(variable("recognizer"))

// Result helpers
transcript(variable("e"))     // Get text result
confidence(variable("e"))     // Confidence score
isFinal(variable("e"))        // Is result final?"""),

            docTip("IndexedDB and Speech APIs require user permission or HTTPS. " +
                   "Always check for browser support with feature detection before using these APIs.")
        );
    }
}
