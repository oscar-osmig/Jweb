package com.osmig.Jweb.framework.testing;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import org.junit.jupiter.api.Test;

import static com.osmig.Jweb.framework.elements.El.*;
import static org.junit.jupiter.api.Assertions.*;

class JWebTestPageRouteTest {

    public static class TestPage implements Template {
        @Override
        public Element render() {
            return div(h1("Test Page"), p("Hello from a page route"));
        }
    }

    public static class TestLayout implements Template {
        private final Element content;

        public TestLayout(Element content) {
            this.content = content;
        }

        @Override
        public Element render() {
            return html(body(content));
        }
    }

    @Test
    void pageRoutesAreTestable() {
        JWeb app = JWeb.create().pages("/test", TestPage.class);

        var result = JWebTest.test(app, MockRequest.get("/test"));

        assertEquals(200, result.getStatus());
        JWebTest.assertContains(result.getBody(), "Hello from a page route");
    }

    @Test
    void pageRoutesRejectNonGetMethods() {
        JWeb app = JWeb.create().pages("/test", TestPage.class);

        var result = JWebTest.test(app, MockRequest.post("/test"));

        assertEquals(405, result.getStatus());
    }

    @Test
    void routerMethodMismatchIs405NotFound404() {
        JWeb app = JWeb.create();
        app.get("/thing", req -> "get response");

        assertEquals(405, JWebTest.test(app, MockRequest.post("/thing")).getStatus());
        assertEquals(404, JWebTest.test(app, MockRequest.get("/missing")).getStatus());
    }

    @Test
    void layoutAppliesWhenSetAfterPages() {
        // layout(...) after pages(...) must still attach the layout
        JWeb app = JWeb.create()
            .pages("/test", TestPage.class)
            .layout(TestLayout.class);

        var route = app.getPageRegistry().findByPath("/test").orElseThrow();
        assertEquals(TestLayout.class, route.layoutClass());
    }
}
