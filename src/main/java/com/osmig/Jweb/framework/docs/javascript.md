# JavaScript DSL

JWeb provides a type-safe DSL for generating JavaScript without writing raw JS.

## Basic Usage

```java
import static com.osmig.Jweb.framework.js.Actions.*;

// Simple script
String js = script()
    .add(onClick("btn").then(toggle("panel")))
    .build();
```

---

## Level 1: Simple Actions

```java
// Toggle visibility
toggle("element-id")

// Show/hide elements
show("panel")
hide("modal")
show("menu", "flex")  // With display mode

// Set text content
setText("status", "Loading...")

// Add/remove CSS classes
addClass("card", "active")
removeClass("card", "loading")
toggleClass("menu", "open")
```

---

## Level 2: Click Handlers

```java
// Simple click handler
onClick("btn-submit").then(toggle("panel"))

// Multiple actions on click
onClick("save-btn").then(all(
    hide("form"),
    show("success-message"),
    call("saveData")
))

// Click with function call
onClick("delete-btn").then(call("deleteItem", "itemId"))
```

---

## Level 3: Form Handlers

```java
// Basic form submission
onSubmit("contact-form")
    .post("/api/contact")
    .ok(showMessage("status").success("Message sent!"))
    .fail(showMessage("status").error("Failed to send"))

// With loading state
onSubmit("login-form")
    .loading("Signing in...")
    .post("/api/login")
    .ok(navigateTo("/dashboard"))
    .fail(responseError("error-message")
        .on401("Invalid credentials")
        .otherwise("Server error"))

// With before/after actions
onSubmit("register-form")
    .loading("Creating account...")
    .before(all(
        hide("error-message"),
        getInputValue("email").storeTo("userEmail")
    ))
    .post("/api/register")
    .ok(all(
        showMessage("status").success("Account created!"),
        resetForm("register-form"),
        navigateTo("/welcome")
    ))
    .fail(responseError("error-message"))
```

---

## Level 4: Script Builder

```java
// Complete script with helpers and state
String js = script()
    .withHelpers()  // Add $_, show, hide, toggle helpers

    // Define state variables
    .state(state()
        .var("isLoading", false)
        .var("currentUser", "null")
        .array("items")
        .var("selectedId", "''"))

    // Define DOM references
    .refs(refs()
        .add("container", "main-container")
        .add("modal", "modal-overlay")
        .add("form", "data-form"))

    // Add handlers
    .add(onClick("open-modal").then(show("modal-overlay", "flex")))
    .add(onClick("close-modal").then(hide("modal-overlay")))
    .add(onSubmit("data-form")
        .loading("Saving...")
        .post("/api/data")
        .ok(call("refreshData")))

    .build();
```

---

## Level 5: Functions

```java
// Define a function
define("handleClick")
    .does(
        toggle("panel"),
        call("logEvent", "'click'")
    )

// Function with parameters
define("showUser")
    .params("userId", "userName")
    .does(
        setText("user-id", "userId"),
        setText("user-name", "userName"),
        show("user-panel")
    )

// Window-scoped function (globally accessible)
windowFunc("doSomething")
    .params("data")
    .does(
        call("processData", "data"),
        show("result")
    )

// Async function
asyncFunc("loadData")
    .does(
        show("loading"),
        await_(fetch("/api/data").ok(assignVar("data", "_data"))),
        hide("loading"),
        call("renderData")
    )
```

---

## Level 6: Fetch Builder

```java
// GET request
fetch("/api/users")
    .ok(assignVar("users", "_data"))
    .fail(showMessage("error").error("Failed to load"))

// POST with JSON
fetch("/api/users")
    .post()
    .body("{name: userName, email: userEmail}")
    .ok(call("onUserCreated"))

// Dynamic URL from variable
fetch("/api/items/").appendVar("itemId")
    .ok(call("showItem", "_data"))

// URL from expression
fetch("").urlFromVar("apiEndpoint + '/search?q=' + query")
    .ok(assignVar("results", "_data"))

// Headers from variables
fetch("/api/protected")
    .headerFromVar("Authorization", "authToken")
    .headerFromVar("X-Request-ID", "requestId")
    .ok(processResponse())

// Handle specific status codes
fetch("/api/data")
    .onStatus(401, navigateTo("/login"))
    .onStatus(404, showMessage("error").error("Not found"))
    .onStatus(500, showMessage("error").error("Server error"))
    .ok(processData())

// Change HTTP method
fetch("/api/resource").post()
fetch("/api/resource").put()
fetch("/api/resource").delete()
```

