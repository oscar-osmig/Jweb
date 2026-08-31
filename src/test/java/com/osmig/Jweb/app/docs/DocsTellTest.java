package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.app.docs.DocsTell.Topic;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract of {@code /docs/tell}: the document an AI assistant grounds on must
 * be complete, version-stamped, splittable, and free of the sources that would
 * mislead it.
 *
 * <p>The load-bearing test here is {@link #everyTopicResolvesOnTheClasspath()} —
 * the guides reach the classpath only through a {@code copy-resources} execution
 * in pom.xml, so a packaging change can drop them while everything still
 * compiles. Without this test that failure ships as a document which looks
 * whole and is missing half its content.
 */
class DocsTellTest {

    // ==================== completeness ====================

    @Test
    void everyTopicResolvesOnTheClasspath() {
        // DocsTell.read throws for a missing resource rather than skipping it,
        // so assembling the whole document is the check.
        String all = assertDoesNotThrow(DocsTell::full,
            "a documentation resource is missing — check the copy-readme execution in pom.xml");
        assertFalse(all.isBlank());
    }

    @Test
    void fullDocumentCarriesEveryTopicInOrder() {
        String all = DocsTell.full();
        int previous = -1;
        for (Topic t : DocsTell.topics()) {
            int begin = all.indexOf("BEGIN " + t.id() + " ");
            assertTrue(begin > 0, () -> "missing section for topic: " + t.id());
            assertTrue(all.contains("END " + t.id() + " "),
                () -> "unterminated section for topic: " + t.id());
            assertTrue(begin > previous,
                () -> "topic out of declared order: " + t.id());
            previous = begin;
        }
    }

    @Test
    void everySectionHasRealContentNotJustItsBanner() {
        for (Topic t : DocsTell.topics()) {
            String one = DocsTell.topic(t.id());
            assertNotNull(one, () -> "no document for topic: " + t.id());
            int begin = one.indexOf("BEGIN " + t.id());
            int end = one.indexOf("END " + t.id());
            assertTrue(end - begin > 500,
                () -> "section looks empty for topic: " + t.id() + " (" + (end - begin) + " chars)");
        }
    }

    @Test
    void topicIdsAreUnique() {
        Set<String> seen = new HashSet<>();
        for (Topic t : DocsTell.topics()) {
            assertTrue(seen.add(t.id()), () -> "duplicate topic id: " + t.id());
        }
    }

    // ==================== version stamping ====================

    @Test
    void versionIsResolvedNotUnknown() {
        String v = DocsTell.version();
        assertNotEquals("unknown", v,
            "build-info.properties should be on the test classpath — check the "
            + "build-info execution of spring-boot-maven-plugin");
        assertTrue(v.matches("\\d+\\.\\d+\\.\\d+.*"), () -> "unexpected version: " + v);
    }

    @Test
    void headerStatesTheVersionAndHowToInstallIt() {
        String all = DocsTell.full();
        String v = DocsTell.version();
        assertTrue(all.contains("JWeb " + v), "document must name the version it describes");
        assertTrue(all.contains("<version>v" + v + "</version>"), "Maven snippet must match the version");
        assertTrue(all.contains("com.github.oscar-osmig:Jweb:v" + v), "Gradle snippet must match the version");
    }

    // ==================== grounding quality ====================

    @Test
    void headerWarnsAboutTheChangesThatDoNotFailToCompile() {
        String all = DocsTell.full();
        assertTrue(all.contains("textarea(\"hello\")"),
            "the silent textarea change must be stated up front");
        assertTrue(all.contains("pushState(state, url)"),
            "the pushState argument-order change must be stated up front");
        assertTrue(all.contains("jweb.El"), "the import rule must be stated up front");
    }

    @Test
    void aTopicFetchedAloneStillCarriesTheHeader() {
        String one = DocsTell.topic("css-dsl");
        assertNotNull(one);
        assertTrue(one.contains("The six rules the DSL follows"),
            "a narrowed fetch must not lose the rules");
        assertTrue(one.contains("JWeb " + DocsTell.version()),
            "a narrowed fetch must not lose the version");
        assertFalse(one.contains("BEGIN backend"), "?topic= must return only that topic");
    }

    @Test
    void framingSourcesThatWouldMisleadAreNotServed() {
        // research.md compares React/Vue/Next and is not JWeb documentation;
        // serving it invites a model to cite it as JWeb guidance.
        for (Topic t : DocsTell.topics()) {
            assertFalse(t.resource().endsWith("research.md"),
                "research.md is a frontend-framework comparison, not JWeb docs");
            assertFalse(t.resource().endsWith("publish.md"),
                "publish.md is about releasing a library, not building an app");
            assertFalse(t.resource().endsWith("design-tooling.md"),
                "design-tooling.md is about an external design skill");
        }
    }

    // ==================== download filename ====================

    @Test
    void fullSetDownloadsUnderAVersionStampedName() {
        assertEquals("jweb-" + DocsTell.version() + "-docs.md", DocsTell.filename(null));
        assertEquals("jweb-" + DocsTell.version() + "-docs.md", DocsTell.filename(""));
        assertEquals("jweb-" + DocsTell.version() + "-docs.md", DocsTell.filename("  "));
    }

    @Test
    void aTopicDownloadsUnderItsOwnName() {
        assertEquals("jweb-" + DocsTell.version() + "-css-dsl.md", DocsTell.filename("css-dsl"));
    }

    @Test
    void everyTopicProducesAUsableFilename() {
        for (Topic t : DocsTell.topics()) {
            String name = DocsTell.filename(t.id());
            assertTrue(name.endsWith(".md"), () -> "not a markdown name: " + name);
            // Anything outside this set would need quoting or escaping in the
            // Content-Disposition header.
            assertTrue(name.matches("[A-Za-z0-9._-]+"),
                () -> "filename needs escaping in a header: " + name);
        }
    }

    // ==================== lookup ====================

    @Test
    void unknownTopicReturnsNullSoTheRouteCan404() {
        assertNull(DocsTell.topic("no-such-topic"));
        assertNull(DocsTell.topic(""));
    }

    @Test
    void repeatedCallsReturnTheSameCachedInstance() {
        assertSame(DocsTell.full(), DocsTell.full(), "full document should be assembled once");
    }
}
