package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.middleware.MiddlewareStack;
import com.osmig.Jweb.framework.middleware.Middlewares;
import com.osmig.Jweb.framework.server.JWebController;
import com.osmig.Jweb.framework.server.Request;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CspNonceTest {

    @AfterEach
    void cleanup() {
        CspNonce.clear();
    }

    @Test
    void scriptElementsGetTheActiveNonce() {
        String nonce = CspNonce.begin();
        String html = VElement.of("script", List.of(new VText("console.log(1)"))).toHtml();
        assertTrue(html.contains("nonce=\"" + nonce + "\""), html);
    }

    @Test
    void nonScriptElementsAreUntouched() {
        CspNonce.begin();
        String html = VElement.of("div", List.of(new VText("hi"))).toHtml();
        assertFalse(html.contains("nonce="), html);
    }

    @Test
    void noNonceStampedOutsideARequest() {
        String html = VElement.of("script", List.of(new VText("console.log(1)"))).toHtml();
        assertFalse(html.contains("nonce="), html);
    }

    @Test
    void explicitNonceAttributeWins() {
        CspNonce.begin();
        String html = VElement.of("script", Map.of("nonce", "fixed"), List.of()).toHtml();
        assertTrue(html.contains("nonce=\"fixed\""), html);
        assertEquals(html.indexOf("nonce="), html.lastIndexOf("nonce="), html);
    }

    @Test
    void securityHeadersUseNonceBasedCspWhenActive() throws Exception {
        String nonce = CspNonce.begin();
        Request req = new Request(new MockHttpServletRequest("GET", "/"));
        new MiddlewareStack().use(Middlewares.securityHeaders()).execute(req, () -> "ok");

        String csp = req.responseHeaders().get("Content-Security-Policy");
        assertTrue(csp.contains("'nonce-" + nonce + "'"), csp);
        assertFalse(csp.contains("script-src") && csp.contains("script-src 'self' 'unsafe-inline'"), csp);
        assertTrue(csp.contains("frame-ancestors 'none'"), csp);
        assertEquals("0", req.responseHeaders().get("X-XSS-Protection"));
        assertNotNull(req.responseHeaders().get("Permissions-Policy"));
    }

    @Test
    void securityHeadersFallBackWithoutANonce() throws Exception {
        Request req = new Request(new MockHttpServletRequest("GET", "/"));
        new MiddlewareStack().use(Middlewares.securityHeaders()).execute(req, () -> "ok");

        String csp = req.responseHeaders().get("Content-Security-Policy");
        assertFalse(csp.contains("nonce-"), csp);
    }

    @Test
    void streamChunkSwapScriptCarriesTheNonce() {
        String nonce = CspNonce.begin();
        String chunk = JWebController.streamChunk("ph-1", "<p>done</p>");
        assertTrue(chunk.contains("<script nonce=\"" + nonce + "\">"), chunk);
    }

    @Test
    void noncesAreUniquePerRequest() {
        String first = CspNonce.begin();
        String second = CspNonce.begin();
        assertNotEquals(first, second);
    }
}
