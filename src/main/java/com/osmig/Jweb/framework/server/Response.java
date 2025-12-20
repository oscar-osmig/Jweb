package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.util.Json;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Response utilities for building HTTP responses.
 *
 * <p>Provides convenient static methods for common response types.</p>
 *
 * <h2>HTML Responses</h2>
 * <pre>
 * // Return an Element as HTML
 * return Response.html(div(h1("Hello")));
 *
 * // Return raw HTML string
 * return Response.html("&lt;h1&gt;Hello&lt;/h1&gt;");
 * </pre>
 *
 * <h2>JSON Responses</h2>
 * <pre>
 * // Return object as JSON
 * return Response.json(user);
 *
 * // Return map as JSON
 * return Response.json(Map.of("status", "ok", "count", 42));
 *
 * // Build JSON inline
 * return Response.json()
 *     .put("name", "John")
 *     .put("age", 30)
 *     .build();
 * </pre>
 *
 * <h2>Redirects</h2>
 * <pre>
 * return Response.redirect("/dashboard");
 * return Response.redirect("/login", true);  // permanent redirect
 * </pre>
 *
 * <h2>Error Responses</h2>
 * <pre>
 * return Response.notFound();
 * return Response.badRequest("Invalid email format");
 * return Response.unauthorized();
 * return Response.forbidden();
 * return Response.error(500, "Something went wrong");
 * </pre>
 *
 * <h2>Custom Responses</h2>
 * <pre>
 * return Response.ok()
 *     .header("X-Custom", "value")
 *     .contentType(MediaType.APPLICATION_PDF)
 *     .body(pdfBytes);
 * </pre>
 */
public final class Response {

    private Response() {
        // Static utility class
    }

    // ========== HTML Responses ==========

