package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
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

    @GET("/:id")
    public User get(@Path("id") Long id) {
        return userService.findById(id);
    }

    @POST
    public User create(@Body User user) {
        return userService.save(user);
    }
}"""),

            docSubtitle("Annotations"),
            codeBlock("""
@REST("/api")    // Base path for controller
@GET             // GET request
@POST            // POST request
@PUT             // PUT request
@DEL             // DELETE request
@Path("id")      // Path parameter
@Body            // Request body (JSON)
@Query("q")      // Query parameter"""),

            docSubtitle("Response Types"),
            codeBlock("""
@GET("/users")
public List<User> users() { ... }  // Auto-JSON

@GET("/download")
public ResponseEntity<byte[]> file() { ... }"""),

            docTip("API docs auto-generated at /api/docs when openapi.enabled=true"),

            ApiEmail.render(),
            ApiSse.render(),
            ApiJobs.render()
        );
    }
}
