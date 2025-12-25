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
        return """
            # JWeb Starter Project

            A web application built with the JWeb Framework - pure Java, no templates!

            ## Quick Start

            ```bash
            mvn spring-boot:run
            ```

            Then open http://localhost:8080 in your browser.

            ## Project Structure

            ```
            src/main/java/com/example/
            ├── app/                    # Your application code
            │   ├── layout/             # Layout components
            │   ├── pages/              # Page components
            │   ├── Routes.java         # Route definitions
            │   └── App.java            # Main application
            └── framework/              # JWeb Framework
            ```

            ## Creating a New Page

            1. Create a class in `app/pages/`:

            ```java
            public class AboutPage implements Template {
                @Override
                public Element render() {
                    return div(h1("About"), p("Our story..."));
                }
            }
            ```

            2. Add route in `Routes.java`:

            ```java
            app.layout(Layout.class).pages("/about", AboutPage.class);
            ```

            ## Features

            - Type-safe HTML, CSS, and JavaScript
            - Reactive State with automatic UI updates
            - Hot Reload during development
            - Component-based architecture

            Happy coding!
            """;
    }
}
