package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
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
            typedRoutes(),
            RoutingMethods.render(),
            RoutingResponses.render(),
            RoutingMiddleware.render()
        );
    }

    private static Element typedRoutes() {
        return section(
            docSubtitle("Typed Routes & Query Params"),
            para("Declare a route's path and parameter types once — registration and " +
                 "every link to it are then compile-time checked. Handlers receive " +
                 "already-parsed values; bad input returns 400, not 500."),
            codeBlock("""
                    static final TypedRoute.Path1<Long> USER =
                        TypedRoute.path("/users/:id", Long.class);

                    app.get(USER, (req, id) -> userPage(id));   // id is a Long
                    a(USER.url(42L), text("Profile"))           // "/users/42"

                    // Query params: parsed, defaulted, no null checks
                    static final Query<Integer> PAGE = Query.of("page", Integer.class).orElse(1);
                    app.get("/products", req -> productList(PAGE.from(req)));"""),
            docTip("Supported param types: String, Integer, Long, Double, Boolean, UUID. " +
                   "Two-param routes use TypedRoute.path(pattern, A.class, B.class).")
        );
    }
}
