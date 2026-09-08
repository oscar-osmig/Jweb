package com.osmig.Jweb.framework.openapi;

import com.osmig.Jweb.app.api.ContactApi;
import com.osmig.Jweb.app.api.ExampleApi;
import com.osmig.Jweb.framework.util.Json;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The generated spec understands the {@code jweb.api} spellings — the
 * demo's own controllers are written with them, so the mounted
 * {@code /api/openapi.json} must describe every endpoint and parameter.
 */
class OpenApiJwebAnnotationsTest {

    @Test
    @SuppressWarnings("unchecked")
    void jwebAnnotatedControllersProduceAFullSpec() {
        String json = OpenApi.create().title("t").version("1").addApi(ExampleApi.class).addApi(ContactApi.class).toJson();
        Map<String, Object> spec = Json.parseMap(json);
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");

        assertTrue(paths.containsKey("/api/v1/example"), paths.keySet().toString());
        assertTrue(paths.containsKey("/api/v1/example/{id}"), paths.keySet().toString());
        assertTrue(paths.containsKey("/api/v1/example/search"), paths.keySet().toString());
        assertTrue(paths.containsKey("/api/v1/contact"), paths.keySet().toString());

        Map<String, Object> byId = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/v1/example/{id}")).get("get");
        List<Map<String, Object>> idParams = (List<Map<String, Object>>) byId.get("parameters");
        assertEquals(1, idParams.size(), idParams.toString());
        assertEquals("id", idParams.get(0).get("name"));
        assertEquals("path", idParams.get(0).get("in"));
        assertEquals(true, idParams.get(0).get("required"));

        Map<String, Object> search = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/v1/example/search")).get("get");
        List<Map<String, Object>> searchParams = (List<Map<String, Object>>) search.get("parameters");
        Map<String, Object> q = searchParams.stream().filter(p -> "q".equals(p.get("name"))).findFirst().orElseThrow();
        Map<String, Object> limit = searchParams.stream().filter(p -> "limit".equals(p.get("name"))).findFirst().orElseThrow();
        assertEquals("query", q.get("in"));
        assertEquals(true, q.get("required"));
        assertEquals(false, limit.get("required"), "a defaultValue makes it optional");
        assertEquals("10", limit.get("default"));

        Map<String, Object> create = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/v1/example")).get("post");
        assertTrue(create.containsKey("requestBody"), "@Body is the request body");
        Map<String, Object> update = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/v1/example/{id}")).get("put");
        assertNotNull(update, "@UPDATE maps to PUT");
        assertNotNull(((Map<String, Object>) paths.get("/api/v1/example/{id}")).get("delete"), "@DEL maps to DELETE");
    }
}
