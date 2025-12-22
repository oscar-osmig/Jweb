package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingSection {
    private RoutingSection() {}

    public static Element render() {
        return section(
            title("Routing"),
            text("Map URL paths to handlers that return elements."),

            subtitle("Basic Routes"),
            code("""
                app.get("/", () -> h1("Home"));
                app.get("/about", () -> new AboutPage().render());
                app.post("/submit", req -> handleSubmit(req));
                app.put("/users/:id", req -> updateUser(req));
                app.delete("/users/:id", req -> deleteUser(req));"""),

            subtitle("Path Parameters"),
            code("""
                // Route: /users/:id
                app.get("/users/:id", req -> {
                    String userId = req.param("id");
                    return div(h1("User: " + userId));
                });

                // Route: /posts/:category/:slug
                app.get("/posts/:category/:slug", req -> {
                    String category = req.param("category");
                    String slug = req.param("slug");
                    return article(h1(slug));
                });"""),

            subtitle("Query Parameters"),
            code("""
                // URL: /search?q=java&page=2
                app.get("/search", req -> {
                    String query = req.query("q");
                    String page = req.query("page", "1");
                    return div(h1("Search: " + query));
                });"""),

            subtitle("Request Body"),
            code("""
                app.post("/login", req -> {
                    String email = req.body("email");
                    String password = req.body("password");
                    return authenticate(email, password);
                });""")
        );
    }
}
