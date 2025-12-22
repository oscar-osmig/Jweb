package com.osmig.Jweb.framework.dev;

import com.osmig.Jweb.framework.core.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 *
 * <p>Setup in application.yaml:</p>
 * <pre>
 * jweb:
 *   dev:
 *     hot-reload: true
 *     watch-paths: src/main/java,src/main/resources
 * </pre>
 *
 * <p>Or in application.properties:</p>
 * <pre>
 * jweb.dev.hot-reload=true
 * jweb.dev.watch-paths=src/main/java,src/main/resources
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
    private static String[] watchPaths = {"src/main/java", "src/main/resources"};

    @Value("${jweb.dev.watch-paths:src/main/java,src/main/resources}")
    public void setWatchPathsFromConfig(String paths) {
        if (paths != null && !paths.isBlank()) {
            watchPaths = paths.split(",");
        }
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
     * Returns the hot reload script as a string.
     */
    public static String clientScript() {
        return "(function(){" +
            "var es=new EventSource('/__jweb_dev/events');" +
            "var lastVersion=null;" +
            "es.onmessage=function(e){" +
            "var data=JSON.parse(e.data);" +
            "if(lastVersion&&data.version!==lastVersion){" +
            "console.log('[JWeb] Reloading...');" +
            "location.reload()}" +
            "lastVersion=data.version};" +
            "es.onerror=function(){" +
            "console.log('[JWeb] Connection lost, reconnecting...');" +
            "setTimeout(function(){location.reload()},2000)};" +
            "console.log('[JWeb] Hot reload connected');" +
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
                        Thread.sleep(100);
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
    public static class DevServerInitializer {
        public DevServerInitializer() {
            DevServer.enabled = true;
            DevServer.startWatching();
        }
    }
}
