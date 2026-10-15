package com.osmig.Jweb.framework.cache;

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

/**
 * Picks the shared side of {@link jweb.Cache} at startup: a
 * {@code Cache.Backend} bean when the app defines one, {@link jweb.RedisCache}
 * when {@code jweb.cache.redis.url} (or {@code jweb.redis.url}) is set, this
 * instance's memory otherwise. Ordered before the jobs and the routes, so a
 * lock or a shared value is there for the first request.
 */
@Configuration
public class CacheConfiguration {

    @Value("${jweb.cache.redis.url:${jweb.redis.url:}}")
    private String redisUrl;

    @Value("${jweb.cache.redis.prefix:jweb:cache:}")
    private String redisPrefix;

    @Value("${jweb.cache.redis.connections:4}")
    private int connections;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 108)
    public ApplicationRunner jwebCacheBackend(ObjectProvider<jweb.Cache.Backend> backends) {
        return args -> {
            jweb.Cache.Backend custom = backends.getIfUnique();
            if (custom != null) {
                jweb.Cache.backend(custom);
                Log.info("jweb.Cache shared backend: {}", custom);
            } else if (redisUrl != null && !redisUrl.isBlank()) {
                jweb.RedisCache redis = new jweb.RedisCache(redisUrl, redisPrefix, connections);
                jweb.Cache.backend(redis);
                Log.info("jweb.Cache shared backend: {} (Cache.shared, locks and RateLimit count across instances)", redis);
            } else {
                Log.debug("jweb.Cache shared backend: in-memory (this instance only)");
            }
        };
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        jweb.Cache.backend().stop();
    }
}
