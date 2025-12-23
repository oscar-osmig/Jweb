package com.osmig.Jweb.app.docs.sections.routing;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class RoutingMiddleware {
    private RoutingMiddleware() {}

    public static Element render() {
        return section(
            h3Title("Middleware"),
            para("Add middleware for authentication, logging, etc."),
            codeBlock("""
// Apply to all routes
app.use((req, next) -> {
    long start = System.currentTimeMillis();
    Object response = next.handle(req);
    long duration = System.currentTimeMillis() - start;
    logger.info("{} {} - {}ms", req.method(), req.path(), duration);
    return response;
});

// Apply to specific paths
app.use("/admin/**", Auth.required());
app.use("/api/**", Auth.jwt(SECRET_KEY));

// Multiple middleware
app.use("/api/**",
    RateLimit.perMinute(100),
    Cors.allow("*"),
    Auth.apiKey()
);"""),

            h3Title("Custom Middleware"),
            codeBlock("""
public class LoggingMiddleware implements Middleware {
    public Object handle(Request req, Next next) {
        logger.info("Request: {} {}", req.method(), req.path());
        try {
            Object response = next.handle(req);
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
