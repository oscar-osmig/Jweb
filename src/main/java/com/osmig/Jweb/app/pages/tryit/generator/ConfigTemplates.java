package com.osmig.Jweb.app.pages.tryit.generator;

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
        return ReadmeContent.get();
    }
}
