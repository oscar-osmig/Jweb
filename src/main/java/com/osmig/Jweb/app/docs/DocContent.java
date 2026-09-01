package com.osmig.Jweb.app.docs;

import jweb.Element;
import com.osmig.Jweb.app.docs.sections.*;

import static jweb.El.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public class DocContent {

    /** Latest-version content — the common case. */
    public static Element get(String section) {
        return get(section, DocVersions.latest());
    }

    /**
     * Content for one section under one docs version. Sections that don't
     * exist in the selected version answer with a pointer to the version
     * that introduced them instead of documentation for code the reader
     * doesn't have.
     */
    public static Element get(String section, String version) {
        String v = DocVersions.normalize(version);
        if (section != null && !DocVersions.sectionAvailable(section, v)) {
            return notInVersion(section, v);
        }
        return switch (section == null ? "intro" : section) {
            case "setup" -> SetupSection.render(v);
            case "elements" -> ElementsSection.render();
            case "styling" -> StylingSection.render();
            case "conditionals" -> ConditionalsSection.render();
            case "components" -> ComponentsSection.render();
            case "javascript" -> JavaScriptSection.render();
            case "routing" -> RoutingSection.render();
            case "fragments" -> FragmentsSection.render();
            case "streaming" -> StreamingSection.render();
            case "state" -> StateSection.render();
            case "forms" -> FormsSection.render();
            case "layouts" -> LayoutsSection.render();
            case "ai" -> AiSection.render();
            case "three" -> ThreeSection.render();
            case "performance" -> PerformanceSection.render();
            case "api" -> ApiSection.render();
            case "security" -> SecuritySection.render();
            case "ui" -> UIComponentsSection.render();
            case "data" -> DataSection.render();
            case "devtools" -> DevToolsSection.render();
            case "examples" -> ExamplesSection.render();
            default -> IntroSection.render();
        };
    }

    private static Element notInVersion(String section, String version) {
        String introduced = DocVersions.introducedIn(section);
        return section(
            docTitle("Not in " + version),
            para("This feature arrived in " + introduced + " — you are viewing the "
                 + "documentation for " + version + ", which does not include it."),
            p(a(attrs().href("/docs?section=" + section),
                text("Switch to " + DocVersions.latest() + " documentation")))
        );
    }
}
