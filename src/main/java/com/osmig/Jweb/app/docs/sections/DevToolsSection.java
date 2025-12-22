package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

public final class DevToolsSection {
    private DevToolsSection() {}

    public static Element render() {
        return section(
            title("DevTools"),
            text("JWeb includes development tools for faster iteration: " +
                 "hot reload, file watching, and browser auto-refresh."),

            subtitle("Hot Reload Setup"),
            text("Enable hot reload in application.yaml to auto-refresh your browser " +
                 "when files change:"),
            code(DEV_CONFIG),

            subtitle("Adding to Layout"),
            text("Add DevServer.script() to your layout for browser refresh:"),
            code(DEV_LAYOUT),

            subtitle("How It Works"),
            list(
                "DevServer watches src/main/java and src/main/resources",
                "When files change, it notifies connected browsers via SSE",
                "Spring Boot DevTools restarts the app automatically",
                "Browser receives reload signal and refreshes"
            ),

            subtitle("Spring Boot DevTools"),
            text("For full hot reload (including Java class reloading), add DevTools:"),
            code(DEV_DEVTOOLS_POM),

            subtitle("IDE Configuration"),
            text("Enable 'Build project automatically' in IntelliJ for seamless hot reload. " +
                 "When you save a file, the app restarts and browser refreshes automatically.")
        );
    }
}
