package com.osmig.Jweb.framework.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What {@code jweb generate} refuses, and what it spells the way the
 * framework will read it back: names that compile, columns that match
 * {@code Sql.snakeCase}, a migration that parses, the package the project
 * really has, and a layout that reaches the generated pages.
 */
class GenerateGuardsTest {

    @Test
    void namesThatWouldNotCompileAreRefusedBeforeAnythingIsWritten() {
        assertEquals("OrderItem", JWebCli.requireIdentifier("name", "OrderItem"));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.requireIdentifier("name", "order-item"));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.requireIdentifier("name", "2things"));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.requireIdentifier("name", "class"));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.requireIdentifier("name", ""));

        assertDoesNotThrow(() -> JWebCli.checkFields(new String[]{"title:string!", "done:boolean", "dueAt:datetime"}));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.checkFields(new String[]{"id:long"}), "the id is generated");
        assertThrows(IllegalArgumentException.class, () -> JWebCli.checkFields(new String[]{"name:string", "Name:text"}), "listed twice");
        assertThrows(IllegalArgumentException.class, () -> JWebCli.checkFields(new String[]{"my-field:string"}));
        assertThrows(IllegalArgumentException.class, () -> JWebCli.checkFields(new String[]{":int"}));
    }

    @Test
    void columnsAreSpelledTheWaySqlReadsThemBack() {
        assertEquals("image_url", Templates.Field.parse("imageURL:string").column());
        assertEquals("user_id", Templates.Field.parse("userID:long").column());
        assertEquals("created_at", Templates.Field.parse("createdAt:datetime").column());
        assertEquals("title", Templates.Field.parse("title").column());
    }

    @Test
    void aRequiredFlagIsNotNullOnce() {
        String migration = Templates.migration("Task", new String[]{"done:boolean!", "title:string!"});
        String done = migration.lines().filter(l -> l.trim().startsWith("done ")).findFirst().orElseThrow().trim();
        assertEquals("done boolean not null default false,", done, "`not null not null` was a syntax error");
        String title = migration.lines().filter(l -> l.trim().startsWith("title ")).findFirst().orElseThrow();
        assertTrue(title.contains(" not null"), title);
        assertFalse(migration.contains("not null not null"), migration);
    }

    @Test
    void thePackageIsTheApplicationClasssNotTheParentPomsGroupId(@TempDir Path dir) throws IOException {
        Path src = dir.resolve("src/main/java/com/acme/shop");
        Files.createDirectories(src.resolve("pages"));
        Files.writeString(src.resolve("Application.java"), """
            package com.acme.shop;

            import jweb.JWebApplication;

            @JWebApplication
            public class Application {}
            """);
        Files.writeString(src.resolve("pages/HomePage.java"), "package com.acme.shop.pages;\npublic class HomePage {}\n");
        assertEquals("com.acme.shop", JWebCli.applicationPackage(dir.resolve("src/main/java")));
        assertNull(JWebCli.applicationPackage(dir.resolve("src/test/java")), "no sources, no package");
    }

    @Test
    void theAppsRoutesRunBeforeGeneratedResourceRoutes(@TempDir Path dir) throws IOException {
        Path project = dir.resolve("shop");
        JWebCli.createProject(project, "shop", "com.example.shop");
        String routes = Files.readString(project.resolve("src/main/java/com/example/shop/Routes.java"));
        assertTrue(routes.contains("@Order(0)"), "unordered beans configure after ordered ones, by name — productRoutes would beat routes");
        assertTrue(routes.contains("import org.springframework.core.annotation.Order;"), routes);
        assertFalse(Templates.resourceRoutes("com.example.shop", "Product").contains("in name order after any @Order"),
            "the old Javadoc described an order that did not hold");
    }

    @Test
    void migrationNumbersThatAreNotASequenceAreLeftAlone(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("V7__a.sql"), "");
        Files.writeString(dir.resolve("V20240101120000__stamp.sql"), "");
        assertEquals(8, JWebCli.nextMigrationVersion(dir), "a timestamp is not the number to continue from");
    }
}
