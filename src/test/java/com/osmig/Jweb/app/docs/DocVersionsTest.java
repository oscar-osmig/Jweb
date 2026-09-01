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
        assertEquals("v2.1.0", DocVersions.latest());
        assertEquals(DocVersions.latest(), DocVersions.all().get(0),
            "the registry must be newest-first — latest() and the dropdown depend on it");
        assertTrue(DocVersions.all().contains("v2.0.0"));
    }

    @Test
    void normalizeMapsMissingAndUnknownToLatest() {
        assertEquals("v2.1.0", DocVersions.normalize(null));
        assertEquals("v2.1.0", DocVersions.normalize(""));
        assertEquals("v2.1.0", DocVersions.normalize("v9.9.9"));
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
        assertEquals("/docs?section=setup", DocVersions.href("setup", "v2.1.0"));
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
        assertTrue(html.contains("v2.1.0 (latest)"), "dropdown labels the latest");
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

        String latest = new DocSidebar("setup", "v2.1.0").render().toHtml();
        assertTrue(latest.contains("3D Scenes"));
        assertTrue(latest.contains("href=\"/docs?section=elements\""), "latest links canonical");
    }

    @Test
    void docsPageShowsABannerOnlyForOldVersions() {
        String old = new DocsPage("setup", "v2.0.0").render().toHtml();
        assertTrue(old.contains("Viewing documentation for v2.0.0"), "banner on old version");
        assertTrue(old.contains("Switch to v2.1.0"), "banner offers the latest");

        String latest = new DocsPage("setup", null).render().toHtml();
        assertFalse(latest.contains("Viewing documentation for"), "no banner on latest");
    }
}
