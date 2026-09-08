package com.osmig.Jweb.framework.routing;

import jweb.Auth;
import jweb.Element;
import jweb.Guard;
import jweb.JWeb;
import jweb.JWebTest;
import jweb.Middleware;
import jweb.MockRequest;
import jweb.MockSession;
import jweb.Principal;
import jweb.Request;
import jweb.Response;
import jweb.Template;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

class GuardsTest {

    public static class AdminPage implements Template {
        @Override public Element render() { return p("admin"); }
    }

    @Test
    void guardsRunInRegistrationOrderAndTheFirstAnswerWins() throws Exception {
        List<String> order = new ArrayList<>();
        Guards guards = new Guards()
            .add("/admin/**", req -> { order.add("first"); return null; })
            .add("/admin/**", req -> { order.add("second"); return "stop"; })
            .add("/admin/**", req -> { order.add("third"); return "never"; })
            .add("/other", req -> { order.add("other"); return "no"; });

        Object answer = guards.check(MockRequest.get("/admin/x").build());
        assertEquals("stop", answer);
        assertEquals(List.of("first", "second"), order);
        assertEquals(4, guards.size());
    }

    @Test
    void patternsFollowTheMiddlewareRules() {
        Guards guards = new Guards().add("/admin/**", req -> "a");
        assertTrue(guards.covers("/admin"));
        assertTrue(guards.covers("/admin/messages"));
        assertFalse(guards.covers("/administrator"));

        Guards glob = new Guards().add("/files/*.png", req -> "p");
        assertTrue(glob.covers("/files/a.png"));
        assertFalse(glob.covers("/files/a/b.png"));

        Guards prefix = new Guards().add("/api", req -> "x");
        assertTrue(prefix.covers("/api/v1/anything"));
        assertTrue(prefix.covers("/apix"), "a plain prefix is a plain prefix, like app.use");
    }

    @Test
    void middlewareBecomesAGuard() throws Exception {
        Middleware deny = (req, chain) -> Response.forbidden("nope");
        Middleware pass = (req, chain) -> chain.next();
        assertNotNull(Guard.of(deny).check(MockRequest.get("/x").build()));
        assertNull(Guard.of(pass).check(MockRequest.get("/x").build()));
    }

    @Test
    void guardsApplyToPageRoutesAndRouterRoutes() {
        JWeb app = JWeb.create()
            .guard("/admin/**", req -> req.principal() != null ? null : Response.redirect("/admin/login"))
            .pages("/admin/dashboard", AdminPage.class)
            .get("/admin/api", req -> "secret")
            .get("/public", req -> "open");

        var page = JWebTest.test(app, MockRequest.get("/admin/dashboard"));
        assertEquals(302, page.getStatus());
        assertEquals(302, JWebTest.test(app, MockRequest.get("/admin/api")).getStatus());
        assertEquals("open", JWebTest.test(app, MockRequest.get("/public")).getBody());

        MockSession session = new MockSession();
        Request login = MockRequest.get("/admin/login").session(session).build();
        Auth.login(login, Principal.of("1", "ada", "admin"));
        assertEquals("secret", JWebTest.test(app, MockRequest.get("/admin/api").session(session)).getBody());
        assertTrue(JWebTest.test(app, MockRequest.get("/admin/dashboard").session(session)).bodyContains("admin"));
    }

    @Test
    void requireLoginLetsTheLoginPageThroughAndRedirectsTheRest() throws Exception {
        Guard guard = Auth.requireLogin("/admin/login");
        assertNull(guard.check(MockRequest.get("/admin/login").build()));
        Object answer = guard.check(MockRequest.get("/admin/messages").build());
        assertInstanceOf(Response.Redirect.class, answer);
        assertEquals("/admin/login", ((Response.Redirect) answer).location());

        MockSession session = new MockSession();
        Auth.login(MockRequest.get("/x").session(session).build(), Principal.of("1"));
        assertNull(guard.check(MockRequest.get("/admin/messages").session(session).build()));
    }

    @Test
    void requireRoleWorksAsAGuardWithItsOwnStatuses() {
        JWeb app = JWeb.create()
            .guard("/admin/**", Auth.requireRole("admin"))
            .get("/admin/x", req -> "ok");

        // No principal → the middleware throws 401; JWebTest reports the failure as a 500
        // with the message, the controller maps JWebException to its status (see ApiHttpTest)
        var anonymous = JWebTest.test(app, MockRequest.get("/admin/x"));
        assertNotEquals(200, anonymous.getStatus());

        MockSession session = new MockSession();
        Auth.login(MockRequest.get("/x").session(session).build(), Principal.of("1", "ada", "admin"));
        assertEquals("ok", JWebTest.test(app, MockRequest.get("/admin/x").session(session)).getBody());
    }

    @Test
    void guardsRunAfterMiddlewareSoHeadersStillApply() {
        List<String> trace = new ArrayList<>();
        JWeb app = JWeb.create()
            .use((req, chain) -> { trace.add("middleware"); return chain.next(); })
            .guard("/x", req -> { trace.add("guard"); return Response.forbidden(); })
            .get("/x", req -> { trace.add("handler"); return "never"; });

        var result = JWebTest.test(app, MockRequest.get("/x"));
        assertEquals(403, result.getStatus());
        assertEquals(List.of("middleware", "guard"), trace);
    }

    @Test
    void redirectAnchorAndRedirectBack() {
        Response.Redirect r = Response.redirect("/about").anchor("access");
        assertEquals("/about#access", r.location());
        assertEquals(302, r.getStatusCode().value());
        assertEquals("/about#other", r.anchor("other").location(), "replaces the fragment");
        assertEquals(301, r.permanent().getStatusCode().value());
        assertEquals(303, Response.seeOther("/x").getStatusCode().value());
        ResponseEntity<Void> asEntity = Response.redirect("/x");   // still a ResponseEntity<Void>
        assertEquals("/x", asEntity.getHeaders().getLocation().toString());

        Request fromPage = MockRequest.get("/act/pref").header("Referer", "/about").build();
        assertEquals("/about", Response.redirectBack(fromPage).location());
        Request fromElsewhere = MockRequest.get("/act/pref").header("Referer", "https://evil.example/x").header("Host", "localhost").build();
        assertEquals("/", Response.redirectBack(fromElsewhere).location(), "foreign referers are not followed");
        Request sameHost = MockRequest.get("/act/pref").header("Referer", "http://localhost:8085/tide").header("Host", "localhost:8085").build();
        assertEquals("http://localhost:8085/tide", Response.redirectBack(sameHost).location());
        assertEquals("/home", Response.redirectBack(MockRequest.get("/x").build(), "/home").location());
    }
}
