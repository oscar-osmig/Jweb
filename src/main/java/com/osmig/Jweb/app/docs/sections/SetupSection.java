package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SetupSection {
    private SetupSection() {}

    public static Element render() {
        return section(
            title("Getting Started"),

            subtitle("Project Structure"),
            code("""
                src/main/java/com/yourapp/
                ├── App.java          # Entry point
                ├── Routes.java       # Route definitions
                ├── layout/           # Layout components
                │   ├── Layout.java
                │   └── Nav.java
                └── pages/            # Page components
                    └── HomePage.java"""),

            subtitle("Your First Route"),
            code("""
                @Component
                public class Routes implements JWebRoutes {
                    public void configure(JWeb app) {
                        app.get("/", () -> h1("Hello World"));

                        app.get("/about", ctx ->
                            new Layout("About", new AboutPage().render()).render()
                        );
                    }
                }"""),

            subtitle("Required Imports"),
            code("""
                import static com.osmig.Jweb.framework.elements.Elements.*;
                import static com.osmig.Jweb.framework.styles.CSS.*;
                import static com.osmig.Jweb.framework.styles.CSSUnits.*;
                import static com.osmig.Jweb.framework.styles.CSSColors.*;""")
        );
    }
}
