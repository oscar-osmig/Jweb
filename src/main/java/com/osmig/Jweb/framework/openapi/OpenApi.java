package com.osmig.Jweb.framework.openapi;

import com.osmig.Jweb.framework.routing.Router;
import com.osmig.Jweb.framework.util.Json;

import java.util.*;

/**
 * OpenAPI specification generator.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Generate OpenAPI JSON
 * String spec = OpenApi.generate(app.getRouter())
 *     .title("My API")
 *     .version("1.0.0")
 *     .description("API for my application")
 *     .server("https://api.example.com")
 *     .toJson();
 *
 * // Serve the spec
 * app.get("/api/openapi.json", req -> Response.json(spec));
 *
 * // Serve Swagger UI
 * app.get("/api/docs", req -> OpenApi.swaggerUi("/api/openapi.json"));
 * }</pre>
 */
public class OpenApi {

    private final Router router;
    private String title = "API Documentation";
    private String version = "1.0.0";
    private String description = "";
    private final List<Map<String, String>> servers = new ArrayList<>();
    private final Map<String, Object> securitySchemes = new LinkedHashMap<>();
    private final List<Map<String, List<String>>> security = new ArrayList<>();
    private String contact;
    private String license;

    private OpenApi(Router router) {
        this.router = router;
    }

    public static OpenApi generate(Router router) {
        return new OpenApi(router);
    }

    public OpenApi title(String title) {
        this.title = title;
        return this;
    }

    public OpenApi version(String version) {
        this.version = version;
        return this;
    }

    public OpenApi description(String description) {
        this.description = description;
        return this;
    }

    public OpenApi server(String url) {
        return server(url, null);
    }

    public OpenApi server(String url, String description) {
        Map<String, String> server = new LinkedHashMap<>();
        server.put("url", url);
        if (description != null) {
            server.put("description", description);
        }
        servers.add(server);
        return this;
    }

    public OpenApi bearerAuth() {
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "http");
        scheme.put("scheme", "bearer");
        scheme.put("bearerFormat", "JWT");
        securitySchemes.put("bearerAuth", scheme);
        security.add(Map.of("bearerAuth", List.of()));
        return this;
    }

    public OpenApi apiKey(String name, String in) {
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "apiKey");
        scheme.put("in", in);
        scheme.put("name", name);
        securitySchemes.put("apiKey", scheme);
        security.add(Map.of("apiKey", List.of()));
        return this;
    }

    /**
     * Generates the OpenAPI specification as a Map.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> spec = new LinkedHashMap<>();

        // OpenAPI version
        spec.put("openapi", "3.0.3");

        // Info
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", title);
        info.put("version", version);
        if (!description.isEmpty()) {
            info.put("description", description);
        }
        spec.put("info", info);

        // Servers
        if (!servers.isEmpty()) {
            spec.put("servers", servers);
        }

        // Paths
        Map<String, Object> paths = new LinkedHashMap<>();
        // Note: In a real implementation, you'd iterate over router.getRoutes()
        // and extract @ApiDoc annotations. This is a simplified version.
        spec.put("paths", paths);

        // Components
        if (!securitySchemes.isEmpty()) {
            Map<String, Object> components = new LinkedHashMap<>();
            components.put("securitySchemes", securitySchemes);
            spec.put("components", components);
        }

        // Security
        if (!security.isEmpty()) {
            spec.put("security", security);
        }

        return spec;
    }

    /**
     * Generates the OpenAPI specification as JSON.
     */
    public String toJson() {
        return Json.stringify(toMap());
    }

    /**
     * Generates a Swagger UI HTML page.
     *
     * @param specUrl URL to the OpenAPI JSON spec
     * @return HTML string
     */
    public static String swaggerUi(String specUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>API Documentation</title>
                <meta charset="utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>
                    SwaggerUIBundle({
                        url: '%s',
                        dom_id: '#swagger-ui',
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIBundle.SwaggerUIStandalonePreset
                        ],
                        layout: "BaseLayout"
                    });
                </script>
            </body>
            </html>
            """.formatted(specUrl);
    }

    /**
     * Generates a Redoc HTML page.
     *
     * @param specUrl URL to the OpenAPI JSON spec
     * @return HTML string
     */
    public static String redocUi(String specUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>API Documentation</title>
                <meta charset="utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
                <style>body { margin: 0; padding: 0; }</style>
            </head>
            <body>
                <redoc spec-url='%s'></redoc>
                <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
            </body>
            </html>
            """.formatted(specUrl);
    }
}
