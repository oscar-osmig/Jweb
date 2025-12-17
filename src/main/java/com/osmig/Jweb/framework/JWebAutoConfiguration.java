package com.osmig.Jweb.framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Auto-configuration for JWeb.
 *
 * Automatically discovers JWebRoutes beans and configures them.
 */
@Configuration
public class JWebAutoConfiguration {

    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(JWeb.class)
    public JWeb jweb() {
        JWeb app = JWeb.create();

        // Configure routes from all JWebRoutes beans
        Map<String, JWebRoutes> routesBeans = context.getBeansOfType(JWebRoutes.class);
        for (JWebRoutes routes : routesBeans.values()) {
            routes.configure(app);
        }

        return app;
    }
}
