package jweb.js;

import com.osmig.Jweb.framework.js.JWebRuntime;
import jweb.Action;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static jweb.El.*;
import static jweb.Js.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Generated JavaScript has to parse. The client runtime is a Java text block
 * and every behavior emits a call into it, so a stray quote or brace in either
 * would only surface in a browser. Node's {@code --check} parses them here —
 * and is skipped, not failed, where Node is missing.
 */
class GeneratedJsSyntaxTest {

    @Test
    void clientRuntimeParses() throws Exception {
        check("jweb-runtime", JWebRuntime.getScript());
    }

    @Test
    void everyBehaviorParses() throws Exception {
        Map<String, String> snippets = new LinkedHashMap<>();

        snippets.put("copy", js(copy("npm i jweb").feedback("Copied!", 1200)));
        snippets.put("copyFrom", js(copyFrom("pre")
            .trigger(v("t")).feedback("Copied!").failText("Copy failed").feedbackClass("copied")));
        snippets.put("navigate", js(navigate("/docs/content?section=state")
            .target(".docs-content").push("/docs?section=state").cache(300_000)));
        snippets.put("navigate-expr", js(navigate(v("t").dot("href"))
            .target("#main").push(v("t").call("getAttribute", "href")).morph().prefetch()));
        snippets.put("prefetch", js(prefetch(".docs-nav-link")
            .within(".docs-sidebar").onHover().delay(50).cache(60_000)
            .url(v("t").dot("href").replace("/docs?", "/docs/content?"))));
        snippets.put("prefetch-visible", js(prefetch(".card a").onVisible()));
        snippets.put("activeLink", js(activeLink(".docs-nav-link").activeClass("active")));
        snippets.put("scrollSpy", js(scrollSpy("#toc", "h2, h3")
            .within(".docs-content").linkClass("toc-link")
            .hasHeadingsClass("has-headers").host("#rail").offset(80).scrollMargin(24)));
        snippets.put("splitPane", js(splitPane("#gutter", "#code")
            .container("#split").minPercent(20).maxPercent(80).min(120).max(900).persist("split")));
        snippets.put("lineGutter", js(lineGutter("#editor", "#lines")
            .mirror("#mirror").errorClass("errline")));
        snippets.put("relineGutter", js(relineGutter("#editor")));
        snippets.put("markLine", js(markLine("#editor", v("n"))));
        snippets.put("resizeToContent", js(resizeToContent("textarea.grow")));
        snippets.put("insertText", js(insertText("#editor", "    ")));
        snippets.put("syncState", "var x=" + syncState("s1").js());
        snippets.put("onStateChange", js(onStateChange("s1", callback("now", "before").log(v("now")))));
        snippets.put("onPopState", js(onPopState(callback("e").log(v("e").dot("state")))));
        snippets.put("customElement", js(customElement("user-card")
            .observedAttributes("name", "email")
            .shadow()
            .template(div(class_("card"), slot()))
            .connected(callback().log("mounted"))
            .disconnected(callback().log("gone"))
            .attributeChanged(callback("name", "oldValue", "newValue").log(v("newValue")))));
        snippets.put("shadow", "var r=" + attachShadow(v("host")).js()
            + ";var s=" + assignedElements(v("slot")).js() + ";"
            + assignSlot(v("child"), "footer").js() + ";"
            + js(onSlotChange(v("slot"), callback("e").log(v("e")))));
        snippets.put("serviceWorker", js(Pwa.registerServiceWorker("/sw.js")));
        snippets.put("statements", func("f", "e")
            .does(preventDefault(), stopPropagation(), return_())
            .toDecl());

        for (Map.Entry<String, String> e : snippets.entrySet()) {
            check(e.getKey(), e.getValue());
        }
    }

    @Test
    void manifestIsValidJson() {
        String json = Pwa.manifest("JWeb Demo")
            .shortName("JWeb").display("standalone").startUrl("/")
            .themeColor("#4f46e5").background("#ffffff")
            .icon("/icon-192.png", "192x192")
            .json();
        assertEquals("{\"name\":\"JWeb Demo\",\"short_name\":\"JWeb\",\"display\":\"standalone\","
            + "\"start_url\":\"/\",\"theme_color\":\"#4f46e5\",\"background_color\":\"#ffffff\","
            + "\"icons\":[{\"src\":\"/icon-192.png\",\"sizes\":\"192x192\",\"type\":\"image/png\"}]}",
            json);
    }

    private static String js(Action action) {
        return action.build();
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
