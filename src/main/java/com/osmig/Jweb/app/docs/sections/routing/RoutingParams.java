package com.osmig.Jweb.app.docs.sections.routing;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingParams {
    private RoutingParams() {}

    public static Element render() {
        return section(
            h3Title("Path Parameters"),
            para("Capture dynamic segments from the URL path."),
            codeBlock("""
// Single parameter
app.get("/users/:id", req -> {
    String id = req.param("id");
    User user = userService.findById(Long.parseLong(id));
    return userProfile(user);
});

// Multiple parameters
app.get("/posts/:category/:slug", req -> {
    String category = req.param("category");
    String slug = req.param("slug");
    Post post = postService.findBySlug(category, slug);
    return postPage(post);
});

// Optional with default
app.get("/blog/:page", req -> {
    int page = req.paramInt("page", 1);
    return blogListing(page);
});"""),

            h3Title("Query Parameters"),
            para("Access query string parameters from the URL."),
            codeBlock("""
// GET /search?q=java&page=2&sort=date
app.get("/search", req -> {
    String query = req.query("q");
    int page = req.queryInt("page", 1);
    String sort = req.query("sort", "relevance");

    List<Result> results = searchService.search(query, page, sort);
    return searchResults(results, query, page);
});

// Check if parameter exists
app.get("/products", req -> {
    boolean inStock = req.hasQuery("inStock");
    String category = req.query("category", "all");
    return productListing(category, inStock);
});""")
        );
    }
}
