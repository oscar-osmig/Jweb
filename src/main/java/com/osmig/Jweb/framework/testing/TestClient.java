package com.osmig.Jweb.framework.testing;

import com.osmig.Jweb.framework.util.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP test client for integration testing JWeb applications.
 *
 * <p>Usage:</p>
 * <pre>
 * // Create client for your running app
 * TestClient client = TestClient.create("http://localhost:8080");
 *
 * // GET request
 * TestResponse response = client.get("/api/users");
 * response.assertOk();
 * response.assertJsonPath("$.length()", 10);
 *
 * // POST with JSON
 * TestResponse response = client.post("/api/users")
 *     .json(Map.of("name", "John", "email", "john@example.com"))
 *     .send();
 * response.assertStatus(201);
 * response.assertJsonContains("id");
 *
 * // With authentication
 * TestClient authedClient = client.withHeader("Authorization", "Bearer " + token);
 * authedClient.get("/api/profile").assertOk();
 *
 * // Chained assertions
 * client.get("/api/users/1")
 *     .assertOk()
 *     .assertContentType("application/json")
 *     .assertJsonPath("$.name", "John")
 *     .assertJsonPath("$.email", "john@example.com");
 * </pre>
 */
public class TestClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Map<String, String> defaultHeaders;
    private Duration timeout = Duration.ofSeconds(30);

    private TestClient(String baseUrl, Map<String, String> headers) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.defaultHeaders = new HashMap<>(headers);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    // ==================== Factory ====================

    /**
     * Creates a test client for the specified base URL.
     */
    public static TestClient create(String baseUrl) {
        return new TestClient(baseUrl, Map.of());
    }

    /**
     * Creates a test client for localhost with default port.
     */
    public static TestClient localhost() {
        return create("http://localhost:8080");
    }

    /**
     * Creates a test client for localhost with specified port.
     */
    public static TestClient localhost(int port) {
        return create("http://localhost:" + port);
    }

    // ==================== Configuration ====================

    /**
     * Returns a new client with an additional default header.
     */
    public TestClient withHeader(String name, String value) {
        Map<String, String> newHeaders = new HashMap<>(defaultHeaders);
        newHeaders.put(name, value);
        return new TestClient(baseUrl, newHeaders);
    }

    /**
     * Returns a new client with Bearer token authentication.
     */
    public TestClient withAuth(String token) {
        return withHeader("Authorization", "Bearer " + token);
    }

    /**
     * Returns a new client with Basic authentication.
     */
    public TestClient withBasicAuth(String username, String password) {
        String credentials = java.util.Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes());
        return withHeader("Authorization", "Basic " + credentials);
    }

    /**
     * Sets the request timeout.
     */
    public TestClient timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    // ==================== Request Methods ====================

    /**
     * Creates a GET request.
     */
    public TestResponse get(String path) {
        return request("GET", path).send();
    }

    /**
     * Creates a POST request builder.
     */
    public RequestBuilder post(String path) {
        return request("POST", path);
    }

    /**
     * Creates a PUT request builder.
     */
    public RequestBuilder put(String path) {
        return request("PUT", path);
    }

    /**
     * Creates a PATCH request builder.
     */
    public RequestBuilder patch(String path) {
        return request("PATCH", path);
    }

    /**
     * Creates a DELETE request.
     */
    public TestResponse delete(String path) {
        return request("DELETE", path).send();
    }

    /**
     * Creates a request builder with the specified method.
     */
    public RequestBuilder request(String method, String path) {
        return new RequestBuilder(this, method, path);
    }

    // ==================== Request Builder ====================

    public static class RequestBuilder {
        private final TestClient client;
        private final String method;
        private final String path;
        private final Map<String, String> headers = new HashMap<>();
        private String body = "";
        private String contentType = null;

        RequestBuilder(TestClient client, String method, String path) {
            this.client = client;
            this.method = method;
            this.path = path;
        }

        /**
         * Sets a request header.
         */
        public RequestBuilder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Sets the request body as JSON.
         */
        public RequestBuilder json(Object body) {
            this.body = Json.stringify(body);
            this.contentType = "application/json";
            return this;
        }

        /**
         * Sets the request body as form data.
         */
        public RequestBuilder form(Map<String, String> data) {
            StringBuilder sb = new StringBuilder();
            data.forEach((k, v) -> {
                if (sb.length() > 0) sb.append("&");
                sb.append(k).append("=").append(v);
            });
            this.body = sb.toString();
            this.contentType = "application/x-www-form-urlencoded";
            return this;
        }

        /**
         * Sets raw body with content type.
         */
        public RequestBuilder body(String body, String contentType) {
            this.body = body;
            this.contentType = contentType;
            return this;
        }

        /**
         * Sends the request and returns the response.
         */
        public TestResponse send() {
            try {
                String url = client.baseUrl + (path.startsWith("/") ? path : "/" + path);

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(client.timeout);

                // Add default headers
                client.defaultHeaders.forEach(builder::header);

                // Add request-specific headers
                headers.forEach(builder::header);

                // Add content type if set
                if (contentType != null) {
                    builder.header("Content-Type", contentType);
                }

                // Set method and body
                if (body != null && !body.isEmpty()) {
                    builder.method(method, HttpRequest.BodyPublishers.ofString(body));
                } else if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                } else {
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                }

                HttpResponse<String> response = client.httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
                );

                return new TestResponse(response);

            } catch (Exception e) {
                throw new RuntimeException("Request failed: " + e.getMessage(), e);
            }
        }
    }

    // ==================== Test Response ====================

    public static class TestResponse {
        private final HttpResponse<String> response;

        TestResponse(HttpResponse<String> response) {
            this.response = response;
        }

        // ========== Getters ==========

        public int status() {
            return response.statusCode();
        }

        public String body() {
            return response.body();
        }

        public String header(String name) {
            return response.headers().firstValue(name).orElse(null);
        }

        public Map<String, String> headers() {
            Map<String, String> map = new HashMap<>();
            response.headers().map().forEach((k, v) -> {
                if (!v.isEmpty()) map.put(k, v.get(0));
            });
            return map;
        }

        public <T> T bodyAs(Class<T> type) {
            return Json.parse(body(), type);
        }

        public Map<String, Object> bodyAsMap() {
            return Json.parseMap(body());
        }

        // ========== Status Assertions ==========

        public TestResponse assertStatus(int expected) {
            if (status() != expected) {
                throw new AssertionError("Expected status " + expected + " but got " + status() + "\nBody: " + truncate(body()));
            }
            return this;
        }

        public TestResponse assertOk() {
            return assertStatus(200);
        }

        public TestResponse assertCreated() {
            return assertStatus(201);
        }

        public TestResponse assertNoContent() {
            return assertStatus(204);
        }

        public TestResponse assertBadRequest() {
            return assertStatus(400);
        }

        public TestResponse assertUnauthorized() {
            return assertStatus(401);
        }

        public TestResponse assertForbidden() {
            return assertStatus(403);
        }

        public TestResponse assertNotFound() {
            return assertStatus(404);
        }

        public TestResponse assertServerError() {
            if (status() < 500 || status() >= 600) {
                throw new AssertionError("Expected 5xx status but got " + status());
            }
            return this;
        }

        public TestResponse assertSuccess() {
            if (status() < 200 || status() >= 300) {
                throw new AssertionError("Expected 2xx status but got " + status() + "\nBody: " + truncate(body()));
            }
            return this;
        }

        // ========== Header Assertions ==========

        public TestResponse assertHeader(String name, String expected) {
            String actual = header(name);
            if (!expected.equals(actual)) {
                throw new AssertionError("Expected header " + name + "=\"" + expected + "\" but got \"" + actual + "\"");
            }
            return this;
        }

        public TestResponse assertHeaderContains(String name, String substring) {
            String actual = header(name);
            if (actual == null || !actual.contains(substring)) {
                throw new AssertionError("Expected header " + name + " to contain \"" + substring + "\" but got \"" + actual + "\"");
            }
            return this;
        }

        public TestResponse assertContentType(String expected) {
            String contentType = header("content-type");
            if (contentType == null || !contentType.contains(expected)) {
                throw new AssertionError("Expected Content-Type \"" + expected + "\" but got \"" + contentType + "\"");
            }
            return this;
        }

        public TestResponse assertJson() {
            return assertContentType("application/json");
        }

        public TestResponse assertHtml() {
            return assertContentType("text/html");
        }

        // ========== Body Assertions ==========

        public TestResponse assertBodyContains(String expected) {
            if (!body().contains(expected)) {
                throw new AssertionError("Expected body to contain \"" + expected + "\"\nActual: " + truncate(body()));
            }
            return this;
        }

        public TestResponse assertBodyEquals(String expected) {
            if (!body().equals(expected)) {
                throw new AssertionError("Expected body:\n" + expected + "\nActual:\n" + truncate(body()));
            }
            return this;
        }

        public TestResponse assertBodyMatches(String regex) {
            if (!body().matches(regex)) {
                throw new AssertionError("Expected body to match regex: " + regex + "\nActual: " + truncate(body()));
            }
            return this;
        }

        public TestResponse assertBodyEmpty() {
            if (body() != null && !body().isEmpty()) {
                throw new AssertionError("Expected empty body but got: " + truncate(body()));
            }
            return this;
        }

        // ========== JSON Assertions ==========

        public TestResponse assertJsonContains(String key) {
            Map<String, Object> json = bodyAsMap();
            if (!json.containsKey(key)) {
                throw new AssertionError("Expected JSON to contain key \"" + key + "\"\nActual: " + json.keySet());
            }
            return this;
        }

        public TestResponse assertJsonEquals(String key, Object expected) {
            Map<String, Object> json = bodyAsMap();
            Object actual = json.get(key);
            if (!expected.equals(actual)) {
                throw new AssertionError("Expected JSON." + key + " = " + expected + " but got " + actual);
            }
            return this;
        }

        public TestResponse assertJsonPath(String path, Object expected) {
            // Simple dot-notation path support
            Map<String, Object> json = bodyAsMap();
            Object value = getJsonPath(json, path);
            if (!expected.equals(value)) {
                throw new AssertionError("Expected $." + path + " = " + expected + " but got " + value);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        private Object getJsonPath(Map<String, Object> json, String path) {
            String[] parts = path.split("\\.");
            Object current = json;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return null;
                }
            }
            return current;
        }

        // ========== Utilities ==========

        public TestResponse print() {
            System.out.println("Status: " + status());
            System.out.println("Headers: " + headers());
            System.out.println("Body: " + body());
            return this;
        }

        private String truncate(String s) {
            if (s == null) return "null";
            return s.length() > 500 ? s.substring(0, 500) + "..." : s;
        }

        @Override
        public String toString() {
            return "TestResponse[status=" + status() + ", body=" + truncate(body()) + "]";
        }
    }
}
