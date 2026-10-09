package jweb;

import com.osmig.Jweb.framework.util.Json;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Response utilities for building HTTP responses — every status a handler
 * needs, without an {@code HttpStatus} import.
 *
 * <h2>HTML Responses</h2>
 * <pre>
 * return Response.html(div(h1("Hello")));
 * return Response.html("&lt;h1&gt;Hello&lt;/h1&gt;");
 * return Response.html(500, errorPage);              // any status
 * </pre>
 *
 * <h2>JSON Responses</h2>
 * <pre>
 * return Response.json(user);
 * return Response.json(Map.of("status", "ok", "count", 42));
 * return Response.json(201, created);
 * return Response.json().put("name", "John").put("age", 30).build();
 * </pre>
 *
 * <h2>Redirects</h2>
 * <pre>
 * return Response.redirect("/dashboard");
 * return Response.redirect("/login", true);                       // permanent (301)
 * return Response.redirect("/about").anchor(saved ? "done" : "form");   // "/about#done"
 * return Response.redirectBack(req);                              // the Referer, or "/"
 * </pre>
 *
 * <h2>Statuses</h2>
 * <pre>
 * return Response.notFound();            // 404
 * return Response.badRequest("Invalid email format");
 * return Response.unauthorized();        // 401
 * return Response.forbidden();           // 403
 * return Response.tooManyRequests();     // 429
 * return Response.serverError();         // 500
 * return Response.noContent();           // 204
 * return Response.created("/items/42");  // 201 + Location
 * return Response.error(418, "I'm a teapot");
 * </pre>
 *
 * <h2>Custom Responses</h2>
 * <pre>
 * return Response.ok()
 *     .header("X-Custom", "value")
 *     .contentType("application/pdf")
 *     .body(pdfBytes);
 * return Response.status(304).header("ETag", etag).build();
 * </pre>
 */
public class Response {

    protected Response() {
        // Static utility class
    }

    // ========== HTML Responses ==========

