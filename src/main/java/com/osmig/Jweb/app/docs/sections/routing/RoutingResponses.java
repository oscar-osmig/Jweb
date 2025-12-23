package com.osmig.Jweb.app.docs.sections.routing;

import com.osmig.Jweb.framework.core.Element;
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
// Custom status and headers
app.get("/custom", req -> Response.status(201)
    .header("X-Custom-Header", "value")
    .body("Created"));

// File download
app.get("/download/:file", req -> {
    byte[] data = fileService.read(req.param("file"));
    return Response.file(data, "report.pdf");
});

// Stream response
app.get("/stream", req -> Response.stream(outputStream -> {
    // Write to outputStream
}));""")
        );
    }
}
