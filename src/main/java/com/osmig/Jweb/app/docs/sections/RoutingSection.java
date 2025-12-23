package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.routing.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingSection {
    private RoutingSection() {}

    public static Element render() {
        return section(
            docTitle("Routing"),
            para("JWeb routing maps URLs to handlers. Handlers return Elements " +
                 "that are rendered as HTML, or Response objects for JSON/redirects."),

            docSubtitle("Overview"),
            para("Configure routes in a class implementing JWebRoutes. " +
                 "Routes support path parameters, query strings, and all HTTP methods."),
            codeBlock("""
                    @Component
                    public class Routes implements JWebRoutes {
                        public void configure(JWeb app) {
                            // Page routes
                            app.pages("/", HomePage.class);
                    
                            // API routes
                            app.get("/api/users", () -> Response.json(users));
                            app.post("/api/users", req -> createUser(req));
                        }
                    }"""),

            RoutingBasics.render(),
            RoutingParams.render(),
            RoutingMethods.render(),
            RoutingResponses.render(),
            RoutingMiddleware.render()
        );
    }
}
