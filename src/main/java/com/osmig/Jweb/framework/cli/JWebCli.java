package com.osmig.Jweb.framework.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * JWeb CLI tool for project scaffolding and code generation.
 *
 * <p>Usage from command line:</p>
 * <pre>
 * # Create a new project
 * java -cp jweb.jar com.osmig.Jweb.framework.cli.JWebCli new myapp
 *
 * # Generate a page
 * java -cp jweb.jar com.osmig.Jweb.framework.cli.JWebCli generate page HomePage
 *
 * # Generate a component
 * java -cp jweb.jar com.osmig.Jweb.framework.cli.JWebCli generate component UserCard
 *
 * # Generate a form model
 * java -cp jweb.jar com.osmig.Jweb.framework.cli.JWebCli generate form UserForm name:string email:email age:int
 *
 * # Generate CRUD
 * java -cp jweb.jar com.osmig.Jweb.framework.cli.JWebCli generate crud User name:string email:string
 * </pre>
 *
 * <p>For easier usage, create an alias or wrapper script:</p>
 * <pre>
 * # jweb.bat (Windows)
 * @echo off
 * java -cp path/to/jweb.jar com.osmig.Jweb.framework.cli.JWebCli %*
 *
 * # jweb (Unix)
 * #!/bin/bash
 * java -cp path/to/jweb.jar com.osmig.Jweb.framework.cli.JWebCli "$@"
 * </pre>
 */
public class JWebCli {

    private static final String VERSION = "1.0.0";
    private static String basePackage = "com.example";
    private static Path projectRoot = Paths.get(".");

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();

        try {
            switch (command) {
                case "new", "create" -> createProject(args);
                case "generate", "g" -> generate(args);
                case "help", "-h", "--help" -> printHelp();
                case "version", "-v", "--version" -> System.out.println("JWeb CLI v" + VERSION);
                default -> {
                    System.err.println("Unknown command: " + command);
                    printHelp();
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    // ==================== Project Creation ====================

    private static void createProject(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: jweb new <project-name> [--package=com.example]");
            return;
        }

        String projectName = args[1];
        String packageName = "com." + projectName.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Parse options
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--package=")) {
                packageName = args[i].substring(10);
            }
        }

        Path projectDir = Paths.get(projectName);
        if (Files.exists(projectDir)) {
            System.err.println("Directory already exists: " + projectName);
            return;
        }

        System.out.println("Creating JWeb project: " + projectName);
        System.out.println("Package: " + packageName);

        // Create directory structure
        String packagePath = packageName.replace('.', '/');
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/pages"));
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/components"));
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/layouts"));
        Files.createDirectories(projectDir.resolve("src/main/resources"));
        Files.createDirectories(projectDir.resolve("src/test/java/" + packagePath));

        // Generate pom.xml
        writeFile(projectDir.resolve("pom.xml"), generatePom(projectName, packageName));

        // Generate main application
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/Application.java"),
            generateApplication(packageName));

