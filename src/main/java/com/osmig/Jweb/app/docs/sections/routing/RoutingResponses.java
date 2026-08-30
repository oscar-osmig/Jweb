package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingResponses {
    private RoutingResponses() {}

    public static Element render() {
        return section(
            h3Title("Response Types"),
            para("Return different response types from handlers."),
            codeBlock("""
// HTML response (default)
app.get("/page", () -> div("HTML content"));

// JSON response
app.get("/api/users", () -> Response.json(userService.findAll()));

// Redirect
app.get("/old-page", req -> Response.redirect("/new-page"));
app.post("/login", req -> {
    if (authenticate(req)) {
        return Response.redirect("/dashboard");
    }
    return Response.redirect("/login?error=true");
});

// Status codes
app.get("/not-found", req -> Response.notFound());
app.get("/forbidden", req -> Response.forbidden());
app.get("/error", req -> Response.serverError());"""),

            h3Title("Custom Responses"),
            codeBlock("""
// Custom headers
app.get("/custom", req -> Response.ok()
    .header("X-Custom-Header", "value")
    .body("Hello"));

// 201 Created with Location header
app.post("/items", req -> Response.created("/items/42"));

// File download
app.get("/download/:file", req -> {
    byte[] data = fileService.read(req.param("file"));
    return Response.ok()
        .header("Content-Disposition", "attachment; filename=report.pdf")
        .body(data);
});""")
        );
    }
}
