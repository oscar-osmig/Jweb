package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SetupSection {
    private SetupSection() {}

    public static Element render() {
        return section(
            docTitle("Getting Started"),
            para("JWeb is a Spring Boot library published on JitPack. Add one dependency, "
                 + "annotate your application class, and write pages in pure Java — "
                 + "no frontend toolchain, no templates."),

            docSubtitle("1. Add the Dependency"),
            para("Maven — add the JitPack repository and the dependency:"),
            codeBlock("""
                    <repositories>
                        <repository>
                            <id>jitpack.io</id>
                            <url>https://jitpack.io</url>
                        </repository>
                    </repositories>

                    <dependency>
                        <groupId>com.github.oscar-osmig</groupId>
                        <artifactId>Jweb</artifactId>
                        <version>v1.0.3</version>
                    </dependency>"""),
            para("Gradle:"),
            codeBlock("""
                    repositories { maven { url 'https://jitpack.io' } }
                    dependencies { implementation 'com.github.oscar-osmig:Jweb:v1.0.3' }"""),
            docTip("Requires Java 21+. Spring Boot's web starter comes transitively — you "
                   + "don't need to add it. Use main-SNAPSHOT to track the latest commit."),

            docSubtitle("2. Your Application Class"),
            para("@JWebApplication replaces @SpringBootApplication. The framework's beans "
                 + "arrive through Spring Boot auto-configuration, so only your own package "
                 + "is component-scanned."),
            codeBlock("""
                    @JWebApplication
                    public class App {
                        public static void main(String[] args) {
                            SpringApplication.run(App.class, args);
                        }
                    }"""),

            docSubtitle("3. Define Routes"),
            para("Implement JWebRoutes in a @Component. Page routes render Templates; "
                 + "router routes take the request and can return anything."),
            codeBlock("""
                    @Component
                    public class Routes implements JWebRoutes {
                        @Override
                        public void configure(JWeb app) {
                            // Production baseline: security headers, request ids, compression
                            app.use(Middlewares.recommended());

                            app.layout(MainLayout.class)      // optional shared layout
                               .pages("/", HomePage.class,
                                      "/about", AboutPage.class);

                            app.get("/hello", req -> "Hello from JWeb");
                        }
                    }"""),

            docSubtitle("4. Create a Page"),
            para("A page is any class implementing Template. Elements come from El, "
                 + "styles from the CSS DSL — both fully type-checked."),
            codeBlock("""
                    import static com.osmig.Jweb.framework.elements.El.*;
                    import static com.osmig.Jweb.framework.styles.CSS.*;
                    import static com.osmig.Jweb.framework.styles.CSSUnits.*;
                    import static com.osmig.Jweb.framework.styles.CSSColors.*;

                    public class HomePage implements Template {
                        @Override
                        public Element render() {
                            return div(style().maxWidth(px(700)).margin(zero, auto).padding(rem(3)),
                                h1(style().fontSize(rem(2)).fontWeight(700), text("Welcome")),
                                p(style().color(hex("#64748b")), text("Your first JWeb page."))
                            );
                        }
                    }"""),
            docTip("Import El.* and CSS.* together — El for elements, CSS for style(). "
                   + "Don't wildcard-import Elements.* alongside El.*; they share names."),

            docSubtitle("5. Run It"),
            codeBlock("""
                    mvn spring-boot:run     # http://localhost:8080

                    jweb build              # production jar + Dockerfile
                    java -jar target/*-exec.jar"""),

            docSubtitle("Configuration (optional)"),
            para("Everything below has a working default — configure only what you need "
                 + "in application.yaml:"),
            codeBlock("""
                    server:
                      port: 8080

                    jweb:
                      # Show stack traces on error pages (development only)
                      dev:
                        debug: false

                      # MongoDB — off by default; the app runs fine without it
                      data:
                        enabled: false
                        mongo:
                          uri: ${MONGO_URI:mongodb://localhost:27017}
                          database: ${MONGO_DB:myapp}

                      # Built-in AI (chat, agents, tools) — any OpenAI-compatible API
                      ai:
                        enabled: false
                        base-url: ${AI_BASE_URL:https://api.openai.com/v1}
                        api-key: ${AI_API_KEY:}
                        model: ${AI_MODEL:gpt-4o-mini}"""),
            warn("Keep secrets out of application.yaml — use environment variables "
                 + "(${AI_API_KEY:}) so tokens never reach your repository or a published jar."),

            docSubtitle("Scaffolding"),
            para("The CLI generates a ready-to-run project with the dependency, "
                 + "application class, routes, layout, and a home page already wired:"),
            codeBlock("""
                    jweb new myapp --package=com.mycompany.myapp
                    cd myapp && mvn spring-boot:run"""),

            docTip("Next: Elements for the HTML DSL, Styling for CSS, Fragments for "
                   + "server-driven UI without JavaScript.")
        );
    }
}
