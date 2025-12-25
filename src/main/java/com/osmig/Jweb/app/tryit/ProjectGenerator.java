package com.osmig.Jweb.app.tryit;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * Generates a complete JWeb starter project as a ZIP file.
 *
 * The generated project includes:
 * - Complete Spring Boot project structure
 * - pom.xml with all dependencies
 * - The entire JWeb framework
 * - Example application code
 * - README with getting started instructions
 */
@Service
public class ProjectGenerator {

    private static final String FRAMEWORK_PATH = "src/main/java/com/osmig/Jweb/framework";

    /**
     * Generate a complete JWeb starter project as a ZIP file.
     */
    public byte[] generate() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add pom.xml
            addEntry(zos, "jweb-starter/pom.xml", generatePomXml());

            // Add application.yaml
            addEntry(zos, "jweb-starter/src/main/resources/application.yaml", generateApplicationYaml());

            // Add main application class
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/App.java", generateAppClass());

            // Add example routes
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/Routes.java", generateRoutesClass());

            // Add example layout
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Layout.java", generateLayoutClass());
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Nav.java", generateNavClass());
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/layout/Footer.java", generateFooterClass());

            // Add example page
            addEntry(zos, "jweb-starter/src/main/java/com/example/app/pages/HomePage.java", generateHomePageClass());

            // Add README
            addEntry(zos, "jweb-starter/README.md", generateReadme());

