package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class DevToolsSection {
    private DevToolsSection() {}

    public static Element render() {
        return section(
            docTitle("DevTools"),
            para("Hot reload and browser auto-refresh for faster development."),

            docSubtitle("Configuration"),
            codeBlock("""
# application.yaml
jweb:
  dev:
    # Enable hot reload
    hot-reload: true
    # Paths to watch for changes
    watch-paths: src/main/java,src/main/resources
    # Debounce delay in ms (lower = faster reload, min 10)
    debounce-ms: 50"""),

            docSubtitle("Add to Layout"),
            para("Include the DevServer script in your layout for auto-refresh:"),
            codeBlock("""
import com.osmig.Jweb.framework.dev.DevServer;

public Element render() {
    return html(
        head(...),
        body(
            content,
            DevServer.script()  // Enables hot reload
        )
    );
}"""),

            docSubtitle("Spring DevTools"),
            para("Add Spring DevTools for automatic app restart on changes:"),
            codeBlock("""
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>"""),

            docSubtitle("How It Works"),
            docList(
                "DevServer watches your source files for changes",
                "Spring DevTools automatically restarts the app",
                "Browser auto-refreshes via SSE (Server-Sent Events)",
                "Debounce batches rapid changes to avoid multiple reloads"
            ),

            docSubtitle("IDE Setup"),
            para("For seamless hot reload, configure your IDE:"),
            docList(
                "IntelliJ: Enable 'Build project automatically' in Settings > Build",
                "IntelliJ: Enable 'Allow auto-make' in Registry (Ctrl+Shift+A)",
                "VS Code: Use the Java Extension Pack with auto-build"
            ),

            docTip("Lower debounce-ms for faster reloads, but may cause multiple reloads on save.")
        );
    }
}
