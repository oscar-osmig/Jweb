package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.state.StateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static jweb.Css.*;
import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Where a render's collected CSS lands: in {@code <head>} for a document, in
 * front of the markup for a swap fragment (a {@code <style>} inserted through
 * {@code innerHTML} does apply).
 */
class PageStyleDeliveryTest {

    @Test
    void aDocumentGetsItsStylesheetAtTheEndOfHead() {
        assertEquals("<html><head><title>x</title><style>.a{}</style></head><body>b</body></html>",
            JWebController.injectPageStyles(
                "<html><head><title>x</title></head><body>b</body></html>", "<style>.a{}</style>"));
    }

    @Test
    void aDocumentWithNoHeadGetsItAtTheEndOfBody() {
        assertEquals("<body>b<style>.a{}</style></body>",
            JWebController.injectPageStyles("<body>b</body>", "<style>.a{}</style>"));
    }

    @Test
    void aFragmentCarriesItsStylesheetInFront() {
        assertEquals("<style>.a{}</style><div>x</div>",
            JWebController.injectPageStyles("<div>x</div>", "<style>.a{}</style>"));
    }

    @Test
    void injectingNothingLeavesTheHtmlAlone() {
        assertEquals("<div>x</div>", JWebController.injectPageStyles("<div>x</div>", ""));
    }

    @AfterEach
    void cleanup() {
        StateManager.clearContext();
    }

    /**
     * {@code Response.html(new Layout(...))} hands back a built response
     * instead of an Element, so it never reaches the Element injection path.
     * It still has to carry the page's CSS.
     */
    @Test
    void anAlreadyBuiltHtmlResponseStillGetsThePagesCss() {
        var context = StateManager.createContext();
        var page = jweb.Response.html(html(head(title("x")), body(
            div(style().hover(style().color(blue))))));

        var withCss = JWebController.withPageStyles(page, context);
        assertTrue(withCss.getBody().contains("<style"), withCss.getBody());
        assertTrue(withCss.getBody().indexOf("<style") < withCss.getBody().indexOf("</head>"),
            withCss.getBody());
        assertEquals(page.getStatusCode(), withCss.getStatusCode());
    }

    @Test
    void aJsonResponseIsLeftAlone() {
        var context = StateManager.createContext();
        div(style().hover(style().color(blue))).toHtml();
        var json = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"a\":1}");

        assertSame(json, JWebController.withPageStyles(json, context));
    }
}
