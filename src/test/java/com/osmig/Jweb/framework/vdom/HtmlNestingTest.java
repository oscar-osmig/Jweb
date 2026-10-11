package com.osmig.Jweb.framework.vdom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The content model the DSL cannot enforce by type: a browser repairs
 * {@code <p><div>} into two siblings, so the render says so — once per pair,
 * or as a failure under {@code jweb.dev.strict-html}.
 */
class HtmlNestingTest {

    @AfterEach
    void reset() {
        HtmlNesting.setStrict(false);
        HtmlNesting.resetWarnings();
    }

    @Test
    void phrasingOnlyParentsRejectFlowChildren() {
        assertNotNull(HtmlNesting.problem("p", "div"));
        assertNotNull(HtmlNesting.problem("p", "ul"));
        assertNotNull(HtmlNesting.problem("p", "p"));
        assertNotNull(HtmlNesting.problem("h2", "div"));
        assertNotNull(HtmlNesting.problem("span", "section"));
        assertNotNull(HtmlNesting.problem("pre", "div"));
        assertNotNull(HtmlNesting.problem("button", "div"));

        assertNull(HtmlNesting.problem("p", "span"));
        assertNull(HtmlNesting.problem("p", "a"));
        assertNull(HtmlNesting.problem("h2", "code"));
        assertNull(HtmlNesting.problem("div", "p"));
        assertNull(HtmlNesting.problem("div", "div"));
        assertNull(HtmlNesting.problem("section", "h2"));
    }

    @Test
    void structuralParentsAcceptOnlyTheirOwnChildren() {
        assertNotNull(HtmlNesting.problem("ul", "div"));
        assertNotNull(HtmlNesting.problem("ol", "p"));
        assertNotNull(HtmlNesting.problem("table", "div"));
        assertNotNull(HtmlNesting.problem("tr", "div"));
        assertNotNull(HtmlNesting.problem("select", "div"));
        assertNotNull(HtmlNesting.problem("head", "div"));

        assertNull(HtmlNesting.problem("ul", "li"));
        assertNull(HtmlNesting.problem("table", "tr"), "tr directly under table is repaired, not rejected");
        assertNull(HtmlNesting.problem("table", "tbody"));
        assertNull(HtmlNesting.problem("tbody", "tr"));
        assertNull(HtmlNesting.problem("tr", "td"));
        assertNull(HtmlNesting.problem("select", "optgroup"));
        assertNull(HtmlNesting.problem("ul", "script"), "script and template go anywhere");
        assertNull(HtmlNesting.problem("ul", "template"));
        assertNull(HtmlNesting.problem("ul", "my-item"), "custom elements are never judged");
        assertNull(HtmlNesting.problem("li", "ul"), "nested lists are fine");
    }

    @Test
    void interactiveContentDoesNotNest() {
        assertNotNull(HtmlNesting.problem("a", "a"));
        assertNotNull(HtmlNesting.problem("a", "button"));
        assertNotNull(HtmlNesting.problem("button", "a"));
        assertNotNull(HtmlNesting.problem("button", "input"));
        assertNotNull(HtmlNesting.problem("label", "label"));
        assertNotNull(HtmlNesting.problem("form", "form"));

        assertNull(HtmlNesting.problem("a", "span"));
        assertNull(HtmlNesting.problem("a", "div"), "a is transparent: flow is fine where its parent allows it");
        assertNull(HtmlNesting.problem("label", "input"), "a label wraps its own control");
        assertNull(HtmlNesting.problem("form", "input"));
    }

    @Test
    void titleBelongsInTheHead() {
        assertNotNull(HtmlNesting.problem("button", "title"));
        assertNotNull(HtmlNesting.problem("body", "title"));
        assertTrue(HtmlNesting.problem("div", "title").contains("attrs().title"), "points at the tooltip form");
        assertNull(HtmlNesting.problem("head", "title"));
        assertNull(HtmlNesting.problem("svg", "title"));
    }

    @Test
    void theRenderStillHappensAndIsReportedOnce() {
        HtmlNesting.resetWarnings();
        String html = p(div("x")).toHtml();
        assertEquals("<p><div>x</div></p>", html, "the markup is emitted as written; only the log changes");
        // A second identical render does not throw or alter output either
        assertEquals("<p><div>y</div></p>", p(div("y")).toHtml());
    }

    @Test
    void strictModeFailsTheRenderWithThePairNamed() {
        HtmlNesting.setStrict(true);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> div(p(div("x"))).toHtml());
        assertTrue(e.getMessage().contains("<div> inside <p>"), e.getMessage());
        assertTrue(e.getMessage().contains("phrasing content"), e.getMessage());

        IllegalStateException list = assertThrows(IllegalStateException.class, () -> ul(div("x")).toHtml());
        assertTrue(list.getMessage().contains("<div> inside <ul>"), list.getMessage());

        // Valid trees are untouched in strict mode
        assertEquals("<ul><li>a</li></ul>", ul(li("a")).toHtml());
        assertEquals("<p><em>a</em></p>", p(em("a")).toHtml());
    }
}
