package com.osmig.Jweb.framework.api;

import com.osmig.Jweb.app.App;
import com.osmig.Jweb.framework.JWeb;
import jweb.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The demo app over real HTTP: the {@code jweb.api} annotations end to end
 * through Spring MVC, guards in front of {@code @REST} controllers, the
 * admin guard, and the streaming demo's live markup.
 */
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "jweb.data.enabled=false")
class ApiHttpTest {

    @Autowired Environment env;
    @Autowired JWeb app;

    private final HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER).build();

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + env.getProperty("local.server.port") + path));
        for (int i = 0; i < headers.length; i += 2) b.header(headers[i], headers[i + 1]);
        return client.send(b.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + env.getProperty("local.server.port") + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void paramQueryBodyAndRequestResolveInARealController() throws Exception {
        HttpResponse<String> byId = get("/api/v1/example/42");
        assertEquals(200, byId.statusCode(), byId.body());
        assertTrue(byId.body().contains("\"id\":42"), byId.body());

        HttpResponse<String> search = get("/api/v1/example/search?q=java");
        assertEquals(200, search.statusCode(), search.body());
        assertTrue(search.body().contains("\"limit\":10"), search.body());
        assertTrue(search.body().contains("\"query\":\"java\""), search.body());

        HttpResponse<String> missing = get("/api/v1/example/search");
        assertEquals(400, missing.statusCode(), missing.body());

        HttpResponse<String> created = post("/api/v1/example", "{\"name\":\"x\"}");
        assertEquals(200, created.statusCode(), created.body());
        assertTrue(created.body().contains("\"created\":true"), created.body());

        HttpResponse<String> info = get("/api/v1/example/info");
        assertEquals(200, info.statusCode(), info.body());
        assertTrue(info.body().contains("\"path\":\"/api/v1/example/info\""), info.body());
        assertTrue(info.body().contains("\"method\":\"GET\""), info.body());
    }

    @Test
    void guardsCoverRestControllersUnderTheirPrefix() throws Exception {
        // A real @REST endpoint, guarded only when the test says so (the header), so the
        // other tests' calls to it are unaffected whatever the order
        app.guard("/api/v1/example/info", req -> "1".equals(req.header("X-Deny"))
            ? Response.unauthorized("guarded") : null);
        HttpResponse<String> guarded = get("/api/v1/example/info", "X-Deny", "1");
        assertEquals(401, guarded.statusCode(), guarded.body());
        assertTrue(guarded.body().contains("guarded"), guarded.body());
        String contentType = guarded.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"), contentType);
        assertTrue(contentType.toLowerCase().contains("utf-8"), "text goes out as UTF-8: " + contentType);
        assertEquals(200, get("/api/v1/example/info").statusCode(), "the guard passed");
        assertEquals(200, get("/api/v1/example/1").statusCode(), "sibling paths untouched");
    }

    @Test
    void adminGuardRedirectsAnonymousVisitorsAndLetsTheLoginPageThrough() throws Exception {
        HttpResponse<String> messages = get("/only-admin/messages");
        assertEquals(302, messages.statusCode());
        assertEquals("/only-admin/log/in", messages.headers().firstValue("Location").orElse(""));

        HttpResponse<String> login = get("/only-admin/log/in");
        assertEquals(200, login.statusCode());
        assertTrue(login.body().contains("Admin Login"));

        HttpResponse<String> badLimit = get("/only-admin/messages?limit=9999");
        assertEquals(302, badLimit.statusCode(), "the guard answers before binding");
    }

    @Test
    void jwebExceptionsCarryTheirStatusThroughTheController() throws Exception {
        app.guard("/guarded-page-by-test/**", jweb.Auth.requireRole("admin"));
        app.get("/guarded-page-by-test/x", req -> "never");
        HttpResponse<String> html = get("/guarded-page-by-test/x");
        assertEquals(401, html.statusCode(), html.body());
        HttpResponse<String> json = get("/guarded-page-by-test/x", "Accept", "application/json");
        assertEquals(401, json.statusCode());
        assertTrue(json.body().contains("Authentication required"), json.body());
    }

    @Test
    void streamingDemoShipsLiveMarkup() throws Exception {
        HttpResponse<String> page = get("/demo/streaming");
        assertEquals(200, page.statusCode());
        String body = page.body();
        assertTrue(body.contains("data-live=\"live_1\""), "live region rendered");
        assertTrue(body.contains("data-state-attr=\"disabled=state_"), "bindAttr rendered");
        assertTrue(body.contains("id=\"click-count\" data-state-bind=\"state_"), "bind rendered the value");
        assertTrue(body.contains(">0</span>"), "bind rendered the initial value");
        assertTrue(body.contains("No clicks yet."), "live region initial render");
    }

    /** Every response carries a request id — a REST controller's, a page's, an error's — and a caller's id is kept. */
    @Test
    void everyResponseCarriesARequestId() throws Exception {
        HttpResponse<String> rest = get("/api/v1/example/42");
        String id = rest.headers().firstValue("X-Request-ID").orElseThrow();
        assertTrue(id.matches("[0-9a-f]{20}"), id);

        HttpResponse<String> page = get("/");
        assertTrue(page.headers().firstValue("X-Request-ID").orElseThrow().matches("[0-9a-f]{20}"));
        assertNotEquals(id, page.headers().firstValue("X-Request-ID").orElseThrow());

        HttpResponse<String> kept = get("/api/v1/example/42", "X-Request-ID", "edge-0042");
        assertEquals("edge-0042", kept.headers().firstValue("X-Request-ID").orElse(""));

        HttpResponse<String> error = get("/api/v1/example/search", "X-Request-ID", "edge-0043", "Accept", "application/json");
        assertEquals(400, error.statusCode(), error.body());
        assertEquals("edge-0043", error.headers().firstValue("X-Request-ID").orElse(""));

        HttpResponse<String> missing = get("/no/such/page", "X-Request-ID", "edge-0044");
        assertEquals(404, missing.statusCode());
        assertEquals("edge-0044", missing.headers().firstValue("X-Request-ID").orElse(""));
    }

    /** The storage route mounted by the framework, through Spring MVC: type, ETag, 304, byte ranges. */
    @Test
    void storedFilesAreServedAtTheStoragePath() throws Exception {
        jweb.Storage.Store before = jweb.Storage.store();
        try {
            jweb.Storage.store(new jweb.Storage.Memory());
            jweb.Storage.put("docs/hello world.txt", "hello, world".getBytes(), "text/plain");

            HttpResponse<String> ok = get("/files/docs/hello%20world.txt");
            assertEquals(200, ok.statusCode(), ok.body());
            assertEquals("hello, world", ok.body());
            assertTrue(ok.headers().firstValue("Content-Type").orElse("").startsWith("text/plain"), ok.headers().map().toString());
            String etag = ok.headers().firstValue("ETag").orElseThrow();
            assertTrue(etag.startsWith("\""), etag);
            assertEquals("private, max-age=3600", ok.headers().firstValue("Cache-Control").orElse(""));

            HttpResponse<String> same = get("/files/docs/hello%20world.txt", "If-None-Match", etag);
            assertEquals(304, same.statusCode());

            HttpResponse<String> part = get("/files/docs/hello%20world.txt", "Range", "bytes=0-4");
            assertEquals(206, part.statusCode(), part.body());
            assertEquals("hello", part.body());
            assertEquals("bytes 0-4/12", part.headers().firstValue("Content-Range").orElse(""));

            assertEquals(404, get("/files/docs/nope.txt").statusCode());
        } finally {
            jweb.Storage.store(before);
        }
    }
}
