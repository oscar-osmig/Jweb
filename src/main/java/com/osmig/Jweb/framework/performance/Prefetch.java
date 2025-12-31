package com.osmig.Jweb.framework.performance;

import com.osmig.Jweb.framework.core.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Built-in prefetching for lightning-fast navigation.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>On hover over links/buttons, the target page is fetched in background</li>
 *   <li>Results are cached by the browser via {@code <link rel="prefetch">}</li>
 *   <li>On click, cached content is shown instantly</li>
 *   <li>Works automatically for all internal links and elements with data-prefetch-url</li>
 * </ul>
 *
 * <p>Usage - Add to your layout:</p>
 * <pre>
 * body(
 *     nav(...),
 *     main(...),
 *     footer(...),
 *     Prefetch.script()  // Add this line
 * )
 * </pre>
 *
 * <p>Links (automatic prefetch on hover):</p>
 * <pre>
 * a(attrs().href("/about"), text("About"))
 * </pre>
 *
 * <p>Buttons (use data-prefetch-url):</p>
 * <pre>
 * button(attrs().data("prefetch-url", "/dashboard"), text("Go to Dashboard"))
 * </pre>
 *
 * <p>Any element (use data-prefetch-url):</p>
 * <pre>
 * div(attrs().data("prefetch-url", "/products").class_("card"),
 *     h3("Products"),
 *     p("Click to view all products")
 * )
 * </pre>
 *
 * <p>Disable for specific elements:</p>
 * <pre>
 * a(attrs().href("/logout").data("no-prefetch", "true"), text("Logout"))
 * button(attrs().data("prefetch-url", "/admin").data("no-prefetch", "true"), text("Admin"))
 * </pre>
 */
public final class Prefetch {

    private Prefetch() {}

    // Default cache TTL in milliseconds (5 minutes)
    private static int cacheTtl = 5 * 60 * 1000;

    // Default hover delay before prefetch (100ms to avoid prefetching on scroll-by)
    private static int hoverDelay = 100;

    // Whether prefetch is enabled globally
    private static boolean enabled = true;

    // Whether debug logging is enabled
    private static boolean debug = false;

    /**
     * Spring configuration for prefetch settings.
     */
    @Component
    public static class PrefetchConfig {
        @Value("${jweb.performance.prefetch.enabled:true}")
        private boolean prefetchEnabled;

        @Value("${jweb.performance.prefetch.cache-ttl:300000}")
        private int prefetchCacheTtl;

        @Value("${jweb.performance.prefetch.hover-delay:100}")
        private int prefetchHoverDelay;

        @Value("${jweb.dev.debug:false}")
        private boolean debugEnabled;

        @PostConstruct
        public void init() {
            Prefetch.enabled = prefetchEnabled;
            Prefetch.cacheTtl = prefetchCacheTtl;
            Prefetch.hoverDelay = prefetchHoverDelay;
            Prefetch.debug = debugEnabled;
            if (prefetchEnabled && debugEnabled) {
                System.out.println("[JWeb] Prefetch enabled (cache: " + prefetchCacheTtl + "ms, hover delay: " + prefetchHoverDelay + "ms)");
            }
        }
    }

    /**
     * Sets the cache TTL (time-to-live) in milliseconds.
     * Default: 5 minutes (300000ms)
     */
    public static void setCacheTtl(int ttlMs) {
        cacheTtl = ttlMs;
    }

    /**
     * Sets the hover delay before prefetching starts.
     * Default: 100ms
     */
    public static void setHoverDelay(int delayMs) {
        hoverDelay = delayMs;
    }

    /**
     * Enables or disables prefetching globally.
     */
    public static void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    /**
     * Returns the prefetch script element.
     * Add this to your layout's body.
     */
    public static Element script() {
        if (!enabled) {
            return fragment();
        }
        return inlineScript(clientScript());
    }

    /**
     * Returns the prefetch script as an HTML string.
     * Used by the framework for automatic injection.
     */
    public static String scriptTag() {
        if (!enabled) {
            return "";
        }
        return "<script>" + clientScript() + "</script>";
    }

