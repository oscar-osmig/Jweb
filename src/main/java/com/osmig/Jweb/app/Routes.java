package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import com.osmig.Jweb.framework.openapi.OpenApi;
import com.osmig.Jweb.framework.routing.RouteHandler;
import com.osmig.Jweb.framework.server.Response;
import com.osmig.Jweb.app.api.AdminApi;
import com.osmig.Jweb.app.api.ContactApi;
import com.osmig.Jweb.app.api.ExampleApi;
import com.osmig.Jweb.app.layout.Layout;
import com.osmig.Jweb.app.pages.HomePage;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
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

    public Routes(AdminApi adminApi) {
        this.adminApi = adminApi;
    }

    @Override
    public void configure(JWeb app) {
        // Page routes
        app.layout(Layout.class)
           .pages(
               "/", HomePage.class,
               "/about", AboutPage.class,
               "/contact", ContactPage.class
           );

        // Docs page needs request access for query params
        app.get("/docs", ctx -> new Layout("Documentation - JWeb",
            new DocsPage(ctx.query("section")).render()
        ).render());

        // Docs content endpoint for client-side navigation (returns only content)
        app.get("/docs/content", ctx -> DocContent.get(ctx.query("section")));

        // Admin login page
        app.get("/only-admin/log/in", ctx -> {
            if (adminApi.isAuthenticated(ctx)) {
                return Response.redirect("/only-admin/messages");
            }
            return Response.html(new Layout("Admin Login",
                new AdminLoginPage().render()
            ).render());
        });

        // Admin login handler
        app.post("/only-admin/log/in", (RouteHandler) ctx -> {
            if (adminApi.login(ctx, ctx.formParam("email"), ctx.formParam("token"))) {
                return Response.redirect("/only-admin/messages");
            }
            return Response.html(new Layout("Admin Login",
                new AdminLoginPage("Invalid email or token").render()
            ).render());
        });

        // Admin messages page
        app.get("/only-admin/messages", ctx -> {
            if (!adminApi.isAuthenticated(ctx)) {
                return Response.redirect("/only-admin/log/in");
            }
            return Response.html(new Layout("Messages - Admin",
                new AdminMessagesPage(adminApi.getMessages()).render()
            ).render());
        });

        // Admin logout
        app.get("/only-admin/logout", ctx -> {
            adminApi.logout(ctx);
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
