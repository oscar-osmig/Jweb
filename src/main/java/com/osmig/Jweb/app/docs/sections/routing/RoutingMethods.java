package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingMethods {
    private RoutingMethods() {}

    public static Element render() {
        return section(
            h3Title("HTTP Methods"),
            para("Handle different HTTP methods with dedicated methods."),
            codeBlock("""
// GET - retrieve data
app.get("/users", () -> userList());
app.get("/users/:id", req -> userDetail(req.param("id")));

// POST - create new resource
app.post("/users", req -> {
    User saved = userService.save(req.formParam("name"), req.formParam("email"));
    return Response.redirect("/users/" + saved.getId());
});

// PUT - update entire resource
app.put("/users/:id", req -> {
    Long id = req.paramLong("id");
    String json = req.body();  // raw request body
    userService.update(id, json);
    return Response.noContent();
});

// DELETE - remove resource
app.delete("/users/:id", req -> {
    userService.delete(req.paramLong("id"));
    return Response.noContent();
});""")
        );
    }
}
