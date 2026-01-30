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

    @Bean
    public ApplicationRunner mongoInitializer() {
        return args -> {
            if (dataEnabled) {
                Mongo.connect(mongoUri, mongoDatabase);
            }
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
