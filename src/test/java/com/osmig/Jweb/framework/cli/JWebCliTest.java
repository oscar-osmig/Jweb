package com.osmig.Jweb.framework.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What {@code jweb new} and {@code jweb generate} write must compile against
 * the framework it ships in — the generated project is a newcomer's first
 * contact with the DSL, and it had drifted three releases behind.
 */
class JWebCliTest {

    @TempDir
    Path tmp;

    @Test
    void aNewProjectCompilesAgainstThisFramework(@TempDir Path dir) throws IOException {
        Path project = dir.resolve("shop");
        JWebCli.createProject(project, "shop", "com.example.shop");

        assertTrue(Files.exists(project.resolve("pom.xml")));
        String pom = Files.readString(project.resolve("pom.xml"));
        assertTrue(pom.contains("<version>" + JWebCli.springBootVersion() + "</version>"), "the Boot line the framework builds on");
        assertTrue(pom.contains("<artifactId>Jweb</artifactId>"), pom);
        assertTrue(pom.contains("<version>" + JWebCli.dependencyVersion() + "</version>"), "this framework's own version: " + JWebCli.dependencyVersion());
        assertFalse(pom.contains("3.2.0"), "no stale Boot pin");
        assertFalse(pom.contains("v1.1.0"), "no stale JWeb pin");

        String properties = Files.readString(project.resolve("src/main/resources/application.properties"));
        assertTrue(properties.contains("server.port=8085"), "the port the Dockerfile exposes");

        String app = Files.readString(project.resolve("src/main/java/com/example/shop/Application.java"));
        assertTrue(app.contains("@JWebApplication"), app);
        String routes = Files.readString(project.resolve("src/main/java/com/example/shop/Routes.java"));
        assertTrue(routes.contains("app.layout(MainLayout.class)"), routes);
        assertTrue(routes.contains(".pages(\"/\", HomePage.class)"), routes);

        compileAll(project);
    }

    @Test
    void generatedPagesComponentsLayoutsAndFormsCompile(@TempDir Path dir) throws IOException {
        Path project = dir.resolve("shop");
        JWebCli.createProject(project, "shop", "com.example.shop");

        String pkg = "com.example.shop";
        Path src = project.resolve("src/main/java/com/example/shop");
        write(src.resolve("pages/AboutPage.java"), Templates.page(pkg, "AboutPage", new String[0]));
        write(src.resolve("components/UserCard.java"), Templates.component(pkg, "UserCard", new String[] {"name:string", "age:int"}));
        write(src.resolve("components/Badge.java"), Templates.component(pkg, "Badge", new String[0]));
        write(src.resolve("layouts/AdminLayout.java"), Templates.layout(pkg, "AdminLayout"));
        write(src.resolve("forms/SignupForm.java"), Templates.formModel(pkg, "SignupForm",
            new String[] {"name:string!", "email:email!", "bio:text", "age:int"}));
        write(src.resolve("api/UsersApi.java"), Templates.api(pkg, "UsersApi", new String[0]));

        compileAll(project);
    }

    @Test
    void versionsComeFromTheRunningFramework() {
        assertNotNull(JWebCli.frameworkVersion());
        assertTrue(JWebCli.dependencyVersion().startsWith("v") || JWebCli.dependencyVersion().endsWith("-SNAPSHOT"),
            JWebCli.dependencyVersion());
        assertTrue(JWebCli.springBootVersion().startsWith("4."), JWebCli.springBootVersion());
    }

    // ==================== helpers ====================

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    /** Compiles every .java under the project against this test's classpath (the framework and Spring). */
    private static void compileAll(Path project) throws IOException {
        List<Path> sources;
        try (Stream<Path> walk = Files.walk(project.resolve("src/main/java"))) {
            sources = walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertFalse(sources.isEmpty(), "nothing generated");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "a JDK is needed to run this test");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Path out = Files.createDirectories(project.resolve("target/classes"));
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromPaths(sources);
            List<String> options = List.of(
                "-classpath", System.getProperty("java.class.path"),
                "-d", out.toString(),
                "-proc:none", "-Xlint:none");
            boolean ok = compiler.getTask(null, fm, diagnostics, options, null, units).call();
            if (!ok) {
                List<String> errors = new ArrayList<>();
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() != Diagnostic.Kind.ERROR) continue;
                    String file = d.getSource() == null ? "?" : project.relativize(Path.of(d.getSource().toUri())).toString();
                    errors.add(file + ":" + d.getLineNumber() + " " + d.getMessage(null));
                }
                fail("generated code does not compile:\n  " + String.join("\n  ", errors));
            }
        }
    }
}
