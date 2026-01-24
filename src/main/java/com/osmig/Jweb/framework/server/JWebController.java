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
    public ResponseEntity<String> handleRequest(
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

        // Try page routes
        Optional<PageRoute> pageMatch = matchPageRoute(path);
        if (pageMatch.isPresent()) {
            return handlePageRoute(pageMatch.get(), servletRequest);
        }

        // Try legacy routes
        Optional<Router.RouteMatch> match = router.match(method, path);

        if (match.isEmpty()) {
            return handleNotFound(path);
        }

        // Create state context for this request
        StateManager.StateContext context = StateManager.createContext();
        try {
            Request request = new Request(servletRequest);

            // Execute through middleware stack
            Object result = middlewareStack.execute(request, () -> match.get().handle(request));

            return processResult(result, context);
        } catch (Exception e) {
            return handleError(e);
        } finally {
            // Always clean up context to prevent memory leaks
            context.clearContext();
        }
    }

    private ResponseEntity<String> processResult(Object result, StateManager.StateContext context) {
        if (result == null) {
            return ResponseEntity.ok().body("");
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
            String hydrationScript = buildHydrationScript(context);
            html = injectHydrationData(html, hydrationScript);

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        }

        if (result instanceof String str) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(str);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(result.toString());
    }

    private String buildHydrationScript(StateManager.StateContext context) {
        HydrationData data = HydrationData.builder()
            .contextId(context.getSessionId())
            .states(new java.util.ArrayList<>(context.getStates().values()))
            .build();
        return data.toScriptTag();
    }

    private String injectHydrationData(String html, String hydrationScript) {
        // Get pre-cached prefetch script
        String prefetchScript = Prefetch.scriptTag();

        // Fast path: if no scripts to inject, return as-is
        if (prefetchScript.isEmpty() && hydrationScript.isEmpty()) {
            return html;
        }

        // Use StringBuilder for efficient string building
        int bodyEnd = html.lastIndexOf(BODY_END);
        if (bodyEnd != -1) {
            return new StringBuilder(html.length() + prefetchScript.length() + hydrationScript.length())
                .append(html, 0, bodyEnd)
                .append(prefetchScript)
                .append(hydrationScript)
                .append(html, bodyEnd, html.length())
                .toString();
        }

        // Inject before </html> if no body
        int htmlEnd = html.lastIndexOf(HTML_END);
        if (htmlEnd != -1) {
            return new StringBuilder(html.length() + prefetchScript.length() + hydrationScript.length())
                .append(html, 0, htmlEnd)
                .append(prefetchScript)
                .append(hydrationScript)
                .append(html, htmlEnd, html.length())
                .toString();
        }

        // Append at end
        return html + prefetchScript + hydrationScript;
    }

    private ResponseEntity<String> handleNotFound(String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.TEXT_HTML)
            .body(ErrorPage.render404(path).toHtml());
    }

    private ResponseEntity<String> handleError(Exception e) {
        e.printStackTrace();
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
            // Create page instance
            Template page = route.pageSupplier().get();
            Element content = page.render();

            // Wrap in layout if specified
            Element result = content;
            if (route.layoutClass() != null) {
                result = wrapInLayout(route.layoutClass(), route.title(), content);
            }

            String html = result.toHtml();
            String hydrationScript = buildHydrationScript(context);
            html = injectHydrationData(html, hydrationScript);

            // Check if this is a prefetch request (has X-Prefetch header)
            boolean isPrefetch = "true".equals(servletRequest.getHeader("X-Prefetch"));
            CacheControl cacheControl = isPrefetch ? PREFETCH_CACHE : NAVIGATION_CACHE;

            return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        } catch (Exception e) {
            return handleError(e);
        } finally {
            // Always clean up context to prevent memory leaks
            context.clearContext();
        }
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
