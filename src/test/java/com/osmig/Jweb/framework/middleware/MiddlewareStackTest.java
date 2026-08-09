package com.osmig.Jweb.framework.middleware;

import com.osmig.Jweb.framework.server.Request;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MiddlewareStackTest {

    private Request request(String method, String path) {
        return new Request(new MockHttpServletRequest(method, path));
    }

    @Test
    void executesInRegistrationOrder() throws Exception {
        List<String> order = new ArrayList<>();
        MiddlewareStack stack = new MiddlewareStack()
            .use((req, chain) -> { order.add("first"); return chain.next(); })
            .use((req, chain) -> { order.add("second"); return chain.next(); });

        Object result = stack.execute(request("GET", "/"), () -> { order.add("handler"); return "ok"; });

        assertEquals("ok", result);
        assertEquals(List.of("first", "second", "handler"), order);
    }

    @Test
    void middlewareCanShortCircuit() throws Exception {
        MiddlewareStack stack = new MiddlewareStack()
            .use((req, chain) -> "blocked");

        Object result = stack.execute(request("GET", "/"), () -> "handler");

        assertEquals("blocked", result);
    }

    @Test
    void pathScopingSupportsPrefixes() throws Exception {
        List<String> hits = new ArrayList<>();
        MiddlewareStack stack = new MiddlewareStack()
            .useForPath("/api", (req, chain) -> { hits.add(req.path()); return chain.next(); });

        stack.execute(request("GET", "/api/users"), () -> "ok");
        stack.execute(request("GET", "/home"), () -> "ok");

        assertEquals(List.of("/api/users"), hits);
    }

    @Test
    void pathScopingSupportsGlobs() throws Exception {
        List<String> hits = new ArrayList<>();
        MiddlewareStack stack = new MiddlewareStack()
            .useForPath("/api/**", (req, chain) -> { hits.add(req.path()); return chain.next(); });

        stack.execute(request("GET", "/api"), () -> "ok");
        stack.execute(request("GET", "/api/users/42"), () -> "ok");
        stack.execute(request("GET", "/apix"), () -> "ok");

        assertEquals(List.of("/api", "/api/users/42"), hits);
    }

    @Test
    void headerMiddlewaresQueueHeadersForAnyResultType() throws Exception {
        Request req = request("GET", "/page");
        MiddlewareStack stack = new MiddlewareStack()
            .use(Middlewares.securityHeaders());

        // Handler returns a plain String (not a ResponseEntity)
        stack.execute(req, () -> "<html></html>");

        assertEquals("nosniff", req.responseHeaders().get("X-Content-Type-Options"));
        assertEquals("DENY", req.responseHeaders().get("X-Frame-Options"));
    }
}
