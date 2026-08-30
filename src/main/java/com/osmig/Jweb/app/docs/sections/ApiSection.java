package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import com.osmig.Jweb.app.docs.sections.api.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiSection {
    private ApiSection() {}

    public static Element render() {
        return section(
            docTitle("REST API"),
            para("Build JSON APIs with simplified annotations."),

            docSubtitle("Basic Controller"),
            codeBlock("""
@REST("/api/users")
public class UserApi {

    @GET
    public List<User> list() {
        return userService.findAll();
    }

    @GET("/{id}")
    public User get(@PathVariable Long id) {
        return userService.findById(id);
    }

    @POST
    public User create(@RequestBody User user) {
        return userService.save(user);
    }
}"""),

            docSubtitle("Annotations"),
            codeBlock("""
@REST("/api")    // Base path for controller
@GET             // GET request
@POST            // POST request
@UPDATE          // PUT request
@PATCH           // PATCH request
@DEL             // DELETE request

// Parameters use the Spring annotations:
@PathVariable    // Path parameter ({id})
@RequestBody     // Request body (JSON)
@RequestParam    // Query parameter"""),

            docSubtitle("Response Types"),
            codeBlock("""
@GET("/users")
public List<User> users() { ... }  // Auto-JSON

@GET("/download")
public ResponseEntity<byte[]> file() { ... }"""),

            docTip("Mount OpenApi.create().addApi(UserApi.class).mount(app) to serve interactive API docs at /docs."),

            ApiSse.render(),
            ApiJobs.render()
        );
    }
}