    /**
     * Returns an HTML response from an Element.
     *
     * @param element the element to render
     * @return HTML response
     */
    public static ResponseEntity<String> html(jweb.Element element) {
        if (element instanceof com.osmig.Jweb.framework.template.Template template) {
            return new Page(200, template);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(element.toHtml());
    }

    /**
     * A {@code Response.html(template)} the router renders as a page: the
     * template's lifecycle hooks, collected styles and scripts, hydration data
     * and runtime all apply, exactly as when the handler returns the template
     * bare — plus the status and headers set here.
     */
    public static final class Page extends ResponseEntity<String> {
        private final com.osmig.Jweb.framework.template.Template template;
        private volatile String rendered;

        Page(int status, com.osmig.Jweb.framework.template.Template template) {
            super(null, htmlHeaders(), HttpStatusCode.valueOf(status));
            this.template = template;
        }

        private static HttpHeaders htmlHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return headers;
        }

        /** The template to render. */
        public com.osmig.Jweb.framework.template.Template template() {
            return template;
        }

        /** The template rendered bare — what a caller outside the router sees. */
        @Override
        public String getBody() {
            String html = rendered;
            if (html == null) {
                html = template.toHtml();
                rendered = html;
            }
            return html;
        }

        @Override
        public boolean hasBody() {
            return true;
        }
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
     * @param status  the HTTP status code
     * @param element the element to render
     * @return HTML response
     */
    public static ResponseEntity<String> html(int status, jweb.Element element) {
        if (element instanceof com.osmig.Jweb.framework.template.Template template) {
            return new Page(status, template);
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(element.toHtml());
    }

    /** Returns an HTML response with a specific status. */
    public static ResponseEntity<String> html(int status, String html) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Returns an HTML response with a specific status.
     *
     * @deprecated Use {@link #html(int, jweb.Element)} — no Spring import needed.
     */
    @Deprecated
    public static ResponseEntity<String> html(HttpStatus status, jweb.Element element) {
        return html(status.value(), element);
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
     * @param status the HTTP status code
     * @param body   the object to serialize
     * @return JSON response
     */
    public static ResponseEntity<String> json(int status, Object body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(body));
    }

    /**
     * Returns a JSON response with a specific status.
     *
     * @deprecated Use {@link #json(int, Object)} — no Spring import needed.
     */
    @Deprecated
    public static ResponseEntity<String> json(HttpStatus status, Object body) {
        return json(status.value(), body);
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
        private int status = 200;

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
         * @param status the status code
         * @return this builder
         */
        public JsonBuilder status(int status) {
            this.status = status;
            return this;
        }

        /** @deprecated Use {@link #status(int)}. */
        @Deprecated
        public JsonBuilder status(HttpStatus status) {
            return status(status.value());
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

    /** Returns a plain text response with a specific status. */
    public static ResponseEntity<String> text(int status, String text) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .body(text);
    }

    // ========== Redirects ==========

    /**
     * Returns a temporary redirect (302). Chain {@link Redirect#anchor} to
     * land on a fragment: {@code redirect("/about").anchor("access")}.
     *
     * @param location the redirect URL
     * @return redirect response
     */
    public static Redirect redirect(String location) {
        return new Redirect(location, HttpStatus.FOUND);
    }

    /**
     * Returns a redirect response.
     *
     * @param location  the redirect URL
     * @param permanent true for 301, false for 302
     * @return redirect response
     */
    public static Redirect redirect(String location, boolean permanent) {
        return new Redirect(location, permanent ? HttpStatus.MOVED_PERMANENTLY : HttpStatus.FOUND);
    }

    /**
     * Returns a "see other" redirect (303) - useful after POST.
     *
     * @param location the redirect URL
     * @return redirect response
     */
    public static Redirect seeOther(String location) {
        return new Redirect(location, HttpStatus.SEE_OTHER);
    }

    /**
     * Redirects to the page the request came from (its {@code Referer}), or
     * to {@code /} when there is none — the natural answer of an action
     * route that just mutated something.
     */
    public static Redirect redirectBack(Request request) {
        return redirectBack(request, "/");
    }

    /** {@link #redirectBack(Request)} with an explicit fallback. */
    public static Redirect redirectBack(Request request, String fallback) {
        String referer = request.header("Referer");
        return new Redirect(sameSite(referer, request) ? referer : fallback, HttpStatus.FOUND);
    }

    /** Only follow a Referer that points back at this site (an open redirect otherwise). */
    private static boolean sameSite(String referer, Request request) {
        if (referer == null || referer.isBlank()) return false;
        if (referer.startsWith("/")) return !referer.startsWith("//");
        try {
            URI uri = URI.create(referer);
            String host = request.header("Host");
            if (uri.getHost() == null || host == null) return false;
            String hostOnly = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
            return uri.getHost().equalsIgnoreCase(hostOnly);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * A redirect response — a {@code ResponseEntity<Void>} with a
     * {@code Location}, plus {@link #anchor} to pick the fragment:
     *
     * <pre>{@code
     * return Response.redirect("/worlds/tide").anchor(aligned ? "content" : "chamber");
     * }</pre>
     */
    public static final class Redirect extends ResponseEntity<Void> {

        private final String location;

        Redirect(String location, HttpStatusCode status) {
            super(null, locationHeader(location), status);
            this.location = location;
        }

        private static HttpHeaders locationHeader(String location) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(location));
            return headers;
        }

        /** The target URL. */
        public String location() {
            return location;
        }

        /** The same redirect to {@code location#anchor} (replacing any fragment). */
        public Redirect anchor(String anchor) {
            String base = location.contains("#") ? location.substring(0, location.indexOf('#')) : location;
            if (anchor == null || anchor.isBlank()) return new Redirect(base, getStatusCode());
            return new Redirect(base + "#" + (anchor.startsWith("#") ? anchor.substring(1) : anchor), getStatusCode());
        }

        /** The same redirect as a 301. */
        public Redirect permanent() {
            return new Redirect(location, HttpStatus.MOVED_PERMANENTLY);
        }

        /** The same redirect as a 303 (after a POST). */
        public Redirect seeOther() {
            return new Redirect(location, HttpStatus.SEE_OTHER);
        }
    }

    // ========== Success Responses ==========

    /**
     * Starts a 200 OK response with headers and a body.
     *
     * @return the builder
     */
    public static ResponseBuilder ok() {
        return new ResponseBuilder(200);
    }

    /**
     * Starts a response with any status code:
     * {@code Response.status(304).header("ETag", etag).build()}.
     *
     * @param code the HTTP status code
     * @return the builder
     */
    public static ResponseBuilder status(int code) {
        return new ResponseBuilder(code);
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

    /** Returns a 201 Created response with a JSON body and a Location. */
    public static ResponseEntity<String> created(String location, Object body) {
        return ResponseEntity.created(URI.create(location))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(body));
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
        return error(400, "Bad Request");
    }

    /**
     * Returns a 400 Bad Request response with a message.
     *
     * @param message the error message
     * @return bad request response
     */
    public static ResponseEntity<String> badRequest(String message) {
        return error(400, message);
    }

    /**
     * Returns a 401 Unauthorized response.
     *
     * @return unauthorized response
     */
    public static ResponseEntity<String> unauthorized() {
        return error(401, "Unauthorized");
    }

    /**
     * Returns a 401 Unauthorized response with a message.
     *
     * @param message the error message
     * @return unauthorized response
     */
    public static ResponseEntity<String> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * Returns a 403 Forbidden response.
     *
     * @return forbidden response
     */
    public static ResponseEntity<String> forbidden() {
        return error(403, "Forbidden");
    }

    /**
     * Returns a 403 Forbidden response with a message.
     *
     * @param message the error message
     * @return forbidden response
     */
    public static ResponseEntity<String> forbidden(String message) {
        return error(403, message);
    }

    /**
     * Returns a 404 Not Found response.
     *
     * @return not found response
     */
    public static ResponseEntity<String> notFound() {
        return error(404, "Not Found");
    }

    /**
     * Returns a 404 Not Found response with a message.
     *
     * @param message the error message
     * @return not found response
     */
    public static ResponseEntity<String> notFound(String message) {
        return error(404, message);
    }

    /** Returns a 429 Too Many Requests response. */
    public static ResponseEntity<String> tooManyRequests() {
        return error(429, "Too Many Requests");
    }

    /** Returns a 429 Too Many Requests response with a message. */
    public static ResponseEntity<String> tooManyRequests(String message) {
        return error(429, message);
    }

    /**
     * Returns a 500 Internal Server Error response.
     *
     * @return server error response
     */
    public static ResponseEntity<String> serverError() {
        return error(500, "Internal Server Error");
    }

    /**
     * Returns a 500 Internal Server Error response with a message.
     *
     * @param message the error message
     * @return server error response
     */
    public static ResponseEntity<String> serverError(String message) {
        return error(500, message);
    }

    /**
     * Returns an error response with custom status and message.
     *
     * @deprecated Use {@link #error(int, String)} — no Spring import needed.
     */
    @Deprecated
    public static ResponseEntity<String> error(HttpStatus status, String message) {
        return error(status.value(), message);
    }

    /**
     * Returns a JSON error response with a numeric status code:
     * {@code {"error":true,"status":418,"message":"..."}}.
     *
     * @param statusCode the HTTP status code
     * @param message    the error message
     * @return error response
     */
    public static ResponseEntity<String> error(int statusCode, String message) {
        return ResponseEntity.status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(Map.of(
                        "error", true,
                        "status", statusCode,
                        "message", message
                )));
    }

    // ========== Response Builder ==========

    /**
     * Builder for creating custom responses.
     */
    public static class ResponseBuilder {
        private final int status;
        private final HttpHeaders headers = new HttpHeaders();
        private MediaType contentType;

        ResponseBuilder(int status) {
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
            if (value != null) headers.add(name, value);
            return this;
        }

        /**
         * Sets the content type: {@code "text/markdown; charset=UTF-8"}.
         *
         * @param contentType the media type
         * @return this builder
         */
        public ResponseBuilder contentType(String contentType) {
            this.contentType = MediaType.parseMediaType(contentType);
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
