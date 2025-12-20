package com.osmig.Jweb.framework.testing;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.server.Request;
import com.osmig.Jweb.framework.util.Json;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test utilities for JWeb applications.
 *
 * <p>Provides convenient methods for testing routes, handlers, and elements.</p>
 *
 * <h2>Testing Routes</h2>
 * <pre>
 * JWeb app = JWeb.create()
 *     .get("/", () -> h1("Hello"))
 *     .get("/users/:id", req -> h1("User " + req.param("id")));
 *
 * // Test a route
 * JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/"));
 * assert result.isSuccess();
 * assert result.bodyContains("Hello");
 *
 * // Test with path params
 * JWebTest.TestResult result = JWebTest.test(app, MockRequest.get("/users/123"));
 * assert result.bodyContains("User 123");
 * </pre>
 *
 * <h2>Testing Handlers Directly</h2>
 * <pre>
 * RouteHandler handler = req -> {
 *     String name = req.query("name");
 *     return div(h1("Hello, " + name));
 * };
 *
 * Request req = MockRequest.get("/greet").queryParam("name", "World").build();
 * Element result = (Element) handler.handle(req);
 * String html = result.toHtml();
 * assert html.contains("Hello, World");
 * </pre>
 *
 * <h2>Testing Elements</h2>
 * <pre>
 * Element card = Card.render("Title", "Content");
 * String html = card.toHtml();
 *
 * // Assert content
 * JWebTest.assertContains(html, "Title");
 * JWebTest.assertContains(html, "Content");
 *
 * // Assert attributes
 * JWebTest.assertHasClass(html, "card");
 * JWebTest.assertHasAttribute(html, "id", "my-card");
 * </pre>
 */
public final class JWebTest {

    private JWebTest() {
        // Static utility class
    }

    // ========== Route Testing ==========

    /**
     * Tests a route on the JWeb app.
     */
    public static TestResult test(JWeb app, MockRequest mockRequest) {
        Request request = mockRequest.build();
        String path = request.path();
        String method = request.method();

        var match = app.getRouter().match(method, path);
        if (match.isEmpty()) {
            return new TestResult(404, null, "Route not found: " + method + " " + path);
        }

        try {
            Object result = app.getMiddlewareStack().execute(request, () -> match.get().handle(request));
            return TestResult.from(result);
        } catch (Exception e) {
            return new TestResult(500, null, e.getMessage());
        }
    }

    /**
     * Tests a handler directly with a mock request.
     */
    public static TestResult testHandler(RouteHandler handler, MockRequest mockRequest) {
        Request request = mockRequest.build();
        try {
            Object result = handler.handle(request);
            return TestResult.from(result);
        } catch (Exception e) {
            return new TestResult(500, null, e.getMessage());
        }
    }

    /**
     * Tests a supplier (for simple routes without request access).
     */
    public static TestResult testSupplier(Supplier<Element> supplier) {
        try {
            Element result = supplier.get();
            return TestResult.from(result);
        } catch (Exception e) {
            return new TestResult(500, null, e.getMessage());
        }
    }

    // ========== Assertion Helpers ==========

    /**
     * Asserts that the HTML contains the specified text.
     */
    public static void assertContains(String html, String expected) {
        if (!html.contains(expected)) {
            throw new AssertionError("Expected HTML to contain '" + expected + "' but it didn't.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that the HTML does not contain the specified text.
     */
    public static void assertNotContains(String html, String notExpected) {
        if (html.contains(notExpected)) {
            throw new AssertionError("Expected HTML to NOT contain '" + notExpected + "' but it did.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that an element with the specified CSS class exists.
     */
    public static void assertHasClass(String html, String className) {
        Pattern pattern = Pattern.compile("class\\s*=\\s*[\"'][^\"']*\\b" + Pattern.quote(className) + "\\b[^\"']*[\"']");
        if (!pattern.matcher(html).find()) {
            throw new AssertionError("Expected element with class '" + className + "' but not found.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that an element with the specified ID exists.
     */
    public static void assertHasId(String html, String id) {
        Pattern pattern = Pattern.compile("id\\s*=\\s*[\"']" + Pattern.quote(id) + "[\"']");
        if (!pattern.matcher(html).find()) {
            throw new AssertionError("Expected element with id '" + id + "' but not found.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that an attribute exists with a specific value.
     */
    public static void assertHasAttribute(String html, String name, String value) {
        Pattern pattern = Pattern.compile(Pattern.quote(name) + "\\s*=\\s*[\"']" + Pattern.quote(value) + "[\"']");
        if (!pattern.matcher(html).find()) {
            throw new AssertionError("Expected attribute " + name + "=\"" + value + "\" but not found.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that the HTML contains a specific tag.
     */
    public static void assertHasTag(String html, String tagName) {
        Pattern pattern = Pattern.compile("<" + Pattern.quote(tagName) + "[\\s>]");
        if (!pattern.matcher(html).find()) {
            throw new AssertionError("Expected <" + tagName + "> tag but not found.\nActual: " + truncate(html));
        }
    }

    /**
     * Asserts that the HTML matches a regex pattern.
     */
    public static void assertMatches(String html, String regex) {
        if (!Pattern.compile(regex).matcher(html).find()) {
            throw new AssertionError("Expected HTML to match pattern '" + regex + "' but it didn't.\nActual: " + truncate(html));
        }
    }

    private static String truncate(String s) {
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    // ========== TestResult ==========

    /**
     * Result of testing a route or handler.
     */
    public static class TestResult {
        private final int status;
        private final String body;
        private final String error;

        public TestResult(int status, String body, String error) {
            this.status = status;
            this.body = body;
            this.error = error;
        }

        public static TestResult from(Object result) {
            if (result == null) {
                return new TestResult(200, "", null);
            }
            if (result instanceof ResponseEntity<?> response) {
                Object responseBody = response.getBody();
                return new TestResult(
                        response.getStatusCode().value(),
                        responseBody != null ? responseBody.toString() : "",
                        null
                );
            }
            if (result instanceof Element element) {
                return new TestResult(200, element.toHtml(), null);
            }
            if (result instanceof String str) {
                return new TestResult(200, str, null);
            }
            return new TestResult(200, result.toString(), null);
        }

        public int getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return status >= 200 && status < 300 && error == null;
        }

        public boolean isClientError() {
            return status >= 400 && status < 500;
        }

        public boolean isServerError() {
            return status >= 500;
        }

        public boolean bodyContains(String text) {
            return body != null && body.contains(text);
        }

        public boolean bodyMatches(String regex) {
            return body != null && Pattern.compile(regex).matcher(body).find();
        }

        public <T> T bodyAs(Class<T> type) {
            return Json.parse(body, type);
        }

        public Map<String, Object> bodyAsMap() {
            return Json.parseMap(body);
        }

        @Override
        public String toString() {
            return "TestResult[status=" + status + ", body=" + truncate(body != null ? body : "") + ", error=" + error + "]";
        }

        private String truncate(String s) {
            return s.length() > 100 ? s.substring(0, 100) + "..." : s;
        }
    }
}
