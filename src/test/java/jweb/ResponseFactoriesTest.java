package jweb;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/** Every status a handler needs, without an HttpStatus import. */
class ResponseFactoriesTest {

    @Test
    void statusFactories() {
        assertEquals(500, Response.serverError().getStatusCode().value());
        assertEquals(500, Response.serverError("boom").getStatusCode().value());
        assertTrue(Response.serverError("boom").getBody().contains("boom"));
        assertEquals(401, Response.unauthorized().getStatusCode().value());
        assertEquals(403, Response.forbidden("no").getStatusCode().value());
        assertEquals(404, Response.notFound().getStatusCode().value());
        assertEquals(429, Response.tooManyRequests().getStatusCode().value());
        assertTrue(Response.tooManyRequests("slow down").getBody().contains("slow down"));
        assertEquals(204, Response.noContent().getStatusCode().value());
        assertEquals(201, Response.created("/items/42").getStatusCode().value());
        assertEquals("/items/42", Response.created("/items/42").getHeaders().getLocation().toString());
        assertEquals(201, Response.created("/items/42", java.util.Map.of("id", 42)).getStatusCode().value());
        assertEquals(418, Response.error(418, "teapot").getStatusCode().value());
        assertTrue(Response.badRequest("bad").getBody().contains("\"status\":400"));
    }

    @Test
    void statusBuilderAndContentTypeString() {
        ResponseEntity<Void> notModified = Response.status(304).header("ETag", "\"abc\"").header("Cache-Control", "no-cache").build();
        assertEquals(304, notModified.getStatusCode().value());
        assertEquals("\"abc\"", notModified.getHeaders().getFirst("ETag"));

        ResponseEntity<String> md = Response.ok().contentType("text/markdown; charset=UTF-8").body("# hi");
        assertEquals("text/markdown;charset=UTF-8", md.getHeaders().getContentType().toString());
        assertEquals("# hi", md.getBody());

        ResponseEntity<String> plain = Response.status(404).contentType("text/plain; charset=UTF-8").body("nope");
        assertEquals(404, plain.getStatusCode().value());
        assertEquals("text/plain", plain.getHeaders().getContentType().getType() + "/" + plain.getHeaders().getContentType().getSubtype());
    }

    @Test
    void htmlAndJsonWithAStatus() {
        ResponseEntity<String> html = Response.html(500, p("down"));
        assertEquals(500, html.getStatusCode().value());
        assertEquals("<p>down</p>", html.getBody());
        assertEquals("text/html", html.getHeaders().getContentType().toString());

        ResponseEntity<String> json = Response.json(201, java.util.Map.of("ok", true));
        assertEquals(201, json.getStatusCode().value());
        assertTrue(json.getBody().contains("\"ok\":true"));

        assertEquals(202, Response.json().put("queued", true).status(202).build().getStatusCode().value());
        assertEquals(418, Response.text(418, "tea").getStatusCode().value());
    }
}
