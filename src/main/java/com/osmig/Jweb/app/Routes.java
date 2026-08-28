package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import com.osmig.Jweb.framework.openapi.OpenApi;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.security.Csrf;
import com.osmig.Jweb.framework.server.Response;
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
import org.springframework.stereotype.Component;

/**
 * Application routes - page routing and structure only.
 * Business logic lives in the api/ package.
 */
@Component
public class Routes implements JWebRoutes {

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
