package com.osmig.Jweb.framework.config;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.routing.Router;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring configuration for JWeb framework.
 */
@Configuration
public class JWebConfiguration implements WebMvcConfigurer {

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
