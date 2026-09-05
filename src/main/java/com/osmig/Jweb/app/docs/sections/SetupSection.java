package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import com.osmig.Jweb.app.docs.DocStyles;
import com.osmig.Jweb.app.docs.DocVersions;

import static jweb.El.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class SetupSection {
    private SetupSection() {}

    public static Element render() {
        return render(DocVersions.latest());
    }

    public static Element render(String version) {
        String v = DocVersions.normalize(version);
        return section(
            docTitle("Getting Started"),
            para("A complete Hello World in three files. JWeb is a Spring Boot library — "
                 + "add one dependency and write pages in pure Java. No frontend toolchain, "
                 + "no templates, no build step."),

            docSubtitle("1. Add the Dependency"),
            para("Maven — the JitPack repository plus the dependency:"),
            dependencyBlock(v),
            para("Gradle:"),
            codeBlock("""
                    repositories { maven { url 'https://jitpack.io' } }
                    dependencies { implementation 'com.github.oscar-osmig:Jweb:%s' }""".formatted(v)),
            docTip("Requires Java 21+. Spring Boot's web starter arrives transitively — "
                   + "you don't add it yourself."),
            warn("Upgrading from 1.x: 2.0.0 is source-compatible but NOT binary-compatible, "
                 + "so recompile against the new jar instead of dropping it in beside the old "
                 + "one. Two call sites still compile but changed meaning: textarea(\"hello\") "
                 + "now renders that text instead of setting name=\"hello\" (use "
                 + "textarea(name(\"bio\"))), and JSHistory.pushState now reads (state, url) in "
                 + "platform order. See dsl-simplification.md for the full migration guide."),

            docSubtitle("2. Project Structure"),
            para("Three files. Java allows one public class per file, so each of these is "
                 + "its own file — putting them in one file will not compile:"),
            codeBlock("""
                    src/main/java/org/example/
                        App.java              <- starts the application
                        Routes.java           <- maps URLs to pages
                        pages/
                            HomePage.java     <- the page itself"""),

            docSubtitle("3. App.java"),
            para("@JWebApplication replaces @SpringBootApplication. Framework beans arrive "
                 + "through auto-configuration, so only your own package is scanned."),
            codeBlock("""
                    package org.example;

                    import jweb.JWebApplication;
                    import org.springframework.boot.SpringApplication;

                    @JWebApplication
                    public class App {
                        public static void main(String[] args) {
                            SpringApplication.run(App.class, args);
                        }
                    }"""),

            docSubtitle("4. Routes.java"),
            para("A @Component implementing JWebRoutes. Page routes render a Template; "
                 + "router routes take the request and can return anything."),
            codeBlock("""
                    package org.example;

                    import jweb.JWeb;
                    import jweb.JWebRoutes;
                    import jweb.Middlewares;
                    import org.example.pages.HomePage;
                    import org.springframework.stereotype.Component;

                    @Component
                    public class Routes implements JWebRoutes {
                        @Override
                        public void configure(JWeb app) {
                            app.use(Middlewares.recommended());   // security headers, request ids

                            app.pages("/", HomePage.class);
                            app.get("/hello", req -> "Hello from JWeb");
                        }
                    }"""),

            docSubtitle("5. pages/HomePage.java"),
            para("A page is any class implementing Template. Elements come from El, styles "
                 + "from the CSS DSL — both fully type-checked."),
            codeBlock("""
                    package org.example.pages;

                    import jweb.Element;
                    import jweb.Template;

                    import static jweb.El.*;
                    import static jweb.Css.*;

                    public class HomePage implements Template {
                        @Override
                        public Element render() {
                            return div(style().maxWidth(px(700)).margin(zero, auto).padding(rem(3)),
                                h1(style().fontSize(rem(2)).fontWeight(700), "Welcome"),
                                p(style().color(hex("#64748b")), "Your first JWeb page.")
                            );
                        }
                    }"""),
            warn("Watch your IDE's auto-import: div, h1, p and text must all come from the "
                 + "single El.* import above. If it offers javax.management.Query.div or "
                 + "com.mongodb.client.model.Indexes.text, reject it — those compile but "
                 + "are not JWeb."),

            docSubtitle("6. Run It"),
            codeBlock("""
                    mvn spring-boot:run

                    #  /       -> your page
                    #  /hello  -> Hello from JWeb"""),
            docTip("Default port is 8080 — set server.port in application.properties "
                   + "or application.yaml to change it."),

            docSubtitle("Next: a Shared Layout (optional)"),
            para("Once you have more than one page, a layout gives them a common shell. "
                 + "It is a Template that takes the page content:"),
            codeBlock("""
                    public class MainLayout implements Template {
                        private final Element content;
                        public MainLayout(Element content) { this.content = content; }

                        @Override
                        public Element render() {
                            return html(
                                head(metaCharset(), metaViewport(), title("My App")),
                                body(nav(a(href("/"), "Home")), main(content))
                            );
                        }
                    }

                    // in Routes.configure:
                    app.layout(MainLayout.class)
                       .pages("/", HomePage.class,
                              "/about", AboutPage.class);"""),

            docSubtitle("Configuration (optional)"),
            para("Everything has a working default — set only what you need:"),
            codeBlock("""
                    server:
                      port: 8080

                    jweb:
                      dev:
                        debug: false          # stack traces on error pages (dev only)

                      data:                   # MongoDB, off by default
                        enabled: false

                      ai:                     # built-in AI, off by default
                        enabled: false
                        api-key: ${AI_API_KEY:}"""),
            warn("Keep secrets in environment variables (${AI_API_KEY:}) so they never "
                 + "reach your repository or a published jar."),

            docSubtitle("Skip the Setup"),
            para("The CLI generates this whole structure, wired and ready to run:"),
            codeBlock("""
                    jweb new myapp --package=com.mycompany.myapp
                    cd myapp && mvn spring-boot:run"""),

            docTip("Next: Elements for the HTML DSL, Styling for CSS, Fragments for "
                   + "server-driven UI without writing JavaScript.")
        );
    }

    /**
     * The Maven snippet with a version chip next to the copy button. The chip
     * shows the version the docs are rendered for and opens a dropdown of the
     * three newest versions; choosing one navigates with {@code ?v=}, which re-renders the
     * whole docs site — snippet, sidebar and content — for that version.
     */
    private static Element dependencyBlock(String version) {
        return div(attrs().class_("doc-code").style(s -> s.position("relative")),
            pre(attrs().style(DocStyles.codeBlock()), code(text("""
                    <repositories>
                        <repository>
                            <id>jitpack.io</id>
                            <url>https://jitpack.io</url>
                        </repository>
                    </repositories>

                    <dependency>
                        <groupId>com.github.oscar-osmig</groupId>
                        <artifactId>Jweb</artifactId>
                        <version>%s</version>
                    </dependency>""".formatted(version)))),
            versionPicker(version),
            button(attrs().class_("code-copy-btn").type("button")
                .aria("label", "Copy code to clipboard"), text("Copy")));
    }

    /**
     * Pure HTML dropdown (details/summary) — no script, styled in DocsPage.
     * Lists the newest versions, plus the selected one when it is older,
     * so the current entry is always there to be marked.
     */
    private static Element versionPicker(String version) {
        java.util.List<String> listed = new java.util.ArrayList<>(DocVersions.recent());
        if (!listed.contains(version)) listed.add(version);
        return details(attrs().class_("ver-picker"),
            summary(attrs().aria("label", "Change documentation version"),
                text(version + " ▾")),
            div(attrs().class_("ver-picker-menu"),
                each(listed, v ->
                    a(attrs().href(DocVersions.href("setup", v))
                        .class_(v.equals(version) ? "ver-picker-item current" : "ver-picker-item"),
                        text(DocVersions.isLatest(v) ? v + " (latest)" : v)))));
    }
}
