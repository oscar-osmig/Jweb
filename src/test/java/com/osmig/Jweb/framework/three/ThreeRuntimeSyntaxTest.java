package com.osmig.Jweb.framework.three;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The client interpreter is a Java text block, so a stray quote or brace
 * only surfaces when a browser refuses to parse it. This parses it with
 * Node's {@code --check} on machines that have Node; elsewhere it is
 * skipped rather than failed.
 */
class ThreeRuntimeSyntaxTest {

    @Test
    void runtimeScriptParses() throws IOException, InterruptedException {
        assumeTrue(hasNode(), "node not on PATH — syntax check skipped");
        Path js = Files.createTempFile("three-runtime", ".js");
        try {
            Files.writeString(js, ThreeRuntime.getScript(), StandardCharsets.UTF_8);
            Process p = new ProcessBuilder("node", "--check", js.toString())
                .redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, p.waitFor(), "three-runtime.js does not parse:\n" + out);
        } finally {
            Files.deleteIfExists(js);
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