        // Generate routes
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/Routes.java"),
            generateRoutes(packageName));

        // Generate home page
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/pages/HomePage.java"),
            generateHomePage(packageName, projectName));

        // Generate main layout
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/layouts/MainLayout.java"),
            generateMainLayout(packageName));

        // Generate application.properties
        writeFile(projectDir.resolve("src/main/resources/application.properties"),
            generateProperties(projectName));

        // Generate .gitignore
        writeFile(projectDir.resolve(".gitignore"), generateGitignore());

        System.out.println("\n\u2713 Project created successfully!");
        System.out.println("\nNext steps:");
        System.out.println("  cd " + projectName);
        System.out.println("  mvn spring-boot:run");
        System.out.println("\nThen open http://localhost:8080 in your browser.");
    }

    // ==================== Code Generation ====================

    private static void generate(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: jweb generate <type> <name> [fields...]");
            System.err.println("Types: page, component, layout, form, crud, api");
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];
        String[] fields = Arrays.copyOfRange(args, 3, args.length);

        // Try to detect package from current directory
        detectProject();

        switch (type) {
            case "page", "p" -> generatePage(name, fields);
            case "component", "c" -> generateComponent(name, fields);
            case "layout", "l" -> generateLayout(name);
            case "form", "f" -> generateForm(name, fields);
            case "crud" -> generateCrud(name, fields);
            case "api" -> generateApi(name, fields);
            default -> System.err.println("Unknown type: " + type);
        }
    }

    private static void generatePage(String name, String[] fields) throws IOException {
        String className = capitalize(name);
        if (!className.endsWith("Page")) {
            className += "Page";
        }

        String code = Templates.page(basePackage, className, fields);
        Path path = resolvePath("pages", className + ".java");
        writeFile(path, code);
        System.out.println("\u2713 Created page: " + path);
    }

    private static void generateComponent(String name, String[] fields) throws IOException {
        String className = capitalize(name);

        String code = Templates.component(basePackage, className, fields);
        Path path = resolvePath("components", className + ".java");
        writeFile(path, code);
        System.out.println("\u2713 Created component: " + path);
    }

    private static void generateLayout(String name) throws IOException {
        String className = capitalize(name);
        if (!className.endsWith("Layout")) {
            className += "Layout";
        }

        String code = Templates.layout(basePackage, className);
        Path path = resolvePath("layouts", className + ".java");
        writeFile(path, code);
        System.out.println("\u2713 Created layout: " + path);
    }

    private static void generateForm(String name, String[] fields) throws IOException {
        String className = capitalize(name);
        if (!className.endsWith("Form")) {
            className += "Form";
        }

        String code = Templates.formModel(basePackage, className, fields);
        Path path = resolvePath("forms", className + ".java");
        writeFile(path, code);
        System.out.println("\u2713 Created form model: " + path);
    }

    private static void generateCrud(String name, String[] fields) throws IOException {
        String entityName = capitalize(name);

        // Generate entity
        String entityCode = Templates.entity(basePackage, entityName, fields);
        writeFile(resolvePath("models", entityName + ".java"), entityCode);
        System.out.println("\u2713 Created entity: " + entityName);

        // Generate repository
        String repoCode = Templates.repository(basePackage, entityName);
        writeFile(resolvePath("repositories", entityName + "Repository.java"), repoCode);
        System.out.println("\u2713 Created repository: " + entityName + "Repository");

        // Generate list page
        String listCode = Templates.listPage(basePackage, entityName, fields);
        writeFile(resolvePath("pages", entityName + "ListPage.java"), listCode);
        System.out.println("\u2713 Created list page: " + entityName + "ListPage");

        // Generate form page
        String formCode = Templates.formPage(basePackage, entityName, fields);
        writeFile(resolvePath("pages", entityName + "FormPage.java"), formCode);
        System.out.println("\u2713 Created form page: " + entityName + "FormPage");

        System.out.println("\nDon't forget to add routes in Routes.java:");
        System.out.println("  .get(\"/" + entityName.toLowerCase() + "s\", " + entityName + "ListPage::new)");
        System.out.println("  .get(\"/" + entityName.toLowerCase() + "s/new\", " + entityName + "FormPage::new)");
    }

    private static void generateApi(String name, String[] fields) throws IOException {
        String className = capitalize(name);
        if (!className.endsWith("Api") && !className.endsWith("Controller")) {
            className += "Api";
        }

        String code = Templates.api(basePackage, className, fields);
        Path path = resolvePath("api", className + ".java");
        writeFile(path, code);
        System.out.println("\u2713 Created API: " + path);
    }

    // ==================== Utilities ====================

    private static void detectProject() {
        // Try to find pom.xml and extract package
        Path pom = projectRoot.resolve("pom.xml");
        if (Files.exists(pom)) {
            try {
                String content = Files.readString(pom);
                int start = content.indexOf("<groupId>") + 9;
                int end = content.indexOf("</groupId>", start);
                if (start > 8 && end > start) {
                    String groupId = content.substring(start, end).trim();
                    int artStart = content.indexOf("<artifactId>") + 12;
                    int artEnd = content.indexOf("</artifactId>", artStart);
                    if (artStart > 11 && artEnd > artStart) {
                        String artifactId = content.substring(artStart, artEnd).trim();
                        basePackage = groupId + "." + artifactId.replaceAll("[^a-zA-Z0-9]", "");
                    }
                }
            } catch (IOException e) {
                // Use default
            }
        }
    }

    private static Path resolvePath(String subpackage, String fileName) throws IOException {
        String packagePath = basePackage.replace('.', '/') + "/" + subpackage;
        Path dir = projectRoot.resolve("src/main/java/" + packagePath);
        Files.createDirectories(dir);
        return dir.resolve(fileName);
    }

    private static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static void printHelp() {
        System.out.println("""
            JWeb CLI v%s

            Usage: jweb <command> [options]

            Commands:
              new <name>              Create a new JWeb project
              generate <type> <name>  Generate code (page, component, layout, form, crud, api)
              help                    Show this help message
              version                 Show version

            Examples:
              jweb new myapp --package=com.mycompany.myapp
              jweb generate page Home
              jweb generate component UserCard
              jweb generate form User name:string email:email age:int
              jweb generate crud Product name:string price:double
              jweb generate api Users

            Field types: string, int, long, double, boolean, date, datetime, email, password
            """.formatted(VERSION));
    }

    // ==================== Template Generation ====================

    private static String generatePom(String projectName, String packageName) {
        String groupId = packageName.contains(".")
            ? packageName.substring(0, packageName.lastIndexOf('.'))
            : packageName;
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>

                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.2.0</version>
                </parent>

                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <name>%s</name>

                <properties>
                    <java.version>17</java.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                    <!-- Add JWeb dependency here -->
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
            """.formatted(groupId, projectName, projectName);
    }

    private static String generateApplication(String packageName) {
        return """
            package %s;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;

            @SpringBootApplication
            public class Application {

                public static void main(String[] args) {
                    SpringApplication.run(Application.class, args);
                }
            }
            """.formatted(packageName);
    }

    private static String generateRoutes(String packageName) {
        return """
            package %s;

            import com.osmig.Jweb.framework.JWebRoutes;
            import com.osmig.Jweb.framework.routing.Router;
            import %s.pages.HomePage;
            import org.springframework.stereotype.Component;

            @Component
            public class Routes implements JWebRoutes {

                @Override
                public void configure(Router router) {
                    router
                        .get("/", HomePage::new);
                }
            }
            """.formatted(packageName, packageName);
    }

    private static String generateHomePage(String packageName, String projectName) {
        return """
            package %s.pages;

            import com.osmig.Jweb.framework.core.Element;
            import com.osmig.Jweb.framework.template.Template;
            import %s.layouts.MainLayout;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

            public class HomePage implements Template {

                @Override
                public Element render() {
                    return MainLayout.wrap(
                        div(attrs().style()
                                .textCenter()
                                .padding(rem(4)),
                            h1(attrs().style()
                                    .fontSize(rem(3))
                                    .fontWeight(700)
                                    .marginBottom(rem(1)),
                                text("Welcome to %s")),
                            p(attrs().style()
                                    .fontSize(rem(1.25))
                                    .color(() -> "#6b7280")
                                    .marginBottom(rem(2)),
                                text("Built with JWeb - Java Web Framework")),
                            a(attrs()
                                    .href("https://github.com/osmig/jweb")
                                    .style()
                                    .display(() -> "inline-block")
                                    .backgroundColor(() -> "#6366f1")
                                    .color(() -> "white")
                                    .padding(rem(0.75), rem(1.5))
                                    .rounded(px(8))
                                    .textDecoration(() -> "none")
                                    .fontWeight(500),
                                text("Get Started")))
                    );
                }
            }
            """.formatted(packageName, packageName, projectName);
    }

    private static String generateMainLayout(String packageName) {
        return """
            package %s.layouts;

            import com.osmig.Jweb.framework.core.Element;

            import static com.osmig.Jweb.framework.elements.Elements.*;
            import static com.osmig.Jweb.framework.styles.CSSUnits.*;

            public class MainLayout {

                public static Element wrap(Element... content) {
                    return html(
                        head(
                            meta(attrs().set("charset", "UTF-8")),
                            meta(attrs()
                                .name("viewport")
                                .content("width=device-width, initial-scale=1.0")),
                            title(text("JWeb App")),
                            style(text(globalStyles()))
                        ),
                        body(attrs().style()
                                .margin(() -> "0")
                                .fontFamily("system-ui, -apple-system, sans-serif")
                                .minHeight(() -> "100vh")
                                .backgroundColor(() -> "#f9fafb"),
                            content)
                    );
                }

                private static String globalStyles() {
                    return \"\"\"
                        *, *::before, *::after { box-sizing: border-box; }
                        body { line-height: 1.5; -webkit-font-smoothing: antialiased; }
                        img, picture, video, canvas, svg { display: block; max-width: 100%%; }
                        input, button, textarea, select { font: inherit; }
                        p, h1, h2, h3, h4, h5, h6 { overflow-wrap: break-word; }
                        \"\"\";
                }
            }
            """.formatted(packageName);
    }

    private static String generateProperties(String projectName) {
        return """
            # JWeb Application Configuration
            spring.application.name=%s
            server.port=8080

            # Development settings
            jweb.dev.hot-reload=true

            # Logging
            logging.level.com.osmig.Jweb=DEBUG
            """.formatted(projectName);
    }

    private static String generateGitignore() {
        return """
            # Compiled files
            target/
            *.class

            # IDE
            .idea/
            *.iml
            .vscode/
            .settings/
            .project
            .classpath

            # OS
            .DS_Store
            Thumbs.db

            # Logs
            *.log
            logs/

            # Maven
            pom.xml.tag
            pom.xml.releaseBackup
            pom.xml.versionsBackup
            release.properties
            """;
    }
}