    /**
     * Returns an HTML response from an Element.
     *
     * @param element the element to render
     * @return HTML response
     */
    public static ResponseEntity<String> html(Element element) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(element.toHtml());
    }

    /**
     * Returns an HTML response from a string.
     *
     * @param html the HTML content
     * @return HTML response
     */
    public static ResponseEntity<String> html(String html) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Returns an HTML response with a specific status.
     *
     * @param status  the HTTP status
     * @param element the element to render
     * @return HTML response
     */
    public static ResponseEntity<String> html(HttpStatus status, Element element) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(element.toHtml());
    }

    // ========== JSON Responses ==========

    /**
     * Returns a JSON response from an object.
     *
     * @param body the object to serialize
     * @return JSON response
     */
    public static ResponseEntity<String> json(Object body) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(body));
    }

    /**
     * Returns a JSON response with a specific status.
     *
     * @param status the HTTP status
     * @param body   the object to serialize
     * @return JSON response
     */
    public static ResponseEntity<String> json(HttpStatus status, Object body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(body));
    }

    /**
     * Creates a JSON builder for inline construction.
     *
     * @return a new JSON builder
     */
    public static JsonBuilder json() {
        return new JsonBuilder();
    }

    /**
     * Builder for constructing JSON responses inline.
     */
    public static class JsonBuilder {
        private final Map<String, Object> data = new HashMap<>();
        private HttpStatus status = HttpStatus.OK;

        /**
         * Adds a key-value pair to the JSON.
         *
         * @param key   the key
         * @param value the value
         * @return this builder
         */
        public JsonBuilder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        /**
         * Sets the HTTP status for the response.
         *
         * @param status the status
         * @return this builder
         */
        public JsonBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Builds the JSON response.
         *
         * @return the response entity
         */
        public ResponseEntity<String> build() {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Json.stringify(data));
        }
    }

    // ========== Text Responses ==========

    /**
     * Returns a plain text response.
     *
     * @param text the text content
     * @return text response
     */
    public static ResponseEntity<String> text(String text) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(text);
    }

    // ========== Redirects ==========

    /**
     * Returns a temporary redirect (302).
     *
     * @param location the redirect URL
     * @return redirect response
     */
    public static ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    /**
     * Returns a redirect response.
     *
     * @param location  the redirect URL
     * @param permanent true for 301, false for 302
     * @return redirect response
     */
    public static ResponseEntity<Void> redirect(String location, boolean permanent) {
        HttpStatus status = permanent ? HttpStatus.MOVED_PERMANENTLY : HttpStatus.FOUND;
        return ResponseEntity.status(status)
                .location(URI.create(location))
                .build();
    }

    /**
     * Returns a "see other" redirect (303) - useful after POST.
     *
     * @param location the redirect URL
     * @return redirect response
     */
    public static ResponseEntity<Void> seeOther(String location) {
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(location))
                .build();
    }

    // ========== Success Responses ==========

    /**
     * Returns an empty 200 OK response.
     *
     * @return OK response
     */
    public static ResponseBuilder ok() {
        return new ResponseBuilder(HttpStatus.OK);
    }

    /**
     * Returns a 201 Created response.
     *
     * @param location the URL of the created resource
     * @return created response
     */
    public static ResponseEntity<Void> created(String location) {
        return ResponseEntity.created(URI.create(location)).build();
    }

    /**
     * Returns a 204 No Content response.
     *
     * @return no content response
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    // ========== Error Responses ==========

    /**
     * Returns a 400 Bad Request response.
     *
     * @return bad request response
     */
    public static ResponseEntity<String> badRequest() {
        return error(HttpStatus.BAD_REQUEST, "Bad Request");
    }

    /**
     * Returns a 400 Bad Request response with a message.
     *
     * @param message the error message
     * @return bad request response
     */
    public static ResponseEntity<String> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Returns a 401 Unauthorized response.
     *
     * @return unauthorized response
     */
    public static ResponseEntity<String> unauthorized() {
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    /**
     * Returns a 401 Unauthorized response with a message.
     *
     * @param message the error message
     * @return unauthorized response
     */
    public static ResponseEntity<String> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Returns a 403 Forbidden response.
     *
     * @return forbidden response
     */
    public static ResponseEntity<String> forbidden() {
        return error(HttpStatus.FORBIDDEN, "Forbidden");
    }

    /**
     * Returns a 403 Forbidden response with a message.
     *
     * @param message the error message
     * @return forbidden response
     */
    public static ResponseEntity<String> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    /**
     * Returns a 404 Not Found response.
     *
     * @return not found response
     */
    public static ResponseEntity<String> notFound() {
        return error(HttpStatus.NOT_FOUND, "Not Found");
    }

    /**
     * Returns a 404 Not Found response with a message.
     *
     * @param message the error message
     * @return not found response
     */
    public static ResponseEntity<String> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Returns a 500 Internal Server Error response.
     *
     * @return server error response
     */
    public static ResponseEntity<String> serverError() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    /**
     * Returns a 500 Internal Server Error response with a message.
     *
     * @param message the error message
     * @return server error response
     */
    public static ResponseEntity<String> serverError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Returns an error response with custom status and message.
     *
     * @param status  the HTTP status
     * @param message the error message
     * @return error response
     */
    public static ResponseEntity<String> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(Map.of(
                        "error", true,
                        "status", status.value(),
                        "message", message
                )));
    }

    /**
     * Returns an error response with a numeric status code.
     *
     * @param statusCode the HTTP status code
     * @param message    the error message
     * @return error response
     */
    public static ResponseEntity<String> error(int statusCode, String message) {
        return error(HttpStatus.valueOf(statusCode), message);
    }

    // ========== Response Builder ==========

    /**
     * Builder for creating custom responses.
     */
    public static class ResponseBuilder {
        private final HttpStatus status;
        private final HttpHeaders headers = new HttpHeaders();
        private MediaType contentType;

        ResponseBuilder(HttpStatus status) {
            this.status = status;
        }

        /**
         * Adds a header to the response.
         *
         * @param name  the header name
         * @param value the header value
         * @return this builder
         */
        public ResponseBuilder header(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Sets the content type.
         *
         * @param contentType the content type
         * @return this builder
         */
        public ResponseBuilder contentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Builds an empty response.
         *
         * @return the response
         */
        public ResponseEntity<Void> build() {
            var builder = ResponseEntity.status(status).headers(headers);
            if (contentType != null) {
                builder.contentType(contentType);
            }
            return builder.build();
        }

        /**
         * Builds a response with a body.
         *
         * @param body the response body
         * @param <T>  the body type
         * @return the response
         */
        public <T> ResponseEntity<T> body(T body) {
            var builder = ResponseEntity.status(status).headers(headers);
            if (contentType != null) {
                builder.contentType(contentType);
            }
            return builder.body(body);
        }
    }
}
