package jweb;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Mirrors everything {@link Metrics} records into a Micrometer
 * {@link MeterRegistry}, so the framework's counters, gauges and timers reach
 * whatever the registry exports to — Prometheus, Datadog, OTLP, CloudWatch.
 *
 * <p>Bound automatically when the application has a {@code MeterRegistry} bean
 * ({@link MicrometerMetricsConfiguration}); by hand with
 * {@link #bind(MeterRegistry)}. Names are passed through unchanged
 * ({@code http.requests.total}, {@code orders.placed}), which is Micrometer's
 * own convention; each registry renders them in its format.</p>
 *
 * <p>This is the only class in the framework that names a Micrometer type.
 * {@code micrometer-core} is an optional dependency: without it nothing loads
 * this class, and {@code jweb.Metrics} works as before.</p>
 */
public final class MicrometerSink implements Metrics.Sink {

    private final MeterRegistry registry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public MicrometerSink(MeterRegistry registry) {
        this.registry = registry;
    }

    /** Attaches a registry: recordings from now on, and the gauges already registered, reach it. */
    public static MicrometerSink bind(MeterRegistry registry) {
        MicrometerSink sink = new MicrometerSink(registry);
        Metrics.sink(sink);
        return sink;
    }

    /** Detaches whatever sink is attached. */
    public static void unbind() {
        Metrics.sink(null);
    }

    public MeterRegistry registry() {
        return registry;
    }

    @Override
    public void counter(String name, long delta) {
        counters.computeIfAbsent(name, n -> Counter.builder(n)
                .description("jweb.Metrics counter " + n)
                .register(registry))
            .increment(delta);
    }

    @Override
    public void gauge(String name, Supplier<Number> value) {
        // strongReference: the supplier is the only thing holding the state; a
        // weakly held gauge would silently stop reporting after a GC.
        Gauge.builder(name, value, s -> {
                Number n = s.get();
                return n == null ? Double.NaN : n.doubleValue();
            })
            .description("jweb.Metrics gauge " + name)
            .strongReference(true)
            .register(registry);
    }

    @Override
    public void timer(String name, long nanos) {
        timers.computeIfAbsent(name, n -> Timer.builder(n)
                .description("jweb.Metrics timer " + n)
                .register(registry))
            .record(nanos, TimeUnit.NANOSECONDS);
    }
}
