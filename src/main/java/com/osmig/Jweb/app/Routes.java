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
 * Application routes.
 */
@Component
public class Routes implements JWebRoutes {

    @Override
    public void configure(JWeb app) {
        app.get("/", ctx -> new Layout("JWeb - Light Java Web Framework",
            new HomePage().render()
        ).render());

        app.get("/docs", ctx -> {
            String section = ctx.query("section");
            return new Layout("Documentation - JWeb",
                new DocsPage(section).render()
            ).render();
        });

        app.get("/about", ctx -> new Layout("About - JWeb",
            new AboutPage().render()
        ).render());

        app.get("/contact", ctx -> new Layout("Contact - JWeb",
            new ContactPage().render()
        ).render());
    }
}