            // Add the entire framework folder
            addFrameworkFolder(zos);
        }

        return baos.toByteArray();
    }

    private void addEntry(ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes());
        zos.closeEntry();
    }

    private void addFrameworkFolder(ZipOutputStream zos) throws IOException {
        Path frameworkPath = Paths.get(FRAMEWORK_PATH);

        if (!Files.exists(frameworkPath)) {
            throw new IOException("Framework folder not found: " + frameworkPath.toAbsolutePath());
        }

        Files.walk(frameworkPath)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    String relativePath = frameworkPath.getParent().relativize(file).toString()
                        .replace("\\", "/");
                    String zipPath = "jweb-starter/src/main/java/com/example/" + relativePath;

                    // Read and modify package declaration
                    String content = Files.readString(file);
                    content = content.replace(
                        "package com.osmig.Jweb.framework",
                        "package com.example.framework"
                    );
                    content = content.replace(
                        "import com.osmig.Jweb.framework",
                        "import com.example.framework"
                    );

                    zos.putNextEntry(new ZipEntry(zipPath));
                    zos.write(content.getBytes());
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    private String generatePomXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>

                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>4.0.0</version>
                    <relativePath/>
                </parent>

                <groupId>com.example</groupId>
                <artifactId>jweb-app</artifactId>
                <version>1.0.0</version>
                <name>My JWeb App</name>
                <description>Web application built with JWeb Framework</description>

                <properties>
                    <java.version>21</java.version>
                </properties>

                <dependencies>
                    <!-- Spring Boot Web -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>

                    <!-- WebSocket support for reactive state -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-websocket</artifactId>
                    </dependency>

                    <!-- DevTools for hot reload -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-devtools</artifactId>
                        <scope>runtime</scope>
                        <optional>true</optional>
                    </dependency>

                    <!-- JSON processing -->
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-databind</artifactId>
                    </dependency>

                    <!-- MongoDB driver (optional) -->
                    <dependency>
                        <groupId>org.mongodb</groupId>
                        <artifactId>mongodb-driver-sync</artifactId>
                        <version>5.2.0</version>
                    </dependency>

                    <!-- Password hashing -->
                    <dependency>
                        <groupId>org.springframework.security</groupId>
                        <artifactId>spring-security-crypto</artifactId>
                    </dependency>

                    <!-- JWT support -->
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-api</artifactId>
                        <version>0.12.6</version>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-impl</artifactId>
                        <version>0.12.6</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-jackson</artifactId>
                        <version>0.12.6</version>
                        <scope>runtime</scope>
                    </dependency>

                    <!-- Testing -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;
    }

    private String generateApplicationYaml() {
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

            # JWeb configuration
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

    private String generateAppClass() {
        return """
            package com.example.app;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            import org.springframework.context.annotation.ComponentScan;

            @SpringBootApplication
            @ComponentScan(basePackages = {"com.example"})
            public class App {
                public static void main(String[] args) {
                    SpringApplication.run(App.class, args);
                }
            }
            """;
    }

    private String generateRoutesClass() {
        return """
            package com.example.app;

            import com.example.framework.JWeb;
            import com.example.framework.JWebRoutes;
            import com.example.app.layout.Layout;
            import com.example.app.pages.HomePage;
            import org.springframework.stereotype.Component;

            @Component
            public class Routes implements JWebRoutes {

                @Override
                public void configure(JWeb app) {
                    // Page routes with layout
                    app.layout(Layout.class)
                       .pages(
                           "/", HomePage.class
                       );

                    // Example API route
                    app.get("/api/hello", ctx -> "Hello from JWeb!");
                }
            }
            """;
    }

    private String generateLayoutClass() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.dev.DevServer;
            import com.example.framework.template.Template;

            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;

            public class Layout implements Template {
                private final String title;
                private final Element content;

                public Layout(String title, Element content) {
                    this.title = title;
                    this.content = content;
                }

                @Override
                public Element render() {
                    return html(
                        head(
                            meta(attrs().charset("UTF-8")),
                            meta(attrs().name("viewport").content("width=device-width, initial-scale=1")),
                            title(title),
                            style(globalStyles())
                        ),
                        body(attrs().style()
                                .margin(zero).fontFamily("system-ui, sans-serif")
                                .minHeight(vh(100)).display(flex).flexDirection(column).done(),
                            new Nav().render(),
                            main(attrs().style().flex(num(1)).padding(rem(2)).done(), content),
                            new Footer().render(),
                            DevServer.script()
                        )
                    );
                }

                private String globalStyles() {
                    return rules(
                        rule("*").boxSizing(borderBox),
                        rule("a").color(hex("#6366f1")).textDecoration(none),
                        rule("a:hover").textDecoration(underline)
                    );
                }
            }
            """;
    }

    private String generateNavClass() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.template.Template;

            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class Nav implements Template {

                @Override
                public Element render() {
                    return nav(attrs().style()
                            .backgroundColor(hex("#6366f1"))
                            .padding(rem(1), rem(2))
                            .display(flex).alignItems(center).justifyContent(spaceBetween).done(),
                        a(attrs().href("/").style()
                            .color(white).fontSize(rem(1.25)).fontWeight(700).done(),
                            text("My App")),
                        div(attrs().style().display(flex).gap(rem(1.5)).done(),
                            navLink("/", "Home"),
                            navLink("/about", "About")
                        )
                    );
                }

                private Element navLink(String href, String label) {
                    return a(attrs().href(href).style()
                        .color(rgba(255, 255, 255, 0.9)).done(),
                        text(label));
                }
            }
            """;
    }

    private String generateFooterClass() {
        return """
            package com.example.app.layout;

            import com.example.framework.core.Element;
            import com.example.framework.template.Template;

            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class Footer implements Template {

                @Override
                public Element render() {
                    return footer(attrs().style()
                            .backgroundColor(gray(100))
                            .padding(rem(1), rem(2))
                            .textAlign(center)
                            .fontSize(rem(0.875)).done(),
                        p("Built with JWeb Framework")
                    );
                }
            }
            """;
    }

    private String generateHomePageClass() {
        return """
            package com.example.app.pages;

            import com.example.framework.core.Element;
            import com.example.framework.state.State;
            import com.example.framework.template.Template;

            import static com.example.framework.elements.Elements.*;
            import static com.example.framework.styles.CSS.*;
            import static com.example.framework.styles.CSSUnits.*;
            import static com.example.framework.styles.CSSColors.*;

            public class HomePage implements Template {

                private final State<Integer> count = State.of(0);

                @Override
                public Element render() {
                    return div(attrs().style()
                            .maxWidth(px(800)).margin(zero, auto).textAlign(center).done(),

                        h1(attrs().style().fontSize(rem(2.5)).marginBottom(rem(1)).done(),
                            text("Welcome to JWeb!")),

                        p(attrs().style().color(gray(600)).marginBottom(rem(2)).done(),
                            text("Build web applications in pure Java")),

                        // Interactive counter example
                        div(attrs().style()
                                .padding(rem(2))
                                .backgroundColor(gray(50))
                                .borderRadius(px(8)).done(),

                            h2("Interactive Counter"),
                            p(attrs().style().fontSize(rem(3)).fontWeight(700).done(),
                                text(String.valueOf(count.get()))),

                            div(attrs().style().display(flex).gap(rem(1)).justifyContent(center).done(),
                                button(attrs()
                                    .onClick(e -> count.update(n -> n - 1))
                                    .style().padding(rem(0.5), rem(1.5))
                                        .fontSize(rem(1.25)).cursor(pointer).done(),
                                    text("-")),

                                button(attrs()
                                    .onClick(e -> count.update(n -> n + 1))
                                    .style().padding(rem(0.5), rem(1.5))
                                        .fontSize(rem(1.25)).cursor(pointer).done(),
                                    text("+"))
                            )
                        ),

                        div(attrs().style().marginTop(rem(3)).done(),
                            h3("Get Started"),
                            ul(attrs().style().textAlign(left).maxWidth(px(400)).margin(zero, auto).done(),
                                li("Edit ", code("src/main/java/com/example/app/pages/HomePage.java")),
                                li("Create new pages in the ", code("pages"), text(" folder")),
                                li("Add routes in ", code("Routes.java")),
                                li("Check out the JWeb documentation")
                            )
                        )
                    );
                }
            }
            """;
    }

    private String generateReadme() {
        return """
            # JWeb Starter Project

            A web application built with the JWeb Framework - pure Java, no templates!

            ## Quick Start

            ```bash
            # Run the application
            mvn spring-boot:run

            # Or build and run
            mvn clean package
            java -jar target/jweb-app-1.0.0.jar
            ```

            Then open http://localhost:8080 in your browser.

            ## Project Structure

            ```
            src/main/java/com/example/
            ├── app/                    # Your application code
            │   ├── layout/             # Layout components (Nav, Footer)
            │   ├── pages/              # Page components
            │   ├── Routes.java         # Route definitions
            │   └── App.java            # Main application class
            │
            └── framework/              # JWeb Framework
                ├── core/               # Core interfaces
                ├── elements/           # HTML elements
                ├── styles/             # CSS DSL
                ├── state/              # Reactive state
                └── ...                 # More features
            ```

            ## Creating a New Page

            1. Create a new class in `app/pages/`:

            ```java
            package com.example.app.pages;

            import com.example.framework.core.Element;
            import com.example.framework.template.Template;
            import static com.example.framework.elements.Elements.*;

            public class AboutPage implements Template {
                @Override
                public Element render() {
                    return div(
                        h1("About Us"),
                        p("This is our story...")
                    );
                }
            }
            ```

            2. Add the route in `Routes.java`:

            ```java
            app.layout(Layout.class)
               .pages(
                   "/", HomePage.class,
                   "/about", AboutPage.class  // Add this
               );
            ```

            ## Features

            - **Type-safe HTML** - Build HTML with Java methods
            - **Type-safe CSS** - CSS properties as Java methods
            - **Reactive State** - Automatic UI updates
            - **Hot Reload** - See changes instantly during development
            - **Component-based** - Build reusable UI components

            ## Learn More

            Visit the JWeb documentation for more examples and guides.

            Happy coding!
            """;
    }
}
