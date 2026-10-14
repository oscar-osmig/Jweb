package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.time.Duration;

/**
 * Starts the {@link jweb.Jobs} worker with the application: on the SQL store
 * when {@link jweb.Sql} is connected (and {@code jweb.jobs.store} is not
 * {@code memory}), on a {@code Jobs.Store} bean when the app defines one, in
 * memory otherwise. Runs after the SQL binding, so the table is there.
 */
@Configuration
public class JobsConfiguration {

    @Value("${jweb.jobs.enabled:true}")
    private boolean enabled;

    @Value("${jweb.jobs.store:auto}")
    private String storeChoice;

    @Value("${jweb.jobs.poll:1s}")
    private Duration poll;

    @Value("${jweb.jobs.timeout:10m}")
    private Duration timeout;

    @Value("${jweb.jobs.keep-done:24h}")
    private Duration keepDone;

    @Value("${jweb.jobs.concurrency:8}")
    private int concurrency;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 120)
    public ApplicationRunner jwebJobsWorker(ObjectProvider<jweb.Jobs.Store> stores) {
        return args -> {
            if (!enabled) {
                Log.info("jweb.Jobs worker disabled (jweb.jobs.enabled=false)");
                return;
            }
            jweb.Jobs.Store custom = stores.getIfUnique();
            if (custom != null) {
                jweb.Jobs.store(custom);
            } else if (!"memory".equalsIgnoreCase(storeChoice) && jweb.Sql.isConnected()) {
                jweb.Jobs.store(new jweb.Jobs.SqlStore(jweb.Sql.database()));
            }
            jweb.Jobs.configure(poll, timeout, keepDone, concurrency);
            jweb.Jobs.start();
        };
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        jweb.Jobs.stop();
    }
}
