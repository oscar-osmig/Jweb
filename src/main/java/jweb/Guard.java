package jweb;

/**
 * A check that runs before a route: return {@code null} to let the request
 * through, or any response (a redirect, an error, an element) to answer it
 * instead. Registered by path pattern on the app and applied to page routes,
 * router routes and {@code @REST} controllers under that pattern alike:
 *
 * <pre>{@code
 * app.guard("/admin/**", req -> req.principal() != null ? null : Response.redirect("/admin/login"));
 * app.guard("/admin/**", Auth.requireLogin("/admin/login"));     // the same, as sugar
 * app.guard("/api/v1/admin/**", Auth.requireRole("admin"));      // any Middleware works too
 * }</pre>
 *
 * <p>Guards run in registration order, after the middleware stack and
 * before the handler; the first non-null answer wins.</p>
 */
@FunctionalInterface
public interface Guard {

    /**
     * @param request the request
     * @return null to pass, or the response to send instead
     */
    Object check(Request request) throws Exception;

    /** A guard from a middleware: the middleware's own response answers, {@code chain.next()} passes. */
    static Guard of(Middleware middleware) {
        Object pass = new Object();
        return request -> {
            Object result = middleware.handle(request, () -> pass);
            return result == pass ? null : result;
        };
    }
}
