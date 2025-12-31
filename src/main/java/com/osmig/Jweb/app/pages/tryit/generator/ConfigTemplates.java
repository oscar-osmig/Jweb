package com.osmig.Jweb.app.pages.tryit.generator;

import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Generates configuration files for the starter project. */
public final class ConfigTemplates {
    private ConfigTemplates() {}

    public static String applicationYaml() {
        return """
            server:
              port: 8080
              compression:
                enabled: true
                mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
                min-response-size: 1024

            spring:
              application:
                name: JWebApp

            jweb:
              api:
                base: /api/v1
              dev:
                hot-reload: true
                watch-paths: src/main/java,src/main/resources
                debounce-ms: 50
              performance:
                minify-css: true
                minify-html: false
                prefetch:
                  enabled: true
                  cache-ttl: 300000
                  hover-delay: 300
            """;
    }

    public static String readme() {
        return readmeContent().replace("com.osmig.Jweb.framework", "com.jweb.framework");
    }

    private static String readmeContent() {
        try {
            ClassPathResource resource = new ClassPathResource("readme/README.md");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return fallbackReadme();
        }
    }

    private static String fallbackReadme() {
        return """
            # JWeb Starter Project

            A web application built with the JWeb Framework - pure Java, no templates!

            ## Quick Start

            ```bash
            mvn spring-boot:run
            ```

            Then open http://localhost:8080 in your browser.

            ## Features

            - Type-safe HTML, CSS, and JavaScript
            - Reactive State with automatic UI updates
            - Hot Reload during development
            - Component-based architecture

            Happy coding!
            """;
    }
}
