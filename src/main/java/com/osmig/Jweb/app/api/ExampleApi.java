package com.osmig.Jweb.app.api;

import com.osmig.Jweb.framework.api.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Example REST API using simplified JWeb annotations.
 */
@REST("/api/v1/example")
public class ExampleApi {

    @GET
    public List<Map<String, Object>> getAll() {
        return List.of(
            Map.of("id", 1, "name", "Item 1"),
            Map.of("id", 2, "name", "Item 2")
        );
    }

    @GET("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        return Map.of("id", id, "name", "Item " + id);
    }

    @GET("/search")
    public Map<String, Object> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Map.of("query", query, "limit", limit, "results", List.of());
    }

    @POST
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        return Map.of("created", true, "data", data);
    }

    @UPDATE("/{id}")
    public Map<String, Object> update(@PathVariable int id, @RequestBody Map<String, Object> data) {
        return Map.of("updated", true, "id", id, "data", data);
    }

    @DEL("/{id}")
    public Map<String, Object> delete(@PathVariable int id) {
        return Map.of("deleted", true, "id", id);
    }

    @GET("/info")
    public Map<String, Object> getInfo(HttpServletRequest request) {
        return Map.of(
            "path", request.getRequestURI(),
            "method", request.getMethod(),
            "ip", request.getRemoteAddr()
        );
    }
}
