package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.testing.JWebTest;
import com.osmig.Jweb.framework.testing.MockRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TypedRouteTest {

    static final TypedRoute.Path1<Long> USER = TypedRoute.path("/users/:id", Long.class);
    static final TypedRoute.Path2<String, Integer> POST =
        TypedRoute.path("/blog/:slug/comments/:page", String.class, Integer.class);

    @Test
    void urlBuildingSubstitutesAndEncodes() {
        assertEquals("/users/42", USER.url(42L));
        assertEquals("/blog/hello-world/comments/3", POST.url("hello-world", 3));
        // URL-encoding of unsafe values
        TypedRoute.Path1<String> search = TypedRoute.path("/tag/:name", String.class);
        assertEquals("/tag/a%2Fb+c", search.url("a/b c"));
    }

    @Test
    void patternParamCountMustMatchTypes() {
        assertThrows(IllegalArgumentException.class,
            () -> TypedRoute.path("/users/:id/:extra", Long.class));
        assertThrows(IllegalArgumentException.class,
            () -> TypedRoute.path("/users", Long.class));
    }

    @Test
    void handlersReceiveParsedTypedParams() {
        JWeb app = JWeb.create();
        app.get(USER, (req, id) -> "user:" + (id + 1));   // arithmetic proves it's a Long

        var result = JWebTest.test(app, MockRequest.get("/users/41"));
        assertEquals(200, result.getStatus());
        assertTrue(result.getBody().contains("user:42"));
    }

    @Test
    void twoParamRoutesParseBoth() {
        JWeb app = JWeb.create();
        app.get(POST, (req, slug, page) -> slug + "/page-" + (page * 2));

        var result = JWebTest.test(app, MockRequest.get("/blog/intro/comments/5"));
        assertTrue(result.getBody().contains("intro/page-10"));
    }

    @Test
    void invalidParamValueThrowsTypedException() {
        TypedRoute.Path1<Long> route = TypedRoute.path("/n/:num", Long.class);
        var ex = assertThrows(TypedRoute.RouteParamException.class,
            () -> route.parse("not-a-number"));
        assertTrue(ex.getMessage().contains("num"));
        assertTrue(ex.getMessage().contains("Long"));
    }

    @Test
    void uuidParamsSupported() {
        TypedRoute.Path1<UUID> route = TypedRoute.path("/things/:uuid", UUID.class);
        UUID id = UUID.randomUUID();
        assertEquals("/things/" + id, route.url(id));
        assertEquals(id, route.parse(id.toString()));
    }
}
