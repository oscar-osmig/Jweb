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
).then(callback("results").call("renderAll", v("results")))

promiseRace(promise1, promise2)  // First to finish
promiseAny(promise1, promise2)   // First successful
promiseAllSettled(promises)      // All, regardless of success

// Retry with exponential backoff
retry(v("apiCall"), 3)    // Max 3 attempts
    .delay(1000)                 // 1s initial delay
    .exponentialBackoff()        // Double delay each retry
    .onRetry(callback("attempt").log("Retry:", v("attempt")))
    .shouldRetry(callback("err").return_(v("err").dot("status").eq(503)))
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
    .onMessage(callback("e").call("handleResult", v("e").dot("data")))
    .onError(callback("err").log(v("err")))
    .build("worker")

// Send data to worker
postMessage(v("worker"), obj("task", "compute", "data", bigData))

// Terminate worker
terminate(v("worker"))

// SharedWorker (multiple tabs)
sharedWorker("/shared-worker.js").build("sw")"""),

            h3Title("Service Workers"),
            codeBlock("""
import static jweb.js.JSServiceWorker.*;

// Register service worker
register("/sw.js")
    .onSuccess(callback("reg").log("SW registered"))
    .onError(callback("err").log(v("err")))
    .build()

// Check for updates
update(v("registration"))

// Unregister
unregister(v("registration"))"""),

            h3Title("Web Crypto"),
            codeBlock("""
import static jweb.js.JSCrypto.*;

// Random values
randomUUID()     // UUID v4
randomBytes(16)  // Random bytes

// Hashing
sha256(v("data"))  // Returns promise
digest().sha512().data(v("data")).build()

// Encryption (AES-GCM)
encrypt().aesGcm(v("key"), v("iv"))
    .data(v("data"))
    .build()

decrypt().aesGcm(v("key"), v("iv"))
    .data(v("encrypted"))
    .build()

// Generate key
generateKey().aesGcm(256).build()"""),

            h3Title("Canvas 2D"),
            codeBlock("""
import static jweb.js.JSCanvas.*;

// Get context
getContext2D(v("canvas"))

// Drawing
fillRect(v("ctx"), 0, 0, 100, 100)
strokeRect(v("ctx"), 10, 10, 80, 80)
clearRect(v("ctx"), 0, 0, width, height)

// Text
fillText(v("ctx"), str("Hello"), 50, 50)
strokeText(v("ctx"), str("World"), 50, 80)

// Path
beginPath(v("ctx"))
moveTo(v("ctx"), 0, 0)
lineTo(v("ctx"), 100, 100)
arc(v("ctx"), 50, 50, 30, 0, Math.PI * 2)
stroke(v("ctx"))
fill(v("ctx"))

// Image
drawImage(v("ctx"), v("img"), 0, 0)"""),

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
parse(v("jsonString"))
stringify(v("obj"))
stringify(v("obj"), 2)  // pretty-printed
safeParse(v("jsonString"), obj())  // with fallback

// FormData
import static jweb.js.JSFormData.*;

formData("checkout-form")   // from a form element
formData()                  // empty FormData
append(v("fd"), "key", str("value"))

// URL
import static jweb.js.JSUrl.*;

url("/path?q=search")
currentUrl()
pathname(url("/path?q=search"))"""),

            h3Title("Internationalization"),
            codeBlock("""
import static jweb.js.JSIntl.*;

// Number formatting
formatCurrency(v("amount"), "USD", "en-US")
// Output: $1,234.56

formatNumber(v("num"), "en-US")
formatPercent(v("ratio"), "en-US")
formatCompact(v("big"), "en-US")   // 1.2M

// Date formatting
formatDateTime(v("date"), "en-US", "long", "short")
// Output: January 21, 2026 at 3:30 PM

// Relative time
formatRelativeTime(-1, "day", "en")  // "yesterday" """),

            docTip("Advanced APIs may require feature detection. Check browser support before using.")
        );
    }
}