---

## Level 7: Async/Await

```java
// Simple await
await_(fetch("/api/data").ok(processData()))

// Async function with await
asyncFunc("loadDashboard")
    .does(
        assignVar("isLoading", "true"),
        await_(fetch("/api/user").ok(assignVar("user", "_data"))),
        await_(fetch("/api/stats").ok(assignVar("stats", "_data"))),
        assignVar("isLoading", "false"),
        call("renderDashboard")
    )

// Try-catch-finally
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
)

// Promise.all for parallel requests
promiseAll(
    fetch("/api/users").ok(assignVar("users", "_data")),
    fetch("/api/posts").ok(assignVar("posts", "_data")),
    fetch("/api/comments").ok(assignVar("comments", "_data"))
)

// Sleep/delay
sleep(1000)  // Wait 1 second
sleep(500)   // Wait 500ms
```

---

## Level 8: DOM Queries

```java
// Single element query
query("#status")
    .setText("Updated!")
    .addClass("success")

// Query with attribute selector
query("[data-active='true']")
    .removeClass("hidden")
    .addClass("visible")

// Query all matching elements
queryAll(".notification")
    .forEach(el -> el.addClass("fade-out"))

// Chained operations
query("#user-panel")
    .removeClass("loading")
    .addClass("loaded")
    .attr("data-ready", "true")
    .show("flex")
```

---

## Level 9: Event Handlers

```java
// Element event
onEvent("click").on("close-btn").then(hideModal("modal"))

// Document event
onEvent("keydown").onDocument().then(
    raw("if(e.key==='Escape')hideModal('modal')")
)

// Window event
onEvent("resize").onWindow().then(call("handleResize"))

// Conditional event
onEvent("click").on("logout-btn")
    .when("isLoggedIn")
    .then(call("logout"))

// Multiple event listeners
script()
    .add(onEvent("click").on("modal-overlay").then(hideOnBackdropClick("modal-overlay")))
    .add(onEvent("popstate").when("isLoggedIn").then(call("clearSession")))
    .add(onEvent("visibilitychange").onDocument().when("document.hidden").then(call("pauseUpdates")))
    .build()
```

---

## Level 10: Modals and Dialogs

```java
// Show modal
showModal("modal-overlay")

// Hide modal
hideModal("modal-overlay")

// Hide on backdrop click
hideOnBackdropClick("modal-overlay")

// Confirm dialog
confirmDialog("confirm-modal")
    .message("Are you sure?")
    .onConfirm(call("doDelete"))

// Danger confirm (styled differently)
confirmDialog("delete-modal")
    .message("Delete this item permanently?")
    .danger()
    .onConfirm(fetch("/api/items/").appendVar("id").delete()
        .ok(call("refreshList")))

// Alert modal with content
alertModal("modal-overlay", "modal-body")
    .success("Operation completed!")
    .detail("Your changes have been saved.")
    .button("Close", "hideModal('modal-overlay')")
```

---

## Level 11: State Management

```java
// Define state variables
state()
    .var("isLoading", false)
    .var("currentUser", "null")
    .var("theme", "\"light\"")
    .array("items")
    .array("selectedIds")

// Assign to variable
assignVar("isLoading", "true")
assignVar("user", "_data.user")
assignVar("items", "_data.items || []")

// Reset variables
resetVars()
    .var("isLoading", false)
    .var("currentUser", "null")
    .array("items")
    .resetForm("myForm")

// Get input value and store
getInputValue("email-input").storeTo("userEmail")
getInputValue("password-input").storeTo("password")
```

---

## Level 12: Templates (HTML Generation)

```java
// Template for list items
template("item")
    .div().style("padding:1rem;border:1px solid #ddd").child()
        .div().style("font-weight:bold").text(escapedField("name")).end()
        .div().style("color:#666").text(field("description")).end()
        .button().onClick("selectItem", "id").text("Select").end()
    .end()
    .buildAs("itemCard")

// Render list using template
renderList("items-container")
    .from("items")
    .using("itemCard")
    .empty("No items found")
    .build()

// Template with conditionals
template("r")
    .div().style("padding:1rem").child()
        .div().text(escapedField("title")).end()
        .when("isAdmin")
            .button().onClick("edit", "id").text("Edit").end()
        .endWhen()
        .whenEquals("status", "ACTIVE")
            .span().style("color:green").text("Active").end()
        .endWhen()
    .end()
    .buildAs("rowTemplate")
```

---

## Level 13: Tabs

