package com.osmig.Jweb.framework;

import com.osmig.Jweb.app.docs.DocContent;
import com.osmig.Jweb.framework.server.ErrorPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks in the security/correctness hardening: the HTML5 doctype is emitted,
 * error responses do not leak internals by default, and the docs content
 * endpoint tolerates a missing section.
 */
class HardeningTest {

    @Test
    void rootHtmlEmitsDoctype() {
        String html = com.osmig.Jweb.framework.elements.Elements.html().toHtml();
        assertTrue(html.startsWith("<!DOCTYPE html>"),
                "root <html> must render the HTML5 doctype so pages avoid quirks mode");
    }

    @Test
    void errorPageHidesInternalsByDefault() {
        // Default (no jweb.errors.detail / JWEB_ERRORS_DETAIL) is production-safe.
        assertFalse(ErrorPage.showDetails(), "error detail must be off by default");

        String html = ErrorPage.render(500, "Server Error",
                new RuntimeException("secret internal boom")).toHtml();

        assertFalse(html.contains("secret internal boom"),
                "exception message must not leak to the client by default");
        assertFalse(html.contains("Stack Trace"),
                "stack trace must not be shown to the client by default");
        assertTrue(html.contains("Something went wrong"),
                "a generic message should be shown instead");
    }

    @Test
    void docsContentToleratesMissingSection() {
        assertNotNull(DocContent.get(null), "a missing ?section= must fall back, not throw");
        assertNotNull(DocContent.get("setup"));
        assertNotNull(DocContent.get("does-not-exist"));
    }
}
