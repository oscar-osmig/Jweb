package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import com.osmig.Jweb.app.layout.Layout;
import com.osmig.Jweb.app.pages.HomePage;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
import com.osmig.Jweb.app.docs.DocsPage;
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
    }
}
