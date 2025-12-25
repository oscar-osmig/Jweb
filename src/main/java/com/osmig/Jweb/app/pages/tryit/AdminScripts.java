package com.osmig.Jweb.app.pages.tryit;

import static com.osmig.Jweb.framework.js.Actions.*;

/** JavaScript handlers for Admin page. */
public final class AdminScripts {
    private AdminScripts() {}

    public static String adminHandlers() {
        return script()
            .withHelpers()
            .state(state()
                .var("adminKey", "")
                .var("adminEmail", "")
                .var("currentTab", "pending")
                .array("requests")
                .var("isLoggedIn", false))
            .refs(refs()
                .add("container", "requests-container")
                .add("loginOverlay", "login-overlay")
                .add("loginForm", "login-form")
                .add("loginError", "login-error")
                .add("modalOverlay", "modal-overlay")
                .add("modalBody", "modal-body")
                .add("adminContent", "admin-content"))
            // Color helpers using DSL
            .add(colorSwitch("status")
                .when("PENDING", "#fef3c7")
                .when("APPROVED", "#d1fae5")
                .otherwise("#fee2e2"), "statusBg")
            .add(colorSwitch("status")
                .when("PENDING", "#92400e")
                .when("APPROVED", "#065f46")
                .otherwise("#991b1b"), "statusTxt")
            .raw(requestCardTemplate())
            .raw(renderRequestsFunc())
            // Async load function using DSL
            .add(asyncFunc("loadRequests")
                .guard("!isLoggedIn")
                .does(
                    fetch("").urlFromVar(ternaryUrl("currentTab==='pending'",
                            "/api/try-it/admin/requests", "/api/try-it/admin/requests/all"))
                        .headerFromVar("X-Admin-Key", "adminKey")
                        .headerFromVar("X-Admin-Email", "adminEmail")
                        .onStatus(401, all(
                            assignVar("isLoggedIn", "false"),
                            show("login-overlay", "flex"),
                            hide("admin-content")
                        ))
                        .ok(all(
                            assignVar("requests", "_data"),
                            call("renderRequests")
                        ))
                ))
            // Approve request with confirm + alert modal
            .add(windowFunc("approveReq")
                .params("id", "email")
                .does(
                    confirmDialog("modal-overlay")
                        .message("Approve this request?")
                        .onConfirm(all(
                            fetch("/api/try-it/admin/approve/").appendVar("id").post()
                                .headerFromVar("X-Admin-Key", "adminKey")
                                .headerFromVar("X-Admin-Email", "adminEmail")
                                .ok(all(
                                    alertModal("modalOverlay", "modalBody")
                                        .success("Approved!")
                                        .detailExpr("'<p style=\"color:#6b7280;margin-bottom:0.5rem\">Token for '+esc(email)+':'+'</p>'")
                                        .inputField("token-input", "_data.token")
                                        .button("Copy Token", "copyToken()", "#6366f1")
                                        .button("Send by Email", "sendTokenEmail(\\'" + "'+esc(email)+'" + "\\',\\'" + "'+_data.token+'" + "\\')", "#10b981")
                                        .statusArea("modal-status"),
                                    call("loadRequests")
                                ))
                                .fail(alertModal("modalOverlay", "modalBody")
                                    .error("Error")
                                    .detailExpr("'<p>'+(_data.message||'Failed')+'</p>'"))
                        ))
                ))
            // Reject request with confirm
            .add(windowFunc("rejectReq")
                .params("id")
                .does(
                    confirmDialog("modal-overlay")
                        .message("Reject this request?")
                        .danger()
                        .onConfirm(
                            fetch("/api/try-it/admin/reject/").appendVar("id").post()
                                .headerFromVar("X-Admin-Key", "adminKey")
                                .headerFromVar("X-Admin-Email", "adminEmail")
                                .header("Content-Type", "application/json")
                                .body("{}")
                                .ok(call("loadRequests"))
                                .fail(alertModal("modalOverlay", "modalBody")
                                    .error("Error")
                                    .detail("Failed to reject"))
                        )
                ))
            // Copy token function
            .add(windowFunc("copyToken")
                .does(
                    selectAndCopy("token-input"),
                    setText("modal-status", "Token copied!")
                ))
            // Send email function - using DSL for clean code
            .add(windowFunc("sendTokenEmail")
                .params("email", "token")
                .async()
                .does(
                    setTextAndColor("modal-status", "Sending email...", "#6b7280"),
                    externalService("emailjs")
                        .call("send", "'service_0cbj03m'", "'template_s4s7d3r'", "{to_email:email,token:token}")
                        .ok(setTextAndColor("modal-status", "Email sent successfully!", "#065f46"))
                        .notAvailable(setTextAndColor("modal-status", "Email service not available", "#991b1b"))
                        .fail(setTextAndColor("modal-status", "Failed to send email", "#991b1b"))
                ))
            .add(loginFormHandler())
            .add(modalCloseHandler())
            .add(tabs(".tab-btn").storesIn("currentTab").onSwitch(call("loadRequests")))
            // Clear session using resetVars
            .add(define("clearSession")
                .does(
                    resetVars()
                        .var("isLoggedIn", false)
                        .var("adminKey", "")
                        .var("adminEmail", "")
                        .array("requests")
                        .resetForm("loginForm"),
                    show("login-overlay", "flex"),
                    hide("admin-content")
                ))
            // Event handlers
            .add(onEvent("click").on("modalOverlay").then(hideOnBackdropClick("modalOverlay")))
            .add(onEvent("popstate").when("isLoggedIn").then(call("clearSession")))
            .add(onEvent("pagehide").then(call("clearSession")))
            .add(onEvent("beforeunload").then(call("clearSession")))
            .add(onEvent("visibilitychange").onDocument().when("document.hidden&&isLoggedIn").then(call("clearSession")))
            .build();
    }

