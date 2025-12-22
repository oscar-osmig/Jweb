package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.*;

public class DocContent {

    public static Element get(String section) {
        return switch (section) {
            case "setup" -> SetupSection.render();
            case "elements" -> ElementsSection.render();
            case "styling" -> StylingSection.render();
            case "conditionals" -> ConditionalsSection.render();
            case "components" -> ComponentsSection.render();
            case "routing" -> RoutingSection.render();
            case "state" -> StateSection.render();
            case "forms" -> FormsSection.render();
            case "layouts" -> LayoutsSection.render();
            case "api" -> ApiSection.render();
            case "security" -> SecuritySection.render();
            case "ui" -> UIComponentsSection.render();
            case "data" -> DataSection.render();
            case "devtools" -> DevToolsSection.render();
            case "examples" -> ExamplesSection.render();
            default -> IntroSection.render();
        };
    }
}
