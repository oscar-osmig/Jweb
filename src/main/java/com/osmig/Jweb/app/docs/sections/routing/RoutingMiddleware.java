package com.osmig.Jweb.app.docs.sections.routing;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingMiddleware {
    private RoutingMiddleware() {}

    public static Element render() {
        return section(
            h3Title("Middleware"),
            para("Add middleware for authentication, logging, etc."),
            codeBlock("""
// Apply to all routes
app.use((req, chain) -> {
    long start = System.currentTimeMillis();
    Object response = chain.next();
    long duration = System.currentTimeMillis() - start;
    logger.info("{} {} - {}ms", req.method(), req.path(), duration);
    return response;
});

// Apply to specific path prefixes
app.use("/admin", Auth.requireAuth("/login"));
app.use("/api", Jwt.protect());

// Multiple middleware (one call each)
app.use("/api", RateLimit.perMinute(100).byIp().build());
app.use("/api", Cors.allowAll());"""),

            h3Title("Custom Middleware"),
            codeBlock("""
public class LoggingMiddleware implements Middleware {
    public Object handle(Request req, MiddlewareChain chain) throws Exception {
        logger.info("Request: {} {}", req.method(), req.path());
        try {
            Object response = chain.next();
            logger.info("Response: 200 OK");
            return response;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            throw e;
        }
    }
}

// Register
app.use(new LoggingMiddleware());""")
        );
    }
}
