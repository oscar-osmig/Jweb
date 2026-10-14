package com.osmig.Jweb.framework.db.sql;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Binds {@link jweb.Sql} to the application's {@code DataSource} bean and runs
 * the pending {@link jweb.Migrations} — which is the case as soon as an app
 * adds {@code spring-boot-starter-jdbc} and a driver and sets
 * {@code spring.datasource.url}. Nothing to configure.
 *
 * <p>The runner is ordered first so the schema is current before any
 * {@code ApplicationRunner} of the app's own touches it. An app with no
 * {@code DataSource} bean loads nothing here. With Flyway or Liquibase on the
 * classpath, Spring Boot runs that and this class leaves the schema to it.</p>
 */
@Configuration
public class SqlConfiguration {

    @Value("${jweb.sql.migrate:true}")
    private boolean migrate;

    @Value("${jweb.sql.migrations:" + jweb.Migrations.DEFAULT_LOCATION + "}")
    private String migrations;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public ApplicationRunner jwebSqlBinding(ObjectProvider<DataSource> dataSources) {
        return args -> {
            DataSource dataSource = dataSources.getIfUnique();
            if (dataSource == null) {
                return;
            }
            jweb.Sql.connect(dataSource);
            Log.info("jweb.Sql bound to {}", dataSource.getClass().getSimpleName());
            if (!migrate) {
                return;
            }
            String other = otherMigrator();
            if (other != null) {
                Log.info("{} is on the classpath; jweb.Migrations leaves the schema to it", other);
                return;
            }
            var report = jweb.Migrations.migrate(dataSource, migrations);
            if (report.count() > 0) {
                Log.info("Applied {} migration(s) from {}; {} were already applied",
                    report.count(), migrations, report.alreadyApplied());
            } else {
                Log.debug("No pending migrations at {} ({} applied)", migrations, report.alreadyApplied());
            }
        };
    }

    private static String otherMigrator() {
        for (String[] candidate : new String[][]{
            {"org.flywaydb.core.Flyway", "Flyway"},
            {"liquibase.integration.spring.SpringLiquibase", "Liquibase"}}) {
            try {
                Class.forName(candidate[0], false, SqlConfiguration.class.getClassLoader());
                return candidate[1];
            } catch (ClassNotFoundException ignored) {
                // not there
            }
        }
        return null;
    }
}
