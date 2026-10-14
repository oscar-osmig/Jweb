package jweb;

import com.osmig.Jweb.framework.db.sql.SqlConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The startup wiring: a {@code DataSource} bean binds {@link Sql} and runs the
 * pending migrations; no bean, nothing happens.
 */
class SqlConfigurationTest {

    @Configuration
    static class WithDataSource {
        @Bean
        DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:sqlconfig;DB_CLOSE_DELAY=-1");
            return ds;
        }
    }

    @AfterEach
    void unbind() {
        Sql.disconnect();
    }

    @Test
    void aDataSourceBeanBindsSqlAndMigrates() {
        new ApplicationContextRunner()
            .withUserConfiguration(WithDataSource.class, SqlConfiguration.class)
            .withPropertyValues("jweb.sql.migrations=classpath:db/test-migration")
            .run(context -> {
                run(context.getBean("jwebSqlBinding", ApplicationRunner.class));
                assertTrue(Sql.isConnected());
                assertEquals(1L, Sql.query("select count(*) from notes").scalar(Long.class));
                assertEquals(2, Migrations.applied(context.getBean(DataSource.class)).size());
                Sql.execute("drop table notes");
                Sql.execute("drop table " + Migrations.TABLE);
            });
    }

    @Test
    void migrationsCanBeTurnedOff() {
        new ApplicationContextRunner()
            .withUserConfiguration(WithDataSource.class, SqlConfiguration.class)
            .withPropertyValues("jweb.sql.migrations=classpath:db/test-migration", "jweb.sql.migrate=false")
            .run(context -> {
                run(context.getBean("jwebSqlBinding", ApplicationRunner.class));
                assertTrue(Sql.isConnected());
                assertTrue(Migrations.applied(context.getBean(DataSource.class)).isEmpty());
            });
    }

    @Test
    void noDataSourceBeanMeansNothingIsBound() {
        new ApplicationContextRunner()
            .withUserConfiguration(SqlConfiguration.class)
            .run(context -> {
                run(context.getBean("jwebSqlBinding", ApplicationRunner.class));
                assertFalse(Sql.isConnected());
            });
    }

    private static void run(ApplicationRunner runner) throws Exception {
        ApplicationArguments none = new DefaultApplicationArguments();
        runner.run(none);
    }
}
