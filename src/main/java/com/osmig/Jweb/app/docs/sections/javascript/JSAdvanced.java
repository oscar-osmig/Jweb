package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSAdvanced {
    private JSAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Advanced JavaScript"),
            para("Powerful APIs for complex applications."),

            h3Title("Promise Utilities"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSPromise.*;

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
import static com.osmig.Jweb.framework.js.JSWorker.*;

// Create dedicated worker
dedicatedWorker("/worker.js")
    .onMessage(callback("e").call("handleResult", variable("e").dot("data")))
    .onError(callback("err").log(variable("err")))
    .build("worker")

// Send data to worker
workerPostMessage(variable("worker"), obj("task", "compute", "data", bigData))

// Terminate worker
workerTerminate(variable("worker"))

// SharedWorker (multiple tabs)
sharedWorker("/shared-worker.js").build("sw")"""),

            h3Title("Service Workers"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSServiceWorker.*;

// Register service worker
registerServiceWorker("/sw.js")
    .onSuccess(callback("reg").log("SW registered"))
    .onError(callback("err").log(variable("err")))

// Check for updates
checkForUpdates(variable("registration"))

// Unregister
unregisterServiceWorker()"""),

            h3Title("Web Crypto"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSCrypto.*;

// Random values
cryptoRandomUUID()     // UUID v4
cryptoRandomBytes(16)  // Random bytes

// Hashing
sha256(variable("data"))  // Returns promise
sha512(variable("data"))

// Encryption (AES)
aesEncrypt(variable("key"), variable("data"), variable("iv"))
aesDecrypt(variable("key"), variable("encrypted"), variable("iv"))

// Generate key
generateAESKey().then(callback("key").call("storeKey", variable("key")))"""),

            h3Title("Canvas 2D"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSCanvas.*;

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
import static com.osmig.Jweb.framework.js.JSPerformance.*;

// High-resolution timing
performanceNow()

// User timing marks
performanceMark("start")
// ... operation ...
performanceMark("end")
performanceMeasure("operation", "start", "end")

// Observe performance entries
performanceObserver(callback("entries")
    .forEach(callback("entry")
        .log(variable("entry").dot("name"), variable("entry").dot("duration"))
    )
).observe(obj("entryTypes", array("measure")))"""),

            h3Title("JSON & Data"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSJson.*;

// Parse/stringify
jsonParse(variable("jsonString"))
jsonStringify(variable("obj"))
jsonStringifyPretty(variable("obj"), 2)

// FormData
import static com.osmig.Jweb.framework.js.JSFormData.*;

formData(variable("formElement"))
formDataEmpty()
formDataAppend(variable("fd"), "key", "value")

// URL
import static com.osmig.Jweb.framework.js.JSUrl.*;

urlParse(str("/path?q=search"))
urlSearchParams(obj("q", "search", "page", 1))
urlSearchParamsGet(variable("params"), "q")"""),

            h3Title("Internationalization"),
            codeBlock("""
import static com.osmig.Jweb.framework.js.JSIntl.*;

// Number formatting
numberFormat("en-US", obj(
    "style", "currency",
    "currency", "USD"
)).format(variable("amount"))
// Output: $1,234.56

// Date formatting
dateTimeFormat("en-US", obj(
    "dateStyle", "long",
    "timeStyle", "short"
)).format(variable("date"))
// Output: January 21, 2026 at 3:30 PM

// Relative time
relativeTimeFormat("en", obj("numeric", "auto"))
    .format(-1, "day")  // "yesterday" """),

            docTip("Advanced APIs may require feature detection. Check browser support before using.")
        );
    }
}
