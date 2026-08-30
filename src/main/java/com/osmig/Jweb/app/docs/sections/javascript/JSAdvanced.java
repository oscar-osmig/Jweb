package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSAdvanced {
    private JSAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Advanced JavaScript"),
            para("Powerful APIs for complex applications."),

            h3Title("Promise Utilities"),
            codeBlock("""
import static jweb.js.JSPromise.*;

// Promise combinators
promiseAll(
    fetch("/api/users").get().toVal(),
    fetch("/api/posts").get().toVal()
).then(callback("results").call("renderAll", variable("results")))

promiseRace(promise1, promise2)  // First to finish
promiseAny(promise1, promise2)   // First successful
promiseAllSettled(promises)      // All, regardless of success

// Retry with exponential backoff
retry(variable("apiCall"), 3)    // Max 3 attempts
    .delay(1000)                 // 1s initial delay
    .exponentialBackoff()        // Double delay each retry
    .onRetry(callback("attempt").log("Retry:", variable("attempt")))
    .shouldRetry(callback("err").ret(variable("err").dot("status").eq(503)))
    .build()

// Timeout
timeout(fetch("/api/slow").get().toVal(), 3000)
    .errorMessage("Request timed out")
    .build()

// Cancellable promise
cancellable(fetch("/api/data").get().toVal())
    .controller("abortCtrl")
    .timeout(5000)
    .build("result")"""),

            h3Title("Web Workers"),
            codeBlock("""
import static jweb.js.JSWorker.*;

// Create dedicated worker
worker("/worker.js")
    .onMessage(callback("e").call("handleResult", variable("e").dot("data")))
    .onError(callback("err").log(variable("err")))
    .build("worker")

// Send data to worker
postMessage(variable("worker"), obj("task", "compute", "data", bigData))

// Terminate worker
terminate(variable("worker"))

// SharedWorker (multiple tabs)
sharedWorker("/shared-worker.js").build("sw")"""),

            h3Title("Service Workers"),
            codeBlock("""
import static jweb.js.JSServiceWorker.*;

// Register service worker
register("/sw.js")
    .onSuccess(callback("reg").log("SW registered"))
    .onError(callback("err").log(variable("err")))
    .build()

// Check for updates
update(variable("registration"))

// Unregister
unregister(variable("registration"))"""),

            h3Title("Web Crypto"),
            codeBlock("""
import static jweb.js.JSCrypto.*;

// Random values
randomUUID()     // UUID v4
randomBytes(16)  // Random bytes

// Hashing
sha256(variable("data"))  // Returns promise
digest().sha512().data(variable("data")).build()

// Encryption (AES-GCM)
encrypt().aesGcm(variable("key"), variable("iv"))
    .data(variable("data"))
    .build()

decrypt().aesGcm(variable("key"), variable("iv"))
    .data(variable("encrypted"))
    .build()

// Generate key
generateKey().aesGcm(256).build()"""),

            h3Title("Canvas 2D"),
            codeBlock("""
import static jweb.js.JSCanvas.*;

// Get context
getContext2D(variable("canvas"))

// Drawing
fillRect(variable("ctx"), 0, 0, 100, 100)
strokeRect(variable("ctx"), 10, 10, 80, 80)
clearRect(variable("ctx"), 0, 0, width, height)

// Text
fillText(variable("ctx"), str("Hello"), 50, 50)
strokeText(variable("ctx"), str("World"), 50, 80)

// Path
beginPath(variable("ctx"))
moveTo(variable("ctx"), 0, 0)
lineTo(variable("ctx"), 100, 100)
arc(variable("ctx"), 50, 50, 30, 0, Math.PI * 2)
stroke(variable("ctx"))
fill(variable("ctx"))

// Image
drawImage(variable("ctx"), variable("img"), 0, 0)"""),

            h3Title("Performance API"),
            codeBlock("""
import static jweb.js.JSPerformance.*;

// High-resolution timing
now()

// User timing marks
mark("start")
// ... operation ...
mark("end")
measure("operation", "start", "end")

// Read recorded entries
getEntriesByName("operation")
clearMarks()
clearMeasures()"""),

            h3Title("JSON & Data"),
            codeBlock("""
import static jweb.js.JSJson.*;

// Parse/stringify
parse(variable("jsonString"))
stringify(variable("obj"))
stringify(variable("obj"), 2)  // pretty-printed
safeParse(variable("jsonString"), obj())  // with fallback

// FormData
import static jweb.js.JSFormData.*;

formData("checkout-form")   // from a form element
formData()                  // empty FormData
append(variable("fd"), "key", str("value"))

// URL
import static jweb.js.JSUrl.*;

url("/path?q=search")
currentUrl()
pathname(url("/path?q=search"))"""),

            h3Title("Internationalization"),
            codeBlock("""
import static jweb.js.JSIntl.*;

// Number formatting
formatCurrency(variable("amount"), "USD", "en-US")
// Output: $1,234.56

formatNumber(variable("num"), "en-US")
formatPercent(variable("ratio"), "en-US")
formatCompact(variable("big"), "en-US")   // 1.2M

// Date formatting
formatDateTime(variable("date"), "en-US", "long", "short")
// Output: January 21, 2026 at 3:30 PM

// Relative time
formatRelativeTime(-1, "day", "en")  // "yesterday" """),

            docTip("Advanced APIs may require feature detection. Check browser support before using.")
        );
    }
}
