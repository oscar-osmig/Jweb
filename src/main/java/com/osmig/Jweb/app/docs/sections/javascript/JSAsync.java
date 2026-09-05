package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSAsync {
    private JSAsync() {}

    public static Element render() {
        return section(
            h3Title("Async/Await"),
            para("Build asynchronous JavaScript with type-safe async functions."),
            codeBlock("""
import static jweb.Js.*;

// Simple await
await(fetch("/api/data").ok(processData()))

// Async function
asyncFunc("loadDashboard")
    .does(
        assign("isLoading", "true"),
        await(fetch("/api/user").ok(assign("user", "_data"))),
        await(fetch("/api/stats").ok(assign("stats", "_data"))),
        assign("isLoading", "false"),
        call("renderDashboard")
    )"""),

            h3Title("Try-Catch-Finally"),
            para("Handle errors in async operations."),
            codeBlock("""
asyncTry(
    await(fetch("/api/data").ok(processData()))
)
.catch_(all(
    log("Load failed"),
    showMessage("status").error("Failed to load")
))
.finally_(all(
    hide("loading"),
    assign("isLoading", "false")
))"""),

            h3Title("Promise.all"),
            para("Execute multiple async operations in parallel."),
            codeBlock("""
// Parallel requests
promiseAll(
    fetch("/api/users").ok(assign("users", "_data")),
    fetch("/api/posts").ok(assign("posts", "_data")),
    fetch("/api/comments").ok(assign("comments", "_data"))
)

// With error handling
asyncFunc("loadAll")
    .does(
        show("loading"),
        asyncTry(
            promiseAll(
                fetch("/api/a").ok(assign("a", "_data")),
                fetch("/api/b").ok(assign("b", "_data"))
            ),
            call("renderAll")
        )
        .catch_(showMessage("error").error("Load failed"))
        .finally_(hide("loading"))
    )"""),

            h3Title("Sleep/Delay"),
            para("Add delays for debouncing or animations."),
            codeBlock("""
// Simple delay
sleep(1000)  // Wait 1 second

// Delay in sequence
asyncFunc("showWithDelay")
    .does(
        addClass("element", "fade-in"),
        sleep(300),
        setText("status", "Ready!")
    )

// Debounced search
asyncFunc("search")
    .params("query")
    .does(
        assign("searchQuery", "query"),
        sleep(300)
    )
    .raw("if(searchQuery !== query) return;")
    .does(
        await(fetch("/api/search?q=").appendVar("query")
            .ok(call("showResults", "_data")))
    )"""),

            docTip("Use Promise.all for parallel requests - it's faster than sequential awaits.")
        );
    }
}
