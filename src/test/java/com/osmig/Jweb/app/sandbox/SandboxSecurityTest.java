package com.osmig.Jweb.app.sandbox;

import com.osmig.Jweb.app.sandbox.SandboxDsl.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security and correctness contract of the sandbox interpreter: user code is
 * parsed and evaluated against an explicit whitelist only — no compilation,
 * no reflection, bounded work — and every string that reaches HTML goes
 * through the framework's escaping.
 */
class SandboxSecurityTest {

    private static String render(String expr) {
        Result r = SandboxDsl.run(expr);
        assertTrue(r.isOk(), () -> "expected ok but got: " + r.error());
        return r.element().toHtml();
    }

    // ==================== escaping ====================

    @Test
    void scriptTagInTextLiteralIsEscaped() {
        String html = render("div(text(\"<script>alert(1)</script>\"))");
        assertFalse(html.contains("<script>"), "raw script tag must never survive");
        assertTrue(html.contains("&lt;script&gt;"));
    }

    @Test
    void bareStringChildIsEscapedToo() {
        String html = render("p(\"<img src=x onerror=alert(1)>\")");
        assertFalse(html.contains("<img"));
    }

    @Test
    void fullFragmentEscapesUserCodeEverywhere() {
        String html = SandboxPanes.renderFragment("home",
            "div(text(\"</textarea><script>alert(1)</script>\"))").toHtml();
        assertFalse(html.contains("<script>alert"));
    }

    // ==================== whitelist boundaries ====================

    @Test
    void javaClassesAreUnreachable() {
        for (String probe : new String[]{
            "Runtime(exec(\"rm\"))",
            "java(lang(System(exit(1))))",
            "getClass()",
            "div(style().getClass())",
            "forName(\"java.lang.Runtime\")"}) {
            Result r = SandboxDsl.run(probe);
            assertFalse(r.isOk(), probe + " must not evaluate");
        }
    }

    @Test
    void hexRejectsNonColors() {
        Result r = SandboxDsl.run("div(style().color(hex(\"url(javascript:alert(1))\")))");
        assertFalse(r.isOk());
        assertTrue(r.error().contains("hex color"));
    }

    @Test
    void boxShadowRejectsCssEscapes() {
        Result r = SandboxDsl.run("div(style().boxShadow(\"0 0 url('http://evil')\"))");
        assertFalse(r.isOk());
        // and legit shadows pass
        assertTrue(SandboxDsl.run(
            "div(style().boxShadow(\"0 4px 14px rgba(15, 23, 42, 0.08)\"))").isOk());
    }

    @Test
    void chainOnElementsIsRejected() {
        Result r = SandboxDsl.run("div(text(\"x\")).toString()");
        assertFalse(r.isOk());
    }

    // ==================== resource caps ====================

    @Test
    void deepNestingErrorsInsteadOfOverflowing() {
        String bomb = "div(".repeat(80) + "text(\"x\")" + ")".repeat(80);
        Result r = SandboxDsl.run(bomb);
        assertFalse(r.isOk());
        assertTrue(r.error().contains("deep"));
    }

    @Test
    void oversizedSourceIsRejected() {
        Result r = SandboxDsl.run("div(text(\"" + "x".repeat(SandboxDsl.MAX_SOURCE) + "\"))");
        assertFalse(r.isOk());
    }

    @Test
    void elementCountIsCapped() {
        StringBuilder sb = new StringBuilder("div(");
        for (int i = 0; i < 250; i++) sb.append("span(),");
        sb.append("text(\"x\"))");
        Result r = SandboxDsl.run(sb.toString());
        assertFalse(r.isOk());
    }

    // ==================== compile-style errors ====================

    @Test
    void unknownStyleMethodSuggestsTheRealOne() {
        Result r = SandboxDsl.run("div(style().colr(white))");
        assertFalse(r.isOk());
        assertTrue(r.error().contains("did you mean color"), r.error());
    }

    @Test
    void errorsCarryLineNumbers() {
        Result r = SandboxDsl.run("div(\n  text(\"ok\"),\n  sparkles()\n)");
        assertFalse(r.isOk());
        assertEquals(3, r.errorLine());
    }

    @Test
    void classWrapperIsAcceptedViaReturnExtraction() {
        Result r = SandboxDsl.run(SandboxFiles.byId("home").source());
        assertTrue(r.isOk(), () -> String.valueOf(r.error()));
        assertTrue(r.element().toHtml().contains("Hello, JWeb!"));
    }

    @Test
    void allDslStartersCompileCleanly() {
        for (var f : SandboxFiles.FILES.values()) {
            if (f.mode() != SandboxFiles.Mode.DSL) continue;
            Result r = SandboxDsl.run(f.source());
            assertTrue(r.isOk(), f.id() + " starter failed: " + r.error());
        }
    }

    // ==================== knobs ====================

    @Test
    void knobsAreDerivedFromCodeWithCorrectOffsets() {
        String src = SandboxFiles.byId("home").source();
        Result r = SandboxDsl.run(src);
        assertTrue(r.isOk());
        assertFalse(r.knobs().isEmpty());
        // Every knob's offset span must reproduce its reported value
        for (var k : r.knobs()) {
            assertEquals(k.value().toLowerCase(),
                src.substring(k.start(), k.start() + k.len()).toLowerCase(),
                "offset mismatch for knob " + k.label());
        }
    }

    @Test
    void patchingAtKnobOffsetsRoundTrips() {
        String src = SandboxFiles.byId("home").source();
        Result r = SandboxDsl.run(src);
        var title = r.knobs().stream().filter(k -> k.kind().equals("text")).findFirst().orElseThrow();
        String patched = src.substring(0, title.start()) + "Knobs work!"
            + src.substring(title.start() + title.len());
        String html = render(patched);
        assertTrue(html.contains("Knobs work!"));
    }

    // ==================== static files ====================

    @Test
    void staticFilesNeverEvaluateUserCode() {
        String html = SandboxPanes.renderFragment("app",
            "div(text(\"should be ignored\"))").toHtml();
        assertFalse(html.contains("should be ignored"));
        assertTrue(html.contains("spring-boot:run"), "static preview expected");
    }

    @Test
    void unknownFileIdFallsBackToDefault() {
        assertEquals("home", SandboxFiles.byId("../../etc").id());
        assertEquals("home", SandboxFiles.byId(null).id());
        assertDoesNotThrow(() -> SandboxPanes.renderFragment("💥", null).toHtml());
    }

    // ==================== error fragments keep the last good view ====================

    @Test
    void errorFragmentContainsOnlyStatus() {
        String html = SandboxPanes.renderFragment("home", "div(oops").toHtml();
        assertTrue(html.contains("rx-status"));
        assertFalse(html.contains("rx-view"), "broken code must not replace the preview");
        assertTrue(html.contains("data-ok=\"0\""));
    }

    @Test
    void okFragmentContainsAllThreeParts() {
        String html = SandboxPanes.renderFragment("home", null).toHtml();
        assertTrue(html.contains("rx-status"));
        assertTrue(html.contains("rx-view"));
        assertTrue(html.contains("rx-dynbar"));
        assertTrue(html.contains("data-ok=\"1\""));
    }
}
