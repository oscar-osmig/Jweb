package com.osmig.Jweb.framework.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the CLI generates code that matches the real framework API and
 * detects the project's own coordinates rather than the parent pom's.
 */
class JWebCliTest {

    @Test
    void generatedRoutesImplementTheRealInterface() {
        String routes = JWebCli.generateRoutes("com.acme.app");
        assertTrue(routes.contains("implements JWebRoutes"));
        // The real interface is configure(JWeb app), not configure(Router router).
        assertTrue(routes.contains("void configure(JWeb app)"),
                "generated Routes must match JWebRoutes.configure(JWeb app)");
        assertFalse(routes.contains("Router router"),
                "generated Routes must not reference the wrong signature");
        assertTrue(routes.contains("import com.osmig.Jweb.framework.JWeb;"));
    }

    @Test
    void generatedPomDeclaresTheJwebDependency() {
        String pom = JWebCli.generatePom("myapp", "com.acme.myapp");
        assertFalse(pom.contains("Add JWeb dependency here"),
                "the placeholder comment must be replaced by a real dependency");
        assertTrue(pom.contains("<artifactId>Jweb</artifactId>"),
                "generated pom must depend on the JWeb framework");
        assertTrue(pom.contains("<version>4.0.0</version>"),
                "generated pom must use a Boot version compatible with the framework");
    }

    @Test
    void generatedApplicationScansFrameworkAndAppPackages() {
        String app = JWebCli.generateApplication("com.acme.myapp");
        assertTrue(app.contains("com.osmig.Jweb"),
                "must scan the framework package so its beans load");
        assertTrue(app.contains("com.acme.myapp"),
                "must scan the application's own package");
    }

    @Test
    void detectsProjectGroupIdNotParentGroupId() {
        String pom = """
            <project>
              <parent>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>4.0.0</version>
              </parent>
              <groupId>com.acme</groupId>
              <artifactId>coolapp</artifactId>
              <version>1.0.0</version>
            </project>
            """;
        String basePackage = JWebCli.basePackageFromPom(pom);
        assertEquals("com.acme.coolapp", basePackage,
                "must read the project's groupId, not the parent's");
    }
}
