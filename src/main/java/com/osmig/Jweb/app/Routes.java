package com.osmig.Jweb.app;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.JWebRoutes;
import com.osmig.Jweb.app.layouts.MainLayout;
import com.osmig.Jweb.app.pages.AboutPage;
import com.osmig.Jweb.app.pages.ContactPage;
import com.osmig.Jweb.app.pages.DemoPage;
import com.osmig.Jweb.app.pages.HomePage;
import org.springframework.stereotype.Component;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Application Routes
 *
 * Define all your routes here.
 */
@Component
public class Routes implements JWebRoutes {

    @Override
    public void configure(JWeb app) {
        // Page routes
        app.get("/", HomePage::new)
           .get("/about", AboutPage::new)
           .get("/contact", ContactPage::new)
           .get("/demo", DemoPage::new);

        // Dynamic route with path parameter
        app.get("/hello/:name", req -> {
            String name = req.param("name");
            return new MainLayout("Hello " + name,
                div(
                    h1("Hello, " + name + "!"),
                    p("Welcome to JWeb."),
                    a("/", "← Back Home")
                )
            );
        });

        // Inline route (no separate page class needed)
        app.get("/inline-demo", () ->
            div(class_("container"),
                h1("Inline Demo"),
                p("This page is defined inline, no template needed!"),
                a("/", "← Back")
            )
        );
    }
}
