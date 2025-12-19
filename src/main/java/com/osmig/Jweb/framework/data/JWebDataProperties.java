package com.osmig.Jweb.framework.data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWeb data layer.
 *
 * <p>All properties are prefixed with {@code jweb.data}.</p>
 *
 * <h2>Available Properties</h2>
 * <ul>
 *   <li>{@code jweb.data.enabled} - Enable/disable auto-configuration (default: true)</li>
 *   <li>{@code jweb.data.show-sql} - Show SQL in logs (default: false)</li>
 *   <li>{@code jweb.data.ddl-auto} - Schema generation strategy (default: update)</li>
 *   <li>{@code jweb.data.h2-console} - Enable H2 web console in dev (default: true)</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * # application.properties
 * jweb.data.enabled=true
 * jweb.data.show-sql=true
 * jweb.data.ddl-auto=create-drop
 * jweb.data.h2-console=true
 * }</pre>
 */
@ConfigurationProperties(prefix = "jweb.data")
public class JWebDataProperties {

    /**
     * Whether JWeb data auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether to show SQL statements in logs.
     * Defaults to false in production, consider enabling in development.
     */
    private boolean showSql = false;

    /**
     * Hibernate DDL auto strategy.
     * Options: none, validate, update, create, create-drop
     * Defaults to 'update' which is safe for development.
     */
    private String ddlAuto = "update";

    /**
     * Whether to enable H2 console when H2 is on classpath.
     * Only applies when using H2 database.
     */
    private boolean h2Console = true;

    /**
     * Base packages to scan for entities.
     * If empty, scans from the application main class package.
     */
    private String[] entityPackages = {};

    /**
     * Base packages to scan for repositories.
     * If empty, scans from the application main class package.
     */
    private String[] repositoryPackages = {};

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public void setDdlAuto(String ddlAuto) {
        this.ddlAuto = ddlAuto;
    }

    public boolean isH2Console() {
        return h2Console;
    }

    public void setH2Console(boolean h2Console) {
        this.h2Console = h2Console;
    }

    public String[] getEntityPackages() {
        return entityPackages;
    }

    public void setEntityPackages(String[] entityPackages) {
        this.entityPackages = entityPackages;
    }

    public String[] getRepositoryPackages() {
        return repositoryPackages;
    }

    public void setRepositoryPackages(String[] repositoryPackages) {
        this.repositoryPackages = repositoryPackages;
    }
}
