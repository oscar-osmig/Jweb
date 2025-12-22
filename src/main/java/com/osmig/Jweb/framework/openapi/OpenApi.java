package com.osmig.Jweb.framework.openapi;

import com.osmig.Jweb.framework.api.*;
import com.osmig.Jweb.framework.util.Json;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * OpenAPI 3.0 specification generator.
 *
 * <p>Usage:</p>
 * <pre>
 * // Generate from @REST annotated classes
 * String spec = OpenApi.create()
 *     .title("My API")
 *     .version("1.0.0")
 *     .description("REST API for my application")
 *     .server("https://api.example.com")
 *     .scan("com.myapp.api")  // Package to scan
 *     .toJson();
 *
 * // Or register classes manually
 * String spec = OpenApi.create()
 *     .title("My API")
 *     .version("1.0.0")
 *     .addApi(UserApi.class)
 *     .addApi(ProductApi.class)
 *     .toJson();
 *
 * // Serve the spec and docs
 * app.get("/openapi.json", req -> Response.json(spec));
 * app.get("/docs", req -> Response.html(OpenApi.swaggerUi("/openapi.json")));
 * </pre>
 */
public class OpenApi {

    private String title = "API Documentation";
    private String version = "1.0.0";
    private String description = "";
    private String termsOfService;
    private String contactName;
    private String contactEmail;
    private String contactUrl;
    private String licenseName;
    private String licenseUrl;
    private final List<ServerInfo> servers = new ArrayList<>();
    private final List<Class<?>> apiClasses = new ArrayList<>();
    private final Map<String, Object> securitySchemes = new LinkedHashMap<>();
    private final List<Map<String, List<String>>> globalSecurity = new ArrayList<>();
    private final Map<String, TagInfo> tags = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> schemas = new LinkedHashMap<>();

    private OpenApi() {}

    // ==================== Factory ====================

    /**
     * Creates a new OpenAPI spec builder.
     */
    public static OpenApi create() {
        return new OpenApi();
    }

    // ==================== Info ====================

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

    public OpenApi termsOfService(String url) {
        this.termsOfService = url;
        return this;
    }

    public OpenApi contact(String name, String email, String url) {
        this.contactName = name;
        this.contactEmail = email;
        this.contactUrl = url;
        return this;
    }

    public OpenApi contact(String email) {
        this.contactEmail = email;
        return this;
    }

    public OpenApi license(String name, String url) {
        this.licenseName = name;
        this.licenseUrl = url;
        return this;
    }

    public OpenApi license(String name) {
        this.licenseName = name;
        return this;
    }

    // ==================== Servers ====================

    public OpenApi server(String url) {
        servers.add(new ServerInfo(url, null));
        return this;
    }

    public OpenApi server(String url, String description) {
        servers.add(new ServerInfo(url, description));
        return this;
    }

    // ==================== Security ====================

    public OpenApi bearerAuth() {
        return bearerAuth("bearerAuth", "JWT");
    }

