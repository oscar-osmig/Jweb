package jweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A real browser for a test: headless Chrome, driven over its own DevTools
 * protocol by the framework — no WebDriver, no Playwright, nothing to add.
 * Where {@link RenderedPage} runs the event loop without a browser, this
 * runs the page in one: the runtime's script, the WebSocket, the DOM patches,
 * the CSS, exactly as a visitor gets them.
 *
 * <pre>{@code
 * @EnabledIf("jweb.Browser#available")
 * @SpringBootTest(webEnvironment = RANDOM_PORT)
 * class TodosBrowserTest {
 *     @LocalServerPort int port;
 *
 *     @Test
 *     void addsATodoAndSeesItWithoutAReload() throws Exception {
 *         try (Browser browser = Browser.launch();
 *              Browser.Page page = browser.open("http://localhost:" + port + "/todos")) {
 *             page.type("#new-todo", "Write tests");
 *             page.click("#add");
 *             page.waitForText("ul", "Write tests");
 *             assertTrue(page.console().stream().noneMatch(line -> line.startsWith("error")));
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>{@link #available()} is whether a Chrome or Chromium can be found —
 * {@code JWEB_CHROME} or {@code CHROME_PATH}, the usual install locations,
 * the {@code PATH} — so a suite gates the browser tests on it and runs them
 * where there is one (GitHub's runners have Chrome). Clicks and keys are
 * real input events; {@code waitFor} polls the DOM up to five seconds, the
 * time a patch takes to arrive over the socket and a little more.</p>
 */
public final class Browser implements AutoCloseable {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final Process process;
    private final Path profile;
    private final Cdp cdp;

    private Browser(Process process, Path profile, Cdp cdp) {
        this.process = process;
        this.profile = profile;
        this.cdp = cdp;
    }

    // ==================== finding and launching Chrome ====================

    /** The Chrome or Chromium to launch, when one can be found. */
    public static Optional<Path> chrome() {
        List<String> candidates = new ArrayList<>();
        for (String env : List.of("JWEB_CHROME", "CHROME_PATH", "CHROME_BIN")) {
            String value = System.getenv(env);
            if (value != null && !value.isBlank()) candidates.add(value);
        }
        candidates.addAll(List.of(
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/Applications/Chromium.app/Contents/MacOS/Chromium",
            "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
            "/usr/bin/google-chrome", "/usr/bin/google-chrome-stable", "/usr/bin/chromium", "/usr/bin/chromium-browser",
            "/snap/bin/chromium", "/opt/google/chrome/chrome",
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"));
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.isExecutable(path)) return Optional.of(path);
        }
        String pathVar = System.getenv("PATH");
        if (pathVar != null) {
            for (String dir : pathVar.split(java.io.File.pathSeparator)) {
                for (String name : List.of("google-chrome", "google-chrome-stable", "chromium", "chromium-browser", "chrome")) {
                    Path path = Path.of(dir, name);
                    if (Files.isExecutable(path)) return Optional.of(path);
                }
            }
        }
        return Optional.empty();
    }

    /** Whether a browser can be launched here — the condition to gate browser tests on. */
    public static boolean available() {
        return chrome().isPresent();
    }

    /** Launches a headless Chrome found by {@link #chrome()}. */
    public static Browser launch() {
        return launch(chrome().orElseThrow(() -> new IllegalStateException(
            "No Chrome or Chromium found: install one, or point JWEB_CHROME at it")));
    }

    /** Launches the Chrome at the path, headless, with a throwaway profile. */
    public static Browser launch(Path chrome) {
        try {
            Path profile = Files.createTempDirectory("jweb-browser-");
            List<String> command = new ArrayList<>(List.of(chrome.toString(),
                "--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
                "--no-first-run", "--no-default-browser-check", "--disable-extensions",
                "--remote-debugging-port=0", "--user-data-dir=" + profile,
                "--window-size=1280,900", "about:blank"));
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(profile.resolve("chrome.log").toFile())
                .start();
            String endpoint = devToolsEndpoint(process, profile);
            Cdp cdp = Cdp.connect(endpoint);
            Log.debug("Browser: Chrome {} at {}", process.pid(), endpoint);
            return new Browser(process, profile, cdp);
        } catch (IOException e) {
            throw new IllegalStateException("Could not launch Chrome at " + chrome + ": " + e.getMessage(), e);
        }
    }

    /** Chrome writes DevToolsActivePort in the profile: the port, then the browser socket's path. */
    private static String devToolsEndpoint(Process process, Path profile) throws IOException {
        Path file = profile.resolve("DevToolsActivePort");
        long deadline = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                String log = Files.exists(profile.resolve("chrome.log")) ? Files.readString(profile.resolve("chrome.log")) : "";
                throw new IOException("Chrome exited with " + process.exitValue() + ": " + log.strip());
            }
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                if (lines.size() >= 2 && !lines.get(0).isBlank()) {
                    return "ws://127.0.0.1:" + lines.get(0).trim() + lines.get(1).trim();
                }
            }
            sleep(50);
        }
        process.destroyForcibly();
        throw new IOException("Chrome did not open its DevTools port within 20 seconds");
    }

    /** Opens a new tab at the URL and waits for it to load. */
    public Page open(String url) {
        JsonNode created = cdp.call(null, "Target.createTarget", Map.of("url", "about:blank"));
        String targetId = created.get("targetId").asText();
        JsonNode attached = cdp.call(null, "Target.attachToTarget", Map.of("targetId", targetId, "flatten", true));
        String sessionId = attached.get("sessionId").asText();
        Page page = new Page(cdp, targetId, sessionId);
        page.enable();
        page.navigate(url);
        return page;
    }

    /** Closes every tab and the browser. */
    @Override
    public void close() {
        try {
            cdp.close();
        } finally {
            process.destroy();
            try {
                if (!process.waitFor(3, TimeUnit.SECONDS)) process.destroyForcibly();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
            deleteQuietly(profile);
        }
    }

    private static void deleteQuietly(Path dir) {
        try (var walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // a profile file Chrome still holds; the OS reclaims the temp dir
                }
            });
        } catch (IOException ignored) {
            // nothing to clean
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== a page ====================

    /** One tab. Selectors are CSS selectors; every wait has a five-second default. */
    public static final class Page implements AutoCloseable {

        private final Cdp cdp;
        private final String targetId;
        private final String sessionId;
        private final List<String> console = new CopyOnWriteArrayList<>();
        private volatile CompletableFuture<Void> loaded = new CompletableFuture<>();

        Page(Cdp cdp, String targetId, String sessionId) {
            this.cdp = cdp;
            this.targetId = targetId;
            this.sessionId = sessionId;
        }

        private void enable() {
            cdp.on(sessionId, "Page.loadEventFired", event -> loaded.complete(null));
            cdp.on(sessionId, "Runtime.consoleAPICalled", event -> {
                StringBuilder line = new StringBuilder(event.path("type").asText("log"));
                for (JsonNode arg : event.path("args")) {
                    line.append(' ').append(arg.has("value") ? arg.get("value").asText() : arg.path("description").asText(""));
                }
                console.add(line.toString());
            });
            cdp.on(sessionId, "Runtime.exceptionThrown", event -> {
                JsonNode details = event.path("exceptionDetails");
                String text = details.path("exception").path("description").asText(details.path("text").asText("exception"));
                console.add("error " + text);
            });
            call("Page.enable", Map.of());
            call("Runtime.enable", Map.of());
        }

        /** Goes to the URL and waits for the load event. */
        public Page navigate(String url) {
            loaded = new CompletableFuture<>();
            JsonNode result = call("Page.navigate", Map.of("url", url));
            if (result.has("errorText")) {
                throw new IllegalStateException("Could not open " + url + ": " + result.get("errorText").asText());
            }
            await(loaded, DEFAULT_TIMEOUT.plusSeconds(10), "load of " + url);
            return this;
        }

        public String url() {
            return eval("location.href").asText();
        }

        public String title() {
            return eval("document.title").asText();
        }

        /** The document as the browser has it now, patches applied. */
        public String html() {
            return eval("document.documentElement.outerHTML").asText();
        }

        /** The text of the first element the selector matches; fails when there is none. */
        public String text(String selector) {
            JsonNode node = eval("(function(){var e=document.querySelector(" + quote(selector) + ");return e?e.textContent:null})()");
            if (node.isNull()) throw new AssertionError("No element matches " + selector);
            return node.asText();
        }

        /** The value of an input the selector matches. */
        public String value(String selector) {
            JsonNode node = eval("(function(){var e=document.querySelector(" + quote(selector) + ");return e?e.value:null})()");
            if (node.isNull()) throw new AssertionError("No element matches " + selector);
            return node.asText();
        }

        public boolean exists(String selector) {
            return eval("document.querySelector(" + quote(selector) + ") !== null").asBoolean();
        }

        public int count(String selector) {
            return eval("document.querySelectorAll(" + quote(selector) + ").length").asInt();
        }

        /** Clicks the element with a real mouse event at its centre. */
        public Page click(String selector) {
            JsonNode box = eval("(function(){var e=document.querySelector(" + quote(selector) + ");"
                + "if(!e)return null;e.scrollIntoView({block:'center'});var r=e.getBoundingClientRect();"
                + "return {x:r.left+r.width/2,y:r.top+r.height/2}})()");
            if (box.isNull()) throw new AssertionError("No element matches " + selector);
            double x = box.get("x").asDouble();
            double y = box.get("y").asDouble();
            call("Input.dispatchMouseEvent", Map.of("type", "mouseMoved", "x", x, "y", y));
            call("Input.dispatchMouseEvent", Map.of("type", "mousePressed", "x", x, "y", y, "button", "left", "clickCount", 1));
            call("Input.dispatchMouseEvent", Map.of("type", "mouseReleased", "x", x, "y", y, "button", "left", "clickCount", 1));
            return this;
        }

        /** Focuses the element and types the text as key input (input events fire). */
        public Page type(String selector, String text) {
            focus(selector);
            call("Input.insertText", Map.of("text", text));
            return this;
        }

        /** Clears an input and types into it. */
        public Page fill(String selector, String text) {
            focus(selector);
            eval("(function(){var e=document.activeElement;if(e){e.value='';e.dispatchEvent(new Event('input',{bubbles:true}))}})()");
            call("Input.insertText", Map.of("text", text));
            return this;
        }

        /** Presses a key on the focused element: "Enter", "Tab", "Escape", "a". */
        public Page press(String key) {
            Map<String, Object> down = new LinkedHashMap<>();
            down.put("type", "keyDown");
            down.put("key", key);
            if (key.length() == 1) down.put("text", key);
            if (key.equals("Enter")) {
                down.put("text", "\r");
                down.put("code", "Enter");
                down.put("windowsVirtualKeyCode", 13);
            }
            call("Input.dispatchKeyEvent", down);
            call("Input.dispatchKeyEvent", Map.of("type", "keyUp", "key", key));
            return this;
        }

        /** Submits the form the selector matches (or the form around the element). */
        public Page submit(String selector) {
            JsonNode ok = eval("(function(){var e=document.querySelector(" + quote(selector) + ");if(!e)return false;"
                + "var f=e.tagName==='FORM'?e:e.form||e.closest('form');if(!f)return false;"
                + "if(f.requestSubmit)f.requestSubmit();else f.submit();return true})()");
            if (!ok.asBoolean()) throw new AssertionError("No form matches " + selector);
            return this;
        }

        private void focus(String selector) {
            JsonNode ok = eval("(function(){var e=document.querySelector(" + quote(selector) + ");if(!e)return false;e.focus();return document.activeElement===e})()");
            if (!ok.asBoolean()) throw new AssertionError("Could not focus " + selector);
        }

        /** Waits for an element matching the selector. */
        public Page waitFor(String selector) {
            return waitFor(selector, DEFAULT_TIMEOUT);
        }

        public Page waitFor(String selector, Duration timeout) {
            poll(() -> exists(selector), timeout, "an element matching " + selector);
            return this;
        }

        /** Waits until the element's text contains the given text. */
        public Page waitForText(String selector, String text) {
            return waitForText(selector, text, DEFAULT_TIMEOUT);
        }

        public Page waitForText(String selector, String text, Duration timeout) {
            poll(() -> exists(selector) && text(selector).contains(text), timeout, "\"" + text + "\" in " + selector);
            return this;
        }

        /** Waits until the page's HTML contains the text. */
        public Page waitForHtml(String text) {
            poll(() -> html().contains(text), DEFAULT_TIMEOUT, "\"" + text + "\" in the page");
            return this;
        }

        /** Waits until the expression is truthy. */
        public Page waitUntil(String expression) {
            poll(() -> eval(expression).asBoolean(), DEFAULT_TIMEOUT, expression);
            return this;
        }

        private void poll(Supplier<Boolean> condition, Duration timeout, String what) {
            long deadline = System.currentTimeMillis() + timeout.toMillis();
            while (true) {
                if (condition.get()) return;
                if (System.currentTimeMillis() > deadline) {
                    throw new AssertionError("Timed out after " + timeout.toMillis() + " ms waiting for " + what
                        + (console.isEmpty() ? "" : "\nconsole: " + String.join("\n", console)));
                }
                sleep(50);
            }
        }

        /** Runs the expression in the page and returns its value as JSON. */
        public JsonNode eval(String expression) {
            JsonNode result = call("Runtime.evaluate", Map.of(
                "expression", expression, "returnByValue", true, "awaitPromise", true));
            if (result.has("exceptionDetails")) {
                JsonNode details = result.get("exceptionDetails");
                throw new IllegalStateException("Script failed: " + details.path("exception").path("description")
                    .asText(details.path("text").asText("")) + "\n" + expression);
            }
            JsonNode value = result.path("result").path("value");
            return value.isMissingNode() ? Json.parseTree("null") : value;
        }

        /** The console so far: "log …", "warn …", "error …" lines. */
        public List<String> console() {
            return List.copyOf(console);
        }

        /** The console's errors and uncaught exceptions. */
        public List<String> errors() {
            return console.stream().filter(line -> line.startsWith("error")).toList();
        }

        /** Saves a PNG of the viewport. */
        public Path screenshot(Path file) {
            JsonNode shot = call("Page.captureScreenshot", Map.of("format", "png"));
            try {
                Files.createDirectories(file.toAbsolutePath().getParent());
                Files.write(file, Base64.getDecoder().decode(shot.get("data").asText()));
            } catch (IOException e) {
                throw new IllegalStateException("Could not write " + file, e);
            }
            return file;
        }

        /** Sets a cookie on the page's origin. */
        public Page cookie(String name, String value) {
            call("Network.setCookie", Map.of("name", name, "value", value, "url", url()));
            return this;
        }

        public Page reload() {
            loaded = new CompletableFuture<>();
            call("Page.reload", Map.of());
            await(loaded, DEFAULT_TIMEOUT.plusSeconds(10), "reload");
            return this;
        }

        private JsonNode call(String method, Map<String, Object> params) {
            return cdp.call(sessionId, method, params);
        }

        @Override
        public void close() {
            try {
                cdp.call(null, "Target.closeTarget", Map.of("targetId", targetId));
            } catch (RuntimeException ignored) {
                // the browser may be gone already
            }
            cdp.forget(sessionId);
        }

        private static String quote(String s) {
            return Json.stringify(s);
        }

        private static <T> T await(CompletableFuture<T> future, Duration timeout, String what) {
            try {
                return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new AssertionError("Timed out waiting for " + what);
            } catch (ExecutionException e) {
                throw new IllegalStateException(what + " failed", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted waiting for " + what, e);
            }
        }
    }

    // ==================== the DevTools protocol over a WebSocket ====================

    /** One connection to Chrome: commands with ids, replies by id, events by method and session. */
    static final class Cdp implements WebSocket.Listener {

        private final AtomicInteger ids = new AtomicInteger();
        private final Map<Integer, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();
        private final Map<String, List<Consumer<JsonNode>>> listeners = new ConcurrentHashMap<>();
        private final StringBuilder partial = new StringBuilder();
        private WebSocket socket;

        static Cdp connect(String endpoint) throws IOException {
            Cdp cdp = new Cdp();
            try {
                cdp.socket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(endpoint), cdp)
                    .get(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted connecting to Chrome", e);
            } catch (ExecutionException | TimeoutException e) {
                throw new IOException("Could not connect to Chrome's DevTools at " + endpoint + ": " + e.getMessage(), e);
            }
            return cdp;
        }

        JsonNode call(String sessionId, String method, Map<String, Object> params) {
            int id = ids.incrementAndGet();
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("id", id);
            message.put("method", method);
            message.put("params", params);
            if (sessionId != null) message.put("sessionId", sessionId);
            CompletableFuture<JsonNode> reply = new CompletableFuture<>();
            pending.put(id, reply);
            socket.sendText(Json.stringify(message), true).join();
            try {
                JsonNode response = reply.get(30, TimeUnit.SECONDS);
                if (response.has("error")) {
                    throw new IllegalStateException(method + " failed: " + response.get("error").path("message").asText());
                }
                return response.path("result");
            } catch (TimeoutException e) {
                pending.remove(id);
                throw new IllegalStateException(method + " got no reply from Chrome in 30 seconds");
            } catch (ExecutionException e) {
                throw new IllegalStateException(method + " failed", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted waiting for " + method, e);
            }
        }

        void on(String sessionId, String method, Consumer<JsonNode> listener) {
            listeners.computeIfAbsent(sessionId + "|" + method, k -> new CopyOnWriteArrayList<>()).add(listener);
        }

        void forget(String sessionId) {
            listeners.keySet().removeIf(key -> key.startsWith(sessionId + "|"));
        }

        void close() {
            try {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "done").get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                socket.abort();
            }
            pending.values().forEach(f -> f.completeExceptionally(new IllegalStateException("Browser closed")));
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                String text = partial.toString();
                partial.setLength(0);
                try {
                    handle(Json.parseTree(text));
                } catch (RuntimeException e) {
                    Log.warn("Browser: could not read a DevTools message: {}", e.getMessage());
                }
            }
            webSocket.request(1);
            return null;
        }

        private void handle(JsonNode message) {
            if (message.has("id")) {
                CompletableFuture<JsonNode> reply = pending.remove(message.get("id").asInt());
                if (reply != null) reply.complete(message);
                return;
            }
            String method = message.path("method").asText("");
            String sessionId = message.path("sessionId").asText("null");
            List<Consumer<JsonNode>> list = listeners.get(sessionId + "|" + method);
            if (list != null) {
                JsonNode params = message.path("params");
                for (Consumer<JsonNode> listener : list) listener.accept(params);
            }
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            pending.values().forEach(f -> f.completeExceptionally(error));
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            pending.values().forEach(f -> f.completeExceptionally(new IllegalStateException("Chrome closed the connection: " + reason)));
            return null;
        }

        @SuppressWarnings("unused")
        private static String utf8(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
