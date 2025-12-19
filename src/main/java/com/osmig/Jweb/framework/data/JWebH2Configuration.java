package com.osmig.Jweb.framework.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for H2 database when on classpath.
 *
 * <p>Provides zero-config H2 in-memory database for development.
 * This configuration activates when:</p>
 * <ul>
 *   <li>H2 driver is on classpath</li>
 *   <li>jweb.data.h2-console is not set to false</li>
 * </ul>
 *
 * <p>Access the H2 console at: http://localhost:8080/h2-console</p>
 * <ul>
 *   <li>JDBC URL: jdbc:h2:mem:jwebdb</li>
 *   <li>Username: sa</li>
 *   <li>Password: (empty)</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(name = "org.h2.Driver")
@ConditionalOnProperty(
    prefix = "jweb.data",
    name = "h2-console",
    havingValue = "true",
    matchIfMissing = true
)
public class JWebH2Configuration {

    private static final Logger log = LoggerFactory.getLogger(JWebH2Configuration.class);

    /**
     * Logs H2 console availability message on startup.
     *
     * @param env the Spring environment
     * @return info bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "jwebH2Info")
    public JWebH2Info jwebH2Info(Environment env) {
        return new JWebH2Info(env);
    }

    /**
     * Simple info bean that logs H2 console availability.
     */
    public static class JWebH2Info {

        public JWebH2Info(Environment env) {
            String port = env.getProperty("server.port", "8080");
            String h2Enabled = env.getProperty("spring.h2.console.enabled", "true");
            String h2Path = env.getProperty("spring.h2.console.path", "/h2-console");

            if ("true".equals(h2Enabled)) {
                log.info("""

                    ====================================
                    H2 Console available at:
                    http://localhost:{}{}
                    JDBC URL: jdbc:h2:mem:jwebdb
                    Username: sa
                    Password: (empty)
                    ====================================
                    """, port, h2Path);
            }
        }
    }
}
