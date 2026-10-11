package com.osmig.Jweb.framework.metrics;

import com.osmig.Jweb.framework.util.Log;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds {@link jweb.Metrics} to the application's {@code MeterRegistry} when
 * there is one — which is the case as soon as an app adds
 * {@code spring-boot-starter-actuator} and a registry such as
 * {@code micrometer-registry-prometheus}. Nothing to configure; set
 * {@code jweb.metrics.micrometer=false} to keep the two apart.
 *
 * <p>The class is only considered when {@code micrometer-core} is present,
 * so an application without it never loads a Micrometer type.</p>
 */
@Configuration
@ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(name = "jweb.metrics.micrometer", havingValue = "true", matchIfMissing = true)
public class MicrometerMetricsConfiguration {

    @Bean
    public ApplicationRunner jwebMicrometerBinding(MeterRegistry registry) {
        return args -> {
            jweb.MicrometerSink.bind(registry);
            Log.info("jweb.Metrics bound to Micrometer registry {}", registry.getClass().getSimpleName());
        };
    }
}
