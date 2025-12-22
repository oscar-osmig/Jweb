package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

public final class ApiSection {
    private ApiSection() {}

    public static Element render() {
        return section(
            title("REST API"),
            text("JWeb provides simplified REST API annotations that work alongside " +
                 "Spring's built-in REST support. Use @REST to mark a controller, " +
                 "and @GET, @POST, @PUT, @DEL, @PATCH for endpoints."),

            subtitle("Basic API Controller"),
            code(API_CONTROLLER),

            subtitle("Simplified Annotations"),
            text("JWeb's REST annotations reduce boilerplate compared to standard Spring:"),
            code(API_ANNOTATIONS),

            subtitle("Returning JSON"),
            text("Return any object and it will be serialized to JSON automatically:"),
            code(API_JSON_RESPONSE),

            subtitle("Request Body & Path Variables"),
            code(API_REQUEST_BODY),

            subtitle("OpenAPI Documentation"),
            text("JWeb auto-generates OpenAPI/Swagger documentation. Access it at /api-docs " +
                 "when enabled in application.yaml:"),
            code(API_OPENAPI_CONFIG)
        );
    }
}
