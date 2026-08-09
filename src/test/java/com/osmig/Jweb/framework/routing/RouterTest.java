package com.osmig.Jweb.framework.routing;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RouterTest {

    @Test
    void matchesExactPath() {
        Router router = new Router();
        router.get("/users", req -> "list");

        assertTrue(router.match("GET", "/users").isPresent());
        assertTrue(router.match("GET", "/users/").isPresent()); // trailing slash tolerated
        assertTrue(router.match("GET", "/other").isEmpty());
    }

    @Test
    void extractsPathParams() {
        Router router = new Router();
        router.get("/users/:id/posts/:postId", req -> "post");

        var match = router.match("GET", "/users/42/posts/7").orElseThrow();
        assertEquals("42", match.params().get("id"));
        assertEquals("7", match.params().get("postId"));
    }

    @Test
    void headIsServedByGetHandlers() {
        Router router = new Router();
        router.get("/page", req -> "page");

        assertTrue(router.match("HEAD", "/page").isPresent());
    }

    @Test
    void methodMismatchReportsAllowedMethods() {
        Router router = new Router();
        router.get("/thing", req -> "get");
        router.post("/thing", req -> "post");

        assertTrue(router.match("DELETE", "/thing").isEmpty());
        assertEquals(Set.of("GET", "POST"), router.allowedMethods("/thing"));
        assertTrue(router.allowedMethods("/missing").isEmpty());
    }

    @Test
    void supportsPatch() {
        Router router = new Router();
        router.patch("/thing", req -> "patched");

        assertTrue(router.match("PATCH", "/thing").isPresent());
    }
}
