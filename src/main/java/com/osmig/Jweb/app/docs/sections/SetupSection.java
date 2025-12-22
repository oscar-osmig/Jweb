package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SetupSection {
    private SetupSection() {}

    public static Element render() {
        return section(
            title("Getting Started"),
            text("JWeb is built on Spring Boot. Create a new project with Spring Initializr " +
                 "or add JWeb to an existing Spring Boot project."),

            subtitle("Project Structure"),
            code("""
src/main/java/com/yourapp/
├── App.java          # @SpringBootApplication entry
├── Routes.java       # URL to handler mappings
├── layout/           # Shared layout components
│   ├── Layout.java   # HTML wrapper (head, body)
│   ├── Nav.java      # Navigation bar
│   └── Theme.java    # Colors, spacing, typography
└── pages/            # Page components
    ├── HomePage.java
    └── AboutPage.java"""),

            subtitle("Required Imports"),
            text("Add these static imports to use JWeb's DSL:"),
            code("""
import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;"""),

            subtitle("Your First Route"),
            code("""
@Component
public class Routes implements JWebRoutes {
    public void configure(JWeb app) {
        app.get("/", () -> h1("Hello World"));
    }
}"""),

            subtitle("Run the App"),
            code("mvn spring-boot:run\n# Visit http://localhost:8080")
        );
    }
}
