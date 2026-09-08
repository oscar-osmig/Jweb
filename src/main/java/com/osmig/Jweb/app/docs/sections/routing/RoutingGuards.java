package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/** Guards: one check in front of every route under a prefix. */
public final class RoutingGuards {
    private RoutingGuards() {}

    public static Element render() {
        return section(
            h3Title("Guards"),
            para("A guard runs before every route under a path pattern — page routes, router " +
                 "routes and @REST controllers alike. It returns null to let the request " +
                 "through, or the response to send instead."),
            codeBlock("""
// Your own check: req.principal() is what Auth.login stored
app.guard("/admin/**", req -> req.principal() != null
    ? null
    : Response.redirect("/admin/login"));

// Sugar: redirect anonymous visitors, let the login page itself through
app.guard("/admin/**", Auth.requireLogin("/admin/login"));

// Any Middleware works as a guard — its own response (or 401/403) answers
app.guard("/admin/**", Auth.requireRole("admin"));
app.guard("/api/v1/admin/**", Jwt.protect());"""),
            docList(
                "Patterns are the app.use(...) ones: a prefix (/admin), /admin/** for the prefix and everything under it, or a glob with * inside one segment.",
                "Guards run after the middleware stack and before the handler, in registration order; the first non-null answer wins.",
                "The same guard covers @REST controllers under the prefix — middleware still does not."
            ),
            docTip("Guards decide who reaches a route. What a server event handler may do is decided " +
                   "when the page renders — render nothing for those who may not act.")
        );
    }
}
