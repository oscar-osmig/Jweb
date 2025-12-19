package com.osmig.Jweb.framework.data;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Auto-configuration for JWeb database support.
 *
 * <p>Provides sensible defaults for JPA/Hibernate configuration:</p>
 * <ul>
 *   <li>Enables JPA repositories scanning from the application base package</li>
 *   <li>Configures transaction management</li>
 *   <li>Sets up H2 in-memory database for development (when no datasource configured)</li>
 *   <li>Provides sensible Hibernate defaults</li>
 * </ul>
 *
 * <p>This configuration is only activated when:</p>
 * <ul>
 *   <li>Spring Data JPA is on the classpath</li>
 *   <li>A DataSource class is available</li>
 *   <li>jweb.data.enabled is not explicitly set to false</li>
 * </ul>
 *
 * <h2>Zero-config Development</h2>
 * <pre>{@code
 * // Just add the dependency - H2 in-memory database works automatically
 * @Entity
 * public class User {
 *     @Id @GeneratedValue
 *     private Long id;
 *     private String name;
 * }
 *
 * public interface UserRepository extends JpaRepository<User, Long> {}
 * }</pre>
 *
 * <h2>Production Configuration</h2>
 * <pre>{@code
 * # application.properties
 * spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
 * spring.datasource.username=postgres
 * spring.datasource.password=secret
 * }</pre>
 *
 * @see JWebDataProperties
 * @see JWebDataConfigurer
 */
@AutoConfiguration(
    before = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
@ConditionalOnClass({DataSource.class, JpaRepository.class})
@ConditionalOnProperty(
    prefix = "jweb.data",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(JWebDataProperties.class)
@EnableTransactionManagement
public class JWebDataAutoConfiguration {

    /**
     * Provides JWeb-specific Hibernate customization.
     *
     * @param properties the JWeb data properties
     * @return the configurer bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JWebDataConfigurer jwebDataConfigurer(JWebDataProperties properties) {
        return new JWebDataConfigurer(properties);
    }
}
