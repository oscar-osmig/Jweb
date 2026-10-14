package com.osmig.Jweb.framework.config;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.routing.Router;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring configuration for JWeb framework.
 */
@Configuration
public class JWebConfiguration implements WebMvcConfigurer {

    @Value("${jweb.data.enabled:false}")
    private boolean dataEnabled;

    @Value("${jweb.dev.debug:false}")
    private boolean devDebug;

    @Value("${jweb.dev.strict-html:false}")
    private boolean strictHtml;

    @Value("${jweb.runtime.enabled:true}")
    private boolean runtimeEnabled;

    @Value("${jweb.runtime.expired:banner}")
    private String runtimeExpired;

    @Value("${jweb.runtime.reconnect-attempts:8}")
    private int runtimeReconnectAttempts;

    @Value("${jweb.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${jweb.ai.base-url:https://api.openai.com/v1}")
    private String aiBaseUrl;

    @Value("${jweb.ai.api-key:}")
    private String aiApiKey;

    @Value("${jweb.ai.model:gpt-4o-mini}")
    private String aiModel;

    @Value("${jweb.ai.temperature:0.7}")
    private double aiTemperature;

    @Value("${jweb.ai.timeout-seconds:60}")
    private long aiTimeoutSeconds;

    /**
     * MongoDB connects in {@code db.mongo.MongoConfiguration}, which only
     * exists when the driver does. This is the message for the other case:
     * the property is on and the dependency is missing.
     */
    @Bean
    public ApplicationRunner mongoDriverCheck() {
        return args -> {
            if (!dataEnabled) return;
            try {
                Class.forName("com.mongodb.client.MongoClient", false, getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                com.osmig.Jweb.framework.util.Log.error(
                    "jweb.data.enabled is true but the MongoDB driver is not on the classpath. "
                    + "Add org.mongodb:mongodb-driver-sync to use jweb.Mongo, or set jweb.data.enabled=false.");
            }
        };
    }

    @Bean
    public ApplicationRunner jwebFrameworkSettings() {
        return args -> {
            com.osmig.Jweb.framework.server.ErrorPage.setDebug(devDebug);
            com.osmig.Jweb.framework.vdom.HtmlNesting.setStrict(strictHtml);
            // The framework's own health signal: how many pages hold server state
            jweb.Metrics.gauge("jweb.contexts.active", com.osmig.Jweb.framework.state.StateManager::activeContexts);
            com.osmig.Jweb.framework.js.JWebRuntime.setEnabled(runtimeEnabled);
            com.osmig.Jweb.framework.js.JWebRuntime.setExpiredAction(
                com.osmig.Jweb.framework.js.JWebRuntime.ExpiredAction.parse(runtimeExpired));
            com.osmig.Jweb.framework.js.JWebRuntime.setReconnectAttempts(runtimeReconnectAttempts);
            com.osmig.Jweb.framework.ai.AI.configure(new com.osmig.Jweb.framework.ai.AiConfig()
                .enabled(aiEnabled)
                .baseUrl(aiBaseUrl)
                .apiKey(aiApiKey)
                .model(aiModel)
                .temperature(aiTemperature)
                .timeoutSeconds(aiTimeoutSeconds));
        };
    }

    /**
     * Warms the rendering pipeline in the background right after startup:
     * pre-renders every registered page route once (DSL static init, JIT,
     * lazy beans) so the first real request doesn't pay that cost. With
     * spring.main.lazy-initialization=true this is what makes the first
     * page view fast instead of taking seconds.
     */
    @Bean
    public ApplicationRunner jwebWarmup(JWeb jweb) {
        return args -> {
            Thread warmup = new Thread(() -> {
                for (var route : jweb.getPageRegistry().getRoutes()) {
                    // A page built from the request cannot be rendered ahead of one
                    if (route.needsRequest()) continue;
                    try {
                        com.osmig.Jweb.framework.state.StateManager.withContext(
                            () -> route.page(null).render().toHtml());
                    } catch (Exception e) {
                        com.osmig.Jweb.framework.util.Log.debug(
                            "Warmup skipped {}: {}", route.path(), e.getMessage());
                    }
                }
            }, "jweb-warmup");
            warmup.setDaemon(true);
            warmup.start();
        };
    }

    @Bean
    public Router jwebRouter(JWeb jweb) {
        return jweb.getRouter();
    }

    @Bean
    public com.osmig.Jweb.framework.middleware.MiddlewareStack jwebMiddlewareStack(JWeb jweb) {
        return jweb.getMiddlewareStack();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.svg")
            .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/public/**")
            .addResourceLocations("classpath:/public/");
    }
}
