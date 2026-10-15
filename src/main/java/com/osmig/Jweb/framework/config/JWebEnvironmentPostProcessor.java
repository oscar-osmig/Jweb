package com.osmig.Jweb.framework.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Defaults that have to be in place before Spring Boot initializes logging —
 * which happens before any {@code @PropertySource} (so before
 * {@code jweb.yaml}) is read.
 *
 * <p>{@code logging.pattern.correlation}: the request id (and the trace id,
 * when tracing is present) on every console and file line written during
 * a request, nothing on the lines written outside one. An app that sets the
 * property itself wins; {@code jweb.observability.log-correlation: false}
 * leaves Spring Boot's own default.</p>
 */
public class JWebEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** The source's name, visible in {@code /actuator/env}. */
    public static final String SOURCE = "jwebDefaults";

    /**
     * {@code [id] } when there is a request id, {@code [id trace] } when there
     * is also a trace id, {@code [trace] } with only a trace, nothing
     * otherwise. One Logback {@code %replace} drops the empty brackets and
     * the space next to a missing half.
     */
    public static final String CORRELATION_PATTERN =
        "%replace([%X{requestId:-} %X{traceId:-}] ){'^\\[ \\] $|(?<=\\[) | (?=\\])', ''}";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean wanted = Boolean.parseBoolean(environment.getProperty("jweb.observability.log-correlation", "true"));
        if (!wanted || environment.getPropertySources().contains(SOURCE)) return;
        environment.getPropertySources().addLast(new MapPropertySource(SOURCE, Map.of(
            "logging.pattern.correlation", CORRELATION_PATTERN
        )));
    }
}