    /**
     * Returns the prefetch JavaScript code.
     * Uses link prefetch hints for browser-native caching - safe and fast.
     *
     * <p>Supports:</p>
     * <ul>
     *   <li>Anchor tags: {@code <a href="/path">}</li>
     *   <li>Buttons with data-prefetch-url: {@code <button data-prefetch-url="/path">}</li>
     *   <li>Any element with data-prefetch-url: {@code <div data-prefetch-url="/path">}</li>
     * </ul>
     */
    public static String clientScript() {
        return "(function(){" +
            // Prevent duplicate initialization
            "if(window.__jwebPrefetchInit)return;" +
            "window.__jwebPrefetchInit=true;" +

            "var prefetched={};" +
            "var DELAY=" + hoverDelay + ";" +
            "var hoverTimeout=null;" +
            "var currentEl=null;" + // Track currently hovered element

            // Find prefetchable element (anchor or element with data-prefetch-url)
            "function findPrefetchable(target){" +
                "var el=target.closest('[data-prefetch-url]');" +
                "if(el)return el;" +
                "return target.closest('a[href]')" +
            "}" +

            // Get URL from element (href or data-prefetch-url)
            "function getUrl(el){" +
                "if(!el)return null;" +
                "if(el.dataset&&el.dataset.prefetchUrl)return el.dataset.prefetchUrl;" +
                "return el.href||null" +
            "}" +

            // Check if URL should be prefetched
            "function shouldPrefetch(el){" +
                "if(!el)return false;" +
                "if(el.dataset&&el.dataset.noPrefetch)return false;" +
                "var url=getUrl(el);" +
                "if(!url)return false;" +
                // Skip already prefetched
                "if(prefetched[url])return false;" +
                // Handle relative URLs - convert to absolute for comparison
                "var fullUrl=new URL(url,location.origin).href;" +
                // Skip external links
                "if(fullUrl.indexOf(location.origin)!==0)return false;" +
                // Skip anchors on same page
                "if(fullUrl.indexOf('#')!==-1&&fullUrl.split('#')[0]===location.href.split('#')[0])return false;" +
                "return true" +
            "}" +

            // Prefetch using link element (browser-native, safe)
            "function prefetch(url){" +
                "if(!url)return;" +
                // Convert relative to absolute URL
                "var fullUrl=new URL(url,location.origin).href;" +
                "if(prefetched[fullUrl])return;" +
                "prefetched[fullUrl]=true;" +
                // Use link prefetch for full page preload
                "var link=document.createElement('link');" +
                "link.rel='prefetch';" +
                "link.href=fullUrl;" +
                "link.as='document';" +
                "document.head.appendChild(link)" +
            "}" +

            // Clear any pending prefetch
            "function cancelPrefetch(){" +
                "if(hoverTimeout){" +
                    "clearTimeout(hoverTimeout);" +
                    "hoverTimeout=null" +
                "}" +
                "currentEl=null" +
            "}" +

            // Hover prefetch - only triggers after DELAY ms of continuous hover
            "document.addEventListener('mouseover',function(e){" +
                "var el=findPrefetchable(e.target);" +
                // If moved to different element, cancel previous
                "if(el!==currentEl)cancelPrefetch();" +
                "if(!shouldPrefetch(el))return;" +
                "currentEl=el;" +
                // Only start timeout if not already waiting
                "if(!hoverTimeout){" +
                    "hoverTimeout=setTimeout(function(){" +
                        // Verify still hovering same element
                        "if(currentEl===el){prefetch(getUrl(el))}" +
                        "hoverTimeout=null" +
                    "},DELAY)" +
                "}" +
            "});" +

            // Cancel prefetch when leaving any element
            "document.addEventListener('mouseout',function(e){" +
                "var el=findPrefetchable(e.target);" +
                // Only cancel if leaving the tracked element
                "if(el===currentEl)cancelPrefetch()" +
            "});" +

            // Touch prefetch (on touchstart) - immediate since touch implies intent
            "document.addEventListener('touchstart',function(e){" +
                "var el=findPrefetchable(e.target);" +
                "if(shouldPrefetch(el))prefetch(getUrl(el))" +
            "},{passive:true})" +

            (debug ? ";console.log('[JWeb] Prefetch enabled')" : "") +
        "})();";
    }

    /**
     * Returns prefetch script that also preloads specific URLs on page load.
     * Useful for preloading likely next pages.
     *
     * @param urls URLs to preload immediately
     */
    public static Element scriptWithPreload(String... urls) {
        if (!enabled) {
            return fragment();
        }

        StringBuilder preloadScript = new StringBuilder();
        preloadScript.append("(function(){");
        preloadScript.append("setTimeout(function(){");
        for (String url : urls) {
            // Use link prefetch for browser-native preloading
            preloadScript.append("var l=document.createElement('link');")
                         .append("l.rel='prefetch';")
                         .append("l.href='").append(escapeJs(url)).append("';")
                         .append("l.as='document';")
                         .append("document.head.appendChild(l);");
        }
        preloadScript.append("},500)})();"); // Delay 500ms to not compete with main page load

        return fragment(
            inlineScript(clientScript()),
            inlineScript(preloadScript.toString())
        );
    }

    private static String escapeJs(String s) {
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