```java
// Tab switching
tabs(".tab-btn")
    .storesIn("currentTab")
    .onSwitch(call("loadTabContent"))

// In your HTML
div(class_("tabs"),
    button(class_("tab-btn"), data("tab", "overview"), "Overview"),
    button(class_("tab-btn"), data("tab", "details"), "Details"),
    button(class_("tab-btn"), data("tab", "settings"), "Settings")
)
```

---

## Level 14: External Services

```java
// Call external service (e.g., EmailJS)
externalService("emailjs")
    .call("send", "'service_id'", "'template_id'", "{email: userEmail}")
    .ok(showMessage("status").success("Email sent!"))
    .notAvailable(showMessage("status").error("Service not available"))
    .fail(showMessage("status").error("Failed to send"))
```

---

## Level 15: Combining Everything

```java
public class AdminScripts {
    public static String handlers() {
        return script()
            .withHelpers()

            .state(state()
                .var("isLoggedIn", false)
                .var("currentUser", "null")
                .array("items"))

            .refs(refs()
                .add("container", "items-container")
                .add("modal", "modal-overlay")
                .add("form", "item-form"))

            // Template for item cards
            .raw(template("item")
                .div().style("padding:1rem;background:#fff;margin:0.5rem;border-radius:8px").child()
                    .div().style("font-weight:bold").text(escapedField("name")).end()
                    .button().onClick("editItem", "id").text("Edit").end()
                    .button().onClick("deleteItem", "id").text("Delete").end()
                .end()
                .buildAs("itemCard"))

            // Render function
            .add(define("renderItems")
                .does(raw(renderList("items-container")
                    .from("items")
                    .using("itemCard")
                    .empty("No items yet")
                    .build())))

            // Load items
            .add(asyncFunc("loadItems")
                .does(
                    show("loading"),
                    fetch("/api/items")
                        .headerFromVar("Authorization", "authToken")
                        .ok(all(
                            assignVar("items", "_data"),
                            call("renderItems")
                        ))
                        .fail(showMessage("error").error("Failed to load")),
                    hide("loading")
                ))

            // Delete with confirmation
            .add(windowFunc("deleteItem")
                .params("id")
                .does(
                    confirmDialog("modal-overlay")
                        .message("Delete this item?")
                        .danger()
                        .onConfirm(
                            fetch("/api/items/").appendVar("id").delete()
                                .headerFromVar("Authorization", "authToken")
                                .ok(call("loadItems"))
                                .fail(alertModal("modal-overlay", "modal-body")
                                    .error("Failed to delete"))
                        )
                ))

            // Form submission
            .add(onSubmit("item-form")
                .loading("Saving...")
                .before(hide("form-error"))
                .post("/api/items")
                .headerFrom("Authorization", "authToken")
                .ok(all(
                    hideModal("modal-overlay"),
                    resetForm("item-form"),
                    call("loadItems"),
                    showMessage("status").success("Item saved!")
                ))
                .fail(responseError("form-error")))

            // Event handlers
            .add(onEvent("click").on("modal-overlay").then(hideOnBackdropClick("modal-overlay")))
            .add(onEvent("keydown").onDocument().when("e.key==='Escape'").then(hideModal("modal-overlay")))

            .build();
    }
}
```

---

## Using in Templates

```java
public class AdminPage implements Template {
    @Override
    public Element render() {
        return html(
            head(
                title("Admin"),
                style(getStyles())
            ),
            body(
                div(id("container"),
                    // Page content...
                ),
                div(id("modal-overlay"), class_("modal"),
                    div(id("modal-body"))
                ),
                script(AdminScripts.handlers())
            )
        );
    }
}
```

---

## Action Reference

| Action | Description |
|--------|-------------|
| `show(id)` | Show element |
| `show(id, display)` | Show with display mode |
| `hide(id)` | Hide element |
| `toggle(id)` | Toggle visibility |
| `setText(id, text)` | Set text content |
| `addClass(id, class)` | Add CSS class |
| `removeClass(id, class)` | Remove CSS class |
| `toggleClass(id, class)` | Toggle CSS class |
| `call(func, args...)` | Call function |
| `assignVar(name, value)` | Assign to variable |
| `navigateTo(url)` | Navigate to URL |
| `showModal(id)` | Show modal |
| `hideModal(id)` | Hide modal |
| `resetForm(id)` | Reset form |
| `focus(id)` | Focus element |
| `all(actions...)` | Combine actions |
| `raw(js)` | Raw JavaScript |
| `noop()` | No operation |
