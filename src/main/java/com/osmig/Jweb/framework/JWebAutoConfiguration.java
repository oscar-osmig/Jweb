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
 * <p>Automatically discovers:</p>
 * <ul>
 *   <li>JWebRoutes beans for page configuration</li>
 *   <li>@REST annotated classes are auto-discovered by Spring as REST controllers</li>
 * </ul>
 *
 * <h2>API Controllers</h2>
 * <p>Since JWeb's @REST annotation includes @RestController, API classes are
 * automatically registered by Spring's component scanning. Just place your
 * @REST classes in the component scan path (paths must start with /api/v*).</p>
 *
 * <pre>{@code
 * @REST("/api/v1/users")
 * public class UserApi {
 *     @GET
 *     public List<User> getAll() { ... }
 * }
 * }</pre>
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
