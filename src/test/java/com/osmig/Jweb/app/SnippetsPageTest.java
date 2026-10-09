package com.osmig.Jweb.app;

import com.osmig.Jweb.app.forms.SnippetSubmission;
import com.osmig.Jweb.app.pages.SnippetCard;
import com.osmig.Jweb.app.pages.SnippetsPage;
import com.osmig.Jweb.app.pages.admin.AdminSnippetsPage;
import com.osmig.Jweb.app.sandbox.SandboxPage;
import com.osmig.Jweb.framework.server.CurrentRequest;
import jweb.CsrfToken;
import jweb.Doc;
import jweb.Form;
import jweb.MockRequest;
import jweb.Mongo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The snippets feature's pages: a card is the code, the author and the
 * rendered output; the gallery only shows what was approved; the Sandbox
 * carries the "Add snippet" form; the admin queue carries the approve/reject
 * forms; and every string a visitor typed goes through the escaping pipeline.
 */
class SnippetsPageTest {

    private static Doc snippet(String title, String code) {
        return Doc.of("snippets").id(Mongo.newId())
            .set("title", title)
            .set("author", "Ada")
            .set("code", code)
            .set("status", "approved")
            .set("createdAt", new Date());
    }


    @Test
    void aCardIsTheCodeTheAuthorAndThePreview() {
        String html = SnippetCard.render(snippet("Gradient card",
            "div(style().padding(rem(1)), h2(\"Hi there\"))")).toHtml();

        assertTrue(html.contains("class=\"snip-card\""), html);
        assertTrue(html.contains("div(style().padding(rem(1)), h2("), "the code pane shows the source");
        assertTrue(html.contains("Gradient card"), html);
        assertTrue(html.contains("Ada"), html);
        assertTrue(html.contains("<h2>Hi there</h2>"), "the preview pane renders the snippet's output");
        assertTrue(html.contains("class=\"snip-copy\""), html);
    }

    @Test
    void theGalleryOnlyShowsPublishedSnippets() {
        SnippetsPage page = new SnippetsPage(List.of(snippet("One", "p(\"one\")")));
        String html = page.render().toHtml();

        assertTrue(html.contains("<p>one</p>"), "the published snippet renders");
        assertTrue(html.contains("1 snippet published"), html);
        assertFalse(html.contains("<form"), "the gallery takes no input — submissions come from the Sandbox");
        assertTrue(html.contains("href=\"/sandbox\""), "it points at the Sandbox");
        assertTrue(page.scripts().orElseThrow().build().contains("__snippetsInit"), "the copy script is attached");
        assertEquals("Snippets - JWeb", page.pageTitle());

        String empty = new SnippetsPage(List.of()).render().toHtml();
        assertTrue(empty.contains("No snippets yet"), empty);
    }

    @Test
    void theSandboxCarriesTheAddSnippetForm() {
        // The page reads the CSRF token from the request in flight
        CurrentRequest.set(MockRequest.get("/sandbox").build());
        try {
            SandboxPage page = new SandboxPage("home");
            String html = page.render().toHtml();

            assertTrue(html.contains("id=\"sandbox-add-snippet\""), "the chip on a DSL file");
            assertTrue(html.contains("id=\"snippet-panel\""), html);
            assertTrue(html.contains("action=\"/snippets/submit\""), html);
            assertTrue(html.contains("data-swap-post=\"/snippets/submit\""), html);
            assertTrue(html.contains("data-swap-target=\"#snippet-status\""), html);
            assertTrue(html.contains("name=\"code\"") && html.contains("id=\"snippet-code\""), "the code rides hidden");
            assertTrue(html.contains("name=\"title\"") && html.contains("name=\"author\""), html);
            assertTrue(html.contains("name=\"" + CsrfToken.TOKEN_PARAM_NAME + "\""), "the form carries the CSRF token");
            assertTrue(page.scripts().orElseThrow().build().contains("__sandboxInit"), "the editor script is attached");
            assertEquals("Sandbox - JWeb", page.pageTitle());

            String pom = new SandboxPage("pom").render().toHtml();
            assertFalse(pom.contains("id=\"sandbox-add-snippet\""), "no chip on the static files");
        } finally {
            CurrentRequest.clear();
        }
    }

    @Test
    void aSnippetThatDoesNotCompileShowsTheMessageNotAStackTrace() {
        String html = SnippetCard.render(snippet("Broken", "div(nope(")).toHtml();
        assertTrue(html.contains("class=\"snip-error\""), html);
        assertFalse(html.contains("Exception"), html);
        assertNotNull(SnippetCard.compileError("div(nope("));
        assertNull(SnippetCard.compileError("p(\"fine\")"));
    }

    @Test
    void everythingAVisitorTypedIsEscaped() {
        String html = SnippetCard.render(
            snippet("<b>bold</b>", "div(\"<script>alert(1)</script>\")")
                .set("author", "<img src=x onerror=alert(1)>")).toHtml();
        assertFalse(html.contains("<script>"), html);
        assertFalse(html.contains("<img"), html);
        assertFalse(html.contains("<b>bold</b>"), html);
    }

    @Test
    void theAdminQueueCarriesApproveAndRejectForms() {
        Doc pending = snippet("Pending one", "p(\"x\")").set("status", "pending");
        Doc live = snippet("Live one", "p(\"y\")");

        CurrentRequest.set(MockRequest.get("/only-admin/snippets").build());
        String html;
        try {
            html = new AdminSnippetsPage(List.of(pending), List.of(live), "Snippet published.")
                .render().toHtml();
        } finally {
            CurrentRequest.clear();
        }

        assertTrue(html.contains("action=\"/only-admin/snippets/" + pending.getId() + "/approve\""), html);
        assertTrue(html.contains("action=\"/only-admin/snippets/" + pending.getId() + "/delete\""), html);
        assertTrue(html.contains("action=\"/only-admin/snippets/" + live.getId() + "/delete\""), html);
        assertTrue(html.contains(">Approve</button>") && html.contains(">Reject</button>")
            && html.contains(">Unpublish</button>"), html);
        assertTrue(html.contains("name=\"" + CsrfToken.TOKEN_PARAM_NAME + "\""), "every action carries the CSRF token");
        assertTrue(html.contains("Snippet published."), "the flash notice");
        assertTrue(html.contains("1 awaiting review"), html);
        assertTrue(html.contains("<p>x</p>") && html.contains("<p>y</p>"), "both lists render previews");
        assertTrue(html.contains("href=\"/only-admin/messages\""), "the admin tabs");
    }

    @Test
    void submissionValidationIsTheRecordsAnnotations() {
        assertTrue(Form.validate(SnippetSubmission.class, Map.of()).hasErrors("title"));
        assertTrue(Form.validate(SnippetSubmission.class, Map.of("title", "x", "code", "p()")).hasErrors("author"));
        assertTrue(Form.validate(SnippetSubmission.class, Map.of("title", "x", "author", "Ada")).hasErrors("code"));
        assertTrue(Form.validate(SnippetSubmission.class,
            Map.of("title", "x", "author", "Ada", "code", "p(\"hi\")")).isValid());
        assertTrue(Form.validate(SnippetSubmission.class,
            Map.of("title", "x", "author", "Ada", "code", "p".repeat(10_001))).hasErrors("code"));
        assertTrue(Form.validate(SnippetSubmission.class,
            Map.of("title", "x".repeat(81), "author", "Ada", "code", "p(\"hi\")")).hasErrors("title"));
    }
}
