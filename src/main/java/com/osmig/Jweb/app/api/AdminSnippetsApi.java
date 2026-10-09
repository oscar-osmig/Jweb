package com.osmig.Jweb.app.api;

import com.osmig.Jweb.app.sandbox.SandboxDsl;
import jweb.Doc;
import jweb.Response;
import jweb.api.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The snippet review API. Guarded in {@code Routes}: every call carries the
 * admin email and token as {@code X-Admin-Email} / {@code X-Admin-Token}
 * headers — the same credentials as the login form — or an admin session
 * cookie. Statuses are real (404 for an unknown id, 400 for a bad filter), so
 * the queue can be driven from a script as well as from the admin page.
 *
 * <pre>
 * curl -H "X-Admin-Email: $JWEB_ADMIN_EMAIL" -H "X-Admin-Token: $JWEB_ADMIN_TOKEN" \
 *      https://jweb.build/api/v1/admin/snippets
 * curl -X POST -H ... https://jweb.build/api/v1/admin/snippets/&lt;id&gt;/approve
 * </pre>
 */
@REST("/api/v1/admin/snippets")
public class AdminSnippetsApi {

    private final SnippetStore snippets;

    public AdminSnippetsApi(SnippetStore snippets) {
        this.snippets = snippets;
    }

    /** The review queue (default) or the published list: {@code ?status=pending|approved}. */
    @GET
    public Object list(@Query(value = "status", defaultValue = "pending") String status) {
        List<Doc> docs = switch (status) {
            case SnippetStore.PENDING -> snippets.pending();
            case SnippetStore.APPROVED -> snippets.approved();
            default -> null;
        };
        if (docs == null) {
            return Response.json(400, Map.of("error", "status must be pending or approved"));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", status);
        out.put("count", docs.size());
        out.put("snippets", docs.stream().map(AdminSnippetsApi::summary).toList());
        return out;
    }

    /** One snippet with its code and the HTML its preview renders to. */
    @GET("/{id}")
    public Object get(@Param String id) {
        Doc snippet = snippets.byId(id).orElse(null);
        if (snippet == null) return notFound(id);

        Map<String, Object> out = summary(snippet);
        String code = snippet.getString("code", "");
        SandboxDsl.Result r = SandboxDsl.run(code);
        out.put("code", code);
        out.put("renders", r.isOk());
        out.put("html", r.isOk() ? r.element().toHtml() : null);
        out.put("error", r.isOk() ? null : r.error());
        return out;
    }

    /** Publishes a pending snippet: it appears on /snippets on the next request. */
    @POST("/{id}/approve")
    public Object approve(@Param String id) {
        if (!snippets.approve(id)) return notFound(id);
        return Map.of("approved", true, "id", id);
    }

    /** Rejects a pending snippet, or unpublishes an approved one. */
    @DEL("/{id}")
    public Object delete(@Param String id) {
        if (!snippets.delete(id)) return notFound(id);
        return Map.of("deleted", true, "id", id);
    }

    private static Object notFound(String id) {
        return Response.json(404, Map.of("error", "No snippet with id " + id));
    }

    private static Map<String, Object> summary(Doc s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("title", s.getString("title", ""));
        out.put("author", s.getString("author", ""));
        out.put("status", s.getString("status", ""));
        out.put("createdAt", iso(s.get("createdAt")));
        out.put("approvedAt", iso(s.get("approvedAt")));
        return out;
    }

    private static String iso(Object date) {
        return date instanceof Date d ? d.toInstant().toString() : null;
    }
}
