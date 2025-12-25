package com.osmig.Jweb.app.pages.tryit.generator;

/** Generates application Java files for the starter project. */
public final class AppTemplates {
    private AppTemplates() {}

    public static String appClass() {
        return """
            package com.example.app;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            import org.springframework.context.annotation.ComponentScan;

            @SpringBootApplication
            @ComponentScan(basePackages = {"com.example"})
            public class App {
                public static void main(String[] args) {
                    SpringApplication.run(App.class, args);
                }
            }
            """;
    }

    public static String routesClass() {
        return """
            package com.example.app;

            import com.example.framework.JWeb;
            import com.example.framework.JWebRoutes;
            import com.example.app.layout.Layout;
            import com.example.app.pages.HomePage;
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
