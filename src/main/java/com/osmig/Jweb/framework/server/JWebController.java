package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.core.RawContent;
import com.osmig.Jweb.framework.hydration.HydrationData;
import com.osmig.Jweb.framework.middleware.MiddlewareStack;
import com.osmig.Jweb.framework.performance.Prefetch;
import com.osmig.Jweb.framework.routing.PageRegistry;
import com.osmig.Jweb.framework.routing.PageRoute;
import com.osmig.Jweb.framework.routing.Router;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.template.Template;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Spring MVC Controller that handles JWeb page routes.
 * API routes (/api/**) are handled by @RestController classes.
 */
@Controller
public class JWebController {

    private final Router router;
    private final MiddlewareStack middlewareStack;
    private final PageRegistry pageRegistry;

    // Cache control for navigation responses (short cache for dynamic content)
    private static final CacheControl NAVIGATION_CACHE = CacheControl
            .maxAge(30, TimeUnit.SECONDS)
            .mustRevalidate()
            .cachePrivate();

    // Cache control for prefetch responses (longer cache)
    private static final CacheControl PREFETCH_CACHE = CacheControl
            .maxAge(5, TimeUnit.MINUTES)
            .cachePrivate();

    // Cache layout constructors to avoid reflection overhead per request
    private static final Map<Class<?>, Constructor<?>> layoutConstructorCache = new ConcurrentHashMap<>();

    // Pre-cached markers for fast HTML injection
    private static final String BODY_END = "</body>";
    private static final String HTML_END = "</html>";

    public JWebController(JWeb jweb) {
        this.router = jweb.getRouter();
        this.middlewareStack = jweb.getMiddlewareStack();
        this.pageRegistry = jweb.getPageRegistry();
    }

    @RequestMapping(value = "/**")
    public Object handleRequest(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String method = servletRequest.getMethod();
        String path = servletRequest.getRequestURI();

        // Skip paths handled by other controllers (but not /api/docs, /api/openapi.json, etc.)
        if (path.startsWith("/api/v") ||  // Skip /api/v1/*, /api/v2/* etc (REST endpoints)
            path.startsWith("/h2-console") ||
            path.equals("/jweb") ||
            "websocket".equalsIgnoreCase(servletRequest.getHeader("Upgrade"))) {
            return null;
        }

        // Per-request CSP nonce: the serializer stamps it on <script> tags,
        // securityHeaders() puts it in the Content-Security-Policy header
        com.osmig.Jweb.framework.security.CspNonce.begin();

        // Try page routes (GET/HEAD only — pages are documents)
        Optional<PageRoute> pageMatch = matchPageRoute(path);
        if (pageMatch.isPresent()) {
            if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "GET, HEAD")
                    .body("Method not allowed");
            }
            return handlePageRoute(pageMatch.get(), servletRequest);
        }

        // Try legacy routes
        Optional<Router.RouteMatch> match = router.match(method, path);

        if (match.isEmpty()) {
            // Path exists under another method → 405, not 404
            var allowed = router.allowedMethods(path);
            if (!allowed.isEmpty()) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", String.join(", ", allowed))
                    .body("Method not allowed");
            }

            // 404s still run through middleware (logging, headers, metrics)
            Request request = new Request(servletRequest);
            try {
                Object result = middlewareStack.execute(request, () -> handleNotFound(path));
                return applyQueuedHeaders(processResult(result, null, request), request);
            } catch (Exception e) {
                return handleError(e);
            } finally {
                clearThreadLocals();
            }
        }

        // Create state context for this request
        StateManager.StateContext context = StateManager.createContext();
        try {
            Request request = new Request(servletRequest);

            // Execute through middleware stack
            Object result = middlewareStack.execute(request, () -> match.get().handle(request));

            // SSE emitters stream through Spring MVC directly
            if (result instanceof org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
                return emitter;
            }
            if (result instanceof com.osmig.Jweb.framework.sse.SseEmitter emitter) {
                return emitter.toResponse();
            }

            // Streaming SSR: flush the shell now, stream Suspense blocks as they resolve
            if (result instanceof com.osmig.Jweb.framework.async.Streamed streamed) {
                streamResponse(streamed, context, request, servletResponse);
                return null;   // response already written and committed
            }

            return applyQueuedHeaders(processResult(result, context, request), request);
        } catch (Exception e) {
            return handleError(e);
        } finally {
            // Detach from this thread only. The context stays registered so
            // browser events (WebSocket) can reference it; the TTL reaper in
            // StateManager collects it once idle.
            StateManager.clearContext();
            clearThreadLocals();
        }
    }

    /** Clears per-request thread-locals (DI context, portals, locale, CSP nonce) after a render. */
    private void clearThreadLocals() {
        com.osmig.Jweb.framework.context.Context.clear();
        com.osmig.Jweb.framework.portal.Portal.clear();
        com.osmig.Jweb.framework.i18n.I18n.clearCurrent();
        com.osmig.Jweb.framework.security.CspNonce.clear();
    }

    /** Adds middleware-queued headers to the response (existing headers win). */
    private ResponseEntity<String> applyQueuedHeaders(ResponseEntity<String> response, Request request) {
        var queued = request.responseHeaders();
        if (queued.isEmpty()) {
            return response;
        }
        var headers = new org.springframework.http.HttpHeaders();
        headers.addAll(response.getHeaders());
        queued.forEach((name, value) -> {
            if (headers.get(name) == null) {
                headers.add(name, value);
            }
        });
        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    private ResponseEntity<String> processResult(Object result, StateManager.StateContext context, Request request) {
        if (result == null) {
            return ResponseEntity.ok().body("");
        }

        // Templates returned from handlers get their lifecycle hooks
        if (result instanceof Template template) {
            template.beforeRender(request);
            String html = template.render().toHtml();
            template.afterRender(request);
            html = applyTemplateExtras(html, template);
            if (context != null) {
                html = injectHydrationData(html, buildHydrationScript(context));
            }
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        }

        // If middleware already returned a ResponseEntity, use it directly
        if (result instanceof ResponseEntity<?> responseEntity) {
            @SuppressWarnings("unchecked")
            ResponseEntity<String> typed = (ResponseEntity<String>) responseEntity;
            return typed;
        }

        // Handle RawContent with proper content type
        if (result instanceof RawContent rawContent) {
            MediaType mediaType = rawContent.isJson()
                ? MediaType.APPLICATION_JSON
                : MediaType.TEXT_HTML;
            return ResponseEntity.ok()
                .contentType(mediaType)
                .body(rawContent.toHtml());
        }

        if (result instanceof Element element) {
            String html = element.toHtml();

            // Inject hydration data with state and context info
            // (skipped when no state context exists, e.g. 404 pages)
            if (context != null) {
                String hydrationScript = buildHydrationScript(context);
                html = injectHydrationData(html, hydrationScript);
            }

            // Short private cache so back/forward and quick revisits are free
            return ResponseEntity.ok()
                .cacheControl(NAVIGATION_CACHE)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        }

        if (result instanceof String str) {
            // Heuristic: JSON-shaped strings are served as JSON. Use
            // Response.json(...)/RawContent to set the type explicitly.
            String trimmed = str.stripLeading();
            boolean looksJson = trimmed.startsWith("{") || trimmed.startsWith("[");
            return ResponseEntity.ok()
                .contentType(looksJson ? MediaType.APPLICATION_JSON : MediaType.TEXT_HTML)
                .body(str);
        }

        // POJOs are serialized to real JSON (not toString())
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(com.osmig.Jweb.framework.util.Json.stringify(result));
    }

    private String buildHydrationScript(StateManager.StateContext context) {
        HydrationData data = HydrationData.builder()
            .contextId(context.getSessionId())
            .states(new java.util.ArrayList<>(context.getStates().values()))
            .build();
        return data.toScriptTag();
    }

    private String injectHydrationData(String html, String hydrationScript) {
        // External, immutably-cached script references (the browser caches
        // them across navigations; the ?v= content hash busts on change).
        // Only the per-request hydration data stays inline.
        String prefetchScript = externalPrefetchTag();
        String runtimeScript = externalRuntimeTag();

        // Fast path: if no scripts to inject, return as-is
        if (prefetchScript.isEmpty() && hydrationScript.isEmpty() && runtimeScript.isEmpty()) {
            return html;
        }

        // Order: hydration data first so JWeb.init() can read __JWEB_DATA__
        String scripts = prefetchScript + hydrationScript + runtimeScript;

        // Use StringBuilder for efficient string building
        int bodyEnd = html.lastIndexOf(BODY_END);
        if (bodyEnd != -1) {
            return new StringBuilder(html.length() + scripts.length())
                .append(html, 0, bodyEnd)
                .append(scripts)
                .append(html, bodyEnd, html.length())
                .toString();
        }

        // Inject before </html> if no body
        int htmlEnd = html.lastIndexOf(HTML_END);
        if (htmlEnd != -1) {
            return new StringBuilder(html.length() + scripts.length())
                .append(html, 0, htmlEnd)
                .append(scripts)
                .append(html, htmlEnd, html.length())
                .toString();
        }

        // HTML fragment (no <body>/<html>) — served for swap targets;
        // injecting scripts would duplicate them in the page after the swap
        return html;
    }

    // Cached external script tags (content is fixed after startup; the
    // version hash makes the immutable caching safe)
    private static volatile String cachedRuntimeTag;
    private static volatile String cachedPrefetchTag;

    private static String externalRuntimeTag() {
        String tag = cachedRuntimeTag;
        if (tag == null) {
            if (!com.osmig.Jweb.framework.js.JWebRuntime.isEnabled()) {
                tag = "";
            } else {
                String script = com.osmig.Jweb.framework.js.JWebRuntime.getScript();
                tag = "<script src=\"/jweb/runtime.js?v=" + JWebAssetsController.versionOf(script) + "\"></script>";
            }
            cachedRuntimeTag = tag;
        }
        return tag;
    }

    private static String externalPrefetchTag() {
        String tag = cachedPrefetchTag;
        if (tag == null) {
            String script = Prefetch.clientScript();
            tag = (script == null || script.isEmpty())
                ? ""
                : "<script src=\"/jweb/prefetch.js?v=" + JWebAssetsController.versionOf(script) + "\"></script>";
            cachedPrefetchTag = tag;
        }
        return tag;
    }

    // ==================== Streaming SSR ====================

    /**
     * Renders a {@link com.osmig.Jweb.framework.async.Streamed} page: the
     * shell (with placeholders) flushes immediately; each Suspense block's
     * HTML is written as a chunk the moment its data resolves, replacing its
     * placeholder via a tiny inline script.
     */
    private void streamResponse(com.osmig.Jweb.framework.async.Streamed streamed,
                                StateManager.StateContext context, Request request,
                                HttpServletResponse servletResponse) throws java.io.IOException {
        var streaming = com.osmig.Jweb.framework.async.StreamingContext.open();
        String html;
        try {
            html = streamed.page().get().toHtml();
        } finally {
            com.osmig.Jweb.framework.async.StreamingContext.close();
        }
        if (context != null) {
            html = injectHydrationData(html, buildHydrationScript(context));
        }

        // Split so late chunks land inside <body>
        int bodyEnd = html.lastIndexOf(BODY_END);
        String shell = bodyEnd >= 0 ? html.substring(0, bodyEnd) : html;
        String tail = bodyEnd >= 0 ? html.substring(bodyEnd) : "";
        var pendings = new java.util.ArrayList<>(streaming.pendings());

        servletResponse.setContentType("text/html;charset=UTF-8");
        request.responseHeaders().forEach(servletResponse::setHeader);
        var out = servletResponse.getWriter();

        out.write(shell);
        out.flush();   // commits the response — the shell paints immediately

        while (!pendings.isEmpty()) {
            java.util.concurrent.CompletableFuture.anyOf(
                pendings.stream().map(p -> p.html()).toArray(java.util.concurrent.CompletableFuture[]::new)
            ).join();
            var it = pendings.iterator();
            while (it.hasNext()) {
                var pending = it.next();
                if (pending.html().isDone()) {
                    out.write(streamChunk(pending.placeholderId(), pending.html().join()));
                    it.remove();
                }
            }
            out.flush();
        }

        out.write(tail);
        out.flush();
    }

    /** A streamed block: hidden template + script that swaps the placeholder. */
    public static String streamChunk(String placeholderId, String contentHtml) {
        String templateId = placeholderId + "-c";
        return "<template id=\"" + templateId + "\">" + contentHtml + "</template>"
            + "<script" + com.osmig.Jweb.framework.security.CspNonce.attr() + ">(function(){var p=document.getElementById('" + placeholderId + "'),"
            + "t=document.getElementById('" + templateId + "');"
            + "if(p&&t){p.replaceWith(t.content.cloneNode(true));t.remove();}})()</script>";
    }

    private ResponseEntity<String> handleNotFound(String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.TEXT_HTML)
            .body(ErrorPage.render404(path).toHtml());
    }

    private ResponseEntity<String> handleError(Exception e) {
        // Bad typed-route parameters are client errors, not server errors
        if (e instanceof com.osmig.Jweb.framework.routing.TypedRoute.RouteParamException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_HTML)
                .body(ErrorPage.render404(e.getMessage()).toHtml());
        }
        com.osmig.Jweb.framework.util.Log.error("Unhandled error while handling request: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.TEXT_HTML)
            .body(ErrorPage.render(500, "Server Error", e).toHtml());
    }

    // ==================== Page Route Matching ====================

    private Optional<PageRoute> matchPageRoute(String path) {
        // O(1) HashMap lookup instead of O(n) linear scan
        return pageRegistry.findByPath(path);
    }

    private ResponseEntity<String> handlePageRoute(PageRoute route, HttpServletRequest servletRequest) {
        StateManager.StateContext context = StateManager.createContext();
        try {
            Request request = new Request(servletRequest);

            // Page routes run through the middleware stack like every other
            // route, so auth/CSRF/headers/logging apply to them too.
            Template[] pageHolder = new Template[1];
            Object result = middlewareStack.execute(request, () -> renderPage(route, request, pageHolder));

            // Middleware may short-circuit (auth redirect, rate limit, ...)
            if (!(result instanceof Element element)) {
                return applyQueuedHeaders(processResult(result, context, request), request);
            }

            Template page = pageHolder[0];
            String html = element.toHtml();
            if (page != null) {
                html = applyTemplateExtras(html, page);
            }
            String hydrationScript = buildHydrationScript(context);
            html = injectHydrationData(html, hydrationScript);

            // Check if this is a prefetch request (has X-Prefetch header)
            boolean isPrefetch = "true".equals(servletRequest.getHeader("X-Prefetch"));
            CacheControl cacheControl = isPrefetch ? PREFETCH_CACHE : cacheControlFor(page);

            return applyQueuedHeaders(ResponseEntity.ok()
                .cacheControl(cacheControl)
                .contentType(MediaType.TEXT_HTML)
                .body(html), request);
        } catch (Exception e) {
            return handleError(e);
        } finally {
            // Detach from this thread only — see handleRequest
            StateManager.clearContext();
            clearThreadLocals();
        }
    }

    /**
     * Renders a page route's template with its lifecycle hooks
     * (beforeRender → render → layout → afterRender), wrapped in its layout
     * if configured. The instantiated page is exposed via pageHolder so the
     * caller can apply title/head/script extras to the final HTML.
     */
    private Element renderPage(PageRoute route, Request request, Template[] pageHolder) {
        Template page = route.pageSupplier().get();
        pageHolder[0] = page;
        page.beforeRender(request);
        Element content = page.render();
        String title = page.pageTitle().orElse(route.title());
        Element result = route.layoutClass() != null
            ? wrapInLayout(route.layoutClass(), title, content)
            : content;
        page.afterRender(request);
        return result;
    }

    /** Cache-control derived from the template's cacheable()/cacheDuration(). */
    private CacheControl cacheControlFor(Template page) {
        if (page == null) return NAVIGATION_CACHE;
        if (!page.cacheable()) return CacheControl.noStore();
        if (page.cacheDuration() > 0) {
            return CacheControl.maxAge(page.cacheDuration(), TimeUnit.SECONDS)
                .mustRevalidate()
                .cachePrivate();
        }
        return NAVIGATION_CACHE;
    }

    /**
     * Injects the template's pageTitle/metaDescription/extraHead into the
     * head, and scripts/onMount/onUnmount before the closing body tag.
     */
    private String applyTemplateExtras(String html, Template page) {
        // Title: replace the existing <title> or add one to the head
        Optional<String> pageTitle = page.pageTitle();
        if (pageTitle.isPresent()) {
            String escaped = escapeHtmlText(pageTitle.get());
            int start = html.indexOf("<title>");
            int end = html.indexOf("</title>");
            if (start >= 0 && end > start) {
                html = html.substring(0, start + "<title>".length()) + escaped + html.substring(end);
            } else {
                html = injectBefore(html, "</head>", "<title>" + escaped + "</title>");
            }
        }

        StringBuilder headExtras = new StringBuilder();
        page.metaDescription().ifPresent(desc -> headExtras
            .append("<meta name=\"description\" content=\"")
            .append(escapeHtmlAttribute(desc))
            .append("\">"));
        page.extraHead().ifPresent(extra -> headExtras.append(extra.toHtml()));
        if (headExtras.length() > 0) {
            html = injectBefore(html, "</head>", headExtras.toString());
        }

        StringBuilder bodyExtras = new StringBuilder();
        String scriptOpen = "<script" + com.osmig.Jweb.framework.security.CspNonce.attr() + ">";
        page.scripts().ifPresent(js -> bodyExtras.append(scriptOpen).append(js).append("</script>"));
        String mount = page.onMount();
        if (mount != null && !mount.isBlank()) {
            bodyExtras.append(scriptOpen).append("document.addEventListener('DOMContentLoaded',function(){")
                .append(mount).append("});</script>");
        }
        String unmount = page.onUnmount();
        if (unmount != null && !unmount.isBlank()) {
            bodyExtras.append(scriptOpen).append("window.addEventListener('beforeunload',function(){")
                .append(unmount).append("});</script>");
        }
        if (bodyExtras.length() > 0) {
            html = injectBefore(html, "</body>", bodyExtras.toString());
        }

        return html;
    }

    private static String injectBefore(String html, String marker, String content) {
        int index = html.lastIndexOf(marker);
        if (index < 0) {
            return html + content;
        }
        return html.substring(0, index) + content + html.substring(index);
    }

    private static String escapeHtmlText(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeHtmlAttribute(String text) {
        return escapeHtmlText(text).replace("\"", "&quot;").replace("'", "&#39;");
    }

    private Element wrapInLayout(Class<? extends Template> layoutClass, String title, Element content) {
        try {
            // Get cached constructor or find and cache it
            Constructor<?> cachedCtor = layoutConstructorCache.get(layoutClass);

            if (cachedCtor == null) {
                // Try to find constructor with (String, Element) first
                try {
                    cachedCtor = layoutClass.getConstructor(String.class, Element.class);
                } catch (NoSuchMethodException e) {
                    // Try constructor with just Element
                    cachedCtor = layoutClass.getConstructor(Element.class);
                }
                cachedCtor.setAccessible(true);
                layoutConstructorCache.put(layoutClass, cachedCtor);
            }

            // Invoke the cached constructor
            Template layout;
            if (cachedCtor.getParameterCount() == 2) {
                layout = (Template) cachedCtor.newInstance(title, content);
            } else {
                layout = (Template) cachedCtor.newInstance(content);
            }
            return layout.render();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate layout: " + layoutClass.getName(), e);
        }
    }
}
