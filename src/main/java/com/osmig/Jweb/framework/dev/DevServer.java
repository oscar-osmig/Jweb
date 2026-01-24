package com.osmig.Jweb.framework.dev;

import com.osmig.Jweb.framework.core.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Development hot reload server for automatic browser refresh.
 * Only watches /app code by default, not the framework.
 *
 * <p>Setup in application.yaml:</p>
 * <pre>
 * jweb:
 *   dev:
 *     hot-reload: true
 *     watch-paths: src/main/java/com/osmig/Jweb/app  # Only app code
 * </pre>
 *
 * <p>Or in application.properties:</p>
 * <pre>
 * jweb.dev.hot-reload=true
 * jweb.dev.watch-paths=src/main/java/com/osmig/Jweb/app
 * </pre>
 *
 * <p>Add to your layout/page:</p>
 * <pre>
 * body(
 *     // ... your content ...
 *     DevServer.script()  // Only active when hot-reload is enabled
 * )
 * </pre>
 *
 * <p>The server watches for file changes and notifies connected browsers via SSE.</p>
 */
@Configuration
@ConditionalOnProperty(name = "jweb.dev.hot-reload", havingValue = "true")
public class DevServer {

    private static final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();
    private static final AtomicLong version = new AtomicLong(System.currentTimeMillis());
    private static volatile boolean enabled = false;
    private static volatile boolean watching = false;
    private static volatile boolean debug = false;
    private static String[] watchPaths = {"src/main/java/com/osmig/Jweb/app"};
    private static int debounceMs = 50;

    @Value("${jweb.dev.watch-paths:src/main/java/com/osmig/Jweb/app}")
    public void setWatchPathsFromConfig(String paths) {
        if (paths != null && !paths.isBlank()) {
            watchPaths = paths.split(",");
        }
    }

    @Value("${jweb.dev.debounce-ms:50}")
    public void setDebounceMs(int ms) {
        debounceMs = Math.max(10, ms);
    }

    @Value("${jweb.dev.debug:false}")
    public void setDebug(boolean debugMode) {
        debug = debugMode;
    }

    /**
     * Initializes the hot reload server (called automatically by Spring).
     */
    @Bean
    public DevServerInitializer devServerInitializer() {
        return new DevServerInitializer();
    }

    /**
     * Returns true if hot reload is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables hot reload programmatically.
     */
    public static void enable() {
        enabled = true;
        startWatching();
    }

    /**
     * Disables hot reload.
     */
    public static void disable() {
        enabled = false;
        watching = false;
    }

    /**
     * Sets the paths to watch for changes.
     */
    public static void setWatchPaths(String... paths) {
        watchPaths = paths;
        if (watching) {
            watching = false;
            startWatching();
        }
    }

    /**
     * Manually triggers a reload.
     */
    public static void triggerReload() {
        version.set(System.currentTimeMillis());
        notifyClients();
    }

    /**
     * Returns the JavaScript for hot reload.
     * Only renders content when hot reload is enabled.
     */
    public static Element script() {
        if (!enabled) {
            return fragment(); // Empty fragment when disabled
        }
        return inlineScript(clientScript());
    }

    /**
     * Returns a script that uses Spring DevTools LiveReload for instant refresh.
     * LiveReload is faster than SSE for static changes.
     * Combines LiveReload with SSE for comprehensive coverage.
     */
    public static Element liveReloadScript() {
        return liveReloadScript(35729);
    }

    /**
     * Returns LiveReload script with custom port.
     */
    public static Element liveReloadScript(int port) {
        if (!enabled) {
            return fragment();
        }
        // LiveReload client that connects to Spring DevTools LiveReload server
        return inlineScript(
            "(function(){" +
            "if(window.__jwebLR)return;" +
            "window.__jwebLR=true;" +
            "var ws=new WebSocket('ws://'+location.hostname+':" + port + "/livereload');" +
            "ws.onopen=function(){console.log('[JWeb] LiveReload connected')};" +
            "ws.onmessage=function(e){" +
            "var msg=JSON.parse(e.data);" +
            "if(msg.command==='reload'){location.reload()}};" +
            "ws.onclose=function(){" +
            // Reconnect on disconnect
            "setTimeout(function(){" +
            "var s=document.createElement('script');" +
            "s.textContent='('+arguments.callee.toString()+')()';" +
            "document.body.appendChild(s)},1000)};" +
            "})();"
        );
    }

