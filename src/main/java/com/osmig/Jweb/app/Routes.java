package com.osmig.Jweb.app;

import jweb.Auth;
import jweb.JWeb;
import jweb.JWebRoutes;
import jweb.Middlewares;
import jweb.OpenApi;
import jweb.Form;
import jweb.Response;
import jweb.Session;
import jweb.api.Component;
import jweb.api.Range;
import com.osmig.Jweb.app.api.AdminApi;
import com.osmig.Jweb.app.api.AdminSnippetsApi;
import com.osmig.Jweb.app.api.ContactApi;
import com.osmig.Jweb.app.api.ExampleApi;
import com.osmig.Jweb.app.api.SnippetStore;
import com.osmig.Jweb.app.forms.AdminLogin;
import com.osmig.Jweb.app.forms.ContactForm;
import com.osmig.Jweb.app.forms.ContactStatus;
import com.osmig.Jweb.app.forms.SnippetSubmission;
import com.osmig.Jweb.app.layout.Layout;
import com.osmig.Jweb.app.pages.HomePage;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
import com.osmig.Jweb.app.pages.DemoStreamingPage;
import com.osmig.Jweb.app.pages.SnippetCard;
import com.osmig.Jweb.app.pages.SnippetsPage;
import com.osmig.Jweb.app.pages.ThreeDemoPage;
import com.osmig.Jweb.app.pages.admin.AdminLoginPage;
import com.osmig.Jweb.app.pages.admin.AdminMessagesPage;
import com.osmig.Jweb.app.pages.admin.AdminSnippetsPage;
import com.osmig.Jweb.app.docs.DocsPage;
import com.osmig.Jweb.app.docs.DocContent;
import com.osmig.Jweb.app.docs.DocsTell;

import java.util.Optional;

/**
 * Application routes - page routing and structure only.
 * Business logic lives in the api/ package.
 */
@Component
public class Routes implements JWebRoutes {

    /**
     * The documentation download. Charset is explicit because the docs contain
     * em dashes and arrows; paired with Content-Disposition so opening
     * /docs/tell in a browser saves a .md file.
     */
    private static final String MARKDOWN = "text/markdown; charset=UTF-8";

    /**
     * The unknown-topic reply. Stays text/plain and inline — an error listing
     * the valid ids is meant to be read in the tab, not downloaded.
     */
    private static final String PLAIN_TEXT = "text/plain; charset=UTF-8";

    /** The admin messages view: {@code ?order=oldest&limit=20}, enum by name, bounded. */
    public record MessagesView(Optional<AdminMessagesPage.Order> order,
                               @Range(min = 1, max = 500) Optional<Integer> limit) {}

    private final AdminApi adminApi;
    private final com.osmig.Jweb.app.api.MessageStore messageStore;
    private final SnippetStore snippets;

    public Routes(AdminApi adminApi, com.osmig.Jweb.app.api.MessageStore messageStore,
                  SnippetStore snippets) {
        this.adminApi = adminApi;
        this.messageStore = messageStore;
        this.snippets = snippets;
    }

