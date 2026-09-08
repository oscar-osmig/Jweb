package com.osmig.Jweb.framework.js;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The client runtime is a Java text block; a stray brace only surfaces when
 * a browser refuses it. Parse it with Node's {@code --check} where Node
 * exists, and pin the live-region protocol's attribute names either way.
 */
class JWebRuntimeSyntaxTest {

    @Test
    void runtimeScriptParses() throws IOException, InterruptedException {
        assumeTrue(hasNode(), "node not on PATH — syntax check skipped");
        Path js = Files.createTempFile("jweb-runtime", ".js");
        try {
            Files.writeString(js, JWebRuntime.getScript(), StandardCharsets.UTF_8);
            Process p = new ProcessBuilder("node", "--check", js.toString()).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, p.waitFor(), "runtime.js does not parse:\n" + out);
        } finally {
            Files.deleteIfExists(js);
        }
    }

    @Test
    void liveProtocolLivesInOneFunction() {
        String script = JWebRuntime.getScript();
        int start = script.indexOf("initLive:function(){");
        assertTrue(start > 0, "initLive missing");
        int end = script.indexOf("\n            },", start);
        String fn = script.substring(start, end);
        // Everything the server emits (StateBinding / LiveRegion) is consumed here
        assertTrue(fn.contains("data-state-attr"), "bindAttr protocol");
        assertTrue(fn.contains("data-state-class"), "bindClass protocol");
        assertTrue(fn.contains("[data-live=\""), "live region lookup");
        assertTrue(fn.contains("morphNode"), "regions morph, not replace");
        assertTrue(fn.contains("jweb:stateChange"), "bindings follow state updates");
        assertTrue(script.contains("this.initLive();"), "installed at init");
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
