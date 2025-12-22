package com.osmig.Jweb.framework.data;

import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the H2 Console servlet for Spring Boot 4.0+.
 */
@Configuration
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2ConsoleConfig {

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ServletRegistrationBean<?> h2servletRegistration() {
        Servlet servlet = findH2Servlet();
        if (servlet == null) {
            throw new IllegalStateException("H2 Console servlet not found. Make sure H2 is on the classpath.");
        }

        ServletRegistrationBean registration = new ServletRegistrationBean(servlet);
        registration.addUrlMappings("/h2-console/*");
        registration.setLoadOnStartup(1);
        return registration;
    }

    private Servlet findH2Servlet() {
        // Try Jakarta servlet first (H2 2.2+)
        String[] servletClasses = {
            "org.h2.server.web.JakartaWebServlet",
            "org.h2.server.web.WebServlet"
        };

        for (String className : servletClasses) {
            try {
                Class<?> servletClass = Class.forName(className);
                return (Servlet) servletClass.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
                // Try next class
            }
        }
        return null;
    }
}