    @Override
    public void configure(JWeb app) {
        // Production baseline: security headers, request ids, compression
        app.use(Middlewares.recommended());

        // The contact form writes to the message store — cap per-IP submissions
        app.use("/contact/submit",
            Middlewares.rateLimit(5, 60_000));

        // Page routes: every page keeps the default layout and its own
        // pageTitle()/styles()/scripts() hooks; a page whose constructor
        // needs the request is registered with a lambda
        app.layout(Layout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // Contact form target — returns a status fragment that the runtime
        // swaps into #form-status (works without JS as a plain POST too).
        // ContactForm's annotations are the whole validation: no hand-written
        // null checks, no length checks; the CSRF check is the recommended()
        // middleware's.
        app.post("/contact/submit", ctx -> {
            Form.Bound<ContactForm> submitted = Form.bind(ContactForm.class, ctx);
            if (!submitted.ok()) {
                return ContactStatus.error(submitted.errors().getAllMessages().get(0));
            }
            ContactForm contact = submitted.value();
            messageStore.save(contact.name().trim(), contact.email().trim(), contact.message().trim());
            return ContactStatus.success("Message sent — we'll get back to you soon!");
        });

        // ==================== Snippets ====================

        // Community snippets: the gallery shows what an admin approved. They
        // are sent in from the Sandbox's "Add snippet" form (the editor's
        // code plus a title and a name), rendered through the sandbox's
        // whitelist interpreter — never compiled. Submissions are capped per IP.
        app.use("/snippets/submit", Middlewares.rateLimit(5, 60_000));

        app.pages("/snippets", req -> new SnippetsPage(snippets.approved()));

        // Submission: the record's annotations validate the fields, the
        // interpreter validates the code, and the snippet lands in the queue.
        // Returns a status fragment the Sandbox swaps into its form panel.
        app.post("/snippets/submit", ctx -> {
            Form.Bound<SnippetSubmission> submitted = Form.bind(SnippetSubmission.class, ctx);
            if (!submitted.ok()) {
                return ContactStatus.error(submitted.errors().getAllMessages().get(0));
            }
            SnippetSubmission s = submitted.value();
            String problem = SnippetCard.compileError(s.code());
            if (problem != null) {
                return ContactStatus.error("The code does not compile — " + problem);
            }
            snippets.submit(s.title().trim(), s.author().trim(), s.code().strip());
            return ContactStatus.success(
                "Sent for review — once approved it will appear on /snippets.");
        });

        // Docs page needs request access for query params; ?v= selects the
        // docs version (defaults to latest)
        app.pages("/docs", req -> new DocsPage(req.query("section"), req.query("v")));

        // Docs content endpoint for client-side navigation (returns only content)
        app.get("/docs/content", ctx ->
            DocContent.get(ctx.query("section"), ctx.query("v")));

        // The whole documentation set as one markdown document, for an AI
        // assistant to pull in as grounding before writing JWeb code. Opening the
        // URL in a browser downloads it as a .md file; ?topic=<id> narrows it to
        // one document (still with the header) for clients that do not want the
        // full ~300KB.
        app.get("/docs/tell", ctx -> {
            String topic = ctx.query("topic");
            boolean whole = topic == null || topic.isBlank();
            String body = whole ? DocsTell.full() : DocsTell.topic(topic);

            if (body == null) {
                StringBuilder known = new StringBuilder(
                    "Unknown topic: " + topic + "\n\nValid topic ids:\n");
                for (DocsTell.Topic t : DocsTell.topics()) {
                    known.append("  ").append(t.id()).append(" — ").append(t.title()).append('\n');
                }
                known.append("\nOmit ?topic= for the whole documentation set.\n");
                return Response.status(404)
                    .contentType(PLAIN_TEXT)
                    .body(known.toString());
            }

            // no-cache, not max-age: a client may keep a copy but must revalidate
            // before using it, so a deploy takes effect on the next request
            // instead of up to an hour later. The ETag is what keeps that cheap —
            // an unchanged document answers 304 rather than re-sending ~300KB.
            String etag = DocsTell.etag(topic);
            String ifNoneMatch = ctx.header("If-None-Match");
            if (etag != null && ifNoneMatch != null && ifNoneMatch.contains(etag)) {
                return Response.status(304)
                    .header("ETag", etag)
                    .header("Cache-Control", "no-cache")
                    .build();
            }

            return Response.ok()
                .contentType(MARKDOWN)
                // Opening the URL in a browser saves a .md file rather than
                // rendering a wall of text. Only browsers honour this — curl and
                // anything fetching over HTTP still just get the body.
                .header("Content-Disposition",
                        "attachment; filename=\"" + DocsTell.filename(topic) + "\"")
                .header("X-JWeb-Version", DocsTell.version())
                .header("ETag", etag)
                .header("Cache-Control", "no-cache")
                .body(body);
        });

        // Playground: user code runs through SandboxDsl's whitelist interpreter
        // only — nothing is compiled or reflected, and output uses the normal
        // escaping pipeline. The render POST and the "Add snippet" form carry
        // the page's CSRF token (meta tag and hidden field) for the
        // recommended() middleware.
        app.pages("/sandbox", req -> new com.osmig.Jweb.app.sandbox.SandboxPage(req.query("file")));

        app.post("/sandbox/render", ctx ->
            com.osmig.Jweb.app.sandbox.SandboxPanes.renderFragment(
                ctx.formParam("file"), ctx.formParam("code")));

        // Starter sources for the client-side file switcher (static constants)
        app.get("/sandbox/source", ctx ->
            com.osmig.Jweb.app.sandbox.SandboxFiles.byId(ctx.query("file")).source());

        // Streaming SSR demo: the shell flushes instantly, both blocks
        // stream in as their (deliberately slow) data resolves
        app.get("/demo/streaming", ctx -> jweb.Streamed.of(
            () -> new Layout("Streaming Demo", DemoStreamingPage.content())));

        // Fragment for the demo's swap block — carries its own Actions-DSL
        // handler, whose definitions script executes on swap
        app.get("/demo/streaming/fragment", ctx -> DemoStreamingPage.fragment());

        // Three DSL demo: declarative 3D scenes, zero handwritten JavaScript
        app.get("/demo/three", ctx -> new Layout("3D Scenes - JWeb",
            ThreeDemoPage.content()));

        // Fragment target for the demo's clickable shapes (clickSwap)
        app.get("/demo/three/pick", ctx ->
            ThreeDemoPage.pickFragment(ctx.query("shape")));

        // ==================== Admin ====================

        // One guard covers everything under /only-admin: anonymous visitors
        // are sent to the login page, which the guard itself lets through.
        app.guard("/only-admin/**", Auth.requireLogin("/only-admin/log/in"));

        // Admin login page (a signed-in admin skips it); the sign-out notice
        // is a flash message — written by logout, read once here
        app.get("/only-admin/log/in", ctx -> {
            if (adminApi.isAuthenticated(ctx)) {
                return Response.redirect("/only-admin/messages");
            }
            String notice = Session.of(ctx).flash("notice");
            return new Layout(new AdminLoginPage(null, notice));
        });

        // Admin login handler: the form binds to the AdminLogin record; a
        // blank or malformed submit re-renders the form with its field errors
        app.post("/only-admin/log/in", ctx -> {
            Form.Bound<AdminLogin> submitted = Form.bind(AdminLogin.class, ctx);
            String error = null;
            if (submitted.ok()) {
                AdminLogin login = submitted.value();
                if (adminApi.login(ctx, login.email(), login.token())) {
                    return Response.redirect("/only-admin/messages");
                }
                error = adminApi.isConfigured()
                    ? "Invalid email or token"
                    : "Admin login is not configured — set JWEB_ADMIN_TOKEN and JWEB_ADMIN_EMAIL.";
            }
            return new Layout(new AdminLoginPage(error, submitted));
        });

        // Admin messages page: ?order=newest|oldest (enum by name) and
        // ?limit=n (1..500) bind to MessagesView — out of range is a 400
        app.get("/only-admin/messages", MessagesView.class, (ctx, view) -> {
            var order = view.order().orElse(AdminMessagesPage.Order.NEWEST);
            var messages = order.apply(adminApi.getMessages(), view.limit().orElse(Integer.MAX_VALUE));
            return new Layout(new AdminMessagesPage(messages, order));
        });

        // Snippet review: the queue and the published list, plus the one-shot
        // notice the last action left behind
        app.pages("/only-admin/snippets", req -> new AdminSnippetsPage(
            snippets.pending(), snippets.approved(), Session.of(req).flash("notice")));

        // Approve / reject / unpublish — POSTs carry the CSRF token (the
        // recommended() middleware checks it), then back to the queue
        app.post("/only-admin/snippets/:id/approve", ctx -> {
            Session.of(ctx).flash("notice", snippets.approve(ctx.param("id"))
                ? "Snippet published — it is live on /snippets."
                : "That snippet is no longer there.");
            return Response.redirect("/only-admin/snippets");
        });

        app.post("/only-admin/snippets/:id/delete", ctx -> {
            Session.of(ctx).flash("notice", snippets.delete(ctx.param("id"))
                ? "Snippet removed."
                : "That snippet is no longer there.");
            return Response.redirect("/only-admin/snippets");
        });

        // Admin logout — a POST, so a cross-site link can't trigger it
        app.post("/only-admin/logout", ctx -> {
            adminApi.logout(ctx);
            Session.of(ctx).flash("notice", "You have been signed out.");
            return Response.redirect("/only-admin/log/in");
        });

        // The review API takes the same credentials as the login form, sent
        // as headers so a script can drive the queue; an admin session works
        // too. Anything else is a 401 before the controller runs.
        app.guard("/api/v1/admin/**", req -> adminApi.authorize(req) ? null
            : Response.unauthorized("Admin credentials required: send the "
                + AdminApi.EMAIL_HEADER + " and " + AdminApi.TOKEN_HEADER + " headers"));

        // API documentation
        OpenApi.create()
            .title("JWeb Example API")
            .version("1.0.0")
            .description("Example REST API built with JWeb")
            .addApi(ExampleApi.class)
            .addApi(ContactApi.class)
            .addApi(AdminSnippetsApi.class)
            .mount(app, "/api");
    }
}
