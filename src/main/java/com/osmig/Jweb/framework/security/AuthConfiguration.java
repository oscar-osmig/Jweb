package com.osmig.Jweb.framework.security;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Wires {@link jweb.Auth} at startup: the signing secret
 * ({@code jweb.auth.secret}), whether remember-me cookies are {@code Secure}
 * ({@code jweb.auth.remember.secure}), and the session-versions store — a
 * {@code Auth.Versions} bean when the app defines one, the
 * {@code jweb_auth_versions} table when {@link jweb.Sql} is connected, memory
 * otherwise. Runs after the SQL binding.
 */
@Configuration
public class AuthConfiguration {

    @Value("${jweb.auth.secret:}")
    private String secret;

    @Value("${jweb.auth.remember.secure:false}")
    private boolean rememberSecure;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 105)
    public ApplicationRunner jwebAuthSetup(ObjectProvider<Auth.Versions> versions) {
        return args -> {
            jweb.Auth.secret(secret);
            jweb.Auth.rememberSecure(rememberSecure);
            Auth.Versions custom = versions.getIfUnique();
            if (custom != null) {
                jweb.Auth.versions(custom);
                Log.info("jweb.Auth session versions: {}", custom.getClass().getSimpleName());
            } else if (jweb.Sql.isConnected()) {
                jweb.Auth.versions(new jweb.Auth.SqlVersions(jweb.Sql.database()));
                Log.info("jweb.Auth session versions: {} table", jweb.Auth.SqlVersions.TABLE);
            }
        };
    }
}
