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
 *   <li>@Api annotated classes are auto-discovered by Spring as REST controllers</li>
 * </ul>
 *
 * <h2>API Controllers</h2>
 * <p>Since JWeb's @Api annotation includes @RestController, API classes are
 * automatically registered by Spring's component scanning. Just place your
 * @Api classes in the component scan path.</p>
 *
 * <pre>{@code
 * @Api("/api/v1/users")
 * public class UserApi {
 *     @Get
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
