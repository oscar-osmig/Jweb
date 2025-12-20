package com.osmig.Jweb.framework.http;

import com.osmig.Jweb.framework.util.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Fluent HTTP client for making API requests.
 *
 * <p>Built on Java's HttpClient, provides a simple API for fetching data from external services.</p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>
 * // Simple GET request
 * FetchResult result = Fetch.get("https://api.example.com/users").send();
 * List&lt;User&gt; users = result.asList(User.class);
 *
 * // GET with headers
 * FetchResult result = Fetch.get("https://api.example.com/me")
 *     .header("Authorization", "Bearer token123")
 *     .send();
 *
 * // POST with JSON body
 * FetchResult result = Fetch.post("https://api.example.com/users")
 *     .header("Authorization", "Bearer token123")
 *     .json(newUser)
 *     .send();
 *
 * // PUT request
 * FetchResult result = Fetch.put("https://api.example.com/users/1")
 *     .json(updatedUser)
 *     .send();
 *
 * // DELETE request
 * FetchResult result = Fetch.delete("https://api.example.com/users/1")
 *     .header("Authorization", "Bearer token123")
 *     .send();
 *
 * // With timeout
 * FetchResult result = Fetch.get("https://api.example.com/slow")
 *     .timeout(Duration.ofSeconds(30))
 *     .send();
 *
 * // Check response
 * if (result.isOk()) {
 *     User user = result.as(User.class);
 * } else {
 *     System.err.println("Error: " + result.status());
 * }
 * </pre>
 */
public class Fetch {

    private static final HttpClient sharedClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private final String method;
    private final String url;
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private Duration timeout = Duration.ofSeconds(30);

    private Fetch(String method, String url) {
        this.method = method;
        this.url = url;
    }

    // ========== Factory Methods ==========

    /**
     * Creates a GET request.
     *
     * @param url the URL to fetch
     * @return the Fetch builder
     */
    public static Fetch get(String url) {
        return new Fetch("GET", url);
    }

    /**
     * Creates a POST request.
     *
     * @param url the URL to post to
     * @return the Fetch builder
     */
    public static Fetch post(String url) {
        return new Fetch("POST", url);
    }

    /**
     * Creates a PUT request.
     *
     * @param url the URL to put to
     * @return the Fetch builder
     */
    public static Fetch put(String url) {
        return new Fetch("PUT", url);
    }

    /**
     * Creates a PATCH request.
     *
     * @param url the URL to patch
     * @return the Fetch builder
     */
    public static Fetch patch(String url) {
        return new Fetch("PATCH", url);
    }

    /**
     * Creates a DELETE request.
     *
     * @param url the URL to delete
     * @return the Fetch builder
     */
    public static Fetch delete(String url) {
        return new Fetch("DELETE", url);
    }

    // ========== Builder Methods ==========

    /**
     * Adds a header to the request.
     *
     * @param name  the header name
     * @param value the header value
     * @return this builder
     */
    public Fetch header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Adds the Authorization header with a Bearer token.
     *
     * @param token the bearer token
     * @return this builder
     */
    public Fetch bearer(String token) {
        return header("Authorization", "Bearer " + token);
    }

    /**
     * Adds the Authorization header with Basic auth.
     *
     * @param username the username
     * @param password the password
     * @return this builder
     */
    public Fetch basicAuth(String username, String password) {
        String credentials = java.util.Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes());
        return header("Authorization", "Basic " + credentials);
    }

    /**
     * Sets the Content-Type header.
     *
     * @param contentType the content type
     * @return this builder
     */
    public Fetch contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    /**
     * Sets the Accept header.
     *
     * @param accept the accept header value
     * @return this builder
     */
    public Fetch accept(String accept) {
        return header("Accept", accept);
    }

    /**
     * Sets a JSON body from an object (serializes to JSON).
     *
     * @param data the object to serialize
     * @return this builder
     */
    public Fetch json(Object data) {
        this.body = Json.stringify(data);
        return contentType("application/json");
    }

    /**
     * Sets the raw request body.
     *
     * @param body the body string
     * @return this builder
     */
    public Fetch body(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets form-urlencoded body from a map.
     *
     * @param formData the form data
     * @return this builder
     */
    public Fetch form(Map<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(java.net.URLEncoder.encode(entry.getKey(), java.nio.charset.StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        }
        this.body = sb.toString();
        return contentType("application/x-www-form-urlencoded");
    }

    /**
     * Sets the request timeout.
     *
     * @param timeout the timeout duration
     * @return this builder
     */
    public Fetch timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    // ========== Execute ==========

    /**
     * Sends the request and returns the result.
     *
     * @return the fetch result
     * @throws FetchException if the request fails
     */
    public FetchResult send() {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout);

            // Add headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // Set method and body
            HttpRequest.BodyPublisher bodyPublisher = body != null
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();

            requestBuilder.method(method, bodyPublisher);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = sharedClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return new FetchResult(
                response.statusCode(),
                response.body(),
                response.headers().map()
            );

        } catch (java.io.IOException e) {
            throw new FetchException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FetchException("Request interrupted", e);
        } catch (IllegalArgumentException e) {
            throw new FetchException("Invalid URL: " + url, e);
        }
    }

    /**
     * Sends the request asynchronously.
     *
     * @return a CompletableFuture with the result
     */
    public java.util.concurrent.CompletableFuture<FetchResult> sendAsync() {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout);

            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            HttpRequest.BodyPublisher bodyPublisher = body != null
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();

            requestBuilder.method(method, bodyPublisher);

            HttpRequest request = requestBuilder.build();

            return sharedClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> new FetchResult(
                    response.statusCode(),
                    response.body(),
                    response.headers().map()
                ));

        } catch (IllegalArgumentException e) {
            return java.util.concurrent.CompletableFuture.failedFuture(
                new FetchException("Invalid URL: " + url, e));
        }
    }

    /**
     * Exception thrown when a fetch operation fails.
     */
    public static class FetchException extends RuntimeException {
        public FetchException(String message, Throwable cause) {
            super(message, cause);
        }

        public FetchException(String message) {
            super(message);
        }
    }
}
