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
@org.springframework.context.annotation.ComponentScan("com.osmig.Jweb.framework")
public class JWebAutoConfiguration {

    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(JWeb.class)
    public JWeb jweb() {
        jweb.JWeb app = jweb.JWeb.create();

        // Configure routes from all routes beans — both the short-import
        // jweb.JWebRoutes and the legacy framework JWebRoutes — in a
        // deterministic order: @Order/Ordered first, then alphabetical by
        // bean name.
        Map<String, Object> routesBeans = new java.util.TreeMap<>();
        routesBeans.putAll(context.getBeansOfType(jweb.JWebRoutes.class));
        routesBeans.putAll(context.getBeansOfType(JWebRoutes.class));
        routesBeans.values().stream()
            .distinct()
            .sorted(org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE)
            .forEach(routes -> {
                if (routes instanceof jweb.JWebRoutes r) {
                    r.configure(app);
                } else if (routes instanceof JWebRoutes r) {
                    r.configure(app);
                }
            });

        return app;
    }
}