    /**
     * Returns a combined script that uses both LiveReload and SSE.
     * Best of both worlds: LiveReload for static, SSE for Java changes.
     */
    public static Element combinedScript() {
        if (!enabled) {
            return fragment();
        }
        return fragment(
            liveReloadScript(),
            script()
        );
    }

    /**
     * Returns the hot reload script as a string.
     * Optimized for fast reconnection and instant reload.
     */
    public static String clientScript() {
        String reloadLog = debug ? "console.log('[JWeb] Reloading...');" : "";
        String errorLog = debug ? "console.log('[JWeb] Connection lost, polling for server...');" : "";
        String connectedLog = debug ? "console.log('[JWeb] Hot reload connected');" : "";

        // Optimized client script:
        // 1. Uses SSE for instant notification
        // 2. On disconnect, polls rapidly to detect server restart
        // 3. Reloads immediately when server is back
        return "(function(){" +
            "if(window.__jwebHR)return;" +
            "window.__jwebHR=true;" +
            "var lastVersion=null;" +
            "var connect=function(){" +
            "var es=new EventSource('/__jweb_dev/events');" +
            "es.onmessage=function(e){" +
            "var data=JSON.parse(e.data);" +
            "if(lastVersion&&data.version!==lastVersion){" +
            reloadLog +
            "location.reload()}" +
            "lastVersion=data.version};" +
            "es.onerror=function(){" +
            "es.close();" +
            errorLog +
            // Fast polling to detect server restart (50ms intervals)
            "var poll=function(){" +
            "fetch('/__jweb_dev/status',{cache:'no-store'})" +
            ".then(function(r){if(r.ok){location.reload()}})" +
            ".catch(function(){setTimeout(poll,50)})};" +
            "poll()}};" +
            "connect();" +
            connectedLog +
            "})();";
    }

    /**
     * Creates an SSE emitter for a client connection.
     * Called by the dev controller endpoint.
     */
    public static SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send initial version
        try {
            emitter.send(SseEmitter.event()
                .data("{\"version\":" + version.get() + "}"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    private static void notifyClients() {
        String data = "{\"version\":" + version.get() + "}";
        // Use iterator to safely remove while iterating
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(data));
                return false; // Keep this emitter
            } catch (Exception e) {
                // Client disconnected - remove silently
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
                return true; // Remove this emitter
            }
        });
    }

    private static void startWatching() {
        if (watching) return;
        watching = true;

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "jweb-hot-reload-watcher");
            t.setDaemon(true);
            return t;
        });

        executor.submit(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();

                for (String pathStr : watchPaths) {
                    Path path = Paths.get(pathStr);
                    if (Files.exists(path)) {
                        registerRecursively(path, watchService);
                        System.out.println("[JWeb] Watching: " + path.toAbsolutePath());
                    }
                }

                System.out.println("[JWeb] Hot reload enabled");

                while (watching) {
                    WatchKey key = watchService.take();

                    boolean shouldReload = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        Path changed = (Path) event.context();
                        String fileName = changed.toString();

                        // Only reload for relevant file changes
                        if (fileName.endsWith(".java") ||
                            fileName.endsWith(".html") ||
                            fileName.endsWith(".css") ||
                            fileName.endsWith(".js") ||
                            fileName.endsWith(".properties") ||
                            fileName.endsWith(".yml") ||
                            fileName.endsWith(".yaml")) {
                            shouldReload = true;
                            System.out.println("[JWeb] Changed: " + fileName);
                        }

                        // Register new directories
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path fullPath = ((Path) key.watchable()).resolve(changed);
                            if (Files.isDirectory(fullPath)) {
                                registerRecursively(fullPath, watchService);
                            }
                        }
                    }

                    if (shouldReload) {
                        // Small delay to batch multiple rapid changes
                        Thread.sleep(debounceMs);
                        triggerReload();
                    }

                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[JWeb] Hot reload watcher error: " + e.getMessage());
            }
        });
    }

    private static void registerRecursively(Path path, WatchService watchService) throws IOException {
        Files.walk(path)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                try {
                    dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                } catch (IOException e) {
                    // Ignore
                }
            });
    }

    /**
     * Spring initializer to start watching on application startup.
     */
    public static class DevServerInitializer implements ApplicationListener<ContextRefreshedEvent> {
        private static boolean firstStartup = true;

        public DevServerInitializer() {
            DevServer.enabled = true;
            DevServer.startWatching();
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // On restart (not first startup), immediately bump version
            // This ensures browsers reload as soon as server is ready
            if (!firstStartup) {
                System.out.println("[JWeb] Server restarted - notifying browsers");
                triggerReload();
            }
            firstStartup = false;
        }
    }
}
