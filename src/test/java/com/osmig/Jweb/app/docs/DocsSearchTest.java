package com.osmig.Jweb.app.docs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The docs search: an index built from the rendered sections, ranked hits
 * that need every term, and the fragment the sidebar box shows.
 */
class DocsSearchTest {

    @BeforeAll
    static void freshIndex() {
        DocsSearch.reset();
    }

    @Test
    void theIndexCoversEverySection() {
        List<DocsSearch.Entry> index = DocsSearch.index();
        assertTrue(index.size() > 60, "entries: " + index.size());
        Set<String> sections = index.stream().map(DocsSearch.Entry::section).collect(Collectors.toSet());
        assertEquals(DocSidebar.SECTIONS.keySet(), sections, "every nav section is indexed");
        assertTrue(index.stream().anyMatch(e -> e.anchor() != null), "headings with ids give anchors");
        assertTrue(index.stream().allMatch(e -> !e.heading().isBlank()), "every entry has a heading");
    }

    @Test
    void termsDropPunctuationAndCase() {
        assertEquals(List.of("three", "patch", "x"), DocsSearch.terms("Three.patch(x)"));
        assertEquals(List.of("usestate"), DocsSearch.terms("  useState  "));
        assertEquals(List.of(), DocsSearch.terms(""));
        assertEquals(List.of(), DocsSearch.terms(null));
    }

    @Test
    void aStateQueryRanksTheStateSectionFirst() {
        List<DocsSearch.Hit> hits = DocsSearch.search("useState", 8);
        assertFalse(hits.isEmpty());
        assertEquals("state", hits.get(0).entry().section(), hits.get(0).entry().toString());
        assertTrue(hits.get(0).excerpt().toLowerCase().contains("usestate"), hits.get(0).excerpt());
        assertTrue(hits.size() <= 8);
    }

    @Test
    void everyTermMustMatch() {
        assertTrue(DocsSearch.search("useState zzzqqqxx", 8).isEmpty());
        assertTrue(DocsSearch.search("", 8).isEmpty());
        assertFalse(DocsSearch.search("three patch", 8).isEmpty(), "both words occur in the 3D section");
    }

    @Test
    void theFragmentLinksHitsAndMarksTerms() {
        String html = DocsSearch.results("useState", null).toHtml();
        assertTrue(html.contains("class=\"" + DocsSearch.RESULTS_LIST.name() + "\""), html.substring(0, 200));
        assertTrue(html.contains("href=\"/docs?section=state"), "links to the section: " + html.substring(0, 300));
        assertTrue(html.contains("<mark>useState</mark>"), "the term is marked as it appears");

        String old = DocsSearch.results("useState", "v2.2.3").toHtml();
        assertTrue(old.contains("v=v2.2.3"), "an older docs version rides the link: " + old.substring(0, 300));

        assertEquals("", DocsSearch.results("   ", null).toHtml(), "a blank query clears the box");
        String none = DocsSearch.results("zzzqqqxx", null).toHtml();
        assertTrue(none.contains("No matches for"), none);
        assertTrue(none.contains("zzzqqqxx"), none);
    }

    @Test
    void excerptsWindowAroundTheFirstTerm() {
        String text = "a ".repeat(80) + "needle sits here " + "b ".repeat(80);
        String excerpt = DocsSearch.excerpt(text, List.of("needle"));
        assertTrue(excerpt.startsWith("…"), excerpt);
        assertTrue(excerpt.endsWith("…"), excerpt);
        assertTrue(excerpt.contains("needle sits here"), excerpt);
        assertTrue(excerpt.length() < 200, excerpt);

        assertEquals("short text", DocsSearch.excerpt("short text", List.of("text")));
    }

    @Test
    void theScriptParsesAndTalksToTheEndpoint() throws IOException, InterruptedException {
        String js = DocsSearchScript.build(DocVersions.latest()).build();
        assertTrue(js.contains("/docs/search?q="), js);
        assertTrue(js.contains("encodeURIComponent"), js);
        assertTrue(js.contains(DocSidebar.SEARCH_INPUT.name()), js);
        assertTrue(js.contains(DocSidebar.SEARCH_RESULTS.name()), js);

        String versioned = DocsSearchScript.build("v2.2.3").build();
        assertTrue(versioned.contains("/docs/search?v=v2.2.3&q="), versioned);

        assumeTrue(hasNode(), "node not on PATH — syntax check skipped");
        Path file = Files.createTempFile("docs-search", ".js");
        try {
            Files.writeString(file, js, StandardCharsets.UTF_8);
            Process p = new ProcessBuilder("node", "--check", file.toString()).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, p.waitFor(), "script does not parse:\n" + out + "\n--- source ---\n" + js);
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
