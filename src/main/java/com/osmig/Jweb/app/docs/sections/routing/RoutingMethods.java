package com.osmig.Jweb.app.docs.sections.routing;

import com.osmig.Jweb.framework.core.Element;
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
    User user = req.bodyAs(User.class);
    User saved = userService.save(user);
    return Response.redirect("/users/" + saved.getId());
});

// PUT - update entire resource
app.put("/users/:id", req -> {
    Long id = req.paramLong("id");
    User user = req.bodyAs(User.class);
    userService.update(id, user);
    return Response.ok();
});

// PATCH - partial update
app.patch("/users/:id", req -> {
    Long id = req.paramLong("id");
    Map<String, Object> updates = req.bodyAsMap();
    userService.partialUpdate(id, updates);
    return Response.ok();
});

// DELETE - remove resource
app.delete("/users/:id", req -> {
    userService.delete(req.paramLong("id"));
    return Response.ok();
});""")
        );
    }
}
