package com.osmig.Jweb.framework.routing;

import jweb.Element;
import jweb.JWeb;
import jweb.JWebTest;
import jweb.MockRequest;
import jweb.Request;
import jweb.Template;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

class PageRegistryParamsTest {

    public static class UserPage implements Template {
        private String id;
        @Override public void beforeRender(Request request) { id = request.param("id"); }
        @Override public Element render() {
            return div(p("user " + id), p("via accessor " + pathParam("id")), p("all " + params()));
        }
    }

    public static class PostPage implements Template {
        @Override public Element render() { return p("post " + pathParam("slug") + " in " + pathParam("category")); }
    }

    public static class FilesPage implements Template {
        @Override public Element render() { return p("files"); }
    }

    public static class ExactUsersPage implements Template {
        @Override public Element render() { return p("users index"); }
    }

    @Test
    void patternRoutesMatchAndCaptureParams() {
        PageRegistry registry = new PageRegistry();
        registry.register("/users", ExactUsersPage.class,
                          "/users/:id", UserPage.class,
                          "/posts/:category/:slug", PostPage.class,
                          "/files/*", FilesPage.class);

        assertEquals(Map.of(), registry.match("/users").orElseThrow().params());
        assertEquals(Map.of("id", "42"), registry.match("/users/42").orElseThrow().params());
        assertEquals(Map.of("id", "42"), registry.match("/users/42/").orElseThrow().params(), "trailing slash tolerated");
        assertEquals(Map.of("category", "java", "slug", "records"),
            registry.match("/posts/java/records").orElseThrow().params());
        assertTrue(registry.match("/files/a/b/c.png").isPresent());
        assertTrue(registry.match("/users/42/posts").isEmpty());
        assertTrue(registry.match("/nope").isEmpty());
    }

    @Test
    void exactRoutesWinOverPatterns() {
        PageRegistry registry = new PageRegistry();
        registry.register("/users/:id", UserPage.class, "/users/new", ExactUsersPage.class);
        assertSame(ExactUsersPage.class, registry.match("/users/new").orElseThrow().route().pageSupplier().get().getClass());
        assertSame(UserPage.class, registry.match("/users/7").orElseThrow().route().pageSupplier().get().getClass());
    }

    @Test
    void findByPathStaysExactOnly() {
        PageRegistry registry = new PageRegistry();
        registry.register("/users/:id", UserPage.class);
        assertEquals(Optional.empty(), registry.findByPath("/users/42"));
        assertTrue(registry.match("/users/42").isPresent(), "match() is the dispatch lookup");
    }

    @Test
    void patternRoutesGetANeutralTitle() {
        PageRegistry registry = new PageRegistry();
        registry.register("/users/:id", UserPage.class, "/docs/*", FilesPage.class);
        assertEquals("Page", registry.getRoutes().get(0).title());
        assertEquals("Page", registry.getRoutes().get(1).title());
    }

    @Test
    void pageReadsParamsInBeforeRenderAndThroughTheAccessor() {
        JWeb app = JWeb.create().pages("/users/:id", UserPage.class);
        var result = JWebTest.test(app, MockRequest.get("/users/42"));
        assertEquals(200, result.getStatus());
        assertTrue(result.bodyContains("user 42"), result.getBody());
        assertTrue(result.bodyContains("via accessor 42"), result.getBody());
        assertTrue(result.bodyContains("all {id=42}"), result.getBody());
    }

    public static class TestLayout implements Template {
        private final Element content;
        public TestLayout(Element content) { this.content = content; }
        @Override public Element render() { return html(body(content)); }
    }

    @Test
    void layoutAppliesToPatternRoutesRegisteredBeforeIt() {
        JWeb app = JWeb.create().pages("/users/:id", UserPage.class).layout(TestLayout.class);
        assertEquals(TestLayout.class,
            app.getPageRegistry().match("/users/1").orElseThrow().route().layoutClass());
    }

    // ==================== plain metadata ====================

    @Test
    @SuppressWarnings("deprecation")
    void metadataIsPlainAndTheOptionalFormStillDelegates() {
        Template plain = new Template() {
            @Override public Element render() { return p("x"); }
            @Override public String pageTitle() { return "Plain"; }
            @Override public String description() { return "A page"; }
        };
        assertEquals("Plain", plain.pageTitle());
        assertEquals("A page", plain.description());

        Template legacy = new Template() {
            @Override public Element render() { return p("x"); }
            @Override public Optional<String> metaDescription() { return Optional.of("Old style"); }
        };
        assertEquals("Old style", legacy.description(), "an Optional override still feeds description()");
        assertNull(legacy.pageTitle());
        assertTrue(legacy.cacheable());
    }
}
