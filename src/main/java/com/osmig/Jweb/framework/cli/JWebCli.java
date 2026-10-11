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

    /** The framework version this CLI ships in — what a generated pom depends on. */
    private static final String VERSION = frameworkVersion();
    private static String basePackage = "com.example";
    private static Path projectRoot = Paths.get(".");

    /**
     * The version of the jar this runs from: {@code build-info.properties} (a
     * dev run or the jar), then Maven's {@code pom.properties}; a checkout
     * without either falls back to JitPack's {@code main-SNAPSHOT}.
     */
    static String frameworkVersion() {
        String v = readProperty("META-INF/build-info.properties", "build.version");
        if (v == null) v = readProperty("META-INF/maven/com.osmig/Jweb/pom.properties", "version");
        return v == null ? "main-SNAPSHOT" : v;
    }

    private static String readProperty(String resource, String key) {
        try (java.io.InputStream in = JWebCli.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            java.util.Properties p = new java.util.Properties();
            p.load(in);
            String v = p.getProperty(key);
            return v == null || v.isBlank() ? null : v.trim();
        } catch (IOException e) {
            return null;
        }
    }

    /** The Spring Boot line the framework itself builds on — the generated pom's parent. */
    static String springBootVersion() {
        String v = org.springframework.boot.SpringBootVersion.getVersion();
        return v == null ? "4.0.8" : v;
    }

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
                case "build" -> buildProject();
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

    // ==================== Build ====================

    /**
     * Production build: packages the runnable jar and ensures a multi-stage
     * Dockerfile and .dockerignore exist (never overwrites your own).
     */
    private static void buildProject() throws IOException, InterruptedException {
        java.nio.file.Path here = java.nio.file.Path.of("");

        java.nio.file.Path dockerfile = here.resolve("Dockerfile");
        if (!java.nio.file.Files.exists(dockerfile)) {
            writeFile(dockerfile, dockerfileTemplate());
            System.out.println("  created Dockerfile (multi-stage, JRE runtime)");
        }
        java.nio.file.Path dockerignore = here.resolve(".dockerignore");
        if (!java.nio.file.Files.exists(dockerignore)) {
            writeFile(dockerignore, "target/\n.git/\n.idea/\n*.iml\n.tools/\n");
            System.out.println("  created .dockerignore");
        }

        System.out.println("  packaging (mvn package -DskipTests)...");
        String mvn = java.nio.file.Files.exists(here.resolve("mvnw")) ? "./mvnw" : "mvn";
        Process process = new ProcessBuilder(mvn, "package", "-DskipTests", "-q")
            .inheritIO()
            .start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Build failed (mvn exit " + process.exitValue() + ")");
        }

        System.out.println("""

            Build complete.
              Run locally:   java -jar target/*.jar
              Build image:   docker build -t myapp .
              Run container: docker run -p 8085:8085 myapp
            """);
    }

    private static String dockerfileTemplate() {
        // A generated project has no Maven wrapper, so the build stage brings
        // its own Maven; the runtime stage is a JRE only.
        return """
            # Build stage
            FROM maven:3.9-eclipse-temurin-21-alpine AS build
            WORKDIR /app
            COPY pom.xml ./
            RUN mvn -B -q dependency:go-offline
            COPY src/ src/
            RUN mvn -B -q package -DskipTests

            # Runtime stage
            FROM eclipse-temurin:21-jre-alpine
            WORKDIR /app
            COPY --from=build /app/target/*.jar app.jar
            EXPOSE 8085
            ENTRYPOINT ["java", "-jar", "app.jar"]
            """;
    }

    // ==================== Project Creation ====================

    private static void createProject(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: jweb new <project-name> [--package=com.example]");
            return;
        }

        // The argument may be a name or a path; the project is named after its last segment
        Path projectDir = Paths.get(args[1]);
        String projectName = projectDir.getFileName().toString();
        String packageName = "com." + projectName.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Parse options
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--package=")) {
                packageName = args[i].substring(10);
            }
        }

        if (Files.exists(projectDir)) {
            System.err.println("Directory already exists: " + projectDir);
            return;
        }

        System.out.println("Creating JWeb project: " + projectName);
        System.out.println("Package: " + packageName);
        createProject(projectDir, projectName, packageName);

        System.out.println("\n\u2713 Project created successfully!");
        System.out.println("\nNext steps:");
        System.out.println("  cd " + projectDir);
        System.out.println("  mvn spring-boot:run");
        System.out.println("\nThen open http://localhost:8085 in your browser.");
        System.out.println("The tutorial walks through what was generated: readme/tutorial.md in the JWeb repository.");
    }

    /** Writes a new project into {@code projectDir}. What {@code jweb new} does, callable from tests. */
    static void createProject(Path projectDir, String projectName, String packageName) throws IOException {
        String packagePath = packageName.replace('.', '/');
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/pages"));
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/components"));
        Files.createDirectories(projectDir.resolve("src/main/java/" + packagePath + "/layouts"));
        Files.createDirectories(projectDir.resolve("src/main/resources"));
        Files.createDirectories(projectDir.resolve("src/test/java/" + packagePath));

        writeFile(projectDir.resolve("pom.xml"), generatePom(projectName, packageName));
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/Application.java"),
            generateApplication(packageName));
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/Routes.java"),
            generateRoutes(packageName));
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/pages/HomePage.java"),
            generateHomePage(packageName, projectName));
        writeFile(projectDir.resolve("src/main/java/" + packagePath + "/layouts/MainLayout.java"),
            generateMainLayout(packageName, projectName));
        writeFile(projectDir.resolve("src/main/resources/application.properties"),
            generateProperties(projectName));
        writeFile(projectDir.resolve(".gitignore"), generateGitignore());
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

        String path = entityName.toLowerCase() + "s";
        System.out.println("\nThe entity and repository need Spring Data JPA — add to pom.xml:");
        System.out.println("  spring-boot-starter-data-jpa, and a driver (h2 for a start)");
        System.out.println("\nThen register the pages in Routes.java, handing the repository in");
        System.out.println("(a page in the page table is built by the registry, not by Spring,");
        System.out.println("so it takes its collaborators through the constructor):");
        System.out.println("  @Autowired " + entityName + "Repository " + entityName.toLowerCase() + "s;");
        System.out.println("  app.pages(\"/" + path + "\", () -> new " + entityName + "ListPage(" + entityName.toLowerCase() + "s))");
        System.out.println("     .pages(\"/" + path + "/new\", () -> new " + entityName + "FormPage(" + entityName.toLowerCase() + "s, null))");
        System.out.println("     .pages(\"/" + path + "/:id\", req -> new " + entityName + "FormPage(" + entityName.toLowerCase() + "s, req.requireParamLong(\"id\")));");
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
              build                   Package for production (jar + Dockerfile/.dockerignore)
              help                    Show this help message
              version                 Show version

            Examples:
              jweb new myapp --package=com.mycompany.myapp
              jweb generate page Home
              jweb generate component UserCard
              jweb generate form User name:string! email:email! age:int
              jweb generate crud Product name:string price:double
              jweb generate api Users

            Field types: string, int, long, double, boolean, date, datetime, email, password
            (a trailing ! marks a form field required). A generated project depends on
            JWeb %s; the tutorial that walks through it is readme/tutorial.md.
            """.formatted(VERSION, dependencyVersion()));
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
                    <version>%s</version>
                </parent>

                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <name>%s</name>

                <properties>
                    <java.version>21</java.version>
                </properties>

                <repositories>
                    <repository>
                        <id>jitpack.io</id>
                        <url>https://jitpack.io</url>
                    </repository>
                </repositories>

                <dependencies>
                    <!-- JWeb brings Spring Boot web and websocket support with it -->
                    <dependency>
                        <groupId>com.github.oscar-osmig</groupId>
                        <artifactId>Jweb</artifactId>
                        <version>%s</version>
                    </dependency>
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
            """.formatted(springBootVersion(), groupId, projectName, projectName, dependencyVersion());
    }

    /** JitPack wants the tag name: {@code v3.0.2} for a release, {@code main-SNAPSHOT} for a checkout. */
    static String dependencyVersion() {
        String v = VERSION;
        if (v.endsWith("-SNAPSHOT")) return v;
        return v.startsWith("v") ? v : "v" + v;
    }

    private static String generateApplication(String packageName) {
        return """
            package %s;

            import jweb.JWebApplication;
            import org.springframework.boot.SpringApplication;

            /** The entry point. @JWebApplication is @SpringBootApplication plus the framework's wiring. */
            @JWebApplication
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

            import jweb.JWeb;
            import jweb.JWebRoutes;
            import jweb.Middlewares;
            import %s.layouts.MainLayout;
            import %s.pages.HomePage;
            import org.springframework.stereotype.Component;

            /**
             * Every route of the app, in one place. Pages go in the page table and
             * get the layout, the title hooks and the client runtime for free;
             * app.get / app.post are for handlers that answer with a fragment,
             * JSON or a redirect.
             */
            @Component
            public class Routes implements JWebRoutes {

                @Override
                public void configure(JWeb app) {
                    app.use(Middlewares.recommended());   // security headers, request ids, compression, CSRF

                    // layout() before pages(): the registry captures the default layout as pages register
                    app.layout(MainLayout.class)
                       .pages("/", HomePage.class);
                }
            }
            """.formatted(packageName, packageName, packageName);
    }

    private static String generateHomePage(String packageName, String projectName) {
        return """
            package %s.pages;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Stylesheet;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * The home page. A page is a Template: render() is the markup, styles()
             * the rules it needs (collected into the page's one stylesheet), and the
             * class names are handles declared once and shared by both.
             */
            public class HomePage implements Template {

                static final Cls HERO = cls("hero");
                static final Cls TITLE = cls("hero-title");
                static final Cls LEAD = cls("hero-lead");
                static final Cls CTA = cls("hero-cta");

                @Override
                public String pageTitle() {
                    return "%s";
                }

                @Override
                public Element render() {
                    return section(HERO,
                        h1(TITLE, "Welcome to %s"),
                        p(LEAD, "Built with JWeb — the whole app in Java."),
                        a(CTA, href("https://github.com/oscar-osmig/Jweb/blob/main/readme/tutorial.md"), "Follow the tutorial"));
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(HERO, style().textAlign(center).padding(rem(4), rem(1)))
                        .rule(TITLE, style().fontSize(rem(3)).fontWeight(700).marginBottom(rem(1)))
                        .rule(LEAD, style().fontSize(rem(1.25)).color(hex("#6b7280")).marginBottom(rem(2)))
                        .rule(CTA, style()
                            .display(inlineBlock)
                            .backgroundColor(hex("#6366f1")).color(white)
                            .padding(rem(0.75), rem(1.5)).borderRadius(px(8))
                            .textDecoration(none).fontWeight(500))
                        .rule(CTA.hover(), style().backgroundColor(hex("#4f46e5")));
                }
            }
            """.formatted(packageName, projectName, projectName);
    }

    private static String generateMainLayout(String packageName, String projectName) {
        return """
            package %s.layouts;

            import jweb.Cls;
            import jweb.Element;
            import jweb.Template;
            import jweb.css.Selector;
            import jweb.css.Stylesheet;

            import static jweb.El.*;
            import static jweb.Css.*;

            /**
             * The shell around every page: a (title, content) constructor is what
             * app.layout(MainLayout.class) looks for. The page's own pageTitle()
             * arrives as the title; its styles() and scripts() are collected into
             * the document along with this layout's.
             */
            public class MainLayout implements Template {

                static final Cls HEADER = cls("site-header");
                static final Cls BRAND = cls("site-brand");
                static final Cls MAIN = cls("site-main");
                static final Cls FOOTER = cls("site-footer");

                private final String title;
                private final Element content;

                public MainLayout(String title, Element content) {
                    this.title = title != null ? title : "%s";
                    this.content = content;
                }

                @Override
                public Element render() {
                    return html(attrs().lang("en"),
                        head(
                            metaCharset(),
                            meta("viewport", "width=device-width, initial-scale=1.0"),
                            title(title)),
                        body(
                            header(HEADER, a(BRAND, href("/"), "%s")),
                            main(MAIN, content),
                            footer(FOOTER, "Built with JWeb")));
                }

                @Override
                public Stylesheet styles() {
                    return stylesheet()
                        .rule(Selector.any(), style().boxSizing(borderBox))
                        .rule(Selector.type("body"), style()
                            .margin(zero).minHeight(vh(100)).lineHeight(1.5)
                            .fontFamily(systemUi, sansSerif)
                            .backgroundColor(hex("#f9fafb")).color(hex("#111827")))
                        .rule(HEADER, style()
                            .backgroundColor(white).borderBottom(px(1), solid, hex("#e5e7eb"))
                            .padding(rem(1), rem(2)))
                        .rule(BRAND, style()
                            .fontWeight(700).fontSize(rem(1.25))
                            .color(hex("#111827")).textDecoration(none))
                        .rule(MAIN, style().maxWidth(px(1100)).margin(zero, auto).padding(rem(2)))
                        .rule(FOOTER, style()
                            .textAlign(center).padding(rem(2))
                            .color(hex("#6b7280")).fontSize(rem(0.875)));
                }
            }
            """.formatted(packageName, projectName, projectName);
    }

    private static String generateProperties(String projectName) {
        return """
            # JWeb Application Configuration
            spring.application.name=%s
            server.port=8085

            # Development settings (the framework's own defaults are in jweb.yaml)
            jweb.dev.hot-reload=true
            jweb.dev.watch-paths=src/main/java

            # Logging
            logging.level.com.osmig.Jweb=INFO
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
