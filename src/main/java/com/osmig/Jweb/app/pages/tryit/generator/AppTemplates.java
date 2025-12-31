package com.osmig.Jweb.app.pages.tryit.generator;

/** Generates application Java files for the starter project. */
public final class AppTemplates {
    private AppTemplates() {}

    public static String appClass() {
        return """
            package com.jweb.app;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            import org.springframework.context.annotation.ComponentScan;

            @SpringBootApplication
            @ComponentScan(basePackages = {"com.jweb"})
            public class App {
                public static void main(String[] args) {
                    SpringApplication.run(App.class, args);
                }
            }
            """;
    }

    public static String routesClass() {
        return """
            package com.jweb.app;

            import com.jweb.framework.JWeb;
            import com.jweb.framework.JWebRoutes;
            import com.jweb.app.layout.Layout;
            import com.jweb.app.pages.HomePage;
            import org.springframework.stereotype.Component;

            @Component
            public class Routes implements JWebRoutes {
                @Override
                public void configure(JWeb app) {
                    app.layout(Layout.class).pages("/", HomePage.class);
                    app.get("/api/hello", ctx -> "Hello from JWeb!");
                }
            }
            """;
    }
}
