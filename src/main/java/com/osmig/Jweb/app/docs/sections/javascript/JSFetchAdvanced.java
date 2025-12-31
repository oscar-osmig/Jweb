package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSFetchAdvanced {
    private JSFetchAdvanced() {}

    public static Element render() {
        return section(
            h3Title("Advanced Fetch Builder"),
            para("Enhanced fetch builder with dynamic URLs, headers, and status handling."),

            h3Title("Dynamic URLs"),
            para("Build URLs from variables and expressions."),
            codeBlock("""
// Append variable to URL
fetch("/api/items/").appendVar("itemId")
    .ok(call("showItem", "_data"))
// Generates: fetch('/api/items/' + itemId)

// URL from expression
fetch("").urlFromVar("apiEndpoint + '/search?q=' + query")
    .ok(assignVar("results", "_data"))
// Generates: fetch(apiEndpoint + '/search?q=' + query)

// Multiple parameters
fetch("/api/users/").appendVar("userId").append("/posts")
    .ok(assignVar("posts", "_data"))
// Generates: fetch('/api/users/' + userId + '/posts')"""),

            h3Title("Dynamic Headers"),
            para("Set headers from variables for authentication and custom headers."),
            codeBlock("""
// Auth token from variable
fetch("/api/protected")
    .headerFromVar("Authorization", "authToken")
    .ok(processResponse())
// Generates: headers: { 'Authorization': authToken }

// Multiple dynamic headers
fetch("/api/data")
    .headerFromVar("Authorization", "'Bearer ' + token")
    .headerFromVar("X-Request-ID", "requestId")
    .headerFromVar("X-Tenant-ID", "tenantId")
    .ok(processData())

// Static + dynamic headers
fetch("/api/resource")
    .header("Content-Type", "application/json")
    .headerFromVar("Authorization", "authToken")
    .post()
    .body("{name: userName}")
    .ok(showMessage("status").success("Saved!"))"""),

            h3Title("Status Code Handling"),
            para("Handle specific HTTP status codes with custom actions."),
            codeBlock("""
// Handle specific statuses
fetch("/api/data")
    .onStatus(401, navigateTo("/login"))
    .onStatus(403, showMessage("error").error("Access denied"))
    .onStatus(404, showMessage("error").error("Not found"))
    .onStatus(500, showMessage("error").error("Server error"))
    .ok(processData())

// Different handling per status
fetch("/api/users/" + userId)
    .delete()
    .headerFromVar("Authorization", "token")
    .onStatus(204, all(
        call("removeFromList", "userId"),
        showMessage("status").success("User deleted")
    ))
    .onStatus(409, showMessage("error").error("Cannot delete"))
    .fail(showMessage("error").error("Delete failed"))"""),

            h3Title("Complete Example"),
            codeBlock("""
// CRUD operations with auth
windowFunc("saveUser")
    .params("userData")
    .async()
    .does(
        show("saving"),
        fetch("/api/users")
            .post()
            .headerFromVar("Authorization", "'Bearer ' + authToken")
            .headerFromVar("X-Request-ID", "crypto.randomUUID()")
            .body("userData")
            .onStatus(201, all(
                hideModal("user-modal"),
                call("refreshList"),
                showMessage("status").success("User created!")
            ))
            .onStatus(400, responseError("form-errors"))
            .onStatus(401, navigateTo("/login"))
            .fail(showMessage("status").error("Network error")),
        hide("saving")
    )"""),

            docTip("Use onStatus for precise control over different server responses.")
        );
    }
}
