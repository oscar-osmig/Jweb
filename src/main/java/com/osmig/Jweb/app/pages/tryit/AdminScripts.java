package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.styles.Style;

import static com.osmig.Jweb.framework.js.Actions.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

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
                                    () -> "window._pendingToken=_data.token;window._pendingEmail=email;",
                                    alertModal("modalOverlay", "modalBody")
                                        .success("Approved!")
                                        .detailExpr("'<p style=\"color:#6b7280;margin-bottom:0.5rem\">Token for '+esc(email)+':'+'</p>'")
                                        .inputField("token-input", "_data.token")
                                        .button("Copy Token", "copyToken()", "#6366f1")
                                        .button("Send by Email", "sendPendingToken()", "#10b981")
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
            // Send email function using stored pending values
            .add(windowFunc("sendPendingToken")
                .async()
                .does(
                    setTextAndColor("modal-status", "Sending email...", "#6b7280"),
                    externalService("emailjs")
                        .call("send", "'service_0cbj03m'", "'template_s4s7d3r'", "{to_email:window._pendingEmail,token:window._pendingToken}")
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
            .div().style(new Style<>()
                .backgroundColor(white).borderRadius(px(12)).padding(rem(1.5))
                .marginBottom(rem(1)).boxShadow(px(0), px(1), px(3), rgba(0, 0, 0, 0.1))).child()
                // Header row
                .div().style(new Style<>()
                    .display(flex).justifyContent(spaceBetween).alignItems(flexStart)
                    .marginBottom(rem(1))).child()
                    .div().child()
                        .div().style(new Style<>()
                            .fontWeight(600).fontSize(rem(1.1)).color(hex("#1f2937"))).text(escapedField("email")).end()
                        .div().style(new Style<>()
                            .fontSize(rem(0.875)).color(hex("#6b7280"))).text(dateField("createdAt")).end()
                    .end()
                    .badge("status", "statusBg", "statusTxt")
                .end()
                // Message section
                .div().style(new Style<>()
                    .backgroundColor(hex("#f9fafb")).padding(rem(1))
                    .borderRadius(px(8)).marginBottom(rem(1))).child()
                    .div().style(new Style<>()
                        .fontSize(rem(0.75)).color(hex("#6b7280")).marginBottom(rem(0.25))).text("Message:").end()
                    .div().style(new Style<>().color(hex("#374151"))).text(escapedField("message")).end()
                .end()
                // Token section (conditional)
                .when("token")
                    .div().style(new Style<>()
                        .backgroundColor(hex("#f0fdf4")).padding(rem(0.75))
                        .borderRadius(px(8)).marginBottom(rem(1))).child()
                        .div().style(new Style<>()
                            .fontSize(rem(0.75)).color(hex("#065f46")).marginBottom(rem(0.25))).text("Token:").end()
                        .code().style(new Style<>()
                            .fontSize(rem(0.875)).color(hex("#065f46")).wordBreak(breakAll)).text(field("token")).end()
                    .end()
                .endWhen()
                // Action buttons (conditional)
                .whenEquals("status", "PENDING")
                    .div().style(new Style<>().display(flex).gap(rem(0.5))).child()
                        .button().style(new Style<>()
                            .padding(rem(0.5), rem(1)).backgroundColor(hex("#10b981")).color(white)
                            .border(none).borderRadius(px(6)).cursor(pointer).fontWeight(500))
                            .onClickEscaped("approveReq", "id", "email").text("Approve").end()
                        .button().style(new Style<>()
                            .padding(rem(0.5), rem(1)).backgroundColor(hex("#ef4444")).color(white)
                            .border(none).borderRadius(px(6)).cursor(pointer).fontWeight(500))
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
