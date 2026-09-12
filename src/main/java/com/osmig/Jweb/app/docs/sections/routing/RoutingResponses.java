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
class UserService { List<Object> findAll() { return List.of(); } }
UserService userService = new UserService();
boolean authenticate(Request r) { return true; }
boolean saved = true;
Element maintenancePage() { return div(); }
Object item = new Object();

// HTML response (default)
app.get("/page", () -> div("HTML content"));

// JSON response
app.get("/api/users", req -> Response.json(userService.findAll()));

// Redirect
app.get("/old-page", req -> Response.redirect("/new-page"));
app.post("/login", req -> {
    if (authenticate(req)) {
        return Response.redirect("/dashboard");
    }
    return Response.redirect("/login?error=true");
});
app.get("/act/save", req -> Response.redirect("/about").anchor(saved ? "done" : "form"));
app.get("/act/toggle", req -> Response.redirectBack(req));    // the Referer, or "/"

// Status codes — no HttpStatus import, ever
app.get("/not-found", req -> Response.notFound());
app.get("/forbidden", req -> Response.forbidden());
app.get("/error", req -> Response.serverError());
app.get("/limited", req -> Response.tooManyRequests());
app.get("/deleted", req -> Response.noContent());
app.get("/bad", req -> Response.badRequest("Invalid email"));
app.get("/teapot", req -> Response.error(418, "I'm a teapot"));
app.get("/down", req -> Response.html(503, maintenancePage()));
app.get("/made", req -> Response.json(201, item));"""),

            h3Title("Custom Responses"),
            codeBlock("""
String rss = "<rss></rss>";
String etag = "abc123";
class FileService { byte[] read(String file) { return new byte[0]; } }
FileService fileService = new FileService();

// Custom headers
app.get("/custom", req -> Response.ok()
    .header("X-Custom-Header", "value")
    .body("Hello"));

// Any status, any content type
app.get("/feed", req -> Response.status(200)
    .contentType("application/rss+xml; charset=UTF-8")
    .body(rss));
app.get("/cached", req -> Response.status(304).header("ETag", etag).build());

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