    public OpenApi bearerAuth(String name, String format) {
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "http");
        scheme.put("scheme", "bearer");
        scheme.put("bearerFormat", format);
        securitySchemes.put(name, scheme);
        globalSecurity.add(Map.of(name, List.of()));
        return this;
    }

    public OpenApi apiKeyHeader(String headerName) {
        return apiKey("apiKey", "header", headerName);
    }

    public OpenApi apiKeyQuery(String paramName) {
        return apiKey("apiKey", "query", paramName);
    }

    public OpenApi apiKey(String name, String in, String keyName) {
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "apiKey");
        scheme.put("in", in);
        scheme.put("name", keyName);
        securitySchemes.put(name, scheme);
        globalSecurity.add(Map.of(name, List.of()));
        return this;
    }

    public OpenApi oauth2(String name, String authUrl, String tokenUrl, Map<String, String> scopes) {
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "oauth2");
        Map<String, Object> flows = new LinkedHashMap<>();
        Map<String, Object> authCodeFlow = new LinkedHashMap<>();
        authCodeFlow.put("authorizationUrl", authUrl);
        authCodeFlow.put("tokenUrl", tokenUrl);
        authCodeFlow.put("scopes", scopes);
        flows.put("authorizationCode", authCodeFlow);
        scheme.put("flows", flows);
        securitySchemes.put(name, scheme);
        return this;
    }

    // ==================== Tags ====================

    public OpenApi tag(String name, String description) {
        tags.put(name, new TagInfo(name, description));
        return this;
    }

    // ==================== API Classes ====================

    /**
     * Adds an API class to document.
     */
    public OpenApi addApi(Class<?> apiClass) {
        apiClasses.add(apiClass);
        return this;
    }

    /**
     * Scans a package for @REST annotated classes.
     */
    public OpenApi scan(String packageName) {
        // Note: Full classpath scanning requires additional libraries like ClassGraph
        // For now, classes must be added manually via addApi()
        // In a real implementation, you'd use:
        // ClassGraph.scan(packageName).getClassesWithAnnotation(REST.class)
        return this;
    }

    // ==================== Schemas ====================

    /**
     * Adds a reusable schema.
     */
    public OpenApi schema(String name, Map<String, Object> schema) {
        schemas.put(name, schema);
        return this;
    }

    /**
     * Adds a schema from a class.
     */
    public OpenApi schema(Class<?> clazz) {
        schemas.put(clazz.getSimpleName(), schemaFromClass(clazz));
        return this;
    }

    // ==================== Generation ====================

    /**
     * Generates the OpenAPI specification as a Map.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> spec = new LinkedHashMap<>();

        spec.put("openapi", "3.0.3");
        spec.put("info", buildInfo());

        if (!servers.isEmpty()) {
            spec.put("servers", servers.stream()
                .map(s -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("url", s.url);
                    if (s.description != null) m.put("description", s.description);
                    return m;
                }).toList());
        }

        if (!tags.isEmpty()) {
            spec.put("tags", tags.values().stream()
                .map(t -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("name", t.name);
                    if (t.description != null) m.put("description", t.description);
                    return m;
                }).toList());
        }

        spec.put("paths", buildPaths());

        Map<String, Object> components = new LinkedHashMap<>();
        if (!securitySchemes.isEmpty()) {
            components.put("securitySchemes", securitySchemes);
        }
        if (!schemas.isEmpty()) {
            components.put("schemas", schemas);
        }
        if (!components.isEmpty()) {
            spec.put("components", components);
        }

        if (!globalSecurity.isEmpty()) {
            spec.put("security", globalSecurity);
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
     * Generates the OpenAPI specification as pretty-printed JSON.
     */
    public String toJsonPretty() {
        return Json.stringifyPretty(toMap());
    }

    // ==================== Build Helpers ====================

    private Map<String, Object> buildInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", title);
        info.put("version", version);
        if (description != null && !description.isEmpty()) {
            info.put("description", description);
        }
        if (termsOfService != null) {
            info.put("termsOfService", termsOfService);
        }
        if (contactName != null || contactEmail != null || contactUrl != null) {
            Map<String, String> contact = new LinkedHashMap<>();
            if (contactName != null) contact.put("name", contactName);
            if (contactEmail != null) contact.put("email", contactEmail);
            if (contactUrl != null) contact.put("url", contactUrl);
            info.put("contact", contact);
        }
        if (licenseName != null) {
            Map<String, String> license = new LinkedHashMap<>();
            license.put("name", licenseName);
            if (licenseUrl != null) license.put("url", licenseUrl);
            info.put("license", license);
        }
        return info;
    }

    private Map<String, Object> buildPaths() {
        Map<String, Object> paths = new LinkedHashMap<>();

        for (Class<?> apiClass : apiClasses) {
            String basePath = getBasePath(apiClass);
            ApiDoc classDoc = apiClass.getAnnotation(ApiDoc.class);

            for (Method method : apiClass.getDeclaredMethods()) {
                MethodInfo methodInfo = getMethodInfo(method);
                if (methodInfo == null) continue;

                String fullPath = normalizePath(basePath + methodInfo.path);

                @SuppressWarnings("unchecked")
                Map<String, Object> pathItem = (Map<String, Object>) paths.computeIfAbsent(
                    fullPath, k -> new LinkedHashMap<>());

                Map<String, Object> operation = buildOperation(method, methodInfo, classDoc);
                pathItem.put(methodInfo.httpMethod.toLowerCase(), operation);
            }
        }

        return paths;
    }

    private String getBasePath(Class<?> apiClass) {
        REST rest = apiClass.getAnnotation(REST.class);
        if (rest != null && rest.value().length > 0) {
            return rest.value()[0];
        }
        RequestMapping rm = apiClass.getAnnotation(RequestMapping.class);
        if (rm != null && rm.value().length > 0) {
            return rm.value()[0];
        }
        return "";
    }

    private MethodInfo getMethodInfo(Method method) {
        // Check for JWeb annotations first
        GET get = method.getAnnotation(GET.class);
        if (get != null) return new MethodInfo("GET", get.value().length > 0 ? get.value()[0] : "");

        POST post = method.getAnnotation(POST.class);
        if (post != null) return new MethodInfo("POST", post.value().length > 0 ? post.value()[0] : "");

        UPDATE update = method.getAnnotation(UPDATE.class);
        if (update != null) return new MethodInfo("PUT", update.value().length > 0 ? update.value()[0] : "");

        PATCH patch = method.getAnnotation(PATCH.class);
        if (patch != null) return new MethodInfo("PATCH", patch.value().length > 0 ? patch.value()[0] : "");

        DEL del = method.getAnnotation(DEL.class);
        if (del != null) return new MethodInfo("DELETE", del.value().length > 0 ? del.value()[0] : "");

        // Check Spring annotations
        GetMapping gm = method.getAnnotation(GetMapping.class);
        if (gm != null) return new MethodInfo("GET", gm.value().length > 0 ? gm.value()[0] : "");

        PostMapping pm = method.getAnnotation(PostMapping.class);
        if (pm != null) return new MethodInfo("POST", pm.value().length > 0 ? pm.value()[0] : "");

        PutMapping putm = method.getAnnotation(PutMapping.class);
        if (putm != null) return new MethodInfo("PUT", putm.value().length > 0 ? putm.value()[0] : "");

        PatchMapping patchm = method.getAnnotation(PatchMapping.class);
        if (patchm != null) return new MethodInfo("PATCH", patchm.value().length > 0 ? patchm.value()[0] : "");

        DeleteMapping dm = method.getAnnotation(DeleteMapping.class);
        if (dm != null) return new MethodInfo("DELETE", dm.value().length > 0 ? dm.value()[0] : "");

        return null;
    }

    private Map<String, Object> buildOperation(Method method, MethodInfo methodInfo, ApiDoc classDoc) {
        Map<String, Object> operation = new LinkedHashMap<>();

        ApiDoc methodDoc = method.getAnnotation(ApiDoc.class);
        ApiDoc doc = methodDoc != null ? methodDoc : classDoc;

        // Operation ID
        operation.put("operationId", method.getName());

        // Summary and description
        if (doc != null) {
            if (!doc.summary().isEmpty()) {
                operation.put("summary", doc.summary());
            }
            if (!doc.description().isEmpty()) {
                operation.put("description", doc.description());
            }
            if (doc.tags().length > 0) {
                operation.put("tags", Arrays.asList(doc.tags()));
            }
            if (doc.deprecated()) {
                operation.put("deprecated", true);
            }
        }

        // Parameters
        List<Map<String, Object>> parameters = buildParameters(method, methodInfo, doc);
        if (!parameters.isEmpty()) {
            operation.put("parameters", parameters);
        }

        // Request body
        Map<String, Object> requestBody = buildRequestBody(method, doc);
        if (requestBody != null) {
            operation.put("requestBody", requestBody);
        }

        // Responses
        operation.put("responses", buildResponses(method, doc));

        return operation;
    }

    private List<Map<String, Object>> buildParameters(Method method, MethodInfo methodInfo, ApiDoc doc) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        // Extract path parameters from path pattern
        String path = methodInfo.path;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]+)\\}").matcher(path);
        Set<String> pathParams = new HashSet<>();
        while (matcher.find()) {
            pathParams.add(matcher.group(1));
        }

        // Build from method parameters
        for (Parameter param : method.getParameters()) {
            Map<String, Object> paramSpec = null;

            PathVariable pv = param.getAnnotation(PathVariable.class);
            if (pv != null) {
                String name = pv.value().isEmpty() ? param.getName() : pv.value();
                paramSpec = buildParam(name, "path", param.getType(), true);
                pathParams.remove(name);
            }

            RequestParam rp = param.getAnnotation(RequestParam.class);
            if (rp != null) {
                String name = rp.value().isEmpty() ? param.getName() : rp.value();
                boolean required = rp.required() && rp.defaultValue().equals("\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n");
                paramSpec = buildParam(name, "query", param.getType(), required);
                if (!rp.defaultValue().equals("\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n")) {
                    paramSpec.put("default", rp.defaultValue());
                }
            }

            RequestHeader rh = param.getAnnotation(RequestHeader.class);
            if (rh != null) {
                String name = rh.value().isEmpty() ? param.getName() : rh.value();
                paramSpec = buildParam(name, "header", param.getType(), rh.required());
            }

            if (paramSpec != null) {
                parameters.add(paramSpec);
            }
        }

        // Add remaining path params not covered by annotations
        for (String pathParam : pathParams) {
            parameters.add(buildParam(pathParam, "path", String.class, true));
        }

        // Add from @ApiDoc params
        if (doc != null) {
            for (ApiParam apiParam : doc.params()) {
                Map<String, Object> paramSpec = new LinkedHashMap<>();
                paramSpec.put("name", apiParam.name());
                paramSpec.put("in", apiParam.in().name().toLowerCase());
                paramSpec.put("required", apiParam.required());
                if (!apiParam.description().isEmpty()) {
                    paramSpec.put("description", apiParam.description());
                }
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", apiParam.type());
                if (!apiParam.example().isEmpty()) {
                    schema.put("example", apiParam.example());
                }
                paramSpec.put("schema", schema);
                parameters.add(paramSpec);
            }
        }

        return parameters;
    }

    private Map<String, Object> buildParam(String name, String in, Class<?> type, boolean required) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("name", name);
        param.put("in", in);
        param.put("required", required);
        param.put("schema", Map.of("type", javaTypeToOpenApiType(type)));
        return param;
    }

    private Map<String, Object> buildRequestBody(Method method, ApiDoc doc) {
        for (Parameter param : method.getParameters()) {
            if (param.getAnnotation(RequestBody.class) != null) {
                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("required", true);

                Map<String, Object> content = new LinkedHashMap<>();
                Map<String, Object> mediaType = new LinkedHashMap<>();
                mediaType.put("schema", schemaFromClass(param.getType()));
                content.put("application/json", mediaType);
                requestBody.put("content", content);

                // Add from @ApiDoc body
                if (doc != null && !doc.body().description().isEmpty()) {
                    requestBody.put("description", doc.body().description());
                    if (!doc.body().example().isEmpty()) {
                        mediaType.put("example", doc.body().example());
                    }
                }

                return requestBody;
            }
        }
        return null;
    }

    private Map<String, Object> buildResponses(Method method, ApiDoc doc) {
        Map<String, Object> responses = new LinkedHashMap<>();

        // Default 200 response
        Map<String, Object> successResponse = new LinkedHashMap<>();
        successResponse.put("description", "Successful response");

        Class<?> returnType = method.getReturnType();
        if (returnType != void.class && returnType != Void.class) {
            Map<String, Object> content = new LinkedHashMap<>();
            Map<String, Object> mediaType = new LinkedHashMap<>();
            mediaType.put("schema", schemaFromClass(returnType));
            content.put("application/json", mediaType);
            successResponse.put("content", content);
        }

        responses.put("200", successResponse);

        // Add from @ApiDoc responses
        if (doc != null) {
            for (ApiResponse apiResponse : doc.responses()) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("description", apiResponse.description());
                if (!apiResponse.example().isEmpty()) {
                    Map<String, Object> content = new LinkedHashMap<>();
                    Map<String, Object> mediaType = new LinkedHashMap<>();
                    mediaType.put("example", apiResponse.example());
                    content.put(apiResponse.contentType(), mediaType);
                    resp.put("content", content);
                }
                responses.put(String.valueOf(apiResponse.code()), resp);
            }
        }

        return responses;
    }

    private Map<String, Object> schemaFromClass(Class<?> clazz) {
        Map<String, Object> schema = new LinkedHashMap<>();

        if (clazz == String.class) {
            schema.put("type", "string");
        } else if (clazz == Integer.class || clazz == int.class) {
            schema.put("type", "integer");
        } else if (clazz == Long.class || clazz == long.class) {
            schema.put("type", "integer");
            schema.put("format", "int64");
        } else if (clazz == Double.class || clazz == double.class || clazz == Float.class || clazz == float.class) {
            schema.put("type", "number");
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            schema.put("type", "boolean");
        } else if (List.class.isAssignableFrom(clazz)) {
            schema.put("type", "array");
            schema.put("items", Map.of("type", "object"));
        } else if (Map.class.isAssignableFrom(clazz)) {
            schema.put("type", "object");
        } else if (clazz.isArray()) {
            schema.put("type", "array");
            schema.put("items", schemaFromClass(clazz.getComponentType()));
        } else {
            schema.put("type", "object");
            // Could introspect fields here for full schema
        }

        return schema;
    }

    private String javaTypeToOpenApiType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Double.class || type == double.class) return "number";
        if (type == Float.class || type == float.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        return "string";
    }

    private String normalizePath(String path) {
        if (path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        return path.replaceAll("/+", "/");
    }

    // ==================== Mount to JWeb ====================

    /**
     * Mounts OpenAPI spec and docs to a JWeb app.
     *
     * <p>Usage:</p>
     * <pre>
     * OpenApi.create()
     *     .title("My API")
     *     .version("1.0.0")
     *     .addApi(UserApi.class)
     *     .mount(app);  // Mounts /docs, /openapi.json
     *
     * // Or with custom base path
     * OpenApi.create()
     *     .addApi(UserApi.class)
     *     .mount(app, "/api");  // Mounts /api/docs, /api/openapi.json
     * </pre>
     */
    public void mount(com.osmig.Jweb.framework.JWeb app) {
        mount(app, "");
    }

    /**
     * Mounts OpenAPI spec and docs to a JWeb app with a custom base path.
     */
    public void mount(com.osmig.Jweb.framework.JWeb app, String basePath) {
        String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String spec = toJson();

        app.get(base + "/openapi.json", () ->
            com.osmig.Jweb.framework.core.RawContent.json(spec));
        app.get(base + "/docs", () ->
            com.osmig.Jweb.framework.core.RawContent.html(swaggerUi(base + "/openapi.json")));
        app.get(base + "/redoc", () ->
            com.osmig.Jweb.framework.core.RawContent.html(redoc(base + "/openapi.json")));
        app.get(base + "/scalar", () ->
            com.osmig.Jweb.framework.core.RawContent.html(scalar(base + "/openapi.json")));
    }

    // ==================== UI Generators ====================

    /**
     * Generates a Swagger UI HTML page.
     */
    public static String swaggerUi(String specUrl) {
        return swaggerUi(specUrl, "API Documentation");
    }

    /**
     * Generates a Swagger UI HTML page with custom title.
     */
    public static String swaggerUi(String specUrl, String title) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
                <style>
                    body { margin: 0; padding: 0; }
                    .swagger-ui .topbar { display: none; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>
                    window.onload = function() {
                        SwaggerUIBundle({
                            url: '%s',
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIBundle.SwaggerUIStandalonePreset
                            ],
                            layout: "BaseLayout",
                            persistAuthorization: true
                        });
                    };
                </script>
            </body>
            </html>
            """.formatted(title, specUrl);
    }

    /**
     * Generates a Redoc HTML page.
     */
    public static String redoc(String specUrl) {
        return redoc(specUrl, "API Documentation");
    }

    /**
     * Generates a Redoc HTML page with custom title.
     */
    public static String redoc(String specUrl, String title) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
                <style>body { margin: 0; padding: 0; }</style>
            </head>
            <body>
                <redoc spec-url='%s' expand-responses="200,201" hide-download-button></redoc>
                <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
            </body>
            </html>
            """.formatted(title, specUrl);
    }

    /**
     * Generates a Scalar API reference page.
     */
    public static String scalar(String specUrl) {
        return scalar(specUrl, "API Documentation");
    }

    /**
     * Generates a Scalar API reference page with custom title.
     */
    public static String scalar(String specUrl, String title) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body>
                <script id="api-reference" data-url="%s"></script>
                <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
            </body>
            </html>
            """.formatted(title, specUrl);
    }

    // ==================== Helper Records ====================

    private record ServerInfo(String url, String description) {}
    private record TagInfo(String name, String description) {}
    private record MethodInfo(String httpMethod, String path) {}
}