    private static String requestCardTemplate() {
        return template("r")
            .div().style("background:#fff;border-radius:12px;padding:1.5rem;margin-bottom:1rem;box-shadow:0 1px 3px rgba(0,0,0,0.1)").child()
                // Header row
                .div().style("display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1rem").child()
                    .div().child()
                        .div().style("font-weight:600;font-size:1.1rem;color:#1f2937").text(escapedField("email")).end()
                        .div().style("font-size:0.875rem;color:#6b7280").text(dateField("createdAt")).end()
                    .end()
                    .badge("status", "statusBg", "statusTxt")
                .end()
                // Message section
                .div().style("background:#f9fafb;padding:1rem;border-radius:8px;margin-bottom:1rem").child()
                    .div().style("font-size:0.75rem;color:#6b7280;margin-bottom:0.25rem").text("Message:").end()
                    .div().style("color:#374151").text(escapedField("message")).end()
                .end()
                // Token section (conditional)
                .when("token")
                    .div().style("background:#f0fdf4;padding:0.75rem;border-radius:8px;margin-bottom:1rem").child()
                        .div().style("font-size:0.75rem;color:#065f46;margin-bottom:0.25rem").text("Token:").end()
                        .code().style("font-size:0.875rem;color:#065f46;word-break:break-all").text(field("token")).end()
                    .end()
                .endWhen()
                // Action buttons (conditional)
                .whenEquals("status", "PENDING")
                    .div().style("display:flex;gap:0.5rem").child()
                        .button().style("padding:0.5rem 1rem;background:#10b981;color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:500")
                            .onClickEscaped("approveReq", "id", "email").text("Approve").end()
                        .button().style("padding:0.5rem 1rem;background:#ef4444;color:#fff;border:none;border-radius:6px;cursor:pointer;font-weight:500")
                            .onClick("rejectReq", "id").text("Reject").end()
                    .end()
                .endWhen()
            .end()
            .buildAs("requestCard");
    }

    private static String renderRequestsFunc() {
        return define("renderRequests")
            .raw(renderList("requests-container")
                .from("requests")
                .using("requestCard")
                .empty("No requests found")
                .build())
            .build();
    }

    private static FormHandler loginFormHandler() {
        return onSubmit("login-form")
            .loading("Logging in...")
            .before(all(
                getInputValue("admin-email").storeTo("adminEmail"),
                getInputValue("admin-key").storeTo("adminKey"),
                hide("login-error")
            ))
            .get("/api/try-it/admin/requests")
            .headerFrom("X-Admin-Key", "adminKey")
            .headerFrom("X-Admin-Email", "adminEmail")
            .ok(all(
                assignVar("requests", "_data"),
                assignVar("isLoggedIn", "true"),
                pushState("admin", "true"),
                hide("login-overlay"),
                show("admin-content", "flex"),
                call("renderRequests")
            ))
            .fail(responseError("login-error")
                .on401("Invalid credentials")
                .otherwise("Connection error"));
    }

    private static ClickHandler modalCloseHandler() {
        return onClick("modal-close").then(hideModal("modal-overlay"));
    }
}
