package com.osmig.Jweb.app;

import com.osmig.Jweb.app.api.AdminApi;
import com.osmig.Jweb.app.api.SnippetStore;
import jweb.Doc;
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
 * The snippets feature over real HTTP: the review API behind its header
 * credentials, the admin page behind the login, the public gallery and the
 * Sandbox's "Add snippet" form.
 */
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "jweb.data.enabled=false",
        "jweb.admin.email=admin@example.com",
        "jweb.admin.token=test-admin-token"
    })
class SnippetsHttpTest {

    @Autowired Environment env;
    @Autowired SnippetStore store;

    private final HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER).build();

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(
            "http://localhost:" + env.getProperty("local.server.port") + path));
    }

    private HttpRequest.Builder asAdmin(String path) {
        return request(path)
            .header(AdminApi.EMAIL_HEADER, "admin@example.com")
            .header(AdminApi.TOKEN_HEADER, "test-admin-token");
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void theReviewApiIsClosedWithoutTheAdminHeaders() throws Exception {
        HttpResponse<String> anonymous = send(request("/api/v1/admin/snippets").GET().build());
        assertEquals(401, anonymous.statusCode(), anonymous.body());
        assertTrue(anonymous.body().contains(AdminApi.TOKEN_HEADER), anonymous.body());

        HttpResponse<String> wrong = send(request("/api/v1/admin/snippets")
            .header(AdminApi.EMAIL_HEADER, "admin@example.com")
            .header(AdminApi.TOKEN_HEADER, "nope").GET().build());
        assertEquals(401, wrong.statusCode(), wrong.body());
    }

    @Test
    void theQueueCanBeReviewedFromTheApi() throws Exception {
        Doc s = store.submit("From the API test", "Ada", "p(\"api\")");
        String id = s.getId();

        HttpResponse<String> queue = send(asAdmin("/api/v1/admin/snippets").GET().build());
        assertEquals(200, queue.statusCode(), queue.body());
        assertTrue(queue.body().contains("\"status\":\"pending\""), queue.body());
        assertTrue(queue.body().contains("From the API test"), queue.body());

        HttpResponse<String> one = send(asAdmin("/api/v1/admin/snippets/" + id).GET().build());
        assertEquals(200, one.statusCode(), one.body());
        assertTrue(one.body().contains("\"renders\":true"), one.body());
        assertTrue(one.body().contains("<p>api</p>"), "the rendered preview rides along: " + one.body());

        HttpResponse<String> approved = send(asAdmin("/api/v1/admin/snippets/" + id + "/approve")
            .POST(HttpRequest.BodyPublishers.noBody()).build());
        assertEquals(200, approved.statusCode(), approved.body());
        assertTrue(approved.body().contains("\"approved\":true"), approved.body());

        HttpResponse<String> gallery = send(request("/snippets").GET().build());
        assertEquals(200, gallery.statusCode());
        assertTrue(gallery.body().contains("From the API test"), "approved snippets are on the public page");
        assertTrue(gallery.body().contains("<p>api</p>"), gallery.body());

        HttpResponse<String> published = send(asAdmin("/api/v1/admin/snippets?status=approved").GET().build());
        assertTrue(published.body().contains(id), published.body());

        HttpResponse<String> deleted = send(asAdmin("/api/v1/admin/snippets/" + id).DELETE().build());
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertTrue(deleted.body().contains("\"deleted\":true"), deleted.body());

        HttpResponse<String> again = send(asAdmin("/api/v1/admin/snippets/" + id).DELETE().build());
        assertEquals(404, again.statusCode(), again.body());
        HttpResponse<String> unknown = send(asAdmin("/api/v1/admin/snippets/" + id).GET().build());
        assertEquals(404, unknown.statusCode(), unknown.body());

        HttpResponse<String> badFilter = send(asAdmin("/api/v1/admin/snippets?status=bogus").GET().build());
        assertEquals(400, badFilter.statusCode(), badFilter.body());
    }

    @Test
    void theAdminPageIsBehindTheLogin() throws Exception {
        HttpResponse<String> page = send(request("/only-admin/snippets").GET().build());
        assertEquals(302, page.statusCode());
        assertEquals("/only-admin/log/in", page.headers().firstValue("Location").orElse(""));
    }

    @Test
    void theSandboxPageCarriesTheAddSnippetForm() throws Exception {
        HttpResponse<String> page = send(request("/sandbox").GET().build());
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("id=\"sandbox-add-snippet\""), "the chip");
        assertTrue(page.body().contains("data-swap-post=\"/snippets/submit\""), "the form posts to the submit route");
        assertTrue(page.body().contains("name=\"_csrf\""), "with the page's CSRF token");
    }
}
