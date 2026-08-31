package com.osmig.Jweb.app.sandbox;

import jweb.Element;

import java.util.LinkedHashMap;
import java.util.Map;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * The playground's starter project — a typical JWeb app layout. DSL-mode
 * files run through the {@link SandboxDsl} interpreter as the user types;
 * static-mode files (bootstrap/config) are editable too but keep a fixed,
 * playful preview — user code in them is never evaluated.
 */
public final class SandboxFiles {
    private SandboxFiles() {}

    public enum Mode { DSL, STATIC }

    public record SandboxFile(String id, String path, Mode mode, String source, String blurb) {}

    public static final Map<String, SandboxFile> FILES = new LinkedHashMap<>();

    static {
        put("pom", "pom.xml", Mode.STATIC, SandboxFiles.POM_SRC,
            "One dependency. The rest is Java.");
        put("app", "src/main/java/demo/App.java", Mode.STATIC, SandboxFiles.APP_SRC,
            "The whole backend: one annotation, one main().");
        put("routes", "src/main/java/demo/Routes.java", Mode.STATIC, SandboxFiles.ROUTES_SRC,
            "URLs in, pages out.");
        put("home", "src/main/java/demo/pages/HomePage.java", Mode.DSL, SandboxFiles.HOME_SRC,
            "Edit the code — the preview recompiles as you type.");
        put("greeting", "src/main/java/demo/components/GreetingCard.java", Mode.DSL, SandboxFiles.GREETING_SRC,
            "A reusable component. Rename someone.");
        put("buttons", "src/main/java/demo/components/Buttons.java", Mode.DSL, SandboxFiles.BUTTONS_SRC,
            "One button, infinite moods.");
    }

    private static void put(String id, String path, Mode mode, String src, String blurb) {
        FILES.put(id, new SandboxFile(id, path, mode, src, blurb));
    }

    /** Unknown ids quietly land on the default file — no error surface. */
    public static SandboxFile byId(String id) {
        SandboxFile f = id == null ? null : FILES.get(id);
        return f != null ? f : FILES.get("home");
    }

    // ==================== starter sources ====================

    private static final String POM_SRC = """
        <project>
            <groupId>demo</groupId>
            <artifactId>my-jweb-app</artifactId>
            <version>0.1.0</version>

            <repositories>
                <repository>
                    <id>jitpack.io</id>
                    <url>https://jitpack.io</url>
                </repository>
            </repositories>

            <dependencies>
                <dependency>
                    <groupId>com.github.oscar-osmig</groupId>
                    <artifactId>Jweb</artifactId>
                    <version>v2.0.0</version>
                </dependency>
            </dependencies>
        </project>""";

    private static final String APP_SRC = """
        package demo;

        import jweb.JWebApplication;
        import org.springframework.boot.SpringApplication;

        @JWebApplication
        public class App {
            public static void main(String[] args) {
                SpringApplication.run(App.class, args);
            }
        }""";

    private static final String ROUTES_SRC = """
        package demo;

        import jweb.JWeb;
        import jweb.JWebRoutes;
        import jweb.Middlewares;
        import demo.pages.HomePage;
        import org.springframework.stereotype.Component;

        @Component
        public class Routes implements JWebRoutes {
            @Override
            public void configure(JWeb app) {
                app.use(Middlewares.recommended());

                app.pages("/", HomePage.class);
            }
        }""";

    private static final String HOME_SRC = """
        package demo.pages;

        import jweb.Element;
        import jweb.Template;

        import static jweb.El.*;
        import static jweb.Css.*;

        // Edit anything below — the preview recompiles as you type.
        public class HomePage implements Template {
            @Override
            public Element render() {
                return div(style()
                        .padding(rem(1.5))
                        .background(hex("#4f46e5"))
                        .borderRadius(px(12)).color(white),
                    h1(text("Hello, JWeb!")),
                    p(text("Rendered live, straight from this code."))
                );
            }
        }""";

