package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import com.osmig.Jweb.app.docs.sections.api.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiSection {
    private ApiSection() {}

    public static Element render() {
        return section(
            docTitle("REST API"),
            para("Build JSON APIs with JWeb's annotations — the whole controller is jweb.* " +
                 "and java.*, no Spring import."),

            docSubtitle("Basic Controller"),
            codeBlock("""
import jweb.Request;
import jweb.api.*;

@REST("/api/v1/users")
public class UserApi {

    @GET
    public List<User> list() {
        return userService.findAll();
    }

    @GET("/{id}")
    public User get(@Param long id) {
        return userService.findById(id);
    }

    @GET("/search")
    public List<User> search(@Query String q,
                             @Query(value = "limit", defaultValue = "10") int limit) {
        return userService.search(q, limit);
    }

    @POST
    public User create(@Body User user) {
        return userService.save(user);
    }

    @POST("/{id}/avatar")
    public Map<String, Object> avatar(@Param long id, @Upload("file") UploadedFile file) {
        return Map.of("saved", file.getFilename());
    }

    @GET("/me")
    public Map<String, Object> me(Request req, @Cookie("session") String session) {
        return Map.of("ip", req.ip(), "session", session);
    }
}"""),

            docSubtitle("Annotations"),
            codeBlock("""
@REST("/api/v1/x")   // Controller with base path (must start with /api/v)
@GET  @POST  @UPDATE  @PATCH  @DEL     // one per method; @GET("/{id}") adds a path

// Parameters
@Param       // path placeholder {id} → int, long, String, UUID, enum...
@Query       // query or form parameter; defaultValue, required; Optional<T>, List<T>
@Body        // JSON body → record, POJO, Map, List (or the raw String)
@Header      // request header
@Cookie      // cookie value
@Upload      // multipart file → jweb.UploadedFile (never null: isEmpty())
Request      // a bare jweb.Request parameter is injected

// Wiring
@Component   // a class the container creates once and injects (jweb.api.Component)
@Value       // @Value("${app.setting:default}") on a field or constructor parameter"""),
            para("Missing or malformed input answers 400 with a message naming the parameter. " +
                 "Conversion follows the record-binding rules: numbers, booleans (true/1/yes/on), " +
                 "UUID, enums by name."),

            docSubtitle("Response Types"),
            codeBlock("""
@GET("/users")
public List<User> users() { ... }              // Auto-JSON

@POST("/users")
public Object create(@Body User user) {
    if (!valid(user)) return Response.badRequest("name is required");
    return Response.created("/api/v1/users/" + id, saved);   // 201 + Location + JSON
}

@GET("/download")
public Object file() {
    return Response.ok().contentType("application/pdf").body(bytes);
}"""),

            docTip("A guard registered under the same prefix covers the controller too: " +
                   "app.guard(\"/api/v1/admin/**\", Auth.requireRole(\"admin\")). Middleware does not."),
            docTip("Mount OpenApi.create().addApi(UserApi.class).mount(app, \"/api\") to serve interactive API docs at /api/docs."),

            ApiSse.render(),
            ApiJobs.render()
        );
    }
}
