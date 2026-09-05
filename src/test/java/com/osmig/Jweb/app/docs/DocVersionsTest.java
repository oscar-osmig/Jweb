package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.app.docs.sections.SetupSection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Version-aware docs: the registry, the dependency snippet's version chip,
 * and the gating of sections that arrived in later releases.
 */
class DocVersionsTest {

    @Test
    void latestIsNewestFirstInTheRegistry() {
        assertEquals("v3.0.0", DocVersions.latest());
        assertEquals(DocVersions.latest(), DocVersions.all().get(0),
            "the registry must be newest-first — latest() and the dropdown depend on it");
        assertTrue(DocVersions.all().contains("v2.0.0"));
    }

    @Test
    void normalizeMapsMissingAndUnknownToLatest() {
        assertEquals("v3.0.0", DocVersions.normalize(null));
        assertEquals("v3.0.0", DocVersions.normalize(""));
        assertEquals("v3.0.0", DocVersions.normalize("v9.9.9"));
        assertEquals("v2.0.0", DocVersions.normalize("v2.0.0"));
    }

    @Test
    void threeSectionIsGatedToItsIntroducingVersion() {
        assertTrue(DocVersions.sectionAvailable("three", "v2.1.0"));
        assertFalse(DocVersions.sectionAvailable("three", "v2.0.0"));
        // sections without an entry exist everywhere
        assertTrue(DocVersions.sectionAvailable("setup", "v2.0.0"));
        assertTrue(DocVersions.sectionAvailable("elements", "v2.0.0"));
    }

    @Test
    void latestHrefStaysCanonical() {
        assertEquals("/docs?section=setup", DocVersions.href("setup", "v3.0.0"));
        assertEquals("/docs?section=setup&v=v2.0.0", DocVersions.href("setup", "v2.0.0"));
    }

    // ==================== The dependency snippet ====================

    @Test
    void snippetShowsTheSelectedVersionInMavenAndGradle() {
        String latest = SetupSection.render("v2.1.0").toHtml();
        assertTrue(latest.contains("&lt;version&gt;v2.1.0&lt;/version&gt;"), "maven version");
        assertTrue(latest.contains("Jweb:v2.1.0"), "gradle version");

        String old = SetupSection.render("v2.0.0").toHtml();
        assertTrue(old.contains("&lt;version&gt;v2.0.0&lt;/version&gt;"), "maven version follows selection");
        assertTrue(old.contains("Jweb:v2.0.0"), "gradle version follows selection");
        assertFalse(old.contains("Jweb:v2.1.0"), "no stray latest version in old view");
    }

    @Test
    void versionChipListsAllVersionsAndMarksTheCurrent() {
        String html = SetupSection.render("v2.0.0").toHtml();
        assertTrue(html.contains("ver-picker"), "chip present on the dependency block");
        assertTrue(html.contains("v2.0.0 ▾"), "chip shows the selected version");
        assertTrue(html.contains("v3.0.0 (latest)"), "dropdown labels the latest");
        assertTrue(html.contains("ver-picker-item current"), "current version marked");
        assertTrue(html.contains("href=\"/docs?section=setup\""), "latest entry canonical");
        assertTrue(html.contains("href=\"/docs?section=setup&amp;v=v2.0.0\""), "old entry carries ?v=");
    }

    // ==================== Gating ====================

    @Test
    void gatedSectionAnswersWithANoticeInsteadOfDocs() {
        String html = DocContent.get("three", "v2.0.0").toHtml();
        assertTrue(html.contains("arrived in v2.1.0"), html);
        assertTrue(html.contains("/docs?section=three"), "notice links to the latest docs");

        String latest = DocContent.get("three", "v2.1.0").toHtml();
        assertTrue(latest.contains("3D Scenes"), "available version renders the real section");
        assertFalse(latest.contains("arrived in"), "no gate notice on the latest");
    }

    @Test
    void sidebarHidesGatedSectionsAndThreadsTheVersion() {
        String old = new DocSidebar("setup", "v2.0.0").render().toHtml();
        assertFalse(old.contains("3D Scenes"), "gated section hidden from nav");
        assertTrue(old.contains("/docs?section=elements&amp;v=v2.0.0"), "links keep the version");

        String latest = new DocSidebar("setup", "v3.0.0").render().toHtml();
        assertTrue(latest.contains("3D Scenes"));
        assertTrue(latest.contains("href=\"/docs?section=elements\""), "latest links canonical");
    }

