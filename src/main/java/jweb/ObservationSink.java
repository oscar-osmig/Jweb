package jweb;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Map;

/**
 * Carries {@link Trace} spans into Micrometer Observation: each span is an
 * {@code Observation} with the span's tags as key-values — low cardinality
 * for the tags it started with, high cardinality for the ones added later —
 * stopped when the span closes, with its error.
 *
 * <p>Bound automatically when the application has an
 * {@code ObservationRegistry} bean, which Spring Boot's actuator provides;
 * by hand with {@link #bind(ObservationRegistry)}. The registry decides what
 * an observation becomes: a timer per name in the meter registry, a span in
 * a trace when {@code micrometer-tracing} is present, both when both are.</p>
 *
 * <p>This is the only class in the framework that names a Micrometer
 * Observation type. Spring Framework itself depends on
 * {@code micrometer-observation}, so the class always loads; what it needs
 * is a registry.</p>
 */
public final class ObservationSink implements Trace.Sink {

    private final ObservationRegistry registry;

    public ObservationSink(ObservationRegistry registry) {
        this.registry = registry;
    }

    /** Attaches a registry: every span from now on becomes an observation in it. */
    public static ObservationSink bind(ObservationRegistry registry) {
        ObservationSink sink = new ObservationSink(registry);
        Trace.sink(sink);
        return sink;
    }

    /** Detaches whatever sink is attached. */
    public static void unbind() {
        Trace.sink(null);
    }

    public ObservationRegistry registry() {
        return registry;
    }

    @Override
    public Trace.Span start(String name, Map<String, String> tags) {
        Observation observation = Observation.createNotStarted(name, registry);
        tags.forEach((key, value) -> observation.lowCardinalityKeyValue(key, value == null ? "" : value));
        observation.start();
        Observation.Scope scope = observation.openScope();
        return new Trace.Span() {
            @Override
            public Trace.Span tag(String key, Object value) {
                observation.highCardinalityKeyValue(key, value == null ? "" : String.valueOf(value));
                return this;
            }

            @Override
            public Trace.Span error(Throwable error) {
                if (error != null) observation.error(error);
                return this;
            }

            @Override
            public void close() {
                scope.close();
                observation.stop();
            }

            @Override
            public String toString() {
                return "Trace.Span(" + name + ")";
            }
        };
    }

    @Override
    public String toString() {
        return "ObservationSink(" + registry.getClass().getSimpleName() + ")";
    }
}
