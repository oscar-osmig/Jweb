package com.osmig.Jweb.app.api;

import jweb.Request;
import jweb.api.*;

import java.util.List;
import java.util.Map;

/**
 * Example REST API: JWeb's own annotations end to end — no Spring import.
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
    public Map<String, Object> getById(@Param int id) {
        return Map.of("id", id, "name", "Item " + id);
    }

    @GET("/search")
    public Map<String, Object> search(
            @Query("q") String query,
            @Query(value = "limit", defaultValue = "10") int limit) {
        return Map.of("query", query, "limit", limit, "results", List.of());
    }

    @POST
    public Map<String, Object> create(@Body Map<String, Object> data) {
        return Map.of("created", true, "data", data);
    }

    @UPDATE("/{id}")
    public Map<String, Object> update(@Param int id, @Body Map<String, Object> data) {
        return Map.of("updated", true, "id", id, "data", data);
    }

    @DEL("/{id}")
    public Map<String, Object> delete(@Param int id) {
        return Map.of("deleted", true, "id", id);
    }

    @GET("/info")
    public Map<String, Object> getInfo(Request request) {
        return Map.of(
            "path", request.path(),
            "method", request.method(),
            "ip", request.ip()
        );
    }
}
