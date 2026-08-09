package com.osmig.Jweb.framework.config;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.db.mongo.Mongo;
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

    @Value("${jweb.data.mongo.uri:mongodb://localhost:27017}")
    private String mongoUri;

    @Value("${jweb.data.mongo.database:myapp}")
    private String mongoDatabase;

    @Value("${jweb.dev.debug:false}")
    private boolean devDebug;

    @Value("${jweb.runtime.enabled:true}")
    private boolean runtimeEnabled;

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

    @Bean
    public ApplicationRunner mongoInitializer() {
        return args -> {
            if (dataEnabled) {
                Mongo.connect(mongoUri, mongoDatabase);
            }
        };
    }

    @Bean
    public ApplicationRunner jwebFrameworkSettings() {
        return args -> {
            com.osmig.Jweb.framework.server.ErrorPage.setDebug(devDebug);
            com.osmig.Jweb.framework.js.JWebRuntime.setEnabled(runtimeEnabled);
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
                    try {
                        com.osmig.Jweb.framework.state.StateManager.withContext(
                            () -> route.pageSupplier().get().render().toHtml());
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
