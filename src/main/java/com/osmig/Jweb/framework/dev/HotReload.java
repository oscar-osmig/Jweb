package com.osmig.Jweb.framework.dev;

import com.osmig.Jweb.framework.core.Element;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Hot reload support for instant browser refresh during development.
 *
 * <h2>Recommended Setup (Fastest - ~500ms):</h2>
 * <pre>
 * // In your layout/page
 * body(
 *     // ... your content ...
 *     HotReload.fast()  // Uses DevServer SSE + rapid polling
 * )
 * </pre>
 *
 * <h2>Alternative Options:</h2>
 * <ul>
 *   <li>{@link #fast()} - SSE + rapid polling (recommended)</li>
 *   <li>{@link DevServer#liveReloadScript()} - Spring LiveReload</li>
 *   <li>{@link DevServer#combinedScript()} - Both LiveReload + SSE</li>
 *   <li>{@link #pollingScript(String)} - Manual polling endpoint</li>
 * </ul>
 *
 * <h2>Configuration (application.yaml):</h2>
 * <pre>
 * spring:
 *   devtools:
 *     restart:
 *       poll-interval: 100ms    # Fast change detection
 *       quiet-period: 50ms      # Quick restart after changes
 *     livereload:
 *       enabled: true
 *
 * jweb:
 *   dev:
 *     hot-reload: true
 *     debounce-ms: 10           # Minimal debounce
 * </pre>
 *
 * <h2>Manual Reload Trigger:</h2>
 * <pre>
 * curl http://localhost:8081/__jweb_dev/reload
 * </pre>
 */
public final class HotReload {

    private static boolean enabled = true;
    private static int pollInterval = 1000; // ms

    private HotReload() {}

    /**
     * Enables hot reload.
     */
    public static void enable() {
        enabled = true;
    }

    /**
     * Disables hot reload.
     */
    public static void disable() {
        enabled = false;
    }

    /**
     * Checks if hot reload is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the polling interval in milliseconds.
     */
    public static void setPollInterval(int ms) {
        pollInterval = ms;
    }

    /**
     * Returns the hot reload script element.
     * Only renders content when enabled.
     */
    public static Element hotReloadScript() {
        if (!enabled) {
            return fragment();
        }
        return inlineScript(clientScript());
    }

    /**
     * Returns the fastest hot reload script.
     * Uses SSE for instant notification + rapid polling on disconnect.
     * This is the recommended approach for development.
     *
     * @return Script element for fast hot reload
     */
    public static Element fast() {
        if (!enabled) {
            return fragment();
        }
        return DevServer.script();
    }

    /**
     * Returns hot reload with LiveReload support.
     * Works with Spring DevTools LiveReload server.
     *
     * @return Script element using LiveReload
     */
    public static Element liveReload() {
        if (!enabled) {
            return fragment();
        }
        return DevServer.liveReloadScript();
    }

    /**
     * Returns combined hot reload with both LiveReload and SSE.
     * Best coverage for all change types.
     *
     * @return Script element using both mechanisms
     */
    public static Element combined() {
        if (!enabled) {
            return fragment();
        }
        return DevServer.combinedScript();
    }

    /**
     * Returns the hot reload script that uses localStorage for version tracking.
     * This works even without a server-side version endpoint by checking
     * for page content changes.
     */
    public static String clientScript() {
        return "(function(){" +
            "if(typeof window.__jwebHotReload!=='undefined')return;" +
            "window.__jwebHotReload=true;" +
            "var key='__jweb_page_version';" +
            "var currentHash='" + System.currentTimeMillis() + "';" +
            "var stored=sessionStorage.getItem(key);" +
            "if(stored&&stored!==currentHash){" +
            "sessionStorage.setItem(key,currentHash);" +
            "console.log('[JWeb] Page updated, content refreshed')}" +
            "else{sessionStorage.setItem(key,currentHash)}" +
            "console.log('[JWeb] Hot reload ready');" +
            "})();";
    }

    /**
     * Returns a script that uses SSE for live reloading.
     * Requires DevServer to be configured.
     */
    public static Element sseScript() {
        if (!enabled) {
            return fragment();
        }
        return DevServer.script();
    }

    /**
     * Returns a script that polls an endpoint for changes.
     * Use this when SSE is not available.
     *
     * @param versionEndpoint URL that returns a version/timestamp
     */
    public static Element pollingScript(String versionEndpoint) {
        if (!enabled) {
            return fragment();
        }
        return inlineScript(
            "(function(){" +
            "var lastVersion=null;" +
            "var poll=function(){" +
            "fetch('" + versionEndpoint + "')" +
            ".then(function(r){return r.text()})" +
            ".then(function(v){" +
            "if(lastVersion&&v!==lastVersion){" +
            "console.log('[JWeb] Reloading...');" +
            "location.reload()}" +
            "lastVersion=v})" +
            ".catch(function(){});" +
            "setTimeout(poll," + pollInterval + ")};" +
            "poll();" +
            "console.log('[JWeb] Hot reload polling started');" +
            "})();"
        );
    }

    /**
     * Returns a script that uses BroadcastChannel for reload coordination.
     * Useful when you want to trigger reload from another tab/process.
     */
    public static Element broadcastScript() {
        if (!enabled) {
            return fragment();
        }
        return inlineScript(
            "(function(){" +
            "if(typeof BroadcastChannel==='undefined')return;" +
            "var bc=new BroadcastChannel('jweb-hot-reload');" +
            "bc.onmessage=function(e){" +
            "if(e.data==='reload'){" +
            "console.log('[JWeb] Reload triggered');" +
            "location.reload()}};" +
            "window.__jwebReload=function(){bc.postMessage('reload')};" +
            "console.log('[JWeb] BroadcastChannel ready');" +
            "})();"
        );
    }

    /**
     * Returns JavaScript to trigger reload via BroadcastChannel.
     * Call this from browser console or inject via bookmarklet.
     */
    public static String triggerReloadJs() {
        return "new BroadcastChannel('jweb-hot-reload').postMessage('reload')";
    }

    /**
     * Builder for customizing hot reload behavior.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean useSSE = true;
        private String customEndpoint;
        private int interval = 1000;
        private boolean showIndicator = true;
        private String indicatorPosition = "bottom-right";

        public Builder useSSE(boolean use) { this.useSSE = use; return this; }
        public Builder usePolling(String endpoint) {
            this.useSSE = false;
            this.customEndpoint = endpoint;
            return this;
        }
        public Builder pollInterval(int ms) { this.interval = ms; return this; }
        public Builder showIndicator(boolean show) { this.showIndicator = show; return this; }
        public Builder indicatorPosition(String pos) { this.indicatorPosition = pos; return this; }

        public Element build() {
            if (!enabled) {
                return fragment();
            }

            StringBuilder js = new StringBuilder("(function(){");

            // Connection indicator
            if (showIndicator) {
                String pos = switch (indicatorPosition) {
                    case "top-left" -> "top:8px;left:8px";
                    case "top-right" -> "top:8px;right:8px";
                    case "bottom-left" -> "bottom:8px;left:8px";
                    default -> "bottom:8px;right:8px";
                };
                js.append("var ind=document.createElement('div');");
                js.append("ind.style.cssText='position:fixed;").append(pos).append(";");
                js.append("width:10px;height:10px;border-radius:50%;background:#22c55e;");
                js.append("z-index:99999;opacity:0.7;transition:opacity 0.2s';");
                js.append("ind.title='Hot reload connected';");
                js.append("document.body.appendChild(ind);");
                js.append("window.__jwebIndicator=ind;");
            }

            if (useSSE) {
                js.append("var es=new EventSource('/__jweb_dev/events');");
                js.append("var lastVersion=null;");
                js.append("es.onmessage=function(e){");
                js.append("var data=JSON.parse(e.data);");
                js.append("if(lastVersion&&data.version!==lastVersion){location.reload()}");
                js.append("lastVersion=data.version};");
                js.append("es.onerror=function(){");
                if (showIndicator) {
                    js.append("if(window.__jwebIndicator)window.__jwebIndicator.style.background='#ef4444';");
                }
                js.append("setTimeout(function(){location.reload()},3000)};");
            } else if (customEndpoint != null) {
                js.append("var lastVersion=null;");
                js.append("var poll=function(){");
                js.append("fetch('").append(customEndpoint).append("')");
                js.append(".then(function(r){return r.text()})");
                js.append(".then(function(v){");
                js.append("if(lastVersion&&v!==lastVersion){location.reload()}");
                js.append("lastVersion=v})");
                js.append(".catch(function(){");
                if (showIndicator) {
                    js.append("if(window.__jwebIndicator)window.__jwebIndicator.style.background='#ef4444';");
                }
                js.append("});");
                js.append("setTimeout(poll,").append(interval).append(")};");
                js.append("poll();");
            }

            js.append("console.log('[JWeb] Hot reload ready');");
            js.append("})();");

            return inlineScript(js.toString());
        }
    }
}
