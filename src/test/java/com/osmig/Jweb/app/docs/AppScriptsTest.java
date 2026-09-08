package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.app.sandbox.SandboxScriptAccess;
import com.osmig.Jweb.app.subheader.SubheaderScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The framework's own app is the proof that the DSL covers what a real page
 * needs: its four client scripts are written entirely in it, and what they
 * generate parses.
 */
class AppScriptsTest {

    private static final List<String> SCRIPT_SOURCES = List.of(
        "src/main/java/com/osmig/Jweb/app/docs/DocsNavScript.java",
        "src/main/java/com/osmig/Jweb/app/docs/CodeCopyScript.java",
        "src/main/java/com/osmig/Jweb/app/subheader/SubheaderScript.java",
        "src/main/java/com/osmig/Jweb/app/sandbox/SandboxScript.java");

    @Test
    void noScriptReachesForTheEscapeHatch() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        List<String> violations = new ArrayList<>();
        for (String rel : SCRIPT_SOURCES) {
            List<String> lines = Files.readAllLines(root.resolve(rel), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("unsafeRaw") || line.contains(".raw(") || line.contains("\"\"\"")) {
                    violations.add(rel + ":" + (i + 1) + "  " + line.strip());
                }
            }
        }
        assertTrue(violations.isEmpty(),
            "the app's client scripts must be pure DSL — no raw JavaScript:\n  "
                + String.join("\n  ", violations));
    }

    @Test
    void everyAppScriptParses() throws Exception {
        for (Map.Entry<String, String> e : scripts().entrySet()) check(e.getKey(), e.getValue());
    }

    @Test
    void theScriptsInstallTheBehaviorsTheyClaim() {
        Map<String, String> js = scripts();

        String nav = js.get("docs-nav");
        assertTrue(nav.contains("JWeb.prefetchOn({selector:'.docs-nav-link'"), nav);
        assertTrue(nav.contains("JWeb.activeLink({selector:'.docs-nav-link'"), nav);

        String copy = js.get("code-copy");
        assertTrue(copy.contains("JWeb.copyText((JWeb.nearest(t,'pre')"), copy);
        assertTrue(copy.contains("text:'Copied!'"), copy);

        String rail = js.get("subheader");
        assertTrue(rail.contains("JWeb.scrollSpy({nav:'#subheader-nav'"), rail);
        assertTrue(rail.contains("linkClass:'subheader-link'"), rail);

        String sandbox = js.get("sandbox");
        assertTrue(sandbox.contains("JWeb.lineGutter({textarea:'#sandbox-editor'"), sandbox);
        assertTrue(sandbox.contains("JWeb.splitPane({handle:'#sandbox-gutter'"), sandbox);
        assertTrue(sandbox.contains("JWeb.relineGutter"), sandbox);
        assertTrue(sandbox.contains("JWeb.markLine"), sandbox);
        assertTrue(sandbox.contains("JWeb.insertText"), sandbox);
    }

    @Test
    void docsLinksCarryTheSwapMarkup() {
        String html = new DocSidebar("state").render().toHtml();
        assertTrue(html.contains("data-swap-get=\"/docs/content?section=state\""), html);
        assertTrue(html.contains("data-swap-target=\".docs-content\""), html);
        assertTrue(html.contains("data-swap-push=\"/docs?section=state\""), html);
        assertTrue(html.contains("data-swap-cache=\"300000\""), html);
        // the active link is marked by class only — an inline colour would
        // outrank .docs-nav-link.active for every later navigation
        assertTrue(html.contains("class=\"docs-nav-link active\""), html);
        assertTrue(html.contains("/docs/tell"), html);
        assertTrue(!html.contains("data-swap-get=\"/docs/tell\""), html);
    }

    @Test
    void anOlderVersionKeepsItsParamOnBothUrls() {
        String html = new DocSidebar("state", "v2.2.3").render().toHtml();
        assertTrue(html.contains("data-swap-get=\"/docs/content?section=state&amp;v=v2.2.3\""), html);
        assertTrue(html.contains("data-swap-push=\"/docs?section=state&amp;v=v2.2.3\""), html);
    }

    private static Map<String, String> scripts() {
        Map<String, String> js = new LinkedHashMap<>();
        js.put("docs-nav", DocsNavScript.build());
        js.put("code-copy", CodeCopyScript.build());
        js.put("subheader", SubheaderScript.build());
        js.put("sandbox", SandboxScriptAccess.build());
        return js;
    }

    private static void check(String name, String source) throws IOException, InterruptedException {
        assumeTrue(hasNode(), "node not on PATH — syntax check skipped");
        Path file = Files.createTempFile(name + "-", ".js");
        try {
            Files.writeString(file, source, StandardCharsets.UTF_8);
            Process p = new ProcessBuilder("node", "--check", file.toString())
                .redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, p.waitFor(), name + " does not parse:\n" + out + "\n--- source ---\n" + source);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static boolean hasNode() {
        try {
            Process p = new ProcessBuilder("node", "--version").redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
