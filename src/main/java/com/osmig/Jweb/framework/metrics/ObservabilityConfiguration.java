package com.osmig.Jweb.framework.metrics;

import com.osmig.Jweb.framework.server.RequestIdFilter;
import com.osmig.Jweb.framework.util.Log;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Request ids and traces.
 *
 * <ul>
 *   <li>{@link RequestIdFilter}, first in the chain, unless
 *       {@code jweb.observability.request-id} is false.</li>
 *   <li>{@link jweb.Trace} bound to the application's
 *       {@code ObservationRegistry} when it has one (Spring Boot's actuator
 *       provides it; with {@code micrometer-tracing} the spans join the
 *       request's trace), unless {@code jweb.observability.tracing} is false.
 *       The console pattern that prints the request id on every line is set
 *       by {@code JWebEnvironmentPostProcessor}, before logging starts.</li>
 * </ul>
 */
@Configuration
public class ObservabilityConfiguration {

    @Bean
    @ConditionalOnProperty(name = "jweb.observability.request-id", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RequestIdFilter> jwebRequestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>(new RequestIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("jwebRequestId");
        return registration;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 90)
    @ConditionalOnProperty(name = "jweb.observability.tracing", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner jwebTraceBinding(ObjectProvider<ObservationRegistry> registries,
                                              ObjectProvider<jweb.Trace.Sink> sinks) {
        return args -> {
            jweb.Trace.Sink custom = sinks.getIfUnique();
            if (custom != null) {
                jweb.Trace.sink(custom);
                Log.info("jweb.Trace sink: {}", custom);
                return;
            }
            ObservationRegistry registry = registries.getIfUnique();
            if (registry == null || registry.isNoop()) {
                Log.debug("jweb.Trace: no ObservationRegistry; spans are dropped (add spring-boot-starter-actuator)");
                return;
            }
            jweb.ObservationSink.bind(registry);
            Log.info("jweb.Trace bound to Micrometer Observation ({})", registry.getClass().getSimpleName());
        };
    }
}
