package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import com.osmig.Jweb.framework.openapi.OpenApi;
import com.osmig.Jweb.app.api.ExampleApi;
import com.osmig.Jweb.app.layout.Layout;
import com.osmig.Jweb.app.pages.HomePage;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
import com.osmig.Jweb.app.docs.DocsPage;
import com.osmig.Jweb.app.docs.DocContent;
import org.springframework.stereotype.Component;

/**
 * Application page routes.
 *
 * APIs are auto-discovered from @Api @Component classes.
 * Configure API base path in application.yaml: jweb.api.base=/api/v1
 */
@Component
public class Routes implements JWebRoutes {

    @Override
    public void configure(JWeb app) {
        // Page routes - simple map-style syntax
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

        // API documentation
        OpenApi.create()
            .title("JWeb Example API")
            .version("1.0.0")
            .description("Example REST API built with JWeb")
            .addApi(ExampleApi.class)
            .mount(app, "/api");
    }
}