    // ==================== Prose branching inside shared sections ====================

    @Test
    void atLeastComparesAgainstTheRegistryOrder() {
        assertTrue(DocVersions.atLeast("v2.1.0", "v2.1.0"));
        assertTrue(DocVersions.atLeast("v2.1.0", "v2.0.0"));
        assertFalse(DocVersions.atLeast("v2.0.0", "v2.1.0"));
        // an unreleased floor is newer than everything — content stays hidden
        assertFalse(DocVersions.atLeast("v2.1.0", "v9.9.9"));
    }

    @Test
    void sharedSectionProseBranchesByVersion() {
        String latest = DocContent.get("intro", "v2.1.0").toHtml();
        assertTrue(latest.contains("Three DSL - declarative 3D scenes"),
            "latest intro advertises the feature");
        assertTrue(latest.contains("New in v2.1.0"), "since() block renders on latest");

        String old = DocContent.get("intro", "v2.0.0").toHtml();
        assertFalse(old.contains("Three DSL"), "v2.0.0 intro must not mention it");
        assertFalse(old.contains("New in v2.1.0"), "since() block hidden on old docs");
        assertTrue(old.contains("HTML DSL"), "shared prose still renders");
    }

    @Test
    void renderContextDefaultsToLatestAndNeverLeaks() {
        assertEquals(DocVersions.latest(), DocVersions.current(),
            "outside a render, current() is latest — so since() content shows everywhere else");
        DocContent.get("intro", "v2.0.0");
        assertEquals(DocVersions.latest(), DocVersions.current(),
            "the render context must be cleared even after old-version renders");
    }

    @Test
    void versionContextSurvivesStreamedSuspenseBlocks() {
        // The caveat this pins down: streamed Suspense blocks render on other
        // threads — the docs version must travel with them or since()/before()
        // silently fall back to latest inside the block.
        var streaming = com.osmig.Jweb.framework.async.StreamingContext.open();
        DocVersions.beginRender("v2.0.0");
        try {
            com.osmig.Jweb.framework.async.Suspense.of((java.util.concurrent.Callable<String>) () -> "data")
                .loading(() -> jweb.El.span(jweb.El.text("...")))
                .render(data -> DocComponents.section(
                    DocComponents.para("v=" + DocVersions.current()),
                    DocComponents.since("v2.1.0", DocComponents.para("ONLY-ON-LATEST"))))
                .toHtml();

            String resolved = streaming.pendings().get(0).html().join();
            assertTrue(resolved.contains("v=v2.0.0"),
                "the async block must render under the selected version: " + resolved);
            assertFalse(resolved.contains("ONLY-ON-LATEST"),
                "since() must stay hidden for old docs inside async blocks: " + resolved);
        } finally {
            DocVersions.endRender();
            com.osmig.Jweb.framework.async.StreamingContext.close();
        }
    }

    @Test
    void combinatorsFollowTheRenderContext() {
        DocVersions.beginRender("v2.0.0");
        try {
            assertNotNull(DocComponents.before("v2.1.0", DocComponents.para("old way")));
            assertNull(DocComponents.since("v2.1.0", DocComponents.para("new way")));
            assertNull(DocComponents.sinceText("v2.1.0", "new item"));
        } finally {
            DocVersions.endRender();
        }
        assertNull(DocComponents.before("v2.1.0", DocComponents.para("old way")),
            "on latest, before() content is gone");
        assertNotNull(DocComponents.since("v2.1.0", DocComponents.para("new way")),
            "on latest, since() content shows");
    }

    @Test
    void docsPageShowsABannerOnlyForOldVersions() {
        String old = new DocsPage("setup", "v2.0.0").render().toHtml();
        assertTrue(old.contains("Viewing documentation for v2.0.0"), "banner on old version");
        assertTrue(old.contains("Switch to v3.0.0"), "banner offers the latest");

        String latest = new DocsPage("setup", null).render().toHtml();
        assertFalse(latest.contains("Viewing documentation for"), "no banner on latest");
    }
}