    private static final String GREETING_SRC = """
        package demo.components;

        import jweb.Element;
        import jweb.Template;

        import static jweb.El.*;
        import static jweb.Css.*;

        // Try changing the name, the emoji, or the shadow.
        public class GreetingCard implements Template {
            @Override
            public Element render() {
                return div(style()
                        .padding(rem(1.5))
                        .border(px(1), solid, hex("#e2e8f0"))
                        .borderRadius(px(12))
                        .boxShadow("0 4px 14px rgba(15, 23, 42, 0.08)"),
                    h2(text("Hey, Ada! 👋")),
                    p(style().color(hex("#64748b")),
                        text("This card is one reusable Java class."))
                );
            }
        }""";

    private static final String BUTTONS_SRC = """
        package demo.components;

        import jweb.Element;
        import jweb.Template;

        import static jweb.El.*;
        import static jweb.Css.*;

        // Swap the gradient for hex("#4f46e5"), or go full 1998 with dotted borders.
        public class Buttons implements Template {
            @Override
            public Element render() {
                return button(style()
                        .padding(rem(0.6), rem(1.4))
                        .borderRadius(px(8))
                        .fontWeight(600).cursor(pointer)
                        .background(linearGradient("90deg",
                            hex("#4f46e5"), hex("#db2777")))
                        .color(white),
                    text("Deploy")
                );
            }
        }""";

    // ==================== static previews ====================

    public static Element staticPreview(String id) {
        return switch (id) {
            case "pom" -> pomPreview();
            case "routes" -> routesPreview();
            default -> terminalPreview();
        };
    }

    private static Element terminalPreview() {
        return div(style()
                .backgroundColor(BG_DARK).color(hex("#a5b4fc"))
                .padding(SP_4).borderRadius(ROUNDED_LG)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .fontSize(TEXT_SM).lineHeight(1.9),
            div(style().color(TEXT_LIGHT), text("$ ./mvnw spring-boot:run")),
            div(text("🚀 JWeb dev server on http://localhost:8085")),
            div(text("   ├─ /            → HomePage")),
            div(text("   ├─ components   → GreetingCard, Buttons")),
            div(text("   └─ hot-reload   ✓ watching")),
            div(style().color(hex("#6ee7b7")), text("Ready in 0.042s ⚡ (Java, not magic)"))
        );
    }

    private static Element routesPreview() {
        return div(style()
                .backgroundColor(BG).border(px(1), solid, BORDER)
                .borderRadius(ROUNDED_LG).padding(SP_4)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .fontSize(TEXT_SM).lineHeight(2),
            div(style().color(TEXT_LIGHT).marginBottom(SP_2), text("registered routes")),
            div(span(style().color(hex("#059669")).fontWeight(700), text("GET  ")),
                span(style().color(TEXT), text("/           → HomePage    ")),
                span(style().color(TEXT_LIGHT), text("200 ✓"))),
            div(span(style().color(hex("#059669")).fontWeight(700), text("GET  ")),
                span(style().color(TEXT), text("/teapot     → (reserved)  ")),
                span(style().color(TEXT_LIGHT), text("418 🫖")))
        );
    }

    private static Element pomPreview() {
        return div(style()
                .backgroundColor(BG_DARK).color(hex("#e2e8f0"))
                .padding(SP_4).borderRadius(ROUNDED_LG)
                .fontFamily("ui-monospace, SFMono-Regular, Menlo, monospace")
                .fontSize(TEXT_SM).lineHeight(1.9),
            div(style().color(TEXT_LIGHT), text("$ ./mvnw dependency:resolve")),
            div(text("[INFO] com.github.oscar-osmig:Jweb:v2.0.0 ✓")),
            div(text("[INFO] ...and 0 frontend build tools 🎉")),
            div(style().color(hex("#6ee7b7")), text("[INFO] BUILD SUCCESS"))
        );
    }
}
