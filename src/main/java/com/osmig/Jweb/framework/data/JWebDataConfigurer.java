package com.osmig.Jweb.framework.data;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

import java.util.Map;

/**
 * Configures Hibernate properties based on JWeb data settings.
 *
 * <p>This class customizes Hibernate configuration to provide
 * sensible defaults for JWeb applications.</p>
 *
 * <p>Applied customizations:</p>
 * <ul>
 *   <li>DDL auto strategy from jweb.data.ddl-auto</li>
 *   <li>SQL logging from jweb.data.show-sql</li>
 *   <li>Batch operation settings for better performance</li>
 * </ul>
 */
public class JWebDataConfigurer implements HibernatePropertiesCustomizer {

    private final JWebDataProperties properties;

    public JWebDataConfigurer(JWebDataProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        // Apply JWeb defaults (only if not already set)
        hibernateProperties.putIfAbsent("hibernate.hbm2ddl.auto", properties.getDdlAuto());

        if (properties.isShowSql()) {
            hibernateProperties.put("hibernate.show_sql", "true");
            hibernateProperties.put("hibernate.format_sql", "true");
        }

        // Enable batch operations for better performance
        hibernateProperties.putIfAbsent("hibernate.jdbc.batch_size", "25");
        hibernateProperties.putIfAbsent("hibernate.order_inserts", "true");
        hibernateProperties.putIfAbsent("hibernate.order_updates", "true");

        // Prevent N+1 queries with batch fetching
        hibernateProperties.putIfAbsent("hibernate.default_batch_fetch_size", "16");
    }

    public JWebDataProperties getProperties() {
        return properties;
    }
}
