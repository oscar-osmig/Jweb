package com.osmig.Jweb.app;

import jweb.JWeb;
import jweb.JWebRoutes;
import jweb.OpenApi;
import com.osmig.Jweb.framework.routing.RouteHandler;
import jweb.Csrf;
import jweb.Response;
import com.osmig.Jweb.app.api.AdminApi;
import com.osmig.Jweb.app.api.ContactApi;
import com.osmig.Jweb.app.api.ExampleApi;
import com.osmig.Jweb.app.forms.ContactStatus;
import com.osmig.Jweb.app.layout.Layout;
import com.osmig.Jweb.app.pages.HomePage;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
import com.osmig.Jweb.app.pages.DemoStreamingPage;
import com.osmig.Jweb.app.pages.admin.AdminLoginPage;
import com.osmig.Jweb.app.pages.admin.AdminMessagesPage;
import com.osmig.Jweb.app.docs.DocsPage;
import com.osmig.Jweb.app.docs.DocContent;
import com.osmig.Jweb.app.docs.DocsTell;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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
    private static final MediaType MARKDOWN =
        new MediaType("text", "markdown", StandardCharsets.UTF_8);

    /**
     * The unknown-topic reply. Stays text/plain and inline — an error listing
     * the valid ids is meant to be read in the tab, not downloaded.
     */
    private static final MediaType PLAIN_TEXT =
        new MediaType("text", "plain", StandardCharsets.UTF_8);

    private final AdminApi adminApi;
    private final com.osmig.Jweb.app.api.MessageStore messageStore;

    public Routes(AdminApi adminApi, com.osmig.Jweb.app.api.MessageStore messageStore) {
        this.adminApi = adminApi;
        this.messageStore = messageStore;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public void configure(JWeb app) {
        // Production baseline: security headers, request ids, compression
        app.use(com.osmig.Jweb.framework.middleware.Middlewares.recommended());

        // The contact form writes to the message store — cap per-IP submissions
        app.use("/contact/submit",
            com.osmig.Jweb.framework.middleware.Middlewares.rateLimit(5, 60_000));

        // Page routes
        app.layout(Layout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class
           );

        // Contact page needs the request to issue a session-bound CSRF token
        app.get("/contact", ctx -> new Layout("Contact - JWeb",
            new ContactPage(Csrf.getOrCreateToken(ctx)).render()
        ).render());

        // Contact form target — returns a status fragment that the runtime
        // swaps into #form-status (works without JS as a plain POST too)
        app.post("/contact/submit", (RouteHandler) ctx -> {
            if (!Csrf.isValid(ctx)) {
                return ContactStatus.error("Your session expired — reload the page and try again.");
            }
            String name = ctx.formParam("name");
            String email = ctx.formParam("email");
            String message = ctx.formParam("message");
            if (isBlank(name) || isBlank(email) || isBlank(message)) {
                return ContactStatus.error("All fields are required.");
            }
            if (name.length() > 200 || email.length() > 320 || message.length() > 5_000) {
                return ContactStatus.error("Message is too long.");
            }
            messageStore.save(name.trim(), email.trim(), message.trim());
            return ContactStatus.success("Message sent — we'll get back to you soon!");
        });

        // Docs page needs request access for query params
        app.get("/docs", ctx -> new Layout("Documentation - JWeb",
            new DocsPage(ctx.query("section")).render()
        ).render());

        // Docs content endpoint for client-side navigation (returns only content)
        app.get("/docs/content", ctx -> DocContent.get(ctx.query("section")));

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
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(PLAIN_TEXT)
                    .body(known.toString());
            }

            return Response.ok()
                .contentType(MARKDOWN)
                // Opening the URL in a browser saves a .md file rather than
                // rendering a wall of text. Only browsers honour this — curl and
                // anything fetching over HTTP still just get the body.
                .header("Content-Disposition",
                        "attachment; filename=\"" + DocsTell.filename(topic) + "\"")
                .header("X-JWeb-Version", DocsTell.version())
                // Regenerated only on deploy, and the body is large — let clients
                // and any proxy in front of us hold it for an hour.
                .header("Cache-Control", "public, max-age=3600")
                .body(body);
        });

        // Playground: user code runs through SandboxDsl's whitelist interpreter
        // only — nothing is compiled or reflected, and output uses the normal
        // escaping pipeline. The render POST is stateless (no CSRF surface).
        app.get("/sandbox", ctx -> new Layout("Sandbox - JWeb",
            new com.osmig.Jweb.app.sandbox.SandboxPage(ctx.query("file")).render()
        ).render());

        app.post("/sandbox/render", (RouteHandler) ctx ->
            com.osmig.Jweb.app.sandbox.SandboxPanes.renderFragment(
                ctx.formParam("file"), ctx.formParam("code")));

        // Starter sources for the client-side file switcher (static constants)
        app.get("/sandbox/source", ctx ->
            com.osmig.Jweb.app.sandbox.SandboxFiles.byId(ctx.query("file")).source());

        // Streaming SSR demo: the shell flushes instantly, both blocks
        // stream in as their (deliberately slow) data resolves
        app.get("/demo/streaming", ctx -> com.osmig.Jweb.framework.async.Streamed.of(
            () -> new Layout("Streaming Demo", DemoStreamingPage.content()).render()));

        // Admin login page
        app.get("/only-admin/log/in", ctx -> {
            if (adminApi.isAuthenticated(ctx)) {
                return Response.redirect("/only-admin/messages");
            }
            return Response.html(new Layout("Admin Login",
                new AdminLoginPage(Csrf.getOrCreateToken(ctx)).render()
            ).render());
        });

        // Admin login handler
        app.post("/only-admin/log/in", (RouteHandler) ctx -> {
            String error;
            if (!Csrf.isValid(ctx)) {
                error = "Your session expired — please try again.";
            } else if (adminApi.login(ctx, ctx.formParam("email"), ctx.formParam("token"))) {
                return Response.redirect("/only-admin/messages");
            } else {
                error = adminApi.isConfigured()
                    ? "Invalid email or token"
                    : "Admin login is not configured — set JWEB_ADMIN_TOKEN and JWEB_ADMIN_EMAIL.";
            }
            return Response.html(new Layout("Admin Login",
                new AdminLoginPage(error, Csrf.getOrCreateToken(ctx)).render()
            ).render());
        });

        // Admin messages page
        app.get("/only-admin/messages", ctx -> {
            if (!adminApi.isAuthenticated(ctx)) {
                return Response.redirect("/only-admin/log/in");
            }
            return Response.html(new Layout("Messages - Admin",
                new AdminMessagesPage(adminApi.getMessages(), Csrf.getOrCreateToken(ctx)).render()
            ).render());
        });

        // Admin logout — POST with CSRF token so a cross-site link can't trigger it
        app.post("/only-admin/logout", (RouteHandler) ctx -> {
            if (Csrf.isValid(ctx)) {
                adminApi.logout(ctx);
            }
            return Response.redirect("/");
        });

        // API documentation
        OpenApi.create()
            .title("JWeb Example API")
            .version("1.0.0")
            .description("Example REST API built with JWeb")
            .addApi(ExampleApi.class)
            .addApi(ContactApi.class)
            .mount(app, "/api");
    }
}
