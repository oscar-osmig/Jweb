package com.osmig.Jweb.framework.pubsub;

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
 * Picks the {@link jweb.PubSub} backend at startup: a {@code PubSub.Backend}
 * bean when the app defines one, Redis when {@code jweb.pubsub.redis.url} is
 * set, the in-memory backend otherwise. Nothing to configure for one
 * instance; one property for several.
 */
@Configuration
public class PubSubConfiguration {

    @Value("${jweb.pubsub.redis.url:${jweb.redis.url:}}")
    private String redisUrl;

    @Value("${jweb.pubsub.redis.prefix:jweb:}")
    private String redisPrefix;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 110)
    public ApplicationRunner jwebPubSubBackend(ObjectProvider<jweb.PubSub.Backend> backends) {
        return args -> {
            jweb.PubSub.Backend custom = backends.getIfUnique();
            if (custom != null) {
                jweb.PubSub.backend(custom);
                Log.info("jweb.PubSub backend: {}", custom);
            } else if (redisUrl != null && !redisUrl.isBlank()) {
                jweb.RedisPubSub redis = new jweb.RedisPubSub(redisUrl, redisPrefix);
                jweb.PubSub.backend(redis);
                Log.info("jweb.PubSub backend: {}", redis);
            } else {
                Log.debug("jweb.PubSub backend: in-memory (this instance only)");
            }
        };
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        jweb.PubSub.Backend backend = jweb.PubSub.backend();
        if (backend != null) backend.stop();
    }
}
