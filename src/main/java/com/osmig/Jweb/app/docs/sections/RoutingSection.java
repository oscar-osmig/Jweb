package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingSection {
    private RoutingSection() {}

    public static Element render() {
        return section(
            title("Routing"),
            text("Map URL paths to handlers. Handlers return Elements which are rendered to HTML."),

            subtitle("HTTP Methods"),
            code("""
app.get("/", () -> h1("Home"));
app.post("/submit", req -> handleForm(req));
app.put("/users/:id", req -> updateUser(req));
app.delete("/users/:id", req -> deleteUser(req));"""),

            subtitle("Path Parameters"),
            code("""
// Single parameter
app.get("/users/:id", req -> {
    String id = req.param("id");
    return h1("User: " + id);
});

// Multiple parameters
app.get("/posts/:category/:slug", req -> {
    String category = req.param("category");
    String slug = req.param("slug");
    return article(h1(slug), span(category));
});"""),

            subtitle("Query Parameters"),
            code("""
// URL: /search?q=java&page=2
app.get("/search", req -> {
    String query = req.query("q");
    int page = Integer.parseInt(req.query("page", "1"));
    return searchResults(query, page);
});"""),

            subtitle("Request Body (Forms)"),
            code("""
app.post("/login", req -> {
    String email = req.body("email");
    String password = req.body("password");
    // Or get all: Map<String, String> data = req.formData();
    return authenticate(email, password);
});"""),

            subtitle("Responses"),
            code("""
// Return Element (renders as HTML)
app.get("/page", () -> div("Hello"));

// Return Template
app.get("/home", () -> new HomePage());

// Redirect
app.get("/old", req -> Response.redirect("/new"));

// JSON (for APIs)
app.get("/api/status", () -> Response.json(Map.of("ok", true)));""")
        );
    }
}
