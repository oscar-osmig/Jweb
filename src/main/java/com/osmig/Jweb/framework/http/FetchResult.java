package com.osmig.Jweb.framework.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.osmig.Jweb.framework.util.Json;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the result of an HTTP fetch operation.
 *
 * <p>Provides convenient methods to access the response body as different types:</p>
 * <pre>
 * FetchResult result = Fetch.get("https://api.example.com/users").send();
 *
 * // Get as string
 * String body = result.body();
 *
 * // Parse as JSON object
 * User user = result.as(User.class);
 *
 * // Parse as JSON list
 * List&lt;User&gt; users = result.asList(User.class);
 *
 * // Check status
 * if (result.isOk()) { ... }
 * if (result.isError()) { ... }
 * </pre>
 */
public class FetchResult {

    private final int status;
    private final String body;
    private final Map<String, List<String>> headers;

    public FetchResult(int status, String body, Map<String, List<String>> headers) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    /**
     * Returns the HTTP status code.
     */
    public int status() {
        return status;
    }

    /**
     * Returns the raw response body as a string.
     */
    public String body() {
        return body;
    }

    /**
     * Returns all response headers.
     */
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * Returns a specific header value (first value if multiple).
     */
    public Optional<String> header(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            // Try case-insensitive lookup
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    values = entry.getValue();
                    break;
                }
            }
        }
        return values != null && !values.isEmpty()
            ? Optional.of(values.getFirst())
            : Optional.empty();
    }

    /**
     * Returns true if status is 2xx (success).
     */
    public boolean isOk() {
        return status >= 200 && status < 300;
    }

    /**
     * Returns true if status is 4xx or 5xx (error).
     */
    public boolean isError() {
        return status >= 400;
    }

    /**
     * Returns true if status is 4xx (client error).
     */
    public boolean isClientError() {
        return status >= 400 && status < 500;
    }

    /**
     * Returns true if status is 5xx (server error).
     */
    public boolean isServerError() {
        return status >= 500;
    }

    /**
     * Parses the body as JSON into the specified type.
     *
     * @param type the target class
     * @param <T>  the target type
     * @return the parsed object
     */
    public <T> T as(Class<T> type) {
        return Json.parse(body, type);
    }

    /**
     * Parses the body as JSON using a TypeReference (for generics).
     *
     * @param typeReference the type reference
     * @param <T>           the target type
     * @return the parsed object
     */
    public <T> T as(TypeReference<T> typeReference) {
        return Json.parse(body, typeReference);
    }

    /**
     * Parses the body as a JSON list of the specified type.
     *
     * @param elementType the element class
     * @param <T>         the element type
     * @return the parsed list
     */
    public <T> List<T> asList(Class<T> elementType) {
        return Json.parse(body, new TypeReference<List<T>>() {});
    }

    /**
     * Parses the body as a JSON map.
     *
     * @return the parsed map
     */
    public Map<String, Object> asMap() {
        return Json.parseMap(body);
    }

    /**
     * Safely parses the body, returning Optional.empty() on failure.
     *
     * @param type the target class
     * @param <T>  the target type
     * @return Optional containing the parsed object
     */
    public <T> Optional<T> tryAs(Class<T> type) {
        return Json.tryParse(body, type);
    }

    @Override
    public String toString() {
        return "FetchResult{status=" + status + ", bodyLength=" +
            (body != null ? body.length() : 0) + "}";
    }
}
