package com.osmig.Jweb.app;

import jweb.Browser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo app in a real headless Chrome through {@code jweb.Browser}: the
 * runtime's script, the WebSocket, a server-side click handler, a bound
 * state, a live region and a client-side action — exactly what a visitor
 * gets. Runs wherever a Chrome can be found ({@code Browser.available()}),
 * which includes GitHub's runners; skipped elsewhere.
 */
@EnabledIf("jweb.Browser#available")
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "jweb.data.enabled=false")
class BrowserTest {

    @Autowired Environment env;

    private String url(String path) {
        return "http://localhost:" + env.getProperty("local.server.port") + path;
    }

    @Test
    void aServerHandlerRunsFromARealClickAndThePagePatchesItself(@TempDir Path dir) throws Exception {
        try (Browser browser = Browser.launch();
             Browser.Page page = browser.open(url("/demo/streaming"))) {
            assertTrue(page.url().endsWith("/demo/streaming"), page.url());
            assertFalse(page.title().isBlank());

            // the streamed block arrives after the shell; the runtime connects its socket
            page.waitFor("#click-me", Duration.ofSeconds(10));
            page.waitForText("#click-count", "0");
            page.waitForText("#click-log", "No clicks yet.");
            page.waitUntil("window.JWeb !== undefined");

            page.click("#click-me");
            page.waitForText("#click-count", "1");
            page.waitForText("#click-log", "Click 1");
            page.click("#click-me");
            page.waitForText("#click-count", "2");
            assertEquals(2, page.count("#click-log li"));

            page.click("#click-reset");
            page.waitForText("#click-count", "0");
            page.waitForText("#click-log", "No clicks yet.");

            // a client-side action (the JavaScript DSL): no round trip, the panel toggles
            page.click("#toggle-details");
            page.waitUntil("document.getElementById('actions-panel').style.display !== ''");

            List<String> errors = page.errors();
            assertTrue(errors.isEmpty(), "the page ran without a script error:\n" + String.join("\n", errors));

            Path shot = page.screenshot(dir.resolve("streaming.png"));
            byte[] png = Files.readAllBytes(shot);
            assertTrue(png.length > 1000 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G', "a PNG came back");

            assertTrue(page.html().contains("id=\"click-count\""));
            assertTrue(page.exists("#click-reset"));
            assertFalse(page.exists("#nothing-here"));
            assertEquals(3, page.eval("1 + 2").asInt());
            AssertionError missing = assertThrows(AssertionError.class, () -> page.text("#nothing-here"));
            assertTrue(missing.getMessage().contains("#nothing-here"));
            AssertionError timedOut = assertThrows(AssertionError.class, () -> page.waitFor("#nothing-here", Duration.ofMillis(200)));
            assertTrue(timedOut.getMessage().startsWith("Timed out"), timedOut.getMessage());
        }
    }

    @Test
    void typingAndKeysAreRealInput() throws Exception {
        try (Browser browser = Browser.launch();
             Browser.Page page = browser.open(url("/contact"))) {
            page.waitFor("form");
            String field = page.exists("#name") ? "#name" : "input[name=name]";
            page.type(field, "Ada");
            assertEquals("Ada", page.value(field));
            page.fill(field, "Grace");
            assertEquals("Grace", page.value(field));
            page.press("Tab");
            assertFalse(page.eval("document.activeElement === document.querySelector('" + field + "')").asBoolean(),
                "Tab moved the focus on");
        }
    }

    @Test
    void aMissingChromeIsAClearError() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Browser.launch(Path.of("/no/such/chrome")));
        assertTrue(e.getMessage().contains("/no/such/chrome"), e.getMessage());
        assertTrue(Browser.chrome().isPresent());
    }
}
