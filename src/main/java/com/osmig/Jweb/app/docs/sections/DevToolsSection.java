package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class DevToolsSection {
    private DevToolsSection() {}

    public static Element render() {
        return section(
            docTitle("DevTools"),
            para("Hot reload and browser auto-refresh for faster development."),

            docSubtitle("Enable Hot Reload"),
            codeBlock("""
# application.yaml
jweb:
  dev:
    hot-reload: true
    watch-paths:
      - src/main/java
      - src/main/resources"""),

            docSubtitle("Add to Layout"),
            codeBlock("""
public Element render() {
    return html(
        head(...),
        body(
            content,
            DevServer.script()  // Add this
        )
    );
}"""),

            docSubtitle("Spring DevTools"),
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
                "DevServer watches your source files",
                "Spring DevTools restarts app on changes",
                "Browser auto-refreshes via SSE connection"
            ),

            docTip("Enable 'Build automatically' in IntelliJ for seamless hot reload.")
        );
    }
}
