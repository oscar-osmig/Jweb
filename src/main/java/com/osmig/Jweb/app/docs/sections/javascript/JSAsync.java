package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSAsync {
    private JSAsync() {}

    public static Element render() {
        return section(
            h3Title("Async/Await"),
            para("Build asynchronous JavaScript with type-safe async functions."),
            codeBlock("""
import static com.osmig.Jweb.framework.js.Actions.*;

// Simple await
await_(fetch("/api/data").ok(processData()))

// Async function
asyncFunc("loadDashboard")
    .does(
        assignVar("isLoading", "true"),
        await_(fetch("/api/user").ok(assignVar("user", "_data"))),
        await_(fetch("/api/stats").ok(assignVar("stats", "_data"))),
        assignVar("isLoading", "false"),
        call("renderDashboard")
    )"""),

            h3Title("Try-Catch-Finally"),
            para("Handle errors in async operations."),
            codeBlock("""
asyncTry(
    await_(fetch("/api/data").ok(processData()))
)
.catch_("error",
    logError("error"),
    showMessage("status").error("Failed to load")
)
.finally_(
    hide("loading"),
    assignVar("isLoading", "false")
)"""),

            h3Title("Promise.all"),
            para("Execute multiple async operations in parallel."),
            codeBlock("""
// Parallel requests
promiseAll(
    fetch("/api/users").ok(assignVar("users", "_data")),
    fetch("/api/posts").ok(assignVar("posts", "_data")),
    fetch("/api/comments").ok(assignVar("comments", "_data"))
)

// With error handling
asyncFunc("loadAll")
    .does(
        show("loading"),
        asyncTry(
            promiseAll(
                fetch("/api/a").ok(assignVar("a", "_data")),
                fetch("/api/b").ok(assignVar("b", "_data"))
            ),
            call("renderAll")
        )
        .catch_("e", showMessage("error").error("Load failed"))
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
        assignVar("searchQuery", "query"),
        sleep(300),
        raw("if(searchQuery !== query) return"),
        await_(fetch("/api/search?q=").appendVar("query")
            .ok(call("showResults", "_data")))
    )"""),

            docTip("Use Promise.all for parallel requests - it's faster than sequential awaits.")
        );
    }
}
